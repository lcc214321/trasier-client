package com.trasier.client.impl.spring.opentracing.opentracing.api;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

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
            ((TextMap) context).put("X-Conversation-Id", trasierSpanContext.getConversationId());
            ((TextMap) context).put("X-Trace-Id", trasierSpanContext.getTraceId());
            ((TextMap) context).put("X-Span-Id", trasierSpanContext.getSpanId());
        }
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C context) {
        if (context instanceof TextMap) {
            String conversationId = null;
            String traceId = null;
            String spanId = null;

            for (Map.Entry<String, String> c : ((TextMap) context)) {
                if ("X-Conversation-Id".equalsIgnoreCase(c.getKey())) {
                    conversationId = c.getValue();
                } else if ("X-Trace-Id".equalsIgnoreCase(c.getKey())) {
                    traceId = c.getValue();
                } else if ("X-Span-Id".equalsIgnoreCase(c.getKey())) {
                    spanId = c.getValue();
                }
            }

            if (conversationId != null) {
                if (traceId == null) {
                    traceId = UUID.randomUUID().toString();
                }
                if (spanId == null) {
                    spanId = UUID.randomUUID().toString();
                }
                return new TrasierSpanContext(conversationId, traceId, spanId);
            }
        }

        return null;
    }
}
