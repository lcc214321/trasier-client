package com.trasier.client.impl.spring;

import com.trasier.client.configuration.ApplicationConfiguration;
import com.trasier.client.model.Event;
import com.trasier.client.model.System;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SpringRestClientTest {

    @Test
    public void testSend() {
        // given
        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class))).thenReturn(responseEntity);

        SpringRestClient sut = new SpringRestClient(new ApplicationConfiguration(), new SpringClientConfiguration(), restTemplate);

        // when
        Event event = Event.newRequestEvent(UUID.randomUUID(), new System("Test1"), "TEST")
                .consumer(new System("Test2")).correlationId(UUID.randomUUID()).build();

        boolean result = sut.sendEventsUsingPut(Collections.singletonList(event));

        // then
        assertTrue(result);
        verify(restTemplate);
    }

}