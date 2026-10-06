import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../model/appconfig/mdmsRequest.dart';
import '../../model/appconfig/mdmsResponse.dart';
import '../../model/asset_count/asset_count.dart';
import '../../model/asset_type/asset_type.dart';
import '../../model/brand/brand.dart';
import '../../model/mdms/mdms.dart';
import '../../model/solution_design_type/solution_design_type.dart';
import '../../model/solution_design_type_bom/solution_design_type_bom.dart';
import '../../model/system/system.dart';
import '../../model/warranty/warranty.dart';
import '../../repositories/app_init_repo.dart';
import '../../utils/app_logger.dart';
import '../../utils/envConfig.dart' as env;
import '../../utils/utils.dart';

part 'app_init.freezed.dart';

const _sessionExpiredMdmsError = 'SESSION_EXPIRED';
const _mdmsFallbackFailureMessage =
    'Failed to load configuration data. Please login and try again.';

class AppInitialization extends Bloc<InitEvent, InitState> {
  AppInitialization() : super(const InitState.uninitialized()) {
    on<_AppLaunchEvent>(_onAppLaunch);
    on<_FetchMdmsEvent>(_onFetchMdms);
  }

  MdmsResponseModel? _cachedAppConfig;

  MdmsResponseModel? get cachedAppConfig => _cachedAppConfig;

  FutureOr<void> _onAppLaunch(
    _AppLaunchEvent event,
    Emitter<InitState> emit,
  ) async {
    final appInitRepo = AppInitRepo();

    try {
      final appConfig = await appInitRepo.searchAppConfiguration();

      _cachedAppConfig = appConfig;
      emit(InitState.defaulted(appConfig: appConfig));
    } catch (e) {
      emit(const InitState.error(
          'Failed to load configuration data. Please try again.'));
    }
  }

  FutureOr<void> _onFetchMdms(
    _FetchMdmsEvent event,
    Emitter<InitState> emit,
  ) async {
    final appInitRepo = AppInitRepo();
    final appConfig = _cachedAppConfig;

    if (appConfig == null) {
      emit(const InitState.error('MDMS requested before appConfig.'));
      return;
    }
    emit(InitState.loadingMdms(appConfig: appConfig));

    try {
      final results = await _fetchMdmsResults(appInitRepo);
      await _emitInitializedState(
        emit: emit,
        appConfig: appConfig,
        appInitRepo: appInitRepo,
        results: results,
      );
    } catch (e) {
      AppLogger.instance.info(e.toString());

      if (isSessionExpiredMessage(e.toString())) {
        emit(const InitState.error(_sessionExpiredMdmsError));
        return;
      }

      try {
        final cachedResults =
            await _fetchMdmsResults(appInitRepo, cacheOnly: true);
        await _emitInitializedState(
          emit: emit,
          appConfig: appConfig,
          appInitRepo: appInitRepo,
          results: cachedResults,
        );
      } catch (cacheError) {
        AppLogger.instance.info(cacheError.toString());
        emit(const InitState.error(_mdmsFallbackFailureMessage));
      }
    }
  }

