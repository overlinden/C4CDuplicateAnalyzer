package de.wpsverlinden.c4cduplicateanalyzer.batch;

import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

public class StepLogger implements StepExecutionListener {

    @Autowired
    private Logger LOG;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        final String stepName = stepExecution.getStepName();
        final long readCount = stepExecution.getReadCount();
        final long writeCount = stepExecution.getWriteCount();
        LOG.info("{}: Total read count: {}, Total write count: {}", stepName, readCount, writeCount);
        return stepExecution.getExitStatus();
    }
}
