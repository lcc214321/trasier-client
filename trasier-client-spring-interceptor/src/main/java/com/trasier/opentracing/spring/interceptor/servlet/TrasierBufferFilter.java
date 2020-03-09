package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.configuration.TrasierClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TrasierBufferFilter extends GenericFilterBean {

    private static final Integer MAX_REQUEST_SIZE = 1024 * 1024;

    @Autowired
    private TrasierClientConfiguration configuration;

    public TrasierBufferFilter() {
    }

    public TrasierBufferFilter(TrasierClientConfiguration configuration) {
        this.configuration = configuration;    }

    @Override
    protected void initFilterBean() {
        if (configuration == null) {
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!configuration.isActivated() || configuration.isPayloadTracingDisabled()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        ContentCachingRequestWrapper request = createCachedRequest((HttpServletRequest) servletRequest);
        ContentCachingResponseWrapper response = createCachedResponse((HttpServletResponse) servletResponse);

        filterChain.doFilter(request, response);

        response.copyBodyToResponse();
    }

    protected ContentCachingResponseWrapper createCachedResponse(HttpServletResponse servletResponse) {
        return servletResponse instanceof ContentCachingResponseWrapper ? (ContentCachingResponseWrapper) servletResponse : new ContentCachingResponseWrapper(servletResponse);
    }

    protected ContentCachingRequestWrapper createCachedRequest(HttpServletRequest servletRequest) {
        return servletRequest instanceof ContentCachingRequestWrapper ? (ContentCachingRequestWrapper) servletRequest : new ContentCachingRequestWrapper(servletRequest, MAX_REQUEST_SIZE);
    }

}