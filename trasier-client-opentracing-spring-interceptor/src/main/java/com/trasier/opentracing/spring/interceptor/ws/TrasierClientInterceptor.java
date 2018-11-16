package com.trasier.opentracing.spring.interceptor.ws;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.opentracing.TrasierSpan;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

public class TrasierClientInterceptor extends ClientInterceptorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(TrasierClientInterceptor.class);

    private final Tracer tracer;

    public TrasierClientInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        boolean result = super.handleRequest(messageContext);

        if (result) {
            TrasierSpan span = (TrasierSpan) tracer.activeSpan();
            if (span != null) {
                Span trasierSpan = span.unwrap();
                String endpointName = extractOutgoingEndpointName(messageContext);
                trasierSpan.setOutgoingEndpoint(new Endpoint(StringUtils.isEmpty(endpointName) ? TrasierConstants.UNKNOWN_OUT : endpointName));
                try {
                    trasierSpan.setIncomingContentType(ContentType.XML);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    messageContext.getRequest().writeTo(out);
                    trasierSpan.setIncomingData(out.toString());
                    trasierSpan.setIncomingHeader(extractHeaders(messageContext.getRequest()));
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }

                trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
            }
        }

        return result;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        TrasierSpan span = (TrasierSpan) tracer.activeSpan();
        if (span != null) {
            Span trasierSpan = span.unwrap();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                trasierSpan.setOutgoingContentType(ContentType.XML);
                messageContext.getResponse().writeTo(out);
                String outgoingData = out.toString();
                trasierSpan.setOutgoingData(outgoingData);
                trasierSpan.setOutgoingHeader(extractHeaders(messageContext.getResponse()));
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
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
            trasierSpan.setStatus(TrasierConstants.STATE_ERROR);
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
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                trasierSpan.setStatus(TrasierConstants.STATE_ERROR);
                trasierSpan.setOutgoingContentType(ContentType.TEXT);
                trasierSpan.setOutgoingData(sw.toString());
            } else {
                trasierSpan.setStatus(TrasierConstants.STATE_OK);
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
                    result.add(header.getName(), header.getValue());
                }
            }

        }
        return result.toSingleValueMap();
    }

}