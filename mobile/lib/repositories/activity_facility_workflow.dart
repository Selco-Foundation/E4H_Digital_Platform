import 'package:collection/collection.dart';
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

      print("[$type] found ${media.length} cached media uploads");
      for (var m in media) {
        print(
            "    media id=${m.id} filePath='${m.filePath}' itemType='${m.itemType}' media id=${m.id} activityFacilityId='${m.activityFacilityId}'");
      }

      for (final m in media) {
        if (m.filePath.isEmpty) continue;
        final mediaId = await getFilestoreUrl(m.filePath);
        print("mediaId $mediaId");
        out.add(Document(
          documentType: "${m.assetType}-${m.itemType}",
          fileStore: mediaId,
          documentUid:
              "DOC-${m.assetType}-${m.itemType}-${DateTime.now().toUtc().millisecondsSinceEpoch}",
          geoLocation: GeoLocation(
              latitude: m.latitude ?? "", longitude: m.longitude ?? ""),
        ));
      }
      print("documents - out $out");
    }
    return out;
  }

  Future<String> getActivityFacilitySystem({
    required Isar isar,
    required String activityFacilityId,
    required List<Mdms<SolutionDesignType>> solutionDesignList,
    required String? facilitySolutionDesignCode,
  }) async {
    print("facilitySolutionDesignCode $facilitySolutionDesignCode");
    String fallback = SYSTEM_TYPE.DC.name;
    final spec = await isar.cacheSpecifications
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findFirst(); // fast path; indexed query

    final saved = spec?.system.trim();
    print("saved $saved");
    if (saved != null && saved.isNotEmpty) return saved;

    // 2) Compute from MDMS if we have a code
    if (facilitySolutionDesignCode != null &&
        facilitySolutionDesignCode.trim().isNotEmpty) {
      final match = solutionDesignList
          .map((m) => m.data)
          .firstWhereOrNull((sd) => sd.code == facilitySolutionDesignCode);
      final computed = match?.systemCode.trim();
      print("computed $computed");
      if (computed != null && computed.isNotEmpty) return computed;
    }

    return fallback;
  }
}
