package de.wpsverlinden.c4cduplicateanalyzer;

import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

@RunWith(SpringRunner.class)
public class LevenshteinCalculatorTest {

    @Test
    public void twoIdenticalAccountsHaveDistanceZero() {
        LevenshteinCalculator calculator = new LevenshteinCalculator();

        Account a = Account.builder()
                .Name("Test")
                .build();
        assertThat(calculator.getSimilarity(a, a, 0)).isEqualTo(1);
    }

    @Test
    public void oneAdditionalCharacterHaveDistanceOne() {
        LevenshteinCalculator calculator = new LevenshteinCalculator();

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Test1")
                .build();

        assertThat(calculator.getSimilarity(a, b, 0)).isEqualTo(0.8f);
    }

    @Test
    public void oneMissingCharacterHaveDistanceOne() {
        LevenshteinCalculator calculator = new LevenshteinCalculator();

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Tes")
                .build();

        assertThat(calculator.getSimilarity(a, b, 0)).isEqualTo(0.75f);
    }
    
    @Test
    public void oneReplacedCharacterHaveDistanceOne() {
        LevenshteinCalculator calculator = new LevenshteinCalculator();

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Text")
                .build();

        assertThat(calculator.getSimilarity(a, b, 0)).isEqualTo(0.75f);
    }

    @Test
    public void whenLengthDifferenceTooLargeThatSimilarityCanNotReachedThenNoDistanceIsCalculated() {
        LevenshteinCalculator calculator = new LevenshteinCalculator();

        Account a = Account.builder()
                .Name("Test")
                .build();
        Account b = Account.builder()
                .Name("Text very long name")
                .build();

        assertThat(calculator.getSimilarity(a, b, 0.9f)).isEqualTo(Float.MIN_VALUE);
    }

    @Test
    public void whenFirstAccountIsEmptyDistanceIsLengthOfSecondAccountAndNoDistanceIsCalculated() {
        LevenshteinCalculator calculator = new LevenshteinCalculator();

        Account a = Account.builder()
                .build();
        final String name = "Text very long name";
        Account b = Account.builder()
                .Name(name)
                .build();

        assertThat(calculator.getSimilarity(a, b, 0.0f)).isEqualTo(0);
    }

    @Test
    public void whenSecondAccountIsEmptyDistanceIsLengthOfSecondAccountAndNoDistanceIsCalculated() {
        LevenshteinCalculator calculator = new LevenshteinCalculator();

        final String name = "Text very long name";
        Account a = Account.builder()
                .Name(name)
                .build();

        Account b = Account.builder()
                .build();

        assertThat(calculator.getSimilarity(a, b, 0.0f)).isEqualTo(0);
    }
}
