package org.egov.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.core.URLParams;
import org.egov.repository.querybuilder.*;
import org.egov.repository.rowmapper.*;
import org.egov.util.HRMSUtils;
import org.egov.util.OrganisationUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class OrganisationUserRepository {

    private final OrganisationUserQueryBuilder queryBuilder;
    private final OrgUserRowMapper orgUserRowMapper;
    private final JdbcTemplate jdbcTemplate;

    private final HRMSUtils hrmsUtils;

    @Autowired
    public OrganisationUserRepository(OrganisationUserQueryBuilder queryBuilder, OrgUserRowMapper orgUserRowMapper, JdbcTemplate jdbcTemplate, HRMSUtils hrmsUtils) {
        this.queryBuilder = queryBuilder;
        this.orgUserRowMapper = orgUserRowMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.hrmsUtils = hrmsUtils;
    }

    public List<OrgUser> getOrgUsers(OrgUserSearchRequest orgSearchRequest, URLParams urlParams) {
        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryDocument = queryBuilder.getOrganisationUserSearchQuery(orgSearchRequest, urlParams, preparedStmtListTarget, false);
        List<OrgUser> orgUserList = jdbcTemplate.query(queryDocument, orgUserRowMapper, preparedStmtListTarget.toArray());
        log.info("Fetched documents based on organisation Ids");
        List<OrgUser> orgUserEnricheds = new ArrayList<>();
        for (OrgUser orgUser: orgUserList){
            Employee employee = hrmsUtils.getUserById(orgSearchRequest, orgUser.getUserId());
            if(employee == null)
                continue;
            List<String> jurisdiction = employee.getJurisdictions().stream().map(Jurisdiction::getBoundary).collect(Collectors.toList());
            User user = employee.getUser();
            user.setJurisdiction(jurisdiction);
            OrgUser enriched = OrgUser.builder()
                    .user(employee.getUser())
                    .userId(orgUser.getUserId())
                    .tenantId(orgUser.getTenantId())
                    .organizationId(orgUser.getOrganizationId())
                    .id(orgUser.getId())
                    .auditDetails(orgUser.getAuditDetails())
                    .additionalDetails(orgUser.getAdditionalDetails())
                    .isDeleted(orgUser.getIsDeleted())
                    .build();
            orgUserEnricheds.add(enriched);
        }
        return orgUserEnricheds;
    }


}
