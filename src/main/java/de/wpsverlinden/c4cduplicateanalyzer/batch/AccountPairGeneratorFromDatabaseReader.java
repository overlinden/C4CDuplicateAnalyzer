package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import de.wpsverlinden.c4cduplicateanalyzer.repository.AccountRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

//At the moment the throttleLimit is defaulted to 4. There is no valid workaround for that. As workaround I use:
//(1) no parallelizing task executor in the step configuration,
//(2) chunk size of 1 from spring batch point of view
//(3) custom chunking within itemreader, parallelization in item processor, serializatiion in item writer.
//public class AccountListGeneratorFromDatabaseReader implements ItemStreamReader<Pair<Account, Account>> {
public class AccountPairGeneratorFromDatabaseReader implements ItemStreamReader<List<Pair<Account, Account>>>, StepExecutionListener {

    @Autowired
    private Logger LOG;

    @Autowired
    private ApplicationConfiguration config;

    @Autowired
    private AccountRepository accountRepo;

    private ArrayList<Account> accounts;

    private int outerIndex = 0;
    private int innerIndex = 1;

    private StepExecution stepExecution;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        final long totalCount = accountRepo.count();
        accounts = new ArrayList<>((int) totalCount);
        accountRepo.findAll().forEach(accounts::add);
        accounts.sort(Comparator.comparing(Account::getAccountID));
        stepExecution.getExecutionContext().putLong("TotalPairs", (long)(0.5 * totalCount * totalCount));
    }
//    @Override
//    public Pair<Account, Account> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

    public Pair<Account, Account> readNext() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (innerIndex >= accounts.size()) {
            outerIndex++;
            innerIndex = outerIndex + 1;
        }

        if (outerIndex >= accounts.size() - 1) {
            return null;
        }
        LOG.debug("Reading pair OuterIndex: {}, InnerIndex: {}.", outerIndex, innerIndex);
        Pair<Account, Account> result = Pair.of(accounts.get(outerIndex), accounts.get(innerIndex));
        innerIndex++;
        return result;
    }

    @Override
    public List<Pair<Account, Account>> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        List<Pair<Account, Account>> nextChunk = new ArrayList<>(config.getProcessorChunkSize());
        for (int i = 0; i < config.getProcessorChunkSize(); i++) {
            Pair<Account, Account> nextPair = readNext();
            if (nextPair == null && nextChunk.isEmpty()) {
                return null;
            } else if (nextPair == null && !nextChunk.isEmpty()) {
                return nextChunk;
            } else {
                nextChunk.add(nextPair);
            }
        }
        return nextChunk;
    }
}
