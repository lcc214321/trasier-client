package com.trasier.client.impl.pubsub;

import com.trasier.client.model.Application;
import com.trasier.client.model.Event;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class PubSubConverterTest {
    @Test
    public void convert() throws IOException {
        PubSubConverter sut = new PubSubConverter();
        Event.Builder builder = Event.newRequestEvent(UUID.randomUUID(), new Application("bla"), "noop");
        builder.correlationId(UUID.randomUUID());
        builder.data("aaaaaaaaaaaaaaabbbcccddddddaaa");
        Event event = builder.build();

        byte[] compressed = sut.compressData(event);
        byte[] uncompressed = sut.getByteData(event);

        System.out.println(uncompressed.length);
        System.out.println(new String(uncompressed));
        System.out.println(compressed.length);
        System.out.println(new String(compressed));

        assertTrue(uncompressed.length > compressed.length);
    }
}