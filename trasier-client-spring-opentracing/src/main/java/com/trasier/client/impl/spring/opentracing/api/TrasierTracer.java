package com.trasier.client.impl.spring.opentracing.api;

import com.trasier.client.Client;
import com.trasier.client.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrasierTracer implements Tracer {
    private Client client;
    private TrasierClientConfiguration configuration;
    private TrasierScopeManager trasierScopeManager;

    public TrasierTracer(Client client, TrasierClientConfiguration configuration, TrasierScopeManager trasierScopeManager) {
        this.client = client;
        this.configuration = configuration;
        this.trasierScopeManager = trasierScopeManager;
    }

    @Override
    public ScopeManager scopeManager() {
        return trasierScopeManager;
    }

    @Override
    public Span activeSpan() {
        Scope active = trasierScopeManager.active();
        return active != null ? active.span() : null;
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new TrasierSpanBuilder(client, configuration, this, operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C context) {
        if (context instanceof TextMap) {
            TrasierSpanContext trasierSpanContext = (TrasierSpanContext) spanContext;
            ((TextMap) context).put(TrasierConstants.HEADER_CONVERSATION_ID, trasierSpanContext.getConversationId());
            ((TextMap) context).put(TrasierConstants.HEADER_TRACE_ID, trasierSpanContext.getTraceId());
            ((TextMap) context).put(TrasierConstants.HEADER_SPAN_ID, trasierSpanContext.getSpanId());
            trasierSpanContext.baggageItems().forEach(entry -> {
                ((TextMap) context).put(entry.getKey(), entry.getValue());
            });
            ((TextMap) context).put(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME, configuration.getSystemName());
        }
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C context) {
        if (context instanceof TextMap) {
            String conversationId = null;
            String traceId = null;
            String spanId = null;

            Map<String, String> baggageItems = new HashMap<>();
            for (Map.Entry<String, String> entry : ((TextMap) context)) {
                if (TrasierConstants.HEADER_CONVERSATION_ID.equalsIgnoreCase(entry.getKey())) {
                    conversationId = entry.getValue();
                } else if (TrasierConstants.HEADER_TRACE_ID.equalsIgnoreCase(entry.getKey())) {
                    traceId = entry.getValue();
                } else if (TrasierConstants.HEADER_SPAN_ID.equalsIgnoreCase(entry.getKey())) {
                    spanId = entry.getValue();
                } else {
                    baggageItems.put(entry.getKey(), entry.getValue());
                }
            }

            if (conversationId != null) {
                if (traceId == null) {
                    traceId = UUID.randomUUID().toString();
                }
                if (spanId == null) {
                    spanId = UUID.randomUUID().toString();
                }
                TrasierSpanContext spanContext = new TrasierSpanContext(conversationId, traceId, spanId, baggageItems);
                return spanContext;
            }
        }

        return null;
    }
}
