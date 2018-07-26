package com.trasier.client;

import com.trasier.client.model.ConversationInfo;
import com.trasier.client.model.Span;

import java.util.List;

public interface Client {

    boolean sendSpan(String accountId, String spaceKey, Span span);

    boolean sendSpans(String accountId, String spaceKey, List<Span> spans);

    ConversationInfo readConversation(String accountId, String spaceKey, String conversationId);

    Span readSpan(String accountId, String spaceKey, String conversationId, String traceId, String spanId);

    void close();
}