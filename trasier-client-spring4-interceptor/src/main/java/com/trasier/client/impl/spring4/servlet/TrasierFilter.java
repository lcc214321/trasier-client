package com.trasier.client.impl.spring4.servlet;

import com.google.gson.GsonBuilder;
import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring4.context.TrasierContextHolder;
import com.trasier.client.model.Endpoint;
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
import java.util.UUID;

@Component
@Order(TraceFilter.ORDER)
public class TrasierFilter extends AbstractTrasierFilter {

    protected static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 5;
    private final Client client;
    private final TrasierClientConfiguration configuration;

    public TrasierFilter(Client client, TrasierClientConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (isEnabled(servletRequest)) {
            CachedServletRequestWrapper request = CachedServletRequestWrapper.create((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = CachedServletResponseWrapper.create((HttpServletResponse) servletResponse);

            Span currentSpan = TrasierContextHolder.getCurrentSpan();
            if(currentSpan == null) {
                String conversationId = extractConversationId(request);
                String traceId = extractTraceId(request);
                if(traceId == null) {
                    traceId = UUID.randomUUID().toString();
                }
                String spanId = extractSpanId(request);
                if(spanId == null) {
                    spanId = UUID.randomUUID().toString();
                }


                Span.Builder spanBuilder = Span.newSpan(conversationId, traceId, new Endpoint("in"), getOperationName(request));
                spanBuilder.parentId(spanId);
                currentSpan = spanBuilder.build();
            }

            Map<String, String> requestHeaders = getRequestHeaders(request);
            Map<String, List<String>> parameters = getRequestParameters(request);
            String requestMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(requestHeaders, parameters));
            currentSpan.setIncomingData(requestMessage);

            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                //handle exception to log and rethrow
            }

            Map<String, Integer> statusMap = Collections.singletonMap("status", response.getStatus());
            Map<String, String> responseHeaders = getResponseHeaders(response);
            String responseBody = response.getCachedData();
            String responseMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(statusMap, responseHeaders, responseBody));
            currentSpan.setOutgoingData(responseMessage);
            //TODO entkoppeln
            client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), currentSpan);
            TrasierContextHolder.removeCurrentSpan();
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}