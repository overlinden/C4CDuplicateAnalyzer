package de.wpsverlinden.c4cduplicateanalyzer.model;

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
                    final String data = (Name != null ? Name : "")
                            + (AdditionalName != null ? AdditionalName : "")
                            + (AdditionalName2 != null ? AdditionalName2 : "")
                            + (AdditionalName3 != null ? AdditionalName3 : "")
                            + (Mobile != null ? Mobile : "")
                            + (Phone != null ? Phone : "")
                            + (Fax != null ? Fax : "")
                            + (Email != null ? Email : "")
                            + (WebSite != null ? WebSite : "")
                            + (City != null ? City : "")
                            + (CountryCode != null ? CountryCode : "")
                            + (StateCode != null ? StateCode : "")
                            + (District != null ? District : "")
                            + (Street != null ? Street : "")
                            + (HouseNumber != null ? HouseNumber : "")
                            + (StreetPostalCode != null ? StreetPostalCode : "");
                    serialData = data.replaceAll(" ", "").toLowerCase();
                }
            }
        }
        return serialData;
    }
}
