package com.trasier.client.impl.spring4;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierApplicationConfiguration;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Component
public class SpringRestClient implements Client {
    private final TrasierApplicationConfiguration appConfig;
    private final RestTemplate restTemplate;
    private final OAuthTokenSafe tokenSafe;

    @Autowired
    public SpringRestClient(TrasierApplicationConfiguration appConfig, RestTemplate restTemplate, OAuthTokenSafe tokenSafe) {
        this.appConfig = appConfig;
        this.restTemplate = restTemplate;
        this.tokenSafe = tokenSafe;
    }

    @Override
    public boolean sendSpan(String accountId, String spaceKey, Span span) {
        return this.sendSpans(accountId, spaceKey, Collections.singletonList(span));
    }

    @Override
    public boolean sendSpans(String accountId, String spaceKey, List<Span> spans) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + tokenSafe.getAuthHeader());

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(appConfig.getWriterEndpoint()).buildAndExpand(accountId, spaceKey);
        HttpEntity<List<Span>> requestEntity = new HttpEntity<>(spans, headers);
        ResponseEntity<Void> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, requestEntity, Void.class);
        return !exchange.getStatusCode().is4xxClientError() && !exchange.getStatusCode().is5xxServerError();
    }

    @Override
    public void close() {
        // do nothing
    }
}