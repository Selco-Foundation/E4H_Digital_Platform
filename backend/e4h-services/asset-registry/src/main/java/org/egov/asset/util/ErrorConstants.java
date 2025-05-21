package org.egov.asset.util;

import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Component;

@Component
public class ErrorConstants {
    public static final String ASSET_BRAND_MDMS_DATA_CODE = "ERR_ASSET_BRAND_MDMS_DATA";
    public static final String ASSET_BRAND_MDMS_DATA_MSG = "Mdms data for brand does not exist or invalid in asset-registry module";

    public static final String ASSET_BRAND_ID_VALIDATION_CODE = "ERR_ASSET_BRAND_ID_VALIDATION";
    public static final String ASSET_BRAND_ID_VALIDATION_MSG = "Provided brandId does not matches with the mdms data.";

    public static final String ASSET_TYPE_MDMS_DATA_CODE = "ERR_ASSET_TYPE_MDMS_DATA";
    public static final String ASSET_TYPE_MDMS_DATA_MSG = "Mdms data for asset type does not exist or invalid in asset-registry module";

    public static final String ASSET_TYPE_ID_VALIDATION_CODE = "ERR_ASSET_TYPE_ID_VALIDATION";
    public static final String ASSET_TYPE_ID_VALIDATION_MSG = "Provided assetTypeId does not matches with the mdms data.";

    public static final String ASSET_FACILITY_ID_VALIDATION_CODE = "ERR_ASSET_FACILITY_ID_VALIDATION";
    public static final String ASSET_FACILITY_ID_VALIDATION_MSG = "Provided facilityId does not exist for given tenantId.";

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


}
