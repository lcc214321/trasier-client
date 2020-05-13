package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class TrasierSampleByUrlPatternInterceptor implements TrasierSpanResolverInterceptor {

    @Autowired(required = false)
    private TrasierSampleByUrlPatternConfiguration patternConfiguration;

    public TrasierSampleByUrlPatternInterceptor() {
        this.patternConfiguration = new TrasierSampleByUrlPatternConfiguration();
    }

    public TrasierSampleByUrlPatternInterceptor(TrasierSampleByUrlPatternConfiguration configuration) {
        this.patternConfiguration = configuration;
    }

    @Override
    public void interceptRequestUrlResolved(Span span, String url) {
        if (!span.isCancel()) {
            if (!shouldSample(url)) {
                span.setCancel(true);
            }
        }
    }

    private boolean shouldSample(String urlPart) {
        if (patternConfiguration == null) {
            return true;
        }
        Pattern skipPattern = patternConfiguration.getSkipPattern();
        Pattern defaultSkipPattern = patternConfiguration.getDefaultSkipPattern();
        if (skipPattern != null && skipPattern.matcher(urlPart).matches()) {
            return false;
        }
        return !defaultSkipPattern.matcher(urlPart).matches();
    }

    @Override
    public void interceptMetdataResolved(Span span) {
    }

    @Override
    public void interceptMessagePayloadResolved(Span span) {
    }

}
