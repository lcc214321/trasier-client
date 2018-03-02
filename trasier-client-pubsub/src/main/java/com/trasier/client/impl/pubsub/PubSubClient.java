package com.trasier.client.impl.pubsub;

import com.google.common.annotations.VisibleForTesting;
import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.Client;
import com.trasier.client.model.Event;
import com.trasier.client.utils.Precondition;

import java.util.List;

public class PubSubClient implements Client {

    private final PubSubSender sender;

    @Override
    public boolean sendEvent(Event event) {
        try {
            Message message = sender.sendEvent(event);
            return message != null;
        } catch (Exception e) {
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

    private PubSubClient(PubSubClient.Builder builder) {
        String clientId = builder.clientId;
        String topic = builder.topic;
        String project = builder.project;
        Integer concurrency = builder.concurrency != null ? builder.concurrency : 128;

        Precondition.notNull(project, "project");
        Precondition.notNull(topic, "topic");
        Precondition.notNull(clientId, "clientId");

        Publisher publisher = builder.publisher == null ? createPublisher(project, concurrency) : builder.publisher;
        this.sender = new PubSubSender(builder.topic, builder.clientId, publisher);
    }

    private Publisher createPublisher(String project, Integer concurrency) {
        Integer concurrencyValue = concurrency == null ? 128 : concurrency;
        Pubsub pubsub = Pubsub.builder().build();
        return Publisher.builder().pubsub(pubsub).project(project).concurrency(concurrencyValue).build();
    }

    public static PubSubClient.Builder builder() {
        return new PubSubClient.Builder();
    }

    public static class Builder {
        private String project;
        private String topic;
        private String clientId;
        private Integer concurrency;
        private Publisher publisher;

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

        public PubSubClient.Builder concurrency(Integer concurrency) {
            this.concurrency = concurrency;
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