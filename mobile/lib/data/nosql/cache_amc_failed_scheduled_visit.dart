import 'package:isar_community/isar.dart';

part 'cache_amc_failed_scheduled_visit.g.dart';

@Collection()
class CacheAmcFailedScheduledVisit {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: false)
  late String scheduledVisitId;

  CacheAmcFailedScheduledVisit({
    required this.scheduledVisitId,
  });
}
