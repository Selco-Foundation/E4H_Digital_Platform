// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'asset.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

AssetDetails _$AssetDetailsFromJson(Map<String, dynamic> json) {
  return _AssetDetails.fromJson(json);
}

/// @nodoc
mixin _$AssetDetails {
  @JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
  double? get totalCapacity => throw _privateConstructorUsedError;
  @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
  String? get totalCapacityUnit => throw _privateConstructorUsedError;
  @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
  String? get totalCapacityUOM => throw _privateConstructorUsedError;
  @JsonKey(name: 'capacityUnit', fromJson: _anyToString)
  String? get capacityUnit => throw _privateConstructorUsedError;
  @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
  double? get panelCapacity => throw _privateConstructorUsedError;
  @JsonKey(name: 'batteryType', fromJson: _anyToString)
  String? get batteryType => throw _privateConstructorUsedError;
  @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
  double? get batteryVoltage => throw _privateConstructorUsedError;
  @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
  double? get batteryCapacity => throw _privateConstructorUsedError;
  @JsonKey(name: 'voltageUnit', fromJson: _anyToString)
  String? get voltageUnit => throw _privateConstructorUsedError;
  @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
  double? get inverterCapacity => throw _privateConstructorUsedError;
  @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
  String? get inverterCapacityUnit => throw _privateConstructorUsedError;
  @JsonKey(name: 'currentUnit', fromJson: _anyToString)
  String? get currentUnit => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AssetDetailsCopyWith<AssetDetails> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetDetailsCopyWith<$Res> {
  factory $AssetDetailsCopyWith(
          AssetDetails value, $Res Function(AssetDetails) then) =
      _$AssetDetailsCopyWithImpl<$Res, AssetDetails>;
  @useResult
  $Res call(
      {@JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
      double? totalCapacity,
      @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
      String? totalCapacityUnit,
      @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
      String? totalCapacityUOM,
      @JsonKey(name: 'capacityUnit', fromJson: _anyToString)
      String? capacityUnit,
      @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
      double? panelCapacity,
      @JsonKey(name: 'batteryType', fromJson: _anyToString) String? batteryType,
      @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
      double? batteryVoltage,
      @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
      double? batteryCapacity,
      @JsonKey(name: 'voltageUnit', fromJson: _anyToString) String? voltageUnit,
      @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
      double? inverterCapacity,
      @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
      String? inverterCapacityUnit,
      @JsonKey(name: 'currentUnit', fromJson: _anyToString)
      String? currentUnit});
}

/// @nodoc
class _$AssetDetailsCopyWithImpl<$Res, $Val extends AssetDetails>
    implements $AssetDetailsCopyWith<$Res> {
  _$AssetDetailsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? totalCapacity = freezed,
    Object? totalCapacityUnit = freezed,
    Object? totalCapacityUOM = freezed,
    Object? capacityUnit = freezed,
    Object? panelCapacity = freezed,
    Object? batteryType = freezed,
    Object? batteryVoltage = freezed,
    Object? batteryCapacity = freezed,
    Object? voltageUnit = freezed,
    Object? inverterCapacity = freezed,
    Object? inverterCapacityUnit = freezed,
    Object? currentUnit = freezed,
  }) {
    return _then(_value.copyWith(
      totalCapacity: freezed == totalCapacity
          ? _value.totalCapacity
          : totalCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      totalCapacityUnit: freezed == totalCapacityUnit
          ? _value.totalCapacityUnit
          : totalCapacityUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      totalCapacityUOM: freezed == totalCapacityUOM
          ? _value.totalCapacityUOM
          : totalCapacityUOM // ignore: cast_nullable_to_non_nullable
              as String?,
      capacityUnit: freezed == capacityUnit
          ? _value.capacityUnit
          : capacityUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      panelCapacity: freezed == panelCapacity
          ? _value.panelCapacity
          : panelCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      batteryType: freezed == batteryType
          ? _value.batteryType
          : batteryType // ignore: cast_nullable_to_non_nullable
              as String?,
      batteryVoltage: freezed == batteryVoltage
          ? _value.batteryVoltage
          : batteryVoltage // ignore: cast_nullable_to_non_nullable
              as double?,
      batteryCapacity: freezed == batteryCapacity
          ? _value.batteryCapacity
          : batteryCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      voltageUnit: freezed == voltageUnit
          ? _value.voltageUnit
          : voltageUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      inverterCapacity: freezed == inverterCapacity
          ? _value.inverterCapacity
          : inverterCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      inverterCapacityUnit: freezed == inverterCapacityUnit
          ? _value.inverterCapacityUnit
          : inverterCapacityUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      currentUnit: freezed == currentUnit
          ? _value.currentUnit
          : currentUnit // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AssetDetailsImplCopyWith<$Res>
    implements $AssetDetailsCopyWith<$Res> {
  factory _$$AssetDetailsImplCopyWith(
          _$AssetDetailsImpl value, $Res Function(_$AssetDetailsImpl) then) =
      __$$AssetDetailsImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {@JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
      double? totalCapacity,
      @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
      String? totalCapacityUnit,
      @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
      String? totalCapacityUOM,
      @JsonKey(name: 'capacityUnit', fromJson: _anyToString)
      String? capacityUnit,
      @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
      double? panelCapacity,
      @JsonKey(name: 'batteryType', fromJson: _anyToString) String? batteryType,
      @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
      double? batteryVoltage,
      @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
      double? batteryCapacity,
      @JsonKey(name: 'voltageUnit', fromJson: _anyToString) String? voltageUnit,
      @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
      double? inverterCapacity,
      @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
      String? inverterCapacityUnit,
      @JsonKey(name: 'currentUnit', fromJson: _anyToString)
      String? currentUnit});
}

/// @nodoc
class __$$AssetDetailsImplCopyWithImpl<$Res>
    extends _$AssetDetailsCopyWithImpl<$Res, _$AssetDetailsImpl>
    implements _$$AssetDetailsImplCopyWith<$Res> {
  __$$AssetDetailsImplCopyWithImpl(
      _$AssetDetailsImpl _value, $Res Function(_$AssetDetailsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? totalCapacity = freezed,
    Object? totalCapacityUnit = freezed,
    Object? totalCapacityUOM = freezed,
    Object? capacityUnit = freezed,
    Object? panelCapacity = freezed,
    Object? batteryType = freezed,
    Object? batteryVoltage = freezed,
    Object? batteryCapacity = freezed,
    Object? voltageUnit = freezed,
    Object? inverterCapacity = freezed,
    Object? inverterCapacityUnit = freezed,
    Object? currentUnit = freezed,
  }) {
    return _then(_$AssetDetailsImpl(
      totalCapacity: freezed == totalCapacity
          ? _value.totalCapacity
          : totalCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      totalCapacityUnit: freezed == totalCapacityUnit
          ? _value.totalCapacityUnit
          : totalCapacityUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      totalCapacityUOM: freezed == totalCapacityUOM
          ? _value.totalCapacityUOM
          : totalCapacityUOM // ignore: cast_nullable_to_non_nullable
              as String?,
      capacityUnit: freezed == capacityUnit
          ? _value.capacityUnit
          : capacityUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      panelCapacity: freezed == panelCapacity
          ? _value.panelCapacity
          : panelCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      batteryType: freezed == batteryType
          ? _value.batteryType
          : batteryType // ignore: cast_nullable_to_non_nullable
              as String?,
      batteryVoltage: freezed == batteryVoltage
          ? _value.batteryVoltage
          : batteryVoltage // ignore: cast_nullable_to_non_nullable
              as double?,
      batteryCapacity: freezed == batteryCapacity
          ? _value.batteryCapacity
          : batteryCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      voltageUnit: freezed == voltageUnit
          ? _value.voltageUnit
          : voltageUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      inverterCapacity: freezed == inverterCapacity
          ? _value.inverterCapacity
          : inverterCapacity // ignore: cast_nullable_to_non_nullable
              as double?,
      inverterCapacityUnit: freezed == inverterCapacityUnit
          ? _value.inverterCapacityUnit
          : inverterCapacityUnit // ignore: cast_nullable_to_non_nullable
              as String?,
      currentUnit: freezed == currentUnit
          ? _value.currentUnit
          : currentUnit // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AssetDetailsImpl implements _AssetDetails {
  const _$AssetDetailsImpl(
      {@JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
      this.totalCapacity,
      @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
      this.totalCapacityUnit,
      @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
      this.totalCapacityUOM,
      @JsonKey(name: 'capacityUnit', fromJson: _anyToString) this.capacityUnit,
      @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
      this.panelCapacity,
      @JsonKey(name: 'batteryType', fromJson: _anyToString) this.batteryType,
      @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
      this.batteryVoltage,
      @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
      this.batteryCapacity,
      @JsonKey(name: 'voltageUnit', fromJson: _anyToString) this.voltageUnit,
      @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
      this.inverterCapacity,
      @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
      this.inverterCapacityUnit,
      @JsonKey(name: 'currentUnit', fromJson: _anyToString) this.currentUnit});

  factory _$AssetDetailsImpl.fromJson(Map<String, dynamic> json) =>
      _$$AssetDetailsImplFromJson(json);

  @override
  @JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
  final double? totalCapacity;
  @override
  @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
  final String? totalCapacityUnit;
  @override
  @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
  final String? totalCapacityUOM;
  @override
  @JsonKey(name: 'capacityUnit', fromJson: _anyToString)
  final String? capacityUnit;
  @override
  @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
  final double? panelCapacity;
  @override
  @JsonKey(name: 'batteryType', fromJson: _anyToString)
  final String? batteryType;
  @override
  @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
  final double? batteryVoltage;
  @override
  @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
  final double? batteryCapacity;
  @override
  @JsonKey(name: 'voltageUnit', fromJson: _anyToString)
  final String? voltageUnit;
  @override
  @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
  final double? inverterCapacity;
  @override
  @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
  final String? inverterCapacityUnit;
  @override
  @JsonKey(name: 'currentUnit', fromJson: _anyToString)
  final String? currentUnit;

  @override
  String toString() {
    return 'AssetDetails(totalCapacity: $totalCapacity, totalCapacityUnit: $totalCapacityUnit, totalCapacityUOM: $totalCapacityUOM, capacityUnit: $capacityUnit, panelCapacity: $panelCapacity, batteryType: $batteryType, batteryVoltage: $batteryVoltage, batteryCapacity: $batteryCapacity, voltageUnit: $voltageUnit, inverterCapacity: $inverterCapacity, inverterCapacityUnit: $inverterCapacityUnit, currentUnit: $currentUnit)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AssetDetailsImpl &&
            (identical(other.totalCapacity, totalCapacity) ||
                other.totalCapacity == totalCapacity) &&
            (identical(other.totalCapacityUnit, totalCapacityUnit) ||
                other.totalCapacityUnit == totalCapacityUnit) &&
            (identical(other.totalCapacityUOM, totalCapacityUOM) ||
                other.totalCapacityUOM == totalCapacityUOM) &&
            (identical(other.capacityUnit, capacityUnit) ||
                other.capacityUnit == capacityUnit) &&
            (identical(other.panelCapacity, panelCapacity) ||
                other.panelCapacity == panelCapacity) &&
            (identical(other.batteryType, batteryType) ||
                other.batteryType == batteryType) &&
            (identical(other.batteryVoltage, batteryVoltage) ||
                other.batteryVoltage == batteryVoltage) &&
            (identical(other.batteryCapacity, batteryCapacity) ||
                other.batteryCapacity == batteryCapacity) &&
            (identical(other.voltageUnit, voltageUnit) ||
                other.voltageUnit == voltageUnit) &&
            (identical(other.inverterCapacity, inverterCapacity) ||
                other.inverterCapacity == inverterCapacity) &&
            (identical(other.inverterCapacityUnit, inverterCapacityUnit) ||
                other.inverterCapacityUnit == inverterCapacityUnit) &&
            (identical(other.currentUnit, currentUnit) ||
                other.currentUnit == currentUnit));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      totalCapacity,
      totalCapacityUnit,
      totalCapacityUOM,
      capacityUnit,
      panelCapacity,
      batteryType,
      batteryVoltage,
      batteryCapacity,
      voltageUnit,
      inverterCapacity,
      inverterCapacityUnit,
      currentUnit);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AssetDetailsImplCopyWith<_$AssetDetailsImpl> get copyWith =>
      __$$AssetDetailsImplCopyWithImpl<_$AssetDetailsImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AssetDetailsImplToJson(
      this,
    );
  }
}

abstract class _AssetDetails implements AssetDetails {
  const factory _AssetDetails(
      {@JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
      final double? totalCapacity,
      @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
      final String? totalCapacityUnit,
      @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
      final String? totalCapacityUOM,
      @JsonKey(name: 'capacityUnit', fromJson: _anyToString)
      final String? capacityUnit,
      @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
      final double? panelCapacity,
      @JsonKey(name: 'batteryType', fromJson: _anyToString)
      final String? batteryType,
      @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
      final double? batteryVoltage,
      @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
      final double? batteryCapacity,
      @JsonKey(name: 'voltageUnit', fromJson: _anyToString)
      final String? voltageUnit,
      @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
      final double? inverterCapacity,
      @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
      final String? inverterCapacityUnit,
      @JsonKey(name: 'currentUnit', fromJson: _anyToString)
      final String? currentUnit}) = _$AssetDetailsImpl;

  factory _AssetDetails.fromJson(Map<String, dynamic> json) =
      _$AssetDetailsImpl.fromJson;

  @override
  @JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
  double? get totalCapacity;
  @override
  @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
  String? get totalCapacityUnit;
  @override
  @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
  String? get totalCapacityUOM;
  @override
  @JsonKey(name: 'capacityUnit', fromJson: _anyToString)
  String? get capacityUnit;
  @override
  @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
  double? get panelCapacity;
  @override
  @JsonKey(name: 'batteryType', fromJson: _anyToString)
  String? get batteryType;
  @override
  @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
  double? get batteryVoltage;
  @override
  @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
  double? get batteryCapacity;
  @override
  @JsonKey(name: 'voltageUnit', fromJson: _anyToString)
  String? get voltageUnit;
  @override
  @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
  double? get inverterCapacity;
  @override
  @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
  String? get inverterCapacityUnit;
  @override
  @JsonKey(name: 'currentUnit', fromJson: _anyToString)
  String? get currentUnit;
  @override
  @JsonKey(ignore: true)
  _$$AssetDetailsImplCopyWith<_$AssetDetailsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

Asset _$AssetFromJson(Map<String, dynamic> json) {
  return _Asset.fromJson(json);
}

/// @nodoc
mixin _$Asset {
  @JsonKey(name: 'assetId', fromJson: _anyToString)
  String? get assetId => throw _privateConstructorUsedError;
  @JsonKey(name: 'tenantId', fromJson: _anyToString)
  String? get tenantId => throw _privateConstructorUsedError;
  @JsonKey(name: 'activityFacilityId', fromJson: _anyToString)
  String? get activityFacilityID => throw _privateConstructorUsedError;
  @JsonKey(name: 'facilityID', fromJson: _anyToString)
  String? get facilityID => throw _privateConstructorUsedError;
  @JsonKey(name: 'system', fromJson: _anyToString)
  String? get system => throw _privateConstructorUsedError;
  @JsonKey(name: 'serialNumber', fromJson: _anyToString)
  String? get serialNumber => throw _privateConstructorUsedError;
  @JsonKey(name: 'assetTypeID', fromJson: _anyToString)
  String? get assetTypeID => throw _privateConstructorUsedError;
  AssetDetails? get assetDetails => throw _privateConstructorUsedError;
  @JsonKey(name: 'brandID', fromJson: _anyToString)
  String? get brandID => throw _privateConstructorUsedError;
  @JsonKey(name: 'modelNumber', fromJson: _anyToString)
  String? get modelNumber => throw _privateConstructorUsedError;
  @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
  String? get warrantyStartDate => throw _privateConstructorUsedError;
  int? get warrantyDuration => throw _privateConstructorUsedError;
  @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
  String? get warrantyEndDate => throw _privateConstructorUsedError;
  @JsonKey(name: 'wfStatus', fromJson: _anyToString)
  String? get wfStatus => throw _privateConstructorUsedError;
  bool? get isActive => throw _privateConstructorUsedError;
  List<Document>? get documents => throw _privateConstructorUsedError;
  AuditDetails? get auditDetails => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AssetCopyWith<Asset> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetCopyWith<$Res> {
  factory $AssetCopyWith(Asset value, $Res Function(Asset) then) =
      _$AssetCopyWithImpl<$Res, Asset>;
  @useResult
  $Res call(
      {@JsonKey(name: 'assetId', fromJson: _anyToString) String? assetId,
      @JsonKey(name: 'tenantId', fromJson: _anyToString) String? tenantId,
      @JsonKey(name: 'activityFacilityId', fromJson: _anyToString)
      String? activityFacilityID,
      @JsonKey(name: 'facilityID', fromJson: _anyToString) String? facilityID,
      @JsonKey(name: 'system', fromJson: _anyToString) String? system,
      @JsonKey(name: 'serialNumber', fromJson: _anyToString)
      String? serialNumber,
      @JsonKey(name: 'assetTypeID', fromJson: _anyToString) String? assetTypeID,
      AssetDetails? assetDetails,
      @JsonKey(name: 'brandID', fromJson: _anyToString) String? brandID,
      @JsonKey(name: 'modelNumber', fromJson: _anyToString) String? modelNumber,
      @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
      String? warrantyStartDate,
      int? warrantyDuration,
      @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
      String? warrantyEndDate,
      @JsonKey(name: 'wfStatus', fromJson: _anyToString) String? wfStatus,
      bool? isActive,
      List<Document>? documents,
      AuditDetails? auditDetails});

  $AssetDetailsCopyWith<$Res>? get assetDetails;
  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class _$AssetCopyWithImpl<$Res, $Val extends Asset>
    implements $AssetCopyWith<$Res> {
  _$AssetCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? assetId = freezed,
    Object? tenantId = freezed,
    Object? activityFacilityID = freezed,
    Object? facilityID = freezed,
    Object? system = freezed,
    Object? serialNumber = freezed,
    Object? assetTypeID = freezed,
    Object? assetDetails = freezed,
    Object? brandID = freezed,
    Object? modelNumber = freezed,
    Object? warrantyStartDate = freezed,
    Object? warrantyDuration = freezed,
    Object? warrantyEndDate = freezed,
    Object? wfStatus = freezed,
    Object? isActive = freezed,
    Object? documents = freezed,
    Object? auditDetails = freezed,
  }) {
    return _then(_value.copyWith(
      assetId: freezed == assetId
          ? _value.assetId
          : assetId // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      activityFacilityID: freezed == activityFacilityID
          ? _value.activityFacilityID
          : activityFacilityID // ignore: cast_nullable_to_non_nullable
              as String?,
      facilityID: freezed == facilityID
          ? _value.facilityID
          : facilityID // ignore: cast_nullable_to_non_nullable
              as String?,
      system: freezed == system
          ? _value.system
          : system // ignore: cast_nullable_to_non_nullable
              as String?,
      serialNumber: freezed == serialNumber
          ? _value.serialNumber
          : serialNumber // ignore: cast_nullable_to_non_nullable
              as String?,
      assetTypeID: freezed == assetTypeID
          ? _value.assetTypeID
          : assetTypeID // ignore: cast_nullable_to_non_nullable
              as String?,
      assetDetails: freezed == assetDetails
          ? _value.assetDetails
          : assetDetails // ignore: cast_nullable_to_non_nullable
              as AssetDetails?,
      brandID: freezed == brandID
          ? _value.brandID
          : brandID // ignore: cast_nullable_to_non_nullable
              as String?,
      modelNumber: freezed == modelNumber
          ? _value.modelNumber
          : modelNumber // ignore: cast_nullable_to_non_nullable
              as String?,
      warrantyStartDate: freezed == warrantyStartDate
          ? _value.warrantyStartDate
          : warrantyStartDate // ignore: cast_nullable_to_non_nullable
              as String?,
      warrantyDuration: freezed == warrantyDuration
          ? _value.warrantyDuration
          : warrantyDuration // ignore: cast_nullable_to_non_nullable
              as int?,
      warrantyEndDate: freezed == warrantyEndDate
          ? _value.warrantyEndDate
          : warrantyEndDate // ignore: cast_nullable_to_non_nullable
              as String?,
      wfStatus: freezed == wfStatus
          ? _value.wfStatus
          : wfStatus // ignore: cast_nullable_to_non_nullable
              as String?,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
      documents: freezed == documents
          ? _value.documents
          : documents // ignore: cast_nullable_to_non_nullable
              as List<Document>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $AssetDetailsCopyWith<$Res>? get assetDetails {
    if (_value.assetDetails == null) {
      return null;
    }

    return $AssetDetailsCopyWith<$Res>(_value.assetDetails!, (value) {
      return _then(_value.copyWith(assetDetails: value) as $Val);
    });
  }

  @override
  @pragma('vm:prefer-inline')
  $AuditDetailsCopyWith<$Res>? get auditDetails {
    if (_value.auditDetails == null) {
      return null;
    }

    return $AuditDetailsCopyWith<$Res>(_value.auditDetails!, (value) {
      return _then(_value.copyWith(auditDetails: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$AssetImplCopyWith<$Res> implements $AssetCopyWith<$Res> {
  factory _$$AssetImplCopyWith(
          _$AssetImpl value, $Res Function(_$AssetImpl) then) =
      __$$AssetImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {@JsonKey(name: 'assetId', fromJson: _anyToString) String? assetId,
      @JsonKey(name: 'tenantId', fromJson: _anyToString) String? tenantId,
      @JsonKey(name: 'activityFacilityId', fromJson: _anyToString)
      String? activityFacilityID,
      @JsonKey(name: 'facilityID', fromJson: _anyToString) String? facilityID,
      @JsonKey(name: 'system', fromJson: _anyToString) String? system,
      @JsonKey(name: 'serialNumber', fromJson: _anyToString)
      String? serialNumber,
      @JsonKey(name: 'assetTypeID', fromJson: _anyToString) String? assetTypeID,
      AssetDetails? assetDetails,
      @JsonKey(name: 'brandID', fromJson: _anyToString) String? brandID,
      @JsonKey(name: 'modelNumber', fromJson: _anyToString) String? modelNumber,
      @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
      String? warrantyStartDate,
      int? warrantyDuration,
      @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
      String? warrantyEndDate,
      @JsonKey(name: 'wfStatus', fromJson: _anyToString) String? wfStatus,
      bool? isActive,
      List<Document>? documents,
      AuditDetails? auditDetails});

  @override
  $AssetDetailsCopyWith<$Res>? get assetDetails;
  @override
  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class __$$AssetImplCopyWithImpl<$Res>
    extends _$AssetCopyWithImpl<$Res, _$AssetImpl>
    implements _$$AssetImplCopyWith<$Res> {
  __$$AssetImplCopyWithImpl(
      _$AssetImpl _value, $Res Function(_$AssetImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? assetId = freezed,
    Object? tenantId = freezed,
    Object? activityFacilityID = freezed,
    Object? facilityID = freezed,
    Object? system = freezed,
    Object? serialNumber = freezed,
    Object? assetTypeID = freezed,
    Object? assetDetails = freezed,
    Object? brandID = freezed,
    Object? modelNumber = freezed,
    Object? warrantyStartDate = freezed,
    Object? warrantyDuration = freezed,
    Object? warrantyEndDate = freezed,
    Object? wfStatus = freezed,
    Object? isActive = freezed,
    Object? documents = freezed,
    Object? auditDetails = freezed,
  }) {
    return _then(_$AssetImpl(
      assetId: freezed == assetId
          ? _value.assetId
          : assetId // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      activityFacilityID: freezed == activityFacilityID
          ? _value.activityFacilityID
          : activityFacilityID // ignore: cast_nullable_to_non_nullable
              as String?,
      facilityID: freezed == facilityID
          ? _value.facilityID
          : facilityID // ignore: cast_nullable_to_non_nullable
              as String?,
      system: freezed == system
          ? _value.system
          : system // ignore: cast_nullable_to_non_nullable
              as String?,
      serialNumber: freezed == serialNumber
          ? _value.serialNumber
          : serialNumber // ignore: cast_nullable_to_non_nullable
              as String?,
      assetTypeID: freezed == assetTypeID
          ? _value.assetTypeID
          : assetTypeID // ignore: cast_nullable_to_non_nullable
              as String?,
      assetDetails: freezed == assetDetails
          ? _value.assetDetails
          : assetDetails // ignore: cast_nullable_to_non_nullable
              as AssetDetails?,
      brandID: freezed == brandID
          ? _value.brandID
          : brandID // ignore: cast_nullable_to_non_nullable
              as String?,
      modelNumber: freezed == modelNumber
          ? _value.modelNumber
          : modelNumber // ignore: cast_nullable_to_non_nullable
              as String?,
      warrantyStartDate: freezed == warrantyStartDate
          ? _value.warrantyStartDate
          : warrantyStartDate // ignore: cast_nullable_to_non_nullable
              as String?,
      warrantyDuration: freezed == warrantyDuration
          ? _value.warrantyDuration
          : warrantyDuration // ignore: cast_nullable_to_non_nullable
              as int?,
      warrantyEndDate: freezed == warrantyEndDate
          ? _value.warrantyEndDate
          : warrantyEndDate // ignore: cast_nullable_to_non_nullable
              as String?,
      wfStatus: freezed == wfStatus
          ? _value.wfStatus
          : wfStatus // ignore: cast_nullable_to_non_nullable
              as String?,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
      documents: freezed == documents
          ? _value._documents
          : documents // ignore: cast_nullable_to_non_nullable
              as List<Document>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AssetImpl implements _Asset {
  const _$AssetImpl(
      {@JsonKey(name: 'assetId', fromJson: _anyToString) this.assetId,
      @JsonKey(name: 'tenantId', fromJson: _anyToString) this.tenantId,
      @JsonKey(name: 'activityFacilityId', fromJson: _anyToString)
      this.activityFacilityID,
      @JsonKey(name: 'facilityID', fromJson: _anyToString) this.facilityID,
      @JsonKey(name: 'system', fromJson: _anyToString) this.system,
      @JsonKey(name: 'serialNumber', fromJson: _anyToString) this.serialNumber,
      @JsonKey(name: 'assetTypeID', fromJson: _anyToString) this.assetTypeID,
      this.assetDetails,
      @JsonKey(name: 'brandID', fromJson: _anyToString) this.brandID,
      @JsonKey(name: 'modelNumber', fromJson: _anyToString) this.modelNumber,
      @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
      this.warrantyStartDate,
      this.warrantyDuration,
      @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
      this.warrantyEndDate,
      @JsonKey(name: 'wfStatus', fromJson: _anyToString) this.wfStatus,
      this.isActive,
      final List<Document>? documents,
      this.auditDetails})
      : _documents = documents;

  factory _$AssetImpl.fromJson(Map<String, dynamic> json) =>
      _$$AssetImplFromJson(json);

  @override
  @JsonKey(name: 'assetId', fromJson: _anyToString)
  final String? assetId;
  @override
  @JsonKey(name: 'tenantId', fromJson: _anyToString)
  final String? tenantId;
  @override
  @JsonKey(name: 'activityFacilityId', fromJson: _anyToString)
  final String? activityFacilityID;
  @override
  @JsonKey(name: 'facilityID', fromJson: _anyToString)
  final String? facilityID;
  @override
  @JsonKey(name: 'system', fromJson: _anyToString)
  final String? system;
  @override
  @JsonKey(name: 'serialNumber', fromJson: _anyToString)
  final String? serialNumber;
  @override
  @JsonKey(name: 'assetTypeID', fromJson: _anyToString)
  final String? assetTypeID;
  @override
  final AssetDetails? assetDetails;
  @override
  @JsonKey(name: 'brandID', fromJson: _anyToString)
  final String? brandID;
  @override
  @JsonKey(name: 'modelNumber', fromJson: _anyToString)
  final String? modelNumber;
  @override
  @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
  final String? warrantyStartDate;
  @override
  final int? warrantyDuration;
  @override
  @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
  final String? warrantyEndDate;
  @override
  @JsonKey(name: 'wfStatus', fromJson: _anyToString)
  final String? wfStatus;
  @override
  final bool? isActive;
  final List<Document>? _documents;
  @override
  List<Document>? get documents {
    final value = _documents;
    if (value == null) return null;
    if (_documents is EqualUnmodifiableListView) return _documents;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(value);
  }

  @override
  final AuditDetails? auditDetails;

  @override
  String toString() {
    return 'Asset(assetId: $assetId, tenantId: $tenantId, activityFacilityID: $activityFacilityID, facilityID: $facilityID, system: $system, serialNumber: $serialNumber, assetTypeID: $assetTypeID, assetDetails: $assetDetails, brandID: $brandID, modelNumber: $modelNumber, warrantyStartDate: $warrantyStartDate, warrantyDuration: $warrantyDuration, warrantyEndDate: $warrantyEndDate, wfStatus: $wfStatus, isActive: $isActive, documents: $documents, auditDetails: $auditDetails)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AssetImpl &&
            (identical(other.assetId, assetId) || other.assetId == assetId) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            (identical(other.activityFacilityID, activityFacilityID) ||
                other.activityFacilityID == activityFacilityID) &&
            (identical(other.facilityID, facilityID) ||
                other.facilityID == facilityID) &&
            (identical(other.system, system) || other.system == system) &&
            (identical(other.serialNumber, serialNumber) ||
                other.serialNumber == serialNumber) &&
            (identical(other.assetTypeID, assetTypeID) ||
                other.assetTypeID == assetTypeID) &&
            (identical(other.assetDetails, assetDetails) ||
                other.assetDetails == assetDetails) &&
            (identical(other.brandID, brandID) || other.brandID == brandID) &&
            (identical(other.modelNumber, modelNumber) ||
                other.modelNumber == modelNumber) &&
            (identical(other.warrantyStartDate, warrantyStartDate) ||
                other.warrantyStartDate == warrantyStartDate) &&
            (identical(other.warrantyDuration, warrantyDuration) ||
                other.warrantyDuration == warrantyDuration) &&
            (identical(other.warrantyEndDate, warrantyEndDate) ||
                other.warrantyEndDate == warrantyEndDate) &&
            (identical(other.wfStatus, wfStatus) ||
                other.wfStatus == wfStatus) &&
            (identical(other.isActive, isActive) ||
                other.isActive == isActive) &&
            const DeepCollectionEquality()
                .equals(other._documents, _documents) &&
            (identical(other.auditDetails, auditDetails) ||
                other.auditDetails == auditDetails));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      assetId,
      tenantId,
      activityFacilityID,
      facilityID,
      system,
      serialNumber,
      assetTypeID,
      assetDetails,
      brandID,
      modelNumber,
      warrantyStartDate,
      warrantyDuration,
      warrantyEndDate,
      wfStatus,
      isActive,
      const DeepCollectionEquality().hash(_documents),
      auditDetails);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AssetImplCopyWith<_$AssetImpl> get copyWith =>
      __$$AssetImplCopyWithImpl<_$AssetImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AssetImplToJson(
      this,
    );
  }
}

abstract class _Asset implements Asset {
  const factory _Asset(
      {@JsonKey(name: 'assetId', fromJson: _anyToString) final String? assetId,
      @JsonKey(name: 'tenantId', fromJson: _anyToString) final String? tenantId,
      @JsonKey(name: 'activityFacilityId', fromJson: _anyToString)
      final String? activityFacilityID,
      @JsonKey(name: 'facilityID', fromJson: _anyToString)
      final String? facilityID,
      @JsonKey(name: 'system', fromJson: _anyToString) final String? system,
      @JsonKey(name: 'serialNumber', fromJson: _anyToString)
      final String? serialNumber,
      @JsonKey(name: 'assetTypeID', fromJson: _anyToString)
      final String? assetTypeID,
      final AssetDetails? assetDetails,
      @JsonKey(name: 'brandID', fromJson: _anyToString) final String? brandID,
      @JsonKey(name: 'modelNumber', fromJson: _anyToString)
      final String? modelNumber,
      @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
      final String? warrantyStartDate,
      final int? warrantyDuration,
      @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
      final String? warrantyEndDate,
      @JsonKey(name: 'wfStatus', fromJson: _anyToString) final String? wfStatus,
      final bool? isActive,
      final List<Document>? documents,
      final AuditDetails? auditDetails}) = _$AssetImpl;

  factory _Asset.fromJson(Map<String, dynamic> json) = _$AssetImpl.fromJson;

  @override
  @JsonKey(name: 'assetId', fromJson: _anyToString)
  String? get assetId;
  @override
  @JsonKey(name: 'tenantId', fromJson: _anyToString)
  String? get tenantId;
  @override
  @JsonKey(name: 'activityFacilityId', fromJson: _anyToString)
  String? get activityFacilityID;
  @override
  @JsonKey(name: 'facilityID', fromJson: _anyToString)
  String? get facilityID;
  @override
  @JsonKey(name: 'system', fromJson: _anyToString)
  String? get system;
  @override
  @JsonKey(name: 'serialNumber', fromJson: _anyToString)
  String? get serialNumber;
  @override
  @JsonKey(name: 'assetTypeID', fromJson: _anyToString)
  String? get assetTypeID;
  @override
  AssetDetails? get assetDetails;
  @override
  @JsonKey(name: 'brandID', fromJson: _anyToString)
  String? get brandID;
  @override
  @JsonKey(name: 'modelNumber', fromJson: _anyToString)
  String? get modelNumber;
  @override
  @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
  String? get warrantyStartDate;
  @override
  int? get warrantyDuration;
  @override
  @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
  String? get warrantyEndDate;
  @override
  @JsonKey(name: 'wfStatus', fromJson: _anyToString)
  String? get wfStatus;
  @override
  bool? get isActive;
  @override
  List<Document>? get documents;
  @override
  AuditDetails? get auditDetails;
  @override
  @JsonKey(ignore: true)
  _$$AssetImplCopyWith<_$AssetImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
