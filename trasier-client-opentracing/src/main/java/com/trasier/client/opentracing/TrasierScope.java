package com.trasier.client.opentracing;

import io.opentracing.Scope;
import io.opentracing.Span;

public class TrasierScope implements Scope {
    private final TrasierScopeManager scopeManager;
    private TrasierSpan span;
    private boolean finishSpanOnClose;

    public TrasierScope(TrasierScopeManager scopeManager, TrasierSpan span, boolean finishSpanOnClose) {
        this.scopeManager = scopeManager;
        this.span = span;
        this.finishSpanOnClose = finishSpanOnClose;
    }

    @Override
    public void close() {
        if (finishSpanOnClose) {
            span.finish();
        }
        scopeManager.deactivate(this);
    }

    @Override
    public Span span() {
        return span;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrasierScope that = (TrasierScope) o;

        return span != null ? span.equals(that.span) : that.span == null;
    }

    @Override
    public int hashCode() {
        return span != null ? span.hashCode() : 0;
    }
}
