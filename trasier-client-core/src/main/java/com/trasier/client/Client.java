package com.trasier.client;

import com.trasier.client.model.Event;

import java.util.List;

public interface Client {

    boolean sendEvent(Event event);

    boolean sendEvents(List<Event> events);

}