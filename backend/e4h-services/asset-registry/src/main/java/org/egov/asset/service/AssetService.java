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
        List<String> ids = idgenUtil.getIdList(request.getRequestInfo(), request.getAssetDetail().getAsset().getTenantId(),
                "assetId", "ASSET-[SEQ_ASSET_ID]", 1);
        List<String> documentIds = idgenUtil.getIdList(request.getRequestInfo(), request.getAssetDetail().getAsset().getTenantId(),
                "documentId", "DOCUMENT-[SEQ_DOCUMENT_ID]", request.getAssetDetail().getAsset().getDocuments().size());
        if (!ids.isEmpty())
            request.getAssetDetail().getAsset().setAssetId(ids.get(0));
        else
            throw new CustomException(ErrorConstants.ID_GEN_SERVICE_ERROR_CODE, ErrorConstants.ID_GEN_SERVICE_ERROR_MSG);
        if (request.getAssetDetail().getAsset().getAuditDetails() == null) {
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
        List<Asset> assets = searchAssets(request, limit, offset);

        if (!assets.isEmpty()) {
            List<String> assetIds = assets.stream().map(Asset::getAssetId).collect(Collectors.toList());
            Map<String, List<Document>> documentsMap = searchDocumentsByAssetIds(request.getTenantId(), assetIds);

            assets.forEach(asset -> {
                List<Document> documents = documentsMap.getOrDefault(asset.getAssetId(), new ArrayList<>());
                asset.setDocuments(documents);
            });
        }

        return assets;
    }

    public List<Asset> searchAssets(Asset asset, int limit, int offset) {
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

        if (asset.getWfStatus() != null && !asset.getWfStatus().isBlank()) {
            query.append(" AND wf_status = ?");
            params.add(asset.getWfStatus());
        }

        if (asset.getFacilityID() != null && !asset.getFacilityID().isBlank()) {
            query.append(" AND facility_id = ?");
            params.add(asset.getFacilityID());
        }

        if (asset.getSerialNumber() != null && !asset.getSerialNumber().isBlank()) {
            query.append(" AND serial_number = ?");
            params.add(asset.getSerialNumber());
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

        return jdbcTemplate.query(query.toString(), params.toArray(), assetRowMapper.rowMapper);
    }

    public Map<String, List<Document>> searchDocumentsByAssetIds(String tenantId, List<String> assetIds) {
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

}
