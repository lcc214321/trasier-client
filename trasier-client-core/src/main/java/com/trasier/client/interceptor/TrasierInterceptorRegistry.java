
package com.trasier.client.interceptor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TrasierInterceptorRegistry {

    public List<TrasierSpanInterceptor> spanInterceptors = new LinkedList<>();

    public TrasierInterceptorRegistry() {
        this.spanInterceptors.add(new DefaultTrasierSpanInterceptor());
    }

    public void addSpanInterceptor(TrasierSpanInterceptor interceptor) {
        spanInterceptors.add(interceptor);
    }

    public void removeSpanInterceptor(TrasierSpanInterceptor interceptor) {
        spanInterceptors.remove(interceptor);
    }

    public Collection<TrasierSpanInterceptor> getSpanInterceptors() {
        return spanInterceptors;
    }
}
