import 'dart:async';

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
import '../../repositories/app_init_Repo.dart';
import '../../utils/envConfig.dart' as env;
import '../../utils/utils.dart';

part 'app_init.freezed.dart';

class AppInitialization extends Bloc<InitEvent, InitState> {
  AppInitialization() : super(const InitState.uninitialized()) {
    on<_AppLaunchEvent>(doInitialization);
  }

  //deal with AppInitEvent, fetches appConfig
  FutureOr<void> doInitialization(
      _AppLaunchEvent event, Emitter<InitState> emit) async {
    //initialize repo for fetching appConfig
    final appInitRepo = AppInitRepo();
    final envConfigs = env.EnvironmentConfiguration.instance;
    try {
      final appConfig =
          await appInitRepo.searchAppConfiguration(const MdmsRequestModel(
        //send the request in MdmsRequestModel format
        //take the response in ResponseModel format
        mdmsCriteria: MdmsCriteriaModel(
          tenantId: 'mz',
          moduleDetails: [
            MdmsModuleDetailsModel(
              moduleName: 'HCM-FIELD-APP-CONFIG',
              masterDetails: [
                MdmsMasterDetailsModel('appConfig'),
              ],
            ),
            MdmsModuleDetailsModel(
              moduleName: 'module-version',
              masterDetails: [
                MdmsMasterDetailsModel('ROW_VERSIONS'),
              ],
            ),
          ],
        ),
      ));

      final assetCount = await appInitRepo.searchAssetCount(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset.AssetCount",
        moduleDetails: [],
      )));
      final assetCountList = assetCount ?? [];

      final assetType = await appInitRepo.searchAssetType(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset.AssetType2",
        moduleDetails: [],
      )));
      final assetTypeList = assetType ?? [];

      final system = await appInitRepo.searchSystem(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset-registry.SystemSchema",
        moduleDetails: [],
      )));
      final systemList = system ?? [];

      final warranty = await appInitRepo.searchWarranty(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset.WarrantyDuration",
        moduleDetails: [],
      )));
      final warrantyList = warranty ?? [];

      final brand = await appInitRepo.searchBrand(MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset-registry.BrandSchema",
        moduleDetails: [],
      )));
      final brandList = brand ?? [];

      final solutionDesign =
          await appInitRepo.searchSolutionDesign(MdmsRequestModel(
              mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "facility.SolarSolutionDesignType",
        moduleDetails: [],
      )));
      final solutionDesignList = solutionDesign ?? [];

      final solutionDesignBom =
          await appInitRepo.searchSolutionDesignTypeBom(MdmsRequestModel(
              mdmsCriteria: MdmsCriteriaModel(
        tenantId: env.envConfig.variables.tenantId,
        schemaCode: "asset.SolutionDesignTypeBom",
        moduleDetails: [],
      )));
      final solutionDesignBomList = solutionDesignBom ?? [];

      // ---- Fetch FormConfig docs (raw) ----
      final formsDocs = await appInitRepo.searchFormConfigsRaw(
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

      // ---- Transform & store each schema ----
      for (final doc in formsDocs) {
        final transformed = transformSelcoFormMdmsDocToSchema(doc);

        // carry uniqueIdentifier if present
        final uniqueId = doc['uniqueIdentifier']?.toString();
        if (uniqueId != null && uniqueId.isNotEmpty) {
          transformed['uniqueIdentifier'] = uniqueId;
        }

        await appInitRepo.upsertTransformedSchema(transformed);
      }

      //go to the initialized state once configuration details are fetched
      emit(InitState.initialized(
          appConfig: appConfig,
          assetCount: assetCountList,
          assetType: assetTypeList,
          system: systemList,
          warranty: warrantyList,
          brand: brandList,
          solutionDesign: solutionDesignList,
          solutionDesignBom: solutionDesignBomList));
    } catch (err) {
      rethrow;
    }
  }

  Future<void> storeSchema(dynamic mdmsDoc) async {
    final appInitRepo = AppInitRepo();

    if (mdmsDoc is! Map<String, dynamic>) return;

    final data = (mdmsDoc['data'] is Map)
        ? Map<String, dynamic>.from(mdmsDoc['data'] as Map)
        : <String, dynamic>{};

    if (data.isEmpty) return;

    // carry uniqueIdentifier if present
    final uniqueId = mdmsDoc['uniqueIdentifier']?.toString();
    if (uniqueId != null && uniqueId.isNotEmpty) {
      data['uniqueIdentifier'] = uniqueId;
    }

    // ensure name & version for upsert
    data['name'] ??= (data['formName'] ??
        mdmsDoc['schemaCode'] ??
        uniqueId ??
        'Form_${mdmsDoc['id'] ?? DateTime.now().millisecondsSinceEpoch}');
    data['version'] ??= (data['version'] ?? 1);

    await appInitRepo.upsertTransformedSchema(data);
  }
}

@freezed
class InitEvent with _$InitEvent {
  const factory InitEvent.onLaunch() = _AppLaunchEvent;
}

@freezed
class InitState with _$InitState {
  const InitState._();
  const factory InitState.uninitialized() = _Uninitialized;
  const factory InitState.initialized(
          {required MdmsResponseModel appConfig,
          required List<Mdms<AssetCount>> assetCount,
          required List<Mdms<AssetType>> assetType,
          required List<Mdms<System>> system,
          required List<Mdms<Warranty>> warranty,
          required List<Mdms<Brand>> brand,
          required List<Mdms<SolutionDesignType>> solutionDesign,
          required List<Mdms<SolutionDesignTypeBom>> solutionDesignBom}) =
      Initialized;
}
