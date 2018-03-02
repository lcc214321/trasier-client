package com.trasier.client.impl.spring;

import com.trasier.client.configuration.ApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@Configuration
@ComponentScan(basePackageClasses = SpringRestClient.class)
@EnableOAuth2Client
public class SpringConfiguration {
    @Bean
    protected ApplicationConfiguration appConfig() {
        return new ApplicationConfiguration();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}