// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'asset_type.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

AssetType _$AssetTypeFromJson(Map<String, dynamic> json) {
  return _AssetType.fromJson(json);
}

/// @nodoc
mixin _$AssetType {
  String get code => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  bool get active => throw _privateConstructorUsedError;
  @JsonKey(name: 'form_fields')
  List<FormField> get formFields => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AssetTypeCopyWith<AssetType> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetTypeCopyWith<$Res> {
  factory $AssetTypeCopyWith(AssetType value, $Res Function(AssetType) then) =
      _$AssetTypeCopyWithImpl<$Res, AssetType>;
  @useResult
  $Res call(
      {String code,
      String name,
      bool active,
      @JsonKey(name: 'form_fields') List<FormField> formFields});
}

/// @nodoc
class _$AssetTypeCopyWithImpl<$Res, $Val extends AssetType>
    implements $AssetTypeCopyWith<$Res> {
  _$AssetTypeCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? name = null,
    Object? active = null,
    Object? formFields = null,
  }) {
    return _then(_value.copyWith(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      formFields: null == formFields
          ? _value.formFields
          : formFields // ignore: cast_nullable_to_non_nullable
              as List<FormField>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AssetTypeImplCopyWith<$Res>
    implements $AssetTypeCopyWith<$Res> {
  factory _$$AssetTypeImplCopyWith(
          _$AssetTypeImpl value, $Res Function(_$AssetTypeImpl) then) =
      __$$AssetTypeImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String code,
      String name,
      bool active,
      @JsonKey(name: 'form_fields') List<FormField> formFields});
}

/// @nodoc
class __$$AssetTypeImplCopyWithImpl<$Res>
    extends _$AssetTypeCopyWithImpl<$Res, _$AssetTypeImpl>
    implements _$$AssetTypeImplCopyWith<$Res> {
  __$$AssetTypeImplCopyWithImpl(
      _$AssetTypeImpl _value, $Res Function(_$AssetTypeImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? name = null,
    Object? active = null,
    Object? formFields = null,
  }) {
    return _then(_$AssetTypeImpl(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      formFields: null == formFields
          ? _value._formFields
          : formFields // ignore: cast_nullable_to_non_nullable
              as List<FormField>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AssetTypeImpl implements _AssetType {
  const _$AssetTypeImpl(
      {required this.code,
      required this.name,
      required this.active,
      @JsonKey(name: 'form_fields') required final List<FormField> formFields})
      : _formFields = formFields;

  factory _$AssetTypeImpl.fromJson(Map<String, dynamic> json) =>
      _$$AssetTypeImplFromJson(json);

  @override
  final String code;
  @override
  final String name;
  @override
  final bool active;
  final List<FormField> _formFields;
  @override
  @JsonKey(name: 'form_fields')
  List<FormField> get formFields {
    if (_formFields is EqualUnmodifiableListView) return _formFields;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_formFields);
  }

  @override
  String toString() {
    return 'AssetType(code: $code, name: $name, active: $active, formFields: $formFields)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AssetTypeImpl &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.active, active) || other.active == active) &&
            const DeepCollectionEquality()
                .equals(other._formFields, _formFields));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, code, name, active,
      const DeepCollectionEquality().hash(_formFields));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AssetTypeImplCopyWith<_$AssetTypeImpl> get copyWith =>
      __$$AssetTypeImplCopyWithImpl<_$AssetTypeImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AssetTypeImplToJson(
      this,
    );
  }
}

abstract class _AssetType implements AssetType {
  const factory _AssetType(
      {required final String code,
      required final String name,
      required final bool active,
      @JsonKey(name: 'form_fields')
      required final List<FormField> formFields}) = _$AssetTypeImpl;

  factory _AssetType.fromJson(Map<String, dynamic> json) =
      _$AssetTypeImpl.fromJson;

  @override
  String get code;
  @override
  String get name;
  @override
  bool get active;
  @override
  @JsonKey(name: 'form_fields')
  List<FormField> get formFields;
  @override
  @JsonKey(ignore: true)
  _$$AssetTypeImplCopyWith<_$AssetTypeImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

FormField _$FormFieldFromJson(Map<String, dynamic> json) {
  return _FormField.fromJson(json);
}

/// @nodoc
mixin _$FormField {
  String? get key => throw _privateConstructorUsedError;
  String? get name => throw _privateConstructorUsedError;
  String? get system => throw _privateConstructorUsedError;
  List<String>? get options => throw _privateConstructorUsedError;
  List<String>? get types => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $FormFieldCopyWith<FormField> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $FormFieldCopyWith<$Res> {
  factory $FormFieldCopyWith(FormField value, $Res Function(FormField) then) =
      _$FormFieldCopyWithImpl<$Res, FormField>;
  @useResult
  $Res call(
      {String? key,
      String? name,
      String? system,
      List<String>? options,
      List<String>? types});
}

/// @nodoc
class _$FormFieldCopyWithImpl<$Res, $Val extends FormField>
    implements $FormFieldCopyWith<$Res> {
  _$FormFieldCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? key = freezed,
    Object? name = freezed,
    Object? system = freezed,
    Object? options = freezed,
    Object? types = freezed,
  }) {
    return _then(_value.copyWith(
      key: freezed == key
          ? _value.key
          : key // ignore: cast_nullable_to_non_nullable
              as String?,
      name: freezed == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String?,
      system: freezed == system
          ? _value.system
          : system // ignore: cast_nullable_to_non_nullable
              as String?,
      options: freezed == options
          ? _value.options
          : options // ignore: cast_nullable_to_non_nullable
              as List<String>?,
      types: freezed == types
          ? _value.types
          : types // ignore: cast_nullable_to_non_nullable
              as List<String>?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$FormFieldImplCopyWith<$Res>
    implements $FormFieldCopyWith<$Res> {
  factory _$$FormFieldImplCopyWith(
          _$FormFieldImpl value, $Res Function(_$FormFieldImpl) then) =
      __$$FormFieldImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? key,
      String? name,
      String? system,
      List<String>? options,
      List<String>? types});
}

/// @nodoc
class __$$FormFieldImplCopyWithImpl<$Res>
    extends _$FormFieldCopyWithImpl<$Res, _$FormFieldImpl>
    implements _$$FormFieldImplCopyWith<$Res> {
  __$$FormFieldImplCopyWithImpl(
      _$FormFieldImpl _value, $Res Function(_$FormFieldImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? key = freezed,
    Object? name = freezed,
    Object? system = freezed,
    Object? options = freezed,
    Object? types = freezed,
  }) {
    return _then(_$FormFieldImpl(
      key: freezed == key
          ? _value.key
          : key // ignore: cast_nullable_to_non_nullable
              as String?,
      name: freezed == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String?,
      system: freezed == system
          ? _value.system
          : system // ignore: cast_nullable_to_non_nullable
              as String?,
      options: freezed == options
          ? _value._options
          : options // ignore: cast_nullable_to_non_nullable
              as List<String>?,
      types: freezed == types
          ? _value._types
          : types // ignore: cast_nullable_to_non_nullable
              as List<String>?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$FormFieldImpl implements _FormField {
  const _$FormFieldImpl(
      {this.key,
      this.name,
      this.system,
      final List<String>? options,
      final List<String>? types})
      : _options = options,
        _types = types;

  factory _$FormFieldImpl.fromJson(Map<String, dynamic> json) =>
      _$$FormFieldImplFromJson(json);

  @override
  final String? key;
  @override
  final String? name;
  @override
  final String? system;
  final List<String>? _options;
  @override
  List<String>? get options {
    final value = _options;
    if (value == null) return null;
    if (_options is EqualUnmodifiableListView) return _options;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(value);
  }

  final List<String>? _types;
  @override
  List<String>? get types {
    final value = _types;
    if (value == null) return null;
    if (_types is EqualUnmodifiableListView) return _types;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(value);
  }

  @override
  String toString() {
    return 'FormField(key: $key, name: $name, system: $system, options: $options, types: $types)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FormFieldImpl &&
            (identical(other.key, key) || other.key == key) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.system, system) || other.system == system) &&
            const DeepCollectionEquality().equals(other._options, _options) &&
            const DeepCollectionEquality().equals(other._types, _types));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      key,
      name,
      system,
      const DeepCollectionEquality().hash(_options),
      const DeepCollectionEquality().hash(_types));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FormFieldImplCopyWith<_$FormFieldImpl> get copyWith =>
      __$$FormFieldImplCopyWithImpl<_$FormFieldImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$FormFieldImplToJson(
      this,
    );
  }
}

abstract class _FormField implements FormField {
  const factory _FormField(
      {final String? key,
      final String? name,
      final String? system,
      final List<String>? options,
      final List<String>? types}) = _$FormFieldImpl;

  factory _FormField.fromJson(Map<String, dynamic> json) =
      _$FormFieldImpl.fromJson;

  @override
  String? get key;
  @override
  String? get name;
  @override
  String? get system;
  @override
  List<String>? get options;
  @override
  List<String>? get types;
  @override
  @JsonKey(ignore: true)
  _$$FormFieldImplCopyWith<_$FormFieldImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AssetTypeData _$AssetTypeDataFromJson(Map<String, dynamic> json) {
  return _AssetTypeData.fromJson(json);
}

/// @nodoc
mixin _$AssetTypeData {
  int get id => throw _privateConstructorUsedError;
  String get module => throw _privateConstructorUsedError;
  String get tenantId => throw _privateConstructorUsedError;
  @JsonKey(name: 'AssetType')
  List<AssetType> get assetType => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AssetTypeDataCopyWith<AssetTypeData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetTypeDataCopyWith<$Res> {
  factory $AssetTypeDataCopyWith(
          AssetTypeData value, $Res Function(AssetTypeData) then) =
      _$AssetTypeDataCopyWithImpl<$Res, AssetTypeData>;
  @useResult
  $Res call(
      {int id,
      String module,
      String tenantId,
      @JsonKey(name: 'AssetType') List<AssetType> assetType});
}

/// @nodoc
class _$AssetTypeDataCopyWithImpl<$Res, $Val extends AssetTypeData>
    implements $AssetTypeDataCopyWith<$Res> {
  _$AssetTypeDataCopyWithImpl(this._value, this._then);

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
    Object? assetType = null,
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
      assetType: null == assetType
          ? _value.assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as List<AssetType>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AssetTypeDataImplCopyWith<$Res>
    implements $AssetTypeDataCopyWith<$Res> {
  factory _$$AssetTypeDataImplCopyWith(
          _$AssetTypeDataImpl value, $Res Function(_$AssetTypeDataImpl) then) =
      __$$AssetTypeDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int id,
      String module,
      String tenantId,
      @JsonKey(name: 'AssetType') List<AssetType> assetType});
}

/// @nodoc
class __$$AssetTypeDataImplCopyWithImpl<$Res>
    extends _$AssetTypeDataCopyWithImpl<$Res, _$AssetTypeDataImpl>
    implements _$$AssetTypeDataImplCopyWith<$Res> {
  __$$AssetTypeDataImplCopyWithImpl(
      _$AssetTypeDataImpl _value, $Res Function(_$AssetTypeDataImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? module = null,
    Object? tenantId = null,
    Object? assetType = null,
  }) {
    return _then(_$AssetTypeDataImpl(
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
      assetType: null == assetType
          ? _value._assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as List<AssetType>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AssetTypeDataImpl implements _AssetTypeData {
  const _$AssetTypeDataImpl(
      {required this.id,
      required this.module,
      required this.tenantId,
      @JsonKey(name: 'AssetType') required final List<AssetType> assetType})
      : _assetType = assetType;

  factory _$AssetTypeDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$AssetTypeDataImplFromJson(json);

  @override
  final int id;
  @override
  final String module;
  @override
  final String tenantId;
  final List<AssetType> _assetType;
  @override
  @JsonKey(name: 'AssetType')
  List<AssetType> get assetType {
    if (_assetType is EqualUnmodifiableListView) return _assetType;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assetType);
  }

  @override
  String toString() {
    return 'AssetTypeData(id: $id, module: $module, tenantId: $tenantId, assetType: $assetType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AssetTypeDataImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.module, module) || other.module == module) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            const DeepCollectionEquality()
                .equals(other._assetType, _assetType));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, id, module, tenantId,
      const DeepCollectionEquality().hash(_assetType));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AssetTypeDataImplCopyWith<_$AssetTypeDataImpl> get copyWith =>
      __$$AssetTypeDataImplCopyWithImpl<_$AssetTypeDataImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AssetTypeDataImplToJson(
      this,
    );
  }
}

abstract class _AssetTypeData implements AssetTypeData {
  const factory _AssetTypeData(
      {required final int id,
      required final String module,
      required final String tenantId,
      @JsonKey(name: 'AssetType')
      required final List<AssetType> assetType}) = _$AssetTypeDataImpl;

  factory _AssetTypeData.fromJson(Map<String, dynamic> json) =
      _$AssetTypeDataImpl.fromJson;

  @override
  int get id;
  @override
  String get module;
  @override
  String get tenantId;
  @override
  @JsonKey(name: 'AssetType')
  List<AssetType> get assetType;
  @override
  @JsonKey(ignore: true)
  _$$AssetTypeDataImplCopyWith<_$AssetTypeDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
