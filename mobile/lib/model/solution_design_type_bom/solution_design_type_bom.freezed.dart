// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'solution_design_type_bom.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

SolutionDesignTypeBom _$SolutionDesignTypeBomFromJson(
    Map<String, dynamic> json) {
  return _SolutionDesignTypeBom.fromJson(json);
}

/// @nodoc
mixin _$SolutionDesignTypeBom {
  String get solutionDesignTypeCode => throw _privateConstructorUsedError;
  String get systemCode => throw _privateConstructorUsedError;
  List<BomEntry> get bomForms => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $SolutionDesignTypeBomCopyWith<SolutionDesignTypeBom> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SolutionDesignTypeBomCopyWith<$Res> {
  factory $SolutionDesignTypeBomCopyWith(SolutionDesignTypeBom value,
          $Res Function(SolutionDesignTypeBom) then) =
      _$SolutionDesignTypeBomCopyWithImpl<$Res, SolutionDesignTypeBom>;
  @useResult
  $Res call(
      {String solutionDesignTypeCode,
      String systemCode,
      List<BomEntry> bomForms});
}

/// @nodoc
class _$SolutionDesignTypeBomCopyWithImpl<$Res,
        $Val extends SolutionDesignTypeBom>
    implements $SolutionDesignTypeBomCopyWith<$Res> {
  _$SolutionDesignTypeBomCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? solutionDesignTypeCode = null,
    Object? systemCode = null,
    Object? bomForms = null,
  }) {
    return _then(_value.copyWith(
      solutionDesignTypeCode: null == solutionDesignTypeCode
          ? _value.solutionDesignTypeCode
          : solutionDesignTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
      systemCode: null == systemCode
          ? _value.systemCode
          : systemCode // ignore: cast_nullable_to_non_nullable
              as String,
      bomForms: null == bomForms
          ? _value.bomForms
          : bomForms // ignore: cast_nullable_to_non_nullable
              as List<BomEntry>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SolutionDesignTypeBomImplCopyWith<$Res>
    implements $SolutionDesignTypeBomCopyWith<$Res> {
  factory _$$SolutionDesignTypeBomImplCopyWith(
          _$SolutionDesignTypeBomImpl value,
          $Res Function(_$SolutionDesignTypeBomImpl) then) =
      __$$SolutionDesignTypeBomImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String solutionDesignTypeCode,
      String systemCode,
      List<BomEntry> bomForms});
}

/// @nodoc
class __$$SolutionDesignTypeBomImplCopyWithImpl<$Res>
    extends _$SolutionDesignTypeBomCopyWithImpl<$Res,
        _$SolutionDesignTypeBomImpl>
    implements _$$SolutionDesignTypeBomImplCopyWith<$Res> {
  __$$SolutionDesignTypeBomImplCopyWithImpl(_$SolutionDesignTypeBomImpl _value,
      $Res Function(_$SolutionDesignTypeBomImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? solutionDesignTypeCode = null,
    Object? systemCode = null,
    Object? bomForms = null,
  }) {
    return _then(_$SolutionDesignTypeBomImpl(
      solutionDesignTypeCode: null == solutionDesignTypeCode
          ? _value.solutionDesignTypeCode
          : solutionDesignTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
      systemCode: null == systemCode
          ? _value.systemCode
          : systemCode // ignore: cast_nullable_to_non_nullable
              as String,
      bomForms: null == bomForms
          ? _value._bomForms
          : bomForms // ignore: cast_nullable_to_non_nullable
              as List<BomEntry>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$SolutionDesignTypeBomImpl implements _SolutionDesignTypeBom {
  const _$SolutionDesignTypeBomImpl(
      {required this.solutionDesignTypeCode,
      required this.systemCode,
      required final List<BomEntry> bomForms})
      : _bomForms = bomForms;

  factory _$SolutionDesignTypeBomImpl.fromJson(Map<String, dynamic> json) =>
      _$$SolutionDesignTypeBomImplFromJson(json);

  @override
  final String solutionDesignTypeCode;
  @override
  final String systemCode;
  final List<BomEntry> _bomForms;
  @override
  List<BomEntry> get bomForms {
    if (_bomForms is EqualUnmodifiableListView) return _bomForms;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_bomForms);
  }

  @override
  String toString() {
    return 'SolutionDesignTypeBom(solutionDesignTypeCode: $solutionDesignTypeCode, systemCode: $systemCode, bomForms: $bomForms)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SolutionDesignTypeBomImpl &&
            (identical(other.solutionDesignTypeCode, solutionDesignTypeCode) ||
                other.solutionDesignTypeCode == solutionDesignTypeCode) &&
            (identical(other.systemCode, systemCode) ||
                other.systemCode == systemCode) &&
            const DeepCollectionEquality().equals(other._bomForms, _bomForms));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, solutionDesignTypeCode,
      systemCode, const DeepCollectionEquality().hash(_bomForms));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SolutionDesignTypeBomImplCopyWith<_$SolutionDesignTypeBomImpl>
      get copyWith => __$$SolutionDesignTypeBomImplCopyWithImpl<
          _$SolutionDesignTypeBomImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SolutionDesignTypeBomImplToJson(
      this,
    );
  }
}

abstract class _SolutionDesignTypeBom implements SolutionDesignTypeBom {
  const factory _SolutionDesignTypeBom(
      {required final String solutionDesignTypeCode,
      required final String systemCode,
      required final List<BomEntry> bomForms}) = _$SolutionDesignTypeBomImpl;

  factory _SolutionDesignTypeBom.fromJson(Map<String, dynamic> json) =
      _$SolutionDesignTypeBomImpl.fromJson;

  @override
  String get solutionDesignTypeCode;
  @override
  String get systemCode;
  @override
  List<BomEntry> get bomForms;
  @override
  @JsonKey(ignore: true)
  _$$SolutionDesignTypeBomImplCopyWith<_$SolutionDesignTypeBomImpl>
      get copyWith => throw _privateConstructorUsedError;
}

BomEntry _$BomEntryFromJson(Map<String, dynamic> json) {
  return _BomEntry.fromJson(json);
}

/// @nodoc
mixin _$BomEntry {
  String get name => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $BomEntryCopyWith<BomEntry> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BomEntryCopyWith<$Res> {
  factory $BomEntryCopyWith(BomEntry value, $Res Function(BomEntry) then) =
      _$BomEntryCopyWithImpl<$Res, BomEntry>;
  @useResult
  $Res call({String name});
}

/// @nodoc
class _$BomEntryCopyWithImpl<$Res, $Val extends BomEntry>
    implements $BomEntryCopyWith<$Res> {
  _$BomEntryCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? name = null,
  }) {
    return _then(_value.copyWith(
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BomEntryImplCopyWith<$Res>
    implements $BomEntryCopyWith<$Res> {
  factory _$$BomEntryImplCopyWith(
          _$BomEntryImpl value, $Res Function(_$BomEntryImpl) then) =
      __$$BomEntryImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String name});
}

/// @nodoc
class __$$BomEntryImplCopyWithImpl<$Res>
    extends _$BomEntryCopyWithImpl<$Res, _$BomEntryImpl>
    implements _$$BomEntryImplCopyWith<$Res> {
  __$$BomEntryImplCopyWithImpl(
      _$BomEntryImpl _value, $Res Function(_$BomEntryImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? name = null,
  }) {
    return _then(_$BomEntryImpl(
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BomEntryImpl implements _BomEntry {
  const _$BomEntryImpl({required this.name});

  factory _$BomEntryImpl.fromJson(Map<String, dynamic> json) =>
      _$$BomEntryImplFromJson(json);

  @override
  final String name;

  @override
  String toString() {
    return 'BomEntry(name: $name)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BomEntryImpl &&
            (identical(other.name, name) || other.name == name));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, name);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BomEntryImplCopyWith<_$BomEntryImpl> get copyWith =>
      __$$BomEntryImplCopyWithImpl<_$BomEntryImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BomEntryImplToJson(
      this,
    );
  }
}

abstract class _BomEntry implements BomEntry {
  const factory _BomEntry({required final String name}) = _$BomEntryImpl;

  factory _BomEntry.fromJson(Map<String, dynamic> json) =
      _$BomEntryImpl.fromJson;

  @override
  String get name;
  @override
  @JsonKey(ignore: true)
  _$$BomEntryImplCopyWith<_$BomEntryImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
