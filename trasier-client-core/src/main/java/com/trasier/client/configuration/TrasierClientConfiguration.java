package com.trasier.client.configuration;

public class TrasierClientConfiguration implements ClientConfiguration {
    private String accountId;
    private String spaceKey;
    private String clientId;
    private String clientSecret;
    private String systemName;
    private boolean activated = true;
    private boolean payloadTracingDisabled;

    @Override
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setPayloadTracingDisabled(boolean payloadTracingDisabled) {
        this.payloadTracingDisabled = payloadTracingDisabled;
    }

    @Override
    public boolean isPayloadTracingDisabled() {
        return payloadTracingDisabled;
    }

}