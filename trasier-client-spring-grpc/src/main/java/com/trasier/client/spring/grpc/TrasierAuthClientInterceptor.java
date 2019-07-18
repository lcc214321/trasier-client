package com.trasier.client.spring.grpc;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.spring.auth.OAuthTokenSafe;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrasierAuthClientInterceptor implements ClientInterceptor {

    private final TrasierClientConfiguration clientConfiguration;
    private final OAuthTokenSafe oAuthTokenSafe;

    @Autowired
    public TrasierAuthClientInterceptor(final TrasierClientConfiguration clientConfiguration, final OAuthTokenSafe oAuthTokenSafe) {
        this.clientConfiguration = clientConfiguration;
        this.oAuthTokenSafe = oAuthTokenSafe;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ClientInterceptors.CheckedForwardingClientCall(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            protected void checkedStart(Listener listener, Metadata metadata) {
                metadata.put(Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER), oAuthTokenSafe.getToken());
                metadata.put(Metadata.Key.of("accountId", Metadata.ASCII_STRING_MARSHALLER), clientConfiguration.getAccountId());
                metadata.put(Metadata.Key.of("spaceKey", Metadata.ASCII_STRING_MARSHALLER), clientConfiguration.getSpaceKey());
                delegate().start(listener, metadata);
            }
        };
    }

}