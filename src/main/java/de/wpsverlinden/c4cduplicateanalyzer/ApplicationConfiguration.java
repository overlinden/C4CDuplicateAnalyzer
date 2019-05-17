
package de.wpsverlinden.c4cduplicateanalyzer;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("c4cduplicateanalyzer")
@Data
public class ApplicationConfiguration {
    
    private List<JobConfig> jobs = new ArrayList<>();
    private String endpoint;
    private String user;
    private String password;
    private int chunkSize;
    
    @Data
    public static class JobConfig {
        private String countryCode;
        private String roles;
        private String outputFileName;
        private float threshold;
    }
}
