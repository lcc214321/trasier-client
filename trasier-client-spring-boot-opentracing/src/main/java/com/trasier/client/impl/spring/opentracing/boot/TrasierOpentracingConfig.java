/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package com.trasier.client.impl.spring.opentracing.boot;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.impl.spring.TrasierSpringConfiguration;
import com.trasier.client.impl.spring.opentracing.api.TrasierScopeManager;
import com.trasier.client.impl.spring.opentracing.api.TrasierTracer;
import com.trasier.client.impl.spring.opentracing.interceptor.TrasierFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TrasierSpringConfiguration.class)
@ComponentScan(basePackageClasses = { TrasierFilter.class })
public class TrasierOpentracingConfig {

    @Bean
    @ConfigurationProperties("trasier.client")
    public TrasierClientConfiguration trasierSpringClientConfiguration() {
        return new TrasierClientConfiguration();
    }

    @Bean
    @ConfigurationProperties("trasier.endpoint")
    protected TrasierEndpointConfiguration endpointConfig() {
        return new TrasierEndpointConfiguration();
    }

    @Bean
    public TrasierTracer trasierTracer(Client client, TrasierClientConfiguration configuration) {
        return new TrasierTracer(client, configuration, new TrasierScopeManager());
    }

}