package com.trasier.client.impl.spring4.context;

import com.trasier.client.model.Span;
import org.springframework.core.NamedThreadLocal;

public class TrasierContextHolder {
    private static final ThreadLocal<TrasierContext> CURRENT_TRASIER_CONTEXT = new NamedThreadLocal<>("TrasierContext");

    static Span getCurrentSpan() {
        return isTracing() ? CURRENT_TRASIER_CONTEXT.get().span : null;
    }

    static void setCurrentSpan(Span span) {
        push(span, false);
    }

    static void removeCurrentSpan() {
        CURRENT_TRASIER_CONTEXT.remove();
    }

    static boolean isTracing() {
        return CURRENT_TRASIER_CONTEXT.get() != null;
    }

    static void close() {
        TrasierContext current = CURRENT_TRASIER_CONTEXT.get();
        CURRENT_TRASIER_CONTEXT.remove();
        while (current != null) {
            current = current.parent;
            if (current != null && !current.autoClose) {
                CURRENT_TRASIER_CONTEXT.set(current);
            }
        }
    }

    static void push(Span span, boolean autoClose) {
        if (isCurrent(span)) {
            return;
        }
        CURRENT_TRASIER_CONTEXT.set(new TrasierContext(span, autoClose));
    }

    private static boolean isCurrent(Span span) {
        if (span == null || CURRENT_TRASIER_CONTEXT.get() == null) {
            return false;
        }
        return span.equals(CURRENT_TRASIER_CONTEXT.get().span);
    }

    private static class TrasierContext {
        Span span;
        boolean autoClose;
        TrasierContext parent;

        TrasierContext(Span span, boolean autoClose) {
            this.span = span;
            this.autoClose = autoClose;
            this.parent = CURRENT_TRASIER_CONTEXT.get();
        }
    }
}