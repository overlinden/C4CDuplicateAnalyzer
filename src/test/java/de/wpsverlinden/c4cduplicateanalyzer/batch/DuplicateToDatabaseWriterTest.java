package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import de.wpsverlinden.c4cduplicateanalyzer.persistence.DuplicateRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateToDatabaseWriterTest {

    @Mock
    private DuplicateRepository repo;

    @InjectMocks
    private DuplicateToDatebaseWriter duplicateToDatabaseWriter;

    @Test
    public void singleDuplicateIsSaved() throws Exception {
        Duplicate d = new Duplicate();
        d.setA("A");
        d.setB("B");
        d.setSimilarity(0f);

        List<Duplicate> duplicateList = Collections.singletonList(d);
        List<List<Duplicate>> listOfDuplicateList = Collections.singletonList(duplicateList);
        duplicateToDatabaseWriter.write(listOfDuplicateList);
        Mockito.verify(repo, new Times(1)).saveAll(duplicateList);
    }

    @Test
    public void listOfDuplicatesIsSaved() throws Exception {
        Duplicate d1 = new Duplicate();
        d1.setA("A1");
        d1.setB("B1");
        d1.setSimilarity(0f);
        List<Duplicate> duplicateList1 = Collections.singletonList(d1);

        Duplicate d2 = new Duplicate();
        d2.setA("A2");
        d2.setB("B2");
        d2.setSimilarity(0.5f);
        List<Duplicate> duplicateList2 = Collections.singletonList(d2);

        List<List<Duplicate>> listOfDuplicateList = Stream.of(duplicateList1, duplicateList2).collect(Collectors.toList());
        duplicateToDatabaseWriter.write(listOfDuplicateList);
        Mockito.verify(repo, new Times(1)).saveAll(duplicateList1);
        Mockito.verify(repo, new Times(1)).saveAll(duplicateList2);
    }
}
