package org.egov.field_planner.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.web.models.FieldPlanTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FieldPlanTemplateEnrichment {

    private final FieldPlannerServiceUtil fieldPlannerServiceUtil;

    public void enrichOnCreate(FieldPlanTemplate template, RequestInfo requestInfo) {
        template.setId(UUID.randomUUID().toString());
        AuditDetails auditDetails = fieldPlannerServiceUtil.getAuditDetails(
                requestInfo.getUserInfo().getUuid(), null, true);
        template.setAuditDetails(auditDetails);
    }

    public void enrichOnUpdate(FieldPlanTemplate template, FieldPlanTemplate templateFromDb, RequestInfo requestInfo) {
        template.setAuditDetails(templateFromDb.getAuditDetails());
        AuditDetails auditDetails = fieldPlannerServiceUtil.getAuditDetails(
                requestInfo.getUserInfo().getUuid(), templateFromDb.getAuditDetails(), false);
        template.setAuditDetails(auditDetails);
    }
}
