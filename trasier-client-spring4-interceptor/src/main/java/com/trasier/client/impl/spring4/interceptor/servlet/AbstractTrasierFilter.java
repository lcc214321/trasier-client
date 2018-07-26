package com.trasier.client.impl.spring4.interceptor.servlet;

import com.trasier.client.TrasierConstants;
import com.trasier.client.model.ContentType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractTrasierFilter extends GenericFilterBean {
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";

    protected boolean isEnabled(ServletRequest servletRequest) {
        return servletRequest instanceof HttpServletRequest &&
                extractConversationId((HttpServletRequest) servletRequest) != null;
    }

    protected String extractConversationId(HttpServletRequest servletRequest) {
        return servletRequest.getHeader(TrasierConstants.HEADER_CONVERSATION_ID);
    }

    protected String extractTraceId(HttpServletRequest servletRequest) {
        return servletRequest.getHeader(TrasierConstants.HEADER_TRACE_ID);
    }

    protected String extractSpanId(HttpServletRequest servletRequest) {
        return servletRequest.getHeader(TrasierConstants.HEADER_SPAN_ID);
    }

    protected String extractIncomingEndpointName(HttpServletRequest servletRequest) {
        String incomingEndpointName = servletRequest.getHeader(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME);
        return StringUtils.isEmpty(incomingEndpointName) ? TrasierConstants.UNKNOWN : incomingEndpointName;
    }

    protected ContentType extractContentType(HttpServletRequest servletRequest) {
        return ContentType.XML;
    }

    protected Map<String, String> getRequestHeaders(HttpServletRequest request) {
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

    protected Map<String, List<String>> getRequestParameters(HttpServletRequest request) {
        Map<String, List<String>> parametersMap = new TreeMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String parameterKey : parameterMap.keySet()) {
            parametersMap.put(parameterKey, Arrays.asList(parameterMap.get(parameterKey)));
        }
        return parametersMap;
    }


    protected Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headerMap = new TreeMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }
        return headerMap;
    }

    protected String extractOperationName(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String operation = requestURI;
        return method.toUpperCase() + "_" + operation.toUpperCase();
    }
}