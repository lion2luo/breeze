// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user.proto

package com.weibo.test.proto;

public final class UserProto {
  private UserProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_User_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_User_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_Address_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_Address_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_Address_OtherEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_Address_OtherEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\nuser.proto\022\005proto\"a\n\004User\022\014\n\004name\030\001 \001(" +
      "\t\022\013\n\003age\030\002 \001(\005\022\035\n\006gender\030\003 \001(\0162\r.proto.G" +
      "ender\022\037\n\007address\030\004 \003(\0132\016.proto.Address\"~" +
      "\n\007Address\022\n\n\002id\030\001 \001(\005\022\017\n\007address\030\002 \001(\t\022(" +
      "\n\005other\030\003 \003(\0132\031.proto.Address.OtherEntry" +
      "\032,\n\nOtherEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030\002 \001" +
      "(\t:\0028\001*\034\n\006Gender\022\007\n\003Man\020\000\022\t\n\005Woman\020\001B)\n\024" +
      "com.weibo.test.protoB\tUserProtoP\001\242\002\003HLWb" +
      "\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_proto_User_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_proto_User_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_User_descriptor,
        new String[] { "Name", "Age", "Gender", "Address", });
    internal_static_proto_Address_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_proto_Address_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_Address_descriptor,
        new String[] { "Id", "Address", "Other", });
    internal_static_proto_Address_OtherEntry_descriptor =
      internal_static_proto_Address_descriptor.getNestedTypes().get(0);
    internal_static_proto_Address_OtherEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_Address_OtherEntry_descriptor,
        new String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
