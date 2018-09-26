package com.trasier.client.impl.spring.opentracing.api;

import io.opentracing.SpanContext;

import java.util.Map;

public class TrasierSpanContext implements SpanContext {
    private final String conversationId;
    private final String traceId;
    private final String spanId;

    public TrasierSpanContext(String conversationId, String traceId, String spanId) {
        this.conversationId = conversationId;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return null;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }
}