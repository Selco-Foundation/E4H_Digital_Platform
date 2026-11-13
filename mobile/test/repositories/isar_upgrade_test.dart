import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:isar_community/isar.dart';

import '../../lib/data/nosql/cache_assessment_draft.dart';
import '../../lib/data/nosql/cache_media_upload.dart';
import '../../lib/data/nosql/cache_operation_checkpoint.dart';
import '../../lib/data/nosql/cache_submission_job.dart';
import '../support/offline_isar.dart';

const fixturePath = 'test/fixtures/isar_3_1_0/E4H.isar.gz';

Future<Isar> openUpgradeDatabase(String path) => Isar.open(
  [
    CacheAssessmentDraftSchema,
    CacheSubmissionJobSchema,
    CacheMediaUploadSchema,
    CacheOperationCheckpointSchema,
  ],
  directory: path,
  name: 'E4H',
  inspector: false,
);

void main() {
  setUpAll(initializeOfflineIsar);

  test('original Isar data survives opening, writing, and reopening', () async {
    final directory = await Directory.systemTemp.createTemp('isar-upgrade-');
    addTearDown(() => directory.delete(recursive: true));
    await File(
      '${directory.path}/E4H.isar',
    ).writeAsBytes(gzip.decode(await File(fixturePath).readAsBytes()));
    var db = await openUpgradeDatabase(directory.path);
    try {
      final draft =
          await db.cacheAssessmentDrafts
              .filter()
              .draftKeyEqualTo('legacy-draft')
              .findFirst();
      expect(draft?.id, 41);
      expect(draft?.requestJson, '{"offline":true}');
      expect(draft?.status, 'draft');
      final job =
          await db.cacheSubmissionJobs
              .filter()
              .activityFacilityIdEqualTo('activity-1')
              .findFirst();
      expect(job?.id, 42);
      expect(job?.retryCount, 2);
      expect(job?.status, 'queued');
      final media = await db.cacheMediaUploads.get(43);
      expect(media?.filePath, '/fixture/offline/evidence.jpg');
      expect(media?.activityFacilityId, 'activity-1');
      final checkpoint =
          await db.cacheOperationCheckpoints
              .filter()
              .entryKeyEqualTo('activity-1:submit:media:1')
              .findFirst();
      expect(checkpoint?.id, 44);
      expect(checkpoint?.remoteId, 'uploaded-1');
      expect(checkpoint?.status, 'success');
      await db.writeTxn(() async {
        job!.retryCount = 3;
        await db.cacheSubmissionJobs.put(job);
        final nextId = await db.cacheAssessmentDrafts.put(
          CacheAssessmentDraft(
            draftKey: 'new-draft',
            tenantId: 'in',
            assessorId: 'fixture-user',
            phase: 'installation',
            status: 'draft',
            planFacilityId: 'facility-2',
            facilityName: 'New clinic',
            facilityType: 'PHC',
            requestJson: '{}',
          ),
        );
        expect(nextId, greaterThan(41));
      });
      await db.close();
      db = await openUpgradeDatabase(directory.path);
      expect((await db.cacheSubmissionJobs.get(42))?.retryCount, 3);
      expect(await db.cacheAssessmentDrafts.count(), 2);
      expect(
        (await db.cacheAssessmentDrafts.get(41))?.draftKey,
        'legacy-draft',
      );
      expect(
        (await db.cacheMediaUploads.get(43))?.filePath,
        '/fixture/offline/evidence.jpg',
      );
      expect(
        (await db.cacheOperationCheckpoints.get(44))?.remoteId,
        'uploaded-1',
      );
    } finally {
      await db.close();
    }
  });
}
