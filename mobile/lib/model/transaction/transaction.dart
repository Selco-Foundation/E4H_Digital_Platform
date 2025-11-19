import 'package:isar/isar.dart';

import '../comment/comment.dart';

part 'transaction.g.dart';

@Embedded()
class Transaction {
  String? transactionId;
  String? processInstanceId;
  String? activityFacilityId;

  @Embedded()
  List<Comment>? comments;

  Transaction({
    this.transactionId,
    this.processInstanceId,
    this.activityFacilityId,
    this.comments,
  });

  factory Transaction.fromJson(Map<String, dynamic> json) => Transaction(
        transactionId: json['transactionId']?.toString(),
        processInstanceId: json['processInstanceId']?.toString(),
        activityFacilityId: json['activityFacilityId']?.toString(),
        comments: json['comments'] is List
            ? (json['comments'] as List)
                .map((e) => Comment.fromJson(Map<String, dynamic>.from(e)))
                .toList()
            : null,
      );

  Map<String, dynamic> toJson() => {
        'transactionId': transactionId,
        'processInstanceId': processInstanceId,
        'activityFacilityId': activityFacilityId,
        'comments': comments?.map((c) => c.toJson()).toList(),
      };
}
