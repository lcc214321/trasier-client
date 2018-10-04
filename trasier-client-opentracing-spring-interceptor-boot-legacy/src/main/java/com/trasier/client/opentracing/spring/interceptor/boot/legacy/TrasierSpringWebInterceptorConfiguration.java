package com.trasier.client.opentracing.spring.interceptor.boot.legacy;

import com.trasier.opentracing.spring.interceptor.InterceptorWebConfiguration;
import com.trasier.opentracing.spring.interceptor.servlet.TrasierBufferFilter;
import com.trasier.opentracing.spring.interceptor.servlet.TrasierFilter;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.starter.WebClientTracingProperties;
import io.opentracing.contrib.spring.web.starter.WebTracingProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.support.InterceptingHttpAccessor;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnBean({Tracer.class, InterceptingHttpAccessor.class})
@ConditionalOnClass({RestTemplate.class})
@ConditionalOnProperty(prefix = WebClientTracingProperties.CONFIGURATION_PREFIX, name = "enabled", matchIfMissing = true)
@Import(InterceptorWebConfiguration.class)
public class TrasierSpringWebInterceptorConfiguration {
    @Bean
    public FilterRegistrationBean trasierBufferFilter(WebTracingProperties tracingConfiguration) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setOrder(tracingConfiguration.getOrder() - 2);
        registrationBean.setFilter(new TrasierBufferFilter());
        registrationBean.setUrlPatterns(tracingConfiguration.getUrlPatterns());
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean trasierFilter(WebTracingProperties tracingConfiguration) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setOrder(tracingConfiguration.getOrder() - 1);
        registrationBean.setFilter(new TrasierFilter());
        registrationBean.setUrlPatterns(tracingConfiguration.getUrlPatterns());
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }
}