package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.interceptor.TrasierSamplingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TrasierSampleByOperationInterceptor implements TrasierSamplingInterceptor {

    @Autowired(required = false)
    private TrasierSampleByOperationConfiguration configuration;

    public TrasierSampleByOperationInterceptor() {
        this.configuration = new TrasierSampleByOperationConfiguration();
    }

    public TrasierSampleByOperationInterceptor(TrasierSampleByOperationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean shouldSample(Span span, Map<String, Object> params) {
        if (span.isCancel()) {
            return false;
        }

        if (!isOnWhitelist(span)) {
            return false;
        }
        if (isOnBlacklist(span)){
            return false;
        }

        return true;
    }

    private boolean isOnBlacklist(Span span) {
        List<String> list = configuration.getBlacklist();
        if (!list.isEmpty() && list.contains(span.getName())) {
            return true;
        }
        return false;
    }

    private boolean isOnWhitelist(Span span) {
        List<String> list = configuration.getWhitelist();
        if (!list.isEmpty() && !list.contains(span.getName())) {
            return false;
        }
        return true;
    }

}
