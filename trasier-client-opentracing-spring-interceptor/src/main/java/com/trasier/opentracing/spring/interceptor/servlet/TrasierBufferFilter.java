package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierTracer;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;
import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import io.opentracing.util.GlobalTracer;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
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
import java.util.ArrayList;
import java.util.List;

public class TrasierBufferFilter extends GenericFilterBean {
    private static final String SKIP_PATTERN = TrasierBufferFilter.class.getName() + ".skipPattern";

    @Autowired
    private TrasierClientConfiguration configuration;

    public TrasierBufferFilter() {
    }

    public TrasierBufferFilter(TrasierClientConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
        if (needsInitialization()) {
            initialize();
        }

        super.initBeanWrapper(bw);
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
    }

    protected CachedServletResponseWrapper createCachedResponse(HttpServletResponse servletResponse) throws IOException {
        return servletResponse instanceof CachedServletResponseWrapper ? (CachedServletResponseWrapper) servletResponse : CachedServletResponseWrapper.create(servletResponse);
    }

    protected CachedServletRequestWrapper createCachedRequest(HttpServletRequest servletRequest) throws IOException {
        return servletRequest instanceof CachedServletResponseWrapper ? (CachedServletRequestWrapper) servletRequest : CachedServletRequestWrapper.create(servletRequest);
    }

    private synchronized void initialize() {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        TrasierClientConfiguration configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
        TrasierTracer tracer = webApplicationContext.getBean(TrasierTracer.class);
        GlobalTracer.register(tracer);
        List<ServletFilterSpanDecorator> decoratorList = new ArrayList<>();
        decoratorList.add(ServletFilterSpanDecorator.STANDARD_TAGS);
        decoratorList.add(new TrasierServletFilterSpanDecorator(configuration));
        getServletContext().setAttribute(TracingFilter.SPAN_DECORATORS, decoratorList);
    }

    private boolean needsInitialization() {
        return configuration == null;
    }

}