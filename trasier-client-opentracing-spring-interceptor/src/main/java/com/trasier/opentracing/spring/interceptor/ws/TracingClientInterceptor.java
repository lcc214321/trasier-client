package com.trasier.opentracing.spring.interceptor.ws;

import com.trasier.client.api.Span;
import com.trasier.client.interceptor.TrasierSamplingInterceptor;
import com.trasier.client.opentracing.TrasierScope;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TracingClientInterceptor extends ClientInterceptorAdapter {
    private final Tracer tracer;
    private final List<TrasierSamplingInterceptor> samplingInterceptors;

    public TracingClientInterceptor(Tracer tracer, List<TrasierSamplingInterceptor> samplingInterceptors) {
        this.tracer = tracer;
        this.samplingInterceptors = samplingInterceptors;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        Scope scope = tracer.buildSpan(extractOperationName(messageContext, null))
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .startActive(true);

        if (scope instanceof TrasierScope) {
            Span unwrap = ((TrasierSpan) scope.span()).unwrap();
            Map<String, Object> params = new HashMap<>();
            params.put("url", extractUrlPath(messageContext));
            for (TrasierSamplingInterceptor samplingInterceptor : samplingInterceptors) {
                if (!samplingInterceptor.shouldSample(unwrap, params)) {
                    unwrap.setCancel(true);
                }
            }
        }

        TransportContext context = TransportContextHolder.getTransportContext();
        if (context.getConnection() instanceof HttpUrlConnection) {
            final HttpUrlConnection httpConnection = (HttpUrlConnection) context.getConnection();
            tracer.inject(scope.span().context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                @Override
                public Iterator<Map.Entry<String, String>> iterator() {
                    return null;
                }

                @Override
                public void put(String key, String value) {
                    try {
                        httpConnection.addRequestHeader(key, value);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
        }

        return super.handleRequest(messageContext);
    }

    private String extractUrlPath(MessageContext messageContext) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
            String soapAction = soapMessage.getSoapAction();
            if (soapAction != null) {
                soapAction = soapAction.replaceAll("\"", "");
            }
            return soapAction;
        }
        return "";
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return super.handleResponse(messageContext);
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return super.handleFault(messageContext);
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        tracer.scopeManager().active().close();
        super.afterCompletion(messageContext, ex);
    }

    private String extractOperationName(MessageContext messageContext, Object endpoint) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();

            String soapAction = soapMessage.getSoapAction();
            SoapBody body = soapMessage.getSoapBody();
            if (body.getPayloadSource() instanceof DOMSource) {
                Node node = ((DOMSource) body.getPayloadSource()).getNode();
                return node.getLocalName();
            } else if (!StringUtils.isEmpty(soapAction)) {
                soapAction = soapAction.replaceAll("\"", "");
                String[] soapActionArray = soapAction.split("/");
                return soapActionArray[soapActionArray.length - 1];
            }
        }

        return extractOperationName(endpoint);
    }

    private String extractOperationName(Object endpoint) {
        if (endpoint instanceof MethodEndpoint) {
            return ((MethodEndpoint) endpoint).getMethod().getName();
        }

        return "UNKNOWN-WS-CALL";
    }
}
