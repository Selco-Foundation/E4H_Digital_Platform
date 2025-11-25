import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:selco/utils/utils.dart';

part 'selected_amc_origin.freezed.dart';

class SelectedAmcOriginBloc
    extends Bloc<SelectedAmcOriginEvent, SelectedAmcOriginState> {
  SelectedAmcOriginBloc() : super(const SelectedAmcOriginState.initial()) {
    on<AmcOriginSelected>(_onAmcOriginSelected);
    on<AmcOriginDeselected>(_onAmcOriginDeselected);
  }

  void _onAmcOriginSelected(
      AmcOriginSelected event, Emitter<SelectedAmcOriginState> emit) {
    emit(SelectedAmcOriginState.selected(event.origin));
  }

  void _onAmcOriginDeselected(
      AmcOriginDeselected event, Emitter<SelectedAmcOriginState> emit) {
    emit(const SelectedAmcOriginState.initial());
  }
}

@freezed
class SelectedAmcOriginEvent with _$SelectedAmcOriginEvent {
  const factory SelectedAmcOriginEvent.select(FormOrigin origin) =
      AmcOriginSelected;
  const factory SelectedAmcOriginEvent.deselect() = AmcOriginDeselected;
}

@freezed
class SelectedAmcOriginState with _$SelectedAmcOriginState {
  const factory SelectedAmcOriginState.initial() = _Initial;
  const factory SelectedAmcOriginState.selected(FormOrigin origin) = _Selected;
}
