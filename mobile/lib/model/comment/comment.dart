import 'package:dart_mappable/dart_mappable.dart';
import 'package:isar/isar.dart';

part 'comment.g.dart';
part 'comment.mapper.dart';

@embedded
@MappableClass(discriminatorValue: MappableClass.useAsDefault, ignoreNull: true)
class Comment with CommentMappable {
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
}
