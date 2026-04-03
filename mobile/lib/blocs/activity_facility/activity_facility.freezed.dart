// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'activity_facility.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$ActivityFacilityEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ActivityFacilityEventCopyWith<$Res> {
  factory $ActivityFacilityEventCopyWith(ActivityFacilityEvent value,
          $Res Function(ActivityFacilityEvent) then) =
      _$ActivityFacilityEventCopyWithImpl<$Res, ActivityFacilityEvent>;
}

/// @nodoc
class _$ActivityFacilityEventCopyWithImpl<$Res,
        $Val extends ActivityFacilityEvent>
    implements $ActivityFacilityEventCopyWith<$Res> {
  _$ActivityFacilityEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$ActivityFacilitySelectEventImplCopyWith<$Res> {
  factory _$$ActivityFacilitySelectEventImplCopyWith(
          _$ActivityFacilitySelectEventImpl value,
          $Res Function(_$ActivityFacilitySelectEventImpl) then) =
      __$$ActivityFacilitySelectEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId});
}

/// @nodoc
class __$$ActivityFacilitySelectEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$ActivityFacilitySelectEventImpl>
    implements _$$ActivityFacilitySelectEventImplCopyWith<$Res> {
  __$$ActivityFacilitySelectEventImplCopyWithImpl(
      _$ActivityFacilitySelectEventImpl _value,
      $Res Function(_$ActivityFacilitySelectEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
  }) {
    return _then(_$ActivityFacilitySelectEventImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ActivityFacilitySelectEventImpl implements ActivityFacilitySelectEvent {
  const _$ActivityFacilitySelectEventImpl(this.activityFacilityId);

  @override
  final String activityFacilityId;

  @override
  String toString() {
    return 'ActivityFacilityEvent.selectActivityFacility(activityFacilityId: $activityFacilityId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilitySelectEventImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilitySelectEventImplCopyWith<_$ActivityFacilitySelectEventImpl>
      get copyWith => __$$ActivityFacilitySelectEventImplCopyWithImpl<
          _$ActivityFacilitySelectEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return selectActivityFacility(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return selectActivityFacility?.call(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (selectActivityFacility != null) {
      return selectActivityFacility(activityFacilityId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return selectActivityFacility(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return selectActivityFacility?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (selectActivityFacility != null) {
      return selectActivityFacility(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilitySelectEvent implements ActivityFacilityEvent {
  const factory ActivityFacilitySelectEvent(final String activityFacilityId) =
      _$ActivityFacilitySelectEventImpl;

  String get activityFacilityId;
  @JsonKey(ignore: true)
  _$$ActivityFacilitySelectEventImplCopyWith<_$ActivityFacilitySelectEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FetchActivityFacilityByWorkflowEventImplCopyWith<$Res> {
  factory _$$FetchActivityFacilityByWorkflowEventImplCopyWith(
          _$FetchActivityFacilityByWorkflowEventImpl value,
          $Res Function(_$FetchActivityFacilityByWorkflowEventImpl) then) =
      __$$FetchActivityFacilityByWorkflowEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<String> workflowStatuses});
}

/// @nodoc
class __$$FetchActivityFacilityByWorkflowEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$FetchActivityFacilityByWorkflowEventImpl>
    implements _$$FetchActivityFacilityByWorkflowEventImplCopyWith<$Res> {
  __$$FetchActivityFacilityByWorkflowEventImplCopyWithImpl(
      _$FetchActivityFacilityByWorkflowEventImpl _value,
      $Res Function(_$FetchActivityFacilityByWorkflowEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? workflowStatuses = null,
  }) {
    return _then(_$FetchActivityFacilityByWorkflowEventImpl(
      workflowStatuses: null == workflowStatuses
          ? _value._workflowStatuses
          : workflowStatuses // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc

class _$FetchActivityFacilityByWorkflowEventImpl
    implements FetchActivityFacilityByWorkflowEvent {
  const _$FetchActivityFacilityByWorkflowEventImpl(
      {required final List<String> workflowStatuses})
      : _workflowStatuses = workflowStatuses;

  final List<String> _workflowStatuses;
  @override
  List<String> get workflowStatuses {
    if (_workflowStatuses is EqualUnmodifiableListView)
      return _workflowStatuses;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_workflowStatuses);
  }

  @override
  String toString() {
    return 'ActivityFacilityEvent.fetchActivityFacilityByWorkflow(workflowStatuses: $workflowStatuses)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FetchActivityFacilityByWorkflowEventImpl &&
            const DeepCollectionEquality()
                .equals(other._workflowStatuses, _workflowStatuses));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, const DeepCollectionEquality().hash(_workflowStatuses));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FetchActivityFacilityByWorkflowEventImplCopyWith<
          _$FetchActivityFacilityByWorkflowEventImpl>
      get copyWith => __$$FetchActivityFacilityByWorkflowEventImplCopyWithImpl<
          _$FetchActivityFacilityByWorkflowEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return fetchActivityFacilityByWorkflow(workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return fetchActivityFacilityByWorkflow?.call(workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchActivityFacilityByWorkflow != null) {
      return fetchActivityFacilityByWorkflow(workflowStatuses);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return fetchActivityFacilityByWorkflow(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return fetchActivityFacilityByWorkflow?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchActivityFacilityByWorkflow != null) {
      return fetchActivityFacilityByWorkflow(this);
    }
    return orElse();
  }
}

abstract class FetchActivityFacilityByWorkflowEvent
    implements ActivityFacilityEvent {
  const factory FetchActivityFacilityByWorkflowEvent(
          {required final List<String> workflowStatuses}) =
      _$FetchActivityFacilityByWorkflowEventImpl;

  List<String> get workflowStatuses;
  @JsonKey(ignore: true)
  _$$FetchActivityFacilityByWorkflowEventImplCopyWith<
          _$FetchActivityFacilityByWorkflowEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$AddUnSubmittedEventImplCopyWith<$Res> {
  factory _$$AddUnSubmittedEventImplCopyWith(_$AddUnSubmittedEventImpl value,
          $Res Function(_$AddUnSubmittedEventImpl) then) =
      __$$AddUnSubmittedEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ActivityFacilityWorkflow workflow, String userType});

  $ActivityFacilityWorkflowCopyWith<$Res> get workflow;
}

/// @nodoc
class __$$AddUnSubmittedEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res, _$AddUnSubmittedEventImpl>
    implements _$$AddUnSubmittedEventImplCopyWith<$Res> {
  __$$AddUnSubmittedEventImplCopyWithImpl(_$AddUnSubmittedEventImpl _value,
      $Res Function(_$AddUnSubmittedEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? workflow = null,
    Object? userType = null,
  }) {
    return _then(_$AddUnSubmittedEventImpl(
      null == workflow
          ? _value.workflow
          : workflow // ignore: cast_nullable_to_non_nullable
              as ActivityFacilityWorkflow,
      null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ActivityFacilityWorkflowCopyWith<$Res> get workflow {
    return $ActivityFacilityWorkflowCopyWith<$Res>(_value.workflow, (value) {
      return _then(_value.copyWith(workflow: value));
    });
  }
}

/// @nodoc

class _$AddUnSubmittedEventImpl implements AddUnSubmittedEvent {
  const _$AddUnSubmittedEventImpl(this.workflow, this.userType);

  @override
  final ActivityFacilityWorkflow workflow;
  @override
  final String userType;

  @override
  String toString() {
    return 'ActivityFacilityEvent.addUnSubmitted(workflow: $workflow, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AddUnSubmittedEventImpl &&
            (identical(other.workflow, workflow) ||
                other.workflow == workflow) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, workflow, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AddUnSubmittedEventImplCopyWith<_$AddUnSubmittedEventImpl> get copyWith =>
      __$$AddUnSubmittedEventImplCopyWithImpl<_$AddUnSubmittedEventImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return addUnSubmitted(workflow, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return addUnSubmitted?.call(workflow, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (addUnSubmitted != null) {
      return addUnSubmitted(workflow, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return addUnSubmitted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return addUnSubmitted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (addUnSubmitted != null) {
      return addUnSubmitted(this);
    }
    return orElse();
  }
}

abstract class AddUnSubmittedEvent implements ActivityFacilityEvent {
  const factory AddUnSubmittedEvent(
          final ActivityFacilityWorkflow workflow, final String userType) =
      _$AddUnSubmittedEventImpl;

  ActivityFacilityWorkflow get workflow;
  String get userType;
  @JsonKey(ignore: true)
  _$$AddUnSubmittedEventImplCopyWith<_$AddUnSubmittedEventImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$LoadUnSubmittedEventImplCopyWith<$Res> {
  factory _$$LoadUnSubmittedEventImplCopyWith(_$LoadUnSubmittedEventImpl value,
          $Res Function(_$LoadUnSubmittedEventImpl) then) =
      __$$LoadUnSubmittedEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<String> statuses, String userType});
}

/// @nodoc
class __$$LoadUnSubmittedEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$LoadUnSubmittedEventImpl>
    implements _$$LoadUnSubmittedEventImplCopyWith<$Res> {
  __$$LoadUnSubmittedEventImplCopyWithImpl(_$LoadUnSubmittedEventImpl _value,
      $Res Function(_$LoadUnSubmittedEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? statuses = null,
    Object? userType = null,
  }) {
    return _then(_$LoadUnSubmittedEventImpl(
      null == statuses
          ? _value._statuses
          : statuses // ignore: cast_nullable_to_non_nullable
              as List<String>,
      null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$LoadUnSubmittedEventImpl implements LoadUnSubmittedEvent {
  const _$LoadUnSubmittedEventImpl(final List<String> statuses, this.userType)
      : _statuses = statuses;

  final List<String> _statuses;
  @override
  List<String> get statuses {
    if (_statuses is EqualUnmodifiableListView) return _statuses;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_statuses);
  }

  @override
  final String userType;

  @override
  String toString() {
    return 'ActivityFacilityEvent.loadUnSubmitted(statuses: $statuses, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$LoadUnSubmittedEventImpl &&
            const DeepCollectionEquality().equals(other._statuses, _statuses) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, const DeepCollectionEquality().hash(_statuses), userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$LoadUnSubmittedEventImplCopyWith<_$LoadUnSubmittedEventImpl>
      get copyWith =>
          __$$LoadUnSubmittedEventImplCopyWithImpl<_$LoadUnSubmittedEventImpl>(
              this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return loadUnSubmitted(statuses, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return loadUnSubmitted?.call(statuses, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (loadUnSubmitted != null) {
      return loadUnSubmitted(statuses, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return loadUnSubmitted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return loadUnSubmitted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (loadUnSubmitted != null) {
      return loadUnSubmitted(this);
    }
    return orElse();
  }
}

abstract class LoadUnSubmittedEvent implements ActivityFacilityEvent {
  const factory LoadUnSubmittedEvent(
          final List<String> statuses, final String userType) =
      _$LoadUnSubmittedEventImpl;

  List<String> get statuses;
  String get userType;
  @JsonKey(ignore: true)
  _$$LoadUnSubmittedEventImplCopyWith<_$LoadUnSubmittedEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$DeleteUnSubmittedEventImplCopyWith<$Res> {
  factory _$$DeleteUnSubmittedEventImplCopyWith(
          _$DeleteUnSubmittedEventImpl value,
          $Res Function(_$DeleteUnSubmittedEventImpl) then) =
      __$$DeleteUnSubmittedEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId, String userType});
}

/// @nodoc
class __$$DeleteUnSubmittedEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$DeleteUnSubmittedEventImpl>
    implements _$$DeleteUnSubmittedEventImplCopyWith<$Res> {
  __$$DeleteUnSubmittedEventImplCopyWithImpl(
      _$DeleteUnSubmittedEventImpl _value,
      $Res Function(_$DeleteUnSubmittedEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? userType = null,
  }) {
    return _then(_$DeleteUnSubmittedEventImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$DeleteUnSubmittedEventImpl implements DeleteUnSubmittedEvent {
  const _$DeleteUnSubmittedEventImpl(this.activityFacilityId, this.userType);

  @override
  final String activityFacilityId;
  @override
  final String userType;

  @override
  String toString() {
    return 'ActivityFacilityEvent.deleteUnSubmitted(activityFacilityId: $activityFacilityId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DeleteUnSubmittedEventImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DeleteUnSubmittedEventImplCopyWith<_$DeleteUnSubmittedEventImpl>
      get copyWith => __$$DeleteUnSubmittedEventImplCopyWithImpl<
          _$DeleteUnSubmittedEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return deleteUnSubmitted(activityFacilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return deleteUnSubmitted?.call(activityFacilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (deleteUnSubmitted != null) {
      return deleteUnSubmitted(activityFacilityId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return deleteUnSubmitted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return deleteUnSubmitted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (deleteUnSubmitted != null) {
      return deleteUnSubmitted(this);
    }
    return orElse();
  }
}

abstract class DeleteUnSubmittedEvent implements ActivityFacilityEvent {
  const factory DeleteUnSubmittedEvent(
          final String activityFacilityId, final String userType) =
      _$DeleteUnSubmittedEventImpl;

  String get activityFacilityId;
  String get userType;
  @JsonKey(ignore: true)
  _$$DeleteUnSubmittedEventImplCopyWith<_$DeleteUnSubmittedEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FetchAllReportCountsEventImplCopyWith<$Res> {
  factory _$$FetchAllReportCountsEventImplCopyWith(
          _$FetchAllReportCountsEventImpl value,
          $Res Function(_$FetchAllReportCountsEventImpl) then) =
      __$$FetchAllReportCountsEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String userType});
}

/// @nodoc
class __$$FetchAllReportCountsEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$FetchAllReportCountsEventImpl>
    implements _$$FetchAllReportCountsEventImplCopyWith<$Res> {
  __$$FetchAllReportCountsEventImplCopyWithImpl(
      _$FetchAllReportCountsEventImpl _value,
      $Res Function(_$FetchAllReportCountsEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? userType = null,
  }) {
    return _then(_$FetchAllReportCountsEventImpl(
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$FetchAllReportCountsEventImpl implements FetchAllReportCountsEvent {
  const _$FetchAllReportCountsEventImpl({required this.userType});

  @override
  final String userType;

  @override
  String toString() {
    return 'ActivityFacilityEvent.fetchAllReportCounts(userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FetchAllReportCountsEventImpl &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FetchAllReportCountsEventImplCopyWith<_$FetchAllReportCountsEventImpl>
      get copyWith => __$$FetchAllReportCountsEventImplCopyWithImpl<
          _$FetchAllReportCountsEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return fetchAllReportCounts(userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return fetchAllReportCounts?.call(userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchAllReportCounts != null) {
      return fetchAllReportCounts(userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return fetchAllReportCounts(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return fetchAllReportCounts?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchAllReportCounts != null) {
      return fetchAllReportCounts(this);
    }
    return orElse();
  }
}

abstract class FetchAllReportCountsEvent implements ActivityFacilityEvent {
  const factory FetchAllReportCountsEvent({required final String userType}) =
      _$FetchAllReportCountsEventImpl;

  String get userType;
  @JsonKey(ignore: true)
  _$$FetchAllReportCountsEventImplCopyWith<_$FetchAllReportCountsEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$GetNewlyAssignedEventImplCopyWith<$Res> {
  factory _$$GetNewlyAssignedEventImplCopyWith(
          _$GetNewlyAssignedEventImpl value,
          $Res Function(_$GetNewlyAssignedEventImpl) then) =
      __$$GetNewlyAssignedEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String userType});
}

/// @nodoc
class __$$GetNewlyAssignedEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$GetNewlyAssignedEventImpl>
    implements _$$GetNewlyAssignedEventImplCopyWith<$Res> {
  __$$GetNewlyAssignedEventImplCopyWithImpl(_$GetNewlyAssignedEventImpl _value,
      $Res Function(_$GetNewlyAssignedEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? userType = null,
  }) {
    return _then(_$GetNewlyAssignedEventImpl(
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$GetNewlyAssignedEventImpl implements GetNewlyAssignedEvent {
  const _$GetNewlyAssignedEventImpl({required this.userType});

  @override
  final String userType;

  @override
  String toString() {
    return 'ActivityFacilityEvent.getNewlyAssigned(userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GetNewlyAssignedEventImpl &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$GetNewlyAssignedEventImplCopyWith<_$GetNewlyAssignedEventImpl>
      get copyWith => __$$GetNewlyAssignedEventImplCopyWithImpl<
          _$GetNewlyAssignedEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return getNewlyAssigned(userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return getNewlyAssigned?.call(userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (getNewlyAssigned != null) {
      return getNewlyAssigned(userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return getNewlyAssigned(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return getNewlyAssigned?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (getNewlyAssigned != null) {
      return getNewlyAssigned(this);
    }
    return orElse();
  }
}

abstract class GetNewlyAssignedEvent implements ActivityFacilityEvent {
  const factory GetNewlyAssignedEvent({required final String userType}) =
      _$GetNewlyAssignedEventImpl;

  String get userType;
  @JsonKey(ignore: true)
  _$$GetNewlyAssignedEventImplCopyWith<_$GetNewlyAssignedEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FetchActivityFacilitySortedEventImplCopyWith<$Res> {
  factory _$$FetchActivityFacilitySortedEventImplCopyWith(
          _$FetchActivityFacilitySortedEventImpl value,
          $Res Function(_$FetchActivityFacilitySortedEventImpl) then) =
      __$$FetchActivityFacilitySortedEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<String> workflowStatuses, String sortDirection});
}

/// @nodoc
class __$$FetchActivityFacilitySortedEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$FetchActivityFacilitySortedEventImpl>
    implements _$$FetchActivityFacilitySortedEventImplCopyWith<$Res> {
  __$$FetchActivityFacilitySortedEventImplCopyWithImpl(
      _$FetchActivityFacilitySortedEventImpl _value,
      $Res Function(_$FetchActivityFacilitySortedEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? workflowStatuses = null,
    Object? sortDirection = null,
  }) {
    return _then(_$FetchActivityFacilitySortedEventImpl(
      workflowStatuses: null == workflowStatuses
          ? _value._workflowStatuses
          : workflowStatuses // ignore: cast_nullable_to_non_nullable
              as List<String>,
      sortDirection: null == sortDirection
          ? _value.sortDirection
          : sortDirection // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$FetchActivityFacilitySortedEventImpl
    implements FetchActivityFacilitySortedEvent {
  const _$FetchActivityFacilitySortedEventImpl(
      {required final List<String> workflowStatuses,
      required this.sortDirection})
      : _workflowStatuses = workflowStatuses;

  final List<String> _workflowStatuses;
  @override
  List<String> get workflowStatuses {
    if (_workflowStatuses is EqualUnmodifiableListView)
      return _workflowStatuses;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_workflowStatuses);
  }

  @override
  final String sortDirection;

  @override
  String toString() {
    return 'ActivityFacilityEvent.fetchActivityFacilitySorted(workflowStatuses: $workflowStatuses, sortDirection: $sortDirection)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FetchActivityFacilitySortedEventImpl &&
            const DeepCollectionEquality()
                .equals(other._workflowStatuses, _workflowStatuses) &&
            (identical(other.sortDirection, sortDirection) ||
                other.sortDirection == sortDirection));
  }

  @override
  int get hashCode => Object.hash(runtimeType,
      const DeepCollectionEquality().hash(_workflowStatuses), sortDirection);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FetchActivityFacilitySortedEventImplCopyWith<
          _$FetchActivityFacilitySortedEventImpl>
      get copyWith => __$$FetchActivityFacilitySortedEventImplCopyWithImpl<
          _$FetchActivityFacilitySortedEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return fetchActivityFacilitySorted(workflowStatuses, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return fetchActivityFacilitySorted?.call(workflowStatuses, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchActivityFacilitySorted != null) {
      return fetchActivityFacilitySorted(workflowStatuses, sortDirection);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return fetchActivityFacilitySorted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return fetchActivityFacilitySorted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchActivityFacilitySorted != null) {
      return fetchActivityFacilitySorted(this);
    }
    return orElse();
  }
}

abstract class FetchActivityFacilitySortedEvent
    implements ActivityFacilityEvent {
  const factory FetchActivityFacilitySortedEvent(
          {required final List<String> workflowStatuses,
          required final String sortDirection}) =
      _$FetchActivityFacilitySortedEventImpl;

  List<String> get workflowStatuses;
  String get sortDirection;
  @JsonKey(ignore: true)
  _$$FetchActivityFacilitySortedEventImplCopyWith<
          _$FetchActivityFacilitySortedEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FetchActivityFacilityBySearchEventImplCopyWith<$Res> {
  factory _$$FetchActivityFacilityBySearchEventImplCopyWith(
          _$FetchActivityFacilityBySearchEventImpl value,
          $Res Function(_$FetchActivityFacilityBySearchEventImpl) then) =
      __$$FetchActivityFacilityBySearchEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String query, List<String> workflowStatuses});
}

/// @nodoc
class __$$FetchActivityFacilityBySearchEventImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$FetchActivityFacilityBySearchEventImpl>
    implements _$$FetchActivityFacilityBySearchEventImplCopyWith<$Res> {
  __$$FetchActivityFacilityBySearchEventImplCopyWithImpl(
      _$FetchActivityFacilityBySearchEventImpl _value,
      $Res Function(_$FetchActivityFacilityBySearchEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? query = null,
    Object? workflowStatuses = null,
  }) {
    return _then(_$FetchActivityFacilityBySearchEventImpl(
      query: null == query
          ? _value.query
          : query // ignore: cast_nullable_to_non_nullable
              as String,
      workflowStatuses: null == workflowStatuses
          ? _value._workflowStatuses
          : workflowStatuses // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc

class _$FetchActivityFacilityBySearchEventImpl
    implements FetchActivityFacilityBySearchEvent {
  const _$FetchActivityFacilityBySearchEventImpl(
      {required this.query, required final List<String> workflowStatuses})
      : _workflowStatuses = workflowStatuses;

  @override
  final String query;
  final List<String> _workflowStatuses;
  @override
  List<String> get workflowStatuses {
    if (_workflowStatuses is EqualUnmodifiableListView)
      return _workflowStatuses;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_workflowStatuses);
  }

  @override
  String toString() {
    return 'ActivityFacilityEvent.fetchActivityFacilityBySearch(query: $query, workflowStatuses: $workflowStatuses)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FetchActivityFacilityBySearchEventImpl &&
            (identical(other.query, query) || other.query == query) &&
            const DeepCollectionEquality()
                .equals(other._workflowStatuses, _workflowStatuses));
  }

  @override
  int get hashCode => Object.hash(runtimeType, query,
      const DeepCollectionEquality().hash(_workflowStatuses));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FetchActivityFacilityBySearchEventImplCopyWith<
          _$FetchActivityFacilityBySearchEventImpl>
      get copyWith => __$$FetchActivityFacilityBySearchEventImplCopyWithImpl<
          _$FetchActivityFacilityBySearchEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return fetchActivityFacilityBySearch(query, workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return fetchActivityFacilityBySearch?.call(query, workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchActivityFacilityBySearch != null) {
      return fetchActivityFacilityBySearch(query, workflowStatuses);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return fetchActivityFacilityBySearch(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return fetchActivityFacilityBySearch?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (fetchActivityFacilityBySearch != null) {
      return fetchActivityFacilityBySearch(this);
    }
    return orElse();
  }
}

abstract class FetchActivityFacilityBySearchEvent
    implements ActivityFacilityEvent {
  const factory FetchActivityFacilityBySearchEvent(
          {required final String query,
          required final List<String> workflowStatuses}) =
      _$FetchActivityFacilityBySearchEventImpl;

  String get query;
  List<String> get workflowStatuses;
  @JsonKey(ignore: true)
  _$$FetchActivityFacilityBySearchEventImplCopyWith<
          _$FetchActivityFacilityBySearchEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ActivityFacilityCheckIfInCacheImplCopyWith<$Res> {
  factory _$$ActivityFacilityCheckIfInCacheImplCopyWith(
          _$ActivityFacilityCheckIfInCacheImpl value,
          $Res Function(_$ActivityFacilityCheckIfInCacheImpl) then) =
      __$$ActivityFacilityCheckIfInCacheImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId, String userType});
}

/// @nodoc
class __$$ActivityFacilityCheckIfInCacheImplCopyWithImpl<$Res>
    extends _$ActivityFacilityEventCopyWithImpl<$Res,
        _$ActivityFacilityCheckIfInCacheImpl>
    implements _$$ActivityFacilityCheckIfInCacheImplCopyWith<$Res> {
  __$$ActivityFacilityCheckIfInCacheImplCopyWithImpl(
      _$ActivityFacilityCheckIfInCacheImpl _value,
      $Res Function(_$ActivityFacilityCheckIfInCacheImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? userType = null,
  }) {
    return _then(_$ActivityFacilityCheckIfInCacheImpl(
      activityFacilityId: null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ActivityFacilityCheckIfInCacheImpl
    implements ActivityFacilityCheckIfInCache {
  const _$ActivityFacilityCheckIfInCacheImpl(
      {required this.activityFacilityId, required this.userType});

  @override
  final String activityFacilityId;
  @override
  final String userType;

  @override
  String toString() {
    return 'ActivityFacilityEvent.checkIfInCache(activityFacilityId: $activityFacilityId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilityCheckIfInCacheImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilityCheckIfInCacheImplCopyWith<
          _$ActivityFacilityCheckIfInCacheImpl>
      get copyWith => __$$ActivityFacilityCheckIfInCacheImplCopyWithImpl<
          _$ActivityFacilityCheckIfInCacheImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) selectActivityFacility,
    required TResult Function(List<String> workflowStatuses)
        fetchActivityFacilityByWorkflow,
    required TResult Function(
            ActivityFacilityWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String activityFacilityId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchActivityFacilitySorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchActivityFacilityBySearch,
    required TResult Function(String activityFacilityId, String userType)
        checkIfInCache,
  }) {
    return checkIfInCache(activityFacilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? selectActivityFacility,
    TResult? Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult? Function(String activityFacilityId, String userType)?
        checkIfInCache,
  }) {
    return checkIfInCache?.call(activityFacilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? selectActivityFacility,
    TResult Function(List<String> workflowStatuses)?
        fetchActivityFacilityByWorkflow,
    TResult Function(ActivityFacilityWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String activityFacilityId, String userType)?
        deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchActivityFacilitySorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchActivityFacilityBySearch,
    TResult Function(String activityFacilityId, String userType)?
        checkIfInCache,
    required TResult orElse(),
  }) {
    if (checkIfInCache != null) {
      return checkIfInCache(activityFacilityId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelectEvent value)
        selectActivityFacility,
    required TResult Function(FetchActivityFacilityByWorkflowEvent value)
        fetchActivityFacilityByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchActivityFacilitySortedEvent value)
        fetchActivityFacilitySorted,
    required TResult Function(FetchActivityFacilityBySearchEvent value)
        fetchActivityFacilityBySearch,
    required TResult Function(ActivityFacilityCheckIfInCache value)
        checkIfInCache,
  }) {
    return checkIfInCache(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelectEvent value)?
        selectActivityFacility,
    TResult? Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult? Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult? Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
  }) {
    return checkIfInCache?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelectEvent value)? selectActivityFacility,
    TResult Function(FetchActivityFacilityByWorkflowEvent value)?
        fetchActivityFacilityByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchActivityFacilitySortedEvent value)?
        fetchActivityFacilitySorted,
    TResult Function(FetchActivityFacilityBySearchEvent value)?
        fetchActivityFacilityBySearch,
    TResult Function(ActivityFacilityCheckIfInCache value)? checkIfInCache,
    required TResult orElse(),
  }) {
    if (checkIfInCache != null) {
      return checkIfInCache(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilityCheckIfInCache implements ActivityFacilityEvent {
  const factory ActivityFacilityCheckIfInCache(
      {required final String activityFacilityId,
      required final String userType}) = _$ActivityFacilityCheckIfInCacheImpl;

  String get activityFacilityId;
  String get userType;
  @JsonKey(ignore: true)
  _$$ActivityFacilityCheckIfInCacheImplCopyWith<
          _$ActivityFacilityCheckIfInCacheImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ActivityFacilityState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ActivityFacilityStateCopyWith<$Res> {
  factory $ActivityFacilityStateCopyWith(ActivityFacilityState value,
          $Res Function(ActivityFacilityState) then) =
      _$ActivityFacilityStateCopyWithImpl<$Res, ActivityFacilityState>;
}

/// @nodoc
class _$ActivityFacilityStateCopyWithImpl<$Res,
        $Val extends ActivityFacilityState>
    implements $ActivityFacilityStateCopyWith<$Res> {
  _$ActivityFacilityStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$ActivityFacilityInitialStateImplCopyWith<$Res> {
  factory _$$ActivityFacilityInitialStateImplCopyWith(
          _$ActivityFacilityInitialStateImpl value,
          $Res Function(_$ActivityFacilityInitialStateImpl) then) =
      __$$ActivityFacilityInitialStateImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ActivityFacilityInitialStateImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ActivityFacilityInitialStateImpl>
    implements _$$ActivityFacilityInitialStateImplCopyWith<$Res> {
  __$$ActivityFacilityInitialStateImplCopyWithImpl(
      _$ActivityFacilityInitialStateImpl _value,
      $Res Function(_$ActivityFacilityInitialStateImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ActivityFacilityInitialStateImpl
    implements _ActivityFacilityInitialState {
  const _$ActivityFacilityInitialStateImpl();

  @override
  String toString() {
    return 'ActivityFacilityState.initial()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilityInitialStateImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _ActivityFacilityInitialState implements ActivityFacilityState {
  const factory _ActivityFacilityInitialState() =
      _$ActivityFacilityInitialStateImpl;
}

/// @nodoc
abstract class _$$ActivityFacilityLoadingStateImplCopyWith<$Res> {
  factory _$$ActivityFacilityLoadingStateImplCopyWith(
          _$ActivityFacilityLoadingStateImpl value,
          $Res Function(_$ActivityFacilityLoadingStateImpl) then) =
      __$$ActivityFacilityLoadingStateImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ActivityFacilityLoadingStateImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ActivityFacilityLoadingStateImpl>
    implements _$$ActivityFacilityLoadingStateImplCopyWith<$Res> {
  __$$ActivityFacilityLoadingStateImplCopyWithImpl(
      _$ActivityFacilityLoadingStateImpl _value,
      $Res Function(_$ActivityFacilityLoadingStateImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ActivityFacilityLoadingStateImpl
    implements _ActivityFacilityLoadingState {
  const _$ActivityFacilityLoadingStateImpl();

  @override
  String toString() {
    return 'ActivityFacilityState.loading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilityLoadingStateImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return loading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return loading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _ActivityFacilityLoadingState implements ActivityFacilityState {
  const factory _ActivityFacilityLoadingState() =
      _$ActivityFacilityLoadingStateImpl;
}

/// @nodoc
abstract class _$$ActivityFacilityInCacheImplCopyWith<$Res> {
  factory _$$ActivityFacilityInCacheImplCopyWith(
          _$ActivityFacilityInCacheImpl value,
          $Res Function(_$ActivityFacilityInCacheImpl) then) =
      __$$ActivityFacilityInCacheImplCopyWithImpl<$Res>;
  @useResult
  $Res call({bool isInCache});
}

/// @nodoc
class __$$ActivityFacilityInCacheImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ActivityFacilityInCacheImpl>
    implements _$$ActivityFacilityInCacheImplCopyWith<$Res> {
  __$$ActivityFacilityInCacheImplCopyWithImpl(
      _$ActivityFacilityInCacheImpl _value,
      $Res Function(_$ActivityFacilityInCacheImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? isInCache = null,
  }) {
    return _then(_$ActivityFacilityInCacheImpl(
      null == isInCache
          ? _value.isInCache
          : isInCache // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$ActivityFacilityInCacheImpl implements ActivityFacilityInCache {
  const _$ActivityFacilityInCacheImpl(this.isInCache);

  @override
  final bool isInCache;

  @override
  String toString() {
    return 'ActivityFacilityState.inCache(isInCache: $isInCache)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilityInCacheImpl &&
            (identical(other.isInCache, isInCache) ||
                other.isInCache == isInCache));
  }

  @override
  int get hashCode => Object.hash(runtimeType, isInCache);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilityInCacheImplCopyWith<_$ActivityFacilityInCacheImpl>
      get copyWith => __$$ActivityFacilityInCacheImplCopyWithImpl<
          _$ActivityFacilityInCacheImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return inCache(isInCache);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return inCache?.call(isInCache);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (inCache != null) {
      return inCache(isInCache);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return inCache(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return inCache?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (inCache != null) {
      return inCache(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilityInCache implements ActivityFacilityState {
  const factory ActivityFacilityInCache(final bool isInCache) =
      _$ActivityFacilityInCacheImpl;

  bool get isInCache;
  @JsonKey(ignore: true)
  _$$ActivityFacilityInCacheImplCopyWith<_$ActivityFacilityInCacheImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ActivityFacilityFetchedStateImplCopyWith<$Res> {
  factory _$$ActivityFacilityFetchedStateImplCopyWith(
          _$ActivityFacilityFetchedStateImpl value,
          $Res Function(_$ActivityFacilityFetchedStateImpl) then) =
      __$$ActivityFacilityFetchedStateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<ActivityFacilityWorkflow> activityFacilityList});
}

/// @nodoc
class __$$ActivityFacilityFetchedStateImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ActivityFacilityFetchedStateImpl>
    implements _$$ActivityFacilityFetchedStateImplCopyWith<$Res> {
  __$$ActivityFacilityFetchedStateImplCopyWithImpl(
      _$ActivityFacilityFetchedStateImpl _value,
      $Res Function(_$ActivityFacilityFetchedStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityList = null,
  }) {
    return _then(_$ActivityFacilityFetchedStateImpl(
      null == activityFacilityList
          ? _value._activityFacilityList
          : activityFacilityList // ignore: cast_nullable_to_non_nullable
              as List<ActivityFacilityWorkflow>,
    ));
  }
}

/// @nodoc

class _$ActivityFacilityFetchedStateImpl
    implements ActivityFacilityFetchedState {
  const _$ActivityFacilityFetchedStateImpl(
      final List<ActivityFacilityWorkflow> activityFacilityList)
      : _activityFacilityList = activityFacilityList;

  final List<ActivityFacilityWorkflow> _activityFacilityList;
  @override
  List<ActivityFacilityWorkflow> get activityFacilityList {
    if (_activityFacilityList is EqualUnmodifiableListView)
      return _activityFacilityList;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_activityFacilityList);
  }

  @override
  String toString() {
    return 'ActivityFacilityState.fetched(activityFacilityList: $activityFacilityList)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilityFetchedStateImpl &&
            const DeepCollectionEquality()
                .equals(other._activityFacilityList, _activityFacilityList));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, const DeepCollectionEquality().hash(_activityFacilityList));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilityFetchedStateImplCopyWith<
          _$ActivityFacilityFetchedStateImpl>
      get copyWith => __$$ActivityFacilityFetchedStateImplCopyWithImpl<
          _$ActivityFacilityFetchedStateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return fetched(activityFacilityList);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return fetched?.call(activityFacilityList);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (fetched != null) {
      return fetched(activityFacilityList);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return fetched(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return fetched?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (fetched != null) {
      return fetched(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilityFetchedState implements ActivityFacilityState {
  const factory ActivityFacilityFetchedState(
          final List<ActivityFacilityWorkflow> activityFacilityList) =
      _$ActivityFacilityFetchedStateImpl;

  List<ActivityFacilityWorkflow> get activityFacilityList;
  @JsonKey(ignore: true)
  _$$ActivityFacilityFetchedStateImplCopyWith<
          _$ActivityFacilityFetchedStateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ActivityFacilitySelectedStateImplCopyWith<$Res> {
  factory _$$ActivityFacilitySelectedStateImplCopyWith(
          _$ActivityFacilitySelectedStateImpl value,
          $Res Function(_$ActivityFacilitySelectedStateImpl) then) =
      __$$ActivityFacilitySelectedStateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId});
}

/// @nodoc
class __$$ActivityFacilitySelectedStateImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ActivityFacilitySelectedStateImpl>
    implements _$$ActivityFacilitySelectedStateImplCopyWith<$Res> {
  __$$ActivityFacilitySelectedStateImplCopyWithImpl(
      _$ActivityFacilitySelectedStateImpl _value,
      $Res Function(_$ActivityFacilitySelectedStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
  }) {
    return _then(_$ActivityFacilitySelectedStateImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ActivityFacilitySelectedStateImpl
    implements ActivityFacilitySelectedState {
  const _$ActivityFacilitySelectedStateImpl(this.activityFacilityId);

  @override
  final String activityFacilityId;

  @override
  String toString() {
    return 'ActivityFacilityState.selected(activityFacilityId: $activityFacilityId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilitySelectedStateImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilitySelectedStateImplCopyWith<
          _$ActivityFacilitySelectedStateImpl>
      get copyWith => __$$ActivityFacilitySelectedStateImplCopyWithImpl<
          _$ActivityFacilitySelectedStateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return selected(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return selected?.call(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(activityFacilityId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return selected(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return selected?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilitySelectedState implements ActivityFacilityState {
  const factory ActivityFacilitySelectedState(final String activityFacilityId) =
      _$ActivityFacilitySelectedStateImpl;

  String get activityFacilityId;
  @JsonKey(ignore: true)
  _$$ActivityFacilitySelectedStateImplCopyWith<
          _$ActivityFacilitySelectedStateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$UnSubmittedLoadedImplCopyWith<$Res> {
  factory _$$UnSubmittedLoadedImplCopyWith(_$UnSubmittedLoadedImpl value,
          $Res Function(_$UnSubmittedLoadedImpl) then) =
      __$$UnSubmittedLoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<ActivityFacilityWorkflow> unSubmitted});
}

/// @nodoc
class __$$UnSubmittedLoadedImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res, _$UnSubmittedLoadedImpl>
    implements _$$UnSubmittedLoadedImplCopyWith<$Res> {
  __$$UnSubmittedLoadedImplCopyWithImpl(_$UnSubmittedLoadedImpl _value,
      $Res Function(_$UnSubmittedLoadedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? unSubmitted = null,
  }) {
    return _then(_$UnSubmittedLoadedImpl(
      null == unSubmitted
          ? _value._unSubmitted
          : unSubmitted // ignore: cast_nullable_to_non_nullable
              as List<ActivityFacilityWorkflow>,
    ));
  }
}

/// @nodoc

class _$UnSubmittedLoadedImpl implements _UnSubmittedLoaded {
  const _$UnSubmittedLoadedImpl(
      final List<ActivityFacilityWorkflow> unSubmitted)
      : _unSubmitted = unSubmitted;

  final List<ActivityFacilityWorkflow> _unSubmitted;
  @override
  List<ActivityFacilityWorkflow> get unSubmitted {
    if (_unSubmitted is EqualUnmodifiableListView) return _unSubmitted;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_unSubmitted);
  }

  @override
  String toString() {
    return 'ActivityFacilityState.unSubmittedLoaded(unSubmitted: $unSubmitted)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UnSubmittedLoadedImpl &&
            const DeepCollectionEquality()
                .equals(other._unSubmitted, _unSubmitted));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, const DeepCollectionEquality().hash(_unSubmitted));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$UnSubmittedLoadedImplCopyWith<_$UnSubmittedLoadedImpl> get copyWith =>
      __$$UnSubmittedLoadedImplCopyWithImpl<_$UnSubmittedLoadedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return unSubmittedLoaded(unSubmitted);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return unSubmittedLoaded?.call(unSubmitted);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedLoaded != null) {
      return unSubmittedLoaded(unSubmitted);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return unSubmittedLoaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return unSubmittedLoaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedLoaded != null) {
      return unSubmittedLoaded(this);
    }
    return orElse();
  }
}

abstract class _UnSubmittedLoaded implements ActivityFacilityState {
  const factory _UnSubmittedLoaded(
          final List<ActivityFacilityWorkflow> unSubmitted) =
      _$UnSubmittedLoadedImpl;

  List<ActivityFacilityWorkflow> get unSubmitted;
  @JsonKey(ignore: true)
  _$$UnSubmittedLoadedImplCopyWith<_$UnSubmittedLoadedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$UnSubmittedAddedImplCopyWith<$Res> {
  factory _$$UnSubmittedAddedImplCopyWith(_$UnSubmittedAddedImpl value,
          $Res Function(_$UnSubmittedAddedImpl) then) =
      __$$UnSubmittedAddedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheUnsubmittedActivityFacility entry});
}

/// @nodoc
class __$$UnSubmittedAddedImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res, _$UnSubmittedAddedImpl>
    implements _$$UnSubmittedAddedImplCopyWith<$Res> {
  __$$UnSubmittedAddedImplCopyWithImpl(_$UnSubmittedAddedImpl _value,
      $Res Function(_$UnSubmittedAddedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$UnSubmittedAddedImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheUnsubmittedActivityFacility,
    ));
  }
}

/// @nodoc

class _$UnSubmittedAddedImpl implements _UnSubmittedAdded {
  const _$UnSubmittedAddedImpl(this.entry);

  @override
  final CacheUnsubmittedActivityFacility entry;

  @override
  String toString() {
    return 'ActivityFacilityState.unSubmittedAdded(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UnSubmittedAddedImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$UnSubmittedAddedImplCopyWith<_$UnSubmittedAddedImpl> get copyWith =>
      __$$UnSubmittedAddedImplCopyWithImpl<_$UnSubmittedAddedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return unSubmittedAdded(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return unSubmittedAdded?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedAdded != null) {
      return unSubmittedAdded(entry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return unSubmittedAdded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return unSubmittedAdded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedAdded != null) {
      return unSubmittedAdded(this);
    }
    return orElse();
  }
}

abstract class _UnSubmittedAdded implements ActivityFacilityState {
  const factory _UnSubmittedAdded(
      final CacheUnsubmittedActivityFacility entry) = _$UnSubmittedAddedImpl;

  CacheUnsubmittedActivityFacility get entry;
  @JsonKey(ignore: true)
  _$$UnSubmittedAddedImplCopyWith<_$UnSubmittedAddedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$UnSubmittedDeletedImplCopyWith<$Res> {
  factory _$$UnSubmittedDeletedImplCopyWith(_$UnSubmittedDeletedImpl value,
          $Res Function(_$UnSubmittedDeletedImpl) then) =
      __$$UnSubmittedDeletedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$UnSubmittedDeletedImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res, _$UnSubmittedDeletedImpl>
    implements _$$UnSubmittedDeletedImplCopyWith<$Res> {
  __$$UnSubmittedDeletedImplCopyWithImpl(_$UnSubmittedDeletedImpl _value,
      $Res Function(_$UnSubmittedDeletedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$UnSubmittedDeletedImpl implements _UnSubmittedDeleted {
  const _$UnSubmittedDeletedImpl();

  @override
  String toString() {
    return 'ActivityFacilityState.unSubmittedDeleted()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$UnSubmittedDeletedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return unSubmittedDeleted();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return unSubmittedDeleted?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedDeleted != null) {
      return unSubmittedDeleted();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return unSubmittedDeleted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return unSubmittedDeleted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedDeleted != null) {
      return unSubmittedDeleted(this);
    }
    return orElse();
  }
}

abstract class _UnSubmittedDeleted implements ActivityFacilityState {
  const factory _UnSubmittedDeleted() = _$UnSubmittedDeletedImpl;
}

/// @nodoc
abstract class _$$ReportCountsLoadedImplCopyWith<$Res> {
  factory _$$ReportCountsLoadedImplCopyWith(_$ReportCountsLoadedImpl value,
          $Res Function(_$ReportCountsLoadedImpl) then) =
      __$$ReportCountsLoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int newReportCount, int inboxCount, int submittedCount});
}

/// @nodoc
class __$$ReportCountsLoadedImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res, _$ReportCountsLoadedImpl>
    implements _$$ReportCountsLoadedImplCopyWith<$Res> {
  __$$ReportCountsLoadedImplCopyWithImpl(_$ReportCountsLoadedImpl _value,
      $Res Function(_$ReportCountsLoadedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? newReportCount = null,
    Object? inboxCount = null,
    Object? submittedCount = null,
  }) {
    return _then(_$ReportCountsLoadedImpl(
      newReportCount: null == newReportCount
          ? _value.newReportCount
          : newReportCount // ignore: cast_nullable_to_non_nullable
              as int,
      inboxCount: null == inboxCount
          ? _value.inboxCount
          : inboxCount // ignore: cast_nullable_to_non_nullable
              as int,
      submittedCount: null == submittedCount
          ? _value.submittedCount
          : submittedCount // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$ReportCountsLoadedImpl implements ReportCountsLoaded {
  const _$ReportCountsLoadedImpl(
      {required this.newReportCount,
      required this.inboxCount,
      required this.submittedCount});

  @override
  final int newReportCount;
  @override
  final int inboxCount;
  @override
  final int submittedCount;

  @override
  String toString() {
    return 'ActivityFacilityState.reportCountsLoaded(newReportCount: $newReportCount, inboxCount: $inboxCount, submittedCount: $submittedCount)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ReportCountsLoadedImpl &&
            (identical(other.newReportCount, newReportCount) ||
                other.newReportCount == newReportCount) &&
            (identical(other.inboxCount, inboxCount) ||
                other.inboxCount == inboxCount) &&
            (identical(other.submittedCount, submittedCount) ||
                other.submittedCount == submittedCount));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, newReportCount, inboxCount, submittedCount);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ReportCountsLoadedImplCopyWith<_$ReportCountsLoadedImpl> get copyWith =>
      __$$ReportCountsLoadedImplCopyWithImpl<_$ReportCountsLoadedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return reportCountsLoaded(newReportCount, inboxCount, submittedCount);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return reportCountsLoaded?.call(newReportCount, inboxCount, submittedCount);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (reportCountsLoaded != null) {
      return reportCountsLoaded(newReportCount, inboxCount, submittedCount);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return reportCountsLoaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return reportCountsLoaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (reportCountsLoaded != null) {
      return reportCountsLoaded(this);
    }
    return orElse();
  }
}

abstract class ReportCountsLoaded implements ActivityFacilityState {
  const factory ReportCountsLoaded(
      {required final int newReportCount,
      required final int inboxCount,
      required final int submittedCount}) = _$ReportCountsLoadedImpl;

  int get newReportCount;
  int get inboxCount;
  int get submittedCount;
  @JsonKey(ignore: true)
  _$$ReportCountsLoadedImplCopyWith<_$ReportCountsLoadedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$NewlyAssignedLoadedImplCopyWith<$Res> {
  factory _$$NewlyAssignedLoadedImplCopyWith(_$NewlyAssignedLoadedImpl value,
          $Res Function(_$NewlyAssignedLoadedImpl) then) =
      __$$NewlyAssignedLoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int count});
}

/// @nodoc
class __$$NewlyAssignedLoadedImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res, _$NewlyAssignedLoadedImpl>
    implements _$$NewlyAssignedLoadedImplCopyWith<$Res> {
  __$$NewlyAssignedLoadedImplCopyWithImpl(_$NewlyAssignedLoadedImpl _value,
      $Res Function(_$NewlyAssignedLoadedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? count = null,
  }) {
    return _then(_$NewlyAssignedLoadedImpl(
      null == count
          ? _value.count
          : count // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$NewlyAssignedLoadedImpl implements NewlyAssignedLoaded {
  const _$NewlyAssignedLoadedImpl(this.count);

  @override
  final int count;

  @override
  String toString() {
    return 'ActivityFacilityState.newlyAssignedLoaded(count: $count)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$NewlyAssignedLoadedImpl &&
            (identical(other.count, count) || other.count == count));
  }

  @override
  int get hashCode => Object.hash(runtimeType, count);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$NewlyAssignedLoadedImplCopyWith<_$NewlyAssignedLoadedImpl> get copyWith =>
      __$$NewlyAssignedLoadedImplCopyWithImpl<_$NewlyAssignedLoadedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return newlyAssignedLoaded(count);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return newlyAssignedLoaded?.call(count);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (newlyAssignedLoaded != null) {
      return newlyAssignedLoaded(count);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return newlyAssignedLoaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return newlyAssignedLoaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (newlyAssignedLoaded != null) {
      return newlyAssignedLoaded(this);
    }
    return orElse();
  }
}

abstract class NewlyAssignedLoaded implements ActivityFacilityState {
  const factory NewlyAssignedLoaded(final int count) =
      _$NewlyAssignedLoadedImpl;

  int get count;
  @JsonKey(ignore: true)
  _$$NewlyAssignedLoadedImplCopyWith<_$NewlyAssignedLoadedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ActivityFacilitySortedStateImplCopyWith<$Res> {
  factory _$$ActivityFacilitySortedStateImplCopyWith(
          _$ActivityFacilitySortedStateImpl value,
          $Res Function(_$ActivityFacilitySortedStateImpl) then) =
      __$$ActivityFacilitySortedStateImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {List<ActivityFacilityWorkflow> activityFacilityList,
      String sortDirection});
}

/// @nodoc
class __$$ActivityFacilitySortedStateImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ActivityFacilitySortedStateImpl>
    implements _$$ActivityFacilitySortedStateImplCopyWith<$Res> {
  __$$ActivityFacilitySortedStateImplCopyWithImpl(
      _$ActivityFacilitySortedStateImpl _value,
      $Res Function(_$ActivityFacilitySortedStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityList = null,
    Object? sortDirection = null,
  }) {
    return _then(_$ActivityFacilitySortedStateImpl(
      activityFacilityList: null == activityFacilityList
          ? _value._activityFacilityList
          : activityFacilityList // ignore: cast_nullable_to_non_nullable
              as List<ActivityFacilityWorkflow>,
      sortDirection: null == sortDirection
          ? _value.sortDirection
          : sortDirection // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ActivityFacilitySortedStateImpl implements ActivityFacilitySortedState {
  const _$ActivityFacilitySortedStateImpl(
      {required final List<ActivityFacilityWorkflow> activityFacilityList,
      required this.sortDirection})
      : _activityFacilityList = activityFacilityList;

  final List<ActivityFacilityWorkflow> _activityFacilityList;
  @override
  List<ActivityFacilityWorkflow> get activityFacilityList {
    if (_activityFacilityList is EqualUnmodifiableListView)
      return _activityFacilityList;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_activityFacilityList);
  }

  @override
  final String sortDirection;

  @override
  String toString() {
    return 'ActivityFacilityState.sorted(activityFacilityList: $activityFacilityList, sortDirection: $sortDirection)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilitySortedStateImpl &&
            const DeepCollectionEquality()
                .equals(other._activityFacilityList, _activityFacilityList) &&
            (identical(other.sortDirection, sortDirection) ||
                other.sortDirection == sortDirection));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(_activityFacilityList),
      sortDirection);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilitySortedStateImplCopyWith<_$ActivityFacilitySortedStateImpl>
      get copyWith => __$$ActivityFacilitySortedStateImplCopyWithImpl<
          _$ActivityFacilitySortedStateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return sorted(activityFacilityList, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return sorted?.call(activityFacilityList, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (sorted != null) {
      return sorted(activityFacilityList, sortDirection);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return sorted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return sorted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (sorted != null) {
      return sorted(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilitySortedState implements ActivityFacilityState {
  const factory ActivityFacilitySortedState(
      {required final List<ActivityFacilityWorkflow> activityFacilityList,
      required final String sortDirection}) = _$ActivityFacilitySortedStateImpl;

  List<ActivityFacilityWorkflow> get activityFacilityList;
  String get sortDirection;
  @JsonKey(ignore: true)
  _$$ActivityFacilitySortedStateImplCopyWith<_$ActivityFacilitySortedStateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ActivityFacilitySearchLoadingImplCopyWith<$Res> {
  factory _$$ActivityFacilitySearchLoadingImplCopyWith(
          _$ActivityFacilitySearchLoadingImpl value,
          $Res Function(_$ActivityFacilitySearchLoadingImpl) then) =
      __$$ActivityFacilitySearchLoadingImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ActivityFacilitySearchLoadingImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ActivityFacilitySearchLoadingImpl>
    implements _$$ActivityFacilitySearchLoadingImplCopyWith<$Res> {
  __$$ActivityFacilitySearchLoadingImplCopyWithImpl(
      _$ActivityFacilitySearchLoadingImpl _value,
      $Res Function(_$ActivityFacilitySearchLoadingImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ActivityFacilitySearchLoadingImpl
    implements ActivityFacilitySearchLoading {
  const _$ActivityFacilitySearchLoadingImpl();

  @override
  String toString() {
    return 'ActivityFacilityState.searchLoading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilitySearchLoadingImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return searchLoading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return searchLoading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (searchLoading != null) {
      return searchLoading();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return searchLoading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return searchLoading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (searchLoading != null) {
      return searchLoading(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilitySearchLoading implements ActivityFacilityState {
  const factory ActivityFacilitySearchLoading() =
      _$ActivityFacilitySearchLoadingImpl;
}

/// @nodoc
abstract class _$$ProjectSearchResultsImplCopyWith<$Res> {
  factory _$$ProjectSearchResultsImplCopyWith(_$ProjectSearchResultsImpl value,
          $Res Function(_$ProjectSearchResultsImpl) then) =
      __$$ProjectSearchResultsImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<ActivityFacilityWorkflow> results});
}

/// @nodoc
class __$$ProjectSearchResultsImplCopyWithImpl<$Res>
    extends _$ActivityFacilityStateCopyWithImpl<$Res,
        _$ProjectSearchResultsImpl>
    implements _$$ProjectSearchResultsImplCopyWith<$Res> {
  __$$ProjectSearchResultsImplCopyWithImpl(_$ProjectSearchResultsImpl _value,
      $Res Function(_$ProjectSearchResultsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? results = null,
  }) {
    return _then(_$ProjectSearchResultsImpl(
      null == results
          ? _value._results
          : results // ignore: cast_nullable_to_non_nullable
              as List<ActivityFacilityWorkflow>,
    ));
  }
}

/// @nodoc

class _$ProjectSearchResultsImpl implements ProjectSearchResults {
  const _$ProjectSearchResultsImpl(final List<ActivityFacilityWorkflow> results)
      : _results = results;

  final List<ActivityFacilityWorkflow> _results;
  @override
  List<ActivityFacilityWorkflow> get results {
    if (_results is EqualUnmodifiableListView) return _results;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_results);
  }

  @override
  String toString() {
    return 'ActivityFacilityState.searchResults(results: $results)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectSearchResultsImpl &&
            const DeepCollectionEquality().equals(other._results, _results));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, const DeepCollectionEquality().hash(_results));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProjectSearchResultsImplCopyWith<_$ProjectSearchResultsImpl>
      get copyWith =>
          __$$ProjectSearchResultsImplCopyWithImpl<_$ProjectSearchResultsImpl>(
              this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool isInCache) inCache,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList)
        fetched,
    required TResult Function(String activityFacilityId) selected,
    required TResult Function(List<ActivityFacilityWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedActivityFacility entry)
        unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ActivityFacilityWorkflow> results)
        searchResults,
  }) {
    return searchResults(results);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool isInCache)? inCache,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult? Function(String activityFacilityId)? selected,
    TResult? Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ActivityFacilityWorkflow> results)? searchResults,
  }) {
    return searchResults?.call(results);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool isInCache)? inCache,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList)?
        fetched,
    TResult Function(String activityFacilityId)? selected,
    TResult Function(List<ActivityFacilityWorkflow> unSubmitted)?
        unSubmittedLoaded,
    TResult Function(CacheUnsubmittedActivityFacility entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ActivityFacilityWorkflow> activityFacilityList,
            String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ActivityFacilityWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (searchResults != null) {
      return searchResults(results);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ActivityFacilityInitialState value) initial,
    required TResult Function(_ActivityFacilityLoadingState value) loading,
    required TResult Function(ActivityFacilityInCache value) inCache,
    required TResult Function(ActivityFacilityFetchedState value) fetched,
    required TResult Function(ActivityFacilitySelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ActivityFacilitySortedState value) sorted,
    required TResult Function(ActivityFacilitySearchLoading value)
        searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return searchResults(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ActivityFacilityInitialState value)? initial,
    TResult? Function(_ActivityFacilityLoadingState value)? loading,
    TResult? Function(ActivityFacilityInCache value)? inCache,
    TResult? Function(ActivityFacilityFetchedState value)? fetched,
    TResult? Function(ActivityFacilitySelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ActivityFacilitySortedState value)? sorted,
    TResult? Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return searchResults?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ActivityFacilityInitialState value)? initial,
    TResult Function(_ActivityFacilityLoadingState value)? loading,
    TResult Function(ActivityFacilityInCache value)? inCache,
    TResult Function(ActivityFacilityFetchedState value)? fetched,
    TResult Function(ActivityFacilitySelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ActivityFacilitySortedState value)? sorted,
    TResult Function(ActivityFacilitySearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (searchResults != null) {
      return searchResults(this);
    }
    return orElse();
  }
}

abstract class ProjectSearchResults implements ActivityFacilityState {
  const factory ProjectSearchResults(
          final List<ActivityFacilityWorkflow> results) =
      _$ProjectSearchResultsImpl;

  List<ActivityFacilityWorkflow> get results;
  @JsonKey(ignore: true)
  _$$ProjectSearchResultsImplCopyWith<_$ProjectSearchResultsImpl>
      get copyWith => throw _privateConstructorUsedError;
}
