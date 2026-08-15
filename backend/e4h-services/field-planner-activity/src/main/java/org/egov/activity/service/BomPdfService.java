package org.egov.activity.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.activity.util.BoundaryLocalizationUtil;
import org.egov.activity.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Builds the BOM installation-report PDF data for an activity facility and generates+saves it via
 * pdf-service, returning the resulting fileStoreId. The pdf-service template ("key") is chosen by
 * {@link BomService#generateAndSaveBOMPdfToFilestore} itself, based on the systemType carried on
 * the request - this class is only responsible for resolving the BOM data and the systemType.
 */
@Service
@Slf4j
public class BomPdfService {

    private static final String SYSTEM_TYPE_KEY = "systemType";
    private static final DateTimeFormatter PROJECT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BomService bomService;
    private final BoundaryLocalizationUtil boundaryLocalizationUtil;

    public BomPdfService(BomService bomService, BoundaryLocalizationUtil boundaryLocalizationUtil) {
        this.bomService = bomService;
        this.boundaryLocalizationUtil = boundaryLocalizationUtil;
    }

    /**
     * Generates the BOM installation-report PDF via pdf-service and returns the resulting
     * fileStoreId.
     */
    public String generateInstallationReportPdf(RequestInfo requestInfo, ActivityFacility activityFacility) {
        log.trace("Entering generateInstallationReportPdf method for activityFacilityId: {}", activityFacility.getId());
        String tenantId = activityFacility.getTenantId();

        String systemType = resolveSystemType(activityFacility);
        BillOfMaterial bom = findBomForActivityFacility(requestInfo, activityFacility, tenantId);
        Map<String, Object> bomData = buildBomData(requestInfo, activityFacility, bom);

        GenerateBOMPdfRequest pdfRequest = GenerateBOMPdfRequest.builder()
                .requestInfo(requestInfo)
                .system(systemType)
                .bomData(bomData)
                .build();

        String fileStoreId = bomService.generateAndSaveBOMPdfToFilestore(pdfRequest, tenantId);
        log.info("BOM installation report PDF generated for activityFacilityId: {}, filestoreId: {}",
                activityFacility.getId(), fileStoreId);
        return fileStoreId;
    }

    private String resolveSystemType(ActivityFacility activityFacility) {
        Object systemType = activityFacility.getAdditionalDetails() != null
                ? activityFacility.getAdditionalDetails().get(SYSTEM_TYPE_KEY)
                : null;
        if (systemType == null || String.valueOf(systemType).isBlank()) {
            log.error("systemType missing on activityFacility additionalDetails, activityFacilityId: {}", activityFacility.getId());
            throw new CustomException("BOM_PDF_GENERATION_FAILED",
                    "systemType is missing on activityFacility " + activityFacility.getId() + " - cannot select a BOM report template");
        }
        return String.valueOf(systemType);
    }

    private BillOfMaterial findBomForActivityFacility(RequestInfo requestInfo, ActivityFacility activityFacility, String tenantId) {
        BomSearchCriteria criteria = BomSearchCriteria.builder()
                .activityFacilityId(List.of(activityFacility.getId()))
                .tenantId(tenantId)
                .build();
        BomSearchRequest searchRequest = BomSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(criteria)
                .build();
        List<BillOfMaterial> boms = bomService.searchBillOfMaterials(searchRequest, 1, 0, tenantId, false, null);
        if (boms == null || boms.isEmpty()) {
            log.error("No BOM found for activityFacilityId: {}", activityFacility.getId());
            throw new CustomException("BOM_PDF_GENERATION_FAILED",
                    "No BOM found for activityFacilityId: " + activityFacility.getId());
        }
        return boms.get(0);
    }

    private Map<String, Object> buildBomData(RequestInfo requestInfo, ActivityFacility activityFacility, BillOfMaterial bom) {
        Map<String, Object> data = new HashMap<>();
        if (bom.getData() != null) {
            data.putAll(bom.getData());
        }

        Facility facility = activityFacility.getFacility();
        // facility.getBoundary() is already populated by facility-service on every facility fetch,
        // but with boundary codes (e.g. India_Assam_Darrang), not human-readable names - localize
        // both levels in one call.
        Boundary boundary = (facility != null) ? facility.getBoundary() : null;
        Map<String, String> boundaryNames = localizeBoundary(boundary, requestInfo);

        data.put("health_facility_name", facility != null ? facility.getFacilityName() : null);
        data.put("health_facility_address", facility != null && facility.getAddress() != null
                ? facility.getAddress().getAddressLine1() : null);
        data.put("vendor_name", activityFacility.getStaffVendorName());
        data.put("project_number", facility != null
                ? (facility.getNinId() != null ? facility.getNinId() : facility.getHfrId()) : null);
        data.put("health_facility_type", facility != null ? facility.getFacilityType() : null);
        data.put("project_state", boundary != null
                ? boundaryLocalizationUtil.localizedNameOrCode(boundaryNames, boundary.getState()) : null);
        data.put("project_block", boundary != null
                ? boundaryLocalizationUtil.localizedNameOrCode(boundaryNames, boundary.getBlock()) : null);
        data.put("project_date", LocalDate.now().format(PROJECT_DATE_FORMATTER));
        data.put("po_wo_number", activityFacility.getFieldPlan() != null
                ? activityFacility.getFieldPlan().getPocNumber() : null);

        return data;
    }

    /* State/block names for one boundary, resolved in a single localization call. */
    private Map<String, String> localizeBoundary(Boundary boundary, RequestInfo requestInfo) {
        if (boundary == null) {
            return Map.of();
        }
        List<String> boundaryCodes = Stream.of(boundary.getState(), boundary.getBlock())
                .filter(Objects::nonNull)
                .toList();
        return boundaryLocalizationUtil.localizeBoundaryCodes(boundaryCodes, requestInfo);
    }
}
