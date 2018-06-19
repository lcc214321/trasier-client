package com.trasier.client.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClientPropertyConfigurationTest {

    @Before
    public void setup() {
        System.setProperty("trasier.client.id", "client-id");
        System.setProperty("trasier.client.secret", "client-secret");
    }

    @After
    public void cleanup() {
        System.setProperty("trasier.client.id", "");
        System.setProperty("trasier.client.secret", "");
    }

    @Test
    public void shouldLoadPropsFromSystermProperties() {
        // given
        ClientPropertyConfiguration sut = new ClientPropertyConfiguration();

        // when
        String clientId = sut.getClientId();
        String secret = sut.getClientSecret();

        // then
        assertEquals("client-id", clientId);
        assertEquals("client-secret", secret);
    }

}