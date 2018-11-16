package com.trasier.client.api;

public interface TrasierConstants {
    String HEADER_CONVERSATION_SAMPLE = "X-Conversation-Sample";
    String HEADER_CONVERSATION_ID = "X-Conversation-Id";
    String HEADER_TRACE_ID = "X-Trace-Id";
    String HEADER_SPAN_ID = "X-Span-Id";
    String HEADER_INCOMING_ENDPOINT_NAME = "X-Incoming-Endpoint-Name";

    String UNKNOWN_IN = "UNKNOWN_IN";
    String UNKNOWN_OUT = "UNKNOWN_OUT";

    String STATE_OK = "OK";
    String STATE_ERROR = "ERROR";
}