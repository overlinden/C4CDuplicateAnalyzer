package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.LevenshteinCalculator;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;

//At the moment the throttleLimit is defaulted to 4. There is no valid workaround for that. As workaround I use:
//(1) no parallelizing task executor in the step configuration,
//(2) chunk size of 1 from spring batch point of view
//(3) custom chunking within itemreader, parallelization in item processor, serializatiion in item writer.

//public class DuplicateFinderProcessor implements ItemProcessor<Pair<Account, Account>>, Duplicate> {
public class DuplicateFinderProcessor implements ItemProcessor<List<Pair<Account, Account>>, List<Duplicate>>, StepExecutionListener {

    @Autowired
    private Logger LOG;

    @Autowired
    private LevenshteinCalculator calculator;

    @Value("#{jobParameters[Threshold]}")
    private float threshold;
    
    private StepExecution stepExecution;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
    
    @Override
    public List<Duplicate> process(List<Pair<Account, Account>> accounts) throws Exception {
        List<Duplicate> duplicates = accounts.parallelStream()
                .map((p) -> {
                    final Account first = p.getFirst();
                    final Account second = p.getSecond();
                    float similarity = calculator.getSimilarity(first.getSerialData(), second.getSerialData(), threshold);
                    if (similarity >= threshold) {
                        LOG.debug("Account1: {}, Account2: {}, Similarity: {}, Return: Duplicate",
                                first.getAccountID(), second.getAccountID(), similarity);
                        return Duplicate.builder()
                                .Account1Id(first.getId())
                                .Account2Id(second.getId())
                                .Similarity(similarity)
                                .build();
                    } else {
                        LOG.debug("Account1: {}, Account2: {}, Similarity: {}, Return: null",
                                first.getAccountID(), second.getAccountID(), similarity);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                //To be replaced by the ChunkLogger when real multithreading is functional.
                long potentialDuplicatesOld = stepExecution.getExecutionContext().getLong("PotentialDuplicates", 0);
                stepExecution.getExecutionContext().putLong("PotentialDuplicates", potentialDuplicatesOld + duplicates.size());
                long accountCountOld = stepExecution.getExecutionContext().getLong("AccountCountOld", 0);
                stepExecution.getExecutionContext().putLong("AccountCountOld", accountCountOld + accounts.size());
        return duplicates;
    }
    
//    @Override
//    public Duplicate process(Pair<Account, Account> accounts) throws Exception {
//        final Account first = accounts.getFirst();
//        final Account second = accounts.getSecond();
//        float similarity = calculator.getSimilarity(first.getSerialData(), second.getSerialData(), threshold);
//        if (similarity >= threshold) {
//            LOG.debug("Account1: {}, Account2: {}, Similarity: {}, Return: Duplicate",
//                    first.getAccountID(), second.getAccountID(), similarity);
//            return Duplicate.builder()
//                    .Account1Id(first.getId())
//                    .Account2Id(second.getId())
//                    .Similarity(similarity)
//                    .build();
//        } else {
//            LOG.debug("Account1: {}, Account2: {}, Similarity: {}, Return: null",
//                    first.getAccountID(), second.getAccountID(), similarity);
//            return null;
//        }
//    }
}