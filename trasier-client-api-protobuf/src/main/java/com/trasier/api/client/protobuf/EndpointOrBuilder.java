// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: WriteService.proto

package com.trasier.api.client.protobuf;

public interface EndpointOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.trasier.api.client.protobuf.Endpoint)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string ipAddress = 2;</code>
   * @return The ipAddress.
   */
  String getIpAddress();
  /**
   * <code>string ipAddress = 2;</code>
   * @return The bytes for ipAddress.
   */
  com.google.protobuf.ByteString
      getIpAddressBytes();

  /**
   * <code>string port = 3;</code>
   * @return The port.
   */
  String getPort();
  /**
   * <code>string port = 3;</code>
   * @return The bytes for port.
   */
  com.google.protobuf.ByteString
      getPortBytes();

  /**
   * <code>string hostname = 4;</code>
   * @return The hostname.
   */
  String getHostname();
  /**
   * <code>string hostname = 4;</code>
   * @return The bytes for hostname.
   */
  com.google.protobuf.ByteString
      getHostnameBytes();
}