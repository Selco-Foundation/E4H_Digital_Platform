import 'package:isar/isar.dart';

import '../data/nosql/cache_media_upload.dart';
import '../model/document/document.dart';
import '../utils/utils.dart';

class ProjectWorkflowRepository {
  ProjectWorkflowRepository();

  Future<List<Document>> collectWorkflowMediaDocs({
    required Isar isar,
    required String projectId,
    required List<String> types,
  }) async {
    final out = <Document>[];
    for (final type in types) {
      final media = await isar.cacheMediaUploads
          .where()
          .projectIdEqualTo(projectId)
          .filter()
          .assetTypeEqualTo(type)
          .findAll();

      print("[$type] found ${media.length} cached media uploads");
      for (var m in media) {
        print(
            "    media id=${m.id} filePath='${m.filePath}' itemType='${m.itemType}' media id=${m.id} projectId='${m.projectId}'");
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
          geoLocation:
              GeoLocation(latitude: m.latitude, longitude: m.longitude),
        ));
      }
      print("documents - out $out");
    }
    return out;
  }
}
