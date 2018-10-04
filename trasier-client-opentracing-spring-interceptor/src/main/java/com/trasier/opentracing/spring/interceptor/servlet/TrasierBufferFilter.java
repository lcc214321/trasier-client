package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.configuration.TrasierClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TrasierBufferFilter extends GenericFilterBean {

    @Autowired
    private volatile TrasierClientConfiguration configuration;

    public TrasierBufferFilter() {
    }

    @Autowired
    public TrasierBufferFilter(TrasierClientConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (needsInitialization()) {
            initialize();
        }

        if (!configuration.isActivated()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        CachedServletRequestWrapper request = createCachedRequest((HttpServletRequest) servletRequest);
        CachedServletResponseWrapper response = createCachedResponse((HttpServletResponse) servletResponse);

        filterChain.doFilter(request, response);
    }

    protected CachedServletResponseWrapper createCachedResponse(HttpServletResponse servletResponse) throws IOException {
        return servletResponse instanceof CachedServletResponseWrapper ? (CachedServletResponseWrapper) servletResponse : CachedServletResponseWrapper.create(servletResponse);
    }

    protected CachedServletRequestWrapper createCachedRequest(HttpServletRequest servletRequest) throws IOException {
        return servletRequest instanceof CachedServletResponseWrapper ? (CachedServletRequestWrapper) servletRequest : CachedServletRequestWrapper.create(servletRequest);
    }

    // TODO optimize this
    private synchronized void initialize() {
        if (needsInitialization()) {
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
        }
    }

    private boolean needsInitialization() {
        return configuration == null;
    }

}