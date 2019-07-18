package com.trasier.client.spring.auth;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class OAuthTokenSafeTest {

    @Test
    public void testRefreshTokenRequestedOnce() {
        // given
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();

        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (80 * 1000));
        token.setRefreshExpiresIn("" + (160 * 1000));
        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);

        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafeImpl(appConfig, clientConfig, restTemplate);

        // when
        sut.getToken();
        sut.getToken();

        // then
        Mockito.verify(restTemplate).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class));
    }

    @Test
    public void testRefreshExpiredToken() {
        // given
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();

        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (-40 * 1000));
        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);

        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafeImpl(appConfig, clientConfig, restTemplate);

        // when
        sut.getToken();
        sut.getToken();

        // then
        Mockito.verify(restTemplate, Mockito.times(2)).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class));
    }

    @Test
    public void testRefreshExpiredTokenValidRefreshToken() {
        // given
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();

        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (80 * 1000));
        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);

        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafeImpl(appConfig, clientConfig, restTemplate);

        // when 1
        HttpEntity<MultiValueMap<String, String>> withoutTokenRequestEntity = ((OAuthTokenSafeImpl) sut).createTokenRequestEntity();
        sut.getToken();

        // then 1
        Assert.assertEquals("client_credentials", withoutTokenRequestEntity.getBody().getFirst("grant_type"));
        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.eq(withoutTokenRequestEntity), ArgumentMatchers.eq(OAuthToken.class));

        // when 2
        HttpEntity<MultiValueMap<String, String>> withInvalidTokenRequestEntity = ((OAuthTokenSafeImpl) sut).createTokenRequestEntity();
        sut.getToken();

        // then 2
        Assert.assertEquals("refresh_token", withInvalidTokenRequestEntity.getBody().getFirst("grant_type"));
        Assert.assertEquals("refreshTokenMock", withInvalidTokenRequestEntity.getBody().getFirst("refresh_token"));
        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.eq(withInvalidTokenRequestEntity), ArgumentMatchers.eq(OAuthToken.class));
    }
}