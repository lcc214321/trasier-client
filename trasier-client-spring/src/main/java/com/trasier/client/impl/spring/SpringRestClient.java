package com.trasier.client.impl.spring;

import com.trasier.client.Client;
import com.trasier.client.configuration.ApplicationConfiguration;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class SpringRestClient implements Client {

    private final ApplicationConfiguration appConfig;
    private final SpringClientConfiguration configuration;
    private final RestTemplate restTemplate;

    @Autowired
    public SpringRestClient(ApplicationConfiguration appConfig, SpringClientConfiguration configuration) {
        this.appConfig = appConfig;
        this.configuration = configuration;
        this.restTemplate = new OAuth2RestTemplate(createOAuth2ProtectedResourceDetails(), new DefaultOAuth2ClientContext());
    }

    public SpringRestClient(ApplicationConfiguration appConfig, SpringClientConfiguration configuration, RestTemplate restTemplate) {
        this.appConfig = appConfig;
        this.configuration = configuration;
        this.restTemplate = restTemplate;
    }

    private OAuth2ProtectedResourceDetails createOAuth2ProtectedResourceDetails() {
        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
        resource.setAccessTokenUri(appConfig.getAuthEndpoint());
        resource.setClientId(configuration.getClientId());
        resource.setClientSecret(configuration.getSecret());
        resource.setGrantType("client_credentials");
        return resource;
    }

    @Override
    public boolean sendSpan(Span span) {
        return this.sendSpans(Collections.singletonList(span));
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Span>> requestEntity = new HttpEntity<>(spans, headers);
        ResponseEntity<Void> exchange = restTemplate.exchange(appConfig.getWriterEndpoint(), HttpMethod.PUT, requestEntity, Void.class);
        return !exchange.getStatusCode().is4xxClientError() && !exchange.getStatusCode().is5xxServerError() ;
    }

    @Override
    public void close() {
        // do nothing
    }

}