  Future<List<dynamic>> _fetchMdmsResults(
    AppInitRepo appInitRepo, {
    bool cacheOnly = false,
  }) {
    final tenantId = env.envConfig.variables.tenantId;

    return Future.wait([
      appInitRepo.searchAssetCount(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "asset-registry.AssetCountSchema",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchAssetType(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "asset-registry.AssetTypeSchema",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchSystem(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "asset-registry.SystemSchema",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchWarranty(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "asset-registry.WarrantyDurationSchema",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchBrand(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "asset-registry.BrandSchema",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchSolutionDesign(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "facility.SolarSolutionDesignType",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchSolutionDesignTypeBom(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "common-masters.SolutionDesignTypeBOMForms",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchFormConfigsRaw(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "common-masters.BOMFormSchema",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      appInitRepo.searchInstallationImages(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "common-masters.InstallationImages",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      ),
      _fetchRejectionReasonsForCache(
        appInitRepo,
        tenantId: tenantId,
        cacheOnly: cacheOnly,
      ),
      _fetchRequiredBomFormKeysForCache(
        appInitRepo,
        tenantId: tenantId,
        cacheOnly: cacheOnly,
      ),
    ]);
  }

  Future<List<dynamic>> _fetchRejectionReasonsForCache(
    AppInitRepo appInitRepo, {
    required String tenantId,
    required bool cacheOnly,
  }) async {
    try {
      return await appInitRepo.searchRejectionReasons(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "Installation.RejectionReasons",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      );
    } catch (e) {
      if (isSessionExpiredMessage(e.toString())) {
        rethrow;
      }

      AppLogger.instance.info('Failed to load rejection reasons: $e');
      return <dynamic>[];
    }
  }

  Future<List<dynamic>> _fetchRequiredBomFormKeysForCache(
    AppInitRepo appInitRepo, {
    required String tenantId,
    required bool cacheOnly,
  }) async {
    try {
      return await appInitRepo.searchRequiredBomFormKeys(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: tenantId,
            schemaCode: "common-masters.RequiredBomFormKeys",
            moduleDetails: [],
          ),
        ),
        cacheOnly: cacheOnly,
      );
    } catch (e) {
      if (isSessionExpiredMessage(e.toString())) {
        rethrow;
      }

      AppLogger.instance.info('Failed to load required BOM form keys: $e');
      return <dynamic>[];
    }
  }

  Future<void> _emitInitializedState({
    required Emitter<InitState> emit,
    required MdmsResponseModel appConfig,
    required AppInitRepo appInitRepo,
    required List<dynamic> results,
  }) async {
    final List<Mdms<AssetCountData>> assetCount =
        (results[0] as List<Mdms<AssetCountData>>?) ?? <Mdms<AssetCountData>>[];

    final List<Mdms<AssetTypeData>> assetType =
        (results[1] as List<Mdms<AssetTypeData>>?) ?? <Mdms<AssetTypeData>>[];

    final List<Mdms<SystemData>> system =
        (results[2] as List<Mdms<SystemData>>?) ?? <Mdms<SystemData>>[];

    final List<Mdms<WarrantyData>> warranty =
        (results[3] as List<Mdms<WarrantyData>>?) ?? <Mdms<WarrantyData>>[];

    final List<Mdms<BrandData>> brand =
        (results[4] as List<Mdms<BrandData>>?) ?? <Mdms<BrandData>>[];

    final List<Mdms<SolutionDesignType>> solutionDesign =
        (results[5] as List<Mdms<SolutionDesignType>>?) ??
            <Mdms<SolutionDesignType>>[];

    final List<Mdms<SolutionDesignTypeBom>> solutionDesignBom =
        (results[6] as List<Mdms<SolutionDesignTypeBom>>?) ??
            <Mdms<SolutionDesignTypeBom>>[];

    final List<Map<String, dynamic>> formsDocs =
        (results[7] as List<dynamic>).cast<Map<String, dynamic>>();

    for (final doc in formsDocs) {
      final transformed = transformSelcoFormMdmsDocToSchema(doc);
      final uniqueId = doc['uniqueIdentifier']?.toString();
      if (uniqueId != null && uniqueId.isNotEmpty) {
        transformed['uniqueIdentifier'] = uniqueId;
      }
      await appInitRepo.upsertTransformedSchema(transformed);
    }

    emit(InitState.initialized(
      appConfig: appConfig,
      assetCount: assetCount,
      assetType: assetType,
      system: system,
      warranty: warranty,
      brand: brand,
      solutionDesign: solutionDesign,
      solutionDesignBom: solutionDesignBom,
    ));
  }
}

@freezed
class InitEvent with _$InitEvent {
  const factory InitEvent.onLaunch() = _AppLaunchEvent;
  const factory InitEvent.fetchMdms() = _FetchMdmsEvent;
}

@freezed
class InitState with _$InitState {
  const InitState._();
  const factory InitState.uninitialized() = _Uninitialized;
  const factory InitState.defaulted({
    required MdmsResponseModel appConfig,
  }) = Defaulted;
  const factory InitState.loadingMdms({
    required MdmsResponseModel appConfig,
  }) = LoadingMdms;
  const factory InitState.initialized(
          {required MdmsResponseModel appConfig,
          required List<Mdms<AssetCountData>> assetCount,
          required List<Mdms<AssetTypeData>> assetType,
          required List<Mdms<SystemData>> system,
          required List<Mdms<WarrantyData>> warranty,
          required List<Mdms<BrandData>> brand,
          required List<Mdms<SolutionDesignType>> solutionDesign,
          required List<Mdms<SolutionDesignTypeBom>> solutionDesignBom}) =
      Initialized;
  const factory InitState.error(String message) = Error;
}
