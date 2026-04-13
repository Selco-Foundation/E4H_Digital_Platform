import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../model/activity_facility_workflow/activity_facility_workflow.dart';

part 'selected_activity_facility.freezed.dart';

class SelectedActivityFacilityBloc
    extends Bloc<SelectedActivityFacilityEvent, SelectedActivityFacilityState> {
  SelectedActivityFacilityBloc()
      : super(const SelectedActivityFacilityState.initial()) {
    on<ActivityFacilitySelected>(_onActivityFacilitySelected);
    on<ActivityFacilityDeselected>(_onActivityFacilityDeselected);
  }

  void _onActivityFacilitySelected(ActivityFacilitySelected event,
      Emitter<SelectedActivityFacilityState> emit) {
    emit(SelectedActivityFacilityState.selected(event.activityFacility));
  }

  void _onActivityFacilityDeselected(ActivityFacilityDeselected event,
      Emitter<SelectedActivityFacilityState> emit) {
    emit(const SelectedActivityFacilityState.initial());
  }
}

@freezed
class SelectedActivityFacilityEvent with _$SelectedActivityFacilityEvent {
  const factory SelectedActivityFacilityEvent.select(
      ActivityFacilityWorkflow activityFacility) = ActivityFacilitySelected;
  const factory SelectedActivityFacilityEvent.deselect() =
      ActivityFacilityDeselected;
}

@freezed
class SelectedActivityFacilityState with _$SelectedActivityFacilityState {
  const factory SelectedActivityFacilityState.initial() = _Initial;
  const factory SelectedActivityFacilityState.selected(
      ActivityFacilityWorkflow activityFacility) = _Selected;
}
