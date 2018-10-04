package com.trasier.client.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

import java.util.ArrayDeque;
import java.util.Deque;

public class TrasierScopeManager implements ScopeManager {
    private final ThreadLocal<Deque<Scope>> scopes = ThreadLocal.withInitial(ArrayDeque::new);

    @Override
    public Scope activate(Span span, boolean finishSpanOnClose) {
        TrasierScope scope = new TrasierScope(this, (TrasierSpan) span, finishSpanOnClose);
        scopes.get().addFirst(scope);
        return scope;
    }

    @Override
    public Scope active() {
        return scopes.get().peekFirst();
    }

    public void deactivate(TrasierSpan span) {
        scopes.get().remove(span);
    }
}