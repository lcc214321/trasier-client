package com.trasier.client.opentracing;

import io.opentracing.SpanContext;

import java.util.Map;

public class TrasierSpanContext implements SpanContext {
    private final String conversationId;
    private final String traceId;
    private final String spanId;
    private final Map<String, String> baggageItems;

    public TrasierSpanContext(String conversationId, String traceId, String spanId, Map<String, String> baggageItems) {
        this.conversationId = conversationId;
        this.traceId = traceId;
        this.spanId = spanId;
        this.baggageItems = baggageItems;
    }

    public Map<String, String> getBaggageItems() {
        return baggageItems;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return baggageItems.entrySet();
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