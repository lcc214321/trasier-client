package com.trasier.api.client.protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.4.0)",
    comments = "Source: WriteService.proto")
public final class WriteServiceGrpc {

  private WriteServiceGrpc() {}

  public static final String SERVICE_NAME = "com.trasier.api.client.protobuf.WriteService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<SpanRequest,
          SpanResponse> METHOD_SEND =
      io.grpc.MethodDescriptor.<SpanRequest, SpanResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "com.trasier.api.client.protobuf.WriteService", "send"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              SpanRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              SpanResponse.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static WriteServiceStub newStub(io.grpc.Channel channel) {
    return new WriteServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static WriteServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new WriteServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static WriteServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new WriteServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class WriteServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<SpanRequest> send(
        io.grpc.stub.StreamObserver<SpanResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_SEND, responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_SEND,
            asyncBidiStreamingCall(
              new MethodHandlers<
                      SpanRequest,
                      SpanResponse>(
                  this, METHODID_SEND)))
          .build();
    }
  }

  /**
   */
  public static final class WriteServiceStub extends io.grpc.stub.AbstractStub<WriteServiceStub> {
    private WriteServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private WriteServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected WriteServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new WriteServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<SpanRequest> send(
        io.grpc.stub.StreamObserver<SpanResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_SEND, getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class WriteServiceBlockingStub extends io.grpc.stub.AbstractStub<WriteServiceBlockingStub> {
    private WriteServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private WriteServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected WriteServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new WriteServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class WriteServiceFutureStub extends io.grpc.stub.AbstractStub<WriteServiceFutureStub> {
    private WriteServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private WriteServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected WriteServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new WriteServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_SEND = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final WriteServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(WriteServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.send(
              (io.grpc.stub.StreamObserver<SpanResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class WriteServiceDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return WriteServiceOuterClass.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (WriteServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new WriteServiceDescriptorSupplier())
              .addMethod(METHOD_SEND)
              .build();
        }
      }
    }
    return result;
  }
}
