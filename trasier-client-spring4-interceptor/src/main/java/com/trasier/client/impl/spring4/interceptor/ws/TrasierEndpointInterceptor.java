package com.trasier.client.impl.spring4.interceptor.ws;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring4.interceptor.context.TrasierSpringAccessor;
import com.trasier.client.model.ContentType;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
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
            Span currentSpan = trasierSpringAccessor.createChildSpan(extractOperationName(endpoint));
            currentSpan.setStartTimestamp(System.currentTimeMillis());
            currentSpan.setIncomingContentType(ContentType.XML);

            currentSpan.setIncomingEndpoint(new Endpoint("UNKNOWN"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageContext.getRequest().writeTo(out);
            currentSpan.setIncomingData(out.toString());

            currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
        }

        return super.handleRequest(messageContext, endpoint);
    }

    private String extractOperationName(Object endpoint) {
        if(endpoint instanceof MethodEndpoint) {
            return ((MethodEndpoint)endpoint).getMethod().getName();
        }
        return "UNKNOWN";
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if(trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();

            currentSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            currentSpan.setOutgoingContentType(ContentType.XML);
            currentSpan.setOutgoingEndpoint(new Endpoint(configuration.getSystemName()));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageContext.getResponse().writeTo(out);
            String outgoingData = out.toString();
            currentSpan.setOutgoingData(outgoingData);
            currentSpan.setError(outgoingData.toLowerCase().contains(":fault>"));
            currentSpan.setEndTimestamp(System.currentTimeMillis());

            trasierSpringAccessor.closeSpan(currentSpan);
            client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), currentSpan);
        }

        return super.handleResponse(messageContext, endpoint);
    }

    //TODO handle fault
}