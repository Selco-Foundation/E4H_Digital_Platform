package org.egov.asset.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.asset.mapper.AssetRowMapper;
import org.egov.asset.repository.AssetRepository;
import org.egov.asset.util.IdgenUtil;
import org.egov.asset.util.ResponseInfoFactory;
import digit.models.coremodels.AuditDetails;
import org.egov.asset.web.models.Asset;
import org.egov.asset.web.models.AssetCreateRequest;
import org.egov.asset.web.models.AssetCreateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AssetService {

    private final JdbcTemplate jdbcTemplate;
    private final AssetRowMapper assetRowMapper;
    private final IdgenUtil idgenUtil;
    private final AssetRepository assetRepository;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public AssetService(JdbcTemplate jdbcTemplate, AssetRowMapper assetRowMapper, IdgenUtil idgenUtil, AssetRepository assetRepository, ResponseInfoFactory responseInfoFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.assetRowMapper = assetRowMapper;
        this.idgenUtil = idgenUtil;
        this.assetRepository = assetRepository;
        this.responseInfoFactory = responseInfoFactory;
    }

    public AssetCreateResponse createFacility(AssetCreateRequest request) {
        List<String> ids = idgenUtil.getIdList(request.getRequestInfo(), request.getAssetDetail().getAsset().getTenantId(),
                "assetId","ASSET-[SEQ_ASSET_ID]",1);
        if(!ids.isEmpty())
            request.getAssetDetail().getAsset().setAssetId(ids.get(0));
        if(request.getAssetDetail().getAsset().getAuditDetails()==null){
            AuditDetails auditDetails = AuditDetails.builder()
                    .createdBy(request.getRequestInfo().getUserInfo().getUserName())
                    .createdTime(System.currentTimeMillis())
                    .lastModifiedBy(request.getRequestInfo().getUserInfo().getUserName())
                    .lastModifiedTime(System.currentTimeMillis())
                    .build();
            request.getAssetDetail().getAsset().setAuditDetails(auditDetails);
        }

        assetRepository.pushCreateAsset(request.getAssetDetail().getAsset());
        return AssetCreateResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),true))
                .asset(request.getAssetDetail().getAsset())
                .build();
    }

    public List<Asset> searchAssets(String tenantId, String assetId, String wfStatus, String facilityId, String serialNumber, String modelNumber, String brandId, int limit, int offset) {
        StringBuilder query = new StringBuilder("SELECT * FROM asset WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (tenantId != null && !tenantId.isBlank()) {
            query.append(" AND tenant_id = ?");
            params.add(tenantId);
        }

        if (assetId != null && !assetId.isBlank()) {
            query.append(" AND asset_id = ?");
            params.add(tenantId);
        }

        if (wfStatus != null && !wfStatus.isBlank()) {
            query.append(" AND wf_status = ?");
            params.add(tenantId);
        }

        if (facilityId != null && !facilityId.isBlank()) {
            query.append(" AND facility_id = ?");
            params.add(facilityId);
        }

        if (serialNumber != null && !serialNumber.isBlank()) {
            query.append(" AND serial_number = ?");
            params.add(serialNumber);
        }

        if (modelNumber != null && !modelNumber.isBlank()) {
            query.append(" AND model_number = ?");
            params.add(modelNumber);
        }

        if(brandId !=null && !brandId.isBlank()){
            query.append(" AND brand_id = ?");
            params.add(brandId);
        }

        query.append(" ORDER BY created_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(query.toString(), params.toArray(), assetRowMapper.rowMapper);
    }

}
