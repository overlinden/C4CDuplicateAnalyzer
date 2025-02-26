package de.wpsverlinden.c4cduplicateanalyzer.batch;

import org.slf4j.Logger;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ChunkLogger implements ChunkListener{

    @Autowired
    private Logger LOG;

    @Override
    public void afterChunk(ChunkContext context) {
        final String stepName = context.getStepContext().getStepExecution().getStepName();
        //final long readCount = context.getStepContext().getStepExecution().getReadCount();
        //final long writeCount = context.getStepContext().getStepExecution().getWriteCount();
        final long totalPairs = context.getStepContext().getStepExecution().getExecutionContext().getLong("TotalPairs", 0);
        final long potentialDuplicates = context.getStepContext().getStepExecution().getExecutionContext().getLong("PotentialDuplicates", 0);
        final long accountCount = context.getStepContext().getStepExecution().getExecutionContext().getLong("AccountCountOld", 0);
        final float progress = ((float)accountCount / (float)totalPairs) * 100;
        LOG.info("{}: Progress: {}%, Potential duplicates: {}", stepName, String.format("%.1f",progress), potentialDuplicates);
    }
}