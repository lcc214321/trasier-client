package com.trasier.client.http;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;

public final class AsyncHttpClientFactory {
    public static DefaultAsyncHttpClientConfig.Builder createBuilder() {
        //setting values via -Dorg.asynchttpclient.nameOfTheProperty is possible
        return Dsl.config()
                .setThreadPoolName("trasier")
                .setMaxConnections(500)
                .setUseInsecureTrustManager(true);
    }

    public static AsyncHttpClient createClient(DefaultAsyncHttpClientConfig.Builder clientBuilder) {
        return Dsl.asyncHttpClient(clientBuilder);
    }

    public static AsyncHttpClient createDefaultClient() {
        return AsyncHttpClientFactory.createClient(AsyncHttpClientFactory.createBuilder());
    }

}
