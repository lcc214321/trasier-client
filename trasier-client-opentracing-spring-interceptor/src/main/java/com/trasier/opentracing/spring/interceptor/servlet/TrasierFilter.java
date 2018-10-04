package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.client.opentracing.TrasierTracer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

public class TrasierFilter extends GenericFilterBean {
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";
    private static final String SERVER_SPAN_CONTEXT = TrasierFilter.class.getName() + ".activeSpanContext";

    @Autowired
    private volatile TrasierClientConfiguration configuration;
    @Autowired
    private volatile TrasierTracer tracer;

    public TrasierFilter() {
    }

    @Autowired
    public TrasierFilter(TrasierClientConfiguration configuration, TrasierTracer tracer) {
        this.configuration = configuration;
        this.tracer = tracer;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (needsInitialization()) {
            initialize();
        }

        if (isCurrentRequestTraceable(servletRequest)) {
            TrasierSpan activeSpan = (TrasierSpan) tracer.activeSpan();
            Span trasierSpan = activeSpan.unwrap();
            String conversationId = trasierSpan.getConversationId();
            MDC.put(TrasierConstants.HEADER_CONVERSATION_ID, conversationId);

            enhanceIncomingEndpoint(trasierSpan.getIncomingEndpoint(), servletRequest);
            enhanceOutgoingEndpoint(trasierSpan.getOutgoingEndpoint(), servletRequest);
            handleRequest((CachedServletRequestWrapper) servletRequest, trasierSpan);

            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isCurrentRequestTraceable(ServletRequest servletRequest) {
        return configuration.isActivated() && servletRequest.getAttribute(SERVER_SPAN_CONTEXT) == null
                && tracer.activeSpan() != null;
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

    private void handleRequest(CachedServletRequestWrapper request, Span currentSpan) {
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

    // TODO optimize this
    private synchronized void initialize() {
        if (needsInitialization()) {
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
            tracer = webApplicationContext.getBean(TrasierTracer.class);
        }
    }

    private boolean needsInitialization() {
        return tracer == null;
    }

}