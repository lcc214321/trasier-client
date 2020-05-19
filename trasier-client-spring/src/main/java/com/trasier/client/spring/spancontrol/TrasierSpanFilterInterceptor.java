package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class TrasierSpanFilterInterceptor implements TrasierSpanResolverInterceptor {

    private static final Pattern DEFAULT_SKIP_PATTERN = Pattern.compile(
            "/api-docs.*|/autoconfig|/configprops|/dump|/health|/info|/metrics.*|" +
                    ".*/healthCheckServlet|.*/checkServlet|/admin/check|/actuatorhealth|" +
                    "/hystrix.stream|/mappings|/swagger.*|" +
                    ".*\\.wsdl|.*\\.xsd|.*\\.png|.*\\.css|.*\\.js|.*\\.html|/favicon.ico");

    @Autowired(required = false)
    private TrasierSpanFilterConfiguration filterConfiguration;

    public TrasierSpanFilterInterceptor() {
        this.filterConfiguration = new TrasierSpanFilterConfiguration();
    }

    public TrasierSpanFilterInterceptor(TrasierSpanFilterConfiguration configuration) {
        this.filterConfiguration = configuration;
    }

    @Override
    public void interceptRequestUrlResolved(Span span, String url) {
        if (!span.isCancel()) {
            interceptByUrl(span, url);
        }
    }

    @Override
    public void interceptMetdataResolved(Span span) {
        if (!span.isCancel()) {
            interceptByOperationName(span);
        }
    }

    private void interceptByUrl(Span span, String url) {
        if (DEFAULT_SKIP_PATTERN.matcher(url).matches()) {
            span.setCancel(true);
            return;
        }
        if (filterConfiguration != null && filterConfiguration.getFilters() != null) {
            for (SpanFilter spanFilters : filterConfiguration.getFilters()) {
                Strategy strategy = spanFilters.getStrategy();
                if (strategy == Strategy.disablePayload) {
                    Pattern disablePattern = spanFilters.getUrl();
                    if (!span.isCancel() && disablePattern != null && disablePattern.matcher(url).matches()) {
                        span.setPayloadDisabled(true);
                    }
                }
                if (strategy == Strategy.allow) {
                    Pattern allowPattern = spanFilters.getUrl();
                    if (!span.isCancel() && allowPattern != null && !allowPattern.matcher(url).matches()) {
                        span.setCancel(true);
                    }
                }
                if (strategy == Strategy.cancel) {
                    Pattern skipPattern = spanFilters.getUrl();
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
            for (SpanFilter spanFilters : filterConfiguration.getFilters()) {
                Strategy strategy = spanFilters.getStrategy();
                if (strategy == Strategy.disablePayload) {
                    Pattern disablePattern = spanFilters.getOperation();
                    if (!span.isCancel() && disablePattern != null && disablePattern.matcher(operationName).matches()) {
                        span.setPayloadDisabled(true);
                    }
                }
                if (strategy == Strategy.allow) {
                    Pattern allowPattern = spanFilters.getOperation();
                    if (!span.isCancel() && allowPattern != null && !allowPattern.matcher(operationName).matches()) {
                        span.setCancel(true);
                    }
                }
                if (strategy == Strategy.cancel) {
                    Pattern skipPattern = spanFilters.getOperation();
                    if (!span.isCancel() && skipPattern != null && skipPattern.matcher(operationName).matches()) {
                        span.setCancel(true);
                    }
                }
            }
        }
    }

}
