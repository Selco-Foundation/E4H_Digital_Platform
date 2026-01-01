package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.util.IdgenUtil;
import org.egov.util.OrganisationUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrganisationUserEnrichmentService {

    private final OrganisationUtil organisationUtil;

    @Autowired
    public OrganisationUserEnrichmentService(OrganisationUtil organisationUtil) {
        this.organisationUtil = organisationUtil;
    }

    /* Enrich OrgUser with id and audit details */
    public void enrichOrgUserRequestOnCreate(OrgUserRequest orgUser, RequestInfo requestInfo) {
        orgUser.setId(UUID.randomUUID().toString());
        log.info("fieldPlan id set to " + orgUser.getId());
        AuditDetails auditDetails = organisationUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        orgUser.setAuditDetails(auditDetails);
    }

    public void enrichOrgUserRequestOnUpdate(OrgUserRequest request) {
        AuditDetails auditDetails = AuditDetails.builder().lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();;
        request.setAuditDetails(auditDetails);
        log.info("Enriched org user audit details for update " + request.getId());
    }
}
