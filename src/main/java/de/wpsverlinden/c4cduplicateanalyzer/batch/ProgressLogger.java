package de.wpsverlinden.c4cduplicateanalyzer.batch;

import org.slf4j.Logger;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProgressLogger implements ChunkListener {

    @Autowired
    private Logger LOG;

    @Override
    public void beforeChunk(ChunkContext context) {

    }

    @Override
    public void afterChunk(ChunkContext context) {
        int read = context.getStepContext().getStepExecution().getReadCount();
        int write = context.getStepContext().getStepExecution().getWriteCount();
        LOG.info("{} accounts processed. {} duplicate \"packages\" found.", read, write);
    }

    @Override
    public void afterChunkError(ChunkContext context) {

    }
}