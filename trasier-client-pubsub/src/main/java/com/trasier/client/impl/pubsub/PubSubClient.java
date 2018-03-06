package com.trasier.client.impl.pubsub;

import com.google.common.annotations.VisibleForTesting;
import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.Client;
import com.trasier.client.model.Event;
import com.trasier.client.utils.Precondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PubSubClient implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubClient.class);

    private final PubSubSender sender;

    @Override
    public boolean sendEvent(Event event) {
        try {
            Message message = sender.sendEvent(event);
            return message != null;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendEvents(List<Event> events) {
        boolean result = true;
        for (Event event : events) {
            result &= this.sendEvent(event);
        }
        return result;
    }

    @Override
    public void close() {
        sender.close();
    }

    private PubSubClient(PubSubClient.Builder builder) {
        Precondition.notNull(builder.project, "project");
        Precondition.notNull(builder.topic, "topic");
        Precondition.notNull(builder.clientId, "clientId");
        if (builder.publisher != null && builder.pubsub != null) {
            this.sender = new PubSubSender(builder.topic, builder.clientId, builder.pubsub, builder.publisher);
        } else {
            this.sender = new PubSubSender(builder.project, builder.topic, builder.clientId);
        }
    }

    public static PubSubClient.Builder builder() {
        return new PubSubClient.Builder();
    }

    public static class Builder {
        private String project;
        private String topic;
        private String clientId;
        private Publisher publisher;
        private Pubsub pubsub;

        public PubSubClient.Builder project(String project) {
            this.project = project;
            return this;
        }

        public PubSubClient.Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public PubSubClient.Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        @VisibleForTesting
        PubSubClient.Builder pubsub(Pubsub pubsub) {
            this.pubsub = pubsub;
            return this;
        }

        @VisibleForTesting
        PubSubClient.Builder publisher(Publisher publisher) {
            this.publisher = publisher;
            return this;
        }

        public PubSubClient build() {
            return new PubSubClient(this);
        }
    }
}