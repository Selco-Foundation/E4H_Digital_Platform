import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:selco/model/solution_design_type_bom/solution_design_type_bom.dart';

import '../../model/appconfig/mdmsRequest.dart';
import '../../model/appconfig/mdmsResponse.dart';
import '../../model/asset_count/asset_count.dart';
import '../../model/asset_type/asset_type.dart';
import '../../model/brand/brand.dart';
import '../../model/mdms/mdms.dart';
import '../../model/solution_design_type/solution_design_type.dart';
import '../../model/system/system.dart';
import '../../model/warranty/warranty.dart';
import '../../repositories/app_init_repo.dart';
import '../../utils/envConfig.dart' as env;
import '../../utils/utils.dart';

part 'app_init.freezed.dart';

class AppInitialization extends Bloc<InitEvent, InitState> {
  AppInitialization() : super(const InitState.uninitialized()) {
    on<_AppLaunchEvent>(_onAppLaunch);
    on<_FetchMdmsEvent>(_onFetchMdms);
  }

  MdmsResponseModel? _cachedAppConfig;

  FutureOr<void> _onAppLaunch(
    _AppLaunchEvent event,
    Emitter<InitState> emit,
  ) async {
    final appInitRepo = AppInitRepo();

    try {
      final appConfig = await appInitRepo.searchAppConfiguration(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: env.envConfig.variables.tenantId,
            moduleDetails: [
              const MdmsModuleDetailsModel(
                moduleName: 'HCM-FIELD-APP-CONFIG',
                masterDetails: [
                  MdmsMasterDetailsModel('appConfig'),
                ],
              ),
              const MdmsModuleDetailsModel(
                moduleName: 'module-version',
                masterDetails: [
                  MdmsMasterDetailsModel('ROW_VERSIONS'),
                ],
              ),
            ],
          ),
        ),
      );

      _cachedAppConfig = appConfig;
      emit(InitState.defaulted(appConfig: appConfig));
    } catch (e) {
      emit(InitState.error('Failed to load appConfig: $e'));
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
      final assetCountFut = appInitRepo.searchAssetCount(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset-registry.AssetCountSchema",
        moduleDetails: [],
      )));

      final assetTypeFut = appInitRepo.searchAssetType(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset-registry.AssetTypeSchema",
        moduleDetails: [],
      )));

      final systemFut = appInitRepo.searchSystem(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset-registry.SystemSchema",
        moduleDetails: [],
      )));

      final warrantyFut = appInitRepo.searchWarranty(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset-registry.WarrantyDurationSchema",
        moduleDetails: [],
      )));

      final brandFut = appInitRepo.searchBrand(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset-registry.BrandSchema",
        moduleDetails: [],
      )));

      final solutionDesignFut =
          appInitRepo.searchSolutionDesign(MdmsRequestModel(
              mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "facility.SolarSolutionDesignType",
        moduleDetails: [],
      )));

      final solutionDesignBomFut =
          appInitRepo.searchSolutionDesignTypeBom(MdmsRequestModel(
              mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "common-masters.SolutionDesignTypeBOMForms",
        moduleDetails: [],
      )));

      final formsDocsFut = appInitRepo.searchFormConfigsRaw(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: env.envConfig.variables.tenantId,
            moduleDetails: [
              const MdmsModuleDetailsModel(
                moduleName: 'SELCO',
                masterDetails: [MdmsMasterDetailsModel('FormConfig')],
              ),
            ],
          ),
        ),
      );

      final results = await Future.wait([
        assetCountFut,
        assetTypeFut,
        systemFut,
        warrantyFut,
        brandFut,
        solutionDesignFut,
        solutionDesignBomFut,
        formsDocsFut,
      ]);

      final List<Mdms<AssetCountData>> assetCount =
          (results[0] as List<Mdms<AssetCountData>>?) ??
              <Mdms<AssetCountData>>[];

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

      final List<dynamic> formsDocs = results[7] as List<dynamic>;

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
    } catch (e) {
      debugPrint(e.toString());
      emit(InitState.error('Failed to load MDMS: $e'));
    }
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
