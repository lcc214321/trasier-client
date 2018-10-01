package com.trasier.opentracing.interceptor.spring.ws;

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
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TracingClientInterceptor extends ClientInterceptorAdapter {
    protected final Tracer tracer;

    public TracingClientInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        Scope scope = tracer.buildSpan(extractOperationName(messageContext, null))
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .startActive(true);

        TransportContext context = TransportContextHolder.getTransportContext();
        if (context.getConnection() instanceof URLConnection) {
            URLConnection connection = (URLConnection) context.getConnection();
            tracer.inject(scope.span().context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                @Override
                public Iterator<Map.Entry<String, String>> iterator() {
                    Map<String, String> headers = new HashMap<>();
                    connection.getRequestProperties().entrySet().stream()
                            .filter(entry -> !entry.getValue().isEmpty())
                            .forEach(entry -> headers.put(entry.getKey(), entry.getValue().get(0)));
                    return headers.entrySet().iterator();
                }

                @Override
                public void put(String key, String value) {
                    connection.addRequestProperty(key, value);
                }
            });
        }

        return super.handleRequest(messageContext);
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
