package com.trasier.client.impl.spring4;

import com.trasier.client.configuration.ApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackageClasses = SpringRestClient.class)
public class TrasierSpringConfiguration {
    @Bean
    protected ApplicationConfiguration appConfig() {
        return new ApplicationConfiguration();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}