package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.TrasierSamplingInterceptor;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.client.util.ContentTypeResolver;
import com.trasier.client.util.ExceptionUtils;
import io.opentracing.Span;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class TrasierServletFilterSpanDecorator implements ServletFilterSpanDecorator {
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";
    private static final List<String> USER_AGENTS = Arrays.asList("mozilla", "chrome", "opera", "explorer", "safari");

    private final TrasierClientConfiguration configuration;
    private final List<TrasierSamplingInterceptor> samplingInterceptors;

    public TrasierServletFilterSpanDecorator(TrasierClientConfiguration configuration, List<TrasierSamplingInterceptor> samplingInterceptors) {
        this.configuration = configuration;
        this.samplingInterceptors = samplingInterceptors;
    }

    @Override
    public void onRequest(HttpServletRequest httpServletRequest, Span span) {
        if (configuration.isActivated() && httpServletRequest instanceof CachedServletRequestWrapper) {
            TrasierSpan activeSpan = (TrasierSpan) span;
            com.trasier.client.api.Span trasierSpan = activeSpan.unwrap();
            String conversationId = trasierSpan.getConversationId();
            MDC.put(TrasierConstants.HEADER_CONVERSATION_ID, conversationId);
            handleRequest((CachedServletRequestWrapper) httpServletRequest, trasierSpan);
            applyInterceptors(httpServletRequest, trasierSpan);
        }
    }

    private void applyInterceptors(HttpServletRequest httpServletRequest, com.trasier.client.api.Span trasierSpan) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", httpServletRequest.getServletPath());
        for (TrasierSamplingInterceptor samplingInterceptor : samplingInterceptors) {
            boolean shouldSample = samplingInterceptor.shouldSample(trasierSpan, params);
            if (!shouldSample) {
                trasierSpan.setCancel(true);
            }
        }
    }

    @Override
    public void onResponse(HttpServletRequest httpServletRequest, HttpServletResponse response, Span span) {
        if (configuration.isActivated() && response instanceof CachedServletResponseWrapper) {
            MDC.remove(TrasierConstants.HEADER_CONVERSATION_ID);
            TrasierSpan activeSpan = (TrasierSpan) span;
            com.trasier.client.api.Span trasierSpan = activeSpan.unwrap();
            applyInterceptors(httpServletRequest, trasierSpan);
            handleResponse((CachedServletResponseWrapper) response, trasierSpan);
        }
    }

    @Override
    public void onError(HttpServletRequest httpServletRequest, HttpServletResponse response, Throwable exception, Span span) {
        if (configuration.isActivated() && response instanceof CachedServletResponseWrapper) {
            MDC.remove(TrasierConstants.HEADER_CONVERSATION_ID);
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setStatus(TrasierConstants.STATUS_ERROR);
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.setOutgoingHeader(getResponseHeaders(response));
            trasierSpan.setOutgoingData(ExceptionUtils.getString(exception));
            trasierSpan.setOutgoingContentType(ContentType.TEXT);
        }
    }

    @Override
    public void onTimeout(HttpServletRequest httpServletRequest, HttpServletResponse response, long timeout, Span span) {
        if (configuration.isActivated() && response instanceof CachedServletResponseWrapper) {
            MDC.remove(TrasierConstants.HEADER_CONVERSATION_ID);
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setStatus(TrasierConstants.STATUS_ERROR);
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.setOutgoingHeader(getResponseHeaders(response));
            trasierSpan.setOutgoingData("Execution timeout after " + timeout);
            trasierSpan.setOutgoingContentType(ContentType.TEXT);
        }
    }

    private void handleRequest(CachedServletRequestWrapper request, com.trasier.client.api.Span currentSpan) {
        //TODO handle headers und parameters
        Map<String, String> requestHeaders = getRequestHeaders(request);
        currentSpan.setIncomingHeader(requestHeaders);
        String requestBody = new String(request.getContentAsByteArray());
        currentSpan.setIncomingData(requestBody);
        currentSpan.setName(extractOperationName(request, requestHeaders.get("soapaction"), currentSpan.getName()));
        currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
        currentSpan.setIncomingContentType(ContentTypeResolver.resolveFromPayload(requestBody));
        enhanceIncomingEndpoint(currentSpan.getIncomingEndpoint(), request, requestHeaders);
        enhanceOutgoingEndpoint(currentSpan.getOutgoingEndpoint(), request);
    }

    private String extractOperationName(CachedServletRequestWrapper request, String soapaction, String currentSpanName) {
        // extract for soap calls
        if (!StringUtils.isEmpty(soapaction)) {
            String[] soapActionArray = soapaction.replaceAll("\"", "").split("/");
            return soapActionArray[soapActionArray.length - 1];
        }
        // extract for rest calls
        if (!StringUtils.isEmpty(request.getServletPath()) && request.getServletPath().length() > 1) {
            return request.getServletPath().replace("/", "");
        }
        return currentSpanName;
    }

    private void enhanceIncomingEndpoint(Endpoint incomingEndpoint, ServletRequest request, Map<String, String> requestHeaders) {
        incomingEndpoint.setName(extractIncomingEndpointName(requestHeaders, request));
    }

    private void enhanceOutgoingEndpoint(Endpoint outgoingEndpoint, ServletRequest request) {
        outgoingEndpoint.setName(configuration.getSystemName());
        outgoingEndpoint.setHostname(request.getLocalName());
        outgoingEndpoint.setIpAddress(request.getLocalAddr());
        outgoingEndpoint.setPort("" + request.getLocalPort());
    }

    protected String extractIncomingEndpointName(Map<String, String> requestHeaders, ServletRequest servletRequest) {
        String incomingEndpointName = ((HttpServletRequest) servletRequest).getHeader(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME);
        if (StringUtils.isEmpty(incomingEndpointName)) {
            String userAgent = requestHeaders.get("user-agent");
            if (!StringUtils.isEmpty(userAgent)) {
                for (String agent : USER_AGENTS) {
                    if (userAgent.toLowerCase().contains(agent)) {
                        return "Web Browser";
                    }
                }
            }
        }
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
        currentSpan.setOutgoingHeader(getResponseHeaders(response));
        String responseBody = new String(response.getContentAsByteArray());
        currentSpan.setOutgoingData(responseBody);
        currentSpan.setOutgoingContentType(ContentTypeResolver.resolveFromPayload(responseBody));
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