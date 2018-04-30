package com.trasier.client.model;

public class Endpoint {
    private String name;
    private String ipAddress;
    private String port;
    private String hostname;

    public Endpoint(String name) {
        this.name = name;
    }

    public Endpoint(String name, String ipAddress, String port, String hostname) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        this.hostname = hostname;
    }

    public String getName() {
        return name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port='" + port + '\'' +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}
