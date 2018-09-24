package com.trasier.client.impl.spring.interceptor.ws;

import com.trasier.client.Client;
import com.trasier.client.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring.context.TrasierSpringAccessor;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

import java.io.ByteArrayOutputStream;

@Component
public class TrasierEndpointInterceptor extends TrasierAbstractInterceptor implements EndpointInterceptor {

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
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (!configuration.isDeactivated() && trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();

            String operationName = extractOperationName(messageContext, endpoint);
            currentSpan.setName(StringUtils.isEmpty(operationName) ? TrasierConstants.UNKNOWN : operationName);
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (!configuration.isDeactivated() && trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            messageContext.getResponse().writeTo(out);
            String outgoingData = out.toString();
            currentSpan.setStatus(outgoingData.toLowerCase().contains(":fault>") ? "ERROR" : "OK");
        }

        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        if (!configuration.isDeactivated() && trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();
            currentSpan.setStatus("OK");
        }
        return false;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {

    }
}