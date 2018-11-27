package com.trasier.opentracing.spring.interceptor;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.TrasierSamplingInterceptor;
import com.trasier.opentracing.spring.interceptor.ws.TracingClientInterceptor;
import com.trasier.opentracing.spring.interceptor.ws.TrasierClientInterceptor;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Stream;

@Configuration
public class InterceptorWSConfiguration {
    @Autowired
    private Tracer tracer;

    @Autowired
    private TrasierClientConfiguration configuration;

    @Autowired(required = false)
    private Set<WebServiceTemplate> webServiceTemplates;

    @Autowired(required = false)
    private Set<WebServiceGatewaySupport> webServiceGatewaySupports;

    @Autowired(required = false)
    private List<TrasierSamplingInterceptor> samplingFilter;

    @Bean
    public TrasierClientInterceptor trasierClientInterceptor(Tracer tracer) {
        return new TrasierClientInterceptor(tracer);
    }

    @PostConstruct
    public void init() {
        if (configuration.isActivated()) {
            if (webServiceTemplates != null) {
                webServiceTemplates.forEach(this::registerTracingInterceptor);
            }
            if (webServiceGatewaySupports != null) {
                webServiceGatewaySupports.stream()
                        .map(WebServiceGatewaySupport::getWebServiceTemplate)
                        .forEach(this::registerTracingInterceptor);
            }
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
                interceptors.add(new TracingClientInterceptor(tracer, samplingFilter == null ? Collections.emptyList() : samplingFilter));
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