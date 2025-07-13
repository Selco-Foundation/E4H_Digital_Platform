// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'transaction.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Transaction _$TransactionFromJson(Map<String, dynamic> json) => Transaction(
      transactionId: json['transactionId'] as String?,
      processInstanceId: json['processInstanceId'] as String?,
      projectId: json['projectId'] as String?,
      comments: (json['comments'] as List<dynamic>?)
          ?.map((e) => Comment.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$TransactionToJson(Transaction instance) =>
    <String, dynamic>{
      'transactionId': instance.transactionId,
      'processInstanceId': instance.processInstanceId,
      'projectId': instance.projectId,
      'comments': instance.comments?.map((e) => e.toJson()).toList(),
    };
