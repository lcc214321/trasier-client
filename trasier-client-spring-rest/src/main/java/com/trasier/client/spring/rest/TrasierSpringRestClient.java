package com.trasier.client.spring.rest;

import com.trasier.client.api.Span;
import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.configuration.TrasierProxyConfiguration;
import com.trasier.client.http.AsyncHttpClientFactory;
import com.trasier.client.http.TrasierHttpClient;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.client.TrasierSpringClient;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("trasierSpringClient")
public class TrasierSpringRestClient implements TrasierSpringClient {

    private final TrasierHttpClient client;

    @Autowired
    public TrasierSpringRestClient(TrasierEndpointConfiguration endpointConfiguration, TrasierClientConfiguration clientConfiguration, Optional<TrasierProxyConfiguration> optionalProxyConfiguration, Optional<List<TrasierSpanInterceptor>> optionalSpanInterceptors) {
        AsyncHttpClient client = createHttpClient(optionalProxyConfiguration);
        OAuthTokenSafe tokenSafe = new OAuthTokenSafe(clientConfiguration, endpointConfiguration.getAuthEndpoint(), client);
        TrasierHttpClient trasierHttpClient = new TrasierHttpClient(clientConfiguration, endpointConfiguration, tokenSafe, client);
        this.client = trasierHttpClient;
        optionalSpanInterceptors.ifPresent(it -> it.forEach(this.client::addSpanInterceptor));
    }

    protected AsyncHttpClient createHttpClient(Optional<TrasierProxyConfiguration> optionalProxyConfiguration) {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = AsyncHttpClientFactory.createBuilder();
        if (optionalProxyConfiguration.isPresent()) {
            AsyncHttpClientFactory.setProxy(clientBuilder, optionalProxyConfiguration.get());
        }
        return AsyncHttpClientFactory.createClient(clientBuilder);
    }

    public TrasierSpringRestClient(TrasierHttpClient client) {
        this.client = client;
    }

    @Override
    public boolean sendSpan(Span span) {
        return client.sendSpan(span);
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        return client.sendSpans(spans);
    }

    @Override
    public void close() {
        client.close();
    }

}