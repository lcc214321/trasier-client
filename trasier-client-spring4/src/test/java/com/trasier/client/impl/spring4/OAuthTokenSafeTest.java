package com.trasier.client.impl.spring4;

import com.trasier.client.configuration.TrasierApplicationConfiguration;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

public class OAuthTokenSafeTest {

    @Test
    public void testRefreshTokenRequestedOnce() {
        // given
        RestTemplate restTemplate = mock(RestTemplate.class);
        TrasierSpringClientConfiguration clientConfig = new TrasierSpringClientConfiguration();
        TrasierApplicationConfiguration appConfig = new TrasierApplicationConfiguration();
        ResponseEntity exchange = mock(ResponseEntity.class);

        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setExpiresIn("" + (System.currentTimeMillis() + (80 * 1000)));
        when(exchange.getBody()).thenReturn(token);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class))).thenReturn(exchange);

        OAuthTokenSafe sut = new OAuthTokenSafe(appConfig, clientConfig, restTemplate);

        // when
        sut.getAuthHeader();
        sut.getAuthHeader();

        // then
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(OAuthToken.class));

    }
}