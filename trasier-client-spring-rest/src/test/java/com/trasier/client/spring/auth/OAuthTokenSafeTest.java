package com.trasier.client.spring.auth;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (80 * 1000));
        token.setRefreshExpiresIn("" + (160 * 1000));
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
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (-40 * 1000));
        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafe(appConfig, clientConfig, restTemplate);

        // when
        sut.getAuthHeader();
        sut.getAuthHeader();

        // then
        verify(restTemplate, times(2)).postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class));
    }

    @Test
    public void testRefreshExpiredTokenValidRefreshToken() {
        // given
        RestTemplate restTemplate = mock(RestTemplate.class);
        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();

        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (80 * 1000));
        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafe(appConfig, clientConfig, restTemplate);

        // when 1
        HttpEntity<MultiValueMap<String, String>> withoutTokenRequestEntity = sut.createTokenRequestEntity();
        sut.getAuthHeader();

        // then 1
        Assert.assertEquals("client_credentials", withoutTokenRequestEntity.getBody().getFirst("grant_type"));
        verify(restTemplate, times(1)).postForEntity(anyString(), eq(withoutTokenRequestEntity), eq(OAuthToken.class));

        // when 2
        HttpEntity<MultiValueMap<String, String>> withInvalidTokenRequestEntity = sut.createTokenRequestEntity();
        sut.getAuthHeader();

        // then 2
        Assert.assertEquals("refresh_token", withInvalidTokenRequestEntity.getBody().getFirst("grant_type"));
        Assert.assertEquals("refreshTokenMock", withInvalidTokenRequestEntity.getBody().getFirst("refresh_token"));
        verify(restTemplate, times(1)).postForEntity(anyString(), eq(withInvalidTokenRequestEntity), eq(OAuthToken.class));
    }
}