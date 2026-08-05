package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.web.models.Facility;
import org.egov.field_planner.web.models.PlanFacility;
import org.egov.field_planner.web.models.PlanFacilityIncludeItem;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentFacilityMetadataService {

    private final FieldPlannerFacilityService fieldPlannerFacilityService;
    private final FieldPlannerServiceUtil fieldPlannerServiceUtil;

    public void enrichIncludeItem(PlanFacilityIncludeItem item) {
        if (item == null || StringUtils.isBlank(item.getFacilityId())) {
            return;
        }
        Facility facility = fieldPlannerFacilityService.getFacilityById(item.getFacilityId());
        if (facility == null) {
            log.warn("Could not enrich assessment include metadata — facility not found: {}", item.getFacilityId());
            return;
        }
        applyFacilityMetadata(item, facility);
    }

    public void enrichDisplayFields(PlanFacility facility) {
        if (facility == null || StringUtils.isBlank(facility.getFacilityId())) {
            return;
        }
        if (hasDistrictAndBlock(facility.getDistrict(), facility.getBlock())
                && StringUtils.isNotBlank(facility.getFacilityName())) {
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
            facility.setFacilityCategory(master.getFacilityCategory());
        }
        if (StringUtils.isBlank(facility.getFacilityType())) {
            facility.setFacilityType(master.getFacilityType());
        }
        if (!hasDistrictAndBlock(facility.getDistrict(), facility.getBlock())) {
            DistrictBlock districtBlock = resolveDistrictBlock(master);
            if (StringUtils.isBlank(facility.getDistrict())) {
                facility.setDistrict(districtBlock.district());
            }
            if (StringUtils.isBlank(facility.getBlock())) {
                facility.setBlock(districtBlock.block());
            }
        }
    }

    private void applyFacilityMetadata(PlanFacilityIncludeItem item, Facility facility) {
        if (StringUtils.isBlank(item.getFacilityName())) {
            item.setFacilityName(facility.getFacilityName());
        }
        if (StringUtils.isBlank(item.getFacilityCategory())) {
            item.setFacilityCategory(facility.getFacilityCategory());
        }
        if (StringUtils.isBlank(item.getFacilityType())) {
            item.setFacilityType(facility.getFacilityType());
        }
        if (!hasDistrictAndBlock(item.getDistrict(), item.getBlock())) {
            DistrictBlock districtBlock = resolveDistrictBlock(facility);
            if (StringUtils.isBlank(item.getDistrict())) {
                item.setDistrict(districtBlock.district());
            }
            if (StringUtils.isBlank(item.getBlock())) {
                item.setBlock(districtBlock.block());
            }
        }
    }

    private DistrictBlock resolveDistrictBlock(Facility facility) {
        DistrictBlock fromAddress = readAddressDistrictBlock(facility.getFacilityDetails());
        if (hasDistrictAndBlock(fromAddress.district(), fromAddress.block())) {
            return fromAddress;
        }
        return parseBoundaryCode(facility.getBoundaryCode());
    }

    @SuppressWarnings("unchecked")
    private DistrictBlock readAddressDistrictBlock(Map<String, Object> facilityDetails) {
        if (facilityDetails == null || facilityDetails.isEmpty()) {
            return DistrictBlock.empty();
        }
        Object addressObj = facilityDetails.get("address");
        if (!(addressObj instanceof Map<?, ?> address)) {
            return DistrictBlock.empty();
        }
        String district = stringValue((Map<String, Object>) address, "district");
        String block = stringValue((Map<String, Object>) address, "block");
        return new DistrictBlock(district, block);
    }

    private DistrictBlock parseBoundaryCode(String boundaryCode) {
        if (StringUtils.isBlank(boundaryCode)) {
            return DistrictBlock.empty();
        }
        String[] parts = boundaryCode.split("_");
        if (parts.length >= 4 && "India".equalsIgnoreCase(parts[0])) {
            return new DistrictBlock(
                    fieldPlannerServiceUtil.boundaryCodeToName(parts[2]),
                    fieldPlannerServiceUtil.boundaryCodeToName(parts[3])
            );
        }
        if (parts.length >= 2) {
            return new DistrictBlock(
                    fieldPlannerServiceUtil.boundaryCodeToName(parts[parts.length - 2]),
                    fieldPlannerServiceUtil.boundaryCodeToName(parts[parts.length - 1])
            );
        }
        return DistrictBlock.empty();
    }

    private String stringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private boolean hasDistrictAndBlock(String district, String block) {
        return StringUtils.isNotBlank(district) && StringUtils.isNotBlank(block);
    }

    private record DistrictBlock(String district, String block) {
        static DistrictBlock empty() {
            return new DistrictBlock(null, null);
        }
    }
}
