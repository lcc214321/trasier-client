package com.trasier.opentracing.spring.interceptor.rest;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Span;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class TrasierClientRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierClientRequestInterceptor.class);

    private final Tracer tracer;

    public TrasierClientRequestInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] data, ClientHttpRequestExecution execution) throws IOException {
        TrasierSpan span = (TrasierSpan) tracer.activeSpan();

        if (span != null) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setIncomingContentType(ContentType.JSON);
            trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
            try {
                trasierSpan.setIncomingHeader(request.getHeaders().toSingleValueMap());
                trasierSpan.setIncomingData(new String(data));
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
                try {
                    trasierSpan.setOutgoingHeader(response.getHeaders().toSingleValueMap());
                    String responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
                    trasierSpan.setOutgoingData(responseBody);
                } catch (Exception e) {
                    LOGGER.error("Error while logging response", e);
                }
            }
        }

        return response;
    }
}