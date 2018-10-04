package com.trasier.opentracing.spring.interceptor.rest;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Span;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Tracer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class TrasierClientRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;

    public TrasierClientRequestInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] data, ClientHttpRequestExecution execution) throws IOException {
        TrasierSpan span = (TrasierSpan) tracer.activeSpan();

        if (span != null) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setIncomingHeader(request.getHeaders().toSingleValueMap());
            trasierSpan.setIncomingContentType(ContentType.JSON);
            trasierSpan.setIncomingData(new String(data));
            trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
        }

        ClientHttpResponse response = execution.execute(request, data);

        if (span != null) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            if(response != null) {
                String responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
                trasierSpan.setOutgoingHeader(response.getHeaders().toSingleValueMap());
                trasierSpan.setOutgoingContentType(ContentType.JSON);
                trasierSpan.setOutgoingData(responseBody);
            }
        }

        return response;
    }
}