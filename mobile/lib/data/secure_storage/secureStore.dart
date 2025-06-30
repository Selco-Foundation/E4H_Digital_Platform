import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:selco/model/asset_count/asset_count.dart';
import 'package:selco/model/mdms/mdms.dart';

import '../../model/appconfig/mdmsResponse.dart';
import '../../model/asset_type/asset_type.dart';
import '../../model/brand/brand.dart';
import '../../model/localization/localizationModel.dart';
import '../../model/response/responsemodel.dart';
import '../../model/role_actions/role_actions_model.dart';
import '../../model/system/system.dart';
import '../../model/warranty/warranty.dart';

class SecureStore {
  final storage = const FlutterSecureStorage();
  SecureStore();

  Future setLocalizations(
      LocalizationModel localizationList, String locale) async {
    String jsonLocalizationList = json.encode(localizationList.toJson());
    await storage.write(key: locale, value: jsonLocalizationList);
  }

  Future<String?> getLocalizations(String locale) async {
    return await storage.read(key: locale);
  }

  //App configs
  Future setAppConfig(MdmsResponseModel mdmsResponseModel) async {
    String jsonMdmsResponse = json.encode(mdmsResponseModel.toJson());
    await storage.write(key: 'appConfig', value: jsonMdmsResponse);
  }

  Future<String?> getAppConfig() async {
    return await storage.read(key: 'appConfig');
  }

  //Asset count
  Future setAssetCount(List<Mdms<AssetCount>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((assetCount) => assetCount.toJson()))
        .toList();
    await storage.write(key: 'assetCount', value: json.encode(jsonList));
  }

  Future<String?> getAssetCount() async {
    return await storage.read(key: 'assetCount');
  }

  //Asset type
  Future setAssetType(List<Mdms<AssetType>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((assetType) => assetType.toJson()))
        .toList();
    await storage.write(key: 'assetType', value: json.encode(jsonList));
  }

  Future<String?> getAssetType() async {
    return await storage.read(key: 'assetType');
  }

  //System
  Future setSystem(List<Mdms<System>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((system) => system.toJson())).toList();
    await storage.write(key: 'system', value: json.encode(jsonList));
  }

  Future<String?> getSystem() async {
    return await storage.read(key: 'system');
  }

  //Warranty
  Future setWarranty(List<Mdms<Warranty>> list) async {
    final List<Map<String, dynamic>> jsonList = list
        .map((mdms) => mdms.toJson((warranty) => warranty.toJson()))
        .toList();
    await storage.write(key: 'warranty', value: json.encode(jsonList));
  }

  Future<String?> getWarranty() async {
    return await storage.read(key: 'warranty');
  }

  //Warranty
  Future setBrand(List<Mdms<Brand>> list) async {
    final List<Map<String, dynamic>> jsonList =
        list.map((mdms) => mdms.toJson((brand) => brand.toJson())).toList();
    await storage.write(key: 'brand', value: json.encode(jsonList));
  }

  Future<String?> getBrand() async {
    return await storage.read(key: 'brand');
  }

  // Future<void> addToDraftProjects(ProjectWorkflow project) async {
  //   final jsonString = await storage.read(key: "draft_projects");
  //   List<ProjectWorkflow> currentList = [];
  //
  //   if (jsonString != null && jsonString.isNotEmpty) {
  //     try {
  //       final List<dynamic> decoded = json.decode(jsonString);
  //       currentList = decoded
  //           .map((item) =>
  //               ProjectWorkflow.fromJson(item as Map<String, dynamic>))
  //           .toList();
  //     } catch (_) {
  //       currentList = [];
  //     }
  //   }
  //
  //   currentList.removeWhere((p) => p.project.id == project.project.id);
  //   currentList.add(project);
  //
  //   await storage.write(
  //     key: "draft_projects",
  //     value: json.encode(currentList.map((p) => p.toJson()).toList()),
  //   );
  // }
  //
  // Future<List<ProjectWorkflow>> getDraftProjects() async {
  //   final jsonString = await storage.read(key: "draft_projects");
  //   if (jsonString == null || jsonString.isEmpty) return [];
  //
  //   try {
  //     final List<dynamic> decoded = json.decode(jsonString);
  //     return decoded
  //         .map((item) => ProjectWorkflow.fromJson(item as Map<String, dynamic>))
  //         .toList();
  //   } catch (_) {
  //     return [];
  //   }
  // }
  //
  // Future<void> deleteDraftProject(String projectId) async {
  //   final jsonString = await storage.read(key: "draft_projects");
  //   if (jsonString == null || jsonString.isEmpty) return;
  //
  //   try {
  //     final List<dynamic> decoded = json.decode(jsonString);
  //     final List<ProjectModel> currentList = decoded
  //         .map((item) =>
  //             ProjectModelMapper.fromMap(item as Map<String, dynamic>))
  //         .toList();
  //
  //     currentList.removeWhere((p) => p.id == projectId);
  //
  //     if (currentList.isEmpty) {
  //       await storage.write(key: "draft_projects", value: '');
  //     } else {
  //       final updatedList = currentList.map((p) => p.toMap()).toList();
  //       await storage.write(
  //           key: "draft_projects", value: json.encode(updatedList));
  //     }
  //   } catch (_) {
  //     await storage.write(key: "draft_projects", value: '');
  //   }
  // }

  //access token
  Future setAccessToken(String? accessToken) async {
    await storage.write(key: 'accessToken', value: accessToken);
  }

  Future<String?> getAccessToken() async {
    return await storage.read(key: 'accessToken');
  }

  Future deleteAccessToken() async {
    await storage.delete(key: 'accessToken');
  }

  //other auth information
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
      print(err);
      rethrow;
    }
  }

  Future deleteAccessInfo() async {
    await storage.delete(key: 'accessInfo');
  }

  //role actions
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
      print(err);
      rethrow;
    }
  }

  //Individual ID
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
}
