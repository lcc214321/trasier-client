package com.trasier.client.auth;

//TODO
public class OAuthTokenSafeTest {
//
//    @Test
//    public void testRefreshTokenRequestedOnce() throws MalformedURLException {
//        // given
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
//        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();
//
//        OAuthToken token = new OAuthToken();
//        token.setAccessToken("accessTokenMock");
//        token.setRefreshToken("refreshTokenMock");
//        token.setExpiresIn("" + (80 * 1000));
//        token.setRefreshExpiresIn("" + (160 * 1000));
//        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);
//
//        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class))).thenReturn(exchange);
//
//        OAuthTokenSafe sut = new OAuthTokenSafeImpl(appConfig, clientConfig);
//
//        // when
//        sut.getToken();
//        sut.getToken();
//
//        // then
//        Mockito.verify(restTemplate).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class));
//    }
//
//    @Test
//    public void testRefreshExpiredToken() throws MalformedURLException {
//        // given
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
//        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();
//
//        OAuthToken token = new OAuthToken();
//        token.setAccessToken("accessTokenMock");
//        token.setRefreshToken("refreshTokenMock");
//        token.setExpiresIn("" + (-80 * 1000));
//        token.setRefreshExpiresIn("" + (-40 * 1000));
//        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);
//
//        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class))).thenReturn(exchange);
//
//        OAuthTokenSafe sut = new OAuthTokenSafeImpl(appConfig, clientConfig);
//
//        // when
//        sut.getToken();
//        sut.getToken();
//
//        // then
//        Mockito.verify(restTemplate, Mockito.times(2)).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class));
//    }
//
//    @Test
//    public void testRefreshExpiredTokenValidRefreshToken() throws MalformedURLException {
//        // given
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
//        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();
//
//        OAuthToken token = new OAuthToken();
//        token.setAccessToken("accessTokenMock");
//        token.setRefreshToken("refreshTokenMock");
//        token.setExpiresIn("" + (-80 * 1000));
//        token.setRefreshExpiresIn("" + (80 * 1000));
//        ResponseEntity<OAuthToken> exchange = new ResponseEntity<>(token, HttpStatus.OK);
//
//        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(OAuthToken.class))).thenReturn(exchange);
//
//        OAuthTokenSafe sut = new OAuthTokenSafeImpl(appConfig, clientConfig);
//
//        // when 1
//        String withoutTokenRequestEntity = ((OAuthTokenSafeImpl) sut).createTokenRequest();
//        sut.getToken();
//
//        // then 1
//        Assert.assertTrue(withoutTokenRequestEntity.contains("client_credentials"));
//        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.eq(withoutTokenRequestEntity), ArgumentMatchers.eq(OAuthToken.class));
//
//        // when 2
//        String withInvalidTokenRequestEntity = ((OAuthTokenSafeImpl) sut).createTokenRequest();
//        sut.getToken();
//
//        // then 2
//        Assert.assertTrue(withInvalidTokenRequestEntity.contains("refresh_token"));
//        Assert.assertTrue(withInvalidTokenRequestEntity.contains("refreshTokenMock"));
//        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.eq(withInvalidTokenRequestEntity), ArgumentMatchers.eq(OAuthToken.class));
//    }
}