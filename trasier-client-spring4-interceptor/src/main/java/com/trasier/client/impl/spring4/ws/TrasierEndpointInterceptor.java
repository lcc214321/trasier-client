package com.trasier.client.impl.spring4.ws;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;

@Component
public class TrasierEndpointInterceptor extends EndpointInterceptorAdapter {
    private final Client client;
    private final TrasierClientConfiguration configuration;

    public TrasierEndpointInterceptor(Client client, TrasierClientConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public boolean understands(Element header) {
        return true;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        messageContext.getRequest().writeTo(out);


        return super.handleRequest(messageContext, endpoint);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        messageContext.getRequest();
        return super.handleResponse(messageContext, endpoint);
    }
}