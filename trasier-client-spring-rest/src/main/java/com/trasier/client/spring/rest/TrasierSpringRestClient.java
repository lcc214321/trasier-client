package com.trasier.client.spring.rest;

import com.trasier.client.api.Span;
import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.configuration.TrasierProxyConfiguration;
import com.trasier.client.http.AsyncHttpClientFactory;
import com.trasier.client.http.TrasierHttpClient;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.TrasierCompressSpanInterceptor;
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
    private TrasierCompressSpanInterceptor compressSpanInterceptor;

    @Autowired
    public TrasierSpringRestClient(TrasierEndpointConfiguration endpointConfiguration, TrasierClientConfiguration clientConfiguration, Optional<TrasierProxyConfiguration> optionalProxyConfiguration, Optional<List<TrasierSpanInterceptor>> optionalSpanInterceptors) {
        AsyncHttpClient client = createHttpClient(optionalProxyConfiguration);
        OAuthTokenSafe tokenSafe = new OAuthTokenSafe(clientConfiguration, endpointConfiguration.getAuthEndpoint(), client);
        TrasierHttpClient trasierHttpClient = new TrasierHttpClient(clientConfiguration, endpointConfiguration, tokenSafe, client);
        this.client = trasierHttpClient;
        if (clientConfiguration.isActivated()) {
            tokenSafe.refreshToken();
            optionalSpanInterceptors.ifPresent(it -> it.forEach(this.client::addSpanInterceptor));
            if (!clientConfiguration.isCompressPayloadDisabled()) {
                this.compressSpanInterceptor = new TrasierCompressSpanInterceptor();
            }
        }
    }

    protected AsyncHttpClient createHttpClient(Optional<TrasierProxyConfiguration> optionalProxyConfiguration) {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = AsyncHttpClientFactory.createBuilder();
        if (optionalProxyConfiguration.isPresent()) {
            AsyncHttpClientFactory.setProxy(clientBuilder, optionalProxyConfiguration.get());
        }
        return AsyncHttpClientFactory.createClient(clientBuilder);
    }

    public TrasierSpringRestClient(TrasierHttpClient client, TrasierCompressSpanInterceptor compressSpanInterceptor) {
        this.client = client;
        this.compressSpanInterceptor = compressSpanInterceptor;
    }

    @Override
    public boolean sendSpan(Span span) {
        if (compressSpanInterceptor != null) {
            compressSpanInterceptor.intercept(span);
        }
        return client.sendSpan(span);
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        if (compressSpanInterceptor != null) {
            spans.forEach(compressSpanInterceptor::intercept);
        }
        return client.sendSpans(spans);
    }

    @Override
    public void close() {
        client.close();
    }

}