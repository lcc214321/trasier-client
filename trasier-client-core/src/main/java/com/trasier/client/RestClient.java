package com.trasier.client;

import com.trasier.client.model.Event;

import java.util.List;

public interface RestClient {
    boolean sendEventsUsingPut(List<Event> event);
}