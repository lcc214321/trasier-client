package com.trasier.opentracing.spring.interceptor.rest;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.TrasierSamplingInterceptor;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrasierClientRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierClientRequestInterceptor.class);

    private final Tracer tracer;
    private final List<TrasierSamplingInterceptor> samplingInterceptors;
    private final TrasierClientConfiguration configuration;

    public TrasierClientRequestInterceptor(Tracer tracer, TrasierClientConfiguration configuration, List<TrasierSamplingInterceptor> samplingInterceptors) {
        this.tracer = tracer;
        this.configuration = configuration;
        this.samplingInterceptors = samplingInterceptors;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] data, ClientHttpRequestExecution execution) throws IOException {
        TrasierSpan span = (TrasierSpan) tracer.activeSpan();

        if (span != null) {
            Span trasierSpan = span.unwrap();
            if (!shouldTrace(request, span)) {
                trasierSpan.setCancel(true);
            }

            trasierSpan.setName(extractOperationName(request.getURI(), trasierSpan.getName()));
            trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.setIncomingContentType(ContentType.JSON);
            try {
                trasierSpan.getIncomingHeader().putAll(request.getHeaders().toSingleValueMap());
                if (!configuration.isPayloadTracingDisabled()) {
                    trasierSpan.setIncomingData(new String(data));
                }
            } catch (Exception e) {
                LOGGER.error("Error while logging request", e);
            }
        }

        ClientHttpResponse response = execution.execute(request, data);

        if (span != null) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            if (response != null) {
                trasierSpan.setOutgoingContentType(ContentType.JSON);
                trasierSpan.getOutgoingHeader().putAll(response.getHeaders().toSingleValueMap());
                if (!configuration.isPayloadTracingDisabled()) {
                    try {
                        InputStream body = null;
                        try {
                            body = response.getBody(); // throws exception on empty input stream
                        } catch (Exception e) {
                            LOGGER.debug(e.getMessage(), e);
                        }
                        if (body instanceof ByteArrayInputStream) {
                            String responseBody = StreamUtils.copyToString(body, Charset.defaultCharset());
                            trasierSpan.setOutgoingData(responseBody);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while logging response", e);
                    }
                }
            }
        }

        return response;
    }

    private String extractOperationName(URI requestUri, String defaultName) {
        String path = requestUri.getPath();
        if (!path.isEmpty() && !path.equals("/")) {
            return path;
        }
        return defaultName;
    }

    private boolean shouldTrace(HttpRequest request, TrasierSpan span) {
        if (CollectionUtils.isEmpty(samplingInterceptors)) {
            return true;
        }
        Map<String, Object> params = new HashMap<>();
        URI uri = request.getURI();
        params.put("url", uri.getPath());
        for (TrasierSamplingInterceptor samplingInterceptor : samplingInterceptors) {
            if (!samplingInterceptor.shouldSample(span.unwrap(), params)) {
                return false;
            }
        }
        return true;
    }
}