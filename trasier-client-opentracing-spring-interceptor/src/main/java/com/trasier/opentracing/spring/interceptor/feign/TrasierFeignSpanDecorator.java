package com.trasier.opentracing.spring.interceptor.feign;

import com.trasier.client.configuration.TrasierClientConfiguration;
import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Span;

public class TrasierFeignSpanDecorator implements FeignSpanDecorator {

    private final TrasierClientConfiguration configuration;

    public TrasierFeignSpanDecorator(TrasierClientConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onRequest(feign.Request request, feign.Request.Options options, Span span) {

    }

    @Override
    public void onResponse(feign.Response response, feign.Request.Options options, Span span) {

    }

    @Override
    public void onError(Exception e, feign.Request request, Span span) {

    }

}