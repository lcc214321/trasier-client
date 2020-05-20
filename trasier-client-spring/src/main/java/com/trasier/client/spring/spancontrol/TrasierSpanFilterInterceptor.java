package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierFilterConfiguration;
import com.trasier.client.configuration.TrasierFilterConfiguration.Filter;
import com.trasier.client.configuration.TrasierFilterConfiguration.Strategy;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class TrasierSpanFilterInterceptor implements TrasierSpanResolverInterceptor {

    @Autowired(required = false)
    private TrasierFilterConfiguration filterConfiguration;

    public TrasierSpanFilterInterceptor() {
        this.filterConfiguration = new TrasierFilterConfiguration();
    }

    public TrasierSpanFilterInterceptor(TrasierFilterConfiguration configuration) {
        this.filterConfiguration = configuration;
    }

    @Override
    public void interceptUrlResolved(Span span, String url) {
        if (!span.isCancel()) {
            interceptByUrl(span, url);
        }
    }

    @Override
    public void interceptMetadataResolved(Span span) {
        if (!span.isCancel()) {
            interceptByOperationName(span);
        }
    }

    private void interceptByUrl(Span span, String url) {
        if (TrasierConstants.DEFAULT_SKIP_PATTERN.matcher(url).matches()) {
            span.setCancel(true);
            return;
        }
        if (filterConfiguration != null && filterConfiguration.getFilters() != null) {
            for (Filter filters : filterConfiguration.getFilters()) {
                Strategy strategy = filters.getStrategy();
                if (strategy == Strategy.disablePayload) {
                    Pattern disablePattern = filters.getUrl();
                    if (!span.isCancel() && disablePattern != null && disablePattern.matcher(url).matches()) {
                        span.setPayloadDisabled(true);
                    }
                }
                if (strategy == Strategy.allow) {
                    Pattern allowPattern = filters.getUrl();
                    if (!span.isCancel() && allowPattern != null && !allowPattern.matcher(url).matches()) {
                        span.setCancel(true);
                    }
                }
                if (strategy == Strategy.cancel) {
                    Pattern skipPattern = filters.getUrl();
                    if (!span.isCancel() && skipPattern != null && skipPattern.matcher(url).matches()) {
                        span.setCancel(true);
                    }
                }
            }
        }
    }

    private void interceptByOperationName(Span span) {
        String operationName = span.getName();
        if ("OPTIONS".equalsIgnoreCase(operationName)) {
            span.setCancel(true);
            return;
        }
        if (filterConfiguration != null && filterConfiguration.getFilters() != null) {
            for (Filter filters : filterConfiguration.getFilters()) {
                Strategy strategy = filters.getStrategy();
                if (strategy == Strategy.disablePayload) {
                    Pattern disablePattern = filters.getOperation();
                    if (!span.isCancel() && disablePattern != null && disablePattern.matcher(operationName).matches()) {
                        span.setPayloadDisabled(true);
                    }
                }
                if (strategy == Strategy.allow) {
                    Pattern allowPattern = filters.getOperation();
                    if (!span.isCancel() && allowPattern != null && !allowPattern.matcher(operationName).matches()) {
                        span.setCancel(true);
                    }
                }
                if (strategy == Strategy.cancel) {
                    Pattern skipPattern = filters.getOperation();
                    if (!span.isCancel() && skipPattern != null && skipPattern.matcher(operationName).matches()) {
                        span.setCancel(true);
                    }
                }
            }
        }
    }

}
