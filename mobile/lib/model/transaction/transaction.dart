import 'package:freezed_annotation/freezed_annotation.dart';

import '../comment/comment.dart';

part 'transaction.g.dart';

@JsonSerializable(explicitToJson: true)
class Transaction {
  final String? transactionId;
  final String? processInstanceId;
  final String? projectId;
  final List<Comment>? comments;

  Transaction({
    this.transactionId,
    this.processInstanceId,
    this.projectId,
    this.comments,
  });

  factory Transaction.fromJson(Map<String, dynamic> json) =>
      _$TransactionFromJson(json);
  Map<String, dynamic> toJson() => _$TransactionToJson(this);
}
