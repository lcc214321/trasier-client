package com.trasier.client.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.trasier.client.spring.auth.OAuthToken;
import com.trasier.client.spring.client.TrasierSpringClient;
import com.trasier.client.spring.context.TrasierSpringAccessor;
import com.trasier.client.spring.spancontrol.TrasierSampleByOperationInterceptor;

@Configuration
@ComponentScan(basePackageClasses = {OAuthToken.class, TrasierSpringClient.class, TrasierSpringAccessor.class, TrasierSampleByOperationInterceptor.class})
public class TrasierSpringConfiguration {

    @Bean
    public TrasierSpringClientQueueConfiguration trasierSpringConfiguration() {
        return new TrasierSpringClientQueueConfiguration();
    }

}
