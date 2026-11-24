import 'package:flutter_test/flutter_test.dart';
import 'package:intl/intl.dart';

import '../../lib/data/nosql/cache_scheduled_visit.dart';
import '../../lib/model/scheduled_visit/scheduled_visit.dart';

void main() {
  ScheduledVisit visit({String status = 'SCHEDULED'}) =>
      ScheduledVisit.fromJson({
        'id': '513fb870-74d7-43f5-89bd-ed39e241612d',
        'facilityId': 'FAC/2025/3179',
        'status': status,
        'scheduledDate': 1763337600000,
        'auditDetails': {'lastModifiedTime': 1787725197034},
        'processInstances': [
          {
            'auditDetails': {'lastModifiedTime': 1787725196908}
          },
          {
            'auditDetails': {'lastModifiedTime': 1763337600000}
          },
        ],
      });

  for (final status in [
    'SCHEDULED',
    'DRAFT',
    'SUBMITTED',
    'REJECTED',
    'APPROVED'
  ]) {
    test('$status uses the first workflow timestamp, including after caching',
        () {
      final record = visit(status: status);
      expect(record.displayDate?.millisecondsSinceEpoch, 1787725196908);
      expect(DateFormat('dd/MM/yy').format(record.displayDate!), '26/08/26');
      final cached = CacheScheduledVisit.fromModel(record).toModel();
      expect(cached.displayDate, record.displayDate);
      expect(cached.scheduledDate?.millisecondsSinceEpoch, 1763337600000);
    });
  }

  test('missing workflow date does not fall back to another event', () {
    for (final instances in [
      <Map<String, dynamic>>[],
      <Map<String, dynamic>>[{}],
      <Map<String, dynamic>>[
        {'auditDetails': {}},
        {
          'auditDetails': {'lastModifiedTime': 1787725196908}
        },
      ],
    ]) {
      final record = ScheduledVisit.fromJson({
        'scheduledDate': 1763337600000,
        'auditDetails': {'lastModifiedTime': 1787725197034},
        'processInstances': instances,
      });
      expect(record.displayDate, isNull);
    }
  });
}
