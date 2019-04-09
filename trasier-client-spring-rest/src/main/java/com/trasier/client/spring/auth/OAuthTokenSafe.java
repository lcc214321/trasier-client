package com.trasier.client.spring.auth;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Component
public class OAuthTokenSafe {
    private static final int EXPIRES_IN_TOLERANCE = 60;

    private final TrasierEndpointConfiguration appConfig;
    private final TrasierClientConfiguration springConfig;
    private final RestTemplate restTemplate;

    private OAuthToken token;
    private long tokenExpiresAt;
    private long refreshTokenExpiresAt;

    @Autowired
    public OAuthTokenSafe(TrasierEndpointConfiguration appConfig, TrasierClientConfiguration springConfig) {
        this(appConfig, springConfig, new RestTemplate());
    }

    OAuthTokenSafe(TrasierEndpointConfiguration appConfig, TrasierClientConfiguration springConfig, RestTemplate restTemplate) {
        this.appConfig = appConfig;
        this.springConfig = springConfig;
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public String getAuthHeader() {
        if (isTokenInvalid()) {
            refreshToken();
        }
        return token.getAccessToken();
    }

    private synchronized void refreshToken() {
        if (isTokenInvalid()) {
            long tokenIssued = System.currentTimeMillis();
            HttpEntity<MultiValueMap<String, String>> requestEntity = createTokenRequestEntity();
            ResponseEntity<OAuthToken> exchange = restTemplate.postForEntity(appConfig.getAuthEndpoint(), requestEntity, OAuthToken.class);
            this.token = exchange.getBody();

            this.tokenExpiresAt = tokenIssued + ((Long.parseLong(token.getExpiresIn()) - EXPIRES_IN_TOLERANCE) * 1000);
            this.refreshTokenExpiresAt = tokenIssued + ((Long.parseLong(token.getRefreshExpiresIn()) - EXPIRES_IN_TOLERANCE) * 1000);
        }
    }

    HttpEntity<MultiValueMap<String, String>> createTokenRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String basicAuth = Base64.getEncoder().encodeToString((springConfig.getClientId() + ":" + springConfig.getClientSecret()).getBytes());
        headers.add("Authorization", "Basic " + basicAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("scope", "");
        map.add("client_id", springConfig.getClientId());

        if (isRefreshTokenInvalid()) {
            map.add("grant_type", "client_credentials");
        } else {
            map.add("grant_type", "refresh_token");
            map.add("refresh_token", token.getRefreshToken());
        }

        return new HttpEntity<>(map, headers);
    }

    private boolean isTokenInvalid() {
        return token == null || tokenExpiresAt < System.currentTimeMillis();
    }

    private boolean isRefreshTokenInvalid() {
        return token == null || token.getRefreshToken() == null || refreshTokenExpiresAt < System.currentTimeMillis();
    }
}