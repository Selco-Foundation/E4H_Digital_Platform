import 'package:dart_mappable/dart_mappable.dart';
import 'package:isar/isar.dart';

import '../comment/comment.dart';

part 'transaction.g.dart';
part 'transaction.mapper.dart';

@embedded
@MappableClass(discriminatorValue: MappableClass.useAsDefault, ignoreNull: true)
class Transaction with TransactionMappable {
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
}
