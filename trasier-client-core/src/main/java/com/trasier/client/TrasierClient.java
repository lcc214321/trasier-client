package com.trasier.client;

import com.trasier.client.model.Event;

import java.util.Collections;
import java.util.List;

public class TrasierClient {

    private final RestClient restClient;

    public TrasierClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public boolean sendEvent(Event event) {
        return this.sendEvents(Collections.singletonList(event));
    }

    public boolean sendEvents(List<Event> events) {
        return restClient.sendEventsUsingPut(events);
    }
}