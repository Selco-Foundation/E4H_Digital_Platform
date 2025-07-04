// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'project.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$ProjectEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProjectEventCopyWith<$Res> {
  factory $ProjectEventCopyWith(
          ProjectEvent value, $Res Function(ProjectEvent) then) =
      _$ProjectEventCopyWithImpl<$Res, ProjectEvent>;
}

/// @nodoc
class _$ProjectEventCopyWithImpl<$Res, $Val extends ProjectEvent>
    implements $ProjectEventCopyWith<$Res> {
  _$ProjectEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$ProjectSelectEventImplCopyWith<$Res> {
  factory _$$ProjectSelectEventImplCopyWith(_$ProjectSelectEventImpl value,
          $Res Function(_$ProjectSelectEventImpl) then) =
      __$$ProjectSelectEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId});
}

/// @nodoc
class __$$ProjectSelectEventImplCopyWithImpl<$Res>
    extends _$ProjectEventCopyWithImpl<$Res, _$ProjectSelectEventImpl>
    implements _$$ProjectSelectEventImplCopyWith<$Res> {
  __$$ProjectSelectEventImplCopyWithImpl(_$ProjectSelectEventImpl _value,
      $Res Function(_$ProjectSelectEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
  }) {
    return _then(_$ProjectSelectEventImpl(
      null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ProjectSelectEventImpl implements ProjectSelectEvent {
  const _$ProjectSelectEventImpl(this.projectId);

  @override
  final String projectId;

  @override
  String toString() {
    return 'ProjectEvent.selectProject(projectId: $projectId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectSelectEventImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProjectSelectEventImplCopyWith<_$ProjectSelectEventImpl> get copyWith =>
      __$$ProjectSelectEventImplCopyWithImpl<_$ProjectSelectEventImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return selectProject(projectId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return selectProject?.call(projectId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (selectProject != null) {
      return selectProject(projectId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return selectProject(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return selectProject?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (selectProject != null) {
      return selectProject(this);
    }
    return orElse();
  }
}

abstract class ProjectSelectEvent implements ProjectEvent {
  const factory ProjectSelectEvent(final String projectId) =
      _$ProjectSelectEventImpl;

  String get projectId;
  @JsonKey(ignore: true)
  _$$ProjectSelectEventImplCopyWith<_$ProjectSelectEventImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FetchProjectsByWorkflowEventImplCopyWith<$Res> {
  factory _$$FetchProjectsByWorkflowEventImplCopyWith(
          _$FetchProjectsByWorkflowEventImpl value,
          $Res Function(_$FetchProjectsByWorkflowEventImpl) then) =
      __$$FetchProjectsByWorkflowEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<String> workflowStatuses});
}

/// @nodoc
class __$$FetchProjectsByWorkflowEventImplCopyWithImpl<$Res>
    extends _$ProjectEventCopyWithImpl<$Res, _$FetchProjectsByWorkflowEventImpl>
    implements _$$FetchProjectsByWorkflowEventImplCopyWith<$Res> {
  __$$FetchProjectsByWorkflowEventImplCopyWithImpl(
      _$FetchProjectsByWorkflowEventImpl _value,
      $Res Function(_$FetchProjectsByWorkflowEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? workflowStatuses = null,
  }) {
    return _then(_$FetchProjectsByWorkflowEventImpl(
      workflowStatuses: null == workflowStatuses
          ? _value._workflowStatuses
          : workflowStatuses // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc

class _$FetchProjectsByWorkflowEventImpl
    implements FetchProjectsByWorkflowEvent {
  const _$FetchProjectsByWorkflowEventImpl(
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
    return 'ProjectEvent.fetchProjectsByWorkflow(workflowStatuses: $workflowStatuses)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FetchProjectsByWorkflowEventImpl &&
            const DeepCollectionEquality()
                .equals(other._workflowStatuses, _workflowStatuses));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, const DeepCollectionEquality().hash(_workflowStatuses));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FetchProjectsByWorkflowEventImplCopyWith<
          _$FetchProjectsByWorkflowEventImpl>
      get copyWith => __$$FetchProjectsByWorkflowEventImplCopyWithImpl<
          _$FetchProjectsByWorkflowEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return fetchProjectsByWorkflow(workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return fetchProjectsByWorkflow?.call(workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (fetchProjectsByWorkflow != null) {
      return fetchProjectsByWorkflow(workflowStatuses);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return fetchProjectsByWorkflow(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return fetchProjectsByWorkflow?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (fetchProjectsByWorkflow != null) {
      return fetchProjectsByWorkflow(this);
    }
    return orElse();
  }
}

abstract class FetchProjectsByWorkflowEvent implements ProjectEvent {
  const factory FetchProjectsByWorkflowEvent(
          {required final List<String> workflowStatuses}) =
      _$FetchProjectsByWorkflowEventImpl;

  List<String> get workflowStatuses;
  @JsonKey(ignore: true)
  _$$FetchProjectsByWorkflowEventImplCopyWith<
          _$FetchProjectsByWorkflowEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$AddUnSubmittedEventImplCopyWith<$Res> {
  factory _$$AddUnSubmittedEventImplCopyWith(_$AddUnSubmittedEventImpl value,
          $Res Function(_$AddUnSubmittedEventImpl) then) =
      __$$AddUnSubmittedEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ProjectWorkflow workflow, String userType});

  $ProjectWorkflowCopyWith<$Res> get workflow;
}

/// @nodoc
class __$$AddUnSubmittedEventImplCopyWithImpl<$Res>
    extends _$ProjectEventCopyWithImpl<$Res, _$AddUnSubmittedEventImpl>
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
              as ProjectWorkflow,
      null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ProjectWorkflowCopyWith<$Res> get workflow {
    return $ProjectWorkflowCopyWith<$Res>(_value.workflow, (value) {
      return _then(_value.copyWith(workflow: value));
    });
  }
}

/// @nodoc

class _$AddUnSubmittedEventImpl implements AddUnSubmittedEvent {
  const _$AddUnSubmittedEventImpl(this.workflow, this.userType);

  @override
  final ProjectWorkflow workflow;
  @override
  final String userType;

  @override
  String toString() {
    return 'ProjectEvent.addUnSubmitted(workflow: $workflow, userType: $userType)';
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
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return addUnSubmitted(workflow, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return addUnSubmitted?.call(workflow, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
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
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return addUnSubmitted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return addUnSubmitted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (addUnSubmitted != null) {
      return addUnSubmitted(this);
    }
    return orElse();
  }
}

abstract class AddUnSubmittedEvent implements ProjectEvent {
  const factory AddUnSubmittedEvent(
          final ProjectWorkflow workflow, final String userType) =
      _$AddUnSubmittedEventImpl;

  ProjectWorkflow get workflow;
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
    extends _$ProjectEventCopyWithImpl<$Res, _$LoadUnSubmittedEventImpl>
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
    return 'ProjectEvent.loadUnSubmitted(statuses: $statuses, userType: $userType)';
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
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return loadUnSubmitted(statuses, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return loadUnSubmitted?.call(statuses, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
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
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return loadUnSubmitted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return loadUnSubmitted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (loadUnSubmitted != null) {
      return loadUnSubmitted(this);
    }
    return orElse();
  }
}

abstract class LoadUnSubmittedEvent implements ProjectEvent {
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
  $Res call({String projectId, String userType});
}

/// @nodoc
class __$$DeleteUnSubmittedEventImplCopyWithImpl<$Res>
    extends _$ProjectEventCopyWithImpl<$Res, _$DeleteUnSubmittedEventImpl>
    implements _$$DeleteUnSubmittedEventImplCopyWith<$Res> {
  __$$DeleteUnSubmittedEventImplCopyWithImpl(
      _$DeleteUnSubmittedEventImpl _value,
      $Res Function(_$DeleteUnSubmittedEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? userType = null,
  }) {
    return _then(_$DeleteUnSubmittedEventImpl(
      null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
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
  const _$DeleteUnSubmittedEventImpl(this.projectId, this.userType);

  @override
  final String projectId;
  @override
  final String userType;

  @override
  String toString() {
    return 'ProjectEvent.deleteUnSubmitted(projectId: $projectId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DeleteUnSubmittedEventImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DeleteUnSubmittedEventImplCopyWith<_$DeleteUnSubmittedEventImpl>
      get copyWith => __$$DeleteUnSubmittedEventImplCopyWithImpl<
          _$DeleteUnSubmittedEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return deleteUnSubmitted(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return deleteUnSubmitted?.call(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (deleteUnSubmitted != null) {
      return deleteUnSubmitted(projectId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return deleteUnSubmitted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return deleteUnSubmitted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (deleteUnSubmitted != null) {
      return deleteUnSubmitted(this);
    }
    return orElse();
  }
}

abstract class DeleteUnSubmittedEvent implements ProjectEvent {
  const factory DeleteUnSubmittedEvent(
          final String projectId, final String userType) =
      _$DeleteUnSubmittedEventImpl;

  String get projectId;
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
    extends _$ProjectEventCopyWithImpl<$Res, _$FetchAllReportCountsEventImpl>
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
    return 'ProjectEvent.fetchAllReportCounts(userType: $userType)';
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
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return fetchAllReportCounts(userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return fetchAllReportCounts?.call(userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
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
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return fetchAllReportCounts(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return fetchAllReportCounts?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (fetchAllReportCounts != null) {
      return fetchAllReportCounts(this);
    }
    return orElse();
  }
}

abstract class FetchAllReportCountsEvent implements ProjectEvent {
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
    extends _$ProjectEventCopyWithImpl<$Res, _$GetNewlyAssignedEventImpl>
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
    return 'ProjectEvent.getNewlyAssigned(userType: $userType)';
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
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return getNewlyAssigned(userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return getNewlyAssigned?.call(userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
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
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return getNewlyAssigned(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return getNewlyAssigned?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (getNewlyAssigned != null) {
      return getNewlyAssigned(this);
    }
    return orElse();
  }
}

abstract class GetNewlyAssignedEvent implements ProjectEvent {
  const factory GetNewlyAssignedEvent({required final String userType}) =
      _$GetNewlyAssignedEventImpl;

  String get userType;
  @JsonKey(ignore: true)
  _$$GetNewlyAssignedEventImplCopyWith<_$GetNewlyAssignedEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FetchProjectsSortedEventImplCopyWith<$Res> {
  factory _$$FetchProjectsSortedEventImplCopyWith(
          _$FetchProjectsSortedEventImpl value,
          $Res Function(_$FetchProjectsSortedEventImpl) then) =
      __$$FetchProjectsSortedEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<String> workflowStatuses, String sortDirection});
}

/// @nodoc
class __$$FetchProjectsSortedEventImplCopyWithImpl<$Res>
    extends _$ProjectEventCopyWithImpl<$Res, _$FetchProjectsSortedEventImpl>
    implements _$$FetchProjectsSortedEventImplCopyWith<$Res> {
  __$$FetchProjectsSortedEventImplCopyWithImpl(
      _$FetchProjectsSortedEventImpl _value,
      $Res Function(_$FetchProjectsSortedEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? workflowStatuses = null,
    Object? sortDirection = null,
  }) {
    return _then(_$FetchProjectsSortedEventImpl(
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

class _$FetchProjectsSortedEventImpl implements FetchProjectsSortedEvent {
  const _$FetchProjectsSortedEventImpl(
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
    return 'ProjectEvent.fetchProjectsSorted(workflowStatuses: $workflowStatuses, sortDirection: $sortDirection)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FetchProjectsSortedEventImpl &&
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
  _$$FetchProjectsSortedEventImplCopyWith<_$FetchProjectsSortedEventImpl>
      get copyWith => __$$FetchProjectsSortedEventImplCopyWithImpl<
          _$FetchProjectsSortedEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return fetchProjectsSorted(workflowStatuses, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return fetchProjectsSorted?.call(workflowStatuses, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (fetchProjectsSorted != null) {
      return fetchProjectsSorted(workflowStatuses, sortDirection);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return fetchProjectsSorted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return fetchProjectsSorted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (fetchProjectsSorted != null) {
      return fetchProjectsSorted(this);
    }
    return orElse();
  }
}

abstract class FetchProjectsSortedEvent implements ProjectEvent {
  const factory FetchProjectsSortedEvent(
      {required final List<String> workflowStatuses,
      required final String sortDirection}) = _$FetchProjectsSortedEventImpl;

  List<String> get workflowStatuses;
  String get sortDirection;
  @JsonKey(ignore: true)
  _$$FetchProjectsSortedEventImplCopyWith<_$FetchProjectsSortedEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FetchProjectsBySearchEventImplCopyWith<$Res> {
  factory _$$FetchProjectsBySearchEventImplCopyWith(
          _$FetchProjectsBySearchEventImpl value,
          $Res Function(_$FetchProjectsBySearchEventImpl) then) =
      __$$FetchProjectsBySearchEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String query, List<String> workflowStatuses});
}

/// @nodoc
class __$$FetchProjectsBySearchEventImplCopyWithImpl<$Res>
    extends _$ProjectEventCopyWithImpl<$Res, _$FetchProjectsBySearchEventImpl>
    implements _$$FetchProjectsBySearchEventImplCopyWith<$Res> {
  __$$FetchProjectsBySearchEventImplCopyWithImpl(
      _$FetchProjectsBySearchEventImpl _value,
      $Res Function(_$FetchProjectsBySearchEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? query = null,
    Object? workflowStatuses = null,
  }) {
    return _then(_$FetchProjectsBySearchEventImpl(
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

class _$FetchProjectsBySearchEventImpl implements FetchProjectsBySearchEvent {
  const _$FetchProjectsBySearchEventImpl(
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
    return 'ProjectEvent.fetchProjectsBySearch(query: $query, workflowStatuses: $workflowStatuses)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FetchProjectsBySearchEventImpl &&
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
  _$$FetchProjectsBySearchEventImplCopyWith<_$FetchProjectsBySearchEventImpl>
      get copyWith => __$$FetchProjectsBySearchEventImplCopyWithImpl<
          _$FetchProjectsBySearchEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) selectProject,
    required TResult Function(List<String> workflowStatuses)
        fetchProjectsByWorkflow,
    required TResult Function(ProjectWorkflow workflow, String userType)
        addUnSubmitted,
    required TResult Function(List<String> statuses, String userType)
        loadUnSubmitted,
    required TResult Function(String projectId, String userType)
        deleteUnSubmitted,
    required TResult Function(String userType) fetchAllReportCounts,
    required TResult Function(String userType) getNewlyAssigned,
    required TResult Function(
            List<String> workflowStatuses, String sortDirection)
        fetchProjectsSorted,
    required TResult Function(String query, List<String> workflowStatuses)
        fetchProjectsBySearch,
  }) {
    return fetchProjectsBySearch(query, workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? selectProject,
    TResult? Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult? Function(ProjectWorkflow workflow, String userType)?
        addUnSubmitted,
    TResult? Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult? Function(String projectId, String userType)? deleteUnSubmitted,
    TResult? Function(String userType)? fetchAllReportCounts,
    TResult? Function(String userType)? getNewlyAssigned,
    TResult? Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult? Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
  }) {
    return fetchProjectsBySearch?.call(query, workflowStatuses);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? selectProject,
    TResult Function(List<String> workflowStatuses)? fetchProjectsByWorkflow,
    TResult Function(ProjectWorkflow workflow, String userType)? addUnSubmitted,
    TResult Function(List<String> statuses, String userType)? loadUnSubmitted,
    TResult Function(String projectId, String userType)? deleteUnSubmitted,
    TResult Function(String userType)? fetchAllReportCounts,
    TResult Function(String userType)? getNewlyAssigned,
    TResult Function(List<String> workflowStatuses, String sortDirection)?
        fetchProjectsSorted,
    TResult Function(String query, List<String> workflowStatuses)?
        fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (fetchProjectsBySearch != null) {
      return fetchProjectsBySearch(query, workflowStatuses);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelectEvent value) selectProject,
    required TResult Function(FetchProjectsByWorkflowEvent value)
        fetchProjectsByWorkflow,
    required TResult Function(AddUnSubmittedEvent value) addUnSubmitted,
    required TResult Function(LoadUnSubmittedEvent value) loadUnSubmitted,
    required TResult Function(DeleteUnSubmittedEvent value) deleteUnSubmitted,
    required TResult Function(FetchAllReportCountsEvent value)
        fetchAllReportCounts,
    required TResult Function(GetNewlyAssignedEvent value) getNewlyAssigned,
    required TResult Function(FetchProjectsSortedEvent value)
        fetchProjectsSorted,
    required TResult Function(FetchProjectsBySearchEvent value)
        fetchProjectsBySearch,
  }) {
    return fetchProjectsBySearch(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelectEvent value)? selectProject,
    TResult? Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult? Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult? Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult? Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult? Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult? Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult? Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult? Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
  }) {
    return fetchProjectsBySearch?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelectEvent value)? selectProject,
    TResult Function(FetchProjectsByWorkflowEvent value)?
        fetchProjectsByWorkflow,
    TResult Function(AddUnSubmittedEvent value)? addUnSubmitted,
    TResult Function(LoadUnSubmittedEvent value)? loadUnSubmitted,
    TResult Function(DeleteUnSubmittedEvent value)? deleteUnSubmitted,
    TResult Function(FetchAllReportCountsEvent value)? fetchAllReportCounts,
    TResult Function(GetNewlyAssignedEvent value)? getNewlyAssigned,
    TResult Function(FetchProjectsSortedEvent value)? fetchProjectsSorted,
    TResult Function(FetchProjectsBySearchEvent value)? fetchProjectsBySearch,
    required TResult orElse(),
  }) {
    if (fetchProjectsBySearch != null) {
      return fetchProjectsBySearch(this);
    }
    return orElse();
  }
}

abstract class FetchProjectsBySearchEvent implements ProjectEvent {
  const factory FetchProjectsBySearchEvent(
          {required final String query,
          required final List<String> workflowStatuses}) =
      _$FetchProjectsBySearchEventImpl;

  String get query;
  List<String> get workflowStatuses;
  @JsonKey(ignore: true)
  _$$FetchProjectsBySearchEventImplCopyWith<_$FetchProjectsBySearchEventImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ProjectState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProjectStateCopyWith<$Res> {
  factory $ProjectStateCopyWith(
          ProjectState value, $Res Function(ProjectState) then) =
      _$ProjectStateCopyWithImpl<$Res, ProjectState>;
}

/// @nodoc
class _$ProjectStateCopyWithImpl<$Res, $Val extends ProjectState>
    implements $ProjectStateCopyWith<$Res> {
  _$ProjectStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$ProjectInitialStateImplCopyWith<$Res> {
  factory _$$ProjectInitialStateImplCopyWith(_$ProjectInitialStateImpl value,
          $Res Function(_$ProjectInitialStateImpl) then) =
      __$$ProjectInitialStateImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ProjectInitialStateImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$ProjectInitialStateImpl>
    implements _$$ProjectInitialStateImplCopyWith<$Res> {
  __$$ProjectInitialStateImplCopyWithImpl(_$ProjectInitialStateImpl _value,
      $Res Function(_$ProjectInitialStateImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ProjectInitialStateImpl implements _ProjectInitialState {
  const _$ProjectInitialStateImpl();

  @override
  String toString() {
    return 'ProjectState.initial()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectInitialStateImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _ProjectInitialState implements ProjectState {
  const factory _ProjectInitialState() = _$ProjectInitialStateImpl;
}

/// @nodoc
abstract class _$$ProjectLoadingStateImplCopyWith<$Res> {
  factory _$$ProjectLoadingStateImplCopyWith(_$ProjectLoadingStateImpl value,
          $Res Function(_$ProjectLoadingStateImpl) then) =
      __$$ProjectLoadingStateImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ProjectLoadingStateImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$ProjectLoadingStateImpl>
    implements _$$ProjectLoadingStateImplCopyWith<$Res> {
  __$$ProjectLoadingStateImplCopyWithImpl(_$ProjectLoadingStateImpl _value,
      $Res Function(_$ProjectLoadingStateImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ProjectLoadingStateImpl implements _ProjectLoadingState {
  const _$ProjectLoadingStateImpl();

  @override
  String toString() {
    return 'ProjectState.loading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectLoadingStateImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return loading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return loading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _ProjectLoadingState implements ProjectState {
  const factory _ProjectLoadingState() = _$ProjectLoadingStateImpl;
}

/// @nodoc
abstract class _$$ProjectFetchedStateImplCopyWith<$Res> {
  factory _$$ProjectFetchedStateImplCopyWith(_$ProjectFetchedStateImpl value,
          $Res Function(_$ProjectFetchedStateImpl) then) =
      __$$ProjectFetchedStateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<ProjectWorkflow> projectsList});
}

/// @nodoc
class __$$ProjectFetchedStateImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$ProjectFetchedStateImpl>
    implements _$$ProjectFetchedStateImplCopyWith<$Res> {
  __$$ProjectFetchedStateImplCopyWithImpl(_$ProjectFetchedStateImpl _value,
      $Res Function(_$ProjectFetchedStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectsList = null,
  }) {
    return _then(_$ProjectFetchedStateImpl(
      null == projectsList
          ? _value._projectsList
          : projectsList // ignore: cast_nullable_to_non_nullable
              as List<ProjectWorkflow>,
    ));
  }
}

/// @nodoc

class _$ProjectFetchedStateImpl implements ProjectFetchedState {
  const _$ProjectFetchedStateImpl(final List<ProjectWorkflow> projectsList)
      : _projectsList = projectsList;

  final List<ProjectWorkflow> _projectsList;
  @override
  List<ProjectWorkflow> get projectsList {
    if (_projectsList is EqualUnmodifiableListView) return _projectsList;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_projectsList);
  }

  @override
  String toString() {
    return 'ProjectState.fetched(projectsList: $projectsList)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectFetchedStateImpl &&
            const DeepCollectionEquality()
                .equals(other._projectsList, _projectsList));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, const DeepCollectionEquality().hash(_projectsList));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProjectFetchedStateImplCopyWith<_$ProjectFetchedStateImpl> get copyWith =>
      __$$ProjectFetchedStateImplCopyWithImpl<_$ProjectFetchedStateImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return fetched(projectsList);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return fetched?.call(projectsList);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (fetched != null) {
      return fetched(projectsList);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return fetched(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return fetched?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (fetched != null) {
      return fetched(this);
    }
    return orElse();
  }
}

abstract class ProjectFetchedState implements ProjectState {
  const factory ProjectFetchedState(final List<ProjectWorkflow> projectsList) =
      _$ProjectFetchedStateImpl;

  List<ProjectWorkflow> get projectsList;
  @JsonKey(ignore: true)
  _$$ProjectFetchedStateImplCopyWith<_$ProjectFetchedStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ProjectSelectedStateImplCopyWith<$Res> {
  factory _$$ProjectSelectedStateImplCopyWith(_$ProjectSelectedStateImpl value,
          $Res Function(_$ProjectSelectedStateImpl) then) =
      __$$ProjectSelectedStateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId});
}

/// @nodoc
class __$$ProjectSelectedStateImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$ProjectSelectedStateImpl>
    implements _$$ProjectSelectedStateImplCopyWith<$Res> {
  __$$ProjectSelectedStateImplCopyWithImpl(_$ProjectSelectedStateImpl _value,
      $Res Function(_$ProjectSelectedStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
  }) {
    return _then(_$ProjectSelectedStateImpl(
      null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ProjectSelectedStateImpl implements ProjectSelectedState {
  const _$ProjectSelectedStateImpl(this.projectId);

  @override
  final String projectId;

  @override
  String toString() {
    return 'ProjectState.selected(projectId: $projectId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectSelectedStateImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProjectSelectedStateImplCopyWith<_$ProjectSelectedStateImpl>
      get copyWith =>
          __$$ProjectSelectedStateImplCopyWithImpl<_$ProjectSelectedStateImpl>(
              this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return selected(projectId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return selected?.call(projectId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(projectId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return selected(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return selected?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(this);
    }
    return orElse();
  }
}

abstract class ProjectSelectedState implements ProjectState {
  const factory ProjectSelectedState(final String projectId) =
      _$ProjectSelectedStateImpl;

  String get projectId;
  @JsonKey(ignore: true)
  _$$ProjectSelectedStateImplCopyWith<_$ProjectSelectedStateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$UnSubmittedLoadedImplCopyWith<$Res> {
  factory _$$UnSubmittedLoadedImplCopyWith(_$UnSubmittedLoadedImpl value,
          $Res Function(_$UnSubmittedLoadedImpl) then) =
      __$$UnSubmittedLoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<ProjectWorkflow> unSubmitted});
}

/// @nodoc
class __$$UnSubmittedLoadedImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$UnSubmittedLoadedImpl>
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
              as List<ProjectWorkflow>,
    ));
  }
}

/// @nodoc

class _$UnSubmittedLoadedImpl implements _UnSubmittedLoaded {
  const _$UnSubmittedLoadedImpl(final List<ProjectWorkflow> unSubmitted)
      : _unSubmitted = unSubmitted;

  final List<ProjectWorkflow> _unSubmitted;
  @override
  List<ProjectWorkflow> get unSubmitted {
    if (_unSubmitted is EqualUnmodifiableListView) return _unSubmitted;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_unSubmitted);
  }

  @override
  String toString() {
    return 'ProjectState.unSubmittedLoaded(unSubmitted: $unSubmitted)';
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
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return unSubmittedLoaded(unSubmitted);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return unSubmittedLoaded?.call(unSubmitted);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return unSubmittedLoaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return unSubmittedLoaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedLoaded != null) {
      return unSubmittedLoaded(this);
    }
    return orElse();
  }
}

abstract class _UnSubmittedLoaded implements ProjectState {
  const factory _UnSubmittedLoaded(final List<ProjectWorkflow> unSubmitted) =
      _$UnSubmittedLoadedImpl;

  List<ProjectWorkflow> get unSubmitted;
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
  $Res call({CacheUnsubmittedProject entry});
}

/// @nodoc
class __$$UnSubmittedAddedImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$UnSubmittedAddedImpl>
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
              as CacheUnsubmittedProject,
    ));
  }
}

/// @nodoc

class _$UnSubmittedAddedImpl implements _UnSubmittedAdded {
  const _$UnSubmittedAddedImpl(this.entry);

  @override
  final CacheUnsubmittedProject entry;

  @override
  String toString() {
    return 'ProjectState.unSubmittedAdded(entry: $entry)';
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
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return unSubmittedAdded(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return unSubmittedAdded?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return unSubmittedAdded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return unSubmittedAdded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedAdded != null) {
      return unSubmittedAdded(this);
    }
    return orElse();
  }
}

abstract class _UnSubmittedAdded implements ProjectState {
  const factory _UnSubmittedAdded(final CacheUnsubmittedProject entry) =
      _$UnSubmittedAddedImpl;

  CacheUnsubmittedProject get entry;
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
    extends _$ProjectStateCopyWithImpl<$Res, _$UnSubmittedDeletedImpl>
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
    return 'ProjectState.unSubmittedDeleted()';
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
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return unSubmittedDeleted();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return unSubmittedDeleted?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return unSubmittedDeleted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return unSubmittedDeleted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (unSubmittedDeleted != null) {
      return unSubmittedDeleted(this);
    }
    return orElse();
  }
}

abstract class _UnSubmittedDeleted implements ProjectState {
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
    extends _$ProjectStateCopyWithImpl<$Res, _$ReportCountsLoadedImpl>
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
    return 'ProjectState.reportCountsLoaded(newReportCount: $newReportCount, inboxCount: $inboxCount, submittedCount: $submittedCount)';
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
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return reportCountsLoaded(newReportCount, inboxCount, submittedCount);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return reportCountsLoaded?.call(newReportCount, inboxCount, submittedCount);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return reportCountsLoaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return reportCountsLoaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (reportCountsLoaded != null) {
      return reportCountsLoaded(this);
    }
    return orElse();
  }
}

abstract class ReportCountsLoaded implements ProjectState {
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
    extends _$ProjectStateCopyWithImpl<$Res, _$NewlyAssignedLoadedImpl>
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
    return 'ProjectState.newlyAssignedLoaded(count: $count)';
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
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return newlyAssignedLoaded(count);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return newlyAssignedLoaded?.call(count);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return newlyAssignedLoaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return newlyAssignedLoaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (newlyAssignedLoaded != null) {
      return newlyAssignedLoaded(this);
    }
    return orElse();
  }
}

abstract class NewlyAssignedLoaded implements ProjectState {
  const factory NewlyAssignedLoaded(final int count) =
      _$NewlyAssignedLoadedImpl;

  int get count;
  @JsonKey(ignore: true)
  _$$NewlyAssignedLoadedImplCopyWith<_$NewlyAssignedLoadedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ProjectSortedStateImplCopyWith<$Res> {
  factory _$$ProjectSortedStateImplCopyWith(_$ProjectSortedStateImpl value,
          $Res Function(_$ProjectSortedStateImpl) then) =
      __$$ProjectSortedStateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<ProjectWorkflow> projectsList, String sortDirection});
}

/// @nodoc
class __$$ProjectSortedStateImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$ProjectSortedStateImpl>
    implements _$$ProjectSortedStateImplCopyWith<$Res> {
  __$$ProjectSortedStateImplCopyWithImpl(_$ProjectSortedStateImpl _value,
      $Res Function(_$ProjectSortedStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectsList = null,
    Object? sortDirection = null,
  }) {
    return _then(_$ProjectSortedStateImpl(
      projectsList: null == projectsList
          ? _value._projectsList
          : projectsList // ignore: cast_nullable_to_non_nullable
              as List<ProjectWorkflow>,
      sortDirection: null == sortDirection
          ? _value.sortDirection
          : sortDirection // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ProjectSortedStateImpl implements ProjectSortedState {
  const _$ProjectSortedStateImpl(
      {required final List<ProjectWorkflow> projectsList,
      required this.sortDirection})
      : _projectsList = projectsList;

  final List<ProjectWorkflow> _projectsList;
  @override
  List<ProjectWorkflow> get projectsList {
    if (_projectsList is EqualUnmodifiableListView) return _projectsList;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_projectsList);
  }

  @override
  final String sortDirection;

  @override
  String toString() {
    return 'ProjectState.sorted(projectsList: $projectsList, sortDirection: $sortDirection)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectSortedStateImpl &&
            const DeepCollectionEquality()
                .equals(other._projectsList, _projectsList) &&
            (identical(other.sortDirection, sortDirection) ||
                other.sortDirection == sortDirection));
  }

  @override
  int get hashCode => Object.hash(runtimeType,
      const DeepCollectionEquality().hash(_projectsList), sortDirection);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProjectSortedStateImplCopyWith<_$ProjectSortedStateImpl> get copyWith =>
      __$$ProjectSortedStateImplCopyWithImpl<_$ProjectSortedStateImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return sorted(projectsList, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return sorted?.call(projectsList, sortDirection);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
    required TResult orElse(),
  }) {
    if (sorted != null) {
      return sorted(projectsList, sortDirection);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return sorted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return sorted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (sorted != null) {
      return sorted(this);
    }
    return orElse();
  }
}

abstract class ProjectSortedState implements ProjectState {
  const factory ProjectSortedState(
      {required final List<ProjectWorkflow> projectsList,
      required final String sortDirection}) = _$ProjectSortedStateImpl;

  List<ProjectWorkflow> get projectsList;
  String get sortDirection;
  @JsonKey(ignore: true)
  _$$ProjectSortedStateImplCopyWith<_$ProjectSortedStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ProjectSearchLoadingImplCopyWith<$Res> {
  factory _$$ProjectSearchLoadingImplCopyWith(_$ProjectSearchLoadingImpl value,
          $Res Function(_$ProjectSearchLoadingImpl) then) =
      __$$ProjectSearchLoadingImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ProjectSearchLoadingImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$ProjectSearchLoadingImpl>
    implements _$$ProjectSearchLoadingImplCopyWith<$Res> {
  __$$ProjectSearchLoadingImplCopyWithImpl(_$ProjectSearchLoadingImpl _value,
      $Res Function(_$ProjectSearchLoadingImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ProjectSearchLoadingImpl implements ProjectSearchLoading {
  const _$ProjectSearchLoadingImpl();

  @override
  String toString() {
    return 'ProjectState.searchLoading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectSearchLoadingImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return searchLoading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return searchLoading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return searchLoading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return searchLoading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (searchLoading != null) {
      return searchLoading(this);
    }
    return orElse();
  }
}

abstract class ProjectSearchLoading implements ProjectState {
  const factory ProjectSearchLoading() = _$ProjectSearchLoadingImpl;
}

/// @nodoc
abstract class _$$ProjectSearchResultsImplCopyWith<$Res> {
  factory _$$ProjectSearchResultsImplCopyWith(_$ProjectSearchResultsImpl value,
          $Res Function(_$ProjectSearchResultsImpl) then) =
      __$$ProjectSearchResultsImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<ProjectWorkflow> results});
}

/// @nodoc
class __$$ProjectSearchResultsImplCopyWithImpl<$Res>
    extends _$ProjectStateCopyWithImpl<$Res, _$ProjectSearchResultsImpl>
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
              as List<ProjectWorkflow>,
    ));
  }
}

/// @nodoc

class _$ProjectSearchResultsImpl implements ProjectSearchResults {
  const _$ProjectSearchResultsImpl(final List<ProjectWorkflow> results)
      : _results = results;

  final List<ProjectWorkflow> _results;
  @override
  List<ProjectWorkflow> get results {
    if (_results is EqualUnmodifiableListView) return _results;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_results);
  }

  @override
  String toString() {
    return 'ProjectState.searchResults(results: $results)';
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
    required TResult Function(List<ProjectWorkflow> projectsList) fetched,
    required TResult Function(String projectId) selected,
    required TResult Function(List<ProjectWorkflow> unSubmitted)
        unSubmittedLoaded,
    required TResult Function(CacheUnsubmittedProject entry) unSubmittedAdded,
    required TResult Function() unSubmittedDeleted,
    required TResult Function(
            int newReportCount, int inboxCount, int submittedCount)
        reportCountsLoaded,
    required TResult Function(int count) newlyAssignedLoaded,
    required TResult Function(
            List<ProjectWorkflow> projectsList, String sortDirection)
        sorted,
    required TResult Function() searchLoading,
    required TResult Function(List<ProjectWorkflow> results) searchResults,
  }) {
    return searchResults(results);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult? Function(String projectId)? selected,
    TResult? Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult? Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult? Function()? unSubmittedDeleted,
    TResult? Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult? Function(int count)? newlyAssignedLoaded,
    TResult? Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult? Function()? searchLoading,
    TResult? Function(List<ProjectWorkflow> results)? searchResults,
  }) {
    return searchResults?.call(results);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<ProjectWorkflow> projectsList)? fetched,
    TResult Function(String projectId)? selected,
    TResult Function(List<ProjectWorkflow> unSubmitted)? unSubmittedLoaded,
    TResult Function(CacheUnsubmittedProject entry)? unSubmittedAdded,
    TResult Function()? unSubmittedDeleted,
    TResult Function(int newReportCount, int inboxCount, int submittedCount)?
        reportCountsLoaded,
    TResult Function(int count)? newlyAssignedLoaded,
    TResult Function(List<ProjectWorkflow> projectsList, String sortDirection)?
        sorted,
    TResult Function()? searchLoading,
    TResult Function(List<ProjectWorkflow> results)? searchResults,
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
    required TResult Function(_ProjectInitialState value) initial,
    required TResult Function(_ProjectLoadingState value) loading,
    required TResult Function(ProjectFetchedState value) fetched,
    required TResult Function(ProjectSelectedState value) selected,
    required TResult Function(_UnSubmittedLoaded value) unSubmittedLoaded,
    required TResult Function(_UnSubmittedAdded value) unSubmittedAdded,
    required TResult Function(_UnSubmittedDeleted value) unSubmittedDeleted,
    required TResult Function(ReportCountsLoaded value) reportCountsLoaded,
    required TResult Function(NewlyAssignedLoaded value) newlyAssignedLoaded,
    required TResult Function(ProjectSortedState value) sorted,
    required TResult Function(ProjectSearchLoading value) searchLoading,
    required TResult Function(ProjectSearchResults value) searchResults,
  }) {
    return searchResults(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_ProjectInitialState value)? initial,
    TResult? Function(_ProjectLoadingState value)? loading,
    TResult? Function(ProjectFetchedState value)? fetched,
    TResult? Function(ProjectSelectedState value)? selected,
    TResult? Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult? Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult? Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult? Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult? Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult? Function(ProjectSortedState value)? sorted,
    TResult? Function(ProjectSearchLoading value)? searchLoading,
    TResult? Function(ProjectSearchResults value)? searchResults,
  }) {
    return searchResults?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_ProjectInitialState value)? initial,
    TResult Function(_ProjectLoadingState value)? loading,
    TResult Function(ProjectFetchedState value)? fetched,
    TResult Function(ProjectSelectedState value)? selected,
    TResult Function(_UnSubmittedLoaded value)? unSubmittedLoaded,
    TResult Function(_UnSubmittedAdded value)? unSubmittedAdded,
    TResult Function(_UnSubmittedDeleted value)? unSubmittedDeleted,
    TResult Function(ReportCountsLoaded value)? reportCountsLoaded,
    TResult Function(NewlyAssignedLoaded value)? newlyAssignedLoaded,
    TResult Function(ProjectSortedState value)? sorted,
    TResult Function(ProjectSearchLoading value)? searchLoading,
    TResult Function(ProjectSearchResults value)? searchResults,
    required TResult orElse(),
  }) {
    if (searchResults != null) {
      return searchResults(this);
    }
    return orElse();
  }
}

abstract class ProjectSearchResults implements ProjectState {
  const factory ProjectSearchResults(final List<ProjectWorkflow> results) =
      _$ProjectSearchResultsImpl;

  List<ProjectWorkflow> get results;
  @JsonKey(ignore: true)
  _$$ProjectSearchResultsImplCopyWith<_$ProjectSearchResultsImpl>
      get copyWith => throw _privateConstructorUsedError;
}
