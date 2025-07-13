// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'project_workflow.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

ProjectWorkflow _$ProjectWorkflowFromJson(Map<String, dynamic> json) {
  return _ProjectWorkflow.fromJson(json);
}

/// @nodoc
mixin _$ProjectWorkflow {
  @ProjectModelConverter()
  ProjectModel get project => throw _privateConstructorUsedError;
  String? get status => throw _privateConstructorUsedError;
  List<Transaction>? get transactions => throw _privateConstructorUsedError;
  Workflow? get workflow => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $ProjectWorkflowCopyWith<ProjectWorkflow> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProjectWorkflowCopyWith<$Res> {
  factory $ProjectWorkflowCopyWith(
          ProjectWorkflow value, $Res Function(ProjectWorkflow) then) =
      _$ProjectWorkflowCopyWithImpl<$Res, ProjectWorkflow>;
  @useResult
  $Res call(
      {@ProjectModelConverter() ProjectModel project,
      String? status,
      List<Transaction>? transactions,
      Workflow? workflow});
}

/// @nodoc
class _$ProjectWorkflowCopyWithImpl<$Res, $Val extends ProjectWorkflow>
    implements $ProjectWorkflowCopyWith<$Res> {
  _$ProjectWorkflowCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? project = null,
    Object? status = freezed,
    Object? transactions = freezed,
    Object? workflow = freezed,
  }) {
    return _then(_value.copyWith(
      project: null == project
          ? _value.project
          : project // ignore: cast_nullable_to_non_nullable
              as ProjectModel,
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      transactions: freezed == transactions
          ? _value.transactions
          : transactions // ignore: cast_nullable_to_non_nullable
              as List<Transaction>?,
      workflow: freezed == workflow
          ? _value.workflow
          : workflow // ignore: cast_nullable_to_non_nullable
              as Workflow?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ProjectWorkflowImplCopyWith<$Res>
    implements $ProjectWorkflowCopyWith<$Res> {
  factory _$$ProjectWorkflowImplCopyWith(_$ProjectWorkflowImpl value,
          $Res Function(_$ProjectWorkflowImpl) then) =
      __$$ProjectWorkflowImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {@ProjectModelConverter() ProjectModel project,
      String? status,
      List<Transaction>? transactions,
      Workflow? workflow});
}

/// @nodoc
class __$$ProjectWorkflowImplCopyWithImpl<$Res>
    extends _$ProjectWorkflowCopyWithImpl<$Res, _$ProjectWorkflowImpl>
    implements _$$ProjectWorkflowImplCopyWith<$Res> {
  __$$ProjectWorkflowImplCopyWithImpl(
      _$ProjectWorkflowImpl _value, $Res Function(_$ProjectWorkflowImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? project = null,
    Object? status = freezed,
    Object? transactions = freezed,
    Object? workflow = freezed,
  }) {
    return _then(_$ProjectWorkflowImpl(
      project: null == project
          ? _value.project
          : project // ignore: cast_nullable_to_non_nullable
              as ProjectModel,
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      transactions: freezed == transactions
          ? _value._transactions
          : transactions // ignore: cast_nullable_to_non_nullable
              as List<Transaction>?,
      workflow: freezed == workflow
          ? _value.workflow
          : workflow // ignore: cast_nullable_to_non_nullable
              as Workflow?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ProjectWorkflowImpl implements _ProjectWorkflow {
  const _$ProjectWorkflowImpl(
      {@ProjectModelConverter() required this.project,
      this.status,
      final List<Transaction>? transactions,
      this.workflow})
      : _transactions = transactions;

  factory _$ProjectWorkflowImpl.fromJson(Map<String, dynamic> json) =>
      _$$ProjectWorkflowImplFromJson(json);

  @override
  @ProjectModelConverter()
  final ProjectModel project;
  @override
  final String? status;
  final List<Transaction>? _transactions;
  @override
  List<Transaction>? get transactions {
    final value = _transactions;
    if (value == null) return null;
    if (_transactions is EqualUnmodifiableListView) return _transactions;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(value);
  }

  @override
  final Workflow? workflow;

  @override
  String toString() {
    return 'ProjectWorkflow(project: $project, status: $status, transactions: $transactions, workflow: $workflow)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectWorkflowImpl &&
            (identical(other.project, project) || other.project == project) &&
            (identical(other.status, status) || other.status == status) &&
            const DeepCollectionEquality()
                .equals(other._transactions, _transactions) &&
            (identical(other.workflow, workflow) ||
                other.workflow == workflow));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, project, status,
      const DeepCollectionEquality().hash(_transactions), workflow);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProjectWorkflowImplCopyWith<_$ProjectWorkflowImpl> get copyWith =>
      __$$ProjectWorkflowImplCopyWithImpl<_$ProjectWorkflowImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ProjectWorkflowImplToJson(
      this,
    );
  }
}

abstract class _ProjectWorkflow implements ProjectWorkflow {
  const factory _ProjectWorkflow(
      {@ProjectModelConverter() required final ProjectModel project,
      final String? status,
      final List<Transaction>? transactions,
      final Workflow? workflow}) = _$ProjectWorkflowImpl;

  factory _ProjectWorkflow.fromJson(Map<String, dynamic> json) =
      _$ProjectWorkflowImpl.fromJson;

  @override
  @ProjectModelConverter()
  ProjectModel get project;
  @override
  String? get status;
  @override
  List<Transaction>? get transactions;
  @override
  Workflow? get workflow;
  @override
  @JsonKey(ignore: true)
  _$$ProjectWorkflowImplCopyWith<_$ProjectWorkflowImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
