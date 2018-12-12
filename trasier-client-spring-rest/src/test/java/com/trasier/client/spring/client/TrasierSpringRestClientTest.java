package com.trasier.client.spring.client;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.spring.auth.OAuthTokenSafe;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class TrasierSpringRestClientTest {

    private RestTemplate teamplate;
    private TrasierSpringRestClient sut;
    private TrasierClientConfiguration clientConfig;

    @Before
    public void setup() {
        TrasierEndpointConfiguration config = new TrasierEndpointConfiguration();
        clientConfig = new TrasierClientConfiguration();
        clientConfig.setActivated(true);
        clientConfig.setClientId("clientId");
        clientConfig.setSpaceKey("my-space");
        clientConfig.setSystemName("ping");
        clientConfig.setClientSecret("abcd1234");
        teamplate = mock(RestTemplate.class);
        OAuthTokenSafe tokenSafe = mock(OAuthTokenSafe.class);
        sut = new TrasierSpringRestClient(config, clientConfig, teamplate, tokenSafe);
    }

    @Test
    public void testShouldNotSendSpansWhenDeactivated() {
        // given
        clientConfig.setActivated(false);
        Span span = Span.newSpan("", "", "", "").build();

        // when
        sut.sendSpan(span);

        // then
        verify(teamplate, times(0)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void testShouldSendAndEnrichSpan() {
        // given
        clientConfig.setActivated(true);
        Span span = Span.newSpan("", "", "", "").build();

        // when
        sut.sendSpan(span);

        // then
        assertNotNull(span.getTags().get("trasier_client.-"));
        verify(teamplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void testShouldSendAndEnrichSpansOnServerSide() {
        // given
        clientConfig.setActivated(true);
        Span span = Span.newSpan("", "", "", "").build();
        span.setTags(new HashMap<>());
        span.getTags().put("span.kind", "server");

        // when
        sut.sendSpan(span);

        // then
        assertNotNull(span.getTags().get("trasier_client.server"));
        verify(teamplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void doNothingOnEmptyCollection() {
        // given
        clientConfig.setActivated(true);

        // when
        sut.sendSpans(Collections.emptyList());

        // then
        verify(teamplate, times(0)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }


}