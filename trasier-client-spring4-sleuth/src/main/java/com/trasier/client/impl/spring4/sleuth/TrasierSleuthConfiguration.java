package com.trasier.client.impl.spring4.sleuth;

import com.trasier.client.impl.spring4.TrasierSpringClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = TrasierSleuthConfiguration.class)
public class TrasierSleuthConfiguration {
    @Bean
    @ConfigurationProperties("trasier.client")
    public TrasierSpringClientConfiguration trasierSpringClientConfiguration() {
        return new TrasierSpringClientConfiguration();
    }
}
