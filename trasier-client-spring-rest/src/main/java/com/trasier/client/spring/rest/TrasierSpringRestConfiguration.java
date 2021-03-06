package com.trasier.client.spring.rest;

import com.trasier.client.spring.client.TrasierSpringClient;
import com.trasier.client.spring.context.TrasierSpringAccessor;
import com.trasier.client.spring.spancontrol.TrasierSpanFilterInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {TrasierSpringRestConfiguration.class, TrasierSpringClient.class, TrasierSpringAccessor.class, TrasierSpanFilterInterceptor.class})
public class TrasierSpringRestConfiguration {
}