package com.trasier.client.impl.spring4.interceptor.context;

import com.trasier.client.model.Span;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TrasierSpringAccessor {

    public Span createChildSpan(String operationName) {
        if(isTracing()) {
            Span currentSpan = TrasierContextHolder.getCurrentSpan();
            Span.Builder spanBuilder = Span.newSpan(operationName, currentSpan.getConversationId(), currentSpan.getTraceId(), UUID.randomUUID().toString());
            Span newSpan = spanBuilder.build();
            TrasierContextHolder.setCurrentSpan(newSpan);
            return newSpan;
        }
        return null;
    }

    public Span createSpan(String operationName, String conversationId, String traceId, String spanId) {
        String traceIdNotNull = traceId != null ? traceId : UUID.randomUUID().toString();
        String spanIdNotNull = spanId != null ? spanId : UUID.randomUUID().toString();
        Span.Builder spanBuilder = Span.newSpan(operationName, conversationId, traceIdNotNull, spanIdNotNull);
        spanBuilder.startTimestamp(System.currentTimeMillis());
        Span span = spanBuilder.build();
        TrasierContextHolder.setCurrentSpan(span);
        return span;
    }

    public void closeSpan(Span span) {
        if (span != null) {
            Span currentSpan = TrasierContextHolder.getCurrentSpan();
            currentSpan.setEndTimestamp(System.currentTimeMillis());
            if (!isValidSpan(span, currentSpan)) {
                throw new IllegalArgumentException("Tried to close wrong span.");
            } else {
                TrasierContextHolder.close();
            }
        }
    }

    private boolean isValidSpan(Span span, Span currentSpan) {
        return span.getId().equals(currentSpan.getId()) && span.getTraceId().equals(currentSpan.getTraceId()) && span.getConversationId().equals(currentSpan.getConversationId());
    }

    public Span getCurrentSpan() {
        return TrasierContextHolder.getCurrentSpan();
    }

    public boolean isTracing() {
        return TrasierContextHolder.isTracing();
    }
}