package com.trasier.client.configuration;

public class TrasierEndpointConfiguration {

    private static final String TRASIER_AUTH_ENDPOINT_PROPERTY = "trasier.endpoint.authEndpoint";
    private static final String TRASIER_HTTP_ENDPOINT_PROPERTY = "trasier.endpoint.httpEndpoint";
    private static final String TRASIER_GRPC_ENDPOINT_PROPERTY = "trasier.endpoint.grpcEndpoint";

    private static final String DEFAULT_HTTP_ENDPOINT = "https://writer.trasier.com/api/accounts/{accountId}/spaces/{spaceKey}/spans";
    private static final String DEFAULT_GRPC_ENDPOINT = "grpc.trasier.com:443";
    private static final String DEFAULT_AUTH_ENDPOINT = "https://auth.trasier.com/auth/realms/trasier-prod/protocol/openid-connect/token";

    private String httpEndpoint = System.getProperty(TRASIER_HTTP_ENDPOINT_PROPERTY, DEFAULT_HTTP_ENDPOINT);
    private String grpcEndpoint = System.getProperty(TRASIER_GRPC_ENDPOINT_PROPERTY, DEFAULT_GRPC_ENDPOINT);
    private String authEndpoint = System.getProperty(TRASIER_AUTH_ENDPOINT_PROPERTY, DEFAULT_AUTH_ENDPOINT);

    public String getHttpEndpoint() {
        return httpEndpoint;
    }

    public void setHttpEndpoint(String httpEndpoint) {
        this.httpEndpoint = httpEndpoint;
    }

    public String getGrpcEndpoint() {
        return grpcEndpoint;
    }

    public void setGrpcEndpoint(String grpcEndpoint) {
        this.grpcEndpoint = grpcEndpoint;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

}