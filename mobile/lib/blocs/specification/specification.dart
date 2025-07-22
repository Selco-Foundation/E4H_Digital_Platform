// lib/blocs/specification/specification_bloc.dart

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'specification.freezed.dart';

/// --- BLOC ---
class SpecificationBloc extends Bloc<SpecificationEvent, SpecificationState> {
  SpecificationBloc() : super(const SpecificationState.initial()) {
    on<SpecificationSave>(_onSave);
    on<SpecificationLoad>(_onLoad);
  }

  Future<void> _onSave(
    SpecificationSave event,
    Emitter<SpecificationState> emit,
  ) async {
    emit(SpecificationState.loaded(
      systemName: event.systemName,
      totalCapacity: event.totalCapacity,
      totalCapacityUom: event.totalCapacityUom,
    ));
  }

  Future<void> _onLoad(
    SpecificationLoad event,
    Emitter<SpecificationState> emit,
  ) async {
    // simply re-emit current state so UI can react
    emit(state);
  }
}

/// --- STATES ---
@freezed
class SpecificationState with _$SpecificationState {
  /// initial: nothing saved yet
  const factory SpecificationState.initial() = SpecificationInitial;

  /// once saved, holds the three fields
  const factory SpecificationState.loaded({
    required String systemName,
    required double totalCapacity,
    required String totalCapacityUom,
  }) = SpecificationLoaded;
}

/// --- EVENTS ---
@freezed
class SpecificationEvent with _$SpecificationEvent {
  /// fire this to save new values
  const factory SpecificationEvent.save({
    required String systemName,
    required double totalCapacity,
    required String totalCapacityUom,
  }) = SpecificationSave;

  /// optional: ask the bloc to re-emit whatever it already has
  const factory SpecificationEvent.load() = SpecificationLoad;
}
