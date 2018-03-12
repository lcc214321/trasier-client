[![Build Status](https://travis-ci.org/trasiercom/trasier-client.svg?branch=develop)](https://travis-ci.org/trasiercom/trasier-client)
[![Coverage Status](https://coveralls.io/repos/github/trasiercom/trasier-client/badge.svg?branch=develop&g=3)](https://coveralls.io/github/trasiercom/trasier-client?branch=develop)


Trasier Client sends tracing data into the Trasier System.

# PubSubCLient

Trasier's PubSubClient is a high performant, non I/O blocking pub/sub client. It is based on Netty and uses `async-google-pubsub-client` under the hood.

## Configuration

Trasier PubSubClient must be configured with `serviceAccountToken` `project`, `clientId` and `topic` parameters. Those parameters are provided by Trasier during the registration process.

```
PubSubClient pubSubClient = PubSubClient.builder().serviceAccountToken(...).project(...).clientId(...).topic(...).build();
```

## Usage

### Creating an Event

An event can be either of type `REQUEST`, `RESPONSE` or `NOTICE`.

The Event class has appropriate Builder methods:

```
Event.Builder requestEvent = Event.newRequestEvent(conversationId, producer, operation);

Event.Builder responseEvent = newResponseEvent(Builder requestEvent);

Event.Builder noticeEvent = newEvent(UUID conversationId, Application producer, String operation);
```

The Request and Response Event is used to trace messages exchanged by two Applications (the producer and the consumer application).

The Notice event can be used to trace an internal state of an application (for example complex filtering algorithm).

Parameters `converstaionId`, `application` and `operation` are mandatory.

### Sending events

It is possible to send a single event using `pubSubClient.sendEvent(event)` or a list of events `pubSubClient.sendEvents(events)`. However internally the events are buffered and send one by one without having a negative impact on the performance.

A simple Spring application could look like this:

```
@Autowired
private ApplicationConfiguration config;

private PubSubClient pubSubClient;

@PostConstruct
public void initialize() {
  pubSubClient = PubSubClient.builder()
                              .serviceAccountToken(config.getTrasierServiceAccountToken())
                              .project(config.getTrasierProject())
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

