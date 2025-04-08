// onboarding_bloc.dart
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'onboarding_bloc.freezed.dart';

@freezed
class OnboardingState with _$OnboardingState {
  const factory OnboardingState({required int? selectedValue}) =
      _OnboardingState;

  factory OnboardingState.initial() =>
      const OnboardingState(selectedValue: null);
}

@freezed
class OnboardingEvent with _$OnboardingEvent {
  const factory OnboardingEvent.optionSelected(int value) = OptionSelected;
  const factory OnboardingEvent.navigateNext() = NavigateNext;
}

class OnboardingBloc extends Bloc<OnboardingEvent, OnboardingState> {
  OnboardingBloc() : super(OnboardingState.initial()) {
    on<OptionSelected>(_onOptionSelected);
    on<NavigateNext>(_onNavigateNext);
  }

  Future<void> _onOptionSelected(
    OptionSelected event,
    Emitter<OnboardingState> emit,
  ) async {
    emit(state.copyWith(selectedValue: event.value));
    await Future.delayed(const Duration(milliseconds: 300));
    add(const OnboardingEvent.navigateNext());
  }

  Future<void> _onNavigateNext(
    NavigateNext event,
    Emitter<OnboardingState> emit,
  ) async {
    // Navigation logic will be handled in UI layer
  }
}
