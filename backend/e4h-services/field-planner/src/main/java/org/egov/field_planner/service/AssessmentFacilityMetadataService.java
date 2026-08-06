package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.util.AssessmentBoundaryHelper;
import org.egov.field_planner.util.AssessmentBoundaryHelper.DistrictBlockCodes;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.web.models.Facility;
import org.egov.field_planner.web.models.PlanFacility;
import org.egov.field_planner.web.models.PlanFacilityFilters;
import org.egov.field_planner.web.models.PlanFacilityIncludeItem;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentFacilityMetadataService {

    private final FieldPlannerFacilityService fieldPlannerFacilityService;
    private final FieldPlannerServiceUtil fieldPlannerServiceUtil;
    private final AssessmentFacilityMdmsService facilityMdmsService;

    public void enrichIncludeItem(PlanFacilityIncludeItem item, RequestInfo requestInfo, String tenantId) {
        if (item == null || StringUtils.isBlank(item.getFacilityId())) {
            return;
        }
        Facility facility = fieldPlannerFacilityService.getFacilityById(item.getFacilityId());
        if (facility == null) {
            log.warn("Could not enrich assessment include metadata — facility not found: {}", item.getFacilityId());
            return;
        }
        applyFacilityMetadata(item, facility, requestInfo, tenantId);
    }

    public void enrichDisplayFields(PlanFacility facility, RequestInfo requestInfo, String tenantId) {
        if (facility == null) {
            return;
        }
        String resolvedTenantId = resolveTenantId(requestInfo, tenantId);
        if (StringUtils.isNotBlank(facility.getDistrict())) {
            facility.setDistrict(AssessmentBoundaryHelper.toDistrictDisplayName(
                    facility.getDistrict(), fieldPlannerServiceUtil));
        }
        if (StringUtils.isNotBlank(facility.getBlock())) {
            facility.setBlock(AssessmentBoundaryHelper.toBlockDisplayName(
                    facility.getBlock(), fieldPlannerServiceUtil));
        }
        if (StringUtils.isNotBlank(facility.getFacilityCategory())) {
            facility.setFacilityCategory(facilityMdmsService.toCategoryDisplayName(
                    requestInfo, resolvedTenantId, facility.getFacilityCategory()));
        }
        if (StringUtils.isNotBlank(facility.getFacilityType())) {
            facility.setFacilityType(facilityMdmsService.toTypeDisplayName(
                    requestInfo, resolvedTenantId, facility.getFacilityType()));
        }
        if (StringUtils.isNotBlank(facility.getDistrict())
                && StringUtils.isNotBlank(facility.getFacilityName())) {
            return;
        }
        if (StringUtils.isBlank(facility.getFacilityId())) {
            return;
        }
        Facility master = fieldPlannerFacilityService.getFacilityById(facility.getFacilityId());
        if (master == null) {
            return;
        }
        if (StringUtils.isBlank(facility.getFacilityName())) {
            facility.setFacilityName(master.getFacilityName());
        }
        if (StringUtils.isBlank(facility.getFacilityCategory())) {
            String categoryCode = facilityMdmsService.resolveCategoryCode(
                    requestInfo, resolvedTenantId, master.getFacilityCategory());
            facility.setFacilityCategory(facilityMdmsService.toCategoryDisplayName(
                    requestInfo, resolvedTenantId, categoryCode));
        }
        if (StringUtils.isBlank(facility.getFacilityType())) {
            String typeCode = facilityMdmsService.resolveTypeCode(
                    requestInfo, resolvedTenantId, master.getFacilityType());
            facility.setFacilityType(facilityMdmsService.toTypeDisplayName(
                    requestInfo, resolvedTenantId, typeCode));
        }
        if (StringUtils.isBlank(facility.getDistrict()) || StringUtils.isBlank(facility.getBlock())) {
            DistrictBlockCodes codes = AssessmentBoundaryHelper.extractBoundaryCodes(master.getBoundaryCode());
            if (StringUtils.isBlank(facility.getDistrict()) && StringUtils.isNotBlank(codes.districtCode())) {
                facility.setDistrict(AssessmentBoundaryHelper.toDistrictDisplayName(
                        codes.districtCode(), fieldPlannerServiceUtil));
            }
            if (StringUtils.isBlank(facility.getBlock()) && StringUtils.isNotBlank(codes.blockCode())) {
                facility.setBlock(AssessmentBoundaryHelper.toBlockDisplayName(
                        codes.blockCode(), fieldPlannerServiceUtil));
            }
        }
    }

    public PlanFacilityFilters expandFilters(PlanFacilityFilters filters, RequestInfo requestInfo, String tenantId) {
        if (filters == null) {
            return null;
        }
        String resolvedTenantId = resolveTenantId(requestInfo, tenantId);
        return PlanFacilityFilters.builder()
                .districts(AssessmentBoundaryHelper.expandDistrictFilterValues(
                        filters.getResolvedDistricts(), fieldPlannerServiceUtil))
                .blocks(AssessmentBoundaryHelper.expandBlockFilterValues(
                        filters.getResolvedBlocks(), fieldPlannerServiceUtil))
                .facilityCategories(facilityMdmsService.expandCategoryFilterValues(
                        requestInfo, resolvedTenantId, filters.getResolvedFacilityCategories()))
                .facilityTypes(facilityMdmsService.expandTypeFilterValues(
                        requestInfo, resolvedTenantId, filters.getResolvedFacilityTypes()))
                .phoneStatuses(filters.getPhoneStatuses())
                .fieldStatuses(filters.getFieldStatuses())
                .overallStatuses(filters.getOverallStatuses())
                .build();
    }

    private void applyFacilityMetadata(PlanFacilityIncludeItem item, Facility facility,
                                       RequestInfo requestInfo, String tenantId) {
        String resolvedTenantId = resolveTenantId(requestInfo, tenantId);
        if (StringUtils.isBlank(item.getFacilityName())) {
            item.setFacilityName(facility.getFacilityName());
        }

        String categoryRaw = StringUtils.defaultIfBlank(item.getFacilityCategory(), facility.getFacilityCategory());
        item.setFacilityCategory(facilityMdmsService.resolveCategoryCode(
                requestInfo, resolvedTenantId, categoryRaw));

        String typeRaw = StringUtils.defaultIfBlank(item.getFacilityType(), facility.getFacilityType());
        item.setFacilityType(facilityMdmsService.resolveTypeCode(
                requestInfo, resolvedTenantId, typeRaw));

        DistrictBlockCodes codes = AssessmentBoundaryHelper.extractBoundaryCodes(facility.getBoundaryCode());
        if (StringUtils.isNotBlank(codes.districtCode())) {
            item.setDistrict(codes.districtCode());
        } else if (AssessmentBoundaryHelper.isBoundaryCode(item.getDistrict())) {
            item.setDistrict(item.getDistrict());
        }
        if (StringUtils.isNotBlank(codes.blockCode())) {
            item.setBlock(codes.blockCode());
        } else if (AssessmentBoundaryHelper.isBoundaryCode(item.getBlock())) {
            item.setBlock(item.getBlock());
        }
    }

    private String resolveTenantId(RequestInfo requestInfo, String tenantId) {
        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId;
        }
        if (requestInfo != null && requestInfo.getUserInfo() != null
                && StringUtils.isNotBlank(requestInfo.getUserInfo().getTenantId())) {
            return requestInfo.getUserInfo().getTenantId();
        }
        return "in";
    }
}
