import 'package:isar/isar.dart';

import '../data/nosql/cache_installation_image.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/installation_images/installation_images.dart';
import '../model/mdms/mdms.dart';
import '../utils/envConfig.dart';
import 'app_init_repo.dart' hide envConfig;

class InstallationImagesRepository {
  InstallationImagesRepository(
    this._isar, {
    AppInitRepo? appInitRepo,
  }) : _appInitRepo = appInitRepo ?? AppInitRepo();

  static const String schemaCode = 'common-masters.InstallationImages';

  final AppInitRepo _appInitRepo;
  final Isar _isar;

  Future<List<InstallationImageItem>> fetchMdms(
      {bool cacheOnly = false}) async {
    final docs = await _appInitRepo.searchInstallationImages(
      MdmsRequestModel(
        mdmsCriteria: MdmsCriteriaModel(
          tenantId: envConfig.variables.tenantId,
          schemaCode: schemaCode,
          moduleDetails: [],
        ),
      ),
      cacheOnly: cacheOnly,
    );

    Mdms<InstallationImagesData>? activeDoc;
    for (final doc in docs) {
      if (doc.isActive) {
        activeDoc = doc;
        break;
      }
    }

    if (activeDoc == null) {
      return const <InstallationImageItem>[];
    }

    final data = activeDoc.data;
    return data.installationImage;
  }

  Future<List<CacheInstallationImage>> getCachedImages({
    required String activityFacilityId,
    required String userType,
  }) {
    return _isar.cacheInstallationImages
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .filter()
        .userTypeEqualTo(userType)
        .findAll();
  }

  Future<void> deleteAllCachedImages({
    required String activityFacilityId,
    required String userType,
  }) async {
    await _isar.writeTxn(() async {
      final entries = await _isar.cacheInstallationImages
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .userTypeEqualTo(userType)
          .findAll();

      for (final entry in entries) {
        await _isar.cacheInstallationImages.delete(entry.id);
      }
    });
  }

  Future<void> addCachedImage(CacheInstallationImage entry) async {
    await _isar.writeTxn(() async {
      final existing = await _isar.cacheInstallationImages
          .where()
          .activityFacilityIdEqualTo(entry.activityFacilityId)
          .filter()
          .userTypeEqualTo(entry.userType)
          .and()
          .codeEqualTo(entry.code)
          .and()
          .photoPathEqualTo(entry.photoPath)
          .findFirst();

      if (existing != null) {
        existing.updatedAt = DateTime.now();
        existing.latitude = entry.latitude;
        existing.longitude = entry.longitude;
        await _isar.cacheInstallationImages.put(existing);
      } else {
        await _isar.cacheInstallationImages.put(entry);
      }
    });
  }
}
