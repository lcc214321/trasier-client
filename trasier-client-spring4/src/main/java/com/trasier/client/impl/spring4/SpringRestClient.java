package com.trasier.client.impl.spring4;

import com.trasier.client.Client;
import com.trasier.client.configuration.ApplicationConfiguration;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
public class SpringRestClient implements Client {
    private final ApplicationConfiguration appConfig;
    private final SpringClientConfiguration configuration;
    private final RestTemplate restTemplate;

    @Autowired
    public SpringRestClient(ApplicationConfiguration appConfig, SpringClientConfiguration configuration) {
        this(appConfig, configuration, new RestTemplate());
    }

    public SpringRestClient(ApplicationConfiguration appConfig, SpringClientConfiguration configuration, RestTemplate restTemplate) {
        this.appConfig = appConfig;
        this.configuration = configuration;
        this.restTemplate = restTemplate;

        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    private String getAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String basicAuth = Base64.getEncoder().encodeToString((configuration.getClientId() + ":" + configuration.getSecret()).getBytes());
        headers.add("Authorization", "Basic " + basicAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type","client_credentials");
        map.add("scope","");
        map.add("client_id","trasier-dev_170520_test-1");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);
        ResponseEntity<OAuthToken> exchange = restTemplate.postForEntity(appConfig.getAuthEndpoint(), requestEntity, OAuthToken.class);
        OAuthToken token = exchange.getBody();
        return token.getAccessToken();
    }

    @Override
    public boolean sendSpan(String accountId, String spaceKey, Span span) {
        return this.sendSpans(accountId, spaceKey, Collections.singletonList(span));
    }

    @Override
    public boolean sendSpans(String accountId, String spaceKey, List<Span> spans) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + getAuthHeader());

        HttpEntity<List<Span>> requestEntity = new HttpEntity<>(spans, headers);
        String writerEndpoint = String.format(appConfig.getWriterEndpoint(), accountId, spaceKey);
        ResponseEntity<Void> exchange = restTemplate.exchange(writerEndpoint, HttpMethod.PUT, requestEntity, Void.class);
        return !exchange.getStatusCode().is4xxClientError() && !exchange.getStatusCode().is5xxServerError() ;
    }

    @Override
    public void close() {
        // do nothing
    }
}