package org.egov.asset.service;

import digit.models.coremodels.AuditDetails;
//import digit.models.coremodels.Document;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.mapper.AssetRowMapper;
import org.egov.asset.mapper.DocumentRowMapper;
import org.egov.asset.repository.AssetRepository;
import org.egov.asset.util.ErrorConstants;
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
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public AssetService(JdbcTemplate jdbcTemplate, AssetRowMapper assetRowMapper, DocumentRowMapper documentRowMapper, IdgenUtil idgenUtil, AssetRepository assetRepository, ResponseInfoFactory responseInfoFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.assetRowMapper = assetRowMapper;
        this.documentRowMapper = documentRowMapper;
        this.idgenUtil = idgenUtil;
        this.assetRepository = assetRepository;
        this.responseInfoFactory = responseInfoFactory;
    }

    public AssetCreateResponse createAsset(AssetCreateRequest request) {
        log.info("AssetService::createAsset called for tenantId={}", request.getAssetDetail().getAsset().getTenantId());
        List<String> ids = idgenUtil.getIdList(request.getRequestInfo(), request.getAssetDetail().getAsset().getTenantId(),
                "assetId", "ASSET-[SEQ_ASSET_ID]", 1);
        List<String> documentIds = idgenUtil.getIdList(request.getRequestInfo(), request.getAssetDetail().getAsset().getTenantId(),
                "documentId", "DOCUMENT-[SEQ_DOCUMENT_ID]", request.getAssetDetail().getAsset().getDocuments().size());
        if (!ids.isEmpty())
            request.getAssetDetail().getAsset().setAssetId(ids.get(0));
        else
            throw new CustomException(ErrorConstants.ID_GEN_SERVICE_ERROR_CODE, ErrorConstants.ID_GEN_SERVICE_ERROR_MSG);
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

        assetRepository.pushCreateAsset(request.getAssetDetail().getAsset());
        return AssetCreateResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .asset(request.getAssetDetail().getAsset())
                .build();
    }

    public List<Asset> fetchAssetsWithDocuments(Asset request, int limit, int offset) {
        log.info("AssetService::fetchAssetsWithDocuments called | tenantId={} limit={} offset={}",
                request.getTenantId(), limit, offset);
        List<Asset> assets = searchAssets(request, limit, offset);
        log.debug("Found {} assets for tenantId={}", assets.size(), request.getTenantId());

        if (!assets.isEmpty()) {
            List<String> assetIds = assets.stream().map(Asset::getAssetId).collect(Collectors.toList());
            Map<String, List<Document>> documentsMap = searchDocumentsByAssetIds(request.getTenantId(), assetIds);

            assets.forEach(asset -> {
                List<Document> documents = documentsMap.getOrDefault(asset.getAssetId(), new ArrayList<>());
                asset.setDocuments(documents);
                log.debug("Enriched assetId={} with {} documents", asset.getAssetId(), documents.size());
            });
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
        log.info("AssetService::searchAssets called | tenantId={} assetId={} limit={} offset={}",
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

        log.debug("Executing asset search query={} with params={}", query, params);

        return jdbcTemplate.query(query.toString(), params.toArray(), assetRowMapper.rowMapper);
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
        log.info("AssetService::searchDocumentsByAssetIds called | tenantId={} assetIdsCount={}",
                tenantId, assetIds == null ? 0 : assetIds.size());
        if (assetIds == null || assetIds.isEmpty()) {
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

        log.debug("Executing asset document search query={} with params={}", query, params);

        return jdbcTemplate.query(query.toString(), params.toArray(), (rs) -> {
            Map<String, List<Document>> documentsMap = new HashMap<>();
            while (rs.next()) {
                String assetId = rs.getString("asset_id");
                Document document = documentRowMapper.mapDocument(rs);
                documentsMap.computeIfAbsent(assetId, k -> new ArrayList<>()).add(document);
            }
            return documentsMap;
        });
    }

    public Asset updateAsset(String assetId, AssetCreateRequest request) {
        log.info("AssetService::updateAsset called | assetId={}", assetId);
        if (request == null || request.getAssetDetail() == null || request.getAssetDetail().getAsset() == null) {
            throw new CustomException("INVALID_REQUEST", "Asset request cannot be null");
        }
        Asset updated = request.getAssetDetail().getAsset();
        if (!assetId.equals(updated.getAssetId())) {
            throw new CustomException("ASSET_ID_MISMATCH", "Provided assetId does not match the asset's ID");
        }

        // Check whether asset exists in the database
        List<Asset> existingAssets = searchAssets(Asset.builder().assetId(updated.getAssetId()).tenantId(updated.getTenantId()).build(), 10, 0);
        if (existingAssets == null || existingAssets.isEmpty()) {
            throw new CustomException("ASSET_NOT_FOUND", "Asset with ID " + assetId + " does not exist");
        }

        // Update audit details
        if (updated.getAuditDetails() != null) {
            updated.getAuditDetails().setLastModifiedBy(request.getRequestInfo().getUserInfo().getUserName());
            updated.getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
            log.debug("Updated audit details for assetId={}", updated.getAssetId());
        }
        assetRepository.pushUpdateAsset(updated);
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
