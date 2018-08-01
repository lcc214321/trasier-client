package com.trasier.client.impl.spring4;

import org.junit.Test;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TrasierSpringConfigurationTest {
    @Test
    public void propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = TrasierSpringConfiguration.propertySourcesPlaceholderConfigurer();
        assertNotNull(configurer);
    }

    @Test
    public void createRestTemplate() {
        TrasierSpringConfiguration sut = new TrasierSpringConfiguration();
        RestTemplate restTemplate = sut.createRestTemplate();
        assertNotNull(restTemplate);
    }

    @Test
    public void getterSetterTest() {
        TrasierSpringConfiguration sut = new TrasierSpringConfiguration();

        sut.setQueueSize(1);
        sut.setQueueDelay(2);
        sut.setMaxTaskCount(3);
        sut.setMaxSpansPerTask(4);
        sut.setQueueSizeErrorThresholdMultiplicator(5);

        assertEquals(1, sut.getQueueSize());
        assertEquals(2, sut.getQueueDelay());
        assertEquals(3, sut.getMaxTaskCount());
        assertEquals(4, sut.getMaxSpansPerTask());
        assertEquals(5, sut.getQueueSizeErrorThresholdMultiplicator());

    }
}