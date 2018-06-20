package com.trasier.client.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClientPropertyConfigurationTest {

    @Before
    public void setup() {
        System.setProperty("trasier.client.clientId", "client-id");
        System.setProperty("trasier.client.clientSecret", "client-secret");
        System.setProperty("trasier.client.accountId", "111");
        System.setProperty("trasier.client.spaceKey", "space");
    }

    @After
    public void cleanup() {
        System.setProperty("trasier.client.clientId", "");
        System.setProperty("trasier.client.clientSecret", "");
        System.setProperty("trasier.client.spaceKey", "");
    }

    @Test
    public void shouldLoadPropsFromSystermProperties() {
        // given
        ClientPropertyConfiguration sut = new ClientPropertyConfiguration();

        // when
        String clientId = sut.getClientId();
        String secret = sut.getClientSecret();
        String spaceKey = sut.getSpaceKey();
        String accountId = sut.getAccountId();

        // then
        assertEquals("client-id", clientId);
        assertEquals("client-secret", secret);
        assertEquals("111", accountId);
        assertEquals("space", spaceKey);
    }

}