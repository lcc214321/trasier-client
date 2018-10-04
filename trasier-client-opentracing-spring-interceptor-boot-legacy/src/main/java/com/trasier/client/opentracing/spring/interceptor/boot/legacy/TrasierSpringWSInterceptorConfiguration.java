package com.trasier.client.opentracing.spring.interceptor.boot;/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

import com.trasier.opentracing.spring.interceptor.ws.TracingClientInterceptor;
import com.trasier.opentracing.spring.interceptor.ws.TrasierClientInterceptor;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.InterceptingHttpAccessor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
@ConditionalOnBean({Tracer.class, InterceptingHttpAccessor.class})
@ConditionalOnClass({WebServiceTemplate.class, ClientInterceptorAdapter.class})
@ConditionalOnProperty(
        prefix = "opentracing.spring.web.client",
        name = {"enabled"},
        matchIfMissing = true
)
@AutoConfigureAfter({TrasierSpringWebInterceptorConfiguration.class})
public class TrasierSpringWSInterceptorConfiguration {
    @Autowired
    private Tracer tracer;

    @Autowired(required = false)
    private Set<WebServiceTemplate> webServiceTemplates;

    @Autowired(required = false)
    private Set<WebServiceGatewaySupport> webServiceGatewaySupports;

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
            if (interceptors.stream().noneMatch(i -> i instanceof TracingClientInterceptor)) {
                interceptors.add(new TracingClientInterceptor(tracer));
            }
            if (interceptors.stream().noneMatch(i -> i instanceof TrasierClientInterceptor)) {
                interceptors.add(new TrasierClientInterceptor(tracer));
            }
            webServiceTemplate.setInterceptors(interceptors.toArray(new ClientInterceptor[interceptors.size()]));
        }
    }

    private boolean notYetRegistered(Stream<?> interceptors, Class<?> clazz) {
        return (interceptors).noneMatch(interceptor -> clazz.isAssignableFrom(interceptor.getClass()));
    }
}