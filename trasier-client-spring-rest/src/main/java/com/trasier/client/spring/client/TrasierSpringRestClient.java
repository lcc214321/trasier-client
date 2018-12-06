package com.trasier.client.spring.client;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.auth.OAuthTokenSafe;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
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
        this(applicationConfiguration, clientConfiguration, createRestTemplate(), tokenSafe);
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

        if (spans.isEmpty()) {
            return false;
        } else {
            applyInterceptors(spans);
        }

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


    private static RestTemplate createRestTemplate() throws Exception {
        char[] password = "trasier".toCharArray();
        SSLContext sslContext = new SSLContextBuilder()
                .loadKeyMaterial(keyStore("classpath:trasier.jks", password), password)
                .build();
        HttpClient client = HttpClients.custom().setSSLContext(sslContext).build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
    }


    private static KeyStore keyStore(String file, char[] password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        File key = ResourceUtils.getFile(file);
        try (InputStream in = new FileInputStream(key)) {
            keyStore.load(in, password);
        }
        return keyStore;
    }

}