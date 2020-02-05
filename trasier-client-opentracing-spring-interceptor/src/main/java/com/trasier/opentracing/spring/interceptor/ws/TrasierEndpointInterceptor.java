package com.trasier.opentracing.spring.interceptor.ws;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.SmartEndpointInterceptor;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierSpan;

import io.opentracing.Tracer;

/**
 * Enrich data that we could not access much higher on the filter level.
 * The rest will be handeled by the TrasierServletFilterSpanDecorator
 */
public class TrasierEndpointInterceptor implements SmartEndpointInterceptor {

    private final Tracer tracer;
    private final TrasierClientConfiguration configuration;

    public TrasierEndpointInterceptor(Tracer tracer, TrasierClientConfiguration configuration) {
        this.tracer = tracer;
        this.configuration = configuration;
    }

    @Override
    public boolean shouldIntercept(MessageContext messageContext, Object endpoint) {
        return configuration.isActivated();
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object o) {
        TrasierSpan activeSpan = (TrasierSpan) tracer.activeSpan();
        Span trasierSpan = activeSpan.unwrap();
        if (!trasierSpan.isCancel()) {
            String operationName = WSUtil.extractOperationName(messageContext, o);
            if (operationName != null && !WSUtil.UNKNOWN_WS_CALL.equals(operationName)) {
                trasierSpan.setName(operationName);
            }
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageConttext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
    }

}
