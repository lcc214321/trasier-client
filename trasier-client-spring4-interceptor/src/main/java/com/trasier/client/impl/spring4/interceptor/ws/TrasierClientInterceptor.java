package com.trasier.client.impl.spring4.interceptor.ws;

import com.trasier.client.Client;
import com.trasier.client.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring4.interceptor.context.TrasierSpringAccessor;
import com.trasier.client.model.ContentType;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class TrasierClientInterceptor extends TrasierAbstractInterceptor implements ClientInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(TrasierClientInterceptor.class);

    private final Client client;
    private final TrasierClientConfiguration configuration;
    private final TrasierSpringAccessor trasierSpringAccessor;

    @Autowired
    public TrasierClientInterceptor(Client client, TrasierClientConfiguration configuration, TrasierSpringAccessor trasierSpringAccessor) {
        this.client = client;
        this.configuration = configuration;
        this.trasierSpringAccessor = trasierSpringAccessor;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        if (trasierSpringAccessor.isTracing()) {
            String operationName = extractOperationName(messageContext, null);
            Span currentSpan = trasierSpringAccessor.createChildSpan(StringUtils.isEmpty(operationName) ? TrasierConstants.UNKNOWN : operationName);
            currentSpan.setStartTimestamp(System.currentTimeMillis());
            currentSpan.setIncomingContentType(ContentType.XML);
            currentSpan.setIncomingEndpoint(new Endpoint(configuration.getSystemName()));
            String endpointName = extractOutgoingEndpointName(messageContext);
            currentSpan.setOutgoingEndpoint(new Endpoint(StringUtils.isEmpty(endpointName) ? TrasierConstants.UNKNOWN : endpointName));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                messageContext.getRequest().writeTo(out);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            currentSpan.setIncomingData(out.toString());

            currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        if (trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                messageContext.getResponse().writeTo(out);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            String outgoingData = out.toString();
            currentSpan.setOutgoingData(outgoingData);
        }

        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        if (trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                messageContext.getResponse().writeTo(out);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            String outgoingData = out.toString();
            currentSpan.setOutgoingData(outgoingData);
            currentSpan.setError(true);
        }

        return false;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception e) throws WebServiceClientException {
        if (trasierSpringAccessor.isTracing()) {
            Span currentSpan = trasierSpringAccessor.getCurrentSpan();
            currentSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            currentSpan.setOutgoingContentType(ContentType.XML);

            if (e != null) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                currentSpan.setError(true);
                currentSpan.setOutgoingContentType(ContentType.TEXT);
                currentSpan.setOutgoingData(sw.toString());
            }

            currentSpan.setEndTimestamp(System.currentTimeMillis());
            trasierSpringAccessor.closeSpan(currentSpan);
            client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), currentSpan);
        }
    }
}