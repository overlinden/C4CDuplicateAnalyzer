package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class DuplicateToFileWriter implements ItemStreamWriter<Duplicate> {

    @Value("#{jobParameters[OutputFileName]}")
    private String outputFileName;

    private FlatFileItemWriter<Duplicate> writer;

    @PostConstruct
    private void init() {
        writer = new FlatFileItemWriterBuilder<Duplicate>()
                .resource(new FileSystemResource(outputFileName))
                .encoding(StandardCharsets.UTF_8.name())
                .lineSeparator("\r\n\r\n-----\r\n\r\n")
                .delimited()
                .delimiter("\r\n")
                .fieldExtractor(new BeanWrapperFieldExtractor<Duplicate>() {
                    {
                        setNames(new String[]{"a", "b", "similarity"});
                    }
                })
                .name("duplicateFileWrite")
                .build();
    }

    @Override
    public void write(List<? extends Duplicate> items) throws Exception {
        writer.write(items);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        writer.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        writer.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        writer.close();
    }
}
