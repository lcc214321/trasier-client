package com.trasier.client.configuration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ApplicationConfigurationTest {

    @Test
    public void shouldUseDefaultProperties() {
        // given
        ApplicationConfiguration sut = new ApplicationConfiguration();

        // when / then
        assertTrue(sut.getAuthEndpoint().length() > 1);
        assertTrue(sut.getWriterEndpoint().length() > 1);

        // and when
        System.setProperty("trasier.app.auth.endpoint", "auth_endpoint");
        System.setProperty( "trasier.app.writer.endpoint", "write_endpoint");

        sut = new ApplicationConfiguration();

        // then
        assertEquals("auth_endpoint", sut.getAuthEndpoint());
        assertEquals("write_endpoint", sut.getWriterEndpoint());
    }

}