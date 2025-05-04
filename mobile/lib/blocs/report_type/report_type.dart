import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'report_type.freezed.dart';

// states
@freezed
class ReportTypeState with _$ReportTypeState {
  const factory ReportTypeState.initial() = ReportTypeInitial;
  const factory ReportTypeState.newReport() = ReportTypeNew;
  const factory ReportTypeState.inbox() = ReportTypeInbox;
  const factory ReportTypeState.submitted() = ReportTypeSubmitted;
}

// events
@freezed
class ReportTypeEvent with _$ReportTypeEvent {
  const factory ReportTypeEvent.typeSelected(String reportType) =
      ReportTypeSelected;
}

// bloc
class ReportTypeBloc extends Bloc<ReportTypeEvent, ReportTypeState> {
  ReportTypeBloc() : super(const ReportTypeState.initial()) {
    on<ReportTypeSelected>(_onTypeSelected);
  }

  Future<void> _onTypeSelected(
    ReportTypeSelected event,
    Emitter<ReportTypeState> emit,
  ) async {
    switch (event.reportType.toLowerCase()) {
      case 'new-report':
        emit(const ReportTypeState.newReport());
        break;
      case 'inbox':
        emit(const ReportTypeState.inbox());
        break;
      case 'submitted':
        emit(const ReportTypeState.submitted());
        break;
      default:
        emit(const ReportTypeState.initial());
    }
  }
}
