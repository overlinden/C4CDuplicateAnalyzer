package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.LevenshteinCalculator;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import de.wpsverlinden.c4cduplicateanalyzer.model.Duplicate;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateFinderTest {

    @Mock
    private LevenshteinCalculator calculator;

    @Mock
    private Logger LOG;

    @InjectMocks
    private DuplicateFinder duplicateFinder;

    @Test
    public void calculatorIsCalledWithCorrectArguments() throws Exception {
        ReflectionTestUtils.setField(duplicateFinder, "threshold", 0.5f);
        Mockito.when(calculator.getSimilarity(Mockito.any(), Mockito.any(), Mockito.anyFloat())).thenReturn(0.9f);

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Test1")
                .build();

        duplicateFinder.process(Arrays.asList(a, b));

        Mockito.verify(calculator, times(1)).getSimilarity(Mockito.eq(a), Mockito.eq(b), Mockito.eq(0.5f));
    }

    @Test
    public void firstAccountIsCheckedAgainstTheOthers() throws Exception {
        ReflectionTestUtils.setField(duplicateFinder, "threshold", 0.5f);
        Mockito.when(calculator.getSimilarity(Mockito.any(), Mockito.any(), Mockito.anyFloat())).thenReturn(0.9f);

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Test1")
                .build();
        Account c = Account.builder()
                .Name("Test2")
                .build();

        duplicateFinder.process(Arrays.asList(a, b, c));

        Mockito.verify(calculator).getSimilarity(Mockito.eq(a), Mockito.eq(b), Mockito.eq(0.5f));
        Mockito.verify(calculator).getSimilarity(Mockito.eq(a), Mockito.eq(c), Mockito.eq(0.5f));
    }

    @Test
    public void duplicateIsDroppedIfThresholdIsNotReached() throws Exception {
        ReflectionTestUtils.setField(duplicateFinder, "threshold", 0.9f);
        Mockito.when(calculator.getSimilarity(Mockito.any(), Mockito.any(), Mockito.anyFloat())).thenReturn(0.5f);

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Test1")
                .build();

        List<Duplicate> dupes = duplicateFinder.process(Arrays.asList(a, b));

        assertThat(dupes).isNull();
    }

    @Test
    public void duplicateIsKeptIfThresholdIsReached() throws Exception {
        ReflectionTestUtils.setField(duplicateFinder, "threshold", 0.5f);
        Mockito.when(calculator.getSimilarity(Mockito.any(), Mockito.any(), Mockito.anyFloat())).thenReturn(0.9f);

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Test1")
                .build();

        List<Duplicate> dupes = duplicateFinder.process(Arrays.asList(a, b));

        assertThat(dupes).hasSize(1);
    }

    @Test
    public void duplicateResultContainsCorrectValues() throws Exception {
        ReflectionTestUtils.setField(duplicateFinder, "threshold", 0.5f);
        Mockito.when(calculator.getSimilarity(Mockito.any(), Mockito.any(), Mockito.anyFloat())).thenReturn(0.9f);

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Test1")
                .build();

        List<Duplicate> dupes = duplicateFinder.process(Arrays.asList(a, b));
        Duplicate d = dupes.get(0);

        assertThat(d.getA()).isEqualTo(a.toString());
        assertThat(d.getB()).isEqualTo(b.toString());
        assertThat(d.getSimilarity()).isEqualTo(0.9f);
    }
}
