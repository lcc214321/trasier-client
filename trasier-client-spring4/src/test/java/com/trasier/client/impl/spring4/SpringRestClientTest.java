package com.trasier.client.impl.spring4;

import com.trasier.client.configuration.ApplicationConfiguration;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpringRestClientTest {

    @Test
    public void testSend() {
        // given
        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class))).thenReturn(responseEntity);

        SpringRestClient sut = new SpringRestClient(new ApplicationConfiguration(), new SpringClientConfiguration(), restTemplate);

        // when
        Span span = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("Test1"), "TEST")
                .outgoingEndpoint(new Endpoint("Test2")).startTimestamp(1L).build();

        boolean result = sut.sendSpans(Collections.singletonList(span));

        // then
        assertTrue(result);
        verify(restTemplate);
    }

}