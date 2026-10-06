import 'dart:convert';

import 'package:isar/isar.dart';

import '../../model/scheduled_visit/scheduled_visit.dart';

part 'cache_scheduled_visit.g.dart';

@Collection()
class CacheScheduledVisit {
  Id id = Isar.autoIncrement;

  @Index()
  late String scheduledVisitId;

  @Index()
  late String facilityId;

  @Index()
  late String status;

  @Index()
  late DateTime scheduledDate;

  /// Raw JSON of ScheduledVisit.toJson()
  late String json;

  CacheScheduledVisit({
    required this.scheduledVisitId,
    required this.facilityId,
    required this.status,
    required this.scheduledDate,
    required this.json,
  });

  factory CacheScheduledVisit.fromModel(ScheduledVisit visit) {
    final dt = visit.scheduledDate ?? DateTime.fromMillisecondsSinceEpoch(0);

    return CacheScheduledVisit(
      scheduledVisitId: visit.id ?? '',
      facilityId: visit.facilityId ?? '',
      status: visit.status ?? '',
      scheduledDate: dt,
      json: jsonEncode(visit.toJson()),
    );
  }

  ScheduledVisit toModel() {
    final map = jsonDecode(json) as Map<String, dynamic>;
    return ScheduledVisit.fromJson(map);
  }
}
