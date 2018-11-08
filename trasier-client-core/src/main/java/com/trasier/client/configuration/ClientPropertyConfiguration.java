package com.trasier.client.configuration;

public class ClientPropertyConfiguration implements ClientConfiguration {
    private final String accountId;
    private final String spaceKey;
    private final String clientId;
    private final String clientSecret;
    private final String systemName;
    private final boolean activated;

    public ClientPropertyConfiguration() {
        this(System.getProperty("trasier.client.accountId"), System.getProperty("trasier.client.spaceKey"),
                System.getProperty("trasier.client.clientId"), System.getProperty("trasier.client.clientSecret"),
                System.getProperty("trasier.client.systemName"), Boolean.valueOf(System.getProperty("trasier.client.isDeactivated", "true)")));
    }

    public ClientPropertyConfiguration(String accountId, String spaceKey, String clientId, String clientSecret, String systemName, boolean activated) {
        this.accountId = accountId;
        this.spaceKey = spaceKey;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.systemName = systemName;
        this.activated = activated;
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

    @Override
    public String getSystemName() {
        return systemName;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

}