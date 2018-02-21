package com.trasier.client.configuration;

public class ClientPropertyConfiguration implements ClientConfiguration {

    private String clientId;
    private String secret;

    public ClientPropertyConfiguration() {
        this.clientId = System.getProperty("trasier.client.id");
        this.secret = System.getProperty("trasier.client.secret");
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
