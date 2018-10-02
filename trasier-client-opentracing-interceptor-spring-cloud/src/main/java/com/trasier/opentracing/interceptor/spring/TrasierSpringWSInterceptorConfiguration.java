/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package com.trasier.opentracing.interceptor.spring;

import com.trasier.opentracing.interceptor.spring.servlet.TrasierFilter;
import com.trasier.opentracing.interceptor.spring.ws.TracingClientInterceptor;
import com.trasier.opentracing.interceptor.spring.ws.TrasierClientInterceptor;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.starter.RestTemplateTracingAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
@ConditionalOnBean({Tracer.class})
@ConditionalOnClass({WebServiceTemplate.class})
@ConditionalOnProperty(
        prefix = "opentracing.spring.web.client",
        name = {"enabled"},
        matchIfMissing = true
)
public class TrasierSpringWSInterceptorConfiguration {
    @Autowired
    private Tracer tracer;

    @Autowired(required = false)
    private Set<WebServiceTemplate> webServiceTemplates;

    @Autowired(required = false)
    private Set<WebServiceGatewaySupport> webServiceGatewaySupports;

    public TrasierSpringWSInterceptorConfiguration(RestTemplateTracingAutoConfiguration.RestTemplatePostProcessingConfiguration temp) {

    }

    @Bean
    public FilterRegistrationBean trasierFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new TrasierFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public TrasierClientInterceptor trasierClientInterceptor(Tracer tracer) {
        return new TrasierClientInterceptor(tracer);
    }

    @PostConstruct()
    public void init() {
        if (webServiceTemplates != null) {
            webServiceTemplates.forEach(this::registerTracingInterceptor);
        }
        if (webServiceGatewaySupports != null) {
            webServiceGatewaySupports.stream()
                    .map(WebServiceGatewaySupport::getWebServiceTemplate)
                    .forEach(this::registerTracingInterceptor);
        }
    }

    private void registerTracingInterceptor(WebServiceTemplate webServiceTemplate) {
        ClientInterceptor[] existingInterceptors = webServiceTemplate.getInterceptors();
        if (existingInterceptors == null || notYetRegistered(Arrays.stream(existingInterceptors), TrasierClientInterceptor.class)) {
            List<ClientInterceptor> interceptors = new ArrayList<>();
            if (existingInterceptors != null) {
                interceptors.addAll(Arrays.asList(existingInterceptors));
            }
            interceptors.add(new TracingClientInterceptor(tracer));
            interceptors.add(new TrasierClientInterceptor(tracer));
            webServiceTemplate.setInterceptors(interceptors.toArray(new ClientInterceptor[interceptors.size()]));
        }
    }

    private boolean notYetRegistered(Stream<?> interceptors, Class<?> clazz) {
        return (interceptors).noneMatch(interceptor -> clazz.isAssignableFrom(interceptor.getClass()));
    }

}