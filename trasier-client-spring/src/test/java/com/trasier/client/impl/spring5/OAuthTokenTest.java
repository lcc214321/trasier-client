package com.trasier.client.impl.spring5;

import com.trasier.client.impl.spring5.auth.OAuthToken;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OAuthTokenTest {
    @Test
    public void getAccessToken() {
        OAuthToken token = new OAuthToken();
        token.setAccessToken("at");
        assertEquals("at", token.getAccessToken());
    }

    @Test
    public void getExpiresIn() {
        OAuthToken token = new OAuthToken();
        token.setExpiresIn("1");
        assertEquals("1", token.getExpiresIn());
    }
    @Test
    public void getRefreshToken() {
        OAuthToken token = new OAuthToken();
        token.setRefreshToken("rt");
        assertEquals("rt", token.getRefreshToken());
    }

    @Test
    public void getRefreshExpiresIn() {
        OAuthToken token = new OAuthToken();
        token.setRefreshExpiresIn("2");
        assertEquals("2", token.getRefreshExpiresIn());
    }
}