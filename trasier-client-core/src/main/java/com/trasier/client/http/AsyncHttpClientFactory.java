package com.trasier.client.http;

import com.trasier.client.configuration.TrasierProxyConfiguration;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Realm;
import org.asynchttpclient.proxy.ProxyServer;

public final class AsyncHttpClientFactory {
    public static DefaultAsyncHttpClientConfig.Builder createBuilder(TrasierProxyConfiguration trasierProxyConfiguration) {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = createBuilder();
        ProxyServer.Builder proxyServerBuilder = new ProxyServer.Builder(trasierProxyConfiguration.getHost(), trasierProxyConfiguration.getPort());
        if (trasierProxyConfiguration.getUsername() != null) {
            Realm.Builder realm = new Realm.Builder(trasierProxyConfiguration.getUsername(), trasierProxyConfiguration.getPassword());
            realm.setScheme(Realm.AuthScheme.valueOf(trasierProxyConfiguration.getScheme()));
            proxyServerBuilder.setRealm(realm.build());
        }
        clientBuilder.setProxyServer(proxyServerBuilder.build());
        return clientBuilder;
    }

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
