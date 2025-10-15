package org.egov.asset.web.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.service.AssetService;
import org.egov.asset.util.*;
import org.egov.asset.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.egov.asset.util.AssetConstants.*;

@Service
@Slf4j
public class AssetValidator {

    private final MdmsUtil mdmsUtil;

    private final FacilityUtil facilityUtil;

    private final AssetService assetService;

    private ObjectMapper mapper = new ObjectMapper();


    @Autowired
    public AssetValidator(MdmsUtil mdmsUtil, FacilityUtil facilityUtil, AssetService assetService) {
        this.mdmsUtil = mdmsUtil;
        this.facilityUtil = facilityUtil;
        this.assetService = assetService;
    }

    public void validateCreateAsset(AssetCreateRequest request) {
        log.info("AssetValidator::validateCreateAsset called | tenantId={} assetId={}",
                request.getAssetDetail().getAsset().getTenantId(),
                request.getAssetDetail().getAsset().getAssetId());
        Map<String, String> errorMap = new HashMap<>();
        validateExistingDuplicates(request.getAssetDetail().getAsset(), errorMap);
        if (!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);
        Map<String, Object> mdmsData = mdmsUtil.getMDMSData(request.getRequestInfo(), request.getAssetDetail().getAsset().getTenantId());
        log.debug("Fetched MDMS data keys: {}", mdmsData.keySet());
        if (!CollectionUtils.isEmpty(mdmsData.keySet())) {
            validateMdmsData(request, errorMap, mdmsData);
        }
        if (!CollectionUtils.isEmpty(errorMap.keySet()))
            throw new CustomException(errorMap);

        log.info("AssetValidator::validateCreateAsset completed successfully | assetId={}",
                request.getAssetDetail().getAsset().getAssetId());
    }

    private void validateMdmsData(AssetCreateRequest request, Map<String, String> errorMap, Map<String, Object> mdmsData) {
        Asset asset = request.getAssetDetail().getAsset();
        log.debug("Validating MDMS data for assetId={} assetType={}", asset.getAssetId(), asset.getAssetTypeID());
        validateAssetType(asset, errorMap, mdmsData.get(AssetConstants.ASSET_TYPE_CODE));
        validateBrandType(asset, errorMap, mdmsData.get(AssetConstants.BRAND_CODE));
        validateWarranty(asset, errorMap, mdmsData.get(AssetConstants.WARRANTY_DURATION));
        validateSystem(asset, errorMap, mdmsData.get(AssetConstants.SYSTEM_CODE));
        validateAssetDetails(asset, errorMap);
        validateFacilityId(asset, errorMap);
        validateActivityFacilityId(request, errorMap);
    }

    private void validateAssetDetails(Asset asset, Map<String, String> errorMap) {
        log.debug("Validating asset details for assetTypeID={}", asset.getAssetTypeID());
        if(asset.getAssetTypeID().equalsIgnoreCase("INVERTOR"))
            validateInverterDetails(AssetConverterUtil.convertMapToInverterDetails(asset.getAssetDetails()), asset.getSystem(), errorMap);
        else if(asset.getAssetTypeID().equalsIgnoreCase("BATTERY"))
            validateBatteryDetails(AssetConverterUtil.convertMapToBatteryDetails(asset.getAssetDetails()), asset.getSystem(), errorMap);
        else if (asset.getAssetTypeID().equalsIgnoreCase("PANEL"))
            validatePanelDetails(AssetConverterUtil.convertMapToPanelDetails(asset.getAssetDetails()), asset.getSystem(), errorMap);
    }

    public static void validateInverterDetails(InverterDetails inverterDetails, String systemType, Map<String, String> errorMaps) {
        log.debug("Validating inverter details | systemType={} details={}", systemType, inverterDetails);
        if (inverterDetails == null) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_DETAILS_EMPTY_CODE, ErrorConstants.ASSET_INVERTER_DETAILS_EMPTY_MSG);
            return;
        }

        if (SYSTEM_DC.equals(systemType)) {
            validateDCSystem(inverterDetails, errorMaps);
        } else if (SYSTEM_AC_OFF_GRID.equals(systemType)) {
            validateACOffGridSystem(inverterDetails, errorMaps);
        } else {
            errorMaps.put(ErrorConstants.ASSET_SYSTEM_TYPE_INVALID_CODE, ErrorConstants.ASSET_SYSTEM_TYPE_INVALID_MSG);
        }
    }

    private static void validateDCSystem(InverterDetails inverterDetails, Map<String, String> errorMaps) {
        log.debug("AssetValidator::ValidatingDCsystem");
        if (inverterDetails.getChargeControllerCurrent() == null) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALIDATION_CODE,
                    ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALIDATION_MSG);
        } else if (inverterDetails.getChargeControllerCurrent() != 20.0) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALUE_CODE,
                    ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALUE_MSG);
        }

        if (inverterDetails.getChargeControllerVoltage() == null) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_REQUIRED_CODE,
                    ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_REQUIRED_MSG);
        } else if (!VALID_CHARGE_CONTROLLER_VOLTAGES.contains(inverterDetails.getChargeControllerVoltage())) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_VALUE_CODE,
                    ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_VALUE_MSG);
        }

        if (!"A".equals(inverterDetails.getCurrentUnit())) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CURRENT_UNIT_CODE,
                    ErrorConstants.ASSET_INVERTER_CURRENT_UNIT_MSG);
        }
        if (!"vDC".equals(inverterDetails.getVoltageUnit())) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_VOLTAGE_UNIT_CODE,
                    ErrorConstants.ASSET_INVERTER_VOLTAGE_UNIT_MSG);
        }
    }

    private static void validateACOffGridSystem(InverterDetails inverterDetails, Map<String, String> errorMaps) {
        log.debug("AssetValidator::ValidatingACOffGridsystem");
        if (inverterDetails.getInverterCapacity() == null) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CAPACITY_REQUIRED_CODE,
                    ErrorConstants.ASSET_INVERTER_CAPACITY_REQUIRED_MSG);
        } else {
            try {
                Double capacity = Double.parseDouble(inverterDetails.getInverterCapacity());
                if (!VALID_INVERTER_CAPACITIES.contains(capacity)) {
                    errorMaps.put(ErrorConstants.ASSET_INVERTER_CAPACITY_INVALID_VALUE_CODE,
                            ErrorConstants.ASSET_INVERTER_CAPACITY_INVALID_VALUE_MSG);
                }
            } catch (NumberFormatException e) {
                errorMaps.put(ErrorConstants.ASSET_INVERTER_CAPACITY_INVALID_FORMAT_CODE,
                        ErrorConstants.ASSET_INVERTER_CAPACITY_INVALID_FORMAT_MSG);
            }
        }
        if (!"kVA".equals(inverterDetails.getInverterCapacityUnit())) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CAPACITY_UNIT_CODE,
                    ErrorConstants.ASSET_INVERTER_CAPACITY_UNIT_MSG);
        }
        if (inverterDetails.getTotalCapacity() == null) {
            errorMaps.put(ErrorConstants.ASSET_TOTAL_CAPACITY_REQUIRED_CODE,
                    ErrorConstants.ASSET_TOTAL_CAPACITY_REQUIRED_MSG);
        } else if (inverterDetails.getTotalCapacity() != 1.0) {
            errorMaps.put(ErrorConstants.ASSET_TOTAL_CAPACITY_VALUE_CODE,
                    ErrorConstants.ASSET_TOTAL_CAPACITY_VALUE_MSG);
        }
        if (!"kVA".equals(inverterDetails.getTotalCapacityUOM())) {
            errorMaps.put(ErrorConstants.ASSET_TOTAL_CAPACITY_UNIT_CODE,
                    ErrorConstants.ASSET_TOTAL_CAPACITY_UNIT_MSG);
        }
    }

    public static void validateBatteryDetails(BatteryDetails batteryDetails, String systemType, Map<String, String> errorMap) {
        log.info("AssetValidator::ValidatingBatteryDetails");
        if (batteryDetails == null) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_DETAILS_NULL_CODE, ErrorConstants.ASSET_BATTERY_DETAILS_NULL_MSG);
            return;
        }

        validateCommonBatteryDetails(batteryDetails, errorMap);

        if (SYSTEM_DC.equals(systemType)) {
            validateDCSystemBattery(batteryDetails, errorMap);
        } else if (SYSTEM_AC_OFF_GRID.equals(systemType)) {
            validateACOffGridSystemBattery(batteryDetails, errorMap);
        } else {
            errorMap.put(ErrorConstants.ASSET_SYSTEM_TYPE_INVALID_CODE, ErrorConstants.ASSET_SYSTEM_TYPE_INVALID_MSG);
        }
    }

    private static void validateCommonBatteryDetails(BatteryDetails batteryDetails, Map<String, String> errorMap) {
        log.info("AssetValidator::ValidatingCommonBatteryDetails");
        if (batteryDetails.getTotalCapacity() == null) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_TOTAL_CAPACITY_REQUIRED_CODE,
                    ErrorConstants.ASSET_BATTERY_TOTAL_CAPACITY_REQUIRED_MSG);
        } else if (!VALID_TOTAL_CAPACITIES.contains(batteryDetails.getTotalCapacity())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_TOTAL_CAPACITY_INVALID_CODE,
                    ErrorConstants.ASSET_BATTERY_TOTAL_CAPACITY_INVALID_MSG);
        }

        if (!"kWh".equals(batteryDetails.getTotalCapacityUOM())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_TOTAL_CAPACITY_UOM_CODE,
                    ErrorConstants.ASSET_BATTERY_TOTAL_CAPACITY_UOM_MSG);
        }

        if (batteryDetails.getBatteryType() == null || !VALID_BATTERY_TYPES.contains(batteryDetails.getBatteryType())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_TYPE_INVALID_CODE,
                    ErrorConstants.ASSET_BATTERY_TYPE_INVALID_MSG);
        }

        if (!"Volts".equals(batteryDetails.getVoltageUnit())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_VOLTAGE_UNIT_CODE,
                    ErrorConstants.ASSET_BATTERY_VOLTAGE_UNIT_MSG);
        }

        if (!"Ah".equals(batteryDetails.getCapacityUnit())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_CAPACITY_UNIT_CODE,
                    ErrorConstants.ASSET_BATTERY_CAPACITY_UNIT_MSG);
        }
    }

    private static void validateDCSystemBattery(BatteryDetails batteryDetails, Map<String, String> errorMap) {
        // Validate Battery Voltage for DC system
        log.info("AssetValidator::ValidatingDCSystemBattery");
        if (batteryDetails.getBatteryVoltage() == null) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_VOLTAGE_REQUIRED_DC_CODE,
                    ErrorConstants.ASSET_BATTERY_VOLTAGE_REQUIRED_DC_MSG);
        } else if (!VALID_DC_BATTERY_VOLTAGES.contains(batteryDetails.getBatteryVoltage())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_VOLTAGE_INVALID_DC_CODE,
                    ErrorConstants.ASSET_BATTERY_VOLTAGE_INVALID_DC_MSG);
        }

        // Validate Battery Capacity for DC system
        if (batteryDetails.getBatteryCapacity() == null) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_CAPACITY_REQUIRED_DC_CODE,
                    ErrorConstants.ASSET_BATTERY_CAPACITY_REQUIRED_DC_MSG);
        } else if (!VALID_DC_BATTERY_CAPACITIES.contains(batteryDetails.getBatteryCapacity())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_CAPACITY_INVALID_DC_CODE,
                    ErrorConstants.ASSET_BATTERY_CAPACITY_INVALID_DC_MSG);
        }
    }

    private static void validateACOffGridSystemBattery(BatteryDetails batteryDetails, Map<String, String> errorMap) {
        // Validate Battery Voltage for AC Off Grid system
        log.info("AssetValidator::ValidatingACOffGridSystemBattery");
        if (batteryDetails.getBatteryVoltage() == null) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_VOLTAGE_REQUIRED_AC_CODE,
                    ErrorConstants.ASSET_BATTERY_VOLTAGE_REQUIRED_AC_MSG);
        } else if (!VALID_AC_BATTERY_VOLTAGES.contains(batteryDetails.getBatteryVoltage())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_VOLTAGE_INVALID_AC_CODE,
                    ErrorConstants.ASSET_BATTERY_VOLTAGE_INVALID_AC_MSG);
        }

        // Validate Battery Capacity for AC Off Grid system
        if (batteryDetails.getBatteryCapacity() == null) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_CAPACITY_REQUIRED_AC_CODE,
                    ErrorConstants.ASSET_BATTERY_CAPACITY_REQUIRED_AC_MSG);
        } else if (!VALID_DC_BATTERY_CAPACITIES.contains(batteryDetails.getBatteryCapacity())) {
            errorMap.put(ErrorConstants.ASSET_BATTERY_CAPACITY_INVALID_AC_CODE,
                    ErrorConstants.ASSET_BATTERY_CAPACITY_INVALID_AC_MSG);
        }
    }


    public static void validatePanelDetails(PanelDetails panelDetails, String systemType, Map<String, String> errorMap) {
        log.info("AssetValidator::ValidatingPanelDetails");
        if (panelDetails == null)
            errorMap.put(ErrorConstants.ASSET_PANEL_DETAILS_NULL_CODE, ErrorConstants.ASSET_PANEL_DETAILS_NULL_MSG);

        // Common validations for total capacity
        if (panelDetails.getTotalCapacity() == null)
            errorMap.put(ErrorConstants.ASSET_PANEL_TOTAL_CAPACITY_REQUIRED_CODE, ErrorConstants.ASSET_PANEL_TOTAL_CAPACITY_REQUIRED_MSG);

        if (panelDetails.getTotalCapacityUnit() == null)
            errorMap.put(ErrorConstants.ASSET_PANEL_TOTAL_CAPACITY_UNIT_REQUIRED_CODE, ErrorConstants.ASSET_PANEL_TOTAL_CAPACITY_UNIT_REQUIRED_MSG);

        // System-specific validations
        if (SYSTEM_DC.equals(systemType) || SYSTEM_AC_OFF_GRID.equals(systemType)) {
            // Both DC and AC Off Grid systems require panel capacity
            if (panelDetails.getPanelCapacity() == null) {
                errorMap.put(ErrorConstants.ASSET_PANEL_CAPACITY_REQUIRED_CODE, ErrorConstants.ASSET_PANEL_CAPACITY_REQUIRED_MSG);
            }

            if (!VALID_DC_PANEL_CAPACITIES.contains(panelDetails.getPanelCapacity()) && SYSTEM_DC.equals(systemType)) {
                errorMap.put(ErrorConstants.ASSET_PANEL_CAPACITY_INVALID_VALUE_CODE,
                        ErrorConstants.ASSET_PANEL_CAPACITY_INVALID_VALUE_MSG);
            }

            if (panelDetails.getCapacityUnit() == null)
                errorMap.put(ErrorConstants.ASSET_PANEL_CAPACITY_UNIT_REQUIRED_CODE, ErrorConstants.ASSET_PANEL_CAPACITY_UNIT_REQUIRED_MSG);
        }
    }

    private void validateSystem(Asset asset, Map<String, String> errorMap, Object mdmsSystemData) {
        log.info("AssetValidator::ValidatingSystem");
        if (mdmsSystemData == null || !(mdmsSystemData instanceof List) || ((List<?>) mdmsSystemData).isEmpty()) {
            errorMap.put(ErrorConstants.ASSET_SYSTEM_MDMS_DATA_CODE, ErrorConstants.ASSET_SYSTEM_MDMS_DATA_MSG);
            return;
        }

        LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) ((List<?>) mdmsSystemData).get(0);
        List<?> systemDataList = (List<?>) data.get("System");

        boolean systemDataExists = systemDataList.stream()
                .map(item -> (LinkedHashMap<?, ?>) item)
                .map(map -> map.get("code"))
                .anyMatch(name -> name != null && name.equals(asset.getSystem()));

        if (!systemDataExists) {
            errorMap.put(ErrorConstants.ASSET_SYSTEM_VALIDATION_CODE, ErrorConstants.ASSET_SYSTEM_VALIDATION_MSG);
        }
    }

    private void validateWarranty(Asset asset, Map<String, String> errorMap, Object mdmsWarrantyDurationData) {
        log.info("AssetValidator::ValidatingWarranty");

        // Skip validation if warranty duration is 0
        if (asset.getWarrantyDuration() == null || asset.getWarrantyDuration() == 0) {
            return;
        }

        if (mdmsWarrantyDurationData == null || !(mdmsWarrantyDurationData instanceof List) || ((List<?>) mdmsWarrantyDurationData).isEmpty()) {
//            errorMap.put(ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_CODE, ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_MSG);
            return;
        }

        try{
            LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) ((List<?>) mdmsWarrantyDurationData).get(0);
            List<?> warrantyDurationList = (List<?>) data.get("WarrantyDuration");
            boolean warrantyDurationExist = warrantyDurationList.stream()
                    .map(item -> (LinkedHashMap<?, ?>) item)
                    .anyMatch(map -> {
                        String duration = (String) map.get("duration");
                        String assetTypeCode = (String) map.get("asset_type_code");

                        return duration != null && duration.equals("P"+asset.getWarrantyDuration()+"Y") &&
                                assetTypeCode != null && assetTypeCode.equals(asset.getAssetTypeID());
                    });

            if (!warrantyDurationExist) {
                errorMap.put(ErrorConstants.ASSET_WARRANTY_DURATION_VALIDATION_CODE, ErrorConstants.ASSET_WARRANTY_DURATION_VALIDATION_MSG);
            }
        } catch (ClassCastException | NullPointerException e) {
            errorMap.put(ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_CODE, ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_MSG);
        }
    }

    private void validateBrandType(Asset assetRequest, Map<String, String> errorMap, Object mdmsBrandTypeData) {
        log.info("AssetValidator::ValidatingBrandType");
        if (mdmsBrandTypeData == null || !(mdmsBrandTypeData instanceof List) || ((List<?>) mdmsBrandTypeData).isEmpty()) {
            errorMap.put(ErrorConstants.ASSET_BRAND_MDMS_DATA_CODE, ErrorConstants.ASSET_BRAND_MDMS_DATA_MSG);
            return;
        }

        try {
            LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) ((List<?>) mdmsBrandTypeData).get(0);
            List<?> brandTypeList = (List<?>) data.get("Brand");

            boolean brandTypeExists = brandTypeList.stream()
                    .map(item -> (LinkedHashMap<?, ?>) item)
                    .anyMatch(map -> {
                        String brandId = (String) map.get("code");
                        String assetTypeCode = (String) map.get("asset_type_code");

                        return brandId != null && brandId.equals(assetRequest.getBrandID()) &&
                                assetTypeCode != null && assetTypeCode.equals(assetRequest.getAssetTypeID());
                    });

            if (!brandTypeExists) {
                errorMap.put(ErrorConstants.ASSET_BRAND_ID_VALIDATION_CODE, ErrorConstants.ASSET_BRAND_ID_VALIDATION_MSG);
            }
        } catch (ClassCastException | NullPointerException e) {
            errorMap.put(ErrorConstants.ASSET_BRAND_MDMS_DATA_CODE, ErrorConstants.ASSET_BRAND_MDMS_DATA_MSG);
        }
    }

    private void validateAssetType(Asset assetRequest, Map<String, String> errorMap, Object mdmsAssetTypeData) {
        log.info("AssetValidator::ValidatingAssetType");
        if (mdmsAssetTypeData == null || !(mdmsAssetTypeData instanceof List) || ((List<?>) mdmsAssetTypeData).isEmpty()) {
            errorMap.put(ErrorConstants.ASSET_TYPE_MDMS_DATA_CODE, ErrorConstants.ASSET_TYPE_MDMS_DATA_MSG);
            return;
        }

        try {
            LinkedHashMap<?, ?> data = (LinkedHashMap<?, ?>) ((List<?>) mdmsAssetTypeData).get(0);
            List<?> assetTypeList = (List<?>) data.get("AssetType");

            boolean assetTypeExists = assetTypeList.stream()
                    .map(item -> (LinkedHashMap<?, ?>) item)
                    .map(map -> map.get("code"))
                    .anyMatch(name -> name != null && name.equals(assetRequest.getAssetTypeID()));

            if (!assetTypeExists) {
                errorMap.put(ErrorConstants.ASSET_TYPE_ID_VALIDATION_CODE, ErrorConstants.ASSET_TYPE_ID_VALIDATION_MSG);
            }
        } catch (ClassCastException | NullPointerException e) {
            errorMap.put(ErrorConstants.ASSET_TYPE_MDMS_DATA_CODE, ErrorConstants.ASSET_TYPE_MDMS_DATA_MSG);
        }
    }

    private void validateExistingDuplicates(Asset asset, Map<String, String> errorMap) {
        log.debug("Checking for duplicate asset | assetId={} tenantId={}", asset.getAssetId(), asset.getTenantId());
        List<Asset> assets = assetService.searchAssets(asset,1,0);
        if(!assets.isEmpty())
            errorMap.put(ErrorConstants.ASSET_DUPLICATE_VALIDATION_CODE, ErrorConstants.ASSET_DUPLICATE_VALIDATION_MSG);
    }

    private void validateFacilityId(Asset asset, Map<String,String> errorMap){
        log.debug("Validating facility for assetId={} facilityId={}", asset.getAssetId(), asset.getFacilityID());
        List<Object> facilities = facilityUtil.searchFacility(asset.getTenantId(), asset.getFacilityID());
        if(facilities.isEmpty())
            errorMap.put(ErrorConstants.ASSET_FACILITY_ID_VALIDATION_CODE, ErrorConstants.ASSET_FACILITY_ID_VALIDATION_MSG);
    }

    private void validateActivityFacilityId(AssetCreateRequest request, Map<String,String> errorMap){
        Asset asset = request.getAssetDetail().getAsset();
        log.info("Validating activity facility for assetId={} facilityId={}", asset.getAssetId(), asset.getActivityFacilityID());
        List<Object> activityList = facilityUtil.getActivityFacilityById(request.getRequestInfo(), asset.getFacilityID(), asset.getTenantId());
        if(activityList.isEmpty())
            errorMap.put(ErrorConstants.ASSET_ACTIVITY_FACILITY_ID_VALIDATION_CODE, ErrorConstants.ASSET_ACTIVITY_FACILITY_ID_VALIDATION_MSG);
    }

    public void validateAsset(String assetID, AssetCreateRequest body) {
        log.info("AssetValidator::validateAsset called | pathAssetId={} requestAssetId={}", assetID, body.getAssetDetail().getAsset().getAssetId());
        Map<String, String> errorMap = new HashMap<>();
        Asset asset = body.getAssetDetail().getAsset();
        // Check if assetID matches the asset in the request
        if (!assetID.equals(asset.getAssetId())) {
            errorMap.put(ErrorConstants.ASSET_ID_MISMATCH_CODE, ErrorConstants.ASSET_ID_MISMATCH_MSG);
        }
        // Check if asset exists
        List<Asset> existingAssets = assetService.searchAssets(
            Asset.builder().assetId(assetID).tenantId(asset.getTenantId()).build(), 1, 0);
        if (existingAssets == null || existingAssets.isEmpty()) {
            errorMap.put(ErrorConstants.ASSET_NOT_FOUND_CODE, ErrorConstants.ASSET_NOT_FOUND_MSG);
        }
        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }
        log.info("AssetValidator::validateAsset completed successfully | assetId={}", assetID);
    }
}
