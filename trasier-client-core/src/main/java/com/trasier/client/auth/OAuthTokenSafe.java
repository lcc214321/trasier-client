package com.trasier.client.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.configuration.TrasierClientConfiguration;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OAuthTokenSafe {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenSafe.class);
    private static final int EXPIRES_IN_TOLERANCE = 60;

    private final TrasierClientConfiguration clientConfiguration;
    private final String authUrl;
    private final AsyncHttpClient client;
    private final ObjectMapper mapper;

    private OAuthToken token;
    private long tokenExpiresAt;
    private long refreshTokenExpiresAt;

    public OAuthTokenSafe(TrasierClientConfiguration clientConfiguration, String authUrl, AsyncHttpClient client) {
        this.clientConfiguration = clientConfiguration;
        this.authUrl = authUrl;
        this.client = client;
        this.mapper = new ObjectMapper();
    }

    public String getToken() {
        if (isTokenInvalid()) {
            refreshToken();
        }
        return token.getAccessToken();
    }

    private synchronized void refreshToken() {
        if (isTokenInvalid()) {
            long tokenIssued = System.currentTimeMillis();
            try {
                OAuthToken newToken = fetchToken();
                if (newToken != null) {
                    this.token = newToken;
                    this.tokenExpiresAt = tokenIssued + ((Long.parseLong(token.getExpiresIn()) - EXPIRES_IN_TOLERANCE) * 1000);
                    this.refreshTokenExpiresAt = tokenIssued + ((Long.parseLong(token.getRefreshExpiresIn()) - EXPIRES_IN_TOLERANCE) * 1000);
                }
            } catch (Exception e) {
                LOGGER.error("Could not fetch token, maybe you need to set a proxy or consider disabling trasier.", e);
            }
        }
    }

    private OAuthToken fetchToken() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        String basicAuth = Base64.getEncoder().encodeToString((clientConfiguration.getClientId() + ":" + clientConfiguration.getClientSecret()).getBytes());
        String tokenRequest = createTokenRequest();
        Request request = createRequest(basicAuth, tokenRequest);
        ListenableFuture<Response> future = client.executeRequest(request);
        Response response = future.get(5, TimeUnit.SECONDS);

        int responseCode = response.getStatusCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return mapper.readValue(response.getResponseBody(), OAuthToken.class);
        } else {
            throw new IOException("Could not fetch token. " + responseCode);
        }
    }

    Request createRequest(String basicAuth, String tokenRequest) {
        BoundRequestBuilder requestBuilder = client
                .preparePost(authUrl)
                .setReadTimeout(5000)
                .setRequestTimeout(5000)
                .setCharset(StandardCharsets.UTF_8)
                .setHeader("Authorization", "Basic " + basicAuth)
                //.setFormParams(formParams) // TODO Use formParams instead of concatenated body
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setBody(tokenRequest);
        return requestBuilder.build();
    }

    String createTokenRequest() {
        String tokenRequest = "scope=&client_id=" + clientConfiguration.getClientId();

        if (isRefreshTokenInvalid()) {
            tokenRequest += "&grant_type=client_credentials";
        } else {
            tokenRequest += "&grant_type=refresh_token";
            tokenRequest += "&refresh_token=" + token.getRefreshToken();
        }

        return tokenRequest;
    }

    private boolean isTokenInvalid() {
        return token == null || tokenExpiresAt < System.currentTimeMillis();
    }

    private boolean isRefreshTokenInvalid() {
        return token == null || token.getRefreshToken() == null || refreshTokenExpiresAt < System.currentTimeMillis();
    }

}