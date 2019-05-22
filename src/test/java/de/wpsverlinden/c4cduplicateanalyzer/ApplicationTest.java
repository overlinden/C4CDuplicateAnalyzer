package de.wpsverlinden.c4cduplicateanalyzer;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration.JobConfig;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import static org.assertj.core.api.Assertions.*;
import org.mockito.Captor;
import org.springframework.batch.core.JobParametersBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {
    
    @Mock
    private ApplicationConfiguration config;
    
    @Mock
    private JobLauncher jobLauncher;
    
    @InjectMocks
    private Application app;
    
    @Captor
    private ArgumentCaptor<JobParameters> argument;
    
    @Test
    public void jobParametersAreCorrectlySetFromApplicationConfiguration() throws Exception {

        JobConfig job = new JobConfig();
        job.setCountryCode("DE");
        job.setRoles("R1");
        job.setThreshold(0.75f);
        job.setOutputFileName("out.file");
        Mockito.when(config.getJobs()).thenReturn(Collections.singletonList(job));
        
        app.run();

        Mockito.verify(jobLauncher).run(Mockito.any(), argument.capture());
                    JobParameters expectedParams = new JobParametersBuilder()
                    .addString("CountryCode", "DE")
                    .addString("Roles", "R1")
                    .addDouble("Threshold", 0.75d)
                    .addString("OutputFileName", "out.file")
                    .toJobParameters();
        assertThat(argument.getValue()).isEqualTo(expectedParams);
    }
    
    @Test
    public void multipleJobsAreExecuted() throws Exception {

        JobConfig job = new JobConfig();
        Mockito.when(config.getJobs()).thenReturn(Stream.of(job, job).collect(Collectors.toList()));
        
        app.run();

        Mockito.verify(jobLauncher, times(2)).run(Mockito.any(), Mockito.any());
    }
}
