// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, unnecessary_cast, override_on_non_overriding_member
// ignore_for_file: strict_raw_type, inference_failure_on_untyped_parameter

part of 'transaction.dart';

class TransactionMapper extends ClassMapperBase<Transaction> {
  TransactionMapper._();

  static TransactionMapper? _instance;
  static TransactionMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = TransactionMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'Transaction';

  static String? _$transactionId(Transaction v) => v.transactionId;
  static const Field<Transaction, String> _f$transactionId =
      Field('transactionId', _$transactionId, opt: true);
  static String? _$processInstanceId(Transaction v) => v.processInstanceId;
  static const Field<Transaction, String> _f$processInstanceId =
      Field('processInstanceId', _$processInstanceId, opt: true);
  static String? _$projectId(Transaction v) => v.projectId;
  static const Field<Transaction, String> _f$projectId =
      Field('projectId', _$projectId, opt: true);
  static List<Comment>? _$comments(Transaction v) => v.comments;
  static const Field<Transaction, List<Comment>> _f$comments =
      Field('comments', _$comments, opt: true);

  @override
  final MappableFields<Transaction> fields = const {
    #transactionId: _f$transactionId,
    #processInstanceId: _f$processInstanceId,
    #projectId: _f$projectId,
    #comments: _f$comments,
  };
  @override
  final bool ignoreNull = true;

  static Transaction _instantiate(DecodingData data) {
    return Transaction(
        transactionId: data.dec(_f$transactionId),
        processInstanceId: data.dec(_f$processInstanceId),
        projectId: data.dec(_f$projectId),
        comments: data.dec(_f$comments));
  }

  @override
  final Function instantiate = _instantiate;

  static Transaction fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<Transaction>(map);
  }

  static Transaction fromJson(String json) {
    return ensureInitialized().decodeJson<Transaction>(json);
  }
}

mixin TransactionMappable {
  String toJson() {
    return TransactionMapper.ensureInitialized()
        .encodeJson<Transaction>(this as Transaction);
  }

  Map<String, dynamic> toMap() {
    return TransactionMapper.ensureInitialized()
        .encodeMap<Transaction>(this as Transaction);
  }

  TransactionCopyWith<Transaction, Transaction, Transaction> get copyWith =>
      _TransactionCopyWithImpl(this as Transaction, $identity, $identity);
  @override
  String toString() {
    return TransactionMapper.ensureInitialized()
        .stringifyValue(this as Transaction);
  }

  @override
  bool operator ==(Object other) {
    return TransactionMapper.ensureInitialized()
        .equalsValue(this as Transaction, other);
  }

  @override
  int get hashCode {
    return TransactionMapper.ensureInitialized().hashValue(this as Transaction);
  }
}

extension TransactionValueCopy<$R, $Out>
    on ObjectCopyWith<$R, Transaction, $Out> {
  TransactionCopyWith<$R, Transaction, $Out> get $asTransaction =>
      $base.as((v, t, t2) => _TransactionCopyWithImpl(v, t, t2));
}

abstract class TransactionCopyWith<$R, $In extends Transaction, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  ListCopyWith<$R, Comment, CommentCopyWith<$R, Comment, Comment>>?
      get comments;
  $R call(
      {String? transactionId,
      String? processInstanceId,
      String? projectId,
      List<Comment>? comments});
  TransactionCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _TransactionCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, Transaction, $Out>
    implements TransactionCopyWith<$R, Transaction, $Out> {
  _TransactionCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<Transaction> $mapper =
      TransactionMapper.ensureInitialized();
  @override
  ListCopyWith<$R, Comment, CommentCopyWith<$R, Comment, Comment>>?
      get comments => $value.comments != null
          ? ListCopyWith($value.comments!, (v, t) => v.copyWith.$chain(t),
              (v) => call(comments: v))
          : null;
  @override
  $R call(
          {Object? transactionId = $none,
          Object? processInstanceId = $none,
          Object? projectId = $none,
          Object? comments = $none}) =>
      $apply(FieldCopyWithData({
        if (transactionId != $none) #transactionId: transactionId,
        if (processInstanceId != $none) #processInstanceId: processInstanceId,
        if (projectId != $none) #projectId: projectId,
        if (comments != $none) #comments: comments
      }));
  @override
  Transaction $make(CopyWithData data) => Transaction(
      transactionId: data.get(#transactionId, or: $value.transactionId),
      processInstanceId:
          data.get(#processInstanceId, or: $value.processInstanceId),
      projectId: data.get(#projectId, or: $value.projectId),
      comments: data.get(#comments, or: $value.comments));

  @override
  TransactionCopyWith<$R2, Transaction, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _TransactionCopyWithImpl($value, $cast, t);
}
