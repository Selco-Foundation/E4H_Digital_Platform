// import 'package:freezed_annotation/freezed_annotation.dart';
//
// import '../comment/comment.dart';
//
// part 'transaction.g.dart';
//
// @JsonSerializable(explicitToJson: true)
// class Transaction {
//   final String? transactionId;
//   final String? processInstanceId;
//   final String? projectId;
//   final List<Comment>? comments;
//
//   Transaction({
//     this.transactionId,
//     this.processInstanceId,
//     this.projectId,
//     this.comments,
//   });
//
//   factory Transaction.fromJson(Map<String, dynamic> json) =>
//       _$TransactionFromJson(json);
//   Map<String, dynamic> toJson() => _$TransactionToJson(this);
// }

import 'package:isar/isar.dart';

import '../comment/comment.dart';

part 'transaction.g.dart'; // optional

@Embedded()
class Transaction {
  String? transactionId;
  String? processInstanceId;
  String? projectId;

  /// Embedded comments
  @Embedded()
  List<Comment>? comments;

  Transaction({
    this.transactionId,
    this.processInstanceId,
    this.projectId,
    this.comments,
  });

  factory Transaction.fromJson(Map<String, dynamic> json) => Transaction(
        transactionId: json['transactionId']?.toString(),
        processInstanceId: json['processInstanceId']?.toString(),
        projectId: json['projectId']?.toString(),
        comments: json['comments'] is List
            ? (json['comments'] as List)
                .map((e) => Comment.fromJson(Map<String, dynamic>.from(e)))
                .toList()
            : null,
      );

  Map<String, dynamic> toJson() => {
        'transactionId': transactionId,
        'processInstanceId': processInstanceId,
        'projectId': projectId,
        'comments': comments?.map((c) => c.toJson()).toList(),
      };
}
