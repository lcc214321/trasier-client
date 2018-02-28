package com.trasier.client.impl.pubsub;

import com.trasier.client.model.Event;
import com.trasier.client.model.System;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class PubSubConverterTest {
    @Test
    public void convert() throws IOException {
        PubSubConverter sut = new PubSubConverter();
        Event.Builder builder = Event.newRequestEvent(UUID.randomUUID(), new System("bla"), "noop");
        builder.correlationId(UUID.randomUUID());
        builder.data("aaaaaaaaaaaaaaabbbcccddddddaaa");
        Event event = builder.build();

        byte[] compressed = sut.compressData(event);
        byte[] uncompressed = sut.getByteData(event);

        java.lang.System.out.println(uncompressed.length);
        java.lang.System.out.println(new String(uncompressed));
        java.lang.System.out.println(compressed.length);
        java.lang.System.out.println(new String(compressed));

        assertTrue(uncompressed.length > compressed.length);
    }
}