package com.trasier.client.spring.noop;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.TrasierCompressSpanInterceptor;
import com.trasier.client.spring.client.TrasierSpringClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("trasierSpringClient")
public class TrasierSpringNoopClient implements TrasierSpringClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierSpringNoopClient.class);

    private final TrasierClientConfiguration clientConfiguration;

    private TrasierCompressSpanInterceptor compressSpanInterceptor;

    @Autowired(required = false)
    private final List<TrasierSpanInterceptor> spanInterceptors = new ArrayList<>();

    @Autowired
    public TrasierSpringNoopClient(TrasierClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;

        if (clientConfiguration.isActivated()) {
            this.compressSpanInterceptor = new TrasierCompressSpanInterceptor();
        }
    }

    @Override
    public boolean sendSpan(Span span) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        if (span.isCancel()) {
            return false;
        }

        applyInterceptors(span);

        if (span.isCancel()) {
            return false;
        }

        compressSpanInterceptor.intercept(span);

        //nothing

        return true;
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        return true;
    }

    private void applyInterceptors(Span span) {
        for (TrasierSpanInterceptor spanInterceptor : this.spanInterceptors) {
            spanInterceptor.intercept(span);
        }
    }

    @Override
    public void close() {
    }

    public void setCompressSpanInterceptor(TrasierCompressSpanInterceptor compressSpanInterceptor) {
        this.compressSpanInterceptor = compressSpanInterceptor;
    }

}