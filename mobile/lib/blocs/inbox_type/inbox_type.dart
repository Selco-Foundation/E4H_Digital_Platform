import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'inbox_type.freezed.dart';

@freezed
class InboxTypeState with _$InboxTypeState {
  const factory InboxTypeState.submitted() = InboxTypeSubmitted;
  const factory InboxTypeState.rejected() = InboxTypeRejected;
  const factory InboxTypeState.approved() = InboxTypeApproved;
}

@freezed
class InboxTypeEvent with _$InboxTypeEvent {
  const factory InboxTypeEvent.typeSelected(int inboxType) = InboxTypeSelected;
}

class InboxTypeBloc extends Bloc<InboxTypeEvent, InboxTypeState> {
  InboxTypeBloc() : super(const InboxTypeState.submitted()) {
    on<InboxTypeSelected>(_onTypeSelected);
  }

  Future<void> _onTypeSelected(
    InboxTypeSelected event,
    Emitter<InboxTypeState> emit,
  ) async {
    switch (event.inboxType) {
      case 0:
        emit(const InboxTypeState.submitted());
        break;
      case 1:
        emit(const InboxTypeState.rejected());
        break;
      case 2:
        emit(const InboxTypeState.approved());
        break;
      default:
        emit(const InboxTypeState.submitted());
    }
  }
}
