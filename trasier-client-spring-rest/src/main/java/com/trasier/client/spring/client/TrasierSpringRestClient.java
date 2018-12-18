package com.trasier.client.spring.client;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.auth.OAuthTokenSafe;
import com.trasier.client.util.ProjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component("trasierSpringClient")
public class TrasierSpringRestClient implements TrasierSpringClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierSpringRestClient.class);

    private final TrasierEndpointConfiguration applicationConfiguration;
    private final TrasierClientConfiguration clientConfiguration;
    private final RestTemplate restTemplate;
    private final OAuthTokenSafe tokenSafe;
    @Autowired(required = false)
    private final List<TrasierSpanInterceptor> spanInterceptors = new ArrayList<>();

    @Autowired
    public TrasierSpringRestClient(TrasierEndpointConfiguration applicationConfiguration, TrasierClientConfiguration clientConfiguration, OAuthTokenSafe tokenSafe) throws Exception {
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
    public boolean sendSpans(String accountId, String spaceKey, List<Span> spanList) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        List<Span> spans = new ArrayList<>(spanList);

        applyInterceptors(spans);

        if (spans.isEmpty()) {
            return false;
        }

        enrichSpans(spans);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + tokenSafe.getAuthHeader());

            UriComponents builder = UriComponentsBuilder.fromHttpUrl(applicationConfiguration.getWriterEndpoint()).buildAndExpand(accountId, spaceKey);
            HttpEntity<List<Span>> requestEntity = new HttpEntity<>(spans, headers);
            ResponseEntity<Void> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, requestEntity, Void.class);
            return !exchange.getStatusCode().is4xxClientError() && !exchange.getStatusCode().is5xxServerError();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private void enrichSpans(List<Span> spans) {
        spans.forEach(this::enrichVersion);
    }

    private void enrichVersion(Span span) {
        if (span.getTags() == null) {
            span.setTags(new HashMap<>());
        }
        String spanKind = span.getTags().get("span.kind");
        if (spanKind == null) {
            spanKind = "-";
        }
        span.getTags().put("trasier_client." + spanKind, "rest-spring-client");
        span.getTags().put("trasier_client_version." + spanKind, ProjectUtils.getProjectVersion());
    }

    private void applyInterceptors(List<Span> spans) {
        spans.removeIf(span -> {
            applyInterceptors(span);
            return span.isCancel();
        });
    }

    private void applyInterceptors(Span span) {
        for (TrasierSpanInterceptor spanInterceptor : this.spanInterceptors) {
            spanInterceptor.intercept(span);
        }
    }

    @Override
    public void close() {
        // do nothing
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

}