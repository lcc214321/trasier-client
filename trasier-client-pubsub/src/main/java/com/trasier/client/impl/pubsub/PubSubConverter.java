package com.trasier.client.impl.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.model.Event;
import org.iq80.snappy.Snappy;

import java.io.IOException;

class PubSubConverter {

    private final ObjectMapper mapper;

    PubSubConverter() {
        this.mapper = new ObjectMapper();
    }

    byte[] compressData(Event event) throws IOException {
        return Snappy.compress(mapper.writer().writeValueAsBytes(event));
    }

    byte[] getByteData(Event event) throws IOException {
        return mapper.writer().writeValueAsBytes(event);
    }
}
