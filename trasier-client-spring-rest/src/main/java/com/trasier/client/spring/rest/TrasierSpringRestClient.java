package com.trasier.client.spring.rest;

import com.trasier.client.api.Span;
import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.http.AsyncHttpClientFactory;
import com.trasier.client.http.TrasierHttpClient;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.client.TrasierSpringClient;
import org.asynchttpclient.AsyncHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component("trasierSpringClient")
public class TrasierSpringRestClient implements TrasierSpringClient {

    @Autowired(required = false)
    private final List<TrasierSpanInterceptor> spanInterceptors = new ArrayList<>();
    private final TrasierHttpClient trasierHttpClient;

    @Autowired
    public TrasierSpringRestClient(TrasierEndpointConfiguration endpointConfiguration, TrasierClientConfiguration clientConfiguration) {
        AsyncHttpClient client = AsyncHttpClientFactory.createDefaultClient();
        OAuthTokenSafe tokenSafe = new OAuthTokenSafe(clientConfiguration, endpointConfiguration.getAuthEndpoint(), client);
        TrasierHttpClient trasierHttpClient = new TrasierHttpClient(clientConfiguration, endpointConfiguration, tokenSafe, client);
        this.trasierHttpClient = trasierHttpClient;
    }

    public TrasierSpringRestClient(TrasierHttpClient trasierHttpClient) {
        this.trasierHttpClient = trasierHttpClient;

    }

    @PostConstruct
    public void init() {
        spanInterceptors.forEach(this.trasierHttpClient::addSpanInterceptor);
    }

    @Override
    public boolean sendSpan(Span span) {
        return this.sendSpan(span);
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        return this.sendSpans(spans);
    }

    @Override
    public void close() {
        trasierHttpClient.close();
    }

}