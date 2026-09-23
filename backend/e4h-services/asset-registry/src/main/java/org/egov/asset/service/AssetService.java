package org.egov.asset.service;

import digit.models.coremodels.AuditDetails;
//import digit.models.coremodels.Document;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.mapper.AssetRowMapper;
import org.egov.asset.mapper.DocumentRowMapper;
import org.egov.asset.repository.AssetRepository;
import org.egov.asset.util.AssetConstants;
import org.egov.asset.util.ErrorConstants;
import org.egov.asset.util.FacilityUtil;
import org.egov.asset.util.IdgenUtil;
import org.egov.asset.util.ResponseInfoFactory;
import org.egov.asset.web.models.Asset;
import org.egov.asset.web.models.AssetCreateRequest;
import org.egov.asset.web.models.AssetCreateResponse;
import org.egov.asset.web.models.Document;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class AssetService {

    private final JdbcTemplate jdbcTemplate;
    private final AssetRowMapper assetRowMapper;
    private final DocumentRowMapper documentRowMapper;
    private final IdgenUtil idgenUtil;
    private final AssetRepository assetRepository;
    /** Facilities named in one duplicate flag, capped so a serial number shared estate-wide stays cheap. */
    private static final int MAX_DUPLICATE_FACILITIES = 20;

    private final ResponseInfoFactory responseInfoFactory;
    private final FacilityUtil facilityUtil;

    @Autowired
    public AssetService(JdbcTemplate jdbcTemplate, AssetRowMapper assetRowMapper, DocumentRowMapper documentRowMapper, IdgenUtil idgenUtil, AssetRepository assetRepository, ResponseInfoFactory responseInfoFactory, FacilityUtil facilityUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.assetRowMapper = assetRowMapper;
        this.documentRowMapper = documentRowMapper;
        this.idgenUtil = idgenUtil;
        this.assetRepository = assetRepository;
        this.responseInfoFactory = responseInfoFactory;
        this.facilityUtil = facilityUtil;
    }

    public AssetCreateResponse createAsset(AssetCreateRequest request) {
        log.trace("AssetService::createAsset called");
        String tenantId = request.getAssetDetail().getAsset().getTenantId();
        log.info("Creating asset for tenantId={}", tenantId);
        try {
            List<String> ids = idgenUtil.getIdList(request.getRequestInfo(), tenantId,
                    "assetId", "ASSET-[SEQ_ASSET_ID]", 1);
            log.debug("Generated asset IDs count={}", ids.size());
            List<String> documentIds = idgenUtil.getIdList(request.getRequestInfo(), tenantId,
                    "documentId", "DOCUMENT-[SEQ_DOCUMENT_ID]", request.getAssetDetail().getAsset().getDocuments().size());
            log.debug("Generated document IDs count={}", documentIds.size());
            
            if (!ids.isEmpty()) {
                request.getAssetDetail().getAsset().setAssetId(ids.get(0));
            } else {
                log.error("ID generation failed for asset | tenantId={}", tenantId);
                throw new CustomException(ErrorConstants.ID_GEN_SERVICE_ERROR_CODE, ErrorConstants.ID_GEN_SERVICE_ERROR_MSG);
            }
            
            if (request.getAssetDetail().getAsset().getAuditDetails() == null) {
                log.debug("Setting audit details for assetId={}", request.getAssetDetail().getAsset().getAssetId());
                AuditDetails auditDetails = AuditDetails.builder()
                        .createdBy(request.getRequestInfo().getUserInfo().getUserName())
                        .createdTime(System.currentTimeMillis())
                        .lastModifiedBy(request.getRequestInfo().getUserInfo().getUserName())
                        .lastModifiedTime(System.currentTimeMillis())
                        .build();
                request.getAssetDetail().getAsset().setAuditDetails(auditDetails);
            }
            IntStream.range(0, documentIds.size())
                    .forEach(i -> request.getAssetDetail().getAsset().getDocuments().get(i).setId(documentIds.get(i)));

            enrichPotentialDuplicate(request.getAssetDetail().getAsset());

            log.info("Pushing asset creation to repository | assetId={}", request.getAssetDetail().getAsset().getAssetId());
            assetRepository.pushCreateAsset(request.getAssetDetail().getAsset());
            log.info("Asset created successfully | assetId={} tenantId={}", 
                    request.getAssetDetail().getAsset().getAssetId(), tenantId);
            return AssetCreateResponse.builder()
                    .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                    .asset(request.getAssetDetail().getAsset())
                    .build();
        } catch (CustomException e) {
            log.error("Error creating asset | tenantId={} errorCode={}", tenantId, e.getCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating asset | tenantId={}", tenantId, e);
            throw new CustomException("ASSET_CREATION_ERROR", "Failed to create asset: " + e.getMessage());
        }
    }

    /**
     * Flags the asset as a potential duplicate when the same Asset Brand + Asset Serial Number
     * combination already exists in the registry, by writing {@code isPotentialDuplicate} into its
     * assetDetails (persisted as-is in the asset_details JSONB column).
     * <p>
     * The flag is informational only: a duplicate is never rejected, the asset is created or updated
     * either way. It is written on every asset, true or false, so consumers never have to tell a
     * non-duplicate apart from an asset created before this check existed.
     * <p>
     * Runs on both create and update: an update can move an asset onto an already used brand +
     * serial number, and can equally move it off one, so the flag is always re-evaluated.
     */
    void enrichPotentialDuplicate(Asset asset) {
        log.trace("AssetService::enrichPotentialDuplicate called");
        List<DuplicateMatch> matches = findDuplicates(asset);
        boolean isPotentialDuplicate = !matches.isEmpty();

        // Copied rather than mutated in place: the map comes straight from the request body and
        // nothing guarantees it is modifiable.
        Map<String, Object> assetDetails = asset.getAssetDetails() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(asset.getAssetDetails());
        assetDetails.put(AssetConstants.IS_POTENTIAL_DUPLICATE, isPotentialDuplicate);

        if (isPotentialDuplicate) {
            // Every health facility the serial number was already submitted from, so the UI can name
            // them all. Facilities whose name cannot be resolved are left out rather than carried as
            // nulls - the duplicate flag stands on its own.
            List<String> duplicateFacilityNames = matches.stream()
                    .map(match -> facilityUtil.getFacilityName(match.tenantId(), match.facilityId()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            assetDetails.put(AssetConstants.DUPLICATE_FACILITY_NAME, duplicateFacilityNames);
            log.info("Potential duplicate asset | assetId={} assetTypeID={} brandID={} serialNumber={} duplicateFacilities={} duplicateFacilityNames={}",
                    asset.getAssetId(), asset.getAssetTypeID(), asset.getBrandID(), asset.getSerialNumber(),
                    matches.size(), duplicateFacilityNames);
        } else {
            // An update can clear a duplicate - a corrected serial number must not keep pointing at
            // the facilities it used to clash with.
            assetDetails.remove(AssetConstants.DUPLICATE_FACILITY_NAME);
            log.info("No duplicate found | assetId={} assetTypeID={} brandID={} serialNumber={}",
                    asset.getAssetId(), asset.getAssetTypeID(), asset.getBrandID(), asset.getSerialNumber());
        }

        asset.setAssetDetails(assetDetails);
    }

    /* One health facility a duplicate serial number was already submitted from. */
    private record DuplicateMatch(String facilityId, String tenantId) {
    }

    /**
     * Every health facility already holding an asset with this brand and serial number, oldest
     * submission first, empty when there is none. The lookup is global - no tenant, facility, asset
     * type or workflow status narrowing - as required by the duplicate rule. A blank brand or serial
     * number cannot form the duplicate key, so it never matches.
     * <p>
     * Asset type is deliberately not part of the key: the flag is advisory, so missing a duplicate
     * costs more than flagging one, and the same serial number keyed under two different asset types
     * is itself a data-entry mistake worth surfacing.
     * <p>
     * The asset's own row is excluded, otherwise every update of an unchanged asset would match
     * itself. On create that row does not exist yet, so excluding it changes nothing.
     * <p>
     * Grouped by facility, so several assets recorded against one facility are one match and cost
     * one facility-service lookup.
     */
    private List<DuplicateMatch> findDuplicates(Asset asset) {
        log.trace("AssetService::findDuplicates called");
        String brandId = asset.getBrandID();
        String serialNumber = asset.getSerialNumber();
        if (brandId == null || brandId.isBlank() || serialNumber == null || serialNumber.isBlank()) {
            log.debug("Incomplete duplicate key, skipping lookup | assetId={} brandID={} serialNumber={}",
                    asset.getAssetId(), brandId, serialNumber);
            return Collections.emptyList();
        }

        StringBuilder query = new StringBuilder(
                "SELECT facility_id, tenant_id, MIN(created_time) AS first_created_time FROM asset"
                        + " WHERE brand_id = ? AND serial_number = ?");
        List<Object> params = new ArrayList<>(List.of(brandId, serialNumber));
        if (asset.getAssetId() != null && !asset.getAssetId().isBlank()) {
            query.append(" AND asset_id <> ?");
            params.add(asset.getAssetId());
        }
        query.append(" GROUP BY facility_id, tenant_id ORDER BY first_created_time ASC LIMIT ?");
        params.add(MAX_DUPLICATE_FACILITIES + 1);

        try {
            List<DuplicateMatch> matches = jdbcTemplate.query(query.toString(), params.toArray(),
                    (rs, rowNum) -> new DuplicateMatch(rs.getString("facility_id"), rs.getString("tenant_id")));
            log.info("Duplicate lookup completed | brandID={} serialNumber={} facilityMatches={}",
                    brandId, serialNumber, matches.size());
            if (matches.size() > MAX_DUPLICATE_FACILITIES) {
                // A placeholder serial number bulk-loaded across the whole estate would otherwise fan
                // out into one facility-service call per facility on every create and update.
                log.warn("Serial number {} of brand {} is recorded against more than {} facilities - naming the first {} only",
                        serialNumber, brandId, MAX_DUPLICATE_FACILITIES, MAX_DUPLICATE_FACILITIES);
                return matches.subList(0, MAX_DUPLICATE_FACILITIES);
            }
            return matches;
        } catch (Exception e) {
            log.error("Error checking for duplicate asset | brandID={} serialNumber={} error={}",
                    brandId, serialNumber, e.getMessage(), e);
            throw new CustomException("ASSET_DUPLICATE_CHECK_ERROR",
                    "Failed to check for duplicate assets: " + e.getMessage());
        }
    }

    public List<Asset> fetchAssetsWithDocuments(Asset request, int limit, int offset) {
        log.trace("AssetService::fetchAssetsWithDocuments called");
        log.info("Fetching assets with documents | tenantId={} limit={} offset={}",
                request.getTenantId(), limit, offset);
        List<Asset> assets = searchAssets(request, limit, offset);
        log.debug("Found {} assets for tenantId={}", assets.size(), request.getTenantId());

        if (!assets.isEmpty()) {
            List<String> assetIds = assets.stream().map(Asset::getAssetId).collect(Collectors.toList());
            log.debug("Fetching documents for assetIds count={}", assetIds.size());
            Map<String, List<Document>> documentsMap = searchDocumentsByAssetIds(request.getTenantId(), assetIds);

            log.info("Enriching assets with documents | assetsCount={}", assets.size());
            assets.forEach(asset -> {
                List<Document> documents = documentsMap.getOrDefault(asset.getAssetId(), new ArrayList<>());
                asset.setDocuments(documents);
                log.debug("Enriched assetId={} with {} documents", asset.getAssetId(), documents.size());
            });
            log.info("Assets enriched with documents successfully | assetsCount={}", assets.size());
        }

        return assets;
    }

    public Integer getAssetsCount(Asset request) {
        log.info("AssetService::fetchAssetsWithDocuments called | tenantId={}",
                request.getTenantId());
         Integer count = countAssets(request);
        log.info("Total Assets count is : " + count);

        return count;
    }

    public List<Asset> searchAssets(Asset asset, int limit, int offset) {
        log.trace("AssetService::searchAssets called");
        log.info("Searching assets | tenantId={} assetId={} limit={} offset={}",
                asset.getTenantId(), asset.getAssetId(), limit, offset);
        StringBuilder query = new StringBuilder("SELECT * FROM asset WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (asset.getTenantId() != null && !asset.getTenantId().isBlank()) {
            query.append(" AND tenant_id = ?");
            params.add(asset.getTenantId());
        }

        if (asset.getAssetId() != null && !asset.getAssetId().isBlank()) {
            query.append(" AND asset_id = ?");
            params.add(asset.getAssetId());
        }

        if (!CollectionUtils.isEmpty(asset.getAssetTypeSearch())) {
            query.append(" AND asset_type_id IN (").append(createQuery(asset.getAssetTypeSearch())).append(")");
            params.addAll(asset.getAssetTypeSearch());
        }

        if (asset.getWfStatus() != null && !asset.getWfStatus().isBlank()) {
            query.append(" AND wf_status = ?");
            params.add(asset.getWfStatus());
        }

        if (asset.getIsOperational() != null) {
            query.append(" AND is_operational = ?");
            params.add(asset.getIsOperational());
        }

        if (asset.getFacilityID() != null && !asset.getFacilityID().isBlank()) {
            query.append(" AND facility_id = ?");
            params.add(asset.getFacilityID());
        }

        if (asset.getActivityFacilityID() != null && !asset.getActivityFacilityID().isBlank()) {
            query.append(" AND activity_facility_id = ?");
            params.add(asset.getActivityFacilityID());
        }

//        if (asset.getSerialNumber() != null && !asset.getSerialNumber().isBlank()) {
//            query.append(" AND serial_number = ?");
//            params.add(asset.getSerialNumber());
//        }

        if (!CollectionUtils.isEmpty(asset.getSerialNumberSearch())) {
            query.append(" AND serial_number IN (").append(createQuery(asset.getSerialNumberSearch())).append(")");
            params.addAll(asset.getSerialNumberSearch());
        }

        if (asset.getModelNumber() != null && !asset.getModelNumber().isBlank()) {
            query.append(" AND model_number = ?");
            params.add(asset.getModelNumber());
        }

        if (asset.getBrandID()!= null && !asset.getBrandID().isBlank()) {
            query.append(" AND brand_id = ?");
            params.add(asset.getBrandID());
        }

        query.append(" ORDER BY created_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        log.debug("Executing asset search query | paramsCount={}", params.size());
        try {
            List<Asset> results = jdbcTemplate.query(query.toString(), params.toArray(), assetRowMapper.rowMapper);
            log.debug("Asset search completed | resultsCount={}", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error executing asset search query | tenantId={} error={}", 
                    asset.getTenantId(), e.getMessage(), e);
            throw new CustomException("ASSET_SEARCH_ERROR", "Failed to search assets: " + e.getMessage());
        }
    }

    public Integer countAssets(Asset asset) {
        log.info("AssetService::searchAssets called | tenantId={} assetId={}",
                asset.getTenantId(), asset.getAssetId());
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM asset WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (asset.getTenantId() != null && !asset.getTenantId().isBlank()) {
            query.append(" AND tenant_id = ?");
            params.add(asset.getTenantId());
        }

        if (asset.getAssetId() != null && !asset.getAssetId().isBlank()) {
            query.append(" AND asset_id = ?");
            params.add(asset.getAssetId());
        }

        if (asset.getAssetTypeID() != null && !asset.getAssetTypeID().isBlank()) {
            query.append(" AND asset_type_id = ?");
            params.add(asset.getAssetTypeID());
        }

        if (asset.getWfStatus() != null && !asset.getWfStatus().isBlank()) {
            query.append(" AND wf_status = ?");
            params.add(asset.getWfStatus());
        }

        if (asset.getFacilityID() != null && !asset.getFacilityID().isBlank()) {
            query.append(" AND facility_id = ?");
            params.add(asset.getFacilityID());
        }

        if (asset.getActivityFacilityID() != null && !asset.getActivityFacilityID().isBlank()) {
            query.append(" AND activity_facility_id = ?");
            params.add(asset.getActivityFacilityID());
        }

        if (!CollectionUtils.isEmpty(asset.getSerialNumberSearch())) {
            query.append(" AND serial_number IN (").append(createQuery(asset.getSerialNumberSearch())).append(")");
            params.addAll(asset.getSerialNumberSearch());
        }

        if (asset.getModelNumber() != null && !asset.getModelNumber().isBlank()) {
            query.append(" AND model_number = ?");
            params.add(asset.getModelNumber());
        }

        if (asset.getBrandID()!= null && !asset.getBrandID().isBlank()) {
            query.append(" AND brand_id = ?");
            params.add(asset.getBrandID());
        }

        log.debug("Executing asset search count={} with params={}", query, params);

        return jdbcTemplate.queryForObject(query.toString(), params.toArray(), Integer.class);
    }

    public Map<String, List<Document>> searchDocumentsByAssetIds(String tenantId, List<String> assetIds) {
        log.trace("AssetService::searchDocumentsByAssetIds called");
        int assetIdsCount = assetIds == null ? 0 : assetIds.size();
        log.info("Searching documents by assetIds | tenantId={} assetIdsCount={}", tenantId, assetIdsCount);
        if (assetIds == null || assetIds.isEmpty()) {
            log.debug("No assetIds provided, returning empty map");
            return new HashMap<>();
        }

        StringBuilder query = new StringBuilder("SELECT * FROM asset_documents WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (tenantId != null && !tenantId.isBlank()) {
            query.append(" AND tenant_id = ?");
            params.add(tenantId);
        }
        query.append(" AND asset_id IN (");
        query.append(String.join(",", Collections.nCopies(assetIds.size(), "?")));
        query.append(")");
        params.addAll(assetIds);

        log.debug("Executing document search query | paramsCount={}", params.size());
        try {
            Map<String, List<Document>> documentsMap = jdbcTemplate.query(query.toString(), params.toArray(), (rs) -> {
                Map<String, List<Document>> resultMap = new HashMap<>();
                while (rs.next()) {
                    String assetId = rs.getString("asset_id");
                    Document document = documentRowMapper.mapDocument(rs);
                    resultMap.computeIfAbsent(assetId, k -> new ArrayList<>()).add(document);
                }
                return resultMap;
            });
            log.debug("Document search completed | documentsMapSize={}", documentsMap.size());
            return documentsMap;
        } catch (Exception e) {
            log.error("Error executing document search query | tenantId={} assetIdsCount={} error={}", 
                    tenantId, assetIdsCount, e.getMessage(), e);
            throw new CustomException("DOCUMENT_SEARCH_ERROR", "Failed to search documents: " + e.getMessage());
        }
    }

    public Asset updateAsset(String assetId, AssetCreateRequest request) {
        log.trace("AssetService::updateAsset called");
        log.info("Updating asset | assetId={}", assetId);
        if (request == null || request.getAssetDetail() == null || request.getAssetDetail().getAsset() == null) {
            log.error("Invalid update request | assetId={} request is null", assetId);
            throw new CustomException("INVALID_REQUEST", "Asset request cannot be null");
        }
        Asset updated = request.getAssetDetail().getAsset();
        if (!assetId.equals(updated.getAssetId())) {
            log.error("Asset ID mismatch | pathAssetId={} requestAssetId={}", assetId, updated.getAssetId());
            throw new CustomException("ASSET_ID_MISMATCH", "Provided assetId does not match the asset's ID");
        }

        log.debug("Checking if asset exists | assetId={} tenantId={}", updated.getAssetId(), updated.getTenantId());
        List<Asset> existingAssets = searchAssets(Asset.builder().assetId(updated.getAssetId()).tenantId(updated.getTenantId()).build(), 10, 0);
        if (existingAssets == null || existingAssets.isEmpty()) {
            log.error("Asset not found for update | assetId={} tenantId={}", assetId, updated.getTenantId());
            throw new CustomException("ASSET_NOT_FOUND", "Asset with ID " + assetId + " does not exist");
        }

        if (updated.getAuditDetails() != null) {
            updated.getAuditDetails().setLastModifiedBy(request.getRequestInfo().getUserInfo().getUserName());
            updated.getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
            log.debug("Updated audit details for assetId={}", updated.getAssetId());
        }

        enrichPotentialDuplicate(updated);

        log.info("Pushing asset update to repository | assetId={}", assetId);
        assetRepository.pushUpdateAsset(updated);
        log.info("Asset updated successfully | assetId={} tenantId={}", assetId, updated.getTenantId());
        return updated;
    }

    private String createQuery(Collection<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ? ");
            if (i != length - 1) builder.append(",");
        }
        return builder.toString();
    }
}
