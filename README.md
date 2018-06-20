[![Build Status](https://travis-ci.org/trasiercom/trasier-client.svg?branch=develop)](https://travis-ci.org/trasiercom/trasier-client)
[![Coverage Status](https://coveralls.io/repos/github/trasiercom/trasier-client/badge.svg?branch=develop&g=4)](https://coveralls.io/github/trasiercom/trasier-client?branch=develop)


Trasier Client ingests tracing data into the Trasier system.

# PubSubClient

Trasier's PubSubClient is a high performant, non I/O blocking pub/sub client. It is based on Netty and uses `async-google-pubsub-client` under the hood.

## Configuration

Trasier PubSubClient must be configured by setting the `serviceAccountToken` and `spaceId` parameters.
These parameters are provided by Trasier during the registration process.
Optionally there may be a different `project` and `topic` set, if not provided we will assume the default settings.

```
PubSubClient pubSubClientDefault = PubSubClient.builder().serviceAccountToken(...).spaceId(...).build();
PubSubClient pubSubClientCustomized = PubSubClient.builder().serviceAccountToken(...).project(...).spaceId(...).topic(...).build();
```

## Usage

### Creating a Span

A span is the main entity within the trasier system and can be a regular span in terms of the OpenTracing standard or a single event.

The Span class has appropriate builder methods:

```
Span regularSpan = Span.newSpan(conversationId, traceId, endpoint, name).startTimestamp(startTimestamp).endTimestamp(endTimestamp).build();

Span singleEventSpan = Span.newSpan(conversationId, traceId, endpoint, name).startTimestamp(startTimestamp).build();
```

The regular case is used to trace messages exchanged between two applications. The producing application is considered as the "incoming" endpoint of the span whereas the consuming application is considered the "outgoing" application.

A single event is typically used add additional information to a span that does not represent an actual message but some internal state of an application (for example complex filtering algorithm).

If there is not way of tracing the incoming and outgoing data in on go (asynchronous / reactive behaviour) it is also possible to trace them in two separate single span-events. The trasier system takes care of merging them into a regular span on read-time. However, it is important to keep the IDs and the operationName of the span the span and to take care of setting the correct timestamps and data attributes.

The parameters `converstaionId`, `traceId`, one `endpoint`, one `timestamp` and the `operationName` are mandatory.

### Sending spans

It is possible to submit a single span using `pubSubClient.sendSpan(span)` or a list of spans `pubSubClient.sendSpans(spans)`. However internally all spans are buffered and send in chunks to reduce the operational overhead.

A simple Spring application could look like this:

```
@Autowired
private ApplicationConfiguration config;

private PubSubClient pubSubClient;

@PostConstruct
public void initialize() {
  pubSubClient = PubSubClient.builder()
                              .serviceAccountToken(config.getTrasierServiceAccountToken())
                              .clientId(config.getTrasierProject())
                              .build();
}

@PreDestroy
public void shutdown() {
  pubSubClient.close(); // important, otherwise blocks and the application won't shut down.
}

public String sendBookingRequest(Context context, String request) {
  Span.Builder span = Span.newSpan(context.getConversationId(), context.getTraceId(), new Endpoint("Sendername"), "Booking");
  
  span.startTimestamo(System.currentTimeMillis());
  span.incomingData(request);
  span.contentType(ContentType.XML);
  
  //Do the actual request processing.
  String response = subsystem.executeBookRequest();
  
  span.endTimestamo(System.currentTimeMillis());
  span.outgoingData(response);
  span.contentType(ContentType.XML);

  pubSubClient.sendSpan(span.build());

  return response;
}
```
