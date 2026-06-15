import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../model/appconfig/mdmsResponse.dart';
import '../../model/asset_count/asset_count.dart';
import '../../model/asset_type/asset_type.dart';
import '../../model/brand/brand.dart';
import '../../model/installation_images/installation_images.dart';
import '../../model/localization/localizationModel.dart';
import '../../model/mdms/mdms.dart';
import '../../model/rejection_reason/rejection_reason.dart';
import '../../model/required_bom_form_keys/required_bom_form_keys.dart';
import '../../model/response/responsemodel.dart';
import '../../model/role_actions/role_actions_model.dart';
import '../../model/solution_design_type/solution_design_type.dart';
import '../../model/solution_design_type_bom/solution_design_type_bom.dart';
import '../../model/system/system.dart';
import '../../model/warranty/warranty.dart';

class _SecureStorageKeys {
  static const String appConfig = 'appConfig';
  static const String assetCount = 'assetCount';
  static const String assetType = 'assetType';
  static const String system = 'system';
  static const String warranty = 'warranty';
  static const String brand = 'brand';
  static const String solutionDesign = 'solutionDesign';
  static const String solutionDesignBom = 'solutionDesignBom';
  static const String formConfigsRaw = 'formConfigsRaw';
  static const String amcFormConfigsRaw = 'amcFormConfigsRaw';
  static const String installationImages = 'installationImages';
  static const String rejectionReasons = 'rejectionReasons';
  static const String requiredBomFormKeys = 'requiredBomFormKeys';
  static const String formSchemas = 'forms_schemas';
  static const String accessToken = 'accessToken';
  static const String accessInfo = 'accessInfo';
  static const String actionsWrapper = 'actionsWrapper';
  static const String individualId = 'individualId';
}

class SecureStore {
  final storage = const FlutterSecureStorage(
    aOptions: AndroidOptions(
      encryptedSharedPreferences: true,
      resetOnError: true,
    ),
  );
  SecureStore();

  Future setLocalizations(
      LocalizationModel localizationList, String locale) async {
    String jsonLocalizationList = json.encode(localizationList.toJson());
    await storage.write(key: locale, value: jsonLocalizationList);
  }

  Future<String?> getLocalizations(String locale) async {
    return await storage.read(key: locale);
  }

  Future setAppConfig(MdmsResponseModel mdmsResponseModel) async {
    String jsonMdmsResponse = json.encode(mdmsResponseModel.toJson());
    await storage.write(
      key: _SecureStorageKeys.appConfig,
      value: jsonMdmsResponse,
    );
  }

  Future<String?> getAppConfig() async {
    return await storage.read(key: _SecureStorageKeys.appConfig);
  }

