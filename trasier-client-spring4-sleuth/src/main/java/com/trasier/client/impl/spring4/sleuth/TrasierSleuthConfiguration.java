package com.trasier.client.impl.spring4.sleuth;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = TrasierSleuthConfiguration.class)
public class TrasierSleuthConfiguration {
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
}
