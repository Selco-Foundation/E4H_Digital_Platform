package org.egov.asset.web.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.service.AssetService;
import org.egov.asset.util.AssetConstants;
import org.egov.asset.util.ErrorConstants;
import org.egov.asset.util.FacilityUtil;
import org.egov.asset.util.MdmsUtil;
import org.egov.asset.web.models.Asset;
import org.egov.asset.web.models.AssetCreateRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> errorMap = new HashMap<>();
        validateExistingDuplicates(request.getAssetDetail().getAsset(), errorMap);
        if (!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);
        Map<String, Object> mdmsData = mdmsUtil.getMDMSData(request.getRequestInfo(), request.getAssetDetail().getAsset().getTenantId());
        if (!CollectionUtils.isEmpty(mdmsData.keySet())) {
            validateMdmsData(request.getAssetDetail().getAsset(), errorMap, mdmsData);
        }
        if (!CollectionUtils.isEmpty(errorMap.keySet()))
            throw new CustomException(errorMap);
    }

    private void validateMdmsData(Asset asset, Map<String, String> errorMap, Map<String, Object> mdmsData) {
        validateAssetType(asset, errorMap, mdmsData.get(AssetConstants.ASSET_TYPE_C0DE));
        validateBrandType(asset, errorMap, mdmsData.get(AssetConstants.BRAND_CODE));
        validateWarranty(asset, errorMap, mdmsData.get(AssetConstants.WARRANTY_DURATION));
        validateSystem(asset, errorMap, mdmsData.get(AssetConstants.SYSTEM_CODE));
        validateFacilityId(asset, errorMap);
    }

    private void validateSystem(Asset asset, Map<String, String> errorMap, Object mdmsSystemData) {
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
        if (mdmsWarrantyDurationData == null || !(mdmsWarrantyDurationData instanceof List) || ((List<?>) mdmsWarrantyDurationData).isEmpty()) {
            errorMap.put(ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_CODE, ErrorConstants.ASSET_WARRANTY_DURATION_MDMS_DATA_MSG);
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
        List<Asset> assets = assetService.searchAssets(null,null, asset.getAssetTypeID(), asset.getSerialNumber(), asset.getModelNumber(), asset.getBrandID(), 1, 0);
        if(!assets.isEmpty())
            errorMap.put(ErrorConstants.ASSET_DUPLICATE_VALIDATION_CODE, ErrorConstants.ASSET_DUPLICATE_VALIDATION_MSG);
    }

    private void validateFacilityId(Asset asset, Map<String,String> errorMap){
        List<Object> facilities = facilityUtil.searchFacility(asset.getTenantId(), asset.getFacilityID());
        if(facilities.isEmpty())
            errorMap.put(ErrorConstants.ASSET_FACILITY_ID_VALIDATION_CODE, ErrorConstants.ASSET_FACILITY_ID_VALIDATION_MSG);
    }


}
