package com.trasier.opentracing.interceptor.spring;

import com.trasier.opentracing.interceptor.spring.rest.TrasierClientRequestInterceptor;
import com.trasier.opentracing.interceptor.spring.servlet.TrasierFilter;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.starter.RestTemplateTracingAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
@ConditionalOnBean({Tracer.class})
@ConditionalOnClass({RestTemplate.class})
@ConditionalOnProperty(
        prefix = "opentracing.spring.web.client",
        name = {"enabled"},
        matchIfMissing = true
)
@AutoConfigureAfter({RestTemplateTracingAutoConfiguration.RestTemplatePostProcessingConfiguration.class})
public class TrasierSpringWebInterceptorConfiguration {
    @Autowired
    private Tracer tracer;

    @Autowired(required = false)
    private Set<RestTemplate> restTemplates;

    @Bean
    public FilterRegistrationBean trasierFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new TrasierFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @PostConstruct()
    public void init() {
        if (restTemplates != null) {
            restTemplates.forEach(this::registerTracingInterceptor);
        }
    }

    private void registerTracingInterceptor(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> existingInterceptors = restTemplate.getInterceptors();
        if (existingInterceptors == null || notYetRegistered(existingInterceptors.stream(), TrasierClientRequestInterceptor.class)) {
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            if (existingInterceptors != null) {
                interceptors.addAll(existingInterceptors);
            }
            interceptors.add(new TrasierClientRequestInterceptor(tracer));
            if (!(restTemplate.getRequestFactory() instanceof BufferingClientHttpRequestFactory)) {
                restTemplate.setInterceptors(Collections.emptyList());
                restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));
            }
            restTemplate.setInterceptors(interceptors);

        }

    }

    private boolean notYetRegistered(Stream<?> interceptors, Class<?> clazz) {
        return (interceptors).noneMatch(interceptor -> clazz.isAssignableFrom(interceptor.getClass()));
    }
}