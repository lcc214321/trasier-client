package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierTracer;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;
import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import io.opentracing.util.GlobalTracer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

/**
 * For Non-Spring-Boot applications.
 */
public class TrasierSpringConfigListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());

        TrasierTracer tracer = webApplicationContext.getBean(TrasierTracer.class);
        GlobalTracer.register(tracer);

        TrasierClientConfiguration configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
        List<ServletFilterSpanDecorator> decoratorList = new ArrayList<>();
        decoratorList.add(ServletFilterSpanDecorator.STANDARD_TAGS);
        decoratorList.add(new TrasierServletFilterSpanDecorator(configuration));
        event.getServletContext().setAttribute(TracingFilter.SPAN_DECORATORS, decoratorList);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
