/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package com.trasier.client.impl.spring.opentracing.api;

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
        scopeManager.deactivate(span);
    }

    @Override
    public Span span() {
        return span;
    }
}
