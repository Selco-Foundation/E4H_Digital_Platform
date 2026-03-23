package org.egov.asset.util;

import org.springframework.stereotype.Component;

@Component
public class ErrorConstants {


    public static final String MDMS_SERVICE_ERROR_CODE = "ERR_MDMS_SERVICE";
    public static final String MDMS_SERVICE_ERROR_MSG = "Error occurred while fetching the data from mdms";

    public static final String ID_GEN_SERVICE_ERROR_CODE = "ERR_ID_GEN_SERVICE";
    public static final String ID_GEN_SERVICE_ERROR_MSG = "Id generation failed.";

    public static final String FACILITY_SERVICE_ERROR_CODE = "ERR_FACILITY_SERVICE";
    public static final String FACILITY_SERVICE_ERROR_MSG = "Error while fetching the data from facility service.";

    public static final String FACILITY_SEARCH_REQUIRED_PARAMS_CODE = "ERR_FACILITY_SEARCH_PARAMS";
    public static final String FACILITY_SEARCH_REQUIRED_PARAMS_MSG = "TenantId and facility is mandatory for facility search.";

    public static final String KAFKA_PUSH_ERROR_CODE = "ERR_KAFKA_PUSH";
    public static final String KAFKA_PUSH_ERROR_MSG = "Failed to push asset creation event";

    public static final String UPDATE_ASSET_ERROR_CODE = "UPDATE_ASSET";
    public static final String UPDATE_ASSET_ERROR_MSG = "Failed to update asset";

    public static final String ASSET_BRAND_MDMS_DATA_CODE = "ERR_ASSET_BRAND_MDMS_DATA";
    public static final String ASSET_BRAND_MDMS_DATA_MSG = "Mdms data for brand does not exist or invalid in asset-registry module";

    public static final String ASSET_BRAND_ID_VALIDATION_CODE = "ERR_ASSET_BRAND_ID_VALIDATION";
    public static final String ASSET_BRAND_ID_VALIDATION_MSG = "Provided brandId does not matches with the mdms data.";

    public static final String ASSET_TYPE_MDMS_DATA_CODE = "ERR_ASSET_TYPE_MDMS_DATA";
    public static final String ASSET_TYPE_MDMS_DATA_MSG = "Mdms data for asset type does not exist or invalid in asset-registry module";

    public static final String ASSET_TYPE_ID_VALIDATION_CODE = "ERR_ASSET_TYPE_ID_VALIDATION";
    public static final String ASSET_TYPE_ID_VALIDATION_MSG = "Provided assetTypeId does not matches with the mdms data.";

    public static final String ASSET_FACILITY_ID_VALIDATION_CODE = "ERR_ASSET_FACILITY_ID_VALIDATION";
    public static final String ASSET_ACTIVITY_FACILITY_ID_VALIDATION_CODE = "ERR_ASSET_ACTIVITY_FACILITY_ID_VALIDATION";
    public static final String ASSET_FACILITY_ID_VALIDATION_MSG = "Provided facilityId does not exist for given tenantId.";
    public static final String ASSET_ACTIVITY_FACILITY_ID_VALIDATION_MSG = "Provided activity facilityId does not exist for given tenantId.";

    public static final String ASSET_DUPLICATE_VALIDATION_CODE = "ERR_ASSET_DUPLICATE_VALIDATION";
    public static final String ASSET_DUPLICATE_VALIDATION_MSG = "Provided assetTypeId, serialNumber, brandId and modelNumber already exist.";

    public static final String ASSET_WARRANTY_DURATION_MDMS_DATA_CODE = "ERR_ASSET_WARRANTY_DURATION_MDMS_DATA";
    public static final String ASSET_WARRANTY_DURATION_MDMS_DATA_MSG = "Mdms data for warranty duration does not exist or invalid in asset-registry module.";

    public static final String ASSET_WARRANTY_DURATION_VALIDATION_CODE = "ERR_ASSET_WARRANTY_DURATION_VALIDATION";
    public static final String ASSET_WARRANTY_DURATION_VALIDATION_MSG = "Provided warranty duration does not matches with the mdms data.";

