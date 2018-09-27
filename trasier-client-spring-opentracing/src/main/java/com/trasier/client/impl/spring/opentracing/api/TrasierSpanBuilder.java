package com.trasier.client.impl.spring.opentracing.api;

import com.trasier.client.Client;
import com.trasier.client.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.model.Endpoint;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrasierSpanBuilder implements Tracer.SpanBuilder {
    private Client client;
    private TrasierClientConfiguration configuration;
    private TrasierTracer tracer;
    private String conversationId;
    private String traceId;
    private String spanId;
    private String operationName;
    private long startTimestamp;
    private boolean ignoreActiveSpan;
    private TrasierSpanContext reference;
    private Map<String, String> tags;

    public TrasierSpanBuilder(Client client, TrasierClientConfiguration configuration, TrasierTracer tracer, String operationName) {
        this.client = client;
        this.configuration = configuration;
        this.tracer = tracer;
        this.operationName = operationName;
        this.startTimestamp = System.currentTimeMillis();
        this.tags = new HashMap<>();

        conversationId = UUID.randomUUID().toString();
        traceId = conversationId;
        spanId = UUID.randomUUID().toString();
    }

    @Override
    public TrasierSpanBuilder asChildOf(SpanContext parent) {
        if(parent != null) {
            conversationId = ((TrasierSpanContext)parent).getConversationId();
            traceId = ((TrasierSpanContext)parent).getTraceId();
        }
        return addReference(References.CHILD_OF, parent);
    }

    @Override
    public TrasierSpanBuilder asChildOf(Span parent) {
        return asChildOf(parent != null ? parent.context() : null);
    }

    @Override
    public TrasierSpanBuilder addReference(String type, SpanContext context) {
        if (reference != null || context == null) {
            return this;
        }
        if (References.CHILD_OF.equals(type) || References.FOLLOWS_FROM.equals(type)) {
            this.reference = (TrasierSpanContext) context;
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        this.ignoreActiveSpan = true;
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        if(value != null) {
            tags.put(key, value);
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        tags.put(key, Boolean.toString(value));
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        if(value != null) {
            tags.put(key, value.toString());
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
        return this;
    }

    @Override
    public Scope startActive(boolean finishSpanOnClose) {
        if (!ignoreActiveSpan) {
            Scope parent = tracer.scopeManager().active();
            if (parent != null) {
                asChildOf(parent.span());
            }
        }
        return tracer.scopeManager().activate(start(), finishSpanOnClose);
    }

    @Override
    public Span startManual() {
        return null;
    }

    @Override
    public Span start() {
        boolean server = Tags.SPAN_KIND_SERVER.equals(tags.get(Tags.SPAN_KIND.getKey()));

        if (reference == null && !ignoreActiveSpan) {
            Scope parent = tracer.scopeManager().active();
            if (parent != null) {
                asChildOf(parent.span());
            }
        }

        if(server && reference != null) {
            spanId = reference.getSpanId();
        }

        com.trasier.client.model.Span.SpanBuilder wrappedBuilder = com.trasier.client.model.Span.newSpan(operationName, conversationId, traceId, spanId);
        wrappedBuilder.incomingEndpoint(new Endpoint(TrasierConstants.UNKNOWN_IN));
        wrappedBuilder.outgoingEndpoint(new Endpoint(TrasierConstants.UNKNOWN_OUT));
        wrappedBuilder.startTimestamp(startTimestamp);
        if(reference != null) {
            wrappedBuilder.parentId(reference.getSpanId());
        }
        com.trasier.client.model.Span wrapped = wrappedBuilder.build();
        wrapped.setTags(tags);

        TrasierSpan span = new TrasierSpan(client, configuration, wrapped);
        return span;
    }
}
