package com.trasier.client.http;

import com.trasier.client.configuration.TrasierProxyConfiguration;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Realm;
import org.asynchttpclient.proxy.ProxyServer;

public final class AsyncHttpClientFactory {
    public static void setProxy(DefaultAsyncHttpClientConfig.Builder clientBuilder, TrasierProxyConfiguration trasierProxyConfiguration) {
        if(trasierProxyConfiguration.getHost() != null && !trasierProxyConfiguration.getHost().trim().isEmpty() && trasierProxyConfiguration.getPort() != null) {
            ProxyServer.Builder proxyServerBuilder = new ProxyServer.Builder(trasierProxyConfiguration.getHost().trim(), trasierProxyConfiguration.getPort());
            if (trasierProxyConfiguration.getUsername() != null && trasierProxyConfiguration.getPassword() != null && trasierProxyConfiguration.getScheme() != null) {
                Realm.Builder realm = new Realm.Builder(trasierProxyConfiguration.getUsername(), trasierProxyConfiguration.getPassword());
                realm.setScheme(Realm.AuthScheme.valueOf(trasierProxyConfiguration.getScheme()));
                proxyServerBuilder.setRealm(realm.build());
            }
            clientBuilder.setProxyServer(proxyServerBuilder.build());
        }
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
