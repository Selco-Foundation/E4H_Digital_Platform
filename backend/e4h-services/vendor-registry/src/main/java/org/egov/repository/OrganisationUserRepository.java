package org.egov.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private record OrgUserLinkRow(String id, boolean deleted, long lastModifiedTime) {}

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
        log.trace("OrganisationUserRepository::getOrgUsers entry");
        String tenantId = urlParams != null ? urlParams.getTenantId() : "unknown";
        log.info("Starting organisation user search query for tenant: {}", tenantId);

        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryDocument = queryBuilder.getOrganisationUserSearchQuery(orgSearchRequest, urlParams, preparedStmtListTarget, false);
        List<OrgUser> orgUserList = jdbcTemplate.query(queryDocument, orgUserRowMapper, preparedStmtListTarget.toArray());
        log.info("Fetched documents based on organisation Ids");
        List<OrgUser> orgUserEnricheds = new ArrayList<>();
        for (OrgUser orgUser: orgUserList){
            OrgUser enriched = OrgUser.builder()
                    .userId(orgUser.getUserId())
                    .tenantId(orgUser.getTenantId())
                    .organizationId(orgUser.getOrganizationId())
                    .id(orgUser.getId())
                    .auditDetails(orgUser.getAuditDetails())
                    .additionalDetails(orgUser.getAdditionalDetails())
                    .isDeleted(orgUser.getIsDeleted())
                    .build();
            Employee employee = hrmsUtils.getUserById(orgSearchRequest, orgUser.getUserId());
            if(employee != null){
                enriched.setUser(employee.getUser());
                enriched.getUser().setJurisdictions(employee.getJurisdictions());
            }
            orgUserEnricheds.add(enriched);
        }
        return orgUserEnricheds;
    }

    public Integer getOrganisationsCount(OrgUserSearchRequest orgSearchRequest) {
        List<Object> preparedStatement = new ArrayList<>();
        URLParams urlParams = URLParams.builder().build();
        String queryDocument = queryBuilder.getOrganisationUserSearchQuery(orgSearchRequest, urlParams, preparedStatement, true);
        if (queryDocument == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(queryDocument, preparedStatement.toArray(), Integer.class);
        log.info("Total organisation user count is : " + count);
        return count;
    }

    /**
     * Org-user "delete" is soft-delete on {@code eg_org_user} only; HRMS employee typically stays active, so HRMS
     * search by phone still returns the user. This method sets {@code isdeleted = false} on the canonical row for
     * the given HRMS user + organization (and removes duplicate soft-deleted rows) so the usual active-only org-user
     * query used next can see the link — same flow as a never-deleted link.
     */
    public Optional<String> ensureActiveOrgUserLinkOrReactivateDeleted(String userId, String organizationId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(organizationId)) {
            return Optional.empty();
        }
        String select = "SELECT id, isdeleted, lastmodifiedtime FROM eg_org_user WHERE userid = ? AND organizationid = ?";
        List<OrgUserLinkRow> rows = jdbcTemplate.query(select, (rs, i) -> new OrgUserLinkRow(
                rs.getString("id"),
                rs.getBoolean("isdeleted"),
                rs.getLong("lastmodifiedtime")), userId, organizationId);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Optional<OrgUserLinkRow> active = rows.stream().filter(r -> !r.deleted()).findFirst();
        if (active.isPresent()) {
            String keepId = active.get().id();
            int removed = jdbcTemplate.update(
                    "DELETE FROM eg_org_user WHERE userid = ? AND organizationid = ? AND id <> ? AND isdeleted = true",
                    userId, organizationId, keepId);
            if (removed > 0) {
                log.info("ensureActiveOrgUserLinkOrReactivateDeleted: removed {} soft-deleted duplicate(s); active id={}",
                        removed, keepId);
            }
            return Optional.of(keepId);
        }
        OrgUserLinkRow keep = rows.stream()
                .max(Comparator.comparingLong(OrgUserLinkRow::lastModifiedTime).thenComparing(OrgUserLinkRow::id))
                .orElseThrow();
        long now = System.currentTimeMillis();
        for (OrgUserLinkRow r : rows) {
            if (!r.id().equals(keep.id())) {
                jdbcTemplate.update("DELETE FROM eg_org_user WHERE id = ? AND isdeleted = true", r.id());
            }
        }
        int updated = jdbcTemplate.update(
                "UPDATE eg_org_user SET isdeleted = false, lastmodifiedtime = ? WHERE id = ?",
                now, keep.id());
        log.info("ensureActiveOrgUserLinkOrReactivateDeleted: set isdeleted=false on org_user id={}, rowsUpdated={}",
                keep.id(), updated);
        return Optional.of(keep.id());
    }

}
