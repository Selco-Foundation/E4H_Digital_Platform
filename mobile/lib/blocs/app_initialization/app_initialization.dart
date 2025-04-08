import 'dart:async';

import 'package:collection/collection.dart';
import 'package:selco/models/app_config/mdms_request.dart';
import 'package:selco/models/app_config/mdms_response.dart';
import 'package:selco/models/service_registry/service_registry_model.dart';
import 'package:selco/models/tenant_boundary/data_model.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:recase/recase.dart';
import 'package:selco/data/repositories/app_init_repo.dart';

part 'app_initialization.freezed.dart';

class AppInitialization extends Bloc<InitEvent, InitState> {
  AppInitialization() : super(const InitState.uninitialized()) {
    on<_AppLaunchEvent>(doInitialization);
  }

  //deal with AppInitEvent, fetches appConfig
  FutureOr<void> doInitialization(
    _AppLaunchEvent event,
    Emitter<InitState> emit,
  ) async {
    final appInitRepo = AppInitRepo();
    try {
      // Attempt to fetch app configuration
      final appConfig = await appInitRepo.searchAppConfiguration(
        const MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: 'mz',
            moduleDetails: [
              MdmsModuleDetailsModel(
                moduleName: 'HCM-FIELD-APP-CONFIG',
                masterDetails: [MdmsMasterDetailsModel('appConfig')],
              ),
              MdmsModuleDetailsModel(
                moduleName: 'module-version',
                masterDetails: [MdmsMasterDetailsModel('ROW_VERSIONS')],
              ),
            ],
          ),
        ),
      );

      // Attempt to fetch service registry
      final result = await appInitRepo.searchServiceRegistry(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: envConfig.variables.tenantId,
            moduleDetails: [
              const MdmsModuleDetailsModel(
                moduleName: 'HCM-SERVICE-REGISTRY',
                masterDetails: [MdmsMasterDetailsModel('serviceRegistry')],
              ),
            ],
          ),
        ).toJson(),
      );

      final serviceRegistryList =
          result.serviceRegistryWrapper?.serviceRegistry ?? [];

      // Emit initialized state with fetched data
      emit(
        InitState.initialized(
          appConfig: appConfig,
          serviceRegistryModel: serviceRegistryList,
        ),
      );
    } catch (err) {
      // Emit initialized state with default/null values on error
      emit(
        InitState.initialized(
          appConfig: const MdmsResponseModel(appConfig: null),
          serviceRegistryModel: [],
        ),
      );
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
    required List<ServiceRegistry> serviceRegistryModel,
  }) = Initialized;

  //create an actionMap that will make it easier to maintain endpoints for specific use cases and perform requests
  Map<DataModelType, Map<ApiOperation, String>> get entityActionMapping {
    return when(
      uninitialized: () => {},
      initialized: (appConfiguration, serviceRegistryList) =>
          serviceRegistryList
              .map(
                (e) => e.actions?.map((e) {
                  ApiOperation? operation;
                  DataModelType? type;

                  operation = ApiOperation.values.firstWhereOrNull((element) {
                    return e.action.camelCase == element.name;
                  });

                  type = DataModelType.values.firstWhereOrNull((element) {
                    return e.entityName.camelCase == element.name;
                  });

                  if (operation == null || type == null) return null;

                  return ActionPathModel(
                    operation: operation,
                    type: type,
                    path: e.path,
                  );
                }),
              )
              // .where((element) => ,)
              .expand((element) => element!)
              .whereNotNull()
              .fold(<DataModelType, Map<ApiOperation, String>>{}, (o, element) {
        if (o.containsKey(element.type)) {
          o[element.type]?.addEntries([
            MapEntry(element.operation, element.path),
          ]);
        } else {
          o[element.type] = Map.fromEntries([
            MapEntry(element.operation, element.path),
          ]);
        }

        return o;
      }),
    );
  }
}
