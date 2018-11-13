package com.trasier.opentracing.spring.interceptor.feign;

import com.trasier.client.api.ContentType;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.opentracing.spring.interceptor.rest.TrasierClientRequestInterceptor;
import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrasierFeignSpanDecorator implements FeignSpanDecorator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierClientRequestInterceptor.class);

    private final TrasierClientConfiguration configuration;
    private final Tracer tracer;

    public TrasierFeignSpanDecorator(TrasierClientConfiguration configuration, Tracer tracer) {
        this.configuration = configuration;
        this.tracer = tracer;
    }

    @Override
    public void onRequest(feign.Request request, feign.Request.Options options, Span span) {
        if (span instanceof TrasierSpan) {
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setIncomingContentType(ContentType.JSON);
            trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
            try {
                trasierSpan.setIncomingHeader(toSingleValueMap(request.headers()));
                trasierSpan.setIncomingData(new String(request.body()));
            } catch (Exception e) {
                LOGGER.error("Error while logging request", e);
            }
        }
    }

    @Override
    public void onResponse(feign.Response response, feign.Request.Options options, Span span) {
        if (span instanceof TrasierSpan) {
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            if (response != null) {
                trasierSpan.setOutgoingContentType(ContentType.JSON);
                try {
                    trasierSpan.setOutgoingHeader(toSingleValueMap(response.headers()));
                    String responseBody = StreamUtils.copyToString(response.body().asInputStream(), Charset.defaultCharset());
                    trasierSpan.setOutgoingData(responseBody);
                } catch (Exception e) {
                    LOGGER.error("Error while logging response", e);
                }
            }
        }
    }

    @Override
    public void onError(Exception e, feign.Request request, Span span) {
        if (span instanceof TrasierSpan) {
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.setStatus("ERROR");
        }
    }

    private Map<String, String> toSingleValueMap(Map<String, Collection<String>> headers) {
        LinkedHashMap<String, String> singleValueMap = new LinkedHashMap<>(headers.size());
        headers.forEach((key, valueList) -> singleValueMap.put(key, valueList.iterator().next()));
        return singleValueMap;
    }

}