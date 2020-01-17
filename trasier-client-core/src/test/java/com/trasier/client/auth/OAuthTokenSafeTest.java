package com.trasier.client.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OAuthTokenSafeTest {

    private TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
    private TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();
    private ObjectMapper mapper = new ObjectMapper();
    private Response response = Mockito.mock(Response.class);
    private AsyncHttpClient client = Mockito.mock(AsyncHttpClient.class);

    @Before
    public void init() throws InterruptedException, ExecutionException, TimeoutException {
        ListenableFuture<Response> future = Mockito.mock(ListenableFuture.class);
        Request request = Mockito.mock(Request.class);
        BoundRequestBuilder requestBuilder = Mockito.mock(BoundRequestBuilder.class);
        Mockito.when(future.get(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(response);
        Mockito.when(client.preparePost(ArgumentMatchers.anyString())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setReadTimeout(ArgumentMatchers.anyInt())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setRequestTimeout(ArgumentMatchers.anyInt())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setCharset(ArgumentMatchers.any())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setHeader(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setBody(ArgumentMatchers.anyString())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.build()).thenReturn(request);
        Mockito.when(client.executeRequest(ArgumentMatchers.any(Request.class))).thenReturn(future);
    }

    @Test
    public void testRefreshTokenRequestedOnce() throws JsonProcessingException {
        // given
        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (80 * 1000));
        token.setRefreshExpiresIn("" + (160 * 1000));

        Mockito.when(response.getStatusCode()).thenReturn(200);
        Mockito.when(response.getResponseBody()).thenReturn(mapper.writeValueAsString(token));
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when
        sut.getToken();
        sut.getToken();

        // then
        Mockito.verify(client, Mockito.times(1)).executeRequest(ArgumentMatchers.any(Request.class));
    }

    @Test
    public void testRefreshExpiredToken() throws JsonProcessingException {
        // given
        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (-40 * 1000));

        Mockito.when(response.getStatusCode()).thenReturn(200);
        Mockito.when(response.getResponseBody()).thenReturn(mapper.writeValueAsString(token));
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when
        sut.getToken();
        sut.getToken();

        // then
        Mockito.verify(client, Mockito.times(2)).executeRequest(ArgumentMatchers.any(Request.class));
    }

    @Test
    public void testRefreshExpiredTokenValidRefreshToken() throws JsonProcessingException {
        // given
        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (80 * 1000));

        Mockito.when(response.getStatusCode()).thenReturn(200);
        Mockito.when(response.getResponseBody()).thenReturn(mapper.writeValueAsString(token));
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when 1
        String withoutTokenRequestEntity = sut.createTokenRequest();
        sut.getToken();

        // then 1
        Assert.assertTrue(withoutTokenRequestEntity.contains("client_credentials"));
        Mockito.verify(client, Mockito.times(1)).executeRequest(ArgumentMatchers.any(Request.class));

        // when 2
        String withInvalidTokenRequestEntity = sut.createTokenRequest();
        sut.getToken();

        // then 2
        Assert.assertTrue(withInvalidTokenRequestEntity.contains("refresh_token"));
        Assert.assertTrue(withInvalidTokenRequestEntity.contains("refreshTokenMock"));
        Mockito.verify(client, Mockito.times(2)).executeRequest(ArgumentMatchers.any(Request.class));
    }
}