    public static final String ASSET_SYSTEM_MDMS_DATA_CODE = "ERR_ASSET_SYSTE_MDMS_DATA";
    public static final String ASSET_SYSTEM_MDMS_DATA_MSG = "Mdms data for system does not exist or invalid in asset-registry module.";

    public static final String ASSET_SYSTEM_VALIDATION_CODE = "ERR_ASSET_SYSTEM_VALIDATION";
    public static final String ASSET_SYSTEM_VALIDATION_MSG = "Provided system data does not matches with the mdms data";

    public static final String ASSET_INVERTER_DETAILS_EMPTY_CODE = "ERR_ASSET_INVERTER_DETAILS_EMPTY";
    public static final String ASSET_INVERTER_DETAILS_EMPTY_MSG = "Inverter details cannot be empty";

    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALIDATION_CODE = "ERR_INVERTER_CHARGE_CONTROLLER_CURRENT_VALIDATION";
    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALIDATION_MSG = "Charge Controller Current is required for DC system";

    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALUE_CODE = "ERR_INVERTER_CHARGE_CONTROLLER_CURRENT_VALUE";
    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_CURRENT_VALUE_MSG = "Charge Controller Current must be 20 for DC system";

    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_REQUIRED_CODE = "ERR_INVERTER_CHARGE_CONTROLLER_VOLTAGE_REQUIRED";
    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_REQUIRED_MSG = "Charge Controller Voltage is required for DC system";

    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_VALUE_CODE = "ERR_INVERTER_CHARGE_CONTROLLER_VOLTAGE_VALUE";
    public static final String ASSET_INVERTER_CHARGE_CONTROLLER_VOLTAGE_VALUE_MSG = "Charge Controller Voltage must be one of the valid value for DC system";

    public static final String ASSET_INVERTER_CURRENT_UNIT_CODE = "ERR_INVERTER_CURRENT_UNIT";
    public static final String ASSET_INVERTER_CURRENT_UNIT_MSG = "Current Unit must be 'A' for DC system";

    public static final String ASSET_INVERTER_VOLTAGE_UNIT_CODE = "ERR_INVERTER_VOLTAGE_UNIT";
    public static final String ASSET_INVERTER_VOLTAGE_UNIT_MSG = "Voltage Unit must be 'vDC' for DC system";

    public static final String ASSET_INVERTER_CAPACITY_REQUIRED_CODE = "ERR_INVERTER_CAPACITY_REQUIRED";
    public static final String ASSET_INVERTER_CAPACITY_REQUIRED_MSG = "Inverter Capacity is required for AC Off Grid system";

    public static final String ASSET_INVERTER_CAPACITY_INVALID_FORMAT_CODE = "ERR_INVERTER_CAPACITY_INVALID_FORMAT";
    public static final String ASSET_INVERTER_CAPACITY_INVALID_FORMAT_MSG = "Inverter Capacity must be a valid number";

    public static final String ASSET_INVERTER_CAPACITY_INVALID_VALUE_CODE = "ERR_INVERTER_CAPACITY_INVALID_VALUE";
    public static final String ASSET_INVERTER_CAPACITY_INVALID_VALUE_MSG = "Inverter Capacity must be one of the valid values";

    public static final String ASSET_INVERTER_CAPACITY_UNIT_CODE = "ERR_INVERTER_CAPACITY_UNIT";
    public static final String ASSET_INVERTER_CAPACITY_UNIT_MSG = "Inverter Capacity Unit must be 'kVA' for AC Off Grid system";

    public static final String ASSET_TOTAL_CAPACITY_REQUIRED_CODE = "ERR_TOTAL_CAPACITY_REQUIRED";
    public static final String ASSET_TOTAL_CAPACITY_REQUIRED_MSG = "Total Capacity is required for AC Off Grid system";

    public static final String ASSET_TOTAL_CAPACITY_VALUE_CODE = "ERR_TOTAL_CAPACITY_VALUE";
    public static final String ASSET_TOTAL_CAPACITY_VALUE_MSG = "Total Capacity must be 1 for AC Off Grid system";

    public static final String ASSET_TOTAL_CAPACITY_UNIT_CODE = "ERR_TOTAL_CAPACITY_UNIT";
    public static final String ASSET_TOTAL_CAPACITY_UNIT_MSG = "Total Capacity UOM must be 'kVA' for AC Off Grid system";

