// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'activity_facility_workflow.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

ActivityFacilityWorkflow _$ActivityFacilityWorkflowFromJson(
    Map<String, dynamic> json) {
  return _ActivityFacilityWorkflow.fromJson(json);
}

/// @nodoc
mixin _$ActivityFacilityWorkflow {
  @ActivityFacilityConverter()
  ActivityFacility get activityFacility => throw _privateConstructorUsedError;
  String? get status => throw _privateConstructorUsedError;
  List<Transaction>? get transactions => throw _privateConstructorUsedError;
  @WorkflowFlexConverter()
  Workflow? get workflow => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $ActivityFacilityWorkflowCopyWith<ActivityFacilityWorkflow> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ActivityFacilityWorkflowCopyWith<$Res> {
  factory $ActivityFacilityWorkflowCopyWith(ActivityFacilityWorkflow value,
          $Res Function(ActivityFacilityWorkflow) then) =
      _$ActivityFacilityWorkflowCopyWithImpl<$Res, ActivityFacilityWorkflow>;
  @useResult
  $Res call(
      {@ActivityFacilityConverter() ActivityFacility activityFacility,
      String? status,
      List<Transaction>? transactions,
      @WorkflowFlexConverter() Workflow? workflow});
}

/// @nodoc
class _$ActivityFacilityWorkflowCopyWithImpl<$Res,
        $Val extends ActivityFacilityWorkflow>
    implements $ActivityFacilityWorkflowCopyWith<$Res> {
  _$ActivityFacilityWorkflowCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacility = null,
    Object? status = freezed,
    Object? transactions = freezed,
    Object? workflow = freezed,
  }) {
    return _then(_value.copyWith(
      activityFacility: null == activityFacility
          ? _value.activityFacility
          : activityFacility // ignore: cast_nullable_to_non_nullable
              as ActivityFacility,
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
abstract class _$$ActivityFacilityWorkflowImplCopyWith<$Res>
    implements $ActivityFacilityWorkflowCopyWith<$Res> {
  factory _$$ActivityFacilityWorkflowImplCopyWith(
          _$ActivityFacilityWorkflowImpl value,
          $Res Function(_$ActivityFacilityWorkflowImpl) then) =
      __$$ActivityFacilityWorkflowImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {@ActivityFacilityConverter() ActivityFacility activityFacility,
      String? status,
      List<Transaction>? transactions,
      @WorkflowFlexConverter() Workflow? workflow});
}

/// @nodoc
class __$$ActivityFacilityWorkflowImplCopyWithImpl<$Res>
    extends _$ActivityFacilityWorkflowCopyWithImpl<$Res,
        _$ActivityFacilityWorkflowImpl>
    implements _$$ActivityFacilityWorkflowImplCopyWith<$Res> {
  __$$ActivityFacilityWorkflowImplCopyWithImpl(
      _$ActivityFacilityWorkflowImpl _value,
      $Res Function(_$ActivityFacilityWorkflowImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacility = null,
    Object? status = freezed,
    Object? transactions = freezed,
    Object? workflow = freezed,
  }) {
    return _then(_$ActivityFacilityWorkflowImpl(
      activityFacility: null == activityFacility
          ? _value.activityFacility
          : activityFacility // ignore: cast_nullable_to_non_nullable
              as ActivityFacility,
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
class _$ActivityFacilityWorkflowImpl implements _ActivityFacilityWorkflow {
  const _$ActivityFacilityWorkflowImpl(
      {@ActivityFacilityConverter() required this.activityFacility,
      this.status,
      final List<Transaction>? transactions,
      @WorkflowFlexConverter() this.workflow})
      : _transactions = transactions;

  factory _$ActivityFacilityWorkflowImpl.fromJson(Map<String, dynamic> json) =>
      _$$ActivityFacilityWorkflowImplFromJson(json);

  @override
  @ActivityFacilityConverter()
  final ActivityFacility activityFacility;
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
  @WorkflowFlexConverter()
  final Workflow? workflow;

  @override
  String toString() {
    return 'ActivityFacilityWorkflow(activityFacility: $activityFacility, status: $status, transactions: $transactions, workflow: $workflow)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilityWorkflowImpl &&
            (identical(other.activityFacility, activityFacility) ||
                other.activityFacility == activityFacility) &&
            (identical(other.status, status) || other.status == status) &&
            const DeepCollectionEquality()
                .equals(other._transactions, _transactions) &&
            (identical(other.workflow, workflow) ||
                other.workflow == workflow));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, activityFacility, status,
      const DeepCollectionEquality().hash(_transactions), workflow);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilityWorkflowImplCopyWith<_$ActivityFacilityWorkflowImpl>
      get copyWith => __$$ActivityFacilityWorkflowImplCopyWithImpl<
          _$ActivityFacilityWorkflowImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ActivityFacilityWorkflowImplToJson(
      this,
    );
  }
}

abstract class _ActivityFacilityWorkflow implements ActivityFacilityWorkflow {
  const factory _ActivityFacilityWorkflow(
          {@ActivityFacilityConverter()
          required final ActivityFacility activityFacility,
          final String? status,
          final List<Transaction>? transactions,
          @WorkflowFlexConverter() final Workflow? workflow}) =
      _$ActivityFacilityWorkflowImpl;

  factory _ActivityFacilityWorkflow.fromJson(Map<String, dynamic> json) =
      _$ActivityFacilityWorkflowImpl.fromJson;

  @override
  @ActivityFacilityConverter()
  ActivityFacility get activityFacility;
  @override
  String? get status;
  @override
  List<Transaction>? get transactions;
  @override
  @WorkflowFlexConverter()
  Workflow? get workflow;
  @override
  @JsonKey(ignore: true)
  _$$ActivityFacilityWorkflowImplCopyWith<_$ActivityFacilityWorkflowImpl>
      get copyWith => throw _privateConstructorUsedError;
}
