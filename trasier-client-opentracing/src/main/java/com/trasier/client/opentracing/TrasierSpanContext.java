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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrasierSpanContext that = (TrasierSpanContext) o;

        if (conversationId != null ? !conversationId.equals(that.conversationId) : that.conversationId != null)
            return false;
        if (traceId != null ? !traceId.equals(that.traceId) : that.traceId != null) return false;
        return spanId != null ? spanId.equals(that.spanId) : that.spanId == null;
    }

    @Override
    public int hashCode() {
        int result = conversationId != null ? conversationId.hashCode() : 0;
        result = 31 * result + (traceId != null ? traceId.hashCode() : 0);
        result = 31 * result + (spanId != null ? spanId.hashCode() : 0);
        return result;
    }
}