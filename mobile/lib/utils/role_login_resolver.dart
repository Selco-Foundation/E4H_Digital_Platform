import '../model/response/responsemodel.dart';
import 'utils.dart';

const amcFieldStaffRoleCode = 'AMC_FIELD_STAFF';
const installationReportPartAEditorRoleCode =
    'INSTALLATION_REPORT_PART_A_EDITOR';
const installationReportPartBEditorRoleCode =
    'INSTALLATION_REPORT_PART_B_EDITOR';
const assessorRoleCode = 'ENUMERATOR';
const fieldPocRoleCode = 'FIELD_POC';

enum RoleSelectionOption { staff, supervisor, amc, assessment }

extension RoleSelectionOptionX on RoleSelectionOption {
  String get label {
    switch (this) {
      case RoleSelectionOption.staff:
        return 'Staff';
      case RoleSelectionOption.supervisor:
        return 'Supervisor';
      case RoleSelectionOption.amc:
        return 'AMC';
      case RoleSelectionOption.assessment:
        return 'Assessment';
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
      case RoleSelectionOption.assessment:
        return USER_TYPES.ASSESSOR.name.toLowerCase();
    }
  }

  bool get isAmc => this == RoleSelectionOption.amc;

  bool get isAssessment => this == RoleSelectionOption.assessment;

  String get moduleLabel {
    switch (this) {
      case RoleSelectionOption.staff:
      case RoleSelectionOption.supervisor:
        return 'Installation';
      case RoleSelectionOption.amc:
        return 'AMC';
      case RoleSelectionOption.assessment:
        return 'Assessment';
    }
  }
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
    final hasAssessment =
        codes.contains(assessorRoleCode) || codes.contains(fieldPocRoleCode);

    final moduleOptions = <RoleSelectionOption>[];
    if (hasPartA || hasPartB) {
      moduleOptions.add(
        hasPartB ? RoleSelectionOption.supervisor : RoleSelectionOption.staff,
      );
    }
    if (hasAmc) {
      moduleOptions.add(RoleSelectionOption.amc);
    }
    if (hasAssessment) {
      moduleOptions.add(RoleSelectionOption.assessment);
    }

    if (moduleOptions.length > 1) {
      return RoleLoginResolution.selection(moduleOptions);
    }

    if (moduleOptions.length == 1) {
      switch (moduleOptions.single) {
        case RoleSelectionOption.staff:
          return const RoleLoginResolution.direct(USER_TYPES.FIELD_STAFF);
        case RoleSelectionOption.supervisor:
          return const RoleLoginResolution.direct(USER_TYPES.SUPERVISOR);
        case RoleSelectionOption.amc:
          return const RoleLoginResolution.direct(USER_TYPES.AMC);
        case RoleSelectionOption.assessment:
          return const RoleLoginResolution.direct(USER_TYPES.ASSESSOR);
      }
    }

    return const RoleLoginResolution.direct(USER_TYPES.FIELD_STAFF);
  }
}
