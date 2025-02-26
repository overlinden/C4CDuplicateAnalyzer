package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import de.wpsverlinden.c4cduplicateanalyzer.repository.AccountRepository;
import de.wpsverlinden.c4cduplicateanalyzer.repository.DuplicateRepository;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationConfiguration config;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private DuplicateRepository duplicateRepo;

    @Autowired
    private DataSource datasource;

    @Bean
    public Job duplicateJob() {
        return new JobBuilder("DuplicateJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(databaseCleanupStep())
                .next(accountDownloadStep())
                .next(duplicateFinderStep())
                .next(duplicateToFileExportStep())
                .preventRestart()
                .build();
    }

    @Bean
    public Step databaseCleanupStep() {
        return new StepBuilder("DatabaseCleanupStep", jobRepository)
                .tasklet(databaseCleanupTasklet(), transactionManager)
                .build();
    }
    
    @Bean
    public DatabaseCleanupTasklet databaseCleanupTasklet() {
        return new DatabaseCleanupTasklet();
    }
    
    @Bean
    public ChunkListener chunkLogger() {
        return new ChunkLogger();
    }
    
    @Bean
    public StepExecutionListener stepLogger() {
        return new StepLogger();
    }

    // - - - - - - - - STEP 1: Download and store locally - - - - - - - - - - - - 
    @Bean
    public Step accountDownloadStep() {
        return new StepBuilder("AccountDownloadStep", jobRepository)
                .<Account, Account>chunk(config.getDownloadChunkSize(), transactionManager)
                .reader(accountDownloadReader())
                .processor(accountDownloadProcessor())
                .writer(accountToDatabaseWriter())
                .listener(stepLogger())
                .build();
    }

    @Bean
    @StepScope
    public AccountDownloadReader accountDownloadReader() {
        return new AccountDownloadReader();
    }
    
    @Bean
    public AccountDownloadProcessor accountDownloadProcessor() {
        return new AccountDownloadProcessor();
    }

    @Bean
    public ItemWriter<Account> accountToDatabaseWriter() {
        RepositoryItemWriter<Account> writer = new RepositoryItemWriter<>();
        writer.setRepository(accountRepo);
        return writer;
    }

    // - - - - - - - - STEP 2: Check for duplicates - - - - - - - - - - - - 
    @Bean
    public Step duplicateFinderStep() {
        //At the moment the throttleLimit is defaulted to 4. There is no valid workaround for that. As workaround I use:
        //(1) no parallelizing task executor in the step configuration,
        //(2) chunk size of 1 from spring batch point of view
        //(3) custom chunking within itemreader, parallelization in item processor, serializatiion in item writer.
        return new StepBuilder("DuplicateFinderStep", jobRepository)
//                .<Pair<Account, Account>, Duplicate>chunk(config.getProcessorChunkSize(), transactionManager)
                .<List<Pair<Account, Account>>, List<Duplicate>>chunk(1, transactionManager)
//                .taskExecutor(taskExecutor())
                .reader(syncAccountPairGeneratorFromDatabaseReader())
                .processor(duplicateFinder())
                .writer(duplicateToDatabaseWriter())
                .listener(chunkLogger())
                .listener(stepLogger())
                .listener((StepExecutionListener)accountPairGeneratorFromDatabaseReader())
                .build();

    }

    @Bean
    public SynchronizedItemStreamReader<List<Pair<Account, Account>>> syncAccountPairGeneratorFromDatabaseReader() {
        return new SynchronizedItemStreamReaderBuilder()
                .delegate(accountPairGeneratorFromDatabaseReader())
                .build();

    }
    
    @Bean
    public ItemStreamReader<List<Pair<Account, Account>>> accountPairGeneratorFromDatabaseReader() {
        return new AccountPairGeneratorFromDatabaseReader();
    }
    
    @Bean
    @StepScope
    public DuplicateFinderProcessor duplicateFinder() {
        return new DuplicateFinderProcessor();
    }
    
//    @Bean
//    public TaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
//        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
//        executor.setThreadNamePrefix("worker-");
//        executor.setDaemon(true);
//        executor.afterPropertiesSet();
//        return executor;
//    }
    
    @Bean
    public ItemWriter<List<Duplicate>> duplicateToDatabaseWriter() {
        //RepositoryItemWriter<Duplicate> writer = new RepositoryItemWriter<>();
        //writer.setRepository(duplicateRepo);
        //return writer;
        return new DuplicateListToDatebaseWriter();
    }

    // - - - - - - - - STEP 3: Duplicate to File export - - - - - - - - - - - - 
    @Bean
    public Step duplicateToFileExportStep() {
        return new StepBuilder("DuplicateToFileExportStep", jobRepository)
                .<Pair<Pair<Account, Account>, Float>, Pair<Pair<Account, Account>, Float>>chunk(config.getExportChunkSize(), transactionManager)
                .reader(duplicateFromDatabaseReader())
                .writer(duplicateToFileWriter(null))
                .listener(stepLogger())
                .build();

    }

    private ItemReader<Pair<Pair<Account, Account>, Float>> duplicateFromDatabaseReader() {
        JdbcCursorItemReader<Pair<Pair<Account, Account>, Float>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(datasource);
        reader.setSaveState(true);
        reader.setSql("SELECT a.*, b.*, d.SIMILARITY "
                + "FROM DUPLICATE d "
                + "LEFT JOIN ACCOUNT a ON a.ACCOUNT_ID = d.ACCOUNT1_ID "
                + "LEFT JOIN ACCOUNT b ON b.ACCOUNT_ID = d.ACCOUNT2_ID "
                + "ORDER BY d.SIMILARITY DESC;");
        reader.setRowMapper(new RowMapper<Pair<Pair<Account, Account>, Float>>() {
            @Override
            public Pair<Pair<Account, Account>, Float> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Account a = Account.builder()
                        .AccountID(rs.getInt(1))
                        .Name(rs.getString(2))
                        .AdditionalName(rs.getString(3))
                        .AdditionalName2(rs.getString(4))
                        .AdditionalName3(rs.getString(5))
                        .Phone(rs.getString(6))
                        .Email(rs.getString(7))
                        .Fax(rs.getString(8))
                        .Mobile(rs.getString(9))
                        .WebSite(rs.getString(10))
                        .City(rs.getString(11))
                        .CountryCode(rs.getString(12))
                        .StateCode(rs.getString(13))
                        .District(rs.getString(14))
                        .Street(rs.getString(15))
                        .HouseNumber(rs.getString(16))
                        .StreetPostalCode(rs.getString(17))
                        .ErpID(rs.getString(18))
                        .CreationDate(rs.getDate(19))
                        .build();
                Account b = Account.builder()
                        .AccountID(rs.getInt(21))
                        .Name(rs.getString(22))
                        .AdditionalName(rs.getString(23))
                        .AdditionalName2(rs.getString(24))
                        .AdditionalName3(rs.getString(25))
                        .Phone(rs.getString(26))
                        .Email(rs.getString(27))
                        .Fax(rs.getString(28))
                        .Mobile(rs.getString(29))
                        .WebSite(rs.getString(30))
                        .City(rs.getString(31))
                        .CountryCode(rs.getString(32))
                        .StateCode(rs.getString(33))
                        .District(rs.getString(34))
                        .Street(rs.getString(35))
                        .HouseNumber(rs.getString(36))
                        .StreetPostalCode(rs.getString(37))
                        .ErpID(rs.getString(38))
                        .CreationDate(rs.getDate(39))
                        .build();
                return Pair.of(Pair.of(a, b), rs.getFloat(41));
            }
        });
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Pair<Pair<Account, Account>, Float>> duplicateToFileWriter(@Value("#{jobParameters[OutputFileName]}") String outputFileName) {
        return new FlatFileItemWriterBuilder<Pair<Pair<Account, Account>, Float>>()
                .resource(new FileSystemResource(outputFileName))
                .encoding(StandardCharsets.UTF_8.name())
                .headerCallback((writer) -> {
                    writer.write("1_AccountID|1_Name|1_AdditionalName|1_AdditionalName2|1_AdditionalName3|1_Phone|1_Email|1_Fax|1_Mobile|1_WebSite|1_City|1_CountryCode|1_StateCode|1_District|1_Street|1_HouseNumber|1_StreetPostalCode|1_ErpID|1_CreationOn|2_AccountID|2_Name|2_AdditionalName|2_AdditionalName2|2_AdditionalName3|2_Phone|2_Email|2_Fax|2_Mobile|2_WebSite|2_City|2_CountryCode|2_StateCode|2_District|1_Street|2_HouseNumber|2_StreetPostalCode|1_ErpID|1_CreationOn|Similiarity");
                })
                .lineSeparator("\r\n")
                .name("duplicateToFileWriter")
                .delimited()
                .delimiter("|")
                .fieldExtractor(new FieldExtractor<Pair<Pair<Account, Account>, Float>>() {
                    @Override
                    public Object[] extract(Pair<Pair<Account, Account>, Float> item) {
                        Account a = item.getFirst().getFirst();
                        Account b = item.getFirst().getSecond();
                        float similarity = item.getSecond();
                        return new Object[]{a.getAccountID(), a.getName(), a.getAdditionalName(), a.getAdditionalName2(), a.getAdditionalName3(), a.getPhone(), a.getEmail(), a.getFax(), a.getMobile(), a.getWebSite(), a.getCity(), a.getCountryCode(), a.getStateCode(), a.getDistrict(), a.getStreet(), a.getHouseNumber(), a.getStreetPostalCode(), a.getErpID(), a.getCreationDate(),
                            b.getAccountID(), b.getName(), b.getAdditionalName(), b.getAdditionalName2(), b.getAdditionalName3(), b.getPhone(), b.getEmail(), b.getFax(), b.getMobile(), b.getWebSite(), b.getCity(), b.getCountryCode(), b.getStateCode(), b.getDistrict(), b.getStreet(), b.getHouseNumber(), b.getStreetPostalCode(), b.getErpID(), b.getCreationDate(),
                            similarity};
                    }
                })
                .build();
    }
}