    public static final String ASSET_SYSTEM_TYPE_INVALID_CODE = "ERR_SYSTEM_TYPE_INVALID";
    public static final String ASSET_SYSTEM_TYPE_INVALID_MSG = "Invalid system type. Must be either 'DC' or 'AC Off Grid'";

    // Battery Details Error Codes
    public static final String ASSET_BATTERY_DETAILS_NULL_CODE = "ERR_BATTERY_DETAILS_NULL";
    public static final String ASSET_BATTERY_DETAILS_NULL_MSG = "Battery details cannot be null";

    public static final String ASSET_BATTERY_TOTAL_CAPACITY_REQUIRED_CODE = "ERR_BATTERY_TOTAL_CAPACITY_REQUIRED";
    public static final String ASSET_BATTERY_TOTAL_CAPACITY_REQUIRED_MSG = "Total Capacity is required";

    public static final String ASSET_BATTERY_TOTAL_CAPACITY_INVALID_CODE = "ERR_BATTERY_TOTAL_CAPACITY_INVALID";
    public static final String ASSET_BATTERY_TOTAL_CAPACITY_INVALID_MSG = "Total Capacity must be one of the valid values";

    public static final String ASSET_BATTERY_TOTAL_CAPACITY_UOM_CODE = "ERR_BATTERY_TOTAL_CAPACITY_UOM";
    public static final String ASSET_BATTERY_TOTAL_CAPACITY_UOM_MSG = "Total Capacity UOM must be 'kWh'";

    public static final String ASSET_BATTERY_TYPE_INVALID_CODE = "ERR_BATTERY_TYPE_INVALID";
    public static final String ASSET_BATTERY_TYPE_INVALID_MSG = "Battery Type must be one of the valid types";

    public static final String ASSET_BATTERY_VOLTAGE_UNIT_CODE = "ERR_BATTERY_VOLTAGE_UNIT";
    public static final String ASSET_BATTERY_VOLTAGE_UNIT_MSG = "Voltage Unit must be 'Volts'";

    public static final String ASSET_BATTERY_CAPACITY_UNIT_CODE = "ERR_BATTERY_CAPACITY_UNIT";
    public static final String ASSET_BATTERY_CAPACITY_UNIT_MSG = "Capacity Unit must be 'Ah'";

    // DC System Battery Validation
    public static final String ASSET_BATTERY_VOLTAGE_REQUIRED_DC_CODE = "ERR_BATTERY_VOLTAGE_REQUIRED_DC";
    public static final String ASSET_BATTERY_VOLTAGE_REQUIRED_DC_MSG = "Battery Voltage is required for DC system";

    public static final String ASSET_BATTERY_VOLTAGE_INVALID_DC_CODE = "ERR_BATTERY_VOLTAGE_INVALID_DC";
    public static final String ASSET_BATTERY_VOLTAGE_INVALID_DC_MSG = "Battery Voltage must be one of the valid values for DC system";

    public static final String ASSET_BATTERY_CAPACITY_REQUIRED_DC_CODE = "ERR_BATTERY_CAPACITY_REQUIRED_DC";
    public static final String ASSET_BATTERY_CAPACITY_REQUIRED_DC_MSG = "Battery Capacity is required for DC system";

    public static final String ASSET_BATTERY_CAPACITY_INVALID_DC_CODE = "ERR_BATTERY_CAPACITY_INVALID_DC";
    public static final String ASSET_BATTERY_CAPACITY_INVALID_DC_MSG = "Battery Capacity must be one of the valid values for DC system";

    // AC Off Grid System Battery Validation
    public static final String ASSET_BATTERY_VOLTAGE_REQUIRED_AC_CODE = "ERR_BATTERY_VOLTAGE_REQUIRED_AC";
    public static final String ASSET_BATTERY_VOLTAGE_REQUIRED_AC_MSG = "Battery Voltage is required for AC Off Grid system";

    public static final String ASSET_BATTERY_VOLTAGE_INVALID_AC_CODE = "ERR_BATTERY_VOLTAGE_INVALID_AC";
    public static final String ASSET_BATTERY_VOLTAGE_INVALID_AC_MSG = "Battery Voltage must be one of the valid values for AC Off Grid system";

    public static final String ASSET_BATTERY_CAPACITY_REQUIRED_AC_CODE = "ERR_BATTERY_CAPACITY_REQUIRED_AC";
    public static final String ASSET_BATTERY_CAPACITY_REQUIRED_AC_MSG = "Battery Capacity is required for AC Off Grid system";

    public static final String ASSET_BATTERY_CAPACITY_INVALID_AC_CODE = "ERR_BATTERY_CAPACITY_INVALID_AC";
    public static final String ASSET_BATTERY_CAPACITY_INVALID_AC_MSG = "Battery Capacity must be one of the valid values for AC Off Grid system";

    public static final String ASSET_PANEL_DETAILS_NULL_CODE = "ERR_PANEL_DETAILS_NULL";
    public static final String ASSET_PANEL_DETAILS_NULL_MSG = "Panel details cannot be null";

    public static final String ASSET_PANEL_TOTAL_CAPACITY_REQUIRED_CODE = "ERR_PANEL_TOTAL_CAPACITY_REQUIRED";
    public static final String ASSET_PANEL_TOTAL_CAPACITY_REQUIRED_MSG = "Total Capacity is required";

    public static final String ASSET_PANEL_TOTAL_CAPACITY_UNIT_REQUIRED_CODE = "ERR_PANEL_TOTAL_CAPACITY_UNIT_REQUIRED";
    public static final String ASSET_PANEL_TOTAL_CAPACITY_UNIT_REQUIRED_MSG = "Total Capacity Unit is required";

    public static final String ASSET_PANEL_CAPACITY_REQUIRED_CODE = "ERR_PANEL_CAPACITY_REQUIRED";
    public static final String ASSET_PANEL_CAPACITY_REQUIRED_MSG = "Capacity is required";

    public static final String ASSET_PANEL_CAPACITY_UNIT_REQUIRED_CODE = "ERR_PANEL_CAPACITY_UNIT_REQUIRED";
    public static final String ASSET_PANEL_CAPACITY_UNIT_REQUIRED_MSG = "Capacity Unit is required";

    public static final String ASSET_PANEL_CAPACITY_INVALID_VALUE_CODE = "ERR_PANEL_CAPACITY_INVALID_VALUE";
    public static final String ASSET_PANEL_CAPACITY_INVALID_VALUE_MSG = "Panel Capacity must be one of the valid values";

    public static final String ASSET_ID_MISMATCH_CODE = "ERR_ASSET_ID_MISMATCH";
    public static final String ASSET_ID_MISMATCH_MSG = "Provided assetId does not match the asset's ID";

    public static final String ASSET_NOT_FOUND_CODE = "ERR_ASSET_NOT_FOUND";
    public static final String ASSET_NOT_FOUND_MSG = "Provided asset does not exist or invalid";

    public static final String WF_SERVICE_CALL_ERROR_CODE = "ERR_WF_SERVICE_CALL";
    public static final String WF_SERVICE_CALL_ERROR_MSG = "Error while calling workflow service";

    public static final String WF_TRANSITION_ERROR_CODE = "ERR_WF_TRANSITION";
    public static final String WF_TRANSITION_ERROR_MSG = "Error while processing workflow transition";

    public static final String WF_RESPONSE_PARSING_ERROR_CODE = "ERR_WF_RESPONSE_PARSING";
    public static final String WF_RESPONSE_PARSING_ERROR_MSG = "Error while parsing workflow response";

    public static final String WF_BUSINESS_SERVICE_ERROR_CODE = "ERR_WF_BUSINESS_SERVICE";
    public static final String WF_BUSINESS_SERVICE_ERROR_MSG = "Error while processing business service";

    public static final String USER_SERVICE_CALL_ERROR_CODE = "ERR_USER_SERVICE_CALL";
    public static final String USER_SERVICE_CALL_ERROR_MSG = "Error while calling user service";

    public static final String TENANT_PARSING_ERROR_CODE = "ERR_TENANT_PARSING";
    public static final String TENANT_PARSING_ERROR_MSG = "Error while parsing tenant ID";
}
