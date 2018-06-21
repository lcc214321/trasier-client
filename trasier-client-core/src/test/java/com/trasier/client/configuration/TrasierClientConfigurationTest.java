/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package com.trasier.client.configuration;

import com.trasier.client.configuration.TrasierClientConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TrasierClientConfigurationTest {
    private TrasierClientConfiguration config;

    public TrasierClientConfigurationTest() {
        config = new TrasierClientConfiguration();
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