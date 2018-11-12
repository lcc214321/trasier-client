package com.trasier.client.opentracing.spring.interceptor.boot;

import com.trasier.client.api.Client;
import com.trasier.opentracing.spring.interceptor.InterceptorFeignConfiguration;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.cloud.feign.FeignTracingAutoConfiguration;
import io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@ConditionalOnClass(Client.class)
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(TracerAutoConfiguration.class)
@AutoConfigureBefore(FeignTracingAutoConfiguration.class)
@ConditionalOnProperty(name = "opentracing.spring.cloud.feign.enabled", havingValue = "true", matchIfMissing = true)
@Import(InterceptorFeignConfiguration.class)
public class TrasierFeignInterceptorConfiguration {

}