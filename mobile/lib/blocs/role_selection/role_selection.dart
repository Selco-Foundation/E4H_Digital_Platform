import 'package:flutter_bloc/flutter_bloc.dart';

import '../../model/response/responsemodel.dart';
import '../../utils/role_login_resolver.dart';

class RoleSelectionState {
  const RoleSelectionState({
    required this.availableRoles,
    this.selectedRole,
  });

  final List<RoleSelectionOption> availableRoles;
  final RoleSelectionOption? selectedRole;

  bool get canProceed => selectedRole != null;

  RoleSelectionState copyWith({
    List<RoleSelectionOption>? availableRoles,
    RoleSelectionOption? selectedRole,
  }) {
    return RoleSelectionState(
      availableRoles: availableRoles ?? this.availableRoles,
      selectedRole: selectedRole ?? this.selectedRole,
    );
  }
}

abstract class RoleSelectionEvent {
  const RoleSelectionEvent();
}

class RoleSelectionRoleSelected extends RoleSelectionEvent {
  const RoleSelectionRoleSelected(this.role);

  final RoleSelectionOption role;
}

class RoleSelectionBloc extends Bloc<RoleSelectionEvent, RoleSelectionState> {
  RoleSelectionBloc({required List<Roles> roles})
      : super(RoleSelectionState(
          availableRoles:
              RoleLoginResolver.resolveRoles(roles).selectionOptions,
        )) {
    on<RoleSelectionRoleSelected>(_onRoleSelected);
  }

  void _onRoleSelected(
    RoleSelectionRoleSelected event,
    Emitter<RoleSelectionState> emit,
  ) {
    emit(state.copyWith(selectedRole: event.role));
  }
}
