package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrasierSampleByOperationInterceptor implements TrasierSpanResolverInterceptor {

    @Autowired(required = false)
    private TrasierSampleByOperationConfiguration configuration;

    public TrasierSampleByOperationInterceptor() {
        this.configuration = new TrasierSampleByOperationConfiguration();
    }

    public TrasierSampleByOperationInterceptor(TrasierSampleByOperationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void interceptMetdataResolved(Span span) {
        if (!span.isCancel()) {
            if (shouldCancel(span.getName())) {
                span.setCancel(true);
            }
        }
    }

    private boolean shouldCancel(String operationName) {

        if ("OPTIONS".equalsIgnoreCase(operationName)) {
            return true;
        }

        if (!isOnWhitelist(operationName)) {
            return true;
        }

        if (isOnBlacklist(operationName)){
            return true;
        }

        return false;
    }

    private boolean isOnBlacklist(String operationName) {
        List<String> list = configuration.getBlacklist();
        if (!list.isEmpty() && list.contains(operationName)) {
            return true;
        }
        return false;
    }

    private boolean isOnWhitelist(String operationName) {
        List<String> list = configuration.getWhitelist();
        if (!list.isEmpty() && !list.contains(operationName)) {
            return false;
        }
        return true;
    }

    @Override
    public void interceptRequestUrlResolved(Span span, String url) {
    }

    @Override
    public void interceptMessagePayloadResolved(Span span) {
    }

}
