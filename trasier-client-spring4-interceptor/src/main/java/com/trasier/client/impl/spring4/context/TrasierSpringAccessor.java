package com.trasier.client.impl.spring4.context;

import com.trasier.client.model.Span;
import org.springframework.stereotype.Component;

@Component
public class TrasierSpringAccessor {
    public Span createSpan(String operationName, Span parent) {
        if (parent == null) {
            return createSpan(name);
        }
        return continueSpan(createChild(parent, name));
    }

    public Span close(Span span) {
        if (span == null) {
            return null;
        }

        Span currentSpan = TrasierContextHolder.getCurrentSpan();
        if (!span.equals(currentSpan)) {
            throw new IllegalArgumentException("Tried to close wrong span.");
        } else {
            span.stop();
            SpanContextHolder.close(new SpanContextHolder.SpanFunction() {
                @Override
                public void apply(Span span) {
                    DefaultTracer.this.spanLogger.logStoppedSpan(savedSpan, span);
                }
            });
        }
        return savedSpan;
    }

    protected Span createChild(Span parent, String name) {
        long id = createId();
        if (parent == null) {
            Span span = Span.builder().name(name)
                    .traceIdHigh(this.traceId128 ? createId() : 0L)
                    .traceId(id)
                    .spanId(id).build();
            span = sampledSpan(span, this.defaultSampler);
            this.spanLogger.logStartedSpan(null, span);
            return span;
        }
        else {
            if (!isTracing()) {
                SpanContextHolder.push(parent, true);
            }
            Span span = Span.builder().name(name)
                    .traceIdHigh(parent.getTraceIdHigh())
                    .traceId(parent.getTraceId()).parent(parent.getSpanId()).spanId(id)
                    .processId(parent.getProcessId()).savedSpan(parent)
                    .exportable(parent.isExportable()).build();
            this.spanLogger.logStartedSpan(parent, span);
            return span;
        }
    }


    public Span getCurrentSpan() {
        return TrasierContextHolder.getCurrentSpan();
    }

    public boolean isTracing() {
        return TrasierContextHolder.isTracing();
    }
}