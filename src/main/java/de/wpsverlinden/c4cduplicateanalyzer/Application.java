package de.wpsverlinden.c4cduplicateanalyzer;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration.JobConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job duplicateJob;

    @Autowired
    private ApplicationConfiguration config;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        for (JobConfig j : config.getJobs()) {
            JobParameters params = new JobParametersBuilder()
                    .addString("CountryCode", j.getCountryCode())
                    .addString("Roles", j.getRoles())
                    .addDouble("Threshold", (double)j.getThreshold())
                    .addString("OutputFileName", j.getOutputFileName())
                    .toJobParameters();
            jobLauncher.run(duplicateJob, params);
        }
    }

}
