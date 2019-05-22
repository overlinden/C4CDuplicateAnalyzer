
package de.wpsverlinden.c4cduplicateanalyzer.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
public class AccountTest {
    
    @Test
    public void getSerialDataReturnsConcatenatedFields() {
        Account a = Account.builder()
                .AccountID("id")
                .Name("n")
                .AdditionalName("an")
                .AdditionalName2("an2")
                .AdditionalName3("an3")
                .City("ci")
                .CountryCode("cc")
                .District("d")
                .Email("m@example.com")
                .Phone("0")
                .Fax("1")
                .Mobile("2")
                .HouseNumber("100")
                .StateCode("sc")
                .Street("s")
                .StreetPostalCode("pc")
                .WebSite("http://example.com")
                .build();
        assertThat(a.getSerialData()).isEqualTo("nanan2an3201m@example.comhttp://example.comciccscds100pc");
    }
    
    @Test
    public void getSerialDataReturnsLowercaseString() {
        Account a = Account.builder()
                .Name("Name")
                .build();
        assertThat(a.getSerialData()).isEqualTo("name");
    }
    
    @Test
    public void getSerialDataReturnsReplacesSpacesInString() {
        Account a = Account.builder()
                .Name("name value")
                .build();
        assertThat(a.getSerialData()).isEqualTo("namevalue");
    }
}