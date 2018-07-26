package com.trasier.client.configuration;

public class TrasierEndpointConfiguration {
    private static final String TRASIER_AUTH_ENDPOINT_PROPERTY = "trasier.endpoint.authEndpoint";
    private static final String TRASIER_WRITE_ENDPOINT_PROPERTY = "trasier.endpoint.writerEndpoint";
    private static final String TRASIER_READ_ENDPOINT_PROPERTY = "trasier.endpoint.readerEndpoint";
    private static final String DEFAULT_WRITE_ENDPOINT = "https://writer.trasier.com/api/accounts/{accountId}/spaces/{spaceKey}/spans";
    private static final String DEFAULT_READ_ENDPOINT = "https://reader.trasier.com/api/accounts/{accountId}/spaces/{spaceKey}/conversations/{conversationId}";
    private static final String DEFAULT_AUTH_ENDPOINT = "https://auth.trasier.com/auth/realms/trasier-prod/protocol/openid-connect/token";

    private String writerEndpoint = System.getProperty(TRASIER_WRITE_ENDPOINT_PROPERTY, DEFAULT_WRITE_ENDPOINT);
    private String readerEndpoint = System.getProperty(TRASIER_READ_ENDPOINT_PROPERTY, DEFAULT_READ_ENDPOINT);
    private String authEndpoint = System.getProperty(TRASIER_AUTH_ENDPOINT_PROPERTY, DEFAULT_AUTH_ENDPOINT);

    public String getWriterEndpoint() {
        return writerEndpoint;
    }

    public void setWriterEndpoint(String writerEndpoint) {
        this.writerEndpoint = writerEndpoint;
    }

    public String getReaderEndpoint() {
        return readerEndpoint;
    }

    public void setReaderEndpoint(String readerEndpoint) {
        this.readerEndpoint = readerEndpoint;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }
}