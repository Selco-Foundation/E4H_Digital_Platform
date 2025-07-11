// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, unnecessary_cast, override_on_non_overriding_member
// ignore_for_file: strict_raw_type, inference_failure_on_untyped_parameter

part of 'comment.dart';

class CommentMapper extends ClassMapperBase<Comment> {
  CommentMapper._();

  static CommentMapper? _instance;
  static CommentMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = CommentMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'Comment';

  static String? _$commentId(Comment v) => v.commentId;
  static const Field<Comment, String> _f$commentId =
      Field('commentId', _$commentId, opt: true);
  static String? _$commentMessage(Comment v) => v.commentMessage;
  static const Field<Comment, String> _f$commentMessage =
      Field('commentMessage', _$commentMessage, opt: true);
  static String? _$assetType(Comment v) => v.assetType;
  static const Field<Comment, String> _f$assetType =
      Field('assetType', _$assetType, opt: true);
  static String? _$transactionId(Comment v) => v.transactionId;
  static const Field<Comment, String> _f$transactionId =
      Field('transactionId', _$transactionId, opt: true);

  @override
  final MappableFields<Comment> fields = const {
    #commentId: _f$commentId,
    #commentMessage: _f$commentMessage,
    #assetType: _f$assetType,
    #transactionId: _f$transactionId,
  };
  @override
  final bool ignoreNull = true;

  static Comment _instantiate(DecodingData data) {
    return Comment(
        commentId: data.dec(_f$commentId),
        commentMessage: data.dec(_f$commentMessage),
        assetType: data.dec(_f$assetType),
        transactionId: data.dec(_f$transactionId));
  }

  @override
  final Function instantiate = _instantiate;

  static Comment fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<Comment>(map);
  }

  static Comment fromJson(String json) {
    return ensureInitialized().decodeJson<Comment>(json);
  }
}

mixin CommentMappable {
  String toJson() {
    return CommentMapper.ensureInitialized()
        .encodeJson<Comment>(this as Comment);
  }

  Map<String, dynamic> toMap() {
    return CommentMapper.ensureInitialized()
        .encodeMap<Comment>(this as Comment);
  }

  CommentCopyWith<Comment, Comment, Comment> get copyWith =>
      _CommentCopyWithImpl(this as Comment, $identity, $identity);
  @override
  String toString() {
    return CommentMapper.ensureInitialized().stringifyValue(this as Comment);
  }

  @override
  bool operator ==(Object other) {
    return CommentMapper.ensureInitialized()
        .equalsValue(this as Comment, other);
  }

  @override
  int get hashCode {
    return CommentMapper.ensureInitialized().hashValue(this as Comment);
  }
}

extension CommentValueCopy<$R, $Out> on ObjectCopyWith<$R, Comment, $Out> {
  CommentCopyWith<$R, Comment, $Out> get $asComment =>
      $base.as((v, t, t2) => _CommentCopyWithImpl(v, t, t2));
}

abstract class CommentCopyWith<$R, $In extends Comment, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call(
      {String? commentId,
      String? commentMessage,
      String? assetType,
      String? transactionId});
  CommentCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _CommentCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, Comment, $Out>
    implements CommentCopyWith<$R, Comment, $Out> {
  _CommentCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<Comment> $mapper =
      CommentMapper.ensureInitialized();
  @override
  $R call(
          {Object? commentId = $none,
          Object? commentMessage = $none,
          Object? assetType = $none,
          Object? transactionId = $none}) =>
      $apply(FieldCopyWithData({
        if (commentId != $none) #commentId: commentId,
        if (commentMessage != $none) #commentMessage: commentMessage,
        if (assetType != $none) #assetType: assetType,
        if (transactionId != $none) #transactionId: transactionId
      }));
  @override
  Comment $make(CopyWithData data) => Comment(
      commentId: data.get(#commentId, or: $value.commentId),
      commentMessage: data.get(#commentMessage, or: $value.commentMessage),
      assetType: data.get(#assetType, or: $value.assetType),
      transactionId: data.get(#transactionId, or: $value.transactionId));

  @override
  CommentCopyWith<$R2, Comment, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t) =>
      _CommentCopyWithImpl($value, $cast, t);
}
