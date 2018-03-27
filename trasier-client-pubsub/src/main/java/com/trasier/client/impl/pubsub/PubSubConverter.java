package com.trasier.client.impl.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.model.Span;
import org.iq80.snappy.Snappy;

import java.io.IOException;

class PubSubConverter {

    private final ObjectMapper mapper;

    PubSubConverter() {
        this.mapper = new ObjectMapper();
    }

    byte[] compress(Span span) throws IOException {
        return Snappy.compress(mapper.writer().writeValueAsBytes(span));
    }

    byte[] getByteData(Span span) throws IOException {
        return mapper.writer().writeValueAsBytes(span);
    }
}
