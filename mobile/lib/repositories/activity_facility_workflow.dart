import 'package:collection/collection.dart';
import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_media_upload.dart';
import '../data/nosql/cache_specification.dart';
import '../model/document/document.dart';
import '../model/mdms/mdms.dart';
import '../model/solution_design_type/solution_design_type.dart';
import '../utils/utils.dart';

class ActivityFacilityWorkflowRepository {
  ActivityFacilityWorkflowRepository();

  Future<List<Document>> collectWorkflowMediaDocs({
    required Isar isar,
    required String activityFacilityId,
    required List<String> types,
  }) async {
    final out = <Document>[];
    for (final type in types) {
      final media = await isar.cacheMediaUploads
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .findAll();

      AppLogger.instance
          .info("[$type] found ${media.length} cached media uploads");

      // for (final m in media) {
      //   if (m.filePath.isEmpty) continue;
      //   final mediaId = await getFilestoreUrl(m.filePath);
      //   AppLogger.instance.info(
      //       "mediaId $mediaId filePath-asset-type ${m.filePath} ${m.assetType}");
      //   out.add(Document(
      //     documentType: "${m.assetType}-${m.itemType}",
      //     fileStore: mediaId,
      //     documentUid:
      //         "DOC-${m.assetType}-${m.itemType}-${DateTime.now().toUtc().millisecondsSinceEpoch}",
      //     geoLocation: GeoLocation(
      //         latitude: m.latitude ?? "", longitude: m.longitude ?? ""),
      //   ));
      // }

      final validMedia = media.where((m) => m.filePath.isNotEmpty).toList();
      int batchSize = 15;
      for (var i = 0; i < validMedia.length; i += batchSize) {
        final batch = validMedia.skip(i).take(batchSize).toList();

        final batchFutures = batch.map((m) async {
          final mediaId = await getFilestoreUrl(m.filePath);
          AppLogger.instance.info(
              "mediaId $mediaId filePath-asset-type ${m.filePath} ${m.assetType}");
          return Document(
            documentType: "${m.assetType}-${m.itemType}",
            fileStore: mediaId,
            documentUid:
                "DOC-${m.assetType}-${m.itemType}-${DateTime.now().toUtc().millisecondsSinceEpoch}",
            geoLocation: GeoLocation(
              latitude: m.latitude ?? "",
              longitude: m.longitude ?? "",
            ),
          );
        }).toList();

        final docs = await Future.wait(batchFutures);
        out.addAll(docs);
      }
    }
    return out;
  }

  Future<String> getActivityFacilitySystem({
    required Isar isar,
    required String activityFacilityId,
    required List<Mdms<SolutionDesignType>> solutionDesignList,
    required String? facilitySolutionDesignCode,
  }) async {
    String fallback = SYSTEM_TYPE.DC.name;
    final spec = await isar.cacheSpecifications
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findFirst();

    final saved = spec?.system.trim();
    if (saved != null && saved.isNotEmpty) return saved;

    if (facilitySolutionDesignCode != null &&
        facilitySolutionDesignCode.trim().isNotEmpty) {
      final match = solutionDesignList
          .map((m) => m.data)
          .firstWhereOrNull((sd) => sd.code == facilitySolutionDesignCode);
      final computed = match?.systemCode.trim();
      if (computed != null && computed.isNotEmpty) return computed;
    }

    return fallback;
  }
}
