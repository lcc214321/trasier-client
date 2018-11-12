package com.trasier.opentracing.spring.interceptor;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.opentracing.spring.interceptor.feign.TrasierFeignSpanDecorator;
import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.cloud.feign.FeignContextBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorFeignConfiguration {

    @Autowired
    private TrasierClientConfiguration configuration;

    @Bean
    public FeignSpanDecorator trasierServletFilterSpanDecorator() {
        return new TrasierFeignSpanDecorator(configuration);
    }

    @Bean
    public FeignContextBeanPostProcessor opentracingFeignTracingAutoConfiguration(io.opentracing.contrib.spring.cloud.feign.FeignContextBeanPostProcessor postProcessor) {
        return postProcessor;
    }

}