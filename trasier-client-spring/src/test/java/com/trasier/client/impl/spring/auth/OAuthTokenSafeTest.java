/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package com.trasier.client.impl.spring.auth;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.impl.spring.auth.OAuthToken;
import com.trasier.client.impl.spring.auth.OAuthTokenSafe;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

public class OAuthTokenSafeTest {

    @Test
    public void testRefreshTokenRequestedOnce() {
        // given
        RestTemplate restTemplate = mock(RestTemplate.class);
        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();

        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setExpiresIn("" + (80 * 1000));
        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafe(appConfig, clientConfig, restTemplate);

        // when
        sut.getAuthHeader();
        sut.getAuthHeader();

        // then
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class));
    }

    @Test
    public void testRefreshExpiredToken() {
        // given
        RestTemplate restTemplate = mock(RestTemplate.class);
        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();

        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafe(appConfig, clientConfig, restTemplate);

        // when
        sut.getAuthHeader();
        sut.getAuthHeader();

        // then
        verify(restTemplate, times(2)).postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class));
    }
}