package com.trasier.client.impl.spring.opentracing.interceptor;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring.opentracing.api.TrasierSpan;
import com.trasier.client.impl.spring.opentracing.api.TrasierTracer;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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

@Component
@Primary
@Order(TrasierFilter.ORDER)
public class TrasierFilter extends AbstractTrasierFilter {
    static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 6;

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
            CachedServletRequestWrapper request = CachedServletRequestWrapper.create((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = CachedServletResponseWrapper.create((HttpServletResponse) servletResponse);

            trasierSpan.setIncomingContentType(extractContentType(request));
            enhanceIncomingEndpoint(trasierSpan.getIncomingEndpoint(), request);
            trasierSpan.setOutgoingContentType(extractContentType(request));
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
    }

    private void handleRequest(CachedServletRequestWrapper request, Span currentSpan) {
        //TODO handle headers und parameters
        Map<String, String> requestHeaders = getRequestHeaders(request);
        currentSpan.setIncomingHeader(requestHeaders);
        String requestBody = new String(request.getContentAsByteArray());
        currentSpan.setIncomingData(requestBody);
        currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
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