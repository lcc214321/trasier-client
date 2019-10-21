package com.trasier.opentracing.spring.interceptor.ws;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.client.util.ExceptionUtils;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Node;

import javax.xml.soap.MimeHeader;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;

public class TrasierClientInterceptor extends ClientInterceptorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(TrasierClientInterceptor.class);

    private final Tracer tracer;

    private Endpoint localEndpoint;

    private final TrasierClientConfiguration configuration;

    public TrasierClientInterceptor(Tracer tracer, TrasierClientConfiguration configuration) {
        this.tracer = tracer;
        this.configuration = configuration;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        boolean result = super.handleRequest(messageContext);

        if (result) {
            TrasierSpan span = (TrasierSpan) tracer.activeSpan();
            if (span != null) {
                Span trasierSpan = span.unwrap();
                String endpointName = extractOutgoingEndpointName(messageContext);
                Endpoint outgoingEndpoint = new Endpoint(StringUtils.isEmpty(endpointName) ? TrasierConstants.UNKNOWN_OUT : endpointName);
                trasierSpan.setOutgoingEndpoint(outgoingEndpoint);
                trasierSpan.getIncomingHeader().putAll(extractHeaders(messageContext.getRequest()));
                enhanceIncomingEndpoint(trasierSpan);
                trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
                trasierSpan.setIncomingContentType(ContentType.XML);
                if (!configuration.isPayloadTracingDisabled()) {
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        messageContext.getRequest().writeTo(out);
                        trasierSpan.setIncomingData(out.toString());
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        TrasierSpan span = (TrasierSpan) tracer.activeSpan();
        if (span != null) {
            Span trasierSpan = span.unwrap();
            trasierSpan.getOutgoingHeader().putAll(extractHeaders(messageContext.getResponse()));
            trasierSpan.setOutgoingContentType(ContentType.XML);

            if (!configuration.isPayloadTracingDisabled()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    messageContext.getResponse().writeTo(out);
                    trasierSpan.setOutgoingData(out.toString());
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        return super.handleResponse(messageContext);
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        handleResponse(messageContext);

        TrasierSpan span = (TrasierSpan) tracer.activeSpan();
        if (span != null) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setStatus(TrasierConstants.STATUS_ERROR);
        }

        return super.handleFault(messageContext);
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception e) throws WebServiceClientException {
        TrasierSpan span = (TrasierSpan) tracer.activeSpan();
        if (span != null) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());

            if (e != null) {
                if (!configuration.isPayloadTracingDisabled()) {
                    trasierSpan.setOutgoingData(ExceptionUtils.getString(e));
                }
                trasierSpan.setStatus(TrasierConstants.STATUS_ERROR);
                trasierSpan.setOutgoingContentType(ContentType.TEXT);
            } else {
                trasierSpan.setStatus(TrasierConstants.STATUS_OK);
            }
        }

        super.afterCompletion(messageContext, e);
    }

    private String extractOutgoingEndpointName(MessageContext messageContext) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
            SoapBody body = soapMessage.getSoapBody();
            if (body.getPayloadSource() instanceof DOMSource) {
                Node node = ((DOMSource) body.getPayloadSource()).getNode();
                if (!StringUtils.isEmpty(node.getPrefix())) {
                    return node.getPrefix();
                }
                String namespace = node.getNamespaceURI();
                if (!StringUtils.isEmpty(namespace)) {
                    String[] soapActionArray = namespace.split("/");
                    return soapActionArray[soapActionArray.length - 1];
                }
            }
        }
        return null;
    }

    private Map<String, String> extractHeaders(WebServiceMessage message) {
        LinkedMultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (message instanceof SaajSoapMessage) {
            SaajSoapMessage soapMessage = (SaajSoapMessage) message;
            Iterator allHeaders = soapMessage.getSaajMessage().getMimeHeaders().getAllHeaders();
            while (allHeaders.hasNext()) {
                Object next = allHeaders.next();
                if (next instanceof MimeHeader) {
                    MimeHeader header = (MimeHeader) next;
                    if (header.getValue() != null) {
                        result.add(header.getName(), header.getValue());
                    }
                }
            }

        }
        return result.toSingleValueMap();
    }

    private void enhanceIncomingEndpoint(com.trasier.client.api.Span span) {
        // no synchronisation on purpose
        if (this.localEndpoint == null) {
            Endpoint endpoint = new Endpoint(configuration.getSystemName());
            InetAddress inetAddress = getInetAddress();
            if (inetAddress != null) {
                endpoint.setHostname(inetAddress.getHostName());
                endpoint.setIpAddress(inetAddress.getHostAddress());
            }
            this.localEndpoint = endpoint;
        }
        span.setIncomingEndpoint(localEndpoint);
    }

    private InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // ignore
        }
        return null;
    }

}