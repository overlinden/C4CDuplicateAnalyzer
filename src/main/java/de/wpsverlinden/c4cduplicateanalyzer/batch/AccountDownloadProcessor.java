package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.batch.item.ItemProcessor;

public class AccountDownloadProcessor implements ItemProcessor<Account, Account> {

    @Override
    public Account process(Account item) throws Exception {
        if (Objects.equals(item.getAccountID(), Account.INVALID_ACCOUNT_ID)) {
            return null;
        }
        item.setSerialData(Stream.of(
                item.getName(),
                item.getAdditionalName(),
                item.getAdditionalName2(),
                item.getAdditionalName3(),
                item.getMobile(),
                item.getPhone(),
                item.getFax(),
                item.getEmail(),
                item.getWebSite(),
                item.getCity(),
                item.getCountryCode(),
                item.getStateCode(),
                item.getDistrict(),
                item.getStreet(),
                item.getHouseNumber(),
                item.getStreetPostalCode())
            .filter(Objects::nonNull)
            .map(s -> s.replaceAll(" ", ""))
            .map(String::toLowerCase)
            .collect(Collectors.joining()));
        return item;
    }
}
