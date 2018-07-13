package com.trasier.client.impl.spring4.ws;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring4.context.TrasierSpringAccessor;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;

@Component
public class TrasierEndpointInterceptor extends EndpointInterceptorAdapter {
    private final Client client;
    private final TrasierClientConfiguration configuration;
    private final TrasierSpringAccessor trasierSpringAccessor;

    @Autowired
    public TrasierEndpointInterceptor(Client client, TrasierClientConfiguration configuration, TrasierSpringAccessor trasierSpringAccessor) {
        this.client = client;
        this.configuration = configuration;
        this.trasierSpringAccessor = trasierSpringAccessor;
    }

    @Override
    public boolean understands(Element header) {
        return true;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if(trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.createChildSpan("TODO-operationName");
            currentSpan.setIncomingEndpoint(new Endpoint(configuration.getSystemName()));
            currentSpan.setOutgoingEndpoint(new Endpoint("UNKNOWN"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageContext.getRequest().writeTo(out);
            currentSpan.setIncomingData(out.toString());
        }

        return super.handleRequest(messageContext, endpoint);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if(trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageContext.getResponse().writeTo(out);
            currentSpan.setIncomingData(out.toString());

            //TODO entkoppeln
            client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), currentSpan);
            trasierSpringAccessor.closeSpan(currentSpan);
        }

        return super.handleResponse(messageContext, endpoint);
    }
}