package com.trasier.client.configuration;

public class TrasierClientConfiguration {
    private String accountId;
    private String spaceKey;
    private String clientId;
    private String clientSecret;
    private String systemName;

    private boolean activated = true;
    private boolean payloadTracingDisabled = false;
    private long logMetricsInterval = 10 * 60 * 1000;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setPayloadTracingDisabled(boolean payloadTracingDisabled) {
        this.payloadTracingDisabled = payloadTracingDisabled;
    }

    public boolean isPayloadTracingDisabled() {
        return payloadTracingDisabled;
    }

    public long getLogMetricsInterval() {
        return logMetricsInterval;
    }

    public void setLogMetricsInterval(final long logMetricsInterval) {
        this.logMetricsInterval = logMetricsInterval;
    }

}