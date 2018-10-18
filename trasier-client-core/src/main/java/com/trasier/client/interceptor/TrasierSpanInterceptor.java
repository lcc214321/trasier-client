
package com.trasier.client.interceptor;

import com.trasier.client.api.Span;

public interface TrasierSpanInterceptor {

    /**
     * Gives the possibility to intercept the Span before it is send to the tracing backend.
     * One can change the span name, mask out message payload or prevent the span from being send to the tracing backend.
     */
    void intercept(Span span);

}
