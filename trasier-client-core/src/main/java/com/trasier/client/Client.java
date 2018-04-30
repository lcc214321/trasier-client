package com.trasier.client;

import com.trasier.client.model.Span;

import java.util.List;

public interface Client {

    boolean sendSpan(Span span);

    boolean sendSpans(List<Span> spans);

    void close();

}