package com.trasier.opentracing.interceptor.spring.servlet;

import com.trasier.client.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring.opentracing.api.TrasierSpan;
import com.trasier.client.impl.spring.opentracing.api.TrasierTracer;
import com.trasier.client.model.ContentType;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class TrasierFilter extends AbstractTrasierFilter {
    @Autowired
    private volatile TrasierClientConfiguration configuration;
    @Autowired
    private volatile TrasierTracer tracer;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (needsInitialization()) {
            initialize();
        }

        if (configuration.isDeactivated()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        TrasierSpan activeSpan = (TrasierSpan) tracer.activeSpan();

        if (activeSpan != null) {
            Span trasierSpan = activeSpan.unwrap();

            String conversationId = trasierSpan.getConversationId();
            MDC.put(TrasierConstants.HEADER_CONVERSATION_ID, conversationId);

            CachedServletRequestWrapper request = CachedServletRequestWrapper.create((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = CachedServletResponseWrapper.create((HttpServletResponse) servletResponse);

            enhanceIncomingEndpoint(trasierSpan.getIncomingEndpoint(), request);
            enhanceOutgoingEndpoint(trasierSpan.getOutgoingEndpoint(), request);

            handleRequest(request, trasierSpan);

            try {
                filterChain.doFilter(request, response);
            } finally {
                handleResponse(response, trasierSpan);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void enhanceIncomingEndpoint(Endpoint incomingEndpoint, CachedServletRequestWrapper request) {
        incomingEndpoint.setName(extractIncomingEndpointName(request));
        incomingEndpoint.setHostname(request.getRemoteHost());
        incomingEndpoint.setIpAddress(request.getRemoteAddr());
        incomingEndpoint.setPort("" + request.getRemotePort());
    }

    private void enhanceOutgoingEndpoint(Endpoint outgoingEndpoint, CachedServletRequestWrapper request) {
        outgoingEndpoint.setName(configuration.getSystemName());
        outgoingEndpoint.setHostname(request.getLocalName());
        outgoingEndpoint.setIpAddress(request.getLocalAddr());
        outgoingEndpoint.setPort("" + request.getLocalPort());
    }

    private void handleResponse(CachedServletResponseWrapper response, Span currentSpan) {
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

    private void handleRequest(CachedServletRequestWrapper request, Span currentSpan) {
        //TODO handle headers und parameters
        Map<String, String> requestHeaders = getRequestHeaders(request);
        currentSpan.setIncomingHeader(requestHeaders);
        String requestBody = new String(request.getContentAsByteArray());
        currentSpan.setIncomingData(requestBody);
        currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
        if(requestBody.startsWith("<")) {
           currentSpan.setIncomingContentType(ContentType.XML);
        } else if(requestBody.startsWith("{") || requestBody.startsWith("[")) {
            currentSpan.setIncomingContentType(ContentType.JSON);
        } else if (!requestBody.isEmpty()) {
            currentSpan.setIncomingContentType(ContentType.TEXT);
        } else {
            currentSpan.setIncomingContentType(null);
        }
    }

    private synchronized void initialize() {
        if (needsInitialization()) { // TODO optimize this
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
            tracer = webApplicationContext.getBean(TrasierTracer.class);
        }
    }

    private boolean needsInitialization() {
        return tracer == null;
    }

}