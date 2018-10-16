
package com.trasier.client.interceptor;

import com.trasier.client.api.Span;

public interface TrasierSpanInterceptor {

    void intercept(Span span);

    /**
     * Gives the possibility to cancel sending the span to the tracing backend.
     * @return <code>true</code> if the span should not be send, <code>false</code> otherwise
     */
    boolean cancel(Span span);
}
