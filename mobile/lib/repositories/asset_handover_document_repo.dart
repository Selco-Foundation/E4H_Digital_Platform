import 'package:isar/isar.dart';

import '../data/nosql/cache_asset_handover_document.dart';

class AssetHandoverDocumentInput {
  final String activityFacilityId;
  final String userType;
  final String filePath;
  final String fileName;
  final String fileType;
  final String latitude;
  final String longitude;
  final int? index;

  AssetHandoverDocumentInput({
    required this.activityFacilityId,
    required this.userType,
    required this.filePath,
    required this.fileName,
    required this.fileType,
    required this.latitude,
    required this.longitude,
    this.index,
  });
}

class AssetHandoverDocumentRepository {
  AssetHandoverDocumentRepository(this._isar);

  final Isar _isar;

  String entryIdOf(String activityFacilityId, String filePath) =>
      '$activityFacilityId::$filePath';

  String basename(String pathOrId) {
    final norm = pathOrId.replaceAll('\\', '/');
    final idx = norm.lastIndexOf('/');
    return idx == -1 ? norm : norm.substring(idx + 1);
  }

  Future<List<CacheAssetHandoverDocument>> getCachedFiles({
    required String activityFacilityId,
  }) {
    return _isar.cacheAssetHandoverDocuments
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .sortByCreatedAt()
        .findAll();
  }

  Future<bool> hasCachedFiles({
    required String activityFacilityId,
  }) async {
    final entries = await getCachedFiles(
      activityFacilityId: activityFacilityId,
    );

    return entries.isNotEmpty;
  }

  Future<void> clearProject({
    required String activityFacilityId,
  }) async {
    await _isar.writeTxn(() async {
      final existing = await _isar.cacheAssetHandoverDocuments
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findAll();
      for (final entry in existing) {
        await _isar.cacheAssetHandoverDocuments.delete(entry.id);
      }
    });
  }

  Future<void> replaceAllForProject({
    required String activityFacilityId,
    required List<AssetHandoverDocumentInput> files,
  }) async {
    await _isar.writeTxn(() async {
      final existing = await _isar.cacheAssetHandoverDocuments
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findAll();
      for (final entry in existing) {
        await _isar.cacheAssetHandoverDocuments.delete(entry.id);
      }

      for (final file in files) {
        final entryId = entryIdOf(file.activityFacilityId, file.filePath);
        await _isar.cacheAssetHandoverDocuments.put(
          CacheAssetHandoverDocument(
            activityFacilityId: file.activityFacilityId,
            userType: file.userType,
            entryId: entryId,
            filePath: file.filePath,
            fileName:
                file.fileName.isEmpty ? basename(file.filePath) : file.fileName,
            fileType: file.fileType,
            latitude: file.latitude,
            longitude: file.longitude,
            index: file.index,
          )..createdAt = DateTime.now(),
        );
      }
    });
  }
}
