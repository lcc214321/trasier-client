package com.trasier.client.impl.spring4.context;

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
        if(traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        if(spanId == null) {
            spanId = UUID.randomUUID().toString();
        }

        Span.Builder spanBuilder = Span.newSpan(operationName, conversationId, traceId, spanId);
        spanBuilder.startTimestamp(System.currentTimeMillis());
        Span span = spanBuilder.build();
        TrasierContextHolder.setCurrentSpan(span);
        return span;
    }


    public void closeSpan(Span span) {
        if (span != null) {
            Span currentSpan = TrasierContextHolder.getCurrentSpan();
            currentSpan.setEndTimestamp(System.currentTimeMillis());
            if (!span.equals(currentSpan)) {
                throw new IllegalArgumentException("Tried to close wrong span.");
            } else {
                TrasierContextHolder.close();
            }
        }
    }

//    protected Span createChild(Span parent, String name) {
//        long id = createId();
//        if (parent == null) {
//            Span span = Span.builder().name(name)
//                    .traceIdHigh(this.traceId128 ? createId() : 0L)
//                    .traceId(id)
//                    .spanId(id).build();
//            span = sampledSpan(span, this.defaultSampler);
//            this.spanLogger.logStartedSpan(null, span);
//            return span;
//        }
//        else {
//            if (!isTracing()) {
//                SpanContextHolder.push(parent, true);
//            }
//            Span span = Span.builder().name(name)
//                    .traceIdHigh(parent.getTraceIdHigh())
//                    .traceId(parent.getTraceId()).parent(parent.getSpanId()).spanId(id)
//                    .processId(parent.getProcessId()).savedSpan(parent)
//                    .exportable(parent.isExportable()).build();
//            this.spanLogger.logStartedSpan(parent, span);
//            return span;
//        }
//    }

    public Span getCurrentSpan() {
        return TrasierContextHolder.getCurrentSpan();
    }

    public boolean isTracing() {
        return TrasierContextHolder.isTracing();
    }
}