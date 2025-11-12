import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'user_type.freezed.dart';

@freezed
class UserTypeState with _$UserTypeState {
  const factory UserTypeState.initial() = UserTypeInitial;
  const factory UserTypeState.staff() = UserTypeStaff;
  const factory UserTypeState.supervisor() = UserTypeSupervisor;
}

@freezed
class UserTypeEvent with _$UserTypeEvent {
  const factory UserTypeEvent.typeSelected(String userType) = UserTypeSelected;
}

class UserTypeBloc extends Bloc<UserTypeEvent, UserTypeState> {
  UserTypeBloc() : super(const UserTypeState.initial()) {
    on<UserTypeSelected>(_onTypeSelected);
  }

  Future<void> _onTypeSelected(
    UserTypeSelected event,
    Emitter<UserTypeState> emit,
  ) async {
    switch (event.userType.toLowerCase()) {
      case 'initial':
        emit(const UserTypeState.staff());
        break;
      case 'staff':
        emit(const UserTypeState.staff());
        break;
      case 'supervisor':
        emit(const UserTypeState.supervisor());
        break;
      default:
        emit(const UserTypeState.staff());
    }
  }
}
