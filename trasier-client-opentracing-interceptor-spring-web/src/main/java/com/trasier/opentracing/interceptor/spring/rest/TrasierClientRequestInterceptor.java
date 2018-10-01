package com.trasier.opentracing.interceptor.spring.rest;

import com.trasier.client.impl.spring.opentracing.api.TrasierSpan;
import com.trasier.client.model.ContentType;
import com.trasier.client.model.Span;
import io.opentracing.Tracer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

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
            trasierSpan.setIncomingContentType(ContentType.JSON);
            trasierSpan.setIncomingData(new String(data));
        }

        ClientHttpResponse response = execution.execute(request, data);

        if (span != null) {
            Span trasierSpan = span.unwrap();
            if(response != null) {
                //TODO
            }
        }

        return response;
    }
}