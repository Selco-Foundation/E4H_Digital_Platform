import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../model/appconfig/mdmsResponse.dart';
import '../../model/asset_count/asset_count.dart';
import '../../model/asset_type/asset_type.dart';
import '../../model/brand/brand.dart';
import '../../model/localization/localizationModel.dart';
import '../../model/mdms/mdms.dart';
import '../../model/response/responsemodel.dart';
import '../../model/role_actions/role_actions_model.dart';
import '../../model/solution_design_type/solution_design_type.dart';
import '../../model/solution_design_type_bom/solution_design_type_bom.dart';
import '../../model/system/system.dart';
import '../../model/warranty/warranty.dart';

class SecureStore {
  final storage = const FlutterSecureStorage(
    aOptions: const AndroidOptions(
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
    await storage.write(key: 'appConfig', value: jsonMdmsResponse);
  }

  Future<String?> getAppConfig() async {
    return await storage.read(key: 'appConfig');
  }

  Future setAssetCount(List<Mdms<AssetCountData>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((assetCount) => assetCount.toJson()))
        .toList();
    await storage.write(key: 'assetCount', value: json.encode(jsonList));
  }

  Future<String?> getAssetCount() async {
    return await storage.read(key: 'assetCount');
  }

  Future setAssetType(List<Mdms<AssetTypeData>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((assetType) => assetType.toJson()))
        .toList();
    await storage.write(key: 'assetType', value: json.encode(jsonList));
  }

  Future<String?> getAssetType() async {
    return await storage.read(key: 'assetType');
  }

  Future setSystem(List<Mdms<SystemData>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((system) => system.toJson())).toList();
    await storage.write(key: 'system', value: json.encode(jsonList));
  }

  Future<String?> getSystem() async {
    return await storage.read(key: 'system');
  }

  Future setWarranty(List<Mdms<WarrantyData>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((warranty) => warranty.toJson()))
        .toList();
    await storage.write(key: 'warranty', value: json.encode(jsonList));
  }

  Future<String?> getWarranty() async {
    return await storage.read(key: 'warranty');
  }

  Future setBrand(List<Mdms<BrandData>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((brand) => brand.toJson())).toList();
    await storage.write(key: 'brand', value: json.encode(jsonList));
  }

  Future<String?> getBrand() async {
    return await storage.read(key: 'brand');
  }

  Future setSolutionDesignType(List<Mdms<SolutionDesignType>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((solutionDesign) => solutionDesign.toJson()))
        .toList();
    await storage.write(key: 'solutionDesign', value: json.encode(jsonList));
  }

  Future<String?> getSolutionDesignType() async {
    return await storage.read(key: 'solutionDesign');
  }

  Future setSolutionDesignTypeBom(
      List<Mdms<SolutionDesignTypeBom>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) =>
            mdms.toJson((solutionDesignBom) => solutionDesignBom.toJson()))
        .toList();
    await storage.write(key: 'solutionDesignBom', value: json.encode(jsonList));
  }

  Future<String?> getSolutionDesignTypeBom() async {
    return await storage.read(key: 'solutionDesignBom');
  }

  Future<void> setFormSchemas(Map<String, dynamic> schemas) async {
    await storage.write(
      key: 'forms_schemas',
      value: json.encode(schemas),
    );
  }

  Future<String?> getFormSchemas() async {
    return await storage.read(key: 'forms_schemas');
  }

  Future setAccessToken(String? accessToken) async {
    await storage.write(key: 'accessToken', value: accessToken);
  }

  Future<String?> getAccessToken() async {
    return await storage.read(key: 'accessToken');
  }

  Future deleteAccessToken() async {
    await storage.delete(key: 'accessToken');
  }

  Future setAccessInfo(ResponseModel accessInfo) async {
    String jsonAccessInfo = json.encode(accessInfo.toJson());
    await storage.write(key: 'accessInfo', value: jsonAccessInfo);
  }

  Future<ResponseModel?> getAccessInfo() async {
    String? jsonAccessInfo = await storage.read(key: 'accessInfo');
    if (jsonAccessInfo == null) return null;
    try {
      return ResponseModel.fromJson(json.decode(jsonAccessInfo));
    } catch (err) {
      rethrow;
    }
  }

  Future deleteAccessInfo() async {
    await storage.delete(key: 'accessInfo');
  }

  Future setRoleActions(RoleActionsWrapperModel actionsWrapper) async {
    String jsonActionsWrapper = json.encode(actionsWrapper.toJson());
    await storage.write(key: 'actionsWrapper', value: jsonActionsWrapper);
  }

  Future<RoleActionsWrapperModel?> getRoleActions() async {
    String? jsonActionsWrapper = await storage.read(key: 'actionsWrapper');

    if (jsonActionsWrapper == null) return null;

    try {
      return RoleActionsWrapperModel.fromJson(json.decode(jsonActionsWrapper));
    } catch (err) {
      rethrow;
    }
  }

  Future setSelectedIndividual(String? id) async {
    await storage.write(key: 'individualId', value: id);
  }

  Future<String?> getSelectedIndividual() async {
    final result = await storage.read(key: 'individualId');
    return result;
  }

  Future deleteSelectedIndividual() async {
    await storage.delete(key: 'individualId');
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
