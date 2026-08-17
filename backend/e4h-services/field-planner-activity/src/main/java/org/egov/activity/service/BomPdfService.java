package org.egov.activity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.util.BoundaryLocalizationUtil;
import org.egov.activity.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.egov.activity.util.ActivityConstants.INSTALLATION_IMAGE_DOCUMENT_TYPE_PREFIX;

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

    private static final String DOCUMENTS_KEY = "documents";
    private static final String DOCUMENT_TYPE_KEY = "documentType";
    private static final String FILE_STORE_ID_KEY = "fileStoreId";

    /**
     * Non-image document types carried through to {@link BomService#generateAndSaveBOMPdfToFilestore},
     * which appends them as extra pages at the end of the generated report. Must stay spelled exactly
     * as BomService.APPENDABLE_DOCUMENT_TYPES, which matches on documentType with equals().
     */
    private static final List<String> APPENDABLE_DOCUMENT_TYPES = List.of(
            "ASSET_HANDOVER_DOCUMENT", "INSTALLATION_COMPLETION_CERTIFICATE"
    );

    private static final String SR_NO_KEY = "srNo";
    private static final String SERIAL_NUMBER_KEY = "serialNumber";

    /**
     * assetTypeID (as stored by asset-registry) -> the bomData key its serial numbers render under.
     * Every key is always emitted, empty when the facility has no asset of that type, so the report
     * template never has to guard against a missing field.
     */
    private static final Map<String, String> SERIAL_NUMBER_KEY_BY_ASSET_TYPE = Map.of(
            "PANEL", "panel_serial_number",
            "BATTERY", "battery_serial_number",
            "INVERTER", "inverter_serial_number"
    );

    private final BomService bomService;
    private final BoundaryLocalizationUtil boundaryLocalizationUtil;
    private final ServiceRequestRepository serviceRequest;
    private final ActivityConfiguration activityConfiguration;

    public BomPdfService(BomService bomService, BoundaryLocalizationUtil boundaryLocalizationUtil,
                         ServiceRequestRepository serviceRequest, ActivityConfiguration activityConfiguration) {
        this.bomService = bomService;
        this.boundaryLocalizationUtil = boundaryLocalizationUtil;
        this.serviceRequest = serviceRequest;
        this.activityConfiguration = activityConfiguration;
    }

    /**
     * Generates the BOM installation-report PDF via pdf-service and returns the resulting
     * fileStoreId.
     *
     * @param workflowDocuments documents carried on the incoming workflow. The installation images
     *                          among them are rendered inside the report and the appendable ones are
     *                          merged onto its end, both by
     *                          {@link BomService#generateAndSaveBOMPdfToFilestore}; may be null.
     */
    public String generateInstallationReportPdf(RequestInfo requestInfo, ActivityFacility activityFacility,
                                                List<Document> workflowDocuments) {
        log.trace("Entering generateInstallationReportPdf method for activityFacilityId: {}", activityFacility.getId());
        String tenantId = activityFacility.getTenantId();

        String systemType = resolveSystemType(activityFacility);
        BillOfMaterial bom = findBomForActivityFacility(requestInfo, activityFacility, tenantId);
        Map<String, Object> bomData = buildBomData(requestInfo, activityFacility, bom);
        bomData.put(DOCUMENTS_KEY, toPdfDocuments(workflowDocuments));
        bomData.putAll(buildSerialNumbersByAssetType(requestInfo, activityFacility.getId(), tenantId));

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
        data.put("project_number", activityFacility.getFieldPlan() != null && activityFacility.getFieldPlan().getProject() != null
                ? (activityFacility.getFieldPlan().getProject().getProjectNumber() != null ? activityFacility.getFieldPlan().getProject().getProjectNumber() : null) : null);
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

    /**
     * Groups the serial numbers of the facility's assets by asset type, one bomData entry per type:
     * <pre>
     * "panel_serial_number": [ { "srNo": 1, "serialNumber": "001" }, ... ]
     * </pre>
     * srNo restarts at 1 for each type and follows the order asset-registry returns.
     */
    private Map<String, Object> buildSerialNumbersByAssetType(RequestInfo requestInfo, String activityFacilityId, String tenantId) {
        Map<String, List<Map<String, Object>>> serialNumbersByKey = new LinkedHashMap<>();
        for (String bomDataKey : SERIAL_NUMBER_KEY_BY_ASSET_TYPE.values()) {
            serialNumbersByKey.put(bomDataKey, new ArrayList<>());
        }

        for (Asset asset : searchAssets(requestInfo, activityFacilityId, tenantId)) {
            if (asset == null || asset.getAssetTypeID() == null) {
                continue;
            }
            String bomDataKey = SERIAL_NUMBER_KEY_BY_ASSET_TYPE.get(asset.getAssetTypeID().toUpperCase());
            if (bomDataKey == null || asset.getSerialNumber() == null || asset.getSerialNumber().isBlank()) {
                continue;
            }
            List<Map<String, Object>> serialNumbers = serialNumbersByKey.get(bomDataKey);
            Map<String, Object> entry = new HashMap<>();
            entry.put(SR_NO_KEY, serialNumbers.size() + 1);
            entry.put(SERIAL_NUMBER_KEY, asset.getSerialNumber());
            serialNumbers.add(entry);
        }

        log.debug("Resolved asset serial numbers for activityFacilityId: {} -> {}", activityFacilityId,
                serialNumbersByKey.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue().size())
                        .toList());
        return new LinkedHashMap<>(serialNumbersByKey);
    }

    /**
     * All assets recorded against the activity facility. limit is sent explicitly because
     * asset-registry defaults it to 10, which would silently drop serial numbers.
     */
    private List<Asset> searchAssets(RequestInfo requestInfo, String activityFacilityId, String tenantId) {
        AssetSearchRequest searchRequest = AssetSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(AssetSearchCriteria.builder()
                        .activityFacilityID(activityFacilityId)
                        .tenantId(tenantId)
                        .build())
                .build();

        StringBuilder uri = new StringBuilder(activityConfiguration.getAssetHost())
                .append(activityConfiguration.getAssetSearchUrl())
                .append("?offset=0&limit=").append(activityConfiguration.getAssetSearchLimit());

        try {
            // The endpoint responds with a bare Asset array, not an envelope.
            List<Asset> assets = serviceRequest.fetchResult(uri, searchRequest, new TypeReference<List<Asset>>() {});
            return assets != null ? assets : List.of();
        } catch (Exception e) {
            log.error("Failed to fetch assets for activityFacilityId: {}", activityFacilityId, e);
            throw new CustomException("BOM_PDF_GENERATION_FAILED",
                    "Failed to fetch assets for activityFacility " + activityFacilityId + " - cannot resolve serial numbers");
        }
    }

    /**
     * Selects the workflow documents the report needs - every INSTALLATION_IMAGE-* one plus the
     * appendable types - and flattens them to the raw, ungrouped shape BomService expects under
     * "documents": plain Maps keyed by documentType/fileStoreId.
     * <p>
     * Plain Maps rather than {@link Document} instances on purpose: BomService.enrichBomData reads
     * this list as {@code List<Map<String, Object>>} and would fail on POJO elements. Entries without
     * a fileStoreId are dropped - both downstream consumers ignore them anyway.
     */
    private List<Map<String, Object>> toPdfDocuments(List<Document> workflowDocuments) {
        List<Map<String, Object>> pdfDocuments = new ArrayList<>();
        if (workflowDocuments == null || workflowDocuments.isEmpty()) {
            return pdfDocuments;
        }

        for (Document document : workflowDocuments) {
            if (document == null || document.getDocumentType() == null || document.getFileStoreId() == null) {
                continue;
            }
            if (!isReportDocument(document.getDocumentType())) {
                continue;
            }
            Map<String, Object> pdfDocument = new HashMap<>();
            pdfDocument.put(DOCUMENT_TYPE_KEY, document.getDocumentType());
            pdfDocument.put(FILE_STORE_ID_KEY, document.getFileStoreId());
            pdfDocuments.add(pdfDocument);
        }

        log.debug("Carrying {} of {} workflow documents into the BOM report data",
                pdfDocuments.size(), workflowDocuments.size());
        return pdfDocuments;
    }

    private boolean isReportDocument(String documentType) {
        return documentType.contains(INSTALLATION_IMAGE_DOCUMENT_TYPE_PREFIX)
                || APPENDABLE_DOCUMENT_TYPES.contains(documentType);
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
