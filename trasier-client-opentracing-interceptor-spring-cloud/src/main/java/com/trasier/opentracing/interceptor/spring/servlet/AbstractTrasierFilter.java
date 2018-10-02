package com.trasier.opentracing.interceptor.spring.servlet;

import com.trasier.client.TrasierConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

abstract class AbstractTrasierFilter extends GenericFilterBean {
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";

    protected String extractIncomingEndpointName(HttpServletRequest servletRequest) {
        String incomingEndpointName = servletRequest.getHeader(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME);
        return StringUtils.isEmpty(incomingEndpointName) ? TrasierConstants.UNKNOWN_IN : incomingEndpointName;
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

    protected Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headerMap = new TreeMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }
        return headerMap;
    }
}