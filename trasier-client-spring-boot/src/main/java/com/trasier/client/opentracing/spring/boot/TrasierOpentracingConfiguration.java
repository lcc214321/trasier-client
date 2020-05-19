package com.trasier.client.opentracing.spring.boot;

import com.trasier.client.api.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.configuration.TrasierProxyConfiguration;
import com.trasier.client.opentracing.TrasierScopeManager;
import com.trasier.client.opentracing.TrasierTracer;
import com.trasier.client.spring.spancontrol.TrasierSpanFilterConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(name = "io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration")
public class TrasierOpentracingConfiguration {

    @Bean
    @ConfigurationProperties("trasier.client")
    public TrasierClientConfiguration trasierSpringClientConfiguration() {
        return new TrasierClientConfiguration();
    }

    @Bean
    @ConfigurationProperties("trasier.endpoint")
    protected TrasierEndpointConfiguration endpointConfig() {
        return new TrasierEndpointConfiguration();
    }

    @Bean
    @ConfigurationProperties("trasier.proxy")
    public TrasierProxyConfiguration trasierSpringProxyConfiguration() {
        return new TrasierProxyConfiguration();
    }

    @Bean
    public TrasierTracer trasierTracer(Client client, TrasierClientConfiguration configuration) {
        return new TrasierTracer(client, configuration, new TrasierScopeManager());
    }

    @Bean
    @ConfigurationProperties(prefix = "trasier.client.span")
    public TrasierSpanFilterConfiguration spancontrolConfiguration() {
        return new TrasierSpanFilterConfiguration();
    }

}

// trasier.client.interceptor.sampling.operation.whitelist=angebote
// trasier.client.interceptor.sampling.url.skipPattern=.*netz.*

/*

trasier:
    client:
        interceptor:
            sampling:
                operation:
                    whitelist:
                    - angebote
                    - vorabbuchungen
                    blacklist:
                    - zahlungPruefen
                url:
                    skipUrl: .*netz.*|*.zahlung.*

 */

// trasier.client.span.skipUrl=.*netz.*
// trasier.client.span.skipOperation=ermittleAngebote

/*
trasier:
    client:
        span:
            filters:
            -
               strategy: cancel
               url: .*netz.*|*.zahlung.*
               operation: *.zahlungPruefen.*
            -
               strategy: allow
               url: .*angebote.*
               operation: *.vorabbuchungen.*
            -
               strategy: disablePayload
               url: .*netz.*|*.zahlung.*
               operation: *.zahlungPruefen.*
 */

// trasier.client.span.disablePayloadByUrl=payment.*
// trasier.client.span.disablePayloadByOperation=zahlungPruefen

