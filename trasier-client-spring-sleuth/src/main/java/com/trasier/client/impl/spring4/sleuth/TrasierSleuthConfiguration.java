package com.trasier.client.impl.spring4.sleuth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = TrasierSleuthConfiguration.class)
@ConfigurationProperties("trasier.sleuth")
public class TrasierSleuthConfiguration {
}
