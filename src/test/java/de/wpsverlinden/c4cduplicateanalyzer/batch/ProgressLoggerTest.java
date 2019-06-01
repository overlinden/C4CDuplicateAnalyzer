package de.wpsverlinden.c4cduplicateanalyzer.batch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

@RunWith(MockitoJUnitRunner.class)
public class ProgressLoggerTest {

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    @Mock
    private StepExecution stepExecution;

    @Mock
    private Logger LOG;

    @InjectMocks
    private ProgressLogger progressLogger;

    @Test
    public void logIsWritterAfterChunkCompletion() {
        Mockito.when(chunkContext.getStepContext()).thenReturn(stepContext);
        Mockito.when(stepContext.getStepExecution()).thenReturn(stepExecution);
        Mockito.when(stepExecution.getReadCount()).thenReturn(1);
        Mockito.when(stepExecution.getWriteCount()).thenReturn(2);

        progressLogger.afterChunk(chunkContext);

        Mockito.verify(LOG, times(1)).info(Mockito.eq("{} accounts processed. {} duplicate \"packages\" found."), Mockito.eq(1), Mockito.eq(2));
    }
}
