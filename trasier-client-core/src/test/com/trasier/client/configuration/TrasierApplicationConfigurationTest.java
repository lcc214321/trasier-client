package com.trasier.client.configuration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TrasierApplicationConfigurationTest {

    @Test
    public void shouldUseDefaultProperties() {
        // given
        TrasierApplicationConfiguration sut = new TrasierApplicationConfiguration();

        // when / then
        assertTrue(sut.getAuthEndpoint().length() > 1);
        assertTrue(sut.getWriterEndpoint().length() > 1);

        // and when
        System.setProperty("trasier.endpoint.auth", "auth_endpoint");
        System.setProperty( "trasier.endpoint.writer", "write_endpoint");

        sut = new TrasierApplicationConfiguration();

        // then
        assertEquals("auth_endpoint", sut.getAuthEndpoint());
        assertEquals("write_endpoint", sut.getWriterEndpoint());
    }

}