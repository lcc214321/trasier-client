package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Span;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

public class TrasierServletFilterSpanDecorator implements ServletFilterSpanDecorator {
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";

    @Autowired
    private volatile TrasierClientConfiguration configuration;

    @Override
    public void onRequest(HttpServletRequest httpServletRequest, Span span) {
        if(configuration.isActivated() && httpServletRequest instanceof CachedServletResponseWrapper) {
            TrasierSpan activeSpan = (TrasierSpan) span;
            com.trasier.client.api.Span trasierSpan = activeSpan.unwrap();
            String conversationId = trasierSpan.getConversationId();
            MDC.put(TrasierConstants.HEADER_CONVERSATION_ID, conversationId);

            enhanceIncomingEndpoint(trasierSpan.getIncomingEndpoint(), httpServletRequest);
            enhanceOutgoingEndpoint(trasierSpan.getOutgoingEndpoint(), httpServletRequest);
            handleRequest((CachedServletRequestWrapper) httpServletRequest, trasierSpan);
        }
    }

    @Override
    public void onResponse(HttpServletRequest httpServletRequest, HttpServletResponse response, Span span) {
        if(configuration.isActivated() && response instanceof CachedServletResponseWrapper) {
            handleResponse((CachedServletResponseWrapper) response, ((TrasierSpan) span).unwrap());
        }
    }

    @Override
    public void onError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Throwable exception, Span span) {
        //TODO
    }

    @Override
    public void onTimeout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long timeout, Span span) {
        //TODO
    }

    private void enhanceIncomingEndpoint(Endpoint incomingEndpoint, ServletRequest request) {
        incomingEndpoint.setName(extractIncomingEndpointName(request));
        incomingEndpoint.setHostname(request.getRemoteHost());
        incomingEndpoint.setIpAddress(request.getRemoteAddr());
        incomingEndpoint.setPort("" + request.getRemotePort());
    }

    private void enhanceOutgoingEndpoint(Endpoint outgoingEndpoint, ServletRequest request) {
        outgoingEndpoint.setName(configuration.getSystemName());
        outgoingEndpoint.setHostname(request.getLocalName());
        outgoingEndpoint.setIpAddress(request.getLocalAddr());
        outgoingEndpoint.setPort("" + request.getLocalPort());
    }

    private void handleRequest(CachedServletRequestWrapper request, com.trasier.client.api.Span currentSpan) {
        //TODO handle headers und parameters
        Map<String, String> requestHeaders = getRequestHeaders(request);
        currentSpan.setIncomingHeader(requestHeaders);
        String requestBody = new String(request.getContentAsByteArray());
        currentSpan.setIncomingData(requestBody);
        currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
        if (requestBody.startsWith("<")) {
            currentSpan.setIncomingContentType(ContentType.XML);
        } else if (requestBody.startsWith("{") || requestBody.startsWith("[")) {
            currentSpan.setIncomingContentType(ContentType.JSON);
        } else if (!requestBody.isEmpty()) {
            currentSpan.setIncomingContentType(ContentType.TEXT);
        } else {
            currentSpan.setIncomingContentType(null);
        }
    }

    protected String extractIncomingEndpointName(ServletRequest servletRequest) {
        String incomingEndpointName = ((HttpServletRequest) servletRequest).getHeader(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME);
        return StringUtils.isEmpty(incomingEndpointName) ? TrasierConstants.UNKNOWN_IN : incomingEndpointName;
    }

    protected Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new TreeMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerKey = headerNames.nextElement();
            if (!HEADER_KEY_AUTHORIZATION.equalsIgnoreCase(headerKey)) {
                String headerValue = request.getHeader(headerKey);
                headerMap.put(headerKey, headerValue);
            }
        }
        return headerMap;
    }

    private void handleResponse(CachedServletResponseWrapper response, com.trasier.client.api.Span currentSpan) {
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