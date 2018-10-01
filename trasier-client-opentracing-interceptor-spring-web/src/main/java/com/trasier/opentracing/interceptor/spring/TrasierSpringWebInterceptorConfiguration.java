package com.trasier.opentracing.interceptor.spring;

import com.trasier.opentracing.interceptor.spring.rest.TrasierClientRequestInterceptor;
import com.trasier.opentracing.interceptor.spring.servlet.TrasierFilter;
import com.trasier.opentracing.interceptor.spring.ws.TrasierClientInterceptor;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Configuration
@ComponentScan(basePackageClasses = {TrasierFilter.class})
public class TrasierSpringWebInterceptorConfiguration {
    @Autowired
    private Tracer tracer;

    @Autowired(required = false)
    private Set<WebServiceTemplate> webServiceTemplates;
    @Autowired(required = false)
    private Set<RestTemplate> restTemplates;

    @Bean
    public TrasierClientInterceptor trasierClientInterceptor(Tracer tracer) {
        return new TrasierClientInterceptor(tracer);
    }

    @PostConstruct
    public void init() {
        if (this.webServiceTemplates != null) {
            webServiceTemplates.forEach(this::registerTracingInterceptor);
        }
        if (this.restTemplates != null) {
            restTemplates.forEach(this::registerTracingInterceptor);
        }
    }

    private void registerTracingInterceptor(WebServiceTemplate webServiceTemplate) {
        ClientInterceptor[] existingInterceptors = webServiceTemplate.getInterceptors();
        List<ClientInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new TrasierClientInterceptor(tracer));
        interceptors.addAll(Arrays.asList(existingInterceptors));
        webServiceTemplate.setInterceptors(interceptors.toArray(new ClientInterceptor[interceptors.size()]));
    }

    private void registerTracingInterceptor(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> existingInterceptors = restTemplate.getInterceptors();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.addAll(existingInterceptors);
        interceptors.add(new TrasierClientRequestInterceptor(tracer));
        restTemplate.setInterceptors(interceptors);

    }
}
