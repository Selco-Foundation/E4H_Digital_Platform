// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'onboarding_bloc.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$OnboardingState {
  int? get selectedValue => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $OnboardingStateCopyWith<OnboardingState> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $OnboardingStateCopyWith<$Res> {
  factory $OnboardingStateCopyWith(
          OnboardingState value, $Res Function(OnboardingState) then) =
      _$OnboardingStateCopyWithImpl<$Res, OnboardingState>;
  @useResult
  $Res call({int? selectedValue});
}

/// @nodoc
class _$OnboardingStateCopyWithImpl<$Res, $Val extends OnboardingState>
    implements $OnboardingStateCopyWith<$Res> {
  _$OnboardingStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? selectedValue = freezed,
  }) {
    return _then(_value.copyWith(
      selectedValue: freezed == selectedValue
          ? _value.selectedValue
          : selectedValue // ignore: cast_nullable_to_non_nullable
              as int?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$OnboardingStateImplCopyWith<$Res>
    implements $OnboardingStateCopyWith<$Res> {
  factory _$$OnboardingStateImplCopyWith(_$OnboardingStateImpl value,
          $Res Function(_$OnboardingStateImpl) then) =
      __$$OnboardingStateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int? selectedValue});
}

/// @nodoc
class __$$OnboardingStateImplCopyWithImpl<$Res>
    extends _$OnboardingStateCopyWithImpl<$Res, _$OnboardingStateImpl>
    implements _$$OnboardingStateImplCopyWith<$Res> {
  __$$OnboardingStateImplCopyWithImpl(
      _$OnboardingStateImpl _value, $Res Function(_$OnboardingStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? selectedValue = freezed,
  }) {
    return _then(_$OnboardingStateImpl(
      selectedValue: freezed == selectedValue
          ? _value.selectedValue
          : selectedValue // ignore: cast_nullable_to_non_nullable
              as int?,
    ));
  }
}

/// @nodoc

class _$OnboardingStateImpl implements _OnboardingState {
  const _$OnboardingStateImpl({required this.selectedValue});

  @override
  final int? selectedValue;

  @override
  String toString() {
    return 'OnboardingState(selectedValue: $selectedValue)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$OnboardingStateImpl &&
            (identical(other.selectedValue, selectedValue) ||
                other.selectedValue == selectedValue));
  }

  @override
  int get hashCode => Object.hash(runtimeType, selectedValue);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$OnboardingStateImplCopyWith<_$OnboardingStateImpl> get copyWith =>
      __$$OnboardingStateImplCopyWithImpl<_$OnboardingStateImpl>(
          this, _$identity);
}

abstract class _OnboardingState implements OnboardingState {
  const factory _OnboardingState({required final int? selectedValue}) =
      _$OnboardingStateImpl;

  @override
  int? get selectedValue;
  @override
  @JsonKey(ignore: true)
  _$$OnboardingStateImplCopyWith<_$OnboardingStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$OnboardingEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(int value) optionSelected,
    required TResult Function() navigateNext,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(int value)? optionSelected,
    TResult? Function()? navigateNext,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(int value)? optionSelected,
    TResult Function()? navigateNext,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(OptionSelected value) optionSelected,
    required TResult Function(NavigateNext value) navigateNext,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(OptionSelected value)? optionSelected,
    TResult? Function(NavigateNext value)? navigateNext,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(OptionSelected value)? optionSelected,
    TResult Function(NavigateNext value)? navigateNext,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $OnboardingEventCopyWith<$Res> {
  factory $OnboardingEventCopyWith(
          OnboardingEvent value, $Res Function(OnboardingEvent) then) =
      _$OnboardingEventCopyWithImpl<$Res, OnboardingEvent>;
}

/// @nodoc
class _$OnboardingEventCopyWithImpl<$Res, $Val extends OnboardingEvent>
    implements $OnboardingEventCopyWith<$Res> {
  _$OnboardingEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$OptionSelectedImplCopyWith<$Res> {
  factory _$$OptionSelectedImplCopyWith(_$OptionSelectedImpl value,
          $Res Function(_$OptionSelectedImpl) then) =
      __$$OptionSelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int value});
}

