package com.trasier.client.impl.spring4;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.impl.spring4.auth.OAuthToken;
import com.trasier.client.impl.spring4.auth.OAuthTokenSafe;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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