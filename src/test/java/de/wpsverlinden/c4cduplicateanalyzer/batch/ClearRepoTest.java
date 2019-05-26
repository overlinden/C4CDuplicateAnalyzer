package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.persistence.DuplicateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClearRepoTest {

    @Mock
    private DuplicateRepository repo;

    @InjectMocks
    private ClearRepo clearRepo;

    @Test
    public void testRepoIsCleared() throws Exception {
        clearRepo.execute(null, null);
        Mockito.verify(repo, new Times(1)).deleteAllInBatch();
    }
}
