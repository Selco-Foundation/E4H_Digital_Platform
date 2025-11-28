// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'scheduled_visit.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

ScheduledVisitSearchResponse _$ScheduledVisitSearchResponseFromJson(
    Map<String, dynamic> json) {
  return _ScheduledVisitSearchResponse.fromJson(json);
}

/// @nodoc
mixin _$ScheduledVisitSearchResponse {
  @JsonKey(name: 'ScheduledVisits')
  List<ScheduledVisit> get scheduledVisits =>
      throw _privateConstructorUsedError;
  @JsonKey(name: 'TotalCount')
  int get totalCount => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $ScheduledVisitSearchResponseCopyWith<ScheduledVisitSearchResponse>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ScheduledVisitSearchResponseCopyWith<$Res> {
  factory $ScheduledVisitSearchResponseCopyWith(
          ScheduledVisitSearchResponse value,
          $Res Function(ScheduledVisitSearchResponse) then) =
      _$ScheduledVisitSearchResponseCopyWithImpl<$Res,
          ScheduledVisitSearchResponse>;
  @useResult
  $Res call(
      {@JsonKey(name: 'ScheduledVisits') List<ScheduledVisit> scheduledVisits,
      @JsonKey(name: 'TotalCount') int totalCount});
}

/// @nodoc
class _$ScheduledVisitSearchResponseCopyWithImpl<$Res,
        $Val extends ScheduledVisitSearchResponse>
    implements $ScheduledVisitSearchResponseCopyWith<$Res> {
  _$ScheduledVisitSearchResponseCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisits = null,
    Object? totalCount = null,
  }) {
    return _then(_value.copyWith(
      scheduledVisits: null == scheduledVisits
          ? _value.scheduledVisits
          : scheduledVisits // ignore: cast_nullable_to_non_nullable
              as List<ScheduledVisit>,
      totalCount: null == totalCount
          ? _value.totalCount
          : totalCount // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ScheduledVisitSearchResponseImplCopyWith<$Res>
    implements $ScheduledVisitSearchResponseCopyWith<$Res> {
  factory _$$ScheduledVisitSearchResponseImplCopyWith(
          _$ScheduledVisitSearchResponseImpl value,
          $Res Function(_$ScheduledVisitSearchResponseImpl) then) =
      __$$ScheduledVisitSearchResponseImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {@JsonKey(name: 'ScheduledVisits') List<ScheduledVisit> scheduledVisits,
      @JsonKey(name: 'TotalCount') int totalCount});
}

/// @nodoc
class __$$ScheduledVisitSearchResponseImplCopyWithImpl<$Res>
    extends _$ScheduledVisitSearchResponseCopyWithImpl<$Res,
        _$ScheduledVisitSearchResponseImpl>
    implements _$$ScheduledVisitSearchResponseImplCopyWith<$Res> {
  __$$ScheduledVisitSearchResponseImplCopyWithImpl(
      _$ScheduledVisitSearchResponseImpl _value,
      $Res Function(_$ScheduledVisitSearchResponseImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisits = null,
    Object? totalCount = null,
  }) {
    return _then(_$ScheduledVisitSearchResponseImpl(
      scheduledVisits: null == scheduledVisits
          ? _value._scheduledVisits
          : scheduledVisits // ignore: cast_nullable_to_non_nullable
              as List<ScheduledVisit>,
      totalCount: null == totalCount
          ? _value.totalCount
          : totalCount // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ScheduledVisitSearchResponseImpl
    implements _ScheduledVisitSearchResponse {
  const _$ScheduledVisitSearchResponseImpl(
      {@JsonKey(name: 'ScheduledVisits')
      final List<ScheduledVisit> scheduledVisits = const <ScheduledVisit>[],
      @JsonKey(name: 'TotalCount') this.totalCount = 0})
      : _scheduledVisits = scheduledVisits;

  factory _$ScheduledVisitSearchResponseImpl.fromJson(
          Map<String, dynamic> json) =>
      _$$ScheduledVisitSearchResponseImplFromJson(json);

  final List<ScheduledVisit> _scheduledVisits;
  @override
  @JsonKey(name: 'ScheduledVisits')
  List<ScheduledVisit> get scheduledVisits {
    if (_scheduledVisits is EqualUnmodifiableListView) return _scheduledVisits;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_scheduledVisits);
  }

  @override
  @JsonKey(name: 'TotalCount')
  final int totalCount;

  @override
  String toString() {
    return 'ScheduledVisitSearchResponse(scheduledVisits: $scheduledVisits, totalCount: $totalCount)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ScheduledVisitSearchResponseImpl &&
            const DeepCollectionEquality()
                .equals(other._scheduledVisits, _scheduledVisits) &&
            (identical(other.totalCount, totalCount) ||
                other.totalCount == totalCount));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType,
      const DeepCollectionEquality().hash(_scheduledVisits), totalCount);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ScheduledVisitSearchResponseImplCopyWith<
          _$ScheduledVisitSearchResponseImpl>
      get copyWith => __$$ScheduledVisitSearchResponseImplCopyWithImpl<
          _$ScheduledVisitSearchResponseImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ScheduledVisitSearchResponseImplToJson(
      this,
    );
  }
}

abstract class _ScheduledVisitSearchResponse
    implements ScheduledVisitSearchResponse {
  const factory _ScheduledVisitSearchResponse(
          {@JsonKey(name: 'ScheduledVisits')
          final List<ScheduledVisit> scheduledVisits,
          @JsonKey(name: 'TotalCount') final int totalCount}) =
      _$ScheduledVisitSearchResponseImpl;

  factory _ScheduledVisitSearchResponse.fromJson(Map<String, dynamic> json) =
      _$ScheduledVisitSearchResponseImpl.fromJson;

  @override
  @JsonKey(name: 'ScheduledVisits')
  List<ScheduledVisit> get scheduledVisits;
  @override
  @JsonKey(name: 'TotalCount')
  int get totalCount;
  @override
  @JsonKey(ignore: true)
  _$$ScheduledVisitSearchResponseImplCopyWith<
          _$ScheduledVisitSearchResponseImpl>
      get copyWith => throw _privateConstructorUsedError;
}

AmcConfiguration _$AmcConfigurationFromJson(Map<String, dynamic> json) {
  return _AmcConfiguration.fromJson(json);
}

/// @nodoc
mixin _$AmcConfiguration {
  String? get id => throw _privateConstructorUsedError;
  String? get tenantId => throw _privateConstructorUsedError;
  String? get vendorId => throw _privateConstructorUsedError;
  String? get facilityId => throw _privateConstructorUsedError;
  @FacilityConverter()
  Facility? get facility => throw _privateConstructorUsedError;
  String? get projectId => throw _privateConstructorUsedError;
  Map<String, dynamic>? get project => throw _privateConstructorUsedError;
  List<AmcAssetType> get assetTypes => throw _privateConstructorUsedError;
  List<AmcAssignment> get assignments => throw _privateConstructorUsedError;
  int? get durationMonths => throw _privateConstructorUsedError;
  int? get visitFrequencyMonths => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get configurationStartDate => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get configurationEndDate => throw _privateConstructorUsedError;
  String? get status => throw _privateConstructorUsedError;
  Map<String, dynamic>? get additionalDetails =>
      throw _privateConstructorUsedError;
  AuditDetails? get auditDetails => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AmcConfigurationCopyWith<AmcConfiguration> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AmcConfigurationCopyWith<$Res> {
  factory $AmcConfigurationCopyWith(
          AmcConfiguration value, $Res Function(AmcConfiguration) then) =
      _$AmcConfigurationCopyWithImpl<$Res, AmcConfiguration>;
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? vendorId,
      String? facilityId,
      @FacilityConverter() Facility? facility,
      String? projectId,
      Map<String, dynamic>? project,
      List<AmcAssetType> assetTypes,
      List<AmcAssignment> assignments,
      int? durationMonths,
      int? visitFrequencyMonths,
      @EpochDateTimeConverter() DateTime? configurationStartDate,
      @EpochDateTimeConverter() DateTime? configurationEndDate,
      String? status,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails});

  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class _$AmcConfigurationCopyWithImpl<$Res, $Val extends AmcConfiguration>
    implements $AmcConfigurationCopyWith<$Res> {
  _$AmcConfigurationCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? vendorId = freezed,
    Object? facilityId = freezed,
    Object? facility = freezed,
    Object? projectId = freezed,
    Object? project = freezed,
    Object? assetTypes = null,
    Object? assignments = null,
    Object? durationMonths = freezed,
    Object? visitFrequencyMonths = freezed,
    Object? configurationStartDate = freezed,
    Object? configurationEndDate = freezed,
    Object? status = freezed,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
  }) {
    return _then(_value.copyWith(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      vendorId: freezed == vendorId
          ? _value.vendorId
          : vendorId // ignore: cast_nullable_to_non_nullable
              as String?,
      facilityId: freezed == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
              as String?,
      facility: freezed == facility
          ? _value.facility
          : facility // ignore: cast_nullable_to_non_nullable
              as Facility?,
      projectId: freezed == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String?,
      project: freezed == project
          ? _value.project
          : project // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      assetTypes: null == assetTypes
          ? _value.assetTypes
          : assetTypes // ignore: cast_nullable_to_non_nullable
              as List<AmcAssetType>,
      assignments: null == assignments
          ? _value.assignments
          : assignments // ignore: cast_nullable_to_non_nullable
              as List<AmcAssignment>,
      durationMonths: freezed == durationMonths
          ? _value.durationMonths
          : durationMonths // ignore: cast_nullable_to_non_nullable
              as int?,
      visitFrequencyMonths: freezed == visitFrequencyMonths
          ? _value.visitFrequencyMonths
          : visitFrequencyMonths // ignore: cast_nullable_to_non_nullable
              as int?,
      configurationStartDate: freezed == configurationStartDate
          ? _value.configurationStartDate
          : configurationStartDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      configurationEndDate: freezed == configurationEndDate
          ? _value.configurationEndDate
          : configurationEndDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value.additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
    ) as $Val);
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
abstract class _$$AmcConfigurationImplCopyWith<$Res>
    implements $AmcConfigurationCopyWith<$Res> {
  factory _$$AmcConfigurationImplCopyWith(_$AmcConfigurationImpl value,
          $Res Function(_$AmcConfigurationImpl) then) =
      __$$AmcConfigurationImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? vendorId,
      String? facilityId,
      @FacilityConverter() Facility? facility,
      String? projectId,
      Map<String, dynamic>? project,
      List<AmcAssetType> assetTypes,
      List<AmcAssignment> assignments,
      int? durationMonths,
      int? visitFrequencyMonths,
      @EpochDateTimeConverter() DateTime? configurationStartDate,
      @EpochDateTimeConverter() DateTime? configurationEndDate,
      String? status,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails});

  @override
  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class __$$AmcConfigurationImplCopyWithImpl<$Res>
    extends _$AmcConfigurationCopyWithImpl<$Res, _$AmcConfigurationImpl>
    implements _$$AmcConfigurationImplCopyWith<$Res> {
  __$$AmcConfigurationImplCopyWithImpl(_$AmcConfigurationImpl _value,
      $Res Function(_$AmcConfigurationImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? vendorId = freezed,
    Object? facilityId = freezed,
    Object? facility = freezed,
    Object? projectId = freezed,
    Object? project = freezed,
    Object? assetTypes = null,
    Object? assignments = null,
    Object? durationMonths = freezed,
    Object? visitFrequencyMonths = freezed,
    Object? configurationStartDate = freezed,
    Object? configurationEndDate = freezed,
    Object? status = freezed,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
  }) {
    return _then(_$AmcConfigurationImpl(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      vendorId: freezed == vendorId
          ? _value.vendorId
          : vendorId // ignore: cast_nullable_to_non_nullable
              as String?,
      facilityId: freezed == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
              as String?,
      facility: freezed == facility
          ? _value.facility
          : facility // ignore: cast_nullable_to_non_nullable
              as Facility?,
      projectId: freezed == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String?,
      project: freezed == project
          ? _value._project
          : project // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      assetTypes: null == assetTypes
          ? _value._assetTypes
          : assetTypes // ignore: cast_nullable_to_non_nullable
              as List<AmcAssetType>,
      assignments: null == assignments
          ? _value._assignments
          : assignments // ignore: cast_nullable_to_non_nullable
              as List<AmcAssignment>,
      durationMonths: freezed == durationMonths
          ? _value.durationMonths
          : durationMonths // ignore: cast_nullable_to_non_nullable
              as int?,
      visitFrequencyMonths: freezed == visitFrequencyMonths
          ? _value.visitFrequencyMonths
          : visitFrequencyMonths // ignore: cast_nullable_to_non_nullable
              as int?,
      configurationStartDate: freezed == configurationStartDate
          ? _value.configurationStartDate
          : configurationStartDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      configurationEndDate: freezed == configurationEndDate
          ? _value.configurationEndDate
          : configurationEndDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value._additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AmcConfigurationImpl implements _AmcConfiguration {
  const _$AmcConfigurationImpl(
      {this.id,
      this.tenantId,
      this.vendorId,
      this.facilityId,
      @FacilityConverter() this.facility,
      this.projectId,
      final Map<String, dynamic>? project,
      final List<AmcAssetType> assetTypes = const <AmcAssetType>[],
      final List<AmcAssignment> assignments = const <AmcAssignment>[],
      this.durationMonths,
      this.visitFrequencyMonths,
      @EpochDateTimeConverter() this.configurationStartDate,
      @EpochDateTimeConverter() this.configurationEndDate,
      this.status,
      final Map<String, dynamic>? additionalDetails,
      this.auditDetails})
      : _project = project,
        _assetTypes = assetTypes,
        _assignments = assignments,
        _additionalDetails = additionalDetails;

  factory _$AmcConfigurationImpl.fromJson(Map<String, dynamic> json) =>
      _$$AmcConfigurationImplFromJson(json);

  @override
  final String? id;
  @override
  final String? tenantId;
  @override
  final String? vendorId;
  @override
  final String? facilityId;
  @override
  @FacilityConverter()
  final Facility? facility;
  @override
  final String? projectId;
  final Map<String, dynamic>? _project;
  @override
  Map<String, dynamic>? get project {
    final value = _project;
    if (value == null) return null;
    if (_project is EqualUnmodifiableMapView) return _project;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  final List<AmcAssetType> _assetTypes;
  @override
  @JsonKey()
  List<AmcAssetType> get assetTypes {
    if (_assetTypes is EqualUnmodifiableListView) return _assetTypes;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assetTypes);
  }

  final List<AmcAssignment> _assignments;
  @override
  @JsonKey()
  List<AmcAssignment> get assignments {
    if (_assignments is EqualUnmodifiableListView) return _assignments;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assignments);
  }

  @override
  final int? durationMonths;
  @override
  final int? visitFrequencyMonths;
  @override
  @EpochDateTimeConverter()
  final DateTime? configurationStartDate;
  @override
  @EpochDateTimeConverter()
  final DateTime? configurationEndDate;
  @override
  final String? status;
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
  final AuditDetails? auditDetails;

  @override
  String toString() {
    return 'AmcConfiguration(id: $id, tenantId: $tenantId, vendorId: $vendorId, facilityId: $facilityId, facility: $facility, projectId: $projectId, project: $project, assetTypes: $assetTypes, assignments: $assignments, durationMonths: $durationMonths, visitFrequencyMonths: $visitFrequencyMonths, configurationStartDate: $configurationStartDate, configurationEndDate: $configurationEndDate, status: $status, additionalDetails: $additionalDetails, auditDetails: $auditDetails)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AmcConfigurationImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            (identical(other.vendorId, vendorId) ||
                other.vendorId == vendorId) &&
            (identical(other.facilityId, facilityId) ||
                other.facilityId == facilityId) &&
            (identical(other.facility, facility) ||
                other.facility == facility) &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            const DeepCollectionEquality().equals(other._project, _project) &&
            const DeepCollectionEquality()
                .equals(other._assetTypes, _assetTypes) &&
            const DeepCollectionEquality()
                .equals(other._assignments, _assignments) &&
            (identical(other.durationMonths, durationMonths) ||
                other.durationMonths == durationMonths) &&
            (identical(other.visitFrequencyMonths, visitFrequencyMonths) ||
                other.visitFrequencyMonths == visitFrequencyMonths) &&
            (identical(other.configurationStartDate, configurationStartDate) ||
                other.configurationStartDate == configurationStartDate) &&
            (identical(other.configurationEndDate, configurationEndDate) ||
                other.configurationEndDate == configurationEndDate) &&
            (identical(other.status, status) || other.status == status) &&
            const DeepCollectionEquality()
                .equals(other._additionalDetails, _additionalDetails) &&
            (identical(other.auditDetails, auditDetails) ||
                other.auditDetails == auditDetails));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      tenantId,
      vendorId,
      facilityId,
      facility,
      projectId,
      const DeepCollectionEquality().hash(_project),
      const DeepCollectionEquality().hash(_assetTypes),
      const DeepCollectionEquality().hash(_assignments),
      durationMonths,
      visitFrequencyMonths,
      configurationStartDate,
      configurationEndDate,
      status,
      const DeepCollectionEquality().hash(_additionalDetails),
      auditDetails);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AmcConfigurationImplCopyWith<_$AmcConfigurationImpl> get copyWith =>
      __$$AmcConfigurationImplCopyWithImpl<_$AmcConfigurationImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AmcConfigurationImplToJson(
      this,
    );
  }
}

abstract class _AmcConfiguration implements AmcConfiguration {
  const factory _AmcConfiguration(
      {final String? id,
      final String? tenantId,
      final String? vendorId,
      final String? facilityId,
      @FacilityConverter() final Facility? facility,
      final String? projectId,
      final Map<String, dynamic>? project,
      final List<AmcAssetType> assetTypes,
      final List<AmcAssignment> assignments,
      final int? durationMonths,
      final int? visitFrequencyMonths,
      @EpochDateTimeConverter() final DateTime? configurationStartDate,
      @EpochDateTimeConverter() final DateTime? configurationEndDate,
      final String? status,
      final Map<String, dynamic>? additionalDetails,
      final AuditDetails? auditDetails}) = _$AmcConfigurationImpl;

  factory _AmcConfiguration.fromJson(Map<String, dynamic> json) =
      _$AmcConfigurationImpl.fromJson;

  @override
  String? get id;
  @override
  String? get tenantId;
  @override
  String? get vendorId;
  @override
  String? get facilityId;
  @override
  @FacilityConverter()
  Facility? get facility;
  @override
  String? get projectId;
  @override
  Map<String, dynamic>? get project;
  @override
  List<AmcAssetType> get assetTypes;
  @override
  List<AmcAssignment> get assignments;
  @override
  int? get durationMonths;
  @override
  int? get visitFrequencyMonths;
  @override
  @EpochDateTimeConverter()
  DateTime? get configurationStartDate;
  @override
  @EpochDateTimeConverter()
  DateTime? get configurationEndDate;
  @override
  String? get status;
  @override
  Map<String, dynamic>? get additionalDetails;
  @override
  AuditDetails? get auditDetails;
  @override
  @JsonKey(ignore: true)
  _$$AmcConfigurationImplCopyWith<_$AmcConfigurationImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AmcAssetType _$AmcAssetTypeFromJson(Map<String, dynamic> json) {
  return _AmcAssetType.fromJson(json);
}

/// @nodoc
mixin _$AmcAssetType {
  String? get code => throw _privateConstructorUsedError;
  String? get name => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AmcAssetTypeCopyWith<AmcAssetType> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AmcAssetTypeCopyWith<$Res> {
  factory $AmcAssetTypeCopyWith(
          AmcAssetType value, $Res Function(AmcAssetType) then) =
      _$AmcAssetTypeCopyWithImpl<$Res, AmcAssetType>;
  @useResult
  $Res call({String? code, String? name});
}

/// @nodoc
class _$AmcAssetTypeCopyWithImpl<$Res, $Val extends AmcAssetType>
    implements $AmcAssetTypeCopyWith<$Res> {
  _$AmcAssetTypeCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = freezed,
    Object? name = freezed,
  }) {
    return _then(_value.copyWith(
      code: freezed == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String?,
      name: freezed == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AmcAssetTypeImplCopyWith<$Res>
    implements $AmcAssetTypeCopyWith<$Res> {
  factory _$$AmcAssetTypeImplCopyWith(
          _$AmcAssetTypeImpl value, $Res Function(_$AmcAssetTypeImpl) then) =
      __$$AmcAssetTypeImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String? code, String? name});
}

/// @nodoc
class __$$AmcAssetTypeImplCopyWithImpl<$Res>
    extends _$AmcAssetTypeCopyWithImpl<$Res, _$AmcAssetTypeImpl>
    implements _$$AmcAssetTypeImplCopyWith<$Res> {
  __$$AmcAssetTypeImplCopyWithImpl(
      _$AmcAssetTypeImpl _value, $Res Function(_$AmcAssetTypeImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = freezed,
    Object? name = freezed,
  }) {
    return _then(_$AmcAssetTypeImpl(
      code: freezed == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String?,
      name: freezed == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AmcAssetTypeImpl implements _AmcAssetType {
  const _$AmcAssetTypeImpl({this.code, this.name});

  factory _$AmcAssetTypeImpl.fromJson(Map<String, dynamic> json) =>
      _$$AmcAssetTypeImplFromJson(json);

  @override
  final String? code;
  @override
  final String? name;

  @override
  String toString() {
    return 'AmcAssetType(code: $code, name: $name)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AmcAssetTypeImpl &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.name, name) || other.name == name));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, code, name);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AmcAssetTypeImplCopyWith<_$AmcAssetTypeImpl> get copyWith =>
      __$$AmcAssetTypeImplCopyWithImpl<_$AmcAssetTypeImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AmcAssetTypeImplToJson(
      this,
    );
  }
}

abstract class _AmcAssetType implements AmcAssetType {
  const factory _AmcAssetType({final String? code, final String? name}) =
      _$AmcAssetTypeImpl;

  factory _AmcAssetType.fromJson(Map<String, dynamic> json) =
      _$AmcAssetTypeImpl.fromJson;

  @override
  String? get code;
  @override
  String? get name;
  @override
  @JsonKey(ignore: true)
  _$$AmcAssetTypeImplCopyWith<_$AmcAssetTypeImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AmcAssignment _$AmcAssignmentFromJson(Map<String, dynamic> json) {
  return _AmcAssignment.fromJson(json);
}

/// @nodoc
mixin _$AmcAssignment {
  String? get id => throw _privateConstructorUsedError;
  String? get tenantId => throw _privateConstructorUsedError;
  String? get amcConfigurationId => throw _privateConstructorUsedError;
  String? get assignedUser => throw _privateConstructorUsedError;
  Map<String, dynamic>? get additionalDetails =>
      throw _privateConstructorUsedError;
  AuditDetails? get auditDetails => throw _privateConstructorUsedError;
  bool? get isActive => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AmcAssignmentCopyWith<AmcAssignment> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AmcAssignmentCopyWith<$Res> {
  factory $AmcAssignmentCopyWith(
          AmcAssignment value, $Res Function(AmcAssignment) then) =
      _$AmcAssignmentCopyWithImpl<$Res, AmcAssignment>;
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? amcConfigurationId,
      String? assignedUser,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails,
      bool? isActive});

  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class _$AmcAssignmentCopyWithImpl<$Res, $Val extends AmcAssignment>
    implements $AmcAssignmentCopyWith<$Res> {
  _$AmcAssignmentCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? amcConfigurationId = freezed,
    Object? assignedUser = freezed,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
    Object? isActive = freezed,
  }) {
    return _then(_value.copyWith(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfigurationId: freezed == amcConfigurationId
          ? _value.amcConfigurationId
          : amcConfigurationId // ignore: cast_nullable_to_non_nullable
              as String?,
      assignedUser: freezed == assignedUser
          ? _value.assignedUser
          : assignedUser // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value.additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
    ) as $Val);
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
abstract class _$$AmcAssignmentImplCopyWith<$Res>
    implements $AmcAssignmentCopyWith<$Res> {
  factory _$$AmcAssignmentImplCopyWith(
          _$AmcAssignmentImpl value, $Res Function(_$AmcAssignmentImpl) then) =
      __$$AmcAssignmentImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? amcConfigurationId,
      String? assignedUser,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails,
      bool? isActive});

  @override
  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class __$$AmcAssignmentImplCopyWithImpl<$Res>
    extends _$AmcAssignmentCopyWithImpl<$Res, _$AmcAssignmentImpl>
    implements _$$AmcAssignmentImplCopyWith<$Res> {
  __$$AmcAssignmentImplCopyWithImpl(
      _$AmcAssignmentImpl _value, $Res Function(_$AmcAssignmentImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? amcConfigurationId = freezed,
    Object? assignedUser = freezed,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
    Object? isActive = freezed,
  }) {
    return _then(_$AmcAssignmentImpl(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfigurationId: freezed == amcConfigurationId
          ? _value.amcConfigurationId
          : amcConfigurationId // ignore: cast_nullable_to_non_nullable
              as String?,
      assignedUser: freezed == assignedUser
          ? _value.assignedUser
          : assignedUser // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value._additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AmcAssignmentImpl implements _AmcAssignment {
  const _$AmcAssignmentImpl(
      {this.id,
      this.tenantId,
      this.amcConfigurationId,
      this.assignedUser,
      final Map<String, dynamic>? additionalDetails,
      this.auditDetails,
      this.isActive})
      : _additionalDetails = additionalDetails;

  factory _$AmcAssignmentImpl.fromJson(Map<String, dynamic> json) =>
      _$$AmcAssignmentImplFromJson(json);

  @override
  final String? id;
  @override
  final String? tenantId;
  @override
  final String? amcConfigurationId;
  @override
  final String? assignedUser;
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
  final AuditDetails? auditDetails;
  @override
  final bool? isActive;

  @override
  String toString() {
    return 'AmcAssignment(id: $id, tenantId: $tenantId, amcConfigurationId: $amcConfigurationId, assignedUser: $assignedUser, additionalDetails: $additionalDetails, auditDetails: $auditDetails, isActive: $isActive)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AmcAssignmentImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            (identical(other.amcConfigurationId, amcConfigurationId) ||
                other.amcConfigurationId == amcConfigurationId) &&
            (identical(other.assignedUser, assignedUser) ||
                other.assignedUser == assignedUser) &&
            const DeepCollectionEquality()
                .equals(other._additionalDetails, _additionalDetails) &&
            (identical(other.auditDetails, auditDetails) ||
                other.auditDetails == auditDetails) &&
            (identical(other.isActive, isActive) ||
                other.isActive == isActive));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      tenantId,
      amcConfigurationId,
      assignedUser,
      const DeepCollectionEquality().hash(_additionalDetails),
      auditDetails,
      isActive);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AmcAssignmentImplCopyWith<_$AmcAssignmentImpl> get copyWith =>
      __$$AmcAssignmentImplCopyWithImpl<_$AmcAssignmentImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AmcAssignmentImplToJson(
      this,
    );
  }
}

abstract class _AmcAssignment implements AmcAssignment {
  const factory _AmcAssignment(
      {final String? id,
      final String? tenantId,
      final String? amcConfigurationId,
      final String? assignedUser,
      final Map<String, dynamic>? additionalDetails,
      final AuditDetails? auditDetails,
      final bool? isActive}) = _$AmcAssignmentImpl;

  factory _AmcAssignment.fromJson(Map<String, dynamic> json) =
      _$AmcAssignmentImpl.fromJson;

  @override
  String? get id;
  @override
  String? get tenantId;
  @override
  String? get amcConfigurationId;
  @override
  String? get assignedUser;
  @override
  Map<String, dynamic>? get additionalDetails;
  @override
  AuditDetails? get auditDetails;
  @override
  bool? get isActive;
  @override
  @JsonKey(ignore: true)
  _$$AmcAssignmentImplCopyWith<_$AmcAssignmentImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

ScheduledVisit _$ScheduledVisitFromJson(Map<String, dynamic> json) {
  return _ScheduledVisit.fromJson(json);
}

/// @nodoc
mixin _$ScheduledVisit {
  String? get id => throw _privateConstructorUsedError;
  String? get tenantId => throw _privateConstructorUsedError;
  String? get amcConfigurationId => throw _privateConstructorUsedError;
  AmcConfiguration? get amcConfiguration => throw _privateConstructorUsedError;
  String? get facilityId => throw _privateConstructorUsedError;
  @FacilityConverter()
  Facility? get facility => throw _privateConstructorUsedError;
  int? get visitNumber => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get scheduledDate => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get actualVisitDate => throw _privateConstructorUsedError;
  String? get status => throw _privateConstructorUsedError;
  ScheduledVisitReport? get visitReport => throw _privateConstructorUsedError;
  @WorkflowFlexConverter()
  Workflow? get workflow => throw _privateConstructorUsedError;
  List<Map<String, dynamic>> get processInstances =>
      throw _privateConstructorUsedError;
  List<Transaction>? get transactions => throw _privateConstructorUsedError;
  List<ScheduledVisitAssignment> get assignments =>
      throw _privateConstructorUsedError;
  Map<String, dynamic>? get additionalDetails =>
      throw _privateConstructorUsedError;
  AuditDetails? get auditDetails => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $ScheduledVisitCopyWith<ScheduledVisit> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ScheduledVisitCopyWith<$Res> {
  factory $ScheduledVisitCopyWith(
          ScheduledVisit value, $Res Function(ScheduledVisit) then) =
      _$ScheduledVisitCopyWithImpl<$Res, ScheduledVisit>;
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? amcConfigurationId,
      AmcConfiguration? amcConfiguration,
      String? facilityId,
      @FacilityConverter() Facility? facility,
      int? visitNumber,
      @EpochDateTimeConverter() DateTime? scheduledDate,
      @EpochDateTimeConverter() DateTime? actualVisitDate,
      String? status,
      ScheduledVisitReport? visitReport,
      @WorkflowFlexConverter() Workflow? workflow,
      List<Map<String, dynamic>> processInstances,
      List<Transaction>? transactions,
      List<ScheduledVisitAssignment> assignments,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails});

  $AmcConfigurationCopyWith<$Res>? get amcConfiguration;
  $ScheduledVisitReportCopyWith<$Res>? get visitReport;
  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class _$ScheduledVisitCopyWithImpl<$Res, $Val extends ScheduledVisit>
    implements $ScheduledVisitCopyWith<$Res> {
  _$ScheduledVisitCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? amcConfigurationId = freezed,
    Object? amcConfiguration = freezed,
    Object? facilityId = freezed,
    Object? facility = freezed,
    Object? visitNumber = freezed,
    Object? scheduledDate = freezed,
    Object? actualVisitDate = freezed,
    Object? status = freezed,
    Object? visitReport = freezed,
    Object? workflow = freezed,
    Object? processInstances = null,
    Object? transactions = freezed,
    Object? assignments = null,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
  }) {
    return _then(_value.copyWith(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfigurationId: freezed == amcConfigurationId
          ? _value.amcConfigurationId
          : amcConfigurationId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfiguration: freezed == amcConfiguration
          ? _value.amcConfiguration
          : amcConfiguration // ignore: cast_nullable_to_non_nullable
              as AmcConfiguration?,
      facilityId: freezed == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
              as String?,
      facility: freezed == facility
          ? _value.facility
          : facility // ignore: cast_nullable_to_non_nullable
              as Facility?,
      visitNumber: freezed == visitNumber
          ? _value.visitNumber
          : visitNumber // ignore: cast_nullable_to_non_nullable
              as int?,
      scheduledDate: freezed == scheduledDate
          ? _value.scheduledDate
          : scheduledDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      actualVisitDate: freezed == actualVisitDate
          ? _value.actualVisitDate
          : actualVisitDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      visitReport: freezed == visitReport
          ? _value.visitReport
          : visitReport // ignore: cast_nullable_to_non_nullable
              as ScheduledVisitReport?,
      workflow: freezed == workflow
          ? _value.workflow
          : workflow // ignore: cast_nullable_to_non_nullable
              as Workflow?,
      processInstances: null == processInstances
          ? _value.processInstances
          : processInstances // ignore: cast_nullable_to_non_nullable
              as List<Map<String, dynamic>>,
      transactions: freezed == transactions
          ? _value.transactions
          : transactions // ignore: cast_nullable_to_non_nullable
              as List<Transaction>?,
      assignments: null == assignments
          ? _value.assignments
          : assignments // ignore: cast_nullable_to_non_nullable
              as List<ScheduledVisitAssignment>,
      additionalDetails: freezed == additionalDetails
          ? _value.additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $AmcConfigurationCopyWith<$Res>? get amcConfiguration {
    if (_value.amcConfiguration == null) {
      return null;
    }

    return $AmcConfigurationCopyWith<$Res>(_value.amcConfiguration!, (value) {
      return _then(_value.copyWith(amcConfiguration: value) as $Val);
    });
  }

  @override
  @pragma('vm:prefer-inline')
  $ScheduledVisitReportCopyWith<$Res>? get visitReport {
    if (_value.visitReport == null) {
      return null;
    }

    return $ScheduledVisitReportCopyWith<$Res>(_value.visitReport!, (value) {
      return _then(_value.copyWith(visitReport: value) as $Val);
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
abstract class _$$ScheduledVisitImplCopyWith<$Res>
    implements $ScheduledVisitCopyWith<$Res> {
  factory _$$ScheduledVisitImplCopyWith(_$ScheduledVisitImpl value,
          $Res Function(_$ScheduledVisitImpl) then) =
      __$$ScheduledVisitImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? amcConfigurationId,
      AmcConfiguration? amcConfiguration,
      String? facilityId,
      @FacilityConverter() Facility? facility,
      int? visitNumber,
      @EpochDateTimeConverter() DateTime? scheduledDate,
      @EpochDateTimeConverter() DateTime? actualVisitDate,
      String? status,
      ScheduledVisitReport? visitReport,
      @WorkflowFlexConverter() Workflow? workflow,
      List<Map<String, dynamic>> processInstances,
      List<Transaction>? transactions,
      List<ScheduledVisitAssignment> assignments,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails});

  @override
  $AmcConfigurationCopyWith<$Res>? get amcConfiguration;
  @override
  $ScheduledVisitReportCopyWith<$Res>? get visitReport;
  @override
  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class __$$ScheduledVisitImplCopyWithImpl<$Res>
    extends _$ScheduledVisitCopyWithImpl<$Res, _$ScheduledVisitImpl>
    implements _$$ScheduledVisitImplCopyWith<$Res> {
  __$$ScheduledVisitImplCopyWithImpl(
      _$ScheduledVisitImpl _value, $Res Function(_$ScheduledVisitImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? amcConfigurationId = freezed,
    Object? amcConfiguration = freezed,
    Object? facilityId = freezed,
    Object? facility = freezed,
    Object? visitNumber = freezed,
    Object? scheduledDate = freezed,
    Object? actualVisitDate = freezed,
    Object? status = freezed,
    Object? visitReport = freezed,
    Object? workflow = freezed,
    Object? processInstances = null,
    Object? transactions = freezed,
    Object? assignments = null,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
  }) {
    return _then(_$ScheduledVisitImpl(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfigurationId: freezed == amcConfigurationId
          ? _value.amcConfigurationId
          : amcConfigurationId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfiguration: freezed == amcConfiguration
          ? _value.amcConfiguration
          : amcConfiguration // ignore: cast_nullable_to_non_nullable
              as AmcConfiguration?,
      facilityId: freezed == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
              as String?,
      facility: freezed == facility
          ? _value.facility
          : facility // ignore: cast_nullable_to_non_nullable
              as Facility?,
      visitNumber: freezed == visitNumber
          ? _value.visitNumber
          : visitNumber // ignore: cast_nullable_to_non_nullable
              as int?,
      scheduledDate: freezed == scheduledDate
          ? _value.scheduledDate
          : scheduledDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      actualVisitDate: freezed == actualVisitDate
          ? _value.actualVisitDate
          : actualVisitDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      visitReport: freezed == visitReport
          ? _value.visitReport
          : visitReport // ignore: cast_nullable_to_non_nullable
              as ScheduledVisitReport?,
      workflow: freezed == workflow
          ? _value.workflow
          : workflow // ignore: cast_nullable_to_non_nullable
              as Workflow?,
      processInstances: null == processInstances
          ? _value._processInstances
          : processInstances // ignore: cast_nullable_to_non_nullable
              as List<Map<String, dynamic>>,
      transactions: freezed == transactions
          ? _value._transactions
          : transactions // ignore: cast_nullable_to_non_nullable
              as List<Transaction>?,
      assignments: null == assignments
          ? _value._assignments
          : assignments // ignore: cast_nullable_to_non_nullable
              as List<ScheduledVisitAssignment>,
      additionalDetails: freezed == additionalDetails
          ? _value._additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ScheduledVisitImpl implements _ScheduledVisit {
  const _$ScheduledVisitImpl(
      {this.id,
      this.tenantId,
      this.amcConfigurationId,
      this.amcConfiguration,
      this.facilityId,
      @FacilityConverter() this.facility,
      this.visitNumber,
      @EpochDateTimeConverter() this.scheduledDate,
      @EpochDateTimeConverter() this.actualVisitDate,
      this.status,
      this.visitReport,
      @WorkflowFlexConverter() this.workflow,
      final List<Map<String, dynamic>> processInstances =
          const <Map<String, dynamic>>[],
      final List<Transaction>? transactions,
      final List<ScheduledVisitAssignment> assignments =
          const <ScheduledVisitAssignment>[],
      final Map<String, dynamic>? additionalDetails,
      this.auditDetails})
      : _processInstances = processInstances,
        _transactions = transactions,
        _assignments = assignments,
        _additionalDetails = additionalDetails;

  factory _$ScheduledVisitImpl.fromJson(Map<String, dynamic> json) =>
      _$$ScheduledVisitImplFromJson(json);

  @override
  final String? id;
  @override
  final String? tenantId;
  @override
  final String? amcConfigurationId;
  @override
  final AmcConfiguration? amcConfiguration;
  @override
  final String? facilityId;
  @override
  @FacilityConverter()
  final Facility? facility;
  @override
  final int? visitNumber;
  @override
  @EpochDateTimeConverter()
  final DateTime? scheduledDate;
  @override
  @EpochDateTimeConverter()
  final DateTime? actualVisitDate;
  @override
  final String? status;
  @override
  final ScheduledVisitReport? visitReport;
  @override
  @WorkflowFlexConverter()
  final Workflow? workflow;
  final List<Map<String, dynamic>> _processInstances;
  @override
  @JsonKey()
  List<Map<String, dynamic>> get processInstances {
    if (_processInstances is EqualUnmodifiableListView)
      return _processInstances;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_processInstances);
  }

  final List<Transaction>? _transactions;
  @override
  List<Transaction>? get transactions {
    final value = _transactions;
    if (value == null) return null;
    if (_transactions is EqualUnmodifiableListView) return _transactions;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(value);
  }

  final List<ScheduledVisitAssignment> _assignments;
  @override
  @JsonKey()
  List<ScheduledVisitAssignment> get assignments {
    if (_assignments is EqualUnmodifiableListView) return _assignments;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assignments);
  }

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
  final AuditDetails? auditDetails;

  @override
  String toString() {
    return 'ScheduledVisit(id: $id, tenantId: $tenantId, amcConfigurationId: $amcConfigurationId, amcConfiguration: $amcConfiguration, facilityId: $facilityId, facility: $facility, visitNumber: $visitNumber, scheduledDate: $scheduledDate, actualVisitDate: $actualVisitDate, status: $status, visitReport: $visitReport, workflow: $workflow, processInstances: $processInstances, transactions: $transactions, assignments: $assignments, additionalDetails: $additionalDetails, auditDetails: $auditDetails)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ScheduledVisitImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            (identical(other.amcConfigurationId, amcConfigurationId) ||
                other.amcConfigurationId == amcConfigurationId) &&
            (identical(other.amcConfiguration, amcConfiguration) ||
                other.amcConfiguration == amcConfiguration) &&
            (identical(other.facilityId, facilityId) ||
                other.facilityId == facilityId) &&
            (identical(other.facility, facility) ||
                other.facility == facility) &&
            (identical(other.visitNumber, visitNumber) ||
                other.visitNumber == visitNumber) &&
            (identical(other.scheduledDate, scheduledDate) ||
                other.scheduledDate == scheduledDate) &&
            (identical(other.actualVisitDate, actualVisitDate) ||
                other.actualVisitDate == actualVisitDate) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.visitReport, visitReport) ||
                other.visitReport == visitReport) &&
            (identical(other.workflow, workflow) ||
                other.workflow == workflow) &&
            const DeepCollectionEquality()
                .equals(other._processInstances, _processInstances) &&
            const DeepCollectionEquality()
                .equals(other._transactions, _transactions) &&
            const DeepCollectionEquality()
                .equals(other._assignments, _assignments) &&
            const DeepCollectionEquality()
                .equals(other._additionalDetails, _additionalDetails) &&
            (identical(other.auditDetails, auditDetails) ||
                other.auditDetails == auditDetails));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      tenantId,
      amcConfigurationId,
      amcConfiguration,
      facilityId,
      facility,
      visitNumber,
      scheduledDate,
      actualVisitDate,
      status,
      visitReport,
      workflow,
      const DeepCollectionEquality().hash(_processInstances),
      const DeepCollectionEquality().hash(_transactions),
      const DeepCollectionEquality().hash(_assignments),
      const DeepCollectionEquality().hash(_additionalDetails),
      auditDetails);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ScheduledVisitImplCopyWith<_$ScheduledVisitImpl> get copyWith =>
      __$$ScheduledVisitImplCopyWithImpl<_$ScheduledVisitImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ScheduledVisitImplToJson(
      this,
    );
  }
}

abstract class _ScheduledVisit implements ScheduledVisit {
  const factory _ScheduledVisit(
      {final String? id,
      final String? tenantId,
      final String? amcConfigurationId,
      final AmcConfiguration? amcConfiguration,
      final String? facilityId,
      @FacilityConverter() final Facility? facility,
      final int? visitNumber,
      @EpochDateTimeConverter() final DateTime? scheduledDate,
      @EpochDateTimeConverter() final DateTime? actualVisitDate,
      final String? status,
      final ScheduledVisitReport? visitReport,
      @WorkflowFlexConverter() final Workflow? workflow,
      final List<Map<String, dynamic>> processInstances,
      final List<Transaction>? transactions,
      final List<ScheduledVisitAssignment> assignments,
      final Map<String, dynamic>? additionalDetails,
      final AuditDetails? auditDetails}) = _$ScheduledVisitImpl;

  factory _ScheduledVisit.fromJson(Map<String, dynamic> json) =
      _$ScheduledVisitImpl.fromJson;

  @override
  String? get id;
  @override
  String? get tenantId;
  @override
  String? get amcConfigurationId;
  @override
  AmcConfiguration? get amcConfiguration;
  @override
  String? get facilityId;
  @override
  @FacilityConverter()
  Facility? get facility;
  @override
  int? get visitNumber;
  @override
  @EpochDateTimeConverter()
  DateTime? get scheduledDate;
  @override
  @EpochDateTimeConverter()
  DateTime? get actualVisitDate;
  @override
  String? get status;
  @override
  ScheduledVisitReport? get visitReport;
  @override
  @WorkflowFlexConverter()
  Workflow? get workflow;
  @override
  List<Map<String, dynamic>> get processInstances;
  @override
  List<Transaction>? get transactions;
  @override
  List<ScheduledVisitAssignment> get assignments;
  @override
  Map<String, dynamic>? get additionalDetails;
  @override
  AuditDetails? get auditDetails;
  @override
  @JsonKey(ignore: true)
  _$$ScheduledVisitImplCopyWith<_$ScheduledVisitImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

ScheduledVisitReport _$ScheduledVisitReportFromJson(Map<String, dynamic> json) {
  return _ScheduledVisitReport.fromJson(json);
}

/// @nodoc
mixin _$ScheduledVisitReport {
  String? get schemaCode => throw _privateConstructorUsedError;
  String? get version => throw _privateConstructorUsedError;
  String? get submittedBy => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get submittedAt => throw _privateConstructorUsedError;
  String? get otpReference => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get otpVerifiedAt => throw _privateConstructorUsedError;

  /// Flexible { key: value, ... } map
  Map<String, dynamic>? get responses => throw _privateConstructorUsedError;

  /// Reuse Document from workflow model
  List<Document>? get documents => throw _privateConstructorUsedError;
  Map<String, dynamic>? get additionalDetails =>
      throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $ScheduledVisitReportCopyWith<ScheduledVisitReport> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ScheduledVisitReportCopyWith<$Res> {
  factory $ScheduledVisitReportCopyWith(ScheduledVisitReport value,
          $Res Function(ScheduledVisitReport) then) =
      _$ScheduledVisitReportCopyWithImpl<$Res, ScheduledVisitReport>;
  @useResult
  $Res call(
      {String? schemaCode,
      String? version,
      String? submittedBy,
      @EpochDateTimeConverter() DateTime? submittedAt,
      String? otpReference,
      @EpochDateTimeConverter() DateTime? otpVerifiedAt,
      Map<String, dynamic>? responses,
      List<Document>? documents,
      Map<String, dynamic>? additionalDetails});
}

/// @nodoc
class _$ScheduledVisitReportCopyWithImpl<$Res,
        $Val extends ScheduledVisitReport>
    implements $ScheduledVisitReportCopyWith<$Res> {
  _$ScheduledVisitReportCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? schemaCode = freezed,
    Object? version = freezed,
    Object? submittedBy = freezed,
    Object? submittedAt = freezed,
    Object? otpReference = freezed,
    Object? otpVerifiedAt = freezed,
    Object? responses = freezed,
    Object? documents = freezed,
    Object? additionalDetails = freezed,
  }) {
    return _then(_value.copyWith(
      schemaCode: freezed == schemaCode
          ? _value.schemaCode
          : schemaCode // ignore: cast_nullable_to_non_nullable
              as String?,
      version: freezed == version
          ? _value.version
          : version // ignore: cast_nullable_to_non_nullable
              as String?,
      submittedBy: freezed == submittedBy
          ? _value.submittedBy
          : submittedBy // ignore: cast_nullable_to_non_nullable
              as String?,
      submittedAt: freezed == submittedAt
          ? _value.submittedAt
          : submittedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      otpReference: freezed == otpReference
          ? _value.otpReference
          : otpReference // ignore: cast_nullable_to_non_nullable
              as String?,
      otpVerifiedAt: freezed == otpVerifiedAt
          ? _value.otpVerifiedAt
          : otpVerifiedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      responses: freezed == responses
          ? _value.responses
          : responses // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      documents: freezed == documents
          ? _value.documents
          : documents // ignore: cast_nullable_to_non_nullable
              as List<Document>?,
      additionalDetails: freezed == additionalDetails
          ? _value.additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ScheduledVisitReportImplCopyWith<$Res>
    implements $ScheduledVisitReportCopyWith<$Res> {
  factory _$$ScheduledVisitReportImplCopyWith(_$ScheduledVisitReportImpl value,
          $Res Function(_$ScheduledVisitReportImpl) then) =
      __$$ScheduledVisitReportImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? schemaCode,
      String? version,
      String? submittedBy,
      @EpochDateTimeConverter() DateTime? submittedAt,
      String? otpReference,
      @EpochDateTimeConverter() DateTime? otpVerifiedAt,
      Map<String, dynamic>? responses,
      List<Document>? documents,
      Map<String, dynamic>? additionalDetails});
}

/// @nodoc
class __$$ScheduledVisitReportImplCopyWithImpl<$Res>
    extends _$ScheduledVisitReportCopyWithImpl<$Res, _$ScheduledVisitReportImpl>
    implements _$$ScheduledVisitReportImplCopyWith<$Res> {
  __$$ScheduledVisitReportImplCopyWithImpl(_$ScheduledVisitReportImpl _value,
      $Res Function(_$ScheduledVisitReportImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? schemaCode = freezed,
    Object? version = freezed,
    Object? submittedBy = freezed,
    Object? submittedAt = freezed,
    Object? otpReference = freezed,
    Object? otpVerifiedAt = freezed,
    Object? responses = freezed,
    Object? documents = freezed,
    Object? additionalDetails = freezed,
  }) {
    return _then(_$ScheduledVisitReportImpl(
      schemaCode: freezed == schemaCode
          ? _value.schemaCode
          : schemaCode // ignore: cast_nullable_to_non_nullable
              as String?,
      version: freezed == version
          ? _value.version
          : version // ignore: cast_nullable_to_non_nullable
              as String?,
      submittedBy: freezed == submittedBy
          ? _value.submittedBy
          : submittedBy // ignore: cast_nullable_to_non_nullable
              as String?,
      submittedAt: freezed == submittedAt
          ? _value.submittedAt
          : submittedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      otpReference: freezed == otpReference
          ? _value.otpReference
          : otpReference // ignore: cast_nullable_to_non_nullable
              as String?,
      otpVerifiedAt: freezed == otpVerifiedAt
          ? _value.otpVerifiedAt
          : otpVerifiedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      responses: freezed == responses
          ? _value._responses
          : responses // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      documents: freezed == documents
          ? _value._documents
          : documents // ignore: cast_nullable_to_non_nullable
              as List<Document>?,
      additionalDetails: freezed == additionalDetails
          ? _value._additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ScheduledVisitReportImpl implements _ScheduledVisitReport {
  const _$ScheduledVisitReportImpl(
      {this.schemaCode,
      this.version,
      this.submittedBy,
      @EpochDateTimeConverter() this.submittedAt,
      this.otpReference,
      @EpochDateTimeConverter() this.otpVerifiedAt,
      final Map<String, dynamic>? responses,
      final List<Document>? documents,
      final Map<String, dynamic>? additionalDetails})
      : _responses = responses,
        _documents = documents,
        _additionalDetails = additionalDetails;

  factory _$ScheduledVisitReportImpl.fromJson(Map<String, dynamic> json) =>
      _$$ScheduledVisitReportImplFromJson(json);

  @override
  final String? schemaCode;
  @override
  final String? version;
  @override
  final String? submittedBy;
  @override
  @EpochDateTimeConverter()
  final DateTime? submittedAt;
  @override
  final String? otpReference;
  @override
  @EpochDateTimeConverter()
  final DateTime? otpVerifiedAt;

  /// Flexible { key: value, ... } map
  final Map<String, dynamic>? _responses;

  /// Flexible { key: value, ... } map
  @override
  Map<String, dynamic>? get responses {
    final value = _responses;
    if (value == null) return null;
    if (_responses is EqualUnmodifiableMapView) return _responses;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  /// Reuse Document from workflow model
  final List<Document>? _documents;

  /// Reuse Document from workflow model
  @override
  List<Document>? get documents {
    final value = _documents;
    if (value == null) return null;
    if (_documents is EqualUnmodifiableListView) return _documents;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(value);
  }

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
    return 'ScheduledVisitReport(schemaCode: $schemaCode, version: $version, submittedBy: $submittedBy, submittedAt: $submittedAt, otpReference: $otpReference, otpVerifiedAt: $otpVerifiedAt, responses: $responses, documents: $documents, additionalDetails: $additionalDetails)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ScheduledVisitReportImpl &&
            (identical(other.schemaCode, schemaCode) ||
                other.schemaCode == schemaCode) &&
            (identical(other.version, version) || other.version == version) &&
            (identical(other.submittedBy, submittedBy) ||
                other.submittedBy == submittedBy) &&
            (identical(other.submittedAt, submittedAt) ||
                other.submittedAt == submittedAt) &&
            (identical(other.otpReference, otpReference) ||
                other.otpReference == otpReference) &&
            (identical(other.otpVerifiedAt, otpVerifiedAt) ||
                other.otpVerifiedAt == otpVerifiedAt) &&
            const DeepCollectionEquality()
                .equals(other._responses, _responses) &&
            const DeepCollectionEquality()
                .equals(other._documents, _documents) &&
            const DeepCollectionEquality()
                .equals(other._additionalDetails, _additionalDetails));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      schemaCode,
      version,
      submittedBy,
      submittedAt,
      otpReference,
      otpVerifiedAt,
      const DeepCollectionEquality().hash(_responses),
      const DeepCollectionEquality().hash(_documents),
      const DeepCollectionEquality().hash(_additionalDetails));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ScheduledVisitReportImplCopyWith<_$ScheduledVisitReportImpl>
      get copyWith =>
          __$$ScheduledVisitReportImplCopyWithImpl<_$ScheduledVisitReportImpl>(
              this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ScheduledVisitReportImplToJson(
      this,
    );
  }
}

abstract class _ScheduledVisitReport implements ScheduledVisitReport {
  const factory _ScheduledVisitReport(
          {final String? schemaCode,
          final String? version,
          final String? submittedBy,
          @EpochDateTimeConverter() final DateTime? submittedAt,
          final String? otpReference,
          @EpochDateTimeConverter() final DateTime? otpVerifiedAt,
          final Map<String, dynamic>? responses,
          final List<Document>? documents,
          final Map<String, dynamic>? additionalDetails}) =
      _$ScheduledVisitReportImpl;

  factory _ScheduledVisitReport.fromJson(Map<String, dynamic> json) =
      _$ScheduledVisitReportImpl.fromJson;

  @override
  String? get schemaCode;
  @override
  String? get version;
  @override
  String? get submittedBy;
  @override
  @EpochDateTimeConverter()
  DateTime? get submittedAt;
  @override
  String? get otpReference;
  @override
  @EpochDateTimeConverter()
  DateTime? get otpVerifiedAt;
  @override

  /// Flexible { key: value, ... } map
  Map<String, dynamic>? get responses;
  @override

  /// Reuse Document from workflow model
  List<Document>? get documents;
  @override
  Map<String, dynamic>? get additionalDetails;
  @override
  @JsonKey(ignore: true)
  _$$ScheduledVisitReportImplCopyWith<_$ScheduledVisitReportImpl>
      get copyWith => throw _privateConstructorUsedError;
}

ScheduledVisitAssignment _$ScheduledVisitAssignmentFromJson(
    Map<String, dynamic> json) {
  return _ScheduledVisitAssignment.fromJson(json);
}

/// @nodoc
mixin _$ScheduledVisitAssignment {
  String? get id => throw _privateConstructorUsedError;
  String? get tenantId => throw _privateConstructorUsedError;
  String? get scheduledVisitId => throw _privateConstructorUsedError;
  String? get assignedUser => throw _privateConstructorUsedError;
  Map<String, dynamic>? get additionalDetails =>
      throw _privateConstructorUsedError;
  AuditDetails? get auditDetails => throw _privateConstructorUsedError;
  bool? get isActive => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $ScheduledVisitAssignmentCopyWith<ScheduledVisitAssignment> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ScheduledVisitAssignmentCopyWith<$Res> {
  factory $ScheduledVisitAssignmentCopyWith(ScheduledVisitAssignment value,
          $Res Function(ScheduledVisitAssignment) then) =
      _$ScheduledVisitAssignmentCopyWithImpl<$Res, ScheduledVisitAssignment>;
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? scheduledVisitId,
      String? assignedUser,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails,
      bool? isActive});

  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class _$ScheduledVisitAssignmentCopyWithImpl<$Res,
        $Val extends ScheduledVisitAssignment>
    implements $ScheduledVisitAssignmentCopyWith<$Res> {
  _$ScheduledVisitAssignmentCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? scheduledVisitId = freezed,
    Object? assignedUser = freezed,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
    Object? isActive = freezed,
  }) {
    return _then(_value.copyWith(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      scheduledVisitId: freezed == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String?,
      assignedUser: freezed == assignedUser
          ? _value.assignedUser
          : assignedUser // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value.additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
    ) as $Val);
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
abstract class _$$ScheduledVisitAssignmentImplCopyWith<$Res>
    implements $ScheduledVisitAssignmentCopyWith<$Res> {
  factory _$$ScheduledVisitAssignmentImplCopyWith(
          _$ScheduledVisitAssignmentImpl value,
          $Res Function(_$ScheduledVisitAssignmentImpl) then) =
      __$$ScheduledVisitAssignmentImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? id,
      String? tenantId,
      String? scheduledVisitId,
      String? assignedUser,
      Map<String, dynamic>? additionalDetails,
      AuditDetails? auditDetails,
      bool? isActive});

  @override
  $AuditDetailsCopyWith<$Res>? get auditDetails;
}

/// @nodoc
class __$$ScheduledVisitAssignmentImplCopyWithImpl<$Res>
    extends _$ScheduledVisitAssignmentCopyWithImpl<$Res,
        _$ScheduledVisitAssignmentImpl>
    implements _$$ScheduledVisitAssignmentImplCopyWith<$Res> {
  __$$ScheduledVisitAssignmentImplCopyWithImpl(
      _$ScheduledVisitAssignmentImpl _value,
      $Res Function(_$ScheduledVisitAssignmentImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = freezed,
    Object? tenantId = freezed,
    Object? scheduledVisitId = freezed,
    Object? assignedUser = freezed,
    Object? additionalDetails = freezed,
    Object? auditDetails = freezed,
    Object? isActive = freezed,
  }) {
    return _then(_$ScheduledVisitAssignmentImpl(
      id: freezed == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String?,
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      scheduledVisitId: freezed == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String?,
      assignedUser: freezed == assignedUser
          ? _value.assignedUser
          : assignedUser // ignore: cast_nullable_to_non_nullable
              as String?,
      additionalDetails: freezed == additionalDetails
          ? _value._additionalDetails
          : additionalDetails // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      auditDetails: freezed == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails?,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ScheduledVisitAssignmentImpl implements _ScheduledVisitAssignment {
  const _$ScheduledVisitAssignmentImpl(
      {this.id,
      this.tenantId,
      this.scheduledVisitId,
      this.assignedUser,
      final Map<String, dynamic>? additionalDetails,
      this.auditDetails,
      this.isActive})
      : _additionalDetails = additionalDetails;

  factory _$ScheduledVisitAssignmentImpl.fromJson(Map<String, dynamic> json) =>
      _$$ScheduledVisitAssignmentImplFromJson(json);

  @override
  final String? id;
  @override
  final String? tenantId;
  @override
  final String? scheduledVisitId;
  @override
  final String? assignedUser;
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
  final AuditDetails? auditDetails;
  @override
  final bool? isActive;

  @override
  String toString() {
    return 'ScheduledVisitAssignment(id: $id, tenantId: $tenantId, scheduledVisitId: $scheduledVisitId, assignedUser: $assignedUser, additionalDetails: $additionalDetails, auditDetails: $auditDetails, isActive: $isActive)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ScheduledVisitAssignmentImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            (identical(other.scheduledVisitId, scheduledVisitId) ||
                other.scheduledVisitId == scheduledVisitId) &&
            (identical(other.assignedUser, assignedUser) ||
                other.assignedUser == assignedUser) &&
            const DeepCollectionEquality()
                .equals(other._additionalDetails, _additionalDetails) &&
            (identical(other.auditDetails, auditDetails) ||
                other.auditDetails == auditDetails) &&
            (identical(other.isActive, isActive) ||
                other.isActive == isActive));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      tenantId,
      scheduledVisitId,
      assignedUser,
      const DeepCollectionEquality().hash(_additionalDetails),
      auditDetails,
      isActive);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ScheduledVisitAssignmentImplCopyWith<_$ScheduledVisitAssignmentImpl>
      get copyWith => __$$ScheduledVisitAssignmentImplCopyWithImpl<
          _$ScheduledVisitAssignmentImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ScheduledVisitAssignmentImplToJson(
      this,
    );
  }
}

abstract class _ScheduledVisitAssignment implements ScheduledVisitAssignment {
  const factory _ScheduledVisitAssignment(
      {final String? id,
      final String? tenantId,
      final String? scheduledVisitId,
      final String? assignedUser,
      final Map<String, dynamic>? additionalDetails,
      final AuditDetails? auditDetails,
      final bool? isActive}) = _$ScheduledVisitAssignmentImpl;

  factory _ScheduledVisitAssignment.fromJson(Map<String, dynamic> json) =
      _$ScheduledVisitAssignmentImpl.fromJson;

  @override
  String? get id;
  @override
  String? get tenantId;
  @override
  String? get scheduledVisitId;
  @override
  String? get assignedUser;
  @override
  Map<String, dynamic>? get additionalDetails;
  @override
  AuditDetails? get auditDetails;
  @override
  bool? get isActive;
  @override
  @JsonKey(ignore: true)
  _$$ScheduledVisitAssignmentImplCopyWith<_$ScheduledVisitAssignmentImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ScheduledVisitSearchCriteria {
  String? get tenantId => throw _privateConstructorUsedError;
  String? get facilityId => throw _privateConstructorUsedError;
  String? get amcConfigurationId => throw _privateConstructorUsedError;
  List<String> get statuses => throw _privateConstructorUsedError;
  int? get visitNumber => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get scheduledFrom => throw _privateConstructorUsedError;
  @EpochDateTimeConverter()
  DateTime? get scheduledTo => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ScheduledVisitSearchCriteriaCopyWith<ScheduledVisitSearchCriteria>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ScheduledVisitSearchCriteriaCopyWith<$Res> {
  factory $ScheduledVisitSearchCriteriaCopyWith(
          ScheduledVisitSearchCriteria value,
          $Res Function(ScheduledVisitSearchCriteria) then) =
      _$ScheduledVisitSearchCriteriaCopyWithImpl<$Res,
          ScheduledVisitSearchCriteria>;
  @useResult
  $Res call(
      {String? tenantId,
      String? facilityId,
      String? amcConfigurationId,
      List<String> statuses,
      int? visitNumber,
      @EpochDateTimeConverter() DateTime? scheduledFrom,
      @EpochDateTimeConverter() DateTime? scheduledTo});
}

/// @nodoc
class _$ScheduledVisitSearchCriteriaCopyWithImpl<$Res,
        $Val extends ScheduledVisitSearchCriteria>
    implements $ScheduledVisitSearchCriteriaCopyWith<$Res> {
  _$ScheduledVisitSearchCriteriaCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? tenantId = freezed,
    Object? facilityId = freezed,
    Object? amcConfigurationId = freezed,
    Object? statuses = null,
    Object? visitNumber = freezed,
    Object? scheduledFrom = freezed,
    Object? scheduledTo = freezed,
  }) {
    return _then(_value.copyWith(
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      facilityId: freezed == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfigurationId: freezed == amcConfigurationId
          ? _value.amcConfigurationId
          : amcConfigurationId // ignore: cast_nullable_to_non_nullable
              as String?,
      statuses: null == statuses
          ? _value.statuses
          : statuses // ignore: cast_nullable_to_non_nullable
              as List<String>,
      visitNumber: freezed == visitNumber
          ? _value.visitNumber
          : visitNumber // ignore: cast_nullable_to_non_nullable
              as int?,
      scheduledFrom: freezed == scheduledFrom
          ? _value.scheduledFrom
          : scheduledFrom // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      scheduledTo: freezed == scheduledTo
          ? _value.scheduledTo
          : scheduledTo // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ScheduledVisitSearchCriteriaImplCopyWith<$Res>
    implements $ScheduledVisitSearchCriteriaCopyWith<$Res> {
  factory _$$ScheduledVisitSearchCriteriaImplCopyWith(
          _$ScheduledVisitSearchCriteriaImpl value,
          $Res Function(_$ScheduledVisitSearchCriteriaImpl) then) =
      __$$ScheduledVisitSearchCriteriaImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? tenantId,
      String? facilityId,
      String? amcConfigurationId,
      List<String> statuses,
      int? visitNumber,
      @EpochDateTimeConverter() DateTime? scheduledFrom,
      @EpochDateTimeConverter() DateTime? scheduledTo});
}

/// @nodoc
class __$$ScheduledVisitSearchCriteriaImplCopyWithImpl<$Res>
    extends _$ScheduledVisitSearchCriteriaCopyWithImpl<$Res,
        _$ScheduledVisitSearchCriteriaImpl>
    implements _$$ScheduledVisitSearchCriteriaImplCopyWith<$Res> {
  __$$ScheduledVisitSearchCriteriaImplCopyWithImpl(
      _$ScheduledVisitSearchCriteriaImpl _value,
      $Res Function(_$ScheduledVisitSearchCriteriaImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? tenantId = freezed,
    Object? facilityId = freezed,
    Object? amcConfigurationId = freezed,
    Object? statuses = null,
    Object? visitNumber = freezed,
    Object? scheduledFrom = freezed,
    Object? scheduledTo = freezed,
  }) {
    return _then(_$ScheduledVisitSearchCriteriaImpl(
      tenantId: freezed == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String?,
      facilityId: freezed == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
              as String?,
      amcConfigurationId: freezed == amcConfigurationId
          ? _value.amcConfigurationId
          : amcConfigurationId // ignore: cast_nullable_to_non_nullable
              as String?,
      statuses: null == statuses
          ? _value._statuses
          : statuses // ignore: cast_nullable_to_non_nullable
              as List<String>,
      visitNumber: freezed == visitNumber
          ? _value.visitNumber
          : visitNumber // ignore: cast_nullable_to_non_nullable
              as int?,
      scheduledFrom: freezed == scheduledFrom
          ? _value.scheduledFrom
          : scheduledFrom // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      scheduledTo: freezed == scheduledTo
          ? _value.scheduledTo
          : scheduledTo // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc

class _$ScheduledVisitSearchCriteriaImpl extends _ScheduledVisitSearchCriteria {
  const _$ScheduledVisitSearchCriteriaImpl(
      {this.tenantId,
      this.facilityId,
      this.amcConfigurationId,
      final List<String> statuses = const <String>[],
      this.visitNumber,
      @EpochDateTimeConverter() this.scheduledFrom,
      @EpochDateTimeConverter() this.scheduledTo})
      : _statuses = statuses,
        super._();

  @override
  final String? tenantId;
  @override
  final String? facilityId;
  @override
  final String? amcConfigurationId;
  final List<String> _statuses;
  @override
  @JsonKey()
  List<String> get statuses {
    if (_statuses is EqualUnmodifiableListView) return _statuses;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_statuses);
  }

  @override
  final int? visitNumber;
  @override
  @EpochDateTimeConverter()
  final DateTime? scheduledFrom;
  @override
  @EpochDateTimeConverter()
  final DateTime? scheduledTo;

  @override
  String toString() {
    return 'ScheduledVisitSearchCriteria(tenantId: $tenantId, facilityId: $facilityId, amcConfigurationId: $amcConfigurationId, statuses: $statuses, visitNumber: $visitNumber, scheduledFrom: $scheduledFrom, scheduledTo: $scheduledTo)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ScheduledVisitSearchCriteriaImpl &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            (identical(other.facilityId, facilityId) ||
                other.facilityId == facilityId) &&
            (identical(other.amcConfigurationId, amcConfigurationId) ||
                other.amcConfigurationId == amcConfigurationId) &&
            const DeepCollectionEquality().equals(other._statuses, _statuses) &&
            (identical(other.visitNumber, visitNumber) ||
                other.visitNumber == visitNumber) &&
            (identical(other.scheduledFrom, scheduledFrom) ||
                other.scheduledFrom == scheduledFrom) &&
            (identical(other.scheduledTo, scheduledTo) ||
                other.scheduledTo == scheduledTo));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      tenantId,
      facilityId,
      amcConfigurationId,
      const DeepCollectionEquality().hash(_statuses),
      visitNumber,
      scheduledFrom,
      scheduledTo);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ScheduledVisitSearchCriteriaImplCopyWith<
          _$ScheduledVisitSearchCriteriaImpl>
      get copyWith => __$$ScheduledVisitSearchCriteriaImplCopyWithImpl<
          _$ScheduledVisitSearchCriteriaImpl>(this, _$identity);
}

abstract class _ScheduledVisitSearchCriteria
    extends ScheduledVisitSearchCriteria {
  const factory _ScheduledVisitSearchCriteria(
          {final String? tenantId,
          final String? facilityId,
          final String? amcConfigurationId,
          final List<String> statuses,
          final int? visitNumber,
          @EpochDateTimeConverter() final DateTime? scheduledFrom,
          @EpochDateTimeConverter() final DateTime? scheduledTo}) =
      _$ScheduledVisitSearchCriteriaImpl;
  const _ScheduledVisitSearchCriteria._() : super._();

  @override
  String? get tenantId;
  @override
  String? get facilityId;
  @override
  String? get amcConfigurationId;
  @override
  List<String> get statuses;
  @override
  int? get visitNumber;
  @override
  @EpochDateTimeConverter()
  DateTime? get scheduledFrom;
  @override
  @EpochDateTimeConverter()
  DateTime? get scheduledTo;
  @override
  @JsonKey(ignore: true)
  _$$ScheduledVisitSearchCriteriaImplCopyWith<
          _$ScheduledVisitSearchCriteriaImpl>
      get copyWith => throw _privateConstructorUsedError;
}
