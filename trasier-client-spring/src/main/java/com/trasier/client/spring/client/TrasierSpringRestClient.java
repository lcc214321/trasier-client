package com.trasier.client.spring.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.auth.OAuthTokenSafe;

@Component("trasierSpringClient")
public class TrasierSpringRestClient implements TrasierSpringClient {
    private final TrasierEndpointConfiguration applicationConfiguration;
    private final TrasierClientConfiguration clientConfiguration;
    private final RestTemplate restTemplate;
    private final OAuthTokenSafe tokenSafe;
    @Autowired(required = false)
    private final List<TrasierSpanInterceptor> spanInterceptors = new ArrayList<>();

    @Autowired
    public TrasierSpringRestClient(TrasierEndpointConfiguration applicationConfiguration, TrasierClientConfiguration clientConfiguration, OAuthTokenSafe tokenSafe) {
        this(applicationConfiguration, clientConfiguration, new RestTemplate(), tokenSafe);
    }

    TrasierSpringRestClient(TrasierEndpointConfiguration applicationConfiguration, TrasierClientConfiguration clientConfiguration, RestTemplate restTemplate, OAuthTokenSafe tokenSafe) {
        this.applicationConfiguration = applicationConfiguration;
        this.clientConfiguration = clientConfiguration;
        this.tokenSafe = tokenSafe;
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public boolean sendSpan(Span span) {
        return this.sendSpan(clientConfiguration.getAccountId(), clientConfiguration.getSpaceKey(), span);
    }

    @Override
    public boolean sendSpan(String accountId, String spaceKey, Span span) {
        return this.sendSpans(accountId, spaceKey, Arrays.asList(span));
    }

    public boolean sendSpans(List<Span> spans) {
        return this.sendSpans(clientConfiguration.getAccountId(), clientConfiguration.getSpaceKey(), spans);
    }

    @Override
    public boolean sendSpans(String accountId, String spaceKey, List<Span> spans) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        applyInterceptors(spans);

        if (spans.isEmpty()) {
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + tokenSafe.getAuthHeader());

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(applicationConfiguration.getWriterEndpoint()).buildAndExpand(accountId, spaceKey);
        HttpEntity<List<Span>> requestEntity = new HttpEntity<>(spans, headers);
        ResponseEntity<Void> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, requestEntity, Void.class);
        return !exchange.getStatusCode().is4xxClientError() && !exchange.getStatusCode().is5xxServerError();
    }

    private void applyInterceptors(List<Span> spans) {
        spans.removeIf(span -> !applyInterceptors(span));
    }

    private boolean applyInterceptors(Span next) {
        for (TrasierSpanInterceptor spanInterceptor : this.spanInterceptors) {
            if (spanInterceptor.cancel(next)) {
                return false;
            } else {
                spanInterceptor.intercept(next);
            }
        }
        return true;
    }

    @Override
    public void close() {
        // do nothing
    }
}