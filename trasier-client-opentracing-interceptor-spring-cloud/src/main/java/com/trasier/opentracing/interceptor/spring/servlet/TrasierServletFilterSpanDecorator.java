package com.trasier.opentracing.interceptor.spring.servlet;

import com.trasier.client.impl.spring.opentracing.api.TrasierSpan;
import com.trasier.client.model.ContentType;
import io.opentracing.Span;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class TrasierServletFilterSpanDecorator implements ServletFilterSpanDecorator {
    @Override
    public void onRequest(HttpServletRequest httpServletRequest, Span span) {

    }

    @Override
    public void onResponse(HttpServletRequest httpServletRequest, HttpServletResponse response, Span span) {
        handleResponse((CachedServletResponseWrapper) response, ((TrasierSpan) span).unwrap());
    }

    @Override
    public void onError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Throwable exception, Span span) {

    }

    @Override
    public void onTimeout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long timeout, Span span) {

    }

    private void handleResponse(CachedServletResponseWrapper response, com.trasier.client.model.Span currentSpan) {
        //TODO use Clock everywhere
        currentSpan.setFinishProcessingTimestamp(System.currentTimeMillis());

        //TODO handle headers und status
        Map<String, String> responseHeaders = getResponseHeaders(response);
        currentSpan.setOutgoingHeader(responseHeaders);
        String responseBody = new String(response.getContentAsByteArray());
        currentSpan.setOutgoingData(responseBody);
        if(responseBody.startsWith("<")) {
            currentSpan.setOutgoingContentType(ContentType.XML);
        } else if(responseBody.startsWith("{") || responseBody.startsWith("[")) {
            currentSpan.setOutgoingContentType(ContentType.JSON);
        } else if (!responseBody.isEmpty()) {
            currentSpan.setOutgoingContentType(ContentType.TEXT);
        } else {
            currentSpan.setOutgoingContentType(null);
        }
    }

    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headerMap = new TreeMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }
        return headerMap;
    }

}