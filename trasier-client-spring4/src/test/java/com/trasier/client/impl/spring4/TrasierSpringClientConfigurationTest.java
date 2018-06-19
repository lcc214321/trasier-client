package com.trasier.client.impl.spring4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TrasierSpringConfiguration.class, TrasierSpringClientConfiguration.class})
public class TrasierSpringClientConfigurationTest {

    @Autowired
    private TrasierSpringClientConfiguration config;

    @Test
    public void testPropertiesSet() throws Exception {
        assertNotNull(config.getClientId());
        assertNotNull(config.getClientSecret());
    }
}