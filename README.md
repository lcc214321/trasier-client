[![Build Status](https://travis-ci.org/trasiercom/trasier-client.svg?branch=develop)](https://travis-ci.org/trasiercom/trasier-client)
[![Coverage Status](https://coveralls.io/repos/github/trasiercom/trasier-client/badge.svg?branch=develop&g=3)](https://coveralls.io/github/trasiercom/trasier-client?branch=develop)


Trasier Client sends tracing data into the Trasier System.

# PubSubCLient

Trasier's PubSubClient is a high performant, non I/O blocking pub/sub client. It is based on Netty and uses `async-google-pubsub-client` under the hood.

## Configuration

Trasier PubSubClient must be configured with `project`, `clientId` and `topic` paramaters. Those parameters are provided by Trasier during the registration process.

```
PubSubClient pubSubClient = PubSubClient.builder().project(...).clientId(...).topic(...).build();
```

As for Version 0.7.6 the PubSubClient additionally needs a Google credentials configuration file. This file has to be loaded using the  `GOOGLE_APPLICATION_CREDENTIALS` environment variable:

```
GOOGLE_APPLICATION_CREDENTIALS=/path-to-config-file/gcp-trasier-client-prod.json
```

The json file will also be provided by Trasier.

A more conveniant way to configure credentials is planned to be supported in the future.

## Usage

### Creating an Event

An event can be either of type `REQUEST`, `RESPONSE` or `NOTICE`.

The Event class has apropriate Builder methods:

```
Event.Builder requestEvent = Event.newRequestEvent(conversationId, producer, operation);

Event.Builder responseEvent = newResponseEvent(Builder requestEvent);

Event.Builder noticeEvent = newEvent(UUID conversationId, Application producer, String operation);
```

The Request and Response Event is used to trace messages exchanged by two Applications (the producer and the consumer application).

The Notice event can be used to trace an internal state of an application (for example complex filtering algorithm).

Parameters `converstaionId`, `application` and `aperation` are mandatory.

### Sending events

It is possible to send a single event using `pubSubClient.sendEvent(event)` or a list of events `pubSubClient.sendEvents(events)`. However internally the events are buffered and send one by one without having a negative impact on the performance.

A simple Spring application could look like this:

```
@Autowired
private ApplicationConfiguration config;

private PubSubClient pubSubClient;

@PostConstruct
public void initialize() {
  pubSubClient = PubSubClient.builder().project(config.getTrasierProject())
                              .clientId(config.getTrasierProject())
                              .topic(config.getTrasierTopic())
                              .build();
}

@PreDestroy
public void shutdown() {
  pubSubClient.close(); // important, otherwise blocks and the application won't shut down.
}

public String sendBookingRequest(Context context, String request) {
  Event.Builder requestEvent = Event.newRequestEvent(context.getConversationId(), new Application("Sender"), "Booking")
                .correlationId(context.getCorrelationId()).contentType(ContentType.XML).data(request);

  String response = subsystem.executeBookRequest()

  Event responseEvent = Event.newResponseEvent(requestEvent).data(response).build();

  pubSubClient.sendEvents(Arrays.asList(requestEvent.build(), responseEvent));

return response;

}

```

