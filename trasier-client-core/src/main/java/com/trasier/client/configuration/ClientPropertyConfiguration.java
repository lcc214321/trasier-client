package com.trasier.client.configuration;

public class ClientPropertyConfiguration implements ClientConfiguration {
    private final String accountId;
    private final String spaceKey;
    private final String clientId;
    private final String clientSecret;

    public ClientPropertyConfiguration() {
        this(System.getProperty("trasier.client.accountId"), System.getProperty("trasier.client.spaceKey"),
                System.getProperty("trasier.client.clientId"), System.getProperty("trasier.client.clientSecret"));
    }

    public ClientPropertyConfiguration(String accountId, String spaceKey, String clientId, String clientSecret) {
        this.accountId = accountId;
        this.spaceKey = spaceKey;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    @Override
    public String getSpaceKey() {
        return spaceKey;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }
}