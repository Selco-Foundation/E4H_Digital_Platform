import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../model/projects/project.dart';

part 'selected_project.freezed.dart';

class SelectedProjectBloc
    extends Bloc<SelectedProjectEvent, SelectedProjectState> {
  SelectedProjectBloc() : super(const SelectedProjectState.initial()) {
    on<ProjectSelected>(_onProjectSelected);
    on<ProjectDeselected>(_onProjectDeselected);
  }

  void _onProjectSelected(
      ProjectSelected event, Emitter<SelectedProjectState> emit) {
    emit(SelectedProjectState.selected(event.project));
  }

  void _onProjectDeselected(
      ProjectDeselected event, Emitter<SelectedProjectState> emit) {
    emit(const SelectedProjectState.initial());
  }
}

@freezed
class SelectedProjectEvent with _$SelectedProjectEvent {
  const factory SelectedProjectEvent.select(ProjectModel project) =
      ProjectSelected;
  const factory SelectedProjectEvent.deselect() = ProjectDeselected;
}

@freezed
class SelectedProjectState with _$SelectedProjectState {
  const factory SelectedProjectState.initial() = _Initial;
  const factory SelectedProjectState.selected(ProjectModel project) = _Selected;
}
