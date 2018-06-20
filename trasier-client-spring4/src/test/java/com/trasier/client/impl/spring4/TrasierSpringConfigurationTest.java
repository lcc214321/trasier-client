package com.trasier.client.impl.spring4;

import org.junit.Test;

public class TrasierSpringConfigurationTest {
    @Test
    public void appConfig() {
        new TrasierSpringConfiguration().appConfig();
    }

    @Test
    public void propertySourcesPlaceholderConfigurer() {
        new TrasierSpringConfiguration().propertySourcesPlaceholderConfigurer();
    }

    @Test
    public void createRestTemplate() {
        new TrasierSpringConfiguration().createRestTemplate();
    }
}