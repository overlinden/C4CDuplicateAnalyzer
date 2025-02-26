package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.repository.DuplicateRepository;
import de.wpsverlinden.c4cduplicateanalyzer.repository.AccountRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;


public class DatabaseCleanupTasklet implements Tasklet {
    
    @Autowired
    private AccountRepository accountRepo;
    
    @Autowired
    private DuplicateRepository duplicateRepo;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        duplicateRepo.deleteAll();
        accountRepo.deleteAll();
        return RepeatStatus.FINISHED;
    }
    
}
