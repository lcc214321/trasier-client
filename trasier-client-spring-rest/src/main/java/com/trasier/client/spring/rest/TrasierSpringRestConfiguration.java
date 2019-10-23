package com.trasier.client.spring.rest;

import com.trasier.client.spring.auth.OAuthToken;
import com.trasier.client.spring.auth.OAuthTokenSafe;
import com.trasier.client.spring.client.TrasierSpringClient;
import com.trasier.client.spring.context.TrasierSpringAccessor;
import com.trasier.client.spring.spancontrol.TrasierSampleByOperationInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {TrasierSpringRestConfiguration.class, OAuthToken.class, TrasierSpringClient.class, TrasierSpringAccessor.class, TrasierSampleByOperationInterceptor.class, OAuthTokenSafe.class})
public class TrasierSpringRestConfiguration {
}