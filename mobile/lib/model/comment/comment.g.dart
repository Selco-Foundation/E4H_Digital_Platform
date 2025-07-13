// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'comment.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Comment _$CommentFromJson(Map<String, dynamic> json) => Comment(
      commentId: json['commentId'] as String?,
      commentMessage: json['commentMessage'] as String?,
      assetType: json['assetType'] as String?,
      transactionId: json['transactionId'] as String?,
    );

Map<String, dynamic> _$CommentToJson(Comment instance) => <String, dynamic>{
      'commentId': instance.commentId,
      'commentMessage': instance.commentMessage,
      'assetType': instance.assetType,
      'transactionId': instance.transactionId,
    };