  Future setAssetCount(List<Mdms<AssetCountData>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((assetCount) => assetCount.toJson()))
        .toList();
    await storage.write(
      key: _SecureStorageKeys.assetCount,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getAssetCount() async {
    return await storage.read(key: _SecureStorageKeys.assetCount);
  }

  Future setAssetType(List<Mdms<AssetTypeData>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((assetType) => assetType.toJson()))
        .toList();
    await storage.write(
      key: _SecureStorageKeys.assetType,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getAssetType() async {
    return await storage.read(key: _SecureStorageKeys.assetType);
  }

  Future setSystem(List<Mdms<SystemData>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((system) => system.toJson())).toList();
    await storage.write(
      key: _SecureStorageKeys.system,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getSystem() async {
    return await storage.read(key: _SecureStorageKeys.system);
  }

  Future setWarranty(List<Mdms<WarrantyData>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((warranty) => warranty.toJson()))
        .toList();
    await storage.write(
      key: _SecureStorageKeys.warranty,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getWarranty() async {
    return await storage.read(key: _SecureStorageKeys.warranty);
  }

  Future setBrand(List<Mdms<BrandData>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((brand) => brand.toJson())).toList();
    await storage.write(
      key: _SecureStorageKeys.brand,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getBrand() async {
    return await storage.read(key: _SecureStorageKeys.brand);
  }

  Future setSolutionDesignType(List<Mdms<SolutionDesignType>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((solutionDesign) => solutionDesign.toJson()))
        .toList();
    await storage.write(
      key: _SecureStorageKeys.solutionDesign,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getSolutionDesignType() async {
    return await storage.read(key: _SecureStorageKeys.solutionDesign);
  }

  Future setSolutionDesignTypeBom(
      List<Mdms<SolutionDesignTypeBom>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) =>
            mdms.toJson((solutionDesignBom) => solutionDesignBom.toJson()))
        .toList();
    await storage.write(
      key: _SecureStorageKeys.solutionDesignBom,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getSolutionDesignTypeBom() async {
    return await storage.read(key: _SecureStorageKeys.solutionDesignBom);
  }

  Future<void> setFormConfigsRaw(List<Map<String, dynamic>> list) async {
    await storage.write(
      key: _SecureStorageKeys.formConfigsRaw,
      value: json.encode(list),
    );
  }

  Future<String?> getFormConfigsRaw() async {
    return await storage.read(key: _SecureStorageKeys.formConfigsRaw);
  }

  Future<void> setAMCFormConfigsRaw(List<Map<String, dynamic>> list) async {
    await storage.write(
      key: _SecureStorageKeys.amcFormConfigsRaw,
      value: json.encode(list),
    );
  }

  Future<String?> getAMCFormConfigsRaw() async {
    return await storage.read(key: _SecureStorageKeys.amcFormConfigsRaw);
  }

  Future<void> setInstallationImages(
      List<Mdms<InstallationImagesData>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((data) => data.toJson())).toList();
    await storage.write(
      key: _SecureStorageKeys.installationImages,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getInstallationImages() async {
    return await storage.read(key: _SecureStorageKeys.installationImages);
  }

  Future<void> setRejectionReasons(List<Mdms<RejectionReasonData>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((data) => data.toJson())).toList();
    await storage.write(
      key: _SecureStorageKeys.rejectionReasons,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getRejectionReasons() async {
    return await storage.read(key: _SecureStorageKeys.rejectionReasons);
  }

  Future<void> setRequiredBomFormKeys(
      List<Mdms<RequiredBomFormKeysData>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((data) => data.toJson())).toList();
    await storage.write(
      key: _SecureStorageKeys.requiredBomFormKeys,
      value: json.encode(jsonList),
    );
  }

  Future<String?> getRequiredBomFormKeys() async {
    return await storage.read(key: _SecureStorageKeys.requiredBomFormKeys);
  }

  Future<void> setFormSchemas(Map<String, dynamic> schemas) async {
    await storage.write(
      key: _SecureStorageKeys.formSchemas,
      value: json.encode(schemas),
    );
  }

  Future<String?> getFormSchemas() async {
    return await storage.read(key: _SecureStorageKeys.formSchemas);
  }

  Future setAccessToken(String? accessToken) async {
    await storage.write(
        key: _SecureStorageKeys.accessToken, value: accessToken);
  }

  Future<String?> getAccessToken() async {
    return await storage.read(key: _SecureStorageKeys.accessToken);
  }

  Future deleteAccessToken() async {
    await storage.delete(key: _SecureStorageKeys.accessToken);
  }

  Future setAccessInfo(ResponseModel accessInfo) async {
    String jsonAccessInfo = json.encode(accessInfo.toJson());
    await storage.write(
      key: _SecureStorageKeys.accessInfo,
      value: jsonAccessInfo,
    );
  }

  Future<ResponseModel?> getAccessInfo() async {
    String? jsonAccessInfo =
        await storage.read(key: _SecureStorageKeys.accessInfo);
    if (jsonAccessInfo == null) return null;
    try {
      return ResponseModel.fromJson(json.decode(jsonAccessInfo));
    } catch (err) {
      rethrow;
    }
  }

  Future deleteAccessInfo() async {
    await storage.delete(key: _SecureStorageKeys.accessInfo);
  }

  Future setRoleActions(RoleActionsWrapperModel actionsWrapper) async {
    String jsonActionsWrapper = json.encode(actionsWrapper.toJson());
    await storage.write(
      key: _SecureStorageKeys.actionsWrapper,
      value: jsonActionsWrapper,
    );
  }

  Future<RoleActionsWrapperModel?> getRoleActions() async {
    String? jsonActionsWrapper =
        await storage.read(key: _SecureStorageKeys.actionsWrapper);

    if (jsonActionsWrapper == null) return null;

    try {
      return RoleActionsWrapperModel.fromJson(json.decode(jsonActionsWrapper));
    } catch (err) {
      rethrow;
    }
  }

  Future setSelectedIndividual(String? id) async {
    await storage.write(key: _SecureStorageKeys.individualId, value: id);
  }

  Future<String?> getSelectedIndividual() async {
    final result = await storage.read(key: _SecureStorageKeys.individualId);
    return result;
  }

  Future deleteSelectedIndividual() async {
    await storage.delete(key: _SecureStorageKeys.individualId);
  }

  String _kRawSchema(String schemaKey) => 'raw_schema_$schemaKey';

  Future<void> setRawSchemaDoc(
      String schemaKey, Map<String, dynamic> raw) async {
    await storage.write(key: _kRawSchema(schemaKey), value: jsonEncode(raw));
  }

  Future<Map<String, dynamic>?> getRawSchemaDoc(String schemaKey) async {
    final s = await storage.read(key: _kRawSchema(schemaKey));
    if (s == null || s.isEmpty) return null;
    final d = jsonDecode(s);
    return d is Map<String, dynamic> ? d : null;
  }
}
