package com.trasier.client.configuration;

public class TrasierApplicationConfiguration {
    private static final String DEFAULT_WRITE_ENDPOINT = "https://trasier-writer-dev.app.trasier.com/api/accounts/{accountId}/space/{spaceKey}/spans";
    private static final String DEFAULT_AUTH_ENDPOINT = "https://trasier-keycloak-test.app.trasier.com/auth/realms/trasier-dev/protocol/openid-connect/token";
    private static final String TRASIER_AUTH_ENDPOINT_PROPERTY = "trasier.app.auth.endpoint";
    private static final String TRASIER_WRITE_ENDPOINT_PROPERTY = "trasier.app.writer.endpoint";

    private String writerEndpoint;
    private String authEndpoint;

    public TrasierApplicationConfiguration() {
        String propertyWriterEndpoint = System.getProperty(TRASIER_WRITE_ENDPOINT_PROPERTY);
        String propertyAuthEndpoint = System.getProperty(TRASIER_AUTH_ENDPOINT_PROPERTY);
        this.writerEndpoint = propertyWriterEndpoint != null ? propertyWriterEndpoint : DEFAULT_WRITE_ENDPOINT;
        this.authEndpoint = propertyAuthEndpoint != null ? propertyAuthEndpoint : DEFAULT_AUTH_ENDPOINT;
    }

    public String getWriterEndpoint() {
        return this.writerEndpoint;
    }

    public String getAuthEndpoint() {
        return this.authEndpoint;
    }

}