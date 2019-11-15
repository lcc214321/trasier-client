package com.trasier.client.spring.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

@Component
public class OAuthTokenSafeImpl implements OAuthTokenSafe {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenSafeImpl.class);
    private static final int EXPIRES_IN_TOLERANCE = 60;

    private final TrasierClientConfiguration clientConfig;

    private final ObjectMapper mapper;
    private final URL url;
    private final TrustManager[] trustAllCerts;

    private OAuthToken token;
    private long tokenExpiresAt;
    private long refreshTokenExpiresAt;

    @Autowired
    public OAuthTokenSafeImpl(TrasierEndpointConfiguration endpointConfig, TrasierClientConfiguration clientConfig) throws MalformedURLException {
        this.clientConfig = clientConfig;
        this.mapper = new ObjectMapper();
        this.url = new URL(endpointConfig.getAuthEndpoint());
        this.trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
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
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private OAuthToken fetchToken() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            String basicAuth = Base64.getEncoder().encodeToString((clientConfig.getClientId() + ":" + clientConfig.getClientSecret()).getBytes());
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setInstanceFollowRedirects(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("scope=");
            out.write("&client_id=" + clientConfig.getClientId());

            if (isRefreshTokenInvalid()) {
                out.write("&grant_type=client_credentials");
            } else {
                out.write("&grant_type=refresh_token");
                out.write("&refresh_token=" + token.getRefreshToken());
            }
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return mapper.readValue(connection.getInputStream(), OAuthToken.class);
            } else {
                LOGGER.error("Could not fetch token. " + responseCode);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            connection.disconnect();
        }
        return null;
    }

    private boolean isTokenInvalid() {
        return token == null || tokenExpiresAt < System.currentTimeMillis();
    }

    private boolean isRefreshTokenInvalid() {
        return token == null || token.getRefreshToken() == null || refreshTokenExpiresAt < System.currentTimeMillis();
    }

}