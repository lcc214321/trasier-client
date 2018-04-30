package com.trasier.client.impl.pubsub;

import com.google.common.annotations.VisibleForTesting;
import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.Client;
import com.trasier.client.model.Span;
import com.trasier.client.utils.Precondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PubSubClient implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubClient.class);

    private final PubSubSender sender;

    @Override
    public boolean sendSpan(Span span) {
        try {
            Message message = sender.sendSpan(span);
            return message != null;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        boolean result = true;
        for (Span span : spans) {
            result &= this.sendSpan(span);
        }
        return result;
    }

    @Override
    public void close() {
        sender.close();
    }

    private PubSubClient(PubSubClient.Builder builder) {
        Precondition.notBlank(builder.appId, "appId");

        String project = "trasier-192322";
        if(builder.project != null) {
            project = builder.project;
        }

        String topic = "trasier-prod";
        if(builder.topic != null) {
            topic = builder.topic;
        }

        if (builder.publisher != null && builder.pubsub != null) {
            this.sender = new PubSubSender(topic.trim(), builder.appId.trim(), builder.pubsub, builder.publisher);
        } else {
            Precondition.notBlank(builder.serviceAccountToken, "serviceAccountToken");
            this.sender = new PubSubSender(project.trim(), topic.trim(), builder.appId.trim(), builder.serviceAccountToken.trim());
        }
    }

    public static PubSubClient.Builder builder() {
        return new PubSubClient.Builder();
    }

    public static class Builder {
        private String project;
        private String topic;
        private String appId;
        private String serviceAccountToken;
        private Publisher publisher;
        private Pubsub pubsub;

        public PubSubClient.Builder serviceAccountToken(String serviceAccountToken) {
            this.serviceAccountToken = serviceAccountToken;
            return this;
        }

        public PubSubClient.Builder project(String project) {
            this.project = project;
            return this;
        }

        public PubSubClient.Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public PubSubClient.Builder appId(String appId) {
            this.appId = appId;
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