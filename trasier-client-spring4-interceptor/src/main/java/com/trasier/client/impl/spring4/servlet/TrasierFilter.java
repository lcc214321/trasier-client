package com.trasier.client.impl.spring4.servlet;

import com.google.gson.GsonBuilder;
import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring4.context.TrasierSpringAccessor;
import com.trasier.client.model.Span;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Order(TrasierFilter.ORDER)
public class TrasierFilter extends AbstractTrasierFilter {
    static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 6;

    private final Client client;
    private final TrasierClientConfiguration configuration;
    private final TrasierSpringAccessor trasierSpringAccessor;

    public TrasierFilter(Client client, TrasierClientConfiguration configuration, TrasierSpringAccessor trasierSpringAccessor) {
        this.client = client;
        this.configuration = configuration;
        this.trasierSpringAccessor = trasierSpringAccessor;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (isEnabled(servletRequest)) {
            CachedServletRequestWrapper request = CachedServletRequestWrapper.create((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = CachedServletResponseWrapper.create((HttpServletResponse) servletResponse);

            String conversationId = extractConversationId(request);
            String traceId = extractTraceId(request);
            String spanId = extractSpanId(request);

            Span currentSpan = trasierSpringAccessor.createSpan(getOperationName(request), conversationId, traceId, spanId);

            handleRequest(request, currentSpan);

            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                //handle exception to log and rethrow
            }

            handleResponse(response, currentSpan);

            //TODO entkoppeln
            client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), currentSpan);
            trasierSpringAccessor.closeSpan(currentSpan);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void handleResponse(CachedServletResponseWrapper response, Span currentSpan) {
        Map<String, Integer> statusMap = Collections.singletonMap("status", response.getStatus());
        Map<String, String> responseHeaders = getResponseHeaders(response);
        String responseBody = response.getCachedData();
        String responseMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(statusMap, responseHeaders, responseBody));
        currentSpan.setOutgoingData(responseMessage);
    }

    private void handleRequest(CachedServletRequestWrapper request, Span currentSpan) {
        Map<String, String> requestHeaders = getRequestHeaders(request);
        Map<String, List<String>> parameters = getRequestParameters(request);
        String requestMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(requestHeaders, parameters));
        currentSpan.setIncomingData(requestMessage);
    }
}