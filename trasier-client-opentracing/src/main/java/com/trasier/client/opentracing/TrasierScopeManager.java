package com.trasier.client.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

import java.util.ArrayDeque;
import java.util.Deque;

public class TrasierScopeManager implements ScopeManager {
    private final ThreadLocal<Deque<Scope>> scopes = ThreadLocal.withInitial(ArrayDeque::new);

    @Override
    public Scope activate(Span span) {
        TrasierScope scope = new TrasierScope((TrasierSpan) span);
        scopes.get().addFirst(scope);
        return scope;
    }

    @Override
    public Span activeSpan() {
        TrasierScope trasierScope = (TrasierScope) scopes.get().peekFirst();
        return trasierScope != null ? trasierScope.getSpan() : null;
    }

    public Scope closeActiveSpan() {
        Scope scope = scopes.get().removeFirst();
        scope.close();
        return scope;
    }
}