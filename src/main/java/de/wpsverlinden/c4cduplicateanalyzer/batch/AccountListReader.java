package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import de.wpsverlinden.c4cduplicateanalyzer.feed.ODataFeedReceiver;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.slf4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class AccountListReader implements ItemStreamReader<List<Account>> {

    private static final String COLLECTION_NAME = "CorporateAccountCollection";
    private static final String SELECT_FIELDS = "AccountID,Name,AdditionalName,AdditionalName2,AdditionalName3,Phone,Email,Fax,Mobile,WebSite,City,CountryCode,StateCode,District,Street,HouseNumber,StreetPostalCode";

    @Autowired
    private Logger LOG;

    @Autowired
    private ODataFeedReceiver oDataFeedReceiver;

    @Autowired
    private ApplicationConfiguration config;

    @Value("#{jobParameters[CountryCode]}")
    private String countryCode;

    @Value("#{jobParameters[Roles]}")
    private String roles;

    private List<Account> accounts;
    private int startIndex;

    @Override
    public void open(ExecutionContext ec) throws ItemStreamException {
        accounts = new ArrayList<>();

        if (roles != null) {
            Arrays.stream(roles.split(",")).forEach(this::getAccounts);
        } else {
            getAccounts(null);
        }

        if (ec.containsKey("StartIndex")) {
            startIndex = ec.getInt("StartIndex");
        }
    }

    @Override
    public void update(ExecutionContext ec) throws ItemStreamException {
        ec.putInt("StartIndex", startIndex);
    }

    @Override
    public void close() throws ItemStreamException {
    }

    @Override
    public synchronized List<Account> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (startIndex >= accounts.size() - 1) { //Cancel if no tuple is left
            return null;
        }
        return accounts.subList(startIndex++, accounts.size());
    }

    private void getAccounts(String roleCode) {
        getAccounts(0, roleCode);
    }

    private void getAccounts(int skip, String roleCode) {
        try {
            String filter = "LifeCycleStatusCode eq '2'";
            if (countryCode != null) {
                filter = filter + " and CountryCode eq '" + countryCode + "'";
            }
            if (roleCode != null) {
                filter = filter + "' and RoleCode eq '" + roleCode + "'";
            }
            final String encodedFilter = URLEncoder.encode(filter, "UTF8");
            final String urlParameter = "/?$filter=" + encodedFilter + "&$orderby=AccountID&$select=" + SELECT_FIELDS + "&$skip=" + skip + "&$top=" + config.getChunkSize() + "&$inlinecount=allpages";
            ODataFeed eventFeed = oDataFeedReceiver.readFeed(COLLECTION_NAME, Optional.of(urlParameter));
            int totalRecords = eventFeed.getFeedMetadata().getInlineCount();
            List<Account> collect = eventFeed.getEntries().stream()
                    .map(entry -> buildAccount(entry.getProperties()))
                    .collect(Collectors.toList());
            skip += collect.size();
            LOG.info("Received {} of {} {}accounts from odata feed", skip, totalRecords, roleCode != null ? roleCode + " " : "");
            accounts.addAll(collect);
            if (skip < totalRecords) {
                getAccounts(skip, roleCode);
            }
        } catch (UnsupportedEncodingException ex) {
            LOG.error("Error encoding url parameter: {}", ex.getCause());
        } catch (IOException | ODataException ex) {
            LOG.error("Error receiving accounts: {}", ex.getCause());
        }
    }

    private Account buildAccount(Map<String, Object> propertyMap) {
        return Account.builder()
                .AccountID((String) propertyMap.get("AccountID"))
                .Name((String) propertyMap.get("Name"))
                .AdditionalName((String) propertyMap.get("AdditionalName"))
                .AdditionalName2((String) propertyMap.get("AdditionalName2"))
                .AdditionalName3((String) propertyMap.get("AdditionalName3"))
                .Phone((String) propertyMap.get("Phone"))
                .Email((String) propertyMap.get("Email"))
                .Fax((String) propertyMap.get("Fax"))
                .Mobile((String) propertyMap.get("Mobile"))
                .WebSite((String) propertyMap.get("WebSite"))
                .City((String) propertyMap.get("City"))
                .CountryCode((String) propertyMap.get("CountryCode"))
                .StateCode((String) propertyMap.get("StateCode"))
                .District((String) propertyMap.get("District"))
                .Street((String) propertyMap.get("Street"))
                .StreetPostalCode((String) propertyMap.get("StreetPostalCode"))
                .build();
    }
}
