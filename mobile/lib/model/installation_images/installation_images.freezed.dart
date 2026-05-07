// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'installation_images.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

InstallationImageItem _$InstallationImageItemFromJson(
    Map<String, dynamic> json) {
  return _InstallationImageItem.fromJson(json);
}

/// @nodoc
mixin _$InstallationImageItem {
  String get code => throw _privateConstructorUsedError;
  bool get active => throw _privateConstructorUsedError;
  String get description => throw _privateConstructorUsedError;
  @JsonKey(name: 'required_count')
  int get requiredCount => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $InstallationImageItemCopyWith<InstallationImageItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InstallationImageItemCopyWith<$Res> {
  factory $InstallationImageItemCopyWith(InstallationImageItem value,
          $Res Function(InstallationImageItem) then) =
      _$InstallationImageItemCopyWithImpl<$Res, InstallationImageItem>;
  @useResult
  $Res call(
      {String code,
      bool active,
      String description,
      @JsonKey(name: 'required_count') int requiredCount});
}

/// @nodoc
class _$InstallationImageItemCopyWithImpl<$Res,
        $Val extends InstallationImageItem>
    implements $InstallationImageItemCopyWith<$Res> {
  _$InstallationImageItemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? active = null,
    Object? description = null,
    Object? requiredCount = null,
  }) {
    return _then(_value.copyWith(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      requiredCount: null == requiredCount
          ? _value.requiredCount
          : requiredCount // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$InstallationImageItemImplCopyWith<$Res>
    implements $InstallationImageItemCopyWith<$Res> {
  factory _$$InstallationImageItemImplCopyWith(
          _$InstallationImageItemImpl value,
          $Res Function(_$InstallationImageItemImpl) then) =
      __$$InstallationImageItemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String code,
      bool active,
      String description,
      @JsonKey(name: 'required_count') int requiredCount});
}

/// @nodoc
class __$$InstallationImageItemImplCopyWithImpl<$Res>
    extends _$InstallationImageItemCopyWithImpl<$Res,
        _$InstallationImageItemImpl>
    implements _$$InstallationImageItemImplCopyWith<$Res> {
  __$$InstallationImageItemImplCopyWithImpl(_$InstallationImageItemImpl _value,
      $Res Function(_$InstallationImageItemImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? active = null,
    Object? description = null,
    Object? requiredCount = null,
  }) {
    return _then(_$InstallationImageItemImpl(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      requiredCount: null == requiredCount
          ? _value.requiredCount
          : requiredCount // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$InstallationImageItemImpl extends _InstallationImageItem {
  const _$InstallationImageItemImpl(
      {required this.code,
      required this.active,
      required this.description,
      @JsonKey(name: 'required_count') required this.requiredCount})
      : super._();

  factory _$InstallationImageItemImpl.fromJson(Map<String, dynamic> json) =>
      _$$InstallationImageItemImplFromJson(json);

  @override
  final String code;
  @override
  final bool active;
  @override
  final String description;
  @override
  @JsonKey(name: 'required_count')
  final int requiredCount;

  @override
  String toString() {
    return 'InstallationImageItem(code: $code, active: $active, description: $description, requiredCount: $requiredCount)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InstallationImageItemImpl &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.active, active) || other.active == active) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.requiredCount, requiredCount) ||
                other.requiredCount == requiredCount));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode =>
      Object.hash(runtimeType, code, active, description, requiredCount);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$InstallationImageItemImplCopyWith<_$InstallationImageItemImpl>
      get copyWith => __$$InstallationImageItemImplCopyWithImpl<
          _$InstallationImageItemImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$InstallationImageItemImplToJson(
      this,
    );
  }
}

abstract class _InstallationImageItem extends InstallationImageItem {
  const factory _InstallationImageItem(
          {required final String code,
          required final bool active,
          required final String description,
          @JsonKey(name: 'required_count') required final int requiredCount}) =
      _$InstallationImageItemImpl;
  const _InstallationImageItem._() : super._();

  factory _InstallationImageItem.fromJson(Map<String, dynamic> json) =
      _$InstallationImageItemImpl.fromJson;

  @override
  String get code;
  @override
  bool get active;
  @override
  String get description;
  @override
  @JsonKey(name: 'required_count')
  int get requiredCount;
  @override
  @JsonKey(ignore: true)
  _$$InstallationImageItemImplCopyWith<_$InstallationImageItemImpl>
      get copyWith => throw _privateConstructorUsedError;
}

InstallationImagesData _$InstallationImagesDataFromJson(
    Map<String, dynamic> json) {
  return _InstallationImagesData.fromJson(json);
}

/// @nodoc
mixin _$InstallationImagesData {
  int get id => throw _privateConstructorUsedError;
  String get tenantId => throw _privateConstructorUsedError;
  @JsonKey(name: 'InstallationImage')
  List<InstallationImageItem> get installationImage =>
      throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $InstallationImagesDataCopyWith<InstallationImagesData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InstallationImagesDataCopyWith<$Res> {
  factory $InstallationImagesDataCopyWith(InstallationImagesData value,
          $Res Function(InstallationImagesData) then) =
      _$InstallationImagesDataCopyWithImpl<$Res, InstallationImagesData>;
  @useResult
  $Res call(
      {int id,
      String tenantId,
      @JsonKey(name: 'InstallationImage')
      List<InstallationImageItem> installationImage});
}

/// @nodoc
class _$InstallationImagesDataCopyWithImpl<$Res,
        $Val extends InstallationImagesData>
    implements $InstallationImagesDataCopyWith<$Res> {
  _$InstallationImagesDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? tenantId = null,
    Object? installationImage = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
      installationImage: null == installationImage
          ? _value.installationImage
          : installationImage // ignore: cast_nullable_to_non_nullable
              as List<InstallationImageItem>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$InstallationImagesDataImplCopyWith<$Res>
    implements $InstallationImagesDataCopyWith<$Res> {
  factory _$$InstallationImagesDataImplCopyWith(
          _$InstallationImagesDataImpl value,
          $Res Function(_$InstallationImagesDataImpl) then) =
      __$$InstallationImagesDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int id,
      String tenantId,
      @JsonKey(name: 'InstallationImage')
      List<InstallationImageItem> installationImage});
}

/// @nodoc
class __$$InstallationImagesDataImplCopyWithImpl<$Res>
    extends _$InstallationImagesDataCopyWithImpl<$Res,
        _$InstallationImagesDataImpl>
    implements _$$InstallationImagesDataImplCopyWith<$Res> {
  __$$InstallationImagesDataImplCopyWithImpl(
      _$InstallationImagesDataImpl _value,
      $Res Function(_$InstallationImagesDataImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? tenantId = null,
    Object? installationImage = null,
  }) {
    return _then(_$InstallationImagesDataImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
      installationImage: null == installationImage
          ? _value._installationImage
          : installationImage // ignore: cast_nullable_to_non_nullable
              as List<InstallationImageItem>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$InstallationImagesDataImpl implements _InstallationImagesData {
  const _$InstallationImagesDataImpl(
      {required this.id,
      required this.tenantId,
      @JsonKey(name: 'InstallationImage')
      required final List<InstallationImageItem> installationImage})
      : _installationImage = installationImage;

  factory _$InstallationImagesDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$InstallationImagesDataImplFromJson(json);

  @override
  final int id;
  @override
  final String tenantId;
  final List<InstallationImageItem> _installationImage;
  @override
  @JsonKey(name: 'InstallationImage')
  List<InstallationImageItem> get installationImage {
    if (_installationImage is EqualUnmodifiableListView)
      return _installationImage;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_installationImage);
  }

  @override
  String toString() {
    return 'InstallationImagesData(id: $id, tenantId: $tenantId, installationImage: $installationImage)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InstallationImagesDataImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            const DeepCollectionEquality()
                .equals(other._installationImage, _installationImage));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, id, tenantId,
      const DeepCollectionEquality().hash(_installationImage));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$InstallationImagesDataImplCopyWith<_$InstallationImagesDataImpl>
      get copyWith => __$$InstallationImagesDataImplCopyWithImpl<
          _$InstallationImagesDataImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$InstallationImagesDataImplToJson(
      this,
    );
  }
}

abstract class _InstallationImagesData implements InstallationImagesData {
  const factory _InstallationImagesData(
          {required final int id,
          required final String tenantId,
          @JsonKey(name: 'InstallationImage')
          required final List<InstallationImageItem> installationImage}) =
      _$InstallationImagesDataImpl;

  factory _InstallationImagesData.fromJson(Map<String, dynamic> json) =
      _$InstallationImagesDataImpl.fromJson;

  @override
  int get id;
  @override
  String get tenantId;
  @override
  @JsonKey(name: 'InstallationImage')
  List<InstallationImageItem> get installationImage;
  @override
  @JsonKey(ignore: true)
  _$$InstallationImagesDataImplCopyWith<_$InstallationImagesDataImpl>
      get copyWith => throw _privateConstructorUsedError;
}
