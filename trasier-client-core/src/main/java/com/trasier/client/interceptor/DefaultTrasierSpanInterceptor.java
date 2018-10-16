
package com.trasier.client.interceptor;

import com.trasier.client.api.Span;

public class DefaultTrasierSpanInterceptor implements TrasierSpanInterceptor {

    @Override
    public void intercept(Span span) {
    }

    @Override
    public boolean cancel(Span span) {
        return false;
    }

}
