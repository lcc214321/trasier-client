package com.trasier.client.spring.grpc;

import com.trasier.client.spring.auth.OAuthTokenSafe;
import com.trasier.client.spring.client.TrasierSpringClient;
import com.trasier.client.spring.context.TrasierSpringAccessor;
import com.trasier.client.spring.spancontrol.TrasierSampleByOperationInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {TrasierSpringGrpcConfiguration.class, TrasierSpringClient.class, TrasierSpringAccessor.class, TrasierSampleByOperationInterceptor.class, OAuthTokenSafe.class})
public class TrasierSpringGrpcConfiguration {
}