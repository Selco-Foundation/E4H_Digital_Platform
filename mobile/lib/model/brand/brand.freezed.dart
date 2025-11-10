// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'brand.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

Brand _$BrandFromJson(Map<String, dynamic> json) {
  return _Brand.fromJson(json);
}

/// @nodoc
mixin _$Brand {
  bool get active => throw _privateConstructorUsedError;
  String get code => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  @JsonKey(name: 'asset_type_code')
  String get assetTypeCode => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $BrandCopyWith<Brand> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BrandCopyWith<$Res> {
  factory $BrandCopyWith(Brand value, $Res Function(Brand) then) =
      _$BrandCopyWithImpl<$Res, Brand>;
  @useResult
  $Res call(
      {bool active,
      String code,
      String name,
      @JsonKey(name: 'asset_type_code') String assetTypeCode});
}

/// @nodoc
class _$BrandCopyWithImpl<$Res, $Val extends Brand>
    implements $BrandCopyWith<$Res> {
  _$BrandCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? active = null,
    Object? code = null,
    Object? name = null,
    Object? assetTypeCode = null,
  }) {
    return _then(_value.copyWith(
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      assetTypeCode: null == assetTypeCode
          ? _value.assetTypeCode
          : assetTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BrandImplCopyWith<$Res> implements $BrandCopyWith<$Res> {
  factory _$$BrandImplCopyWith(
          _$BrandImpl value, $Res Function(_$BrandImpl) then) =
      __$$BrandImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {bool active,
      String code,
      String name,
      @JsonKey(name: 'asset_type_code') String assetTypeCode});
}

/// @nodoc
class __$$BrandImplCopyWithImpl<$Res>
    extends _$BrandCopyWithImpl<$Res, _$BrandImpl>
    implements _$$BrandImplCopyWith<$Res> {
  __$$BrandImplCopyWithImpl(
      _$BrandImpl _value, $Res Function(_$BrandImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? active = null,
    Object? code = null,
    Object? name = null,
    Object? assetTypeCode = null,
  }) {
    return _then(_$BrandImpl(
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      assetTypeCode: null == assetTypeCode
          ? _value.assetTypeCode
          : assetTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BrandImpl implements _Brand {
  const _$BrandImpl(
      {required this.active,
      required this.code,
      required this.name,
      @JsonKey(name: 'asset_type_code') required this.assetTypeCode});

  factory _$BrandImpl.fromJson(Map<String, dynamic> json) =>
      _$$BrandImplFromJson(json);

  @override
  final bool active;
  @override
  final String code;
  @override
  final String name;
  @override
  @JsonKey(name: 'asset_type_code')
  final String assetTypeCode;

  @override
  String toString() {
    return 'Brand(active: $active, code: $code, name: $name, assetTypeCode: $assetTypeCode)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BrandImpl &&
            (identical(other.active, active) || other.active == active) &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.assetTypeCode, assetTypeCode) ||
                other.assetTypeCode == assetTypeCode));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode =>
      Object.hash(runtimeType, active, code, name, assetTypeCode);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BrandImplCopyWith<_$BrandImpl> get copyWith =>
      __$$BrandImplCopyWithImpl<_$BrandImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BrandImplToJson(
      this,
    );
  }
}

abstract class _Brand implements Brand {
  const factory _Brand(
      {required final bool active,
      required final String code,
      required final String name,
      @JsonKey(name: 'asset_type_code')
      required final String assetTypeCode}) = _$BrandImpl;

  factory _Brand.fromJson(Map<String, dynamic> json) = _$BrandImpl.fromJson;

  @override
  bool get active;
  @override
  String get code;
  @override
  String get name;
  @override
  @JsonKey(name: 'asset_type_code')
  String get assetTypeCode;
  @override
  @JsonKey(ignore: true)
  _$$BrandImplCopyWith<_$BrandImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BrandData _$BrandDataFromJson(Map<String, dynamic> json) {
  return _BrandData.fromJson(json);
}

/// @nodoc
mixin _$BrandData {
  int get id => throw _privateConstructorUsedError;
  @JsonKey(name: 'Brand')
  List<Brand> get brand => throw _privateConstructorUsedError;
  String get module => throw _privateConstructorUsedError;
  String get tenantId => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $BrandDataCopyWith<BrandData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BrandDataCopyWith<$Res> {
  factory $BrandDataCopyWith(BrandData value, $Res Function(BrandData) then) =
      _$BrandDataCopyWithImpl<$Res, BrandData>;
  @useResult
  $Res call(
      {int id,
      @JsonKey(name: 'Brand') List<Brand> brand,
      String module,
      String tenantId});
}

/// @nodoc
class _$BrandDataCopyWithImpl<$Res, $Val extends BrandData>
    implements $BrandDataCopyWith<$Res> {
  _$BrandDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? brand = null,
    Object? module = null,
    Object? tenantId = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      brand: null == brand
          ? _value.brand
          : brand // ignore: cast_nullable_to_non_nullable
              as List<Brand>,
      module: null == module
          ? _value.module
          : module // ignore: cast_nullable_to_non_nullable
              as String,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BrandDataImplCopyWith<$Res>
    implements $BrandDataCopyWith<$Res> {
  factory _$$BrandDataImplCopyWith(
          _$BrandDataImpl value, $Res Function(_$BrandDataImpl) then) =
      __$$BrandDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int id,
      @JsonKey(name: 'Brand') List<Brand> brand,
      String module,
      String tenantId});
}

/// @nodoc
class __$$BrandDataImplCopyWithImpl<$Res>
    extends _$BrandDataCopyWithImpl<$Res, _$BrandDataImpl>
    implements _$$BrandDataImplCopyWith<$Res> {
  __$$BrandDataImplCopyWithImpl(
      _$BrandDataImpl _value, $Res Function(_$BrandDataImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? brand = null,
    Object? module = null,
    Object? tenantId = null,
  }) {
    return _then(_$BrandDataImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      brand: null == brand
          ? _value._brand
          : brand // ignore: cast_nullable_to_non_nullable
              as List<Brand>,
      module: null == module
          ? _value.module
          : module // ignore: cast_nullable_to_non_nullable
              as String,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BrandDataImpl implements _BrandData {
  const _$BrandDataImpl(
      {required this.id,
      @JsonKey(name: 'Brand') required final List<Brand> brand,
      required this.module,
      required this.tenantId})
      : _brand = brand;

  factory _$BrandDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$BrandDataImplFromJson(json);

  @override
  final int id;
  final List<Brand> _brand;
  @override
  @JsonKey(name: 'Brand')
  List<Brand> get brand {
    if (_brand is EqualUnmodifiableListView) return _brand;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_brand);
  }

  @override
  final String module;
  @override
  final String tenantId;

  @override
  String toString() {
    return 'BrandData(id: $id, brand: $brand, module: $module, tenantId: $tenantId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BrandDataImpl &&
            (identical(other.id, id) || other.id == id) &&
            const DeepCollectionEquality().equals(other._brand, _brand) &&
            (identical(other.module, module) || other.module == module) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, id,
      const DeepCollectionEquality().hash(_brand), module, tenantId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BrandDataImplCopyWith<_$BrandDataImpl> get copyWith =>
      __$$BrandDataImplCopyWithImpl<_$BrandDataImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BrandDataImplToJson(
      this,
    );
  }
}

abstract class _BrandData implements BrandData {
  const factory _BrandData(
      {required final int id,
      @JsonKey(name: 'Brand') required final List<Brand> brand,
      required final String module,
      required final String tenantId}) = _$BrandDataImpl;

  factory _BrandData.fromJson(Map<String, dynamic> json) =
      _$BrandDataImpl.fromJson;

  @override
  int get id;
  @override
  @JsonKey(name: 'Brand')
  List<Brand> get brand;
  @override
  String get module;
  @override
  String get tenantId;
  @override
  @JsonKey(ignore: true)
  _$$BrandDataImplCopyWith<_$BrandDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
