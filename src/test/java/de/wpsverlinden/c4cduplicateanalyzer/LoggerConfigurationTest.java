package de.wpsverlinden.c4cduplicateanalyzer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LoggerConfiguration.class})
public class LoggerConfigurationTest {
    
    @Autowired
    private Logger LOG;
    
    @Test
    public void loggerIsNotNull() {
        assertThat(LOG).isNotNull();
    }
    
    @Test
    public void loggerHasCorrectName() {
        assertThat(LOG.getName()).isEqualTo(LoggerConfigurationTest.class.getName());
    }
}
