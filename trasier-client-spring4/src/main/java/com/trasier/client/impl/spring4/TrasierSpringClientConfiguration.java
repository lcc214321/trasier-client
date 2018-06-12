package com.trasier.client.impl.spring4;

import com.trasier.client.configuration.ClientPropertyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:trasier.properties")
public class TrasierSpringClientConfiguration extends ClientPropertyConfiguration {
    @Value("${trasier.client.id}")
    private String clientId;

    @Value("${trasier.client.secret}")
    private String secret;

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getSecret() {
        return secret;
    }

}
