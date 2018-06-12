package com.trasier.client.impl.spring4.sleuth;

import com.google.gson.GsonBuilder;
import com.trasier.client.impl.spring4.CachedServletRequestWrapper;
import com.trasier.client.impl.spring4.CachedServletResponseWrapper;
import com.trasier.client.impl.spring4.TrasierConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class TrasierFilter extends GenericFilterBean {
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";

    private final SpanAccessor spanAccessor;

    @Autowired
    public TrasierFilter(SpanAccessor spanAccessor) {
        this.spanAccessor = spanAccessor;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (isEnabled(servletRequest)) {
            CachedServletRequestWrapper request = CachedServletRequestWrapper.create((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = CachedServletResponseWrapper.create((HttpServletResponse) servletResponse);

            Span currentSpan = spanAccessor.getCurrentSpan();
            String conversationId = request.getHeader(TrasierConstants.HEADER_CONVERSATION_ID);
            currentSpan.tag(TrasierConstants.TAG_CONVERSATION_ID, conversationId);
            currentSpan.tag(TrasierConstants.TAG_OPERATION_NAME, getOperationName(request));

            Map<String, String> requestHeaders = getRequestHeaders(request);
            Map<String, List<String>> parameters = getRequestParameters(request);
            String requestMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(requestHeaders, parameters));
            currentSpan.tag(TrasierConstants.TAG_REQUEST_MESSAGE, requestMessage);

            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                //handle exception to log and rethrow
            }

            Map<String, Integer> statusMap = Collections.singletonMap("status", response.getStatus());
            Map<String, String> responseHeaders = getResponseHeaders(response);
            String responseBody = response.getCachedData();
            String responseMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(statusMap, responseHeaders, responseBody));
            currentSpan.tag(TrasierConstants.TAG_RESPONSE_MESSAGE, responseMessage);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isEnabled(ServletRequest servletRequest) {
        return servletRequest instanceof HttpServletRequest &&
                ((HttpServletRequest)servletRequest).getHeader(TrasierConstants.HEADER_CONVERSATION_ID) != null;
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new TreeMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerKey = headerNames.nextElement();
            if(!HEADER_KEY_AUTHORIZATION.equalsIgnoreCase(headerKey)) {
                String headerValue = request.getHeader(headerKey);
                headerMap.put(headerKey, headerValue);
            }
        }
        return headerMap;
    }

    private Map<String, List<String>> getRequestParameters(HttpServletRequest request) {
        Map<String, List<String>> parametersMap = new TreeMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String parameterKey : parameterMap.keySet()) {
            parametersMap.put(parameterKey, Arrays.asList(parameterMap.get(parameterKey)));
        }
        return parametersMap;
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

    private String getOperationName(CachedServletRequestWrapper request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String operation = requestURI.replace("/api/", "");
        if(operation.contains("/")) {
            operation = operation.substring(0, operation.indexOf("/"));
        }
        return method.toUpperCase() + "_" + operation.toUpperCase();
    }
}