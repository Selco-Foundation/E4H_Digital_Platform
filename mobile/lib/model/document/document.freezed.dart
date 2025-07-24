// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'document.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

GeoLocation _$GeoLocationFromJson(Map<String, dynamic> json) {
  return _GeoLocation.fromJson(json);
}

/// @nodoc
mixin _$GeoLocation {
  @JsonKey(fromJson: _anyToString)
  String? get latitude => throw _privateConstructorUsedError;
  @JsonKey(fromJson: _anyToString)
  String? get longitude => throw _privateConstructorUsedError;
  Map<String, dynamic>? get additionalDetails =>
      throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $GeoLocationCopyWith<GeoLocation> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GeoLocationCopyWith<$Res> {
  factory $GeoLocationCopyWith(
          GeoLocation value, $Res Function(GeoLocation) then) =
      _$GeoLocationCopyWithImpl<$Res, GeoLocation>;
  @useResult
  $Res call(
      {@JsonKey(fromJson: _anyToString) String? latitude,
      @JsonKey(fromJson: _anyToString) String? longitude,
      Map<String, dynamic>? additionalDetails});
}

/// @nodoc
class _$GeoLocationCopyWithImpl<$Res, $Val extends GeoLocation>
    implements $GeoLocationCopyWith<$Res> {
  _$GeoLocationCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? latitude = freezed,
    Object? longitude = freezed,
    Object? additionalDetails = freezed,
  }) {
    return _then(_value.copyWith(
      latitude: freezed == latitude
          ? _value.latitude
          : latitude // ignore: cast_nullable_to_non_nullable
              as String?,
      longitude: freezed == longitude
          ? _value.longitude
          : longitude // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value.additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$GeoLocationImplCopyWith<$Res>
    implements $GeoLocationCopyWith<$Res> {
  factory _$$GeoLocationImplCopyWith(
          _$GeoLocationImpl value, $Res Function(_$GeoLocationImpl) then) =
      __$$GeoLocationImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {@JsonKey(fromJson: _anyToString) String? latitude,
      @JsonKey(fromJson: _anyToString) String? longitude,
      Map<String, dynamic>? additionalDetails});
}

/// @nodoc
class __$$GeoLocationImplCopyWithImpl<$Res>
    extends _$GeoLocationCopyWithImpl<$Res, _$GeoLocationImpl>
    implements _$$GeoLocationImplCopyWith<$Res> {
  __$$GeoLocationImplCopyWithImpl(
      _$GeoLocationImpl _value, $Res Function(_$GeoLocationImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? latitude = freezed,
    Object? longitude = freezed,
    Object? additionalDetails = freezed,
  }) {
    return _then(_$GeoLocationImpl(
      latitude: freezed == latitude
          ? _value.latitude
          : latitude // ignore: cast_nullable_to_non_nullable
              as String?,
      longitude: freezed == longitude
          ? _value.longitude
          : longitude // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value._additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$GeoLocationImpl implements _GeoLocation {
  const _$GeoLocationImpl(
      {@JsonKey(fromJson: _anyToString) this.latitude,
      @JsonKey(fromJson: _anyToString) this.longitude,
      final Map<String, dynamic>? additionalDetails})
      : _additionalDetails = additionalDetails;

  factory _$GeoLocationImpl.fromJson(Map<String, dynamic> json) =>
      _$$GeoLocationImplFromJson(json);

  @override
  @JsonKey(fromJson: _anyToString)
  final String? latitude;
  @override
  @JsonKey(fromJson: _anyToString)
  final String? longitude;
  final Map<String, dynamic>? _additionalDetails;
  @override
  Map<String, dynamic>? get additionalDetails {
    final value = _additionalDetails;
    if (value == null) return null;
    if (_additionalDetails is EqualUnmodifiableMapView)
      return _additionalDetails;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  @override
  String toString() {
    return 'GeoLocation(latitude: $latitude, longitude: $longitude, additionalDetails: $additionalDetails)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GeoLocationImpl &&
            (identical(other.latitude, latitude) ||
                other.latitude == latitude) &&
            (identical(other.longitude, longitude) ||
                other.longitude == longitude) &&
            const DeepCollectionEquality()
                .equals(other._additionalDetails, _additionalDetails));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, latitude, longitude,
      const DeepCollectionEquality().hash(_additionalDetails));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$GeoLocationImplCopyWith<_$GeoLocationImpl> get copyWith =>
      __$$GeoLocationImplCopyWithImpl<_$GeoLocationImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$GeoLocationImplToJson(
      this,
    );
  }
}

abstract class _GeoLocation implements GeoLocation {
  const factory _GeoLocation(
      {@JsonKey(fromJson: _anyToString) final String? latitude,
      @JsonKey(fromJson: _anyToString) final String? longitude,
      final Map<String, dynamic>? additionalDetails}) = _$GeoLocationImpl;

  factory _GeoLocation.fromJson(Map<String, dynamic> json) =
      _$GeoLocationImpl.fromJson;

  @override
  @JsonKey(fromJson: _anyToString)
  String? get latitude;
  @override
  @JsonKey(fromJson: _anyToString)
  String? get longitude;
  @override
  Map<String, dynamic>? get additionalDetails;
  @override
  @JsonKey(ignore: true)
  _$$GeoLocationImplCopyWith<_$GeoLocationImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$Document {
  @JsonKey(name: 'id', fromJson: _anyToString)
  String? get id => throw _privateConstructorUsedError;
  @JsonKey(name: 'documentType', fromJson: _anyToString)
  String? get documentType => throw _privateConstructorUsedError;

  /// We'll store fileStore from either `fileStore` or `fileStoreId`
  String? get fileStore => throw _privateConstructorUsedError;
  @JsonKey(name: 'documentUid', fromJson: _anyToString)
  String? get documentUid => throw _privateConstructorUsedError;
  Map<String, dynamic>? get additionalDetails =>
      throw _privateConstructorUsedError;
  @JsonKey(name: 'geoLocation')
  GeoLocation? get geoLocation => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $DocumentCopyWith<Document> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DocumentCopyWith<$Res> {
  factory $DocumentCopyWith(Document value, $Res Function(Document) then) =
      _$DocumentCopyWithImpl<$Res, Document>;
  @useResult
  $Res call(
      {@JsonKey(name: 'id', fromJson: _anyToString) String? id,
      @JsonKey(name: 'documentType', fromJson: _anyToString)
      String? documentType,
      String? fileStore,
      @JsonKey(name: 'documentUid', fromJson: _anyToString) String? documentUid,
      Map<String, dynamic>? additionalDetails,
      @JsonKey(name: 'geoLocation') GeoLocation? geoLocation});

  $GeoLocationCopyWith<$Res>? get geoLocation;
}

/// @nodoc
class _$DocumentCopyWithImpl<$Res, $Val extends Document>
    implements $DocumentCopyWith<$Res> {
  _$DocumentCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? documentType = freezed,
    Object? fileStore = freezed,
    Object? documentUid = freezed,
    Object? additionalDetails = freezed,
    Object? geoLocation = freezed,
  }) {
    return _then(_value.copyWith(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      documentType: freezed == documentType
          ? _value.documentType
          : documentType // ignore: cast_nullable_to_non_nullable
              as String?,
      fileStore: freezed == fileStore
          ? _value.fileStore
          : fileStore // ignore: cast_nullable_to_non_nullable
              as String?,
      documentUid: freezed == documentUid
          ? _value.documentUid
          : documentUid // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value.additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      geoLocation: freezed == geoLocation
          ? _value.geoLocation
          : geoLocation // ignore: cast_nullable_to_non_nullable
              as GeoLocation?,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $GeoLocationCopyWith<$Res>? get geoLocation {
    if (_value.geoLocation == null) {
      return null;
    }

    return $GeoLocationCopyWith<$Res>(_value.geoLocation!, (value) {
      return _then(_value.copyWith(geoLocation: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$DocumentImplCopyWith<$Res>
    implements $DocumentCopyWith<$Res> {
  factory _$$DocumentImplCopyWith(
          _$DocumentImpl value, $Res Function(_$DocumentImpl) then) =
      __$$DocumentImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {@JsonKey(name: 'id', fromJson: _anyToString) String? id,
      @JsonKey(name: 'documentType', fromJson: _anyToString)
      String? documentType,
      String? fileStore,
      @JsonKey(name: 'documentUid', fromJson: _anyToString) String? documentUid,
      Map<String, dynamic>? additionalDetails,
      @JsonKey(name: 'geoLocation') GeoLocation? geoLocation});

  @override
  $GeoLocationCopyWith<$Res>? get geoLocation;
}

/// @nodoc
class __$$DocumentImplCopyWithImpl<$Res>
    extends _$DocumentCopyWithImpl<$Res, _$DocumentImpl>
    implements _$$DocumentImplCopyWith<$Res> {
  __$$DocumentImplCopyWithImpl(
      _$DocumentImpl _value, $Res Function(_$DocumentImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? documentType = freezed,
    Object? fileStore = freezed,
    Object? documentUid = freezed,
    Object? additionalDetails = freezed,
    Object? geoLocation = freezed,
  }) {
    return _then(_$DocumentImpl(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      documentType: freezed == documentType
          ? _value.documentType
          : documentType // ignore: cast_nullable_to_non_nullable
              as String?,
      fileStore: freezed == fileStore
          ? _value.fileStore
          : fileStore // ignore: cast_nullable_to_non_nullable
              as String?,
      documentUid: freezed == documentUid
          ? _value.documentUid
          : documentUid // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value._additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      geoLocation: freezed == geoLocation
          ? _value.geoLocation
          : geoLocation // ignore: cast_nullable_to_non_nullable
              as GeoLocation?,
    ));
  }
}

/// @nodoc

class _$DocumentImpl extends _Document {
  const _$DocumentImpl(
      {@JsonKey(name: 'id', fromJson: _anyToString) this.id,
      @JsonKey(name: 'documentType', fromJson: _anyToString) this.documentType,
      this.fileStore,
      @JsonKey(name: 'documentUid', fromJson: _anyToString) this.documentUid,
      final Map<String, dynamic>? additionalDetails,
      @JsonKey(name: 'geoLocation') this.geoLocation})
      : _additionalDetails = additionalDetails,
        super._();

  @override
  @JsonKey(name: 'id', fromJson: _anyToString)
  final String? id;
  @override
  @JsonKey(name: 'documentType', fromJson: _anyToString)
  final String? documentType;

  /// We'll store fileStore from either `fileStore` or `fileStoreId`
  @override
  final String? fileStore;
  @override
  @JsonKey(name: 'documentUid', fromJson: _anyToString)
  final String? documentUid;
  final Map<String, dynamic>? _additionalDetails;
  @override
  Map<String, dynamic>? get additionalDetails {
    final value = _additionalDetails;
    if (value == null) return null;
    if (_additionalDetails is EqualUnmodifiableMapView)
      return _additionalDetails;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  @override
  @JsonKey(name: 'geoLocation')
  final GeoLocation? geoLocation;

  @override
  String toString() {
    return 'Document(id: $id, documentType: $documentType, fileStore: $fileStore, documentUid: $documentUid, additionalDetails: $additionalDetails, geoLocation: $geoLocation)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DocumentImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.documentType, documentType) ||
                other.documentType == documentType) &&
            (identical(other.fileStore, fileStore) ||
                other.fileStore == fileStore) &&
            (identical(other.documentUid, documentUid) ||
                other.documentUid == documentUid) &&
            const DeepCollectionEquality()
                .equals(other._additionalDetails, _additionalDetails) &&
            (identical(other.geoLocation, geoLocation) ||
                other.geoLocation == geoLocation));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      documentType,
      fileStore,
      documentUid,
      const DeepCollectionEquality().hash(_additionalDetails),
      geoLocation);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DocumentImplCopyWith<_$DocumentImpl> get copyWith =>
      __$$DocumentImplCopyWithImpl<_$DocumentImpl>(this, _$identity);
}

abstract class _Document extends Document {
  const factory _Document(
          {@JsonKey(name: 'id', fromJson: _anyToString) final String? id,
          @JsonKey(name: 'documentType', fromJson: _anyToString)
          final String? documentType,
          final String? fileStore,
          @JsonKey(name: 'documentUid', fromJson: _anyToString)
          final String? documentUid,
          final Map<String, dynamic>? additionalDetails,
          @JsonKey(name: 'geoLocation') final GeoLocation? geoLocation}) =
      _$DocumentImpl;
  const _Document._() : super._();

  @override
  @JsonKey(name: 'id', fromJson: _anyToString)
  String? get id;
  @override
  @JsonKey(name: 'documentType', fromJson: _anyToString)
  String? get documentType;
  @override

  /// We'll store fileStore from either `fileStore` or `fileStoreId`
  String? get fileStore;
  @override
  @JsonKey(name: 'documentUid', fromJson: _anyToString)
  String? get documentUid;
  @override
  Map<String, dynamic>? get additionalDetails;
  @override
  @JsonKey(name: 'geoLocation')
  GeoLocation? get geoLocation;
  @override
  @JsonKey(ignore: true)
  _$$DocumentImplCopyWith<_$DocumentImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
