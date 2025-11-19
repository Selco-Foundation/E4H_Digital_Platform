// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'asset_count.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

AssetCount _$AssetCountFromJson(Map<String, dynamic> json) {
  return _AssetCount.fromJson(json);
}

/// @nodoc
mixin _$AssetCount {
  int get max => throw _privateConstructorUsedError;
  int get min => throw _privateConstructorUsedError;
  bool get active => throw _privateConstructorUsedError;
  @JsonKey(name: 'asset_type_code')
  String get assetTypeCode => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AssetCountCopyWith<AssetCount> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetCountCopyWith<$Res> {
  factory $AssetCountCopyWith(
          AssetCount value, $Res Function(AssetCount) then) =
      _$AssetCountCopyWithImpl<$Res, AssetCount>;
  @useResult
  $Res call(
      {int max,
      int min,
      bool active,
      @JsonKey(name: 'asset_type_code') String assetTypeCode});
}

/// @nodoc
class _$AssetCountCopyWithImpl<$Res, $Val extends AssetCount>
    implements $AssetCountCopyWith<$Res> {
  _$AssetCountCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? max = null,
    Object? min = null,
    Object? active = null,
    Object? assetTypeCode = null,
  }) {
    return _then(_value.copyWith(
      max: null == max
          ? _value.max
          : max // ignore: cast_nullable_to_non_nullable
              as int,
      min: null == min
          ? _value.min
          : min // ignore: cast_nullable_to_non_nullable
              as int,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      assetTypeCode: null == assetTypeCode
          ? _value.assetTypeCode
          : assetTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AssetCountImplCopyWith<$Res>
    implements $AssetCountCopyWith<$Res> {
  factory _$$AssetCountImplCopyWith(
          _$AssetCountImpl value, $Res Function(_$AssetCountImpl) then) =
      __$$AssetCountImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int max,
      int min,
      bool active,
      @JsonKey(name: 'asset_type_code') String assetTypeCode});
}

/// @nodoc
class __$$AssetCountImplCopyWithImpl<$Res>
    extends _$AssetCountCopyWithImpl<$Res, _$AssetCountImpl>
    implements _$$AssetCountImplCopyWith<$Res> {
  __$$AssetCountImplCopyWithImpl(
      _$AssetCountImpl _value, $Res Function(_$AssetCountImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? max = null,
    Object? min = null,
    Object? active = null,
    Object? assetTypeCode = null,
  }) {
    return _then(_$AssetCountImpl(
      max: null == max
          ? _value.max
          : max // ignore: cast_nullable_to_non_nullable
              as int,
      min: null == min
          ? _value.min
          : min // ignore: cast_nullable_to_non_nullable
              as int,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      assetTypeCode: null == assetTypeCode
          ? _value.assetTypeCode
          : assetTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AssetCountImpl implements _AssetCount {
  const _$AssetCountImpl(
      {required this.max,
      required this.min,
      required this.active,
      @JsonKey(name: 'asset_type_code') required this.assetTypeCode});

  factory _$AssetCountImpl.fromJson(Map<String, dynamic> json) =>
      _$$AssetCountImplFromJson(json);

  @override
  final int max;
  @override
  final int min;
  @override
  final bool active;
  @override
  @JsonKey(name: 'asset_type_code')
  final String assetTypeCode;

  @override
  String toString() {
    return 'AssetCount(max: $max, min: $min, active: $active, assetTypeCode: $assetTypeCode)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AssetCountImpl &&
            (identical(other.max, max) || other.max == max) &&
            (identical(other.min, min) || other.min == min) &&
            (identical(other.active, active) || other.active == active) &&
            (identical(other.assetTypeCode, assetTypeCode) ||
                other.assetTypeCode == assetTypeCode));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, max, min, active, assetTypeCode);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AssetCountImplCopyWith<_$AssetCountImpl> get copyWith =>
      __$$AssetCountImplCopyWithImpl<_$AssetCountImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AssetCountImplToJson(
      this,
    );
  }
}

abstract class _AssetCount implements AssetCount {
  const factory _AssetCount(
      {required final int max,
      required final int min,
      required final bool active,
      @JsonKey(name: 'asset_type_code')
      required final String assetTypeCode}) = _$AssetCountImpl;

  factory _AssetCount.fromJson(Map<String, dynamic> json) =
      _$AssetCountImpl.fromJson;

  @override
  int get max;
  @override
  int get min;
  @override
  bool get active;
  @override
  @JsonKey(name: 'asset_type_code')
  String get assetTypeCode;
  @override
  @JsonKey(ignore: true)
  _$$AssetCountImplCopyWith<_$AssetCountImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AssetCountData _$AssetCountDataFromJson(Map<String, dynamic> json) {
  return _AssetCountData.fromJson(json);
}

/// @nodoc
mixin _$AssetCountData {
  int get id => throw _privateConstructorUsedError;
  String get module => throw _privateConstructorUsedError;
  String get tenantId => throw _privateConstructorUsedError;
  @JsonKey(name: 'AssetCount')
  List<AssetCount> get assetCount => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AssetCountDataCopyWith<AssetCountData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetCountDataCopyWith<$Res> {
  factory $AssetCountDataCopyWith(
          AssetCountData value, $Res Function(AssetCountData) then) =
      _$AssetCountDataCopyWithImpl<$Res, AssetCountData>;
  @useResult
  $Res call(
      {int id,
      String module,
      String tenantId,
      @JsonKey(name: 'AssetCount') List<AssetCount> assetCount});
}

/// @nodoc
class _$AssetCountDataCopyWithImpl<$Res, $Val extends AssetCountData>
    implements $AssetCountDataCopyWith<$Res> {
  _$AssetCountDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? module = null,
    Object? tenantId = null,
    Object? assetCount = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      module: null == module
          ? _value.module
          : module // ignore: cast_nullable_to_non_nullable
              as String,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
      assetCount: null == assetCount
          ? _value.assetCount
          : assetCount // ignore: cast_nullable_to_non_nullable
              as List<AssetCount>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AssetCountDataImplCopyWith<$Res>
    implements $AssetCountDataCopyWith<$Res> {
  factory _$$AssetCountDataImplCopyWith(_$AssetCountDataImpl value,
          $Res Function(_$AssetCountDataImpl) then) =
      __$$AssetCountDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int id,
      String module,
      String tenantId,
      @JsonKey(name: 'AssetCount') List<AssetCount> assetCount});
}

/// @nodoc
class __$$AssetCountDataImplCopyWithImpl<$Res>
    extends _$AssetCountDataCopyWithImpl<$Res, _$AssetCountDataImpl>
    implements _$$AssetCountDataImplCopyWith<$Res> {
  __$$AssetCountDataImplCopyWithImpl(
      _$AssetCountDataImpl _value, $Res Function(_$AssetCountDataImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? module = null,
    Object? tenantId = null,
    Object? assetCount = null,
  }) {
    return _then(_$AssetCountDataImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      module: null == module
          ? _value.module
          : module // ignore: cast_nullable_to_non_nullable
              as String,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
      assetCount: null == assetCount
          ? _value._assetCount
          : assetCount // ignore: cast_nullable_to_non_nullable
              as List<AssetCount>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AssetCountDataImpl implements _AssetCountData {
  const _$AssetCountDataImpl(
      {required this.id,
      required this.module,
      required this.tenantId,
      @JsonKey(name: 'AssetCount') required final List<AssetCount> assetCount})
      : _assetCount = assetCount;

  factory _$AssetCountDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$AssetCountDataImplFromJson(json);

  @override
  final int id;
  @override
  final String module;
  @override
  final String tenantId;
  final List<AssetCount> _assetCount;
  @override
  @JsonKey(name: 'AssetCount')
  List<AssetCount> get assetCount {
    if (_assetCount is EqualUnmodifiableListView) return _assetCount;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assetCount);
  }

  @override
  String toString() {
    return 'AssetCountData(id: $id, module: $module, tenantId: $tenantId, assetCount: $assetCount)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AssetCountDataImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.module, module) || other.module == module) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            const DeepCollectionEquality()
                .equals(other._assetCount, _assetCount));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, id, module, tenantId,
      const DeepCollectionEquality().hash(_assetCount));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AssetCountDataImplCopyWith<_$AssetCountDataImpl> get copyWith =>
      __$$AssetCountDataImplCopyWithImpl<_$AssetCountDataImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AssetCountDataImplToJson(
      this,
    );
  }
}

abstract class _AssetCountData implements AssetCountData {
  const factory _AssetCountData(
      {required final int id,
      required final String module,
      required final String tenantId,
      @JsonKey(name: 'AssetCount')
      required final List<AssetCount> assetCount}) = _$AssetCountDataImpl;

  factory _AssetCountData.fromJson(Map<String, dynamic> json) =
      _$AssetCountDataImpl.fromJson;

  @override
  int get id;
  @override
  String get module;
  @override
  String get tenantId;
  @override
  @JsonKey(name: 'AssetCount')
  List<AssetCount> get assetCount;
  @override
  @JsonKey(ignore: true)
  _$$AssetCountDataImplCopyWith<_$AssetCountDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
