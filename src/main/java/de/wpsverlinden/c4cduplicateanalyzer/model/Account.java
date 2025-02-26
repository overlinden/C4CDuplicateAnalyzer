package de.wpsverlinden.c4cduplicateanalyzer.model;

import java.time.LocalDate;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Persistable<Integer> {

    @Transient
    public static Integer INVALID_ACCOUNT_ID = -1;
    
    @Id
    private Integer AccountID;
    private String Name, AdditionalName, AdditionalName2, AdditionalName3, Phone, Email, Fax, Mobile, WebSite, City, CountryCode, StateCode, District, Street, HouseNumber, StreetPostalCode, ErpID;
    private Date CreationDate;
    private String SerialData;

    @Override
    public Integer getId() {
        return AccountID;
    }

    @Override
    public boolean isNew() {
        return true;
    }

}
