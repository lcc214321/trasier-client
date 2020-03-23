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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TrasierServletFilterSpanDecorator implements ServletFilterSpanDecorator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierServletFilterSpanDecorator.class);

    private static final Integer MAX_RESPONSE_SIZE = 1024 * 1024;
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";
    private static final List<String> USER_AGENTS_WEB = Arrays.asList("mozilla", "chrome", "opera", "explorer", "safari");
    private static final List<String> USER_AGENTS_HANDHELD_DEVICE = Arrays.asList("iphone", "ipad", "android", "blackberry", "phone", "kindle");

    private final TrasierClientConfiguration configuration;
    private final List<TrasierSamplingInterceptor> samplingInterceptors;
    private Endpoint localEndpoint;

    public TrasierServletFilterSpanDecorator(TrasierClientConfiguration configuration, List<TrasierSamplingInterceptor> samplingInterceptors) {
        this.configuration = configuration;
        this.samplingInterceptors = samplingInterceptors;
    }

    @Override
    public void onRequest(HttpServletRequest httpServletRequest, Span span) {
        if (configuration.isActivated()) {
            TrasierSpan activeSpan = (TrasierSpan) span;
            com.trasier.client.api.Span trasierSpan = activeSpan.unwrap();
            String conversationId = trasierSpan.getConversationId();
            MDC.put(TrasierConstants.HEADER_CONVERSATION_ID, conversationId);
            handleRequest(httpServletRequest, trasierSpan);
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
        if (configuration.isActivated()) {
            MDC.remove(TrasierConstants.HEADER_CONVERSATION_ID);
            TrasierSpan activeSpan = (TrasierSpan) span;
            com.trasier.client.api.Span trasierSpan = activeSpan.unwrap();
            handleResponse(response, trasierSpan);
            handleRequestPayload(httpServletRequest, trasierSpan);
            handleResponsePayload(response, trasierSpan);
        }
    }

    @Override
    public void onError(HttpServletRequest httpServletRequest, HttpServletResponse response, Throwable exception, Span span) {
        if (configuration.isActivated() && response instanceof ContentCachingResponseWrapper) {
            MDC.remove(TrasierConstants.HEADER_CONVERSATION_ID);
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setStatus(TrasierConstants.STATUS_ERROR);
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.getOutgoingHeader().putAll(getResponseHeaders(response));
            trasierSpan.setOutgoingContentType(ContentType.TEXT);
            if (!configuration.isPayloadTracingDisabled()) {
                handleRequestPayload(httpServletRequest, trasierSpan);
                trasierSpan.setOutgoingData(ExceptionUtils.getString(exception));
            }
        }
    }

    @Override
    public void onTimeout(HttpServletRequest httpServletRequest, HttpServletResponse response, long timeout, Span span) {
        if (configuration.isActivated() && response instanceof ContentCachingResponseWrapper) {
            MDC.remove(TrasierConstants.HEADER_CONVERSATION_ID);
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setStatus(TrasierConstants.STATUS_ERROR);
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.getOutgoingHeader().putAll(getResponseHeaders(response));
            trasierSpan.setOutgoingData("Execution timeout after " + timeout);
            trasierSpan.setOutgoingContentType(ContentType.TEXT);
        }
    }

    private void handleRequest(HttpServletRequest request, com.trasier.client.api.Span currentSpan) {
        //TODO handle headers und parameters
        Map<String, String> requestHeaders = getRequestHeaders(request);
        currentSpan.getIncomingHeader().putAll(requestHeaders);
        currentSpan.setName(currentSpan.getName());
        currentSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
        enhanceIncomingEndpoint(currentSpan, request, requestHeaders);
        enhanceOutgoingEndpoint(currentSpan, request);
    }

    private void handleRequestPayload(HttpServletRequest request, com.trasier.client.api.Span currentSpan) {
        if (!configuration.isPayloadTracingDisabled() && request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper contentCachingRequestWrapper = (ContentCachingRequestWrapper) request;
            try {
                //TODO read all left -> should be limited by max payload size
                while (contentCachingRequestWrapper.getInputStream().read() != -1) ;
            } catch (IOException e) {
                LOGGER.error("Could not handle request.", e);
            }
            byte[] requestData = contentCachingRequestWrapper.getContentAsByteArray();
            if (GzipUtil.isGzipStream(requestData)) {
                requestData = GzipUtil.decompress(requestData);
            }
            String requestBody = new String(requestData); //TODO memory waste?
            currentSpan.setIncomingData(requestBody);
            currentSpan.setIncomingContentType(ContentTypeResolver.resolveFromPayload(requestBody));
        }
    }

    private void enhanceIncomingEndpoint(com.trasier.client.api.Span span, ServletRequest request, Map<String, String> requestHeaders) {
        span.getIncomingEndpoint().setName(extractIncomingEndpointName(requestHeaders, request));
    }

    private void enhanceOutgoingEndpoint(com.trasier.client.api.Span span, ServletRequest request) {
        // no synchronisation on purpose
        if (this.localEndpoint == null) {
            Endpoint endpoint = new Endpoint(configuration.getSystemName());
            endpoint.setHostname(request.getLocalName());
            endpoint.setIpAddress(request.getLocalAddr());
            endpoint.setPort("" + request.getLocalPort());
            this.localEndpoint = endpoint;
        }
        span.setOutgoingEndpoint(localEndpoint);
    }

    protected String extractIncomingEndpointName(Map<String, String> requestHeaders, ServletRequest servletRequest) {
        String incomingEndpointName = ((HttpServletRequest) servletRequest).getHeader(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME);
        if (StringUtils.isEmpty(incomingEndpointName)) {
            String userAgent = requestHeaders.get("user-agent");
            if (!StringUtils.isEmpty(userAgent)) {
                for (String agent : USER_AGENTS_WEB) {
                    if (userAgent.toLowerCase().contains(agent)) {
                        return "Web Browser";
                    }
                }
                for (String agent : USER_AGENTS_HANDHELD_DEVICE) {
                    if (userAgent.toLowerCase().contains(agent)) {
                        return "Handheld Device";
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
                if (headerValue != null) {
                    headerMap.put(headerKey, headerValue);
                }
            }
        }
        return headerMap;
    }

    private void handleResponse(HttpServletResponse response, com.trasier.client.api.Span currentSpan) {
        //TODO use Clock everywhere
        currentSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
        currentSpan.getOutgoingHeader().putAll(getResponseHeaders(response));
    }

    private void handleResponsePayload(HttpServletResponse response, com.trasier.client.api.Span currentSpan) {
        if (!configuration.isPayloadTracingDisabled() && response instanceof ContentCachingResponseWrapper) {
            String responseBody = extractResponseBody((ContentCachingResponseWrapper) response);
            currentSpan.setOutgoingData(responseBody);
            currentSpan.setOutgoingContentType(ContentTypeResolver.resolveFromPayload(responseBody));
        }
    }

    private String extractResponseBody(ContentCachingResponseWrapper response) {
        byte[] responseData;

        if (response.getContentSize() <= MAX_RESPONSE_SIZE) {
            responseData = response.getContentAsByteArray();
        } else {
            responseData = new byte[MAX_RESPONSE_SIZE];
            try {
                response.getContentInputStream().read(responseData);
            } catch (IOException e) {
                LOGGER.error("Response cannot be extracted", e);
                return null;
            }
        }

        if (GzipUtil.isGzipStream(responseData)) {
            responseData = GzipUtil.decompress(responseData);
        }

        return new String(responseData); //TODO memory waste?
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
