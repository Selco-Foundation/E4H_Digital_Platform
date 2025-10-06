import 'dart:convert';

import 'package:isar/isar.dart';

part 'comment.g.dart';

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

  /// Try to parse a structured JSON payload like:
  /// {"reason":"...","comment":"..."}
  /// Returns a Map<String,dynamic> only if it looks like the structured form.
  Map<String, dynamic>? get _maybeParsedJson {
    final raw = commentMessage;
    if (raw == null || raw.isEmpty) return null;
    try {
      final decoded = jsonDecode(raw);
      if (decoded is Map) {
        final map = decoded.cast<String, dynamic>();
        final hasReason = map.containsKey('reason');
        final hasComment = map.containsKey('comment');
        if (hasReason || hasComment) return map; // treat as structured
      }
    } catch (_) {
      // not JSON; ignore
    }
    return null;
  }

  String? get reason => _maybeParsedJson?['reason']?.toString();

  /// If the message is structured JSON, ALWAYS return the parsed "comment"
  /// (even when it's an empty string) so the UI shows blank instead of raw JSON.
  /// If not structured, fall back to the plain commentMessage.
  String get displayComment {
    final parsed = _maybeParsedJson;
    if (parsed != null) {
      final val = parsed['comment'];
      // Return "" if missing or null, to avoid showing the raw JSON text
      return (val is String) ? val : '';
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
