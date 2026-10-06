import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../model/scheduled_visit/scheduled_visit.dart';

part 'selected_scheduled_visit.freezed.dart';

class SelectedScheduledVisitBloc
    extends Bloc<SelectedScheduledVisitEvent, SelectedScheduledVisitState> {
  SelectedScheduledVisitBloc()
      : super(const SelectedScheduledVisitState.initial()) {
    on<ScheduledVisitSelected>(_onScheduledVisitSelected);
    on<ScheduledVisitDeselected>(_onScheduledVisitDeselected);
  }

  void _onScheduledVisitSelected(
      ScheduledVisitSelected event, Emitter<SelectedScheduledVisitState> emit) {
    emit(SelectedScheduledVisitState.selected(event.scheduledVisit));
  }

  void _onScheduledVisitDeselected(ScheduledVisitDeselected event,
      Emitter<SelectedScheduledVisitState> emit) {
    emit(const SelectedScheduledVisitState.initial());
  }
}

@freezed
class SelectedScheduledVisitEvent with _$SelectedScheduledVisitEvent {
  const factory SelectedScheduledVisitEvent.select(
      ScheduledVisit scheduledVisit) = ScheduledVisitSelected;
  const factory SelectedScheduledVisitEvent.deselect() =
      ScheduledVisitDeselected;
}

@freezed
class SelectedScheduledVisitState with _$SelectedScheduledVisitState {
  const factory SelectedScheduledVisitState.initial() = _Initial;
  const factory SelectedScheduledVisitState.selected(
      ScheduledVisit scheduledVisit) = _Selected;
}
