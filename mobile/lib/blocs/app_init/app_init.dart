import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:selco/model/warranty/warranty.dart';

import '../../model/appconfig/mdmsRequest.dart';
import '../../model/appconfig/mdmsResponse.dart';
import '../../model/asset_count/asset_count.dart';
import '../../model/asset_type/asset_type.dart';
import '../../model/mdms/mdms.dart';
import '../../model/system/system.dart';
import '../../repositories/app_init_Repo.dart';

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

      final assetCount =
          await appInitRepo.searchAssetCount(const MdmsRequestModel(
              mdmsCriteria: MdmsCriteriaModel(
        tenantId: 'in',
        schemaCode: "asset.AssetCount",
        moduleDetails: [],
      )));
      final assetCountList = assetCount ?? [];

      final assetType =
          await appInitRepo.searchAssetType(const MdmsRequestModel(
              mdmsCriteria: MdmsCriteriaModel(
        tenantId: 'pg',
        schemaCode: "asset.AssetType2",
        moduleDetails: [],
      )));
      final assetTypeList = assetType ?? [];

      final system = await appInitRepo.searchSystem(const MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: 'pg',
        schemaCode: "asset.System",
        moduleDetails: [],
      )));
      final systemList = system ?? [];

      final warranty = await appInitRepo.searchWarranty(const MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
        tenantId: 'pg',
        schemaCode: "asset.WarrantyDuration",
        moduleDetails: [],
      )));
      final warrantyList = warranty ?? [];

      //go to the initialized state once configuration details are fetched
      emit(InitState.initialized(
        appConfig: appConfig,
        assetCount: assetCountList,
        assetType: assetTypeList,
        system: systemList,
        warranty: warrantyList,
      ));
    } catch (err) {
      rethrow;
    }
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
  const factory InitState.initialized({
    required MdmsResponseModel appConfig,
    required List<Mdms<AssetCount>> assetCount,
    required List<Mdms<AssetType>> assetType,
    required List<Mdms<System>> system,
    required List<Mdms<Warranty>> warranty,
  }) = Initialized;
}
