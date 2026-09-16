package org.egov.amc.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.web.models.AmcPlan;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmcPlanEnrichment {

    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;

    public void enrichAmcPlanOnCreate(AmcPlan amcPlan, RequestInfo requestInfo) {
        log.trace("Entering enrichAmcPlanOnCreate method");
        amcPlan.setId(UUID.randomUUID().toString());
        // Always server-controlled: a client cannot create an already soft-deleted plan.
        amcPlan.setIsDeleted(Boolean.FALSE);
        if (amcPlan.getStatus() == null) {
            amcPlan.setStatus("ACTIVE");
        }
        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        amcPlan.setAuditDetails(auditDetails);
        log.info("AMC plan enriched with ID and audit details, planId: {}", amcPlan.getId());
    }

    public void enrichAmcPlanOnUpdate(AmcPlan amcPlan, AmcPlan amcPlanFromDB, RequestInfo requestInfo) {
        log.trace("Entering enrichAmcPlanOnUpdate method for planId: {}", amcPlan.getId());
        amcPlan.setAuditDetails(amcPlanFromDB.getAuditDetails());
        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), amcPlanFromDB.getAuditDetails(), false);
        amcPlan.setAuditDetails(auditDetails);
        log.info("AMC plan audit details enriched for planId: {}", amcPlan.getId());
    }
}