/// @nodoc
class __$$OptionSelectedImplCopyWithImpl<$Res>
    extends _$OnboardingEventCopyWithImpl<$Res, _$OptionSelectedImpl>
    implements _$$OptionSelectedImplCopyWith<$Res> {
  __$$OptionSelectedImplCopyWithImpl(
      _$OptionSelectedImpl _value, $Res Function(_$OptionSelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? value = null,
  }) {
    return _then(_$OptionSelectedImpl(
      null == value
          ? _value.value
          : value // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$OptionSelectedImpl implements OptionSelected {
  const _$OptionSelectedImpl(this.value);

  @override
  final int value;

  @override
  String toString() {
    return 'OnboardingEvent.optionSelected(value: $value)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$OptionSelectedImpl &&
            (identical(other.value, value) || other.value == value));
  }

  @override
  int get hashCode => Object.hash(runtimeType, value);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$OptionSelectedImplCopyWith<_$OptionSelectedImpl> get copyWith =>
      __$$OptionSelectedImplCopyWithImpl<_$OptionSelectedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(int value) optionSelected,
    required TResult Function() navigateNext,
  }) {
    return optionSelected(value);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(int value)? optionSelected,
    TResult? Function()? navigateNext,
  }) {
    return optionSelected?.call(value);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(int value)? optionSelected,
    TResult Function()? navigateNext,
    required TResult orElse(),
  }) {
    if (optionSelected != null) {
      return optionSelected(value);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(OptionSelected value) optionSelected,
    required TResult Function(NavigateNext value) navigateNext,
  }) {
    return optionSelected(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(OptionSelected value)? optionSelected,
    TResult? Function(NavigateNext value)? navigateNext,
  }) {
    return optionSelected?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(OptionSelected value)? optionSelected,
    TResult Function(NavigateNext value)? navigateNext,
    required TResult orElse(),
  }) {
    if (optionSelected != null) {
      return optionSelected(this);
    }
    return orElse();
  }
}

abstract class OptionSelected implements OnboardingEvent {
  const factory OptionSelected(final int value) = _$OptionSelectedImpl;

  int get value;
  @JsonKey(ignore: true)
  _$$OptionSelectedImplCopyWith<_$OptionSelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$NavigateNextImplCopyWith<$Res> {
  factory _$$NavigateNextImplCopyWith(
          _$NavigateNextImpl value, $Res Function(_$NavigateNextImpl) then) =
      __$$NavigateNextImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$NavigateNextImplCopyWithImpl<$Res>
    extends _$OnboardingEventCopyWithImpl<$Res, _$NavigateNextImpl>
    implements _$$NavigateNextImplCopyWith<$Res> {
  __$$NavigateNextImplCopyWithImpl(
      _$NavigateNextImpl _value, $Res Function(_$NavigateNextImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$NavigateNextImpl implements NavigateNext {
  const _$NavigateNextImpl();

  @override
  String toString() {
    return 'OnboardingEvent.navigateNext()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$NavigateNextImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(int value) optionSelected,
    required TResult Function() navigateNext,
  }) {
    return navigateNext();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(int value)? optionSelected,
    TResult? Function()? navigateNext,
  }) {
    return navigateNext?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(int value)? optionSelected,
    TResult Function()? navigateNext,
    required TResult orElse(),
  }) {
    if (navigateNext != null) {
      return navigateNext();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(OptionSelected value) optionSelected,
    required TResult Function(NavigateNext value) navigateNext,
  }) {
    return navigateNext(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(OptionSelected value)? optionSelected,
    TResult? Function(NavigateNext value)? navigateNext,
  }) {
    return navigateNext?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(OptionSelected value)? optionSelected,
    TResult Function(NavigateNext value)? navigateNext,
    required TResult orElse(),
  }) {
    if (navigateNext != null) {
      return navigateNext(this);
    }
    return orElse();
  }
}

abstract class NavigateNext implements OnboardingEvent {
  const factory NavigateNext() = _$NavigateNextImpl;
}
