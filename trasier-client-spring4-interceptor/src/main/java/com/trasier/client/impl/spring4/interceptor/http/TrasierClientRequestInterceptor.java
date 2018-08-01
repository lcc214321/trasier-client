package com.trasier.client.impl.spring4.interceptor.http;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TrasierClientRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Client client;
    private final TrasierClientConfiguration configuration;

    @Autowired
    public TrasierClientRequestInterceptor(Client client, TrasierClientConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] data, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, data);
        return response;
    }
}