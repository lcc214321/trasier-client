package com.trasier.client.impl.spring4;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TrasierSpringClientConfigurationTest {
    private TrasierSpringClientConfiguration config;

    public TrasierSpringClientConfigurationTest() {
        config = new TrasierSpringClientConfiguration();
        config.setAccountId("account-id");
        config.setSpaceKey("space-key");
        config.setClientId("client-id");
        config.setClientSecret("client-secret");
    }

    @Test
    public void testPropertiesSet() throws Exception {
        assertNotNull(config.getAccountId());
        assertNotNull(config.getSpaceKey());
        assertNotNull(config.getClientId());
        assertNotNull(config.getClientSecret());
    }
}