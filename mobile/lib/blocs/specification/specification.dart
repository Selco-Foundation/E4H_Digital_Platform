import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'specification.freezed.dart';

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
    emit(state);
  }
}

@freezed
class SpecificationState with _$SpecificationState {
  const factory SpecificationState.initial() = SpecificationInitial;

  const factory SpecificationState.loaded({
    required String systemName,
    required double totalCapacity,
    required String totalCapacityUom,
  }) = SpecificationLoaded;
}

@freezed
class SpecificationEvent with _$SpecificationEvent {
  const factory SpecificationEvent.save({
    required String systemName,
    required double totalCapacity,
    required String totalCapacityUom,
  }) = SpecificationSave;
  const factory SpecificationEvent.load() = SpecificationLoad;
}
