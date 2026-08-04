import '../model/response/responsemodel.dart';
import 'utils.dart';

const amcFieldStaffRoleCode = 'AMC_FIELD_STAFF';
const installationReportPartAEditorRoleCode =
    'INSTALLATION_REPORT_PART_A_EDITOR';
const installationReportPartBEditorRoleCode =
    'INSTALLATION_REPORT_PART_B_EDITOR';
const assessorRoleCode = 'ENUMERATOR';

enum RoleSelectionOption { staff, supervisor, amc }

extension RoleSelectionOptionX on RoleSelectionOption {
  String get label {
    switch (this) {
      case RoleSelectionOption.staff:
        return 'Staff';
      case RoleSelectionOption.supervisor:
        return 'Supervisor';
      case RoleSelectionOption.amc:
        return 'AMC';
    }
  }

  String get userTypeValue {
    switch (this) {
      case RoleSelectionOption.staff:
        return USER_TYPES.FIELD_STAFF.name.toLowerCase();
      case RoleSelectionOption.supervisor:
        return USER_TYPES.SUPERVISOR.name.toLowerCase();
      case RoleSelectionOption.amc:
        return USER_TYPES.AMC.name.toLowerCase();
    }
  }

  bool get isAmc => this == RoleSelectionOption.amc;
}

class RoleLoginResolution {
  const RoleLoginResolution._({
    required this.requiresSelection,
    required this.selectionOptions,
    this.directUserType,
  });

  const RoleLoginResolution.direct(USER_TYPES userType)
      : this._(
          requiresSelection: false,
          selectionOptions: const [],
          directUserType: userType,
        );

  const RoleLoginResolution.selection(List<RoleSelectionOption> options)
      : this._(
          requiresSelection: true,
          selectionOptions: options,
        );

  final bool requiresSelection;
  final USER_TYPES? directUserType;
  final List<RoleSelectionOption> selectionOptions;
}

class RoleLoginResolver {
  const RoleLoginResolver._();

  static RoleLoginResolution resolveRoles(List<Roles> roles) {
    return resolveRoleCodes(roles.map((role) => role.code));
  }

  static RoleLoginResolution resolveRoleCodes(Iterable<String?> roleCodes) {
    final codes = roleCodes.whereType<String>().toSet();
    final hasAmc = codes.contains(amcFieldStaffRoleCode);
    final hasPartA = codes.contains(installationReportPartAEditorRoleCode);
    final hasPartB = codes.contains(installationReportPartBEditorRoleCode);
    final hasAssessor = codes.contains(assessorRoleCode);

    if (hasAssessor) {
      return const RoleLoginResolution.direct(USER_TYPES.ASSESSOR);
    }

    if (hasAmc && (hasPartA || hasPartB)) {
      if (hasPartB) {
        return const RoleLoginResolution.selection([
          RoleSelectionOption.supervisor,
          RoleSelectionOption.amc,
        ]);
      }

      return const RoleLoginResolution.selection([
        RoleSelectionOption.staff,
        RoleSelectionOption.amc,
      ]);
    }

    if (hasAmc) {
      return const RoleLoginResolution.direct(USER_TYPES.AMC);
    }
    if (hasPartB) {
      return const RoleLoginResolution.direct(USER_TYPES.SUPERVISOR);
    }

    return const RoleLoginResolution.direct(USER_TYPES.FIELD_STAFF);
  }
}
