package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.web.models.OrgServiceRequest;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class OrgService {


    public OrgServiceRequest createOrganisationWithWorkFlow(OrgServiceRequest orgServiceRequest) {
        log.trace("OrgService::createOrganisationWithWorkFlow entry");
        log.warn("createOrganisationWithWorkFlow method is not implemented, returning request as-is");
        return orgServiceRequest;
    }

    public OrgServiceRequest updateOrganisationWithWorkFlow(OrgServiceRequest orgServiceRequest) {
        log.trace("OrgService::updateOrganisationWithWorkFlow entry");
        log.warn("updateOrganisationWithWorkFlow method is not implemented, returning request as-is");
        return orgServiceRequest;
    }

}
