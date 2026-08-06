package org.egov.amc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.util.BoundaryUtil;
import org.egov.amc.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class AmcVisitReportPdfService {

    private static final String AMC_PDF_KEY = "amc-report";
    private static final String IMG1_DOCUMENT_TYPE = "image";
    private static final String INSTALLATION_IMAGE_1_DOCUMENT_TYPE = "INSTALLATION_IMAGE-1";
    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private final ServiceRequestRepository requestRepository;
    private final AMCServiceConfiguration config;
    private final AmcConfigurationService amcConfigurationService;
    private final BoundaryUtil boundaryUtil;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper mapper;

    @Autowired
    public AmcVisitReportPdfService(
            ServiceRequestRepository requestRepository,
            AMCServiceConfiguration config,
            AmcConfigurationService amcConfigurationService,
            BoundaryUtil boundaryUtil) {
        this.requestRepository = requestRepository;
        this.config = config;
        this.amcConfigurationService = amcConfigurationService;
        this.boundaryUtil = boundaryUtil;
    }

    /**
     * Generates the AMC visit report PDF via pdf-service (key=amc-report) and returns the resulting fileStoreId.
     */
    public String generateAmcVisitReportPdf(VisitReportSubmissionRequest request, ScheduledVisit existingVisit, Facility facility) {
        log.trace("Entering generateAmcVisitReportPdf method for visitId: {}", existingVisit.getId());

        Map<String, Object> amcData = buildAmcData(request, existingVisit, facility);

        Map<String, Object> pdfRequestBody = new HashMap<>();
        pdfRequestBody.put("RequestInfo", request.getRequestInfo());
        pdfRequestBody.put("amc", amcData);

        String url = config.getPdfServiceHost() + config.getPdfServiceCreateUrl()
                + "?key=" + AMC_PDF_KEY + "&tenantId=" + existingVisit.getTenantId();

        log.debug("Calling pdf-service for AMC visit report, visitId: {}", existingVisit.getId());
        Object response = requestRepository.fetchResult(new StringBuilder(url), pdfRequestBody);
        Map<String, Object> pdfResponse = mapper.convertValue(response, Map.class);

        @SuppressWarnings("unchecked")
        List<String> fileStoreIds = pdfResponse != null ? (List<String>) pdfResponse.get("filestoreIds") : null;
        if (fileStoreIds == null || fileStoreIds.isEmpty()) {
            log.error("pdf-service did not return a filestoreId for visitId: {}", existingVisit.getId());
            throw new CustomException("AMC_PDF_GENERATION_FAILED", "pdf-service did not return a filestoreId for the AMC report");
        }

        log.info("AMC visit report PDF generated for visitId: {}, filestoreId: {}", existingVisit.getId(), fileStoreIds.get(0));
        return fileStoreIds.get(0);
    }

    /**
     * First non-null geoLocation found across the visit report's documents.
     */
    public GeoLocation resolveGeoLocation(VisitReport visitReport) {
        if (visitReport == null || visitReport.getDocuments() == null) {
            return null;
        }
        return visitReport.getDocuments().stream()
                .map(VisitReportDocument::getGeoLocation)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> buildAmcData(VisitReportSubmissionRequest request, ScheduledVisit existingVisit, Facility facility) {
        VisitReport visitReport = request.getVisitReport();

        Map<String, Object> data = new HashMap<>();
        if (visitReport.getResponses() != null) {
            data.putAll(visitReport.getResponses());
        }

        AmcConfiguration amcConfiguration = getAmcConfigurationWithVendorAndProject(
                request.getRequestInfo(), existingVisit.getAmcConfigurationId(), existingVisit.getTenantId());

        int totalVisits = (amcConfiguration.getVisitFrequencyMonths() != null && amcConfiguration.getVisitFrequencyMonths() != 0)
                ? amcConfiguration.getDurationMonths() / amcConfiguration.getVisitFrequencyMonths()
                : 0;

        Boundary boundary = resolveBoundary(facility.getBoundaryCode());
        GeoLocation geoLocation = resolveGeoLocation(visitReport);

        data.put("report_id", existingVisit.getId());
        data.put("tenantId", existingVisit.getTenantId());
        data.put("amc_number", existingVisit.getVisitNumber() + "/" + totalVisits);
        data.put("location_captured", geoLocation != null ? "Yes" : "No");
        data.put("actual_scheduled_amc_date", formatEpochMillisAsDate(existingVisit.getActualVisitDate()));
        data.put("actual_submission_amc_date", formatEpochMillisAsDate(System.currentTimeMillis()));
        data.put("health_facility_name", facility.getFacilityName());
        data.put("health_facility_address", facility.getAddress() != null ? facility.getAddress().getAddressLine1() : null);
        data.put("health_facility_type", facility.getFacilityType());
        data.put("vendor_name", amcConfiguration.getVendor() != null ? amcConfiguration.getVendor().getName() : null);
        data.put("project_number", amcConfiguration.getProject() != null ? amcConfiguration.getProject().getProjectNumber() : null);
        data.put("project_date", formatEpochMillisAsDate(existingVisit.getAmcConfiguration().getConfigurationStartDate()));
        data.put("project_state", boundary != null ? boundary.getState() : null);
        data.put("project_district", boundary != null ? boundary.getDistrict() : null);
        data.put("project_block", boundary != null ? boundary.getBlock() : null);
        data.put("nin_id", facility.getNinId() != null ? facility.getNinId() : facility.getHfrId());
        data.put("po_wo_number", null);
        data.put("documents", buildDocumentsForPdf(visitReport));

        return data;
    }

    private AmcConfiguration getAmcConfigurationWithVendorAndProject(
            org.egov.common.contract.request.RequestInfo requestInfo, String amcConfigurationId, String tenantId) {
        AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder()
                .ids(List.of(amcConfigurationId))
                .tenantId(tenantId)
                .build();
        AmcConfigurationSearchRequest searchRequest = AmcConfigurationSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
        List<AmcConfiguration> list = amcConfigurationService.searchAmcConfiguration(searchRequest, 1, 0, tenantId, false, null);
        if (list == null || list.isEmpty()) {
            log.error("AMC configuration not found for id: {}", amcConfigurationId);
            throw new CustomException("AMC_PDF_GENERATION_FAILED", "AMC configuration not found: " + amcConfigurationId);
        }
        return list.get(0);
    }

    private String formatEpochMillisAsDate(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(REPORT_DATE_FORMATTER);
    }

    private Boundary resolveBoundary(String boundaryCode) {
        if (boundaryCode == null) {
            return null;
        }
        Map<String, Boundary> boundaries = boundaryUtil.getBoundaryByCode();
        return boundaries != null ? boundaries.get(boundaryCode) : null;
    }

    private List<Map<String, Object>> buildDocumentsForPdf(VisitReport visitReport) {
        if (visitReport == null || visitReport.getDocuments() == null) {
            return List.of();
        }
        Map<String, List<String>> fileStoreIdsByType = new LinkedHashMap<>();
        for (VisitReportDocument doc : visitReport.getDocuments()) {
            String type = normalizeDocumentType(doc.getDocumentType());
            fileStoreIdsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(doc.getFileStoreId());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        fileStoreIdsByType.forEach((type, fileStoreIds) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("documentType", type);
            entry.put("fileStoreIds", fileStoreIds);
            result.add(entry);
        });
        return result;
    }

    private String normalizeDocumentType(String documentType) {
        if (IMG1_DOCUMENT_TYPE.equalsIgnoreCase(documentType)) {
            return INSTALLATION_IMAGE_1_DOCUMENT_TYPE;
        }
        return documentType;
    }
}
