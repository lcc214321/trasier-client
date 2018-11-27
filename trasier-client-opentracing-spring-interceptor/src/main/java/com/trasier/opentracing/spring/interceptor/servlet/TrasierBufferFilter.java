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
    private TrasierClientConfiguration configuration;

    public TrasierBufferFilter() {
    }

    public TrasierBufferFilter(TrasierClientConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void initFilterBean() {
        if (configuration == null) {
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!configuration.isActivated()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        CachedServletRequestWrapper request = createCachedRequest((HttpServletRequest) servletRequest);
        CachedServletResponseWrapper response = createCachedResponse((HttpServletResponse) servletResponse);

        filterChain.doFilter(request, response);

        response.getWriter().flush();
    }

    protected CachedServletResponseWrapper createCachedResponse(HttpServletResponse servletResponse) throws IOException {
        return servletResponse instanceof CachedServletResponseWrapper ? (CachedServletResponseWrapper) servletResponse : CachedServletResponseWrapper.create(servletResponse);
    }

    protected CachedServletRequestWrapper createCachedRequest(HttpServletRequest servletRequest) throws IOException {
        return servletRequest instanceof CachedServletResponseWrapper ? (CachedServletRequestWrapper) servletRequest : CachedServletRequestWrapper.create(servletRequest);
    }

}