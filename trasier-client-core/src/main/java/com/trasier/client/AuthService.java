package com.trasier.client;

import com.trasier.client.configuration.ApplicationConfiguration;
import com.trasier.client.configuration.ClientConfiguration;

/**
 * Created by lukasz on 05.02.18.
 */
public class AuthService {

    private final RestClient restClient;
    private ApplicationConfiguration appConfig;
    private final ClientConfiguration configuration;

    public AuthService(RestClient restClient, ApplicationConfiguration appConfig, ClientConfiguration configuration) {
        this.restClient = restClient;
        this.appConfig = appConfig;
        this.configuration = configuration;
    }

    public String getToken() {
        return null;
    }

    public String login() {
        return null;
    }
}
