package facility.repository;

import facility.web.models.AuditDetails;
import facility.web.models.MobileChangeRequest;
import facility.web.models.MobileChangeRequestSearchCriteria;
import facility.web.models.MobileChangeRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Synchronous JDBC access to {@code facility_mobile_change_request}.
 *
 * <p>Writes deliberately bypass the persister: "one pending request per user" is enforced by a
 * partial unique index, and status moves are conditional updates ({@code WHERE status =
 * 'PENDING'}), both of which need the caller to see the outcome before it answers.
 *
 * <p>Mobile numbers go in and come out exactly as handed over - encryption is the service's job.
 */
@Repository
@RequiredArgsConstructor
public class MobileChangeRequestRepository {

    private static final String TABLE = "facility_mobile_change_request";

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<MobileChangeRequest> ROW_MAPPER = (rs, rowNum) -> MobileChangeRequest.builder()
            .id(rs.getString("id"))
            .tenantId(rs.getString("tenant_id"))
            .userUuid(rs.getString("user_uuid"))
            .userName(rs.getString("user_name"))
            .facilityId(rs.getString("facility_id"))
            .facilityName(rs.getString("facility_name"))
            .boundaryCode(rs.getString("boundary_code"))
            .currentMobileNumber(rs.getString("current_mobile_number"))
            .newMobileNumber(rs.getString("new_mobile_number"))
            .status(MobileChangeRequestStatus.valueOf(rs.getString("status")))
            .comment(rs.getString("comment"))
            .actionBy(rs.getString("action_by"))
            .actionTime((Long) rs.getObject("action_time"))
            .auditDetails(AuditDetails.builder()
                    .createdBy(rs.getString("created_by"))
                    .createdDate((Long) rs.getObject("created_time"))
                    .lastModifiedBy(rs.getString("last_modified_by"))
                    .lastModifiedDate((Long) rs.getObject("last_modified_time"))
                    .build())
            .build();

    /**
     * @throws org.springframework.dao.DuplicateKeyException when the user already has a pending
     *         request (partial unique index {@code uniq_fmcr_user_pending})
     */
    public void insert(MobileChangeRequest request) {
        AuditDetails audit = request.getAuditDetails();
        jdbcTemplate.update("INSERT INTO " + TABLE + " (id, tenant_id, user_uuid, user_name, facility_id, facility_name,"
                        + " boundary_code, current_mobile_number, new_mobile_number, status, created_by, created_time,"
                        + " last_modified_by, last_modified_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                request.getId(), request.getTenantId(), request.getUserUuid(), request.getUserName(),
                request.getFacilityId(), request.getFacilityName(), request.getBoundaryCode(),
                request.getCurrentMobileNumber(), request.getNewMobileNumber(), request.getStatus().name(),
                audit.getCreatedBy(), audit.getCreatedDate(), audit.getLastModifiedBy(), audit.getLastModifiedDate());
    }

    /**
     * Moves a request out of {@code PENDING}. Returns false when it was no longer pending, i.e.
     * someone else (the user cancelling, another CRM deciding) got there first.
     */
    public boolean closePending(String id, MobileChangeRequestStatus newStatus, String comment, String actionBy, long actionTime) {
        int rows = jdbcTemplate.update("UPDATE " + TABLE + " SET status = ?, comment = ?, action_by = ?, action_time = ?,"
                        + " last_modified_by = ?, last_modified_time = ? WHERE id = ? AND status = ?",
                newStatus.name(), comment, actionBy, actionTime, actionBy, actionTime, id,
                MobileChangeRequestStatus.PENDING.name());
        return rows > 0;
    }

    public MobileChangeRequest findById(String id) {
        List<MobileChangeRequest> rows = jdbcTemplate.query("SELECT * FROM " + TABLE + " WHERE id = ?", ROW_MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean hasPending(String userUuid) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + TABLE + " WHERE user_uuid = ? AND status = ?",
                Integer.class, userUuid, MobileChangeRequestStatus.PENDING.name());
        return count != null && count > 0;
    }

    /** Newest first. */
    public List<MobileChangeRequest> search(MobileChangeRequestSearchCriteria criteria, int limit, int offset) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(criteria, params);
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query("SELECT * FROM " + TABLE + where + " ORDER BY created_time DESC, id LIMIT ? OFFSET ?",
                ROW_MAPPER, params.toArray());
    }

    public int count(MobileChangeRequestSearchCriteria criteria) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(criteria, params);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + TABLE + where, Integer.class, params.toArray());
        return count == null ? 0 : count;
    }

    /**
     * The facility a POC user belongs to, by the {@code Facility} boundary of their HRMS
     * jurisdiction. More than one row is a data problem the caller must not resolve by guessing.
     */
    public List<Map<String, Object>> findFacilitiesByBoundaryCode(String boundaryCode) {
        return jdbcTemplate.queryForList(
                "SELECT id, tenant_id, facility_name FROM facility WHERE boundary_code = ?", boundaryCode);
    }

    /** Writes the facility's POC phone (already encrypted by the caller). */
    public int updateFacilityPocPhone(String facilityId, String tenantId, String encryptedPhone, String updatedBy, long updatedAt) {
        return jdbcTemplate.update(
                "UPDATE facility SET facility_poc_phone = ?, updated_by = ?, updated_at = ? WHERE id = ? AND tenant_id = ?",
                encryptedPhone, updatedBy, updatedAt, facilityId, tenantId);
    }

    private static String buildWhere(MobileChangeRequestSearchCriteria criteria, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (criteria == null) {
            return where.toString();
        }
        if (criteria.getTenantId() != null && !criteria.getTenantId().isBlank()) {
            where.append(" AND tenant_id = ?");
            params.add(criteria.getTenantId());
        }
        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            where.append(" AND id IN (").append(placeholders(criteria.getIds().size())).append(")");
            params.addAll(criteria.getIds());
        }
        if (criteria.getUserUuid() != null && !criteria.getUserUuid().isBlank()) {
            where.append(" AND user_uuid = ?");
            params.add(criteria.getUserUuid());
        }
        if (criteria.getFacilityId() != null && !criteria.getFacilityId().isBlank()) {
            where.append(" AND facility_id = ?");
            params.add(criteria.getFacilityId());
        }
        if (!CollectionUtils.isEmpty(criteria.getStatus())) {
            where.append(" AND status IN (").append(placeholders(criteria.getStatus().size())).append(")");
            criteria.getStatus().forEach(s -> params.add(s.name()));
        }
        return where.toString();
    }

    private static String placeholders(int n) {
        return String.join(", ", Collections.nCopies(n, "?"));
    }
}
