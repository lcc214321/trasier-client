package com.trasier.client.impl.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfiguration.class, SpringClientConfiguration.class})
public class SpringClientConfigurationTest {

    @Autowired
    private SpringClientConfiguration config;

    @Test
    public void testPropertiesSet() throws Exception {
        assertEquals("trasier-dev-writer-demo-0", config.getClientId());
    }
}