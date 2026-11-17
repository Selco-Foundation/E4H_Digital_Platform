package org.egov.amc.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.service.AmcConfigurationService;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationSearchCriteria;
import org.egov.amc.web.models.AmcConfigurationSearchRequest;
import org.egov.amc.web.models.AssetAmc;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.common.service.IdGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetAmcEnrichment {

    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FOR_PROJECT = " for project ";
    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;

    @Autowired
    private final AmcConfigurationService amcConfigurationService;

    /* Enrich Project on Create Request */
    public void enrichAssetAmcOnCreate(AssetAmc assetAmc, RequestInfo requestInfo) {
        //Enrich Project id and audit details
        enrichAssetAmcRequestOnCreate(assetAmc, requestInfo);
        log.info("Enriched AMC request with id and Audit details");

    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichAssetAmcRequestOnCreate(AssetAmc assetAmc, RequestInfo requestInfo) {
        assetAmc.setId(UUID.randomUUID().toString());
        log.info("AMC configs id set to " + assetAmc.getId());
        String amcConfigurationIds = assetAmc.getAmcConfigurationId();
        AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder().ids(new ArrayList<>(List.of(amcConfigurationIds))).tenantId(assetAmc.getTenantId()).build();
        AmcConfigurationSearchRequest request = AmcConfigurationSearchRequest.builder().RequestInfo(requestInfo).searchCriteria(criteria).build();
        List<AmcConfiguration> amcConfigurationList = amcConfigurationService.searchAmcConfiguration(request, 10, 0, assetAmc.getTenantId(), false, null );
        if (amcConfigurationList !=null && !amcConfigurationList.isEmpty()){
            long configurationStartDate = amcConfigurationList.get(0).getConfigurationStartDate();
            int duration = amcConfigurationList.get(0).getDurationMonths();
            // Convertir milliseconds -> LocalDateTime
            LocalDateTime start = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(configurationStartDate),
                    ZoneOffset.UTC
            );

            // Ajouter les mois
            LocalDateTime end = start.plusMonths(duration);

            // Reconvertir LocalDateTime → milliseconds
            long endDateMillis = end.toInstant(ZoneOffset.UTC).toEpochMilli();
            assetAmc.setAmcEndDate(endDateMillis);
        }

        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        assetAmc.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichAssetAmcRequestOnUpdate(AssetAmc assetAmc, AssetAmc assetAmcFromDB, RequestInfo requestInfo) {
        assetAmc.setAuditDetails(assetAmcFromDB.getAuditDetails());
        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), assetAmcFromDB.getAuditDetails(), false);
        assetAmc.setAuditDetails(auditDetails);
        log.info("Enriched AMC configs audit details for amc " + assetAmc.getId());
    }


}
