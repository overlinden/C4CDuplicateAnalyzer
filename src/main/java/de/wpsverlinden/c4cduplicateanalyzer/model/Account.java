package de.wpsverlinden.c4cduplicateanalyzer.model;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public class Account {

    @Getter
    private final String AccountID, Name, AdditionalName, AdditionalName2, AdditionalName3, Phone, Email, Fax, Mobile, WebSite, City, CountryCode, StateCode, District, Street, HouseNumber, StreetPostalCode;

    private volatile String serialData;

    public String getSerialData() {
        if (serialData == null) {
            synchronized(this) {
                if (serialData == null) {
                    serialData = Stream.of(Name, AdditionalName, AdditionalName2, AdditionalName3, Mobile, Phone, Fax, Email, WebSite, City, CountryCode, StateCode, District, Street, HouseNumber, StreetPostalCode)
                            .filter(Objects::nonNull)
                            .map(s -> s.replaceAll(" ", ""))
                            .map(String::toLowerCase)
                            .collect(Collectors.joining());
                    }
            }
        }
        return serialData;
    }
}
