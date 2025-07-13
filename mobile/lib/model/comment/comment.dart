import 'package:freezed_annotation/freezed_annotation.dart';

part 'comment.g.dart';

@JsonSerializable()
class Comment {
  final String? commentId;
  final String? commentMessage;
  final String? assetType;
  final String? transactionId;

  Comment({
    this.commentId,
    this.commentMessage,
    this.assetType,
    this.transactionId,
  });

  factory Comment.fromJson(Map<String, dynamic> json) =>
      _$CommentFromJson(json);
  Map<String, dynamic> toJson() => _$CommentToJson(this);
}
