package com.trasier.client.impl.spring5.auth;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private long tokenIssued;
    private long tokenExpiresAt;

    @Autowired
    public OAuthTokenSafe(TrasierEndpointConfiguration appConfig, TrasierClientConfiguration springConfig, @Qualifier("trasierRestTemplate") RestTemplate restTemplate) {
        this.appConfig = appConfig;
        this.springConfig = springConfig;
        this.restTemplate = restTemplate;
    }

    public String getAuthHeader() {
        if (!isTokenValid()) {
            refreshToken();
        }
        return token.getAccessToken();
    }

    private boolean isTokenValid() {
        return token != null && tokenExpiresAt > System.currentTimeMillis();
    }

    private synchronized void refreshToken() {
        if(!isTokenValid()) {
            //TODO Hackergarten? -> Use refresh_token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String basicAuth = Base64.getEncoder().encodeToString((springConfig.getClientId() + ":" + springConfig.getClientSecret()).getBytes());
            headers.add("Authorization", "Basic " + basicAuth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("scope", "");
            map.add("client_id", springConfig.getClientId());

            this.tokenIssued = System.currentTimeMillis();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);
            ResponseEntity<OAuthToken> exchange = restTemplate.postForEntity(appConfig.getAuthEndpoint(), requestEntity, OAuthToken.class);
            this.token = exchange.getBody();

            this.tokenExpiresAt = tokenIssued + ((Long.parseLong(token.getExpiresIn()) - EXPIRES_IN_TOLERANCE) * 1000);
        }
    }
}