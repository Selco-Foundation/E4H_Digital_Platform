package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.util.OrganisationUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        log.trace("OrganisationUserEnrichmentService::enrichOrgUserRequestOnCreate entry");
        orgUser.setId(UUID.randomUUID().toString());
        orgUser.setIsDeleted(false);
        log.debug("Generated organisation user ID: {}", orgUser.getId());

        AuditDetails auditDetails = organisationUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        orgUser.setAuditDetails(auditDetails);
        log.debug("Audit details set for organisation user: {}", orgUser.getId());
    }

    public void enrichOrgUserRequestOnDelete(DeleteOrgUserRequest request) {
        AuditDetails auditDetails = AuditDetails.builder().lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();;
        request.setAuditDetails(auditDetails);
        log.info("Enriched org user audit details for update " + request.getId());
    }

    public void enrichOrgUserRequestOnUpdate(OrgUserRequest request) {
        AuditDetails auditDetails = AuditDetails.builder().lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();;
        request.setAuditDetails(auditDetails);
        log.info("Enriched org user audit details for update " + request.getId());
    }
}
