package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import de.wpsverlinden.c4cduplicateanalyzer.LevenshteinCalculator;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class DuplicateFinder implements ItemProcessor<List<Account>, List<Duplicate>> {

    @Autowired
    private Logger LOG;

    @Autowired
    private LevenshteinCalculator calculator;
    
    @Value("#{jobParameters[Threshold]}")
    private float threshold;
    
    @Override
    public List<Duplicate> process(List<Account> accounts) throws Exception {
        Account a = accounts.get(0);
        List<Account> accntsToCompare = accounts.subList(1, accounts.size());
        List<Duplicate> result = accntsToCompare.parallelStream()
                .map(b -> {
                    return Duplicate.builder()
                            .a(a.toString())
                            .b(b.toString())
                            .similarity(calculator.getSimilarity(a, b, threshold))
                            .build();
                })
                .peek(r -> LOG.debug(r.toString()))
                .filter(r -> r.getSimilarity() >= threshold)
                .collect(Collectors.toList());
        if (result.size() >= 1) {
            return result;
        } else {
            return null;
        }
    }
}
