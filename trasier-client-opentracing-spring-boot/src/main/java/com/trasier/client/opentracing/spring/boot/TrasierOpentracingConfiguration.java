package com.trasier.client.opentracing.spring.boot;

import com.trasier.client.api.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.opentracing.TrasierScopeManager;
import com.trasier.client.opentracing.TrasierTracer;
import com.trasier.client.spring.TrasierSpringConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TrasierSpringConfiguration.class})
public class TrasierOpentracingConfiguration {

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