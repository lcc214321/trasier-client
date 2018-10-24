package com.trasier.client.opentracing;

import com.trasier.client.api.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Map;

public class TrasierSpan implements Span {
    private final Client client;
    private TrasierClientConfiguration configuration;
    private final com.trasier.client.api.Span wrapped;
    private Map<String, String> baggageItems;

    public TrasierSpan(Client client, TrasierClientConfiguration configuration, com.trasier.client.api.Span wrapped, Map<String, String> baggageItems) {
        this.client = client;
        this.configuration = configuration;
        this.wrapped = wrapped;
        this.baggageItems = baggageItems;
    }

    @Override
    public SpanContext context() {
        return new TrasierSpanContext(wrapped.getConversationId(), wrapped.getTraceId(), wrapped.getId(), baggageItems);
    }

    @Override
    public Span setTag(String key, String value) {
        wrapped.getTags().put(key, value);
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        wrapped.getTags().put(key, Boolean.toString(value));
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        if(value != null) {
            wrapped.getTags().put(key, value.toString());
        }
        return this;
    }

    @Override
    public Span log(Map<String, ?> values) {
        //TODO Hackergarten
        return this;
    }

    @Override
    public Span log(long micros, Map<String, ?> values) {
        //TODO Hackergarten
        return this;
    }

    @Override
    public Span log(String value) {
        //TODO Hackergarten
        return null;
    }

    @Override
    public Span log(long micros, String value) {
        //TODO Hackergarten
        return this;
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        baggageItems.put(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return baggageItems.get(key);
    }

    @Override
    public Span setOperationName(String operationName) {
        wrapped.setName(operationName);
        return this;
    }

    @Override
    public void finish() {
        finish(System.currentTimeMillis());
    }

    @Override
    public void finish(long endTimestamp) {
        wrapped.setEndTimestamp(endTimestamp);
        client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), unwrap());
    }

    public com.trasier.client.api.Span unwrap() {
        return wrapped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrasierSpan that = (TrasierSpan) o;

        return wrapped != null ? wrapped.equals(that.wrapped) : that.wrapped == null;
    }

    @Override
    public int hashCode() {
        return wrapped != null ? wrapped.hashCode() : 0;
    }
}