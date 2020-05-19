/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2020.
 */
package com.trasier.client.interceptor;

import com.trasier.client.api.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public final class SafeSpanResolverInterceptorInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SafeSpanResolverInterceptorInvoker.class);

    private final List<TrasierSpanResolverInterceptor> samplingInterceptors;

    public SafeSpanResolverInterceptorInvoker(List<TrasierSpanResolverInterceptor> samplingInterceptors) {
        this.samplingInterceptors = samplingInterceptors != null ? samplingInterceptors : Collections.emptyList();
    }

    public void invokeOnRequestUriResolved(Span trasierSpan, String url) {
        if (!trasierSpan.isCancel()) {
            for (TrasierSpanResolverInterceptor samplingInterceptor : samplingInterceptors) {
                try {
                    samplingInterceptor.interceptRequestUrlResolved(trasierSpan, url);
                } catch(Exception e) {
                    LOGGER.error("Error while intercepting span url. Span is not cancelled", e);
                }
            }
        }
    }

    public void invokeOnMetadataResolved(Span trasierSpan) {
        if (!trasierSpan.isCancel()) {
            for (TrasierSpanResolverInterceptor samplingInterceptor : samplingInterceptors) {
                try {
                    samplingInterceptor.interceptMetdataResolved(trasierSpan);
                } catch (Exception e) {
                    LOGGER.error("Error while intercepting span metadata. Span is not cancelled.", e);
                }
            }
        }
    }

}
