package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.client.opentracing.TrasierTracer;
import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
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
    private static final String SERVER_SPAN_CONTEXT = TrasierFilter.class.getName() + ".activeSpanContext";

    @Autowired
    private volatile TrasierClientConfiguration configuration;
    @Autowired
    private volatile TrasierTracer tracer;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (needsInitialization()) {
            initialize();
        }

        if (!configuration.isActivated() || servletRequest.getAttribute(SERVER_SPAN_CONTEXT) != null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final TrasierSpan activeSpan = (TrasierSpan) tracer.activeSpan();

        if (activeSpan != null) {
            servletRequest.setAttribute(SERVER_SPAN_CONTEXT, activeSpan.context());

            Span trasierSpan = activeSpan.unwrap();
            String conversationId = trasierSpan.getConversationId();
            MDC.put(TrasierConstants.HEADER_CONVERSATION_ID, conversationId);

            CachedServletRequestWrapper request = createCachedRequest((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = createCachedResponse((HttpServletResponse) servletResponse);

            enhanceIncomingEndpoint(trasierSpan.getIncomingEndpoint(), request);
            enhanceOutgoingEndpoint(trasierSpan.getOutgoingEndpoint(), request);
            handleRequest(request, trasierSpan);

            filterChain.doFilter(request, response);
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