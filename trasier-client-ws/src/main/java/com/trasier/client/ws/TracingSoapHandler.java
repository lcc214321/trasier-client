package com.trasier.client.ws;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierScopeManager;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.client.opentracing.TrasierTracer;
import com.trasier.client.util.LocalEndpointHolder;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TracingSoapHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracingSoapHandler.class);

    private final Tracer tracer;
    private final TrasierClientConfiguration clientConfig;

    public TracingSoapHandler(Tracer tracer, TrasierClientConfiguration clientConfig) {
        this.tracer = tracer;
        this.clientConfig = clientConfig;
    }

    @Override
    public void close(MessageContext messageContext) {
    }

    @Override
    public Set<QName> getHeaders() {
        return new HashSet<>();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        if (tracer instanceof TrasierTracer && clientConfig.isActivated()) {
            handle(soapMessageContext, false);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        if (tracer instanceof TrasierTracer && clientConfig.isActivated()) {
            handle(soapMessageContext, true);
        }
        return true;
    }

    private void handle(SOAPMessageContext soapMessageContext, boolean isFault) {
        TrasierTracer tracer = (TrasierTracer) this.tracer;
        TrasierSpan trasierSpan;

        boolean isRequest = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (isRequest) {
            Method method = (Method) soapMessageContext.get("java.lang.reflect.Method");
            String methodName = method != null ? method.getName() : "soap";
            trasierSpan = (TrasierSpan) tracer.buildSpan(methodName).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
            tracer.activateSpan(trasierSpan);
        } else {
            trasierSpan = (TrasierSpan) tracer.activeSpan();
        }
        Span span = trasierSpan.unwrap();

        try {
            tracer.inject(trasierSpan.context(), Format.Builtin.HTTP_HEADERS, new SOAPMessageInjectAdapter(soapMessageContext, clientConfig, span));

            String message = getMessagePayload(soapMessageContext);

            if (isRequest) {
                span.setIncomingContentType(ContentType.XML);
                span.setIncomingEndpoint(LocalEndpointHolder.getLocalEndpoint(clientConfig.getSystemName()));
                URL url = getHttpConnectionUrl(soapMessageContext);
                if (url != null) {
                    span.setOutgoingEndpoint(createEndpoint(url.getHost().substring(0, url.getHost().indexOf(".")), url.getHost()));
                }
                span.setStartTimestamp(new Date().getTime());
                span.setIncomingHeader(createIncommingHeaders(url));
                span.setIncomingData(message);
            } else {
                span.setOutgoingContentType(ContentType.XML);
                span.setEndTimestamp(new Date().getTime());
                span.setOutgoingData(message);
                span.setStatus(isFault ? "ERROR" : "OK");
                TrasierScopeManager scopeManager = (TrasierScopeManager)tracer.scopeManager();
                scopeManager.activeScope().close();
                trasierSpan.finish();
            }
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    private URL getHttpConnectionUrl(SOAPMessageContext soapMessageContext) {
        HttpURLConnection httpConnection = (HttpURLConnection) soapMessageContext.get("http.connection");
        return httpConnection != null ? httpConnection.getURL() : null;
    }

    private String getMessagePayload(SOAPMessageContext soapMessageContext) throws SOAPException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        soapMessageContext.getMessage().writeTo(out);
        return out.toString();
    }

    private Map<String, String> createIncommingHeaders(URL url) {
        Map<String, String> traceableHeaders = new HashMap<>();
        if (url != null) {
            traceableHeaders.put("url", url.toString());
        }
        return traceableHeaders;
    }

    private static Endpoint createEndpoint(String name, String host) {
        Endpoint endpoint = new Endpoint(name);
        endpoint.setHostname(host);
        return endpoint;
    }

}
