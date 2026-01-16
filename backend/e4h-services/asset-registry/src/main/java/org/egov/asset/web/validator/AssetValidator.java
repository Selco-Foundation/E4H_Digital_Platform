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
        log.trace("AssetValidator::validateCreateAsset called");
        String tenantId = request.getAssetDetail().getAsset().getTenantId();
        String assetId = request.getAssetDetail().getAsset().getAssetId();
        log.info("Validating create asset request | tenantId={} assetId={}", tenantId, assetId);
        Map<String, String> errorMap = new HashMap<>();
        validateExistingDuplicates(request.getAssetDetail().getAsset(), errorMap);
        if (!CollectionUtils.isEmpty(errorMap)) {
            log.warn("Validation failed: duplicate asset found | tenantId={} assetId={}", tenantId, assetId);
            throw new CustomException(errorMap);
        }
        Map<String, Object> mdmsData = mdmsUtil.getMDMSData(request.getRequestInfo(), tenantId);
        log.debug("Fetched MDMS data | tenantId={} keysCount={}", tenantId, mdmsData.keySet().size());
        if (!CollectionUtils.isEmpty(mdmsData.keySet())) {
            validateMdmsData(request, errorMap, mdmsData);
        }
        if (!CollectionUtils.isEmpty(errorMap.keySet())) {
            log.warn("Validation failed: MDMS validation errors | tenantId={} assetId={} errorCount={}", 
                    tenantId, assetId, errorMap.size());
            throw new CustomException(errorMap);
        }

        log.info("Asset validation completed successfully | tenantId={} assetId={}", tenantId, assetId);
    }

    private void validateMdmsData(AssetCreateRequest request, Map<String, String> errorMap, Map<String, Object> mdmsData) {
        log.trace("AssetValidator::validateMdmsData called");
        Asset asset = request.getAssetDetail().getAsset();
        log.debug("Validating MDMS data | assetId={} assetType={}", asset.getAssetId(), asset.getAssetTypeID());
        validateAssetType(asset, errorMap, mdmsData.get(AssetConstants.ASSET_TYPE_CODE));
        validateBrandType(asset, errorMap, mdmsData.get(AssetConstants.BRAND_CODE));
        validateWarranty(asset, errorMap, mdmsData.get(AssetConstants.WARRANTY_DURATION));
        validateSystem(asset, errorMap, mdmsData.get(AssetConstants.SYSTEM_CODE));
        validateAssetDetails(asset, errorMap);
        validateFacilityId(asset, errorMap);
        validateActivityFacilityId(request, errorMap);
    }

    private void validateAssetDetails(Asset asset, Map<String, String> errorMap) {
        log.trace("AssetValidator::validateAssetDetails called");
        log.debug("Validating asset details | assetId={} assetTypeID={}", asset.getAssetId(), asset.getAssetTypeID());
        if(asset.getAssetTypeID().equalsIgnoreCase("INVERTOR"))
            validateInverterDetails(AssetConverterUtil.convertMapToInverterDetails(asset.getAssetDetails()), asset.getSystem(), errorMap);
        else if(asset.getAssetTypeID().equalsIgnoreCase("BATTERY"))
            validateBatteryDetails(AssetConverterUtil.convertMapToBatteryDetails(asset.getAssetDetails()), asset.getSystem(), errorMap);
        else if (asset.getAssetTypeID().equalsIgnoreCase("PANEL"))
            validatePanelDetails(AssetConverterUtil.convertMapToPanelDetails(asset.getAssetDetails()), asset.getSystem(), errorMap);
    }

    public static void validateInverterDetails(InverterDetails inverterDetails, String systemType, Map<String, String> errorMaps) {
        log.trace("AssetValidator::validateInverterDetails called");
        log.debug("Validating inverter details | systemType={}", systemType);
        if (inverterDetails == null) {
            log.warn("Inverter details are null");
            errorMaps.put(ErrorConstants.ASSET_INVERTER_DETAILS_EMPTY_CODE, ErrorConstants.ASSET_INVERTER_DETAILS_EMPTY_MSG);
            return;
        }

        if (SYSTEM_DC.equals(systemType)) {
            validateDCSystem(inverterDetails, errorMaps);
        }
        if (SYSTEM_AC_OFF_GRID.equals(systemType)) {
            validateACOffGridSystem(inverterDetails, errorMaps);
        }
    }

    private static void validateDCSystem(InverterDetails inverterDetails, Map<String, String> errorMaps) {
        log.trace("AssetValidator::validateDCSystem called");
        log.debug("Validating DC system inverter details");
        if (inverterDetails.getChargeControllerCurrent() == null) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALIDATION_CODE,
                    ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALIDATION_MSG);
        } else if (inverterDetails.getChargeControllerCurrent() != 20.0) {
            log.debug("Charge controller current validation failed | value={}", inverterDetails.getChargeControllerCurrent());
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALUE_CODE,
                    ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALUE_MSG);
        }

        if (inverterDetails.getChargeControllerVoltage() == null) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_REQUIRED_CODE,
                    ErrorConstants.ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_REQUIRED_MSG);
        } else if (!VALID_CHARGE_CONTROLLER_VOLTAGES.contains(inverterDetails.getChargeControllerVoltage())) {
            log.debug("Charge controller voltage validation failed | value={}", inverterDetails.getChargeControllerVoltage());
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
        log.trace("AssetValidator::validateACOffGridSystem called");
        log.debug("Validating AC Off Grid system inverter details");
        if (inverterDetails.getInverterCapacity() == null) {
            errorMaps.put(ErrorConstants.ASSET_INVERTER_CAPACITY_REQUIRED_CODE,
                    ErrorConstants.ASSET_INVERTER_CAPACITY_REQUIRED_MSG);
        } else {
            try {
                Double capacity = Double.parseDouble(inverterDetails.getInverterCapacity());
                if (!VALID_INVERTER_CAPACITIES.contains(capacity)) {
                    log.debug("Inverter capacity validation failed | value={}", capacity);
                    errorMaps.put(ErrorConstants.ASSET_INVERTER_CAPACITY_INVALID_VALUE_CODE,
                            ErrorConstants.ASSET_INVERTER_CAPACITY_INVALID_VALUE_MSG);
                }
            } catch (NumberFormatException e) {
                log.warn("Inverter capacity format invalid | value={}", inverterDetails.getInverterCapacity());
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
            log.debug("Total capacity validation failed | value={}", inverterDetails.getTotalCapacity());
            errorMaps.put(ErrorConstants.ASSET_TOTAL_CAPACITY_VALUE_CODE,
                    ErrorConstants.ASSET_TOTAL_CAPACITY_VALUE_MSG);
        }
        if (!"kVA".equals(inverterDetails.getTotalCapacityUOM())) {
            errorMaps.put(ErrorConstants.ASSET_TOTAL_CAPACITY_UNIT_CODE,
                    ErrorConstants.ASSET_TOTAL_CAPACITY_UNIT_MSG);
        }
    }

    public static void validateBatteryDetails(BatteryDetails batteryDetails, String systemType, Map<String, String> errorMap) {
        log.trace("AssetValidator::validateBatteryDetails called");
        log.debug("Validating battery details | systemType={}", systemType);
        if (batteryDetails == null) {
            log.warn("Battery details are null");
            errorMap.put(ErrorConstants.ASSET_BATTERY_DETAILS_NULL_CODE, ErrorConstants.ASSET_BATTERY_DETAILS_NULL_MSG);
            return;
        }

        validateCommonBatteryDetails(batteryDetails, errorMap);

        if (SYSTEM_DC.equals(systemType)) {
            validateDCSystemBattery(batteryDetails, errorMap);
        }
        if (SYSTEM_AC_OFF_GRID.equals(systemType)) {
            validateACOffGridSystemBattery(batteryDetails, errorMap);
        }
    }

    private static void validateCommonBatteryDetails(BatteryDetails batteryDetails, Map<String, String> errorMap) {
        log.trace("AssetValidator::validateCommonBatteryDetails called");
        log.debug("Validating common battery details");
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
        log.trace("AssetValidator::validateDCSystemBattery called");
        log.debug("Validating DC system battery details");
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
        log.trace("AssetValidator::validateACOffGridSystemBattery called");
        log.debug("Validating AC Off Grid system battery details");
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
        log.trace("AssetValidator::validatePanelDetails called");
        log.debug("Validating panel details | systemType={}", systemType);
        if (panelDetails == null) {
            log.warn("Panel details are null");
            errorMap.put(ErrorConstants.ASSET_PANEL_DETAILS_NULL_CODE, ErrorConstants.ASSET_PANEL_DETAILS_NULL_MSG);
            return;
        }

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
        log.trace("AssetValidator::validateSystem called");
        log.debug("Validating system | assetId={} system={}", asset.getAssetId(), asset.getSystem());
        if (mdmsSystemData == null || !(mdmsSystemData instanceof List) || ((List<?>) mdmsSystemData).isEmpty()) {
            log.warn("MDMS system data is empty or invalid");
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
        log.trace("AssetValidator::validateWarranty called");
        log.debug("Validating warranty | assetId={} warrantyDuration={}", 
                asset.getAssetId(), asset.getWarrantyDuration());

        if (asset.getWarrantyDuration() == null || asset.getWarrantyDuration() == 0) {
            log.debug("Skipping warranty validation: duration is 0 or null");
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
            log.warn("Error parsing warranty MDMS data | error={}", e.getMessage());
            errorMap.put(ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_CODE, ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_MSG);
        }
    }

    private void validateBrandType(Asset assetRequest, Map<String, String> errorMap, Object mdmsBrandTypeData) {
        log.trace("AssetValidator::validateBrandType called");
        log.debug("Validating brand type | assetId={} brandID={}", assetRequest.getAssetId(), assetRequest.getBrandID());
        if (mdmsBrandTypeData == null || !(mdmsBrandTypeData instanceof List) || ((List<?>) mdmsBrandTypeData).isEmpty()) {
            log.warn("MDMS brand type data is empty or invalid");
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
            log.warn("Error parsing brand MDMS data | error={}", e.getMessage());
            errorMap.put(ErrorConstants.ASSET_BRAND_MDMS_DATA_CODE, ErrorConstants.ASSET_BRAND_MDMS_DATA_MSG);
        }
    }

    private void validateAssetType(Asset assetRequest, Map<String, String> errorMap, Object mdmsAssetTypeData) {
        log.trace("AssetValidator::validateAssetType called");
        log.debug("Validating asset type | assetId={} assetTypeID={}", assetRequest.getAssetId(), assetRequest.getAssetTypeID());
        if (mdmsAssetTypeData == null || !(mdmsAssetTypeData instanceof List) || ((List<?>) mdmsAssetTypeData).isEmpty()) {
            log.warn("MDMS asset type data is empty or invalid");
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
            log.warn("Error parsing asset type MDMS data | error={}", e.getMessage());
            errorMap.put(ErrorConstants.ASSET_TYPE_MDMS_DATA_CODE, ErrorConstants.ASSET_TYPE_MDMS_DATA_MSG);
        }
    }

    private void validateExistingDuplicates(Asset asset, Map<String, String> errorMap) {
        log.trace("AssetValidator::validateExistingDuplicates called");
        log.debug("Checking for duplicate asset | assetId={} tenantId={}", asset.getAssetId(), asset.getTenantId());
        List<Asset> assets = assetService.searchAssets(asset, 1, 0);
        if(!assets.isEmpty()) {
            log.warn("Duplicate asset found | assetId={} tenantId={}", asset.getAssetId(), asset.getTenantId());
            errorMap.put(ErrorConstants.ASSET_DUPLICATE_VALIDATION_CODE, ErrorConstants.ASSET_DUPLICATE_VALIDATION_MSG);
        }
    }

    private void validateFacilityId(Asset asset, Map<String,String> errorMap){
        log.trace("AssetValidator::validateFacilityId called");
        log.debug("Validating facility | assetId={} facilityId={}", asset.getAssetId(), asset.getFacilityID());
        List<Object> facilities = facilityUtil.searchFacility(asset.getTenantId(), asset.getFacilityID());
        if(facilities.isEmpty()) {
            log.warn("Facility not found | assetId={} facilityId={} tenantId={}", 
                    asset.getAssetId(), asset.getFacilityID(), asset.getTenantId());
            errorMap.put(ErrorConstants.ASSET_FACILITY_ID_VALIDATION_CODE, ErrorConstants.ASSET_FACILITY_ID_VALIDATION_MSG);
        }
    }

    private void validateActivityFacilityId(AssetCreateRequest request, Map<String,String> errorMap){
        log.trace("AssetValidator::validateActivityFacilityId called");
        Asset asset = request.getAssetDetail().getAsset();
        log.debug("Validating activity facility | assetId={} activityFacilityID={}", 
                asset.getAssetId(), asset.getActivityFacilityID());
        List<Object> activityList = facilityUtil.getActivityFacilityById(request.getRequestInfo(), asset.getFacilityID(), asset.getTenantId());
        if(activityList.isEmpty()) {
            log.warn("Activity facility not found | assetId={} activityFacilityID={} tenantId={}", 
                    asset.getAssetId(), asset.getActivityFacilityID(), asset.getTenantId());
            errorMap.put(ErrorConstants.ASSET_ACTIVITY_FACILITY_ID_VALIDATION_CODE, ErrorConstants.ASSET_ACTIVITY_FACILITY_ID_VALIDATION_MSG);
        }
    }

    public void validateAsset(String assetID, AssetCreateRequest body) {
        log.trace("AssetValidator::validateAsset called");
        String requestAssetId = body.getAssetDetail().getAsset().getAssetId();
        log.info("Validating asset | pathAssetId={} requestAssetId={}", assetID, requestAssetId);
        Map<String, String> errorMap = new HashMap<>();
        Asset asset = body.getAssetDetail().getAsset();
        
        if (!assetID.equals(asset.getAssetId())) {
            log.warn("Asset ID mismatch | pathAssetId={} requestAssetId={}", assetID, requestAssetId);
            errorMap.put(ErrorConstants.ASSET_ID_MISMATCH_CODE, ErrorConstants.ASSET_ID_MISMATCH_MSG);
        }
        
        log.debug("Checking if asset exists | assetID={} tenantId={}", assetID, asset.getTenantId());
        List<Asset> existingAssets = assetService.searchAssets(
            Asset.builder().assetId(assetID).tenantId(asset.getTenantId()).build(), 1, 0);
        if (existingAssets == null || existingAssets.isEmpty()) {
            log.warn("Asset not found | assetID={} tenantId={}", assetID, asset.getTenantId());
            errorMap.put(ErrorConstants.ASSET_NOT_FOUND_CODE, ErrorConstants.ASSET_NOT_FOUND_MSG);
        }
        if (!errorMap.isEmpty()) {
            log.warn("Asset validation failed | assetID={} errorCount={}", assetID, errorMap.size());
            throw new CustomException(errorMap);
        }
        log.info("Asset validation completed successfully | assetID={}", assetID);
    }
}
