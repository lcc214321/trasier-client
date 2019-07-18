package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.interceptor.TrasierSamplingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TrasierSampleByUrlPatternInterceptor implements TrasierSamplingInterceptor {

    @Autowired(required = false)
    private TrasierSampleByUrlPatternConfiguration patternConfiguration;

    public TrasierSampleByUrlPatternInterceptor() {
        this.patternConfiguration = new TrasierSampleByUrlPatternConfiguration();
    }

    public TrasierSampleByUrlPatternInterceptor(TrasierSampleByUrlPatternConfiguration configuration) {
        this.patternConfiguration = configuration;
    }

    @Override
    public boolean shouldSample(Span span, Map<String, Object> params) {
        if (span.isCancel()) {
            return false;
        }
        Object url = params.get("url");
        if (url instanceof String) {
            return shouldSample((String)url);
        }
        return true;
    }

    private boolean shouldSample(String urlPart) {
        Pattern skipPattern = patternConfiguration.getSkipPattern();
        Pattern defaultSkipPattern = patternConfiguration.getDefaultSkipPattern();
        if (skipPattern != null && skipPattern.matcher(urlPart).matches()) {
            return false;
        }
        return !defaultSkipPattern.matcher(urlPart).matches();
    }
}
