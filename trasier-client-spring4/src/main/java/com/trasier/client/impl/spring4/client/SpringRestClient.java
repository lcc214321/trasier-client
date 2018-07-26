package com.trasier.client.impl.spring4.client;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.impl.spring4.auth.OAuthTokenSafe;
import com.trasier.client.model.ConversationInfo;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Component("trasierSpringClient")
public class SpringRestClient implements SpringClient {
    private final TrasierEndpointConfiguration applicationConfiguration;
    private final TrasierClientConfiguration clientConfiguration;
    private final RestTemplate restTemplate;
    private final OAuthTokenSafe tokenSafe;

    @Autowired
    public SpringRestClient(TrasierEndpointConfiguration applicationConfiguration, TrasierClientConfiguration clientConfiguration, @Qualifier("trasierRestTemplate") RestTemplate restTemplate, OAuthTokenSafe tokenSafe) {
        this.applicationConfiguration = applicationConfiguration;
        this.clientConfiguration = clientConfiguration;
        this.restTemplate = restTemplate;
        this.tokenSafe = tokenSafe;
    }

    public boolean sendSpan(Span span) {
        return this.sendSpan(clientConfiguration.getAccountId(), clientConfiguration.getSpaceKey(), span);
    }

    @Override
    public boolean sendSpan(String accountId, String spaceKey, Span span) {
        return this.sendSpans(accountId, spaceKey, Collections.singletonList(span));
    }

    public boolean sendSpans(List<Span> spans) {
        return this.sendSpans(clientConfiguration.getAccountId(), clientConfiguration.getSpaceKey(), spans);
    }

    @Override
    public boolean sendSpans(String accountId, String spaceKey, List<Span> spans) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + tokenSafe.getAuthHeader());

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(applicationConfiguration.getWriterEndpoint()).buildAndExpand(accountId, spaceKey);
        HttpEntity<List<Span>> requestEntity = new HttpEntity<>(spans, headers);
        ResponseEntity<Void> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, requestEntity, Void.class);
        return !exchange.getStatusCode().is4xxClientError() && !exchange.getStatusCode().is5xxServerError();
    }

    @Override
    public ConversationInfo readConversation(String accountId, String spaceKey, String conversationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + tokenSafe.getAuthHeader());

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(applicationConfiguration.getReaderEndpoint()).buildAndExpand(accountId, spaceKey, conversationId);

        ResponseEntity<ConversationInfo> forEntity = restTemplate.getForEntity(builder.toUriString(), ConversationInfo.class);
        return forEntity.getBody();
    }

    @Override
    public Span readSpan(String accountId, String spaceKey, String conversationId, String traceId, String spanId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + tokenSafe.getAuthHeader());

        String url = applicationConfiguration.getReaderEndpoint() + "/traces/{traceId}/spans/{spanId}" ;
        UriComponents builder = UriComponentsBuilder.fromHttpUrl(url).buildAndExpand(accountId, spaceKey, conversationId, traceId, spanId);

        ResponseEntity<Span> forEntity = restTemplate.getForEntity(builder.toUriString(), Span.class);
        return forEntity.getBody();
    }

    @Override
    public void close() {
        // do nothing
    }
}