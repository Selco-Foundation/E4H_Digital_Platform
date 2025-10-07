package org.egov.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.ProjectStaff;
import org.egov.repository.querybuilder.*;
import org.egov.repository.rowmapper.*;
import org.egov.service.EncryptionService;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.util.OrganisationConstant.ORGANISATION_ENCRYPT_KEY;

@Repository
@Slf4j
public class OrganisationUserRepository {

    private final OrganisationUserQueryBuilder queryBuilder;
    private final OrgUserRowMapper orgUserRowMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OrganisationUserRepository(OrganisationUserQueryBuilder queryBuilder, OrgUserRowMapper orgUserRowMapper, JdbcTemplate jdbcTemplate) {
        this.queryBuilder = queryBuilder;
        this.orgUserRowMapper = orgUserRowMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrgUser> getOrgUsers(OrgUserSearchRequest orgSearchRequest, URLParams urlParams) {
        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryDocument = queryBuilder.getOrganisationUserSearchQuery(orgSearchRequest, urlParams, preparedStmtListTarget, false);
        List<OrgUser> orgUserList = jdbcTemplate.query(queryDocument, orgUserRowMapper, preparedStmtListTarget.toArray());
        log.info("Fetched documents based on organisation Ids");
        return orgUserList;
    }


}
