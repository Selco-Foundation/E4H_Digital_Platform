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

import 'dart:convert';

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

  Map<String, dynamic>? get _maybeParsedJson {
    final raw = commentMessage;
    if (raw == null || raw.isEmpty) return null;
    try {
      final decoded = jsonDecode(raw);
      if (decoded is Map && decoded.containsKey('reason')) {
        return decoded.cast<String, dynamic>();
      }
    } catch (_) {
      // not JSON; ignore
    }
    return null;
  }

  String? get reason => _maybeParsedJson?['reason']?.toString();

  String get displayComment {
    final parsed = _maybeParsedJson;
    if (parsed != null) {
      final txt = parsed['comment']?.toString();
      if (txt != null && txt.isNotEmpty) return txt;
    }
    return commentMessage ?? '';
  }

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
