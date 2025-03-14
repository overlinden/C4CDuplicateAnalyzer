package de.wpsverlinden.c4cduplicateanalyzer.batch;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import de.wpsverlinden.c4cduplicateanalyzer.feed.JsonResponseHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class AccountDownloadReader implements ItemStreamReader<Account> {

    private static final String COLLECTION_NAME = "CorporateAccountCollection";
    private static final String SELECT_FIELDS = "AccountID,Name,AdditionalName,AdditionalName2,AdditionalName3,Phone,Email,Fax,Mobile,WebSite,City,CountryCode,StateCode,District,Street,HouseNumber,StreetPostalCode,ExternalID,CreationOn";

    @Autowired
    private Logger LOG;

    @Autowired
    private CloseableHttpClient client;

    @Autowired
    private ApplicationConfiguration config;

    @Autowired
    private JsonResponseHandler responseHandler;

    @Value("#{jobParameters['CountryCode']}")
    private String countryCode;

    @Value("#{jobParameters['Roles']}")
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
    public Account read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (startIndex >= accounts.size()) {
            return null;
        }
        return accounts.get(startIndex++);
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
                filter = filter + " and RoleCode eq '" + roleCode + "'";
            }
            final String encodedFilter = URLEncoder.encode(filter, "UTF8");
            final String urlParameter = "?$filter=" + encodedFilter + "&$orderby=AccountID&$select=" + SELECT_FIELDS + "&$skip=" + skip + "&$top=" + config.getDownloadChunkSize() + "&$inlinecount=allpages";
            HttpGet get = new HttpGet(config.getEndpoint() + "/" + COLLECTION_NAME + urlParameter);
            get.setHeader(HttpHeaders.ACCEPT, "application/json");
            JSONObject response = client.execute(get, responseHandler);
            final JSONObject d = response.getJSONObject("d");
            int totalRecords = d.getInt("__count");
            JSONArray accountsArray = d.getJSONArray("results");
            accountsArray.forEach(a -> {
                Account acc = buildAccount(a);
                if (acc != null) {
                    accounts.add(acc);
                }
            });
            skip += accountsArray.length();
            LOG.info("Received {} of {} {}accounts from odata feed", skip, totalRecords, roleCode != null ? roleCode + " " : "");
            if (skip < totalRecords) {
                getAccounts(skip, roleCode);
            }
        } catch (UnsupportedEncodingException ex) {
            LOG.error("Error encoding url parameter: {}", ex.getCause());
        } catch (IOException ex) {
            LOG.error("Error receiving accounts: {}", ex.getCause());
        }
    }

    private Account buildAccount(Object o) {
        if (!(o instanceof JSONObject)) {
            LOG.error("Invalid object received {}", o.getClass().getName());
            return null;
        }
        JSONObject object = (JSONObject)o;
        int accountId = Account.INVALID_ACCOUNT_ID;
        try {
            accountId = object.getInt("AccountID");
        } catch (JSONException ex) {
            //do nothing, filter later
        }
        
        final String dateString = object.getString("CreationOn");
        final Date creationOn = new Date(Long.parseLong(dateString.substring(6, dateString.length()-2)));

        return Account.builder()
                .AccountID(accountId)
                .Name(object.getString("Name"))
                .AdditionalName(object.getString("AdditionalName"))
                .AdditionalName2(object.getString("AdditionalName2"))
                .AdditionalName3(object.getString("AdditionalName3"))
                .Phone(object.getString("Phone"))
                .Email(object.getString("Email"))
                .Fax(object.getString("Fax"))
                .Mobile(object.getString("Mobile"))
                .WebSite(object.getString("WebSite"))
                .City(object.getString("City"))
                .CountryCode(object.getString("CountryCode"))
                .StateCode(object.getString("StateCode"))
                .District(object.getString("District"))
                .Street(object.getString("Street"))
                .StreetPostalCode(object.getString("StreetPostalCode"))
                .ErpID(object.getString("ExternalID"))
                .CreationDate(creationOn)
                .build();
    }
}
