package com.trasier.client.impl.spring4.interceptor.servlet;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring4.interceptor.context.TrasierSpringAccessor;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
@Order(TrasierFilter.ORDER)
public class TrasierFilter extends AbstractTrasierFilter {

    static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 6;

    @Autowired
    private volatile Client client;
    @Autowired
    private volatile TrasierClientConfiguration configuration;
    @Autowired
    private volatile TrasierSpringAccessor trasierSpringAccessor;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (needsInitialization()) {
            initialize();
        }

        if (configuration.isDeactivated()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (isEnabled(servletRequest)) {
            CachedServletRequestWrapper request = CachedServletRequestWrapper.create((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = CachedServletResponseWrapper.create((HttpServletResponse) servletResponse);

            String conversationId = extractConversationId(request);
            String traceId = extractTraceId(request);
            String spanId = extractSpanId(request);

            Span currentSpan = trasierSpringAccessor.createSpan(extractOperationName(request), conversationId, traceId, spanId);
            currentSpan.setIncomingContentType(extractContentType(request));
            currentSpan.setIncomingEndpoint(new Endpoint(extractIncomingEndpointName(request)));
            currentSpan.setOutgoingContentType(extractContentType(request));
            currentSpan.setOutgoingEndpoint(new Endpoint(configuration.getSystemName()));

            handleRequest(request, currentSpan);

            try {
                filterChain.doFilter(request, response);
            } finally {
                handleResponse(response, currentSpan);
                client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), currentSpan);
                trasierSpringAccessor.closeSpan(currentSpan);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void handleResponse(CachedServletResponseWrapper response, Span currentSpan) {
        currentSpan.setFinishProcessingTimestamp(System.currentTimeMillis());

        //TODO handle headers und status
//        Map<String, Integer> statusMap = Collections.singletonMap("status", response.getStatus());
//        Map<String, String> responseHeaders = getResponseHeaders(response);
//        String responseMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(statusMap, responseHeaders, responseBody));

        String responseBody = new String(response.getContentAsByteArray());
        currentSpan.setOutgoingData(responseBody);
    }

    private void handleRequest(CachedServletRequestWrapper request, Span currentSpan) {
        //TODO handle headers und parameters
//        Map<String, String> requestHeaders = getRequestHeaders(request);
//        Map<String, List<String>> parameters = getRequestParameters(request);
//        String requestMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(requestHeaders, parameters));

        String requestBody = new String(request.getContentAsByteArray());
        currentSpan.setIncomingData(requestBody);
        currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
    }

    private synchronized void initialize() {
        if (needsInitialization()) { // TODO optimize this
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            client = webApplicationContext.getBean(Client.class);
            configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
            trasierSpringAccessor = webApplicationContext.getBean(TrasierSpringAccessor.class);
        }
    }

    private boolean needsInitialization() {
        return client == null;
    }
}