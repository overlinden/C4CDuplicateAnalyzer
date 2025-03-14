package de.wpsverlinden.c4cduplicateanalyzer.feed;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonResponseHandler implements HttpClientResponseHandler<JSONObject>{

    @Autowired
    private Logger LOG;
        
    @Override
    public JSONObject handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
        if (response.getCode() != HttpStatus.SC_OK) {
            LOG.error("Invalid http response: {}", response.getCode());
            return null;
        }
        
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
       return json;
    }
    
}
