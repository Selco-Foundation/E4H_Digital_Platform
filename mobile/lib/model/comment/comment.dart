// import 'package:freezed_annotation/freezed_annotation.dart';
//
// part 'comment.g.dart';
//
// @JsonSerializable()
// class Comment {
//   final String? commentId;
//   final String? commentMessage;
//   final String? assetType;
//   final String? transactionId;
//
//   Comment({
//     this.commentId,
//     this.commentMessage,
//     this.assetType,
//     this.transactionId,
//   });
//
//   factory Comment.fromJson(Map<String, dynamic> json) =>
//       _$CommentFromJson(json);
//   Map<String, dynamic> toJson() => _$CommentToJson(this);
// }

import 'package:isar/isar.dart';

part 'comment.g.dart'; // optional (Isar will embed, codegen not required)

@Embedded()
class Comment {
  String? commentId;
  String? commentMessage;
  String? assetType;
  String? transactionId;

  Comment({
    this.commentId,
    this.commentMessage,
    this.assetType,
    this.transactionId,
  });

  factory Comment.fromJson(Map<String, dynamic> json) => Comment(
        commentId: json['commentId']?.toString(),
        commentMessage: json['commentMessage']?.toString(),
        assetType: json['assetType']?.toString(),
        transactionId: json['transactionId']?.toString(),
      );

  Map<String, dynamic> toJson() => {
        'commentId': commentId,
        'commentMessage': commentMessage,
        'assetType': assetType,
        'transactionId': transactionId,
      };
}
