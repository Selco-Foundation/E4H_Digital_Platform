import 'package:synchronized/synchronized.dart';

import '../data/secure_storage/secureStore.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';

abstract class ReportBookmarkRepository<T> {
  static final _lock = Lock();
  final SecureStore storage;
  final String tenantId;
  final String userId;
  final String userType;
  final String kind;

  ReportBookmarkRepository(
      {SecureStore? storage,
      required this.tenantId,
      required this.userId,
      required this.userType,
      required this.kind})
      : storage = storage ?? SecureStore();

  String identity(T item);
  String facilityName(T item);
  Map<String, dynamic> encode(T item);
  T decode(Map<String, dynamic> json);

  Future<Map<String, dynamic>> _read() async {
    if (userId.trim().isEmpty) return {};
    return storage.getReportBookmarks(kind, tenantId, userId, userType);
  }

  Future<Set<String>> ids() async => (await _read()).keys.toSet();
  Future<int> count() async => (await _read()).length;
  Future<List<T>> list({String query = '', String sortOrder = 'DESC'}) async {
    final entries = (await _read())
        .values
        .map((e) => Map<String, dynamic>.from(e as Map))
        .toList();
    entries.sort((a, b) {
      final order =
          (a['bookmarkedAt'] as int).compareTo(b['bookmarkedAt'] as int);
      return sortOrder == 'ASC' ? order : -order;
    });
    final search = query.trim().toLowerCase();
    return entries
        .map((e) => decode(Map<String, dynamic>.from(e['record'] as Map)))
        .where((item) => facilityName(item).toLowerCase().contains(search))
        .toList();
  }

  Future<void> save(T item) => _lock.synchronized(() async {
        final id = identity(item).trim();
        if (id.isEmpty || userId.trim().isEmpty) {
          throw const FormatException('Missing report bookmark identity');
        }
        final entries = await _read();
        entries[id] = {
          'record': encode(item),
          'bookmarkedAt': (entries[id] as Map?)?['bookmarkedAt'] ??
              DateTime.now().microsecondsSinceEpoch,
        };
        await storage.setReportBookmarks(
            kind, tenantId, userId, userType, entries);
      });

  Future<void> remove(String id) => _lock.synchronized(() async {
        final entries = await _read();
        if (entries.remove(id.trim()) == null) return;
        await storage.setReportBookmarks(
            kind, tenantId, userId, userType, entries);
      });
}

class InstallationBookmarkRepository
    extends ReportBookmarkRepository<ActivityFacilityWorkflow> {
  InstallationBookmarkRepository(
      {super.storage,
      required super.tenantId,
      required super.userId,
      required super.userType})
      : super(kind: 'installation');
  @override
  String identity(ActivityFacilityWorkflow item) => item.activityFacility.id;
  @override
  String facilityName(ActivityFacilityWorkflow item) =>
      item.activityFacility.facility?.facilityName ?? '';
  @override
  Map<String, dynamic> encode(ActivityFacilityWorkflow item) => item.toJson();
  @override
  ActivityFacilityWorkflow decode(Map<String, dynamic> json) {
    final record = ActivityFacilityWorkflow.fromJson(json);
    final facility = Map<String, dynamic>.from(json['activityFacility'] as Map);
    record.activityFacility.description = facility['description'] as String?;
    record.activityFacility.rowVersion = facility['rowVersion'] as int?;
    return record;
  }
}

class AmcBookmarkRepository extends ReportBookmarkRepository<ScheduledVisit> {
  AmcBookmarkRepository(
      {super.storage,
      required super.tenantId,
      required super.userId,
      required super.userType})
      : super(kind: 'amc');
  @override
  String identity(ScheduledVisit item) => item.id ?? '';
  @override
  String facilityName(ScheduledVisit item) => item.facility?.facilityName ?? '';
  @override
  Map<String, dynamic> encode(ScheduledVisit item) => item.toJson();
  @override
  ScheduledVisit decode(Map<String, dynamic> json) =>
      ScheduledVisit.fromJson(json);
}

/// Called only after the server has accepted the final report workflow action.
Future<void> removeSubmittedReportBookmark(
    {required String id,
    required String action,
    SecureStore? storage,
    String? tenantId}) async {
  final isInstallation = action == WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name ||
      action == WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name;
  if (!isInstallation && action != 'SUBMIT_VISIT_REPORT') return;
  try {
    final store = storage ?? SecureStore();
    final user = (await store.getAccessInfo())?.userRequest;
    final userId = user?.uuid ?? user?.userName ?? '';
    final type = isInstallation
        ? (action == WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name
            ? USER_TYPES.SUPERVISOR.name
            : USER_TYPES.FIELD_STAFF.name)
        : USER_TYPES.AMC.name;
    final tenant = tenantId ?? envConfig.variables.tenantId;
    final repo = isInstallation
        ? InstallationBookmarkRepository(
            storage: store, tenantId: tenant, userId: userId, userType: type)
        : AmcBookmarkRepository(
            storage: store, tenantId: tenant, userId: userId, userType: type);
    if (repo is InstallationBookmarkRepository) {
      await repo.remove(id);
    } else if (repo is AmcBookmarkRepository) {
      await repo.remove(id);
    }
  } catch (error) {
    AppLogger.instance
        .info('Unable to remove submitted report bookmark: $error');
  }
}
