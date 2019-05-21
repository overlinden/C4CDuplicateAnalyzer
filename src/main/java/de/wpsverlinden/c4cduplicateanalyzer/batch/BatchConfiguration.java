package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import de.wpsverlinden.c4cduplicateanalyzer.persistence.DuplicateRepository;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort.Direction;

@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ClearRepo clearRepo;

    @Autowired
    private ItemStreamReader<List<Account>> accountListReader;

    @Autowired
    private ItemProcessor<List<Account>, List<Duplicate>> duplicateFinder;

    @Autowired
    private DuplicateToFileWriter duplicateToFileWriter;

    @Autowired
    private DuplicateToDatebaseWriter duplicateToDatabaseWriter;

    @Autowired
    private DuplicateRepository repo;
    
    @Autowired
    private ProgressLogger progressLogger;

    @Autowired
    private ApplicationConfiguration config;

    @Bean
    public Job duplicateJob() {
        return jobBuilderFactory.get("DuplicateJob")
                .incrementer(new RunIdIncrementer())
                .start(cleanupStep())
                .next(accountDuplicateStep())
                .next(databaseToFileStep())
                .preventRestart()
                .build();
    }

    @Bean
    public Step cleanupStep() {
        return stepBuilderFactory.get("cleanupStep")
                .tasklet(clearRepo)
                .build();
    }

    @Bean
    public Step accountDuplicateStep() {
        return stepBuilderFactory.get("AccountDuplicateStep")
                .<List<Account>, List<Duplicate>>chunk(config.getChunkSize())
                .reader(syncAccountListReader())
                .processor(duplicateFinder)
                .writer(duplicateToDatabaseWriter)
                .taskExecutor(executor())
                .listener(progressLogger)
                .build();
    }

    @Bean
    public Step databaseToFileStep() {
        return stepBuilderFactory.get("AccountDuplicateStep")
                .<Duplicate, Duplicate>chunk(config.getChunkSize())
                .reader(duplicateDatabaseReader())
                .writer(duplicateToFileWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<Duplicate> duplicateDatabaseReader() {
        LinkedHashMap<String, Direction> sorts = new LinkedHashMap<>();
        sorts.put("similarity", Direction.DESC);
        sorts.put("a", Direction.ASC);
        sorts.put("b", Direction.ASC);
        return new RepositoryItemReaderBuilder<Duplicate>()
                .repository(repo)
                .methodName("findAll")
                .sorts(sorts)
                .name("duplicateDatabaseReader")
                .build();
    }

    @Bean
    public TaskExecutor executor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("worker_");
        executor.setConcurrencyLimit(Runtime.getRuntime().availableProcessors());
        return executor;
    }
    
    @Bean
    public SynchronizedItemStreamReader<List<Account>> syncAccountListReader() {
        return new SynchronizedItemStreamReaderBuilder<List<Account>>().delegate(accountListReader).build();
    }
}