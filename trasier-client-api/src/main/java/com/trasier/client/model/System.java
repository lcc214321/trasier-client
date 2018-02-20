package com.trasier.client.model;

public class System {
    private String name;
    private String ipAddress;
    private String hostname;

    public System(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "System{" +
                "name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}