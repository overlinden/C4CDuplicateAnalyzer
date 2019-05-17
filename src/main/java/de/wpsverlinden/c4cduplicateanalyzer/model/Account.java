package de.wpsverlinden.c4cduplicateanalyzer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

    private String AccountID, Name, AdditionalName, AdditionalName2, AdditionalName3, Phone, Email, Fax, Mobile, WebSite, City, CountryCode, StateCode, District, Street, HouseNumber, StreetPostalCode;

    public String getSerialData() {
        return (Name != null ? Name : "")
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
                + (StreetPostalCode != null ? StreetPostalCode : "").replaceAll(" ", "").toLowerCase();
    }
}
