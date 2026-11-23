package org.egov.hrms.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.hrms.model.AuditDetails;
import org.egov.hrms.model.JurisdictionRole;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps eg_hrms_jurisdiction_role rows into roles grouped by the jurisdiction they belong to, so
 * EmployeeRepository can hang them off each jurisdiction without a second pass over the rows.
 *
 * The name field is left null here; it is filled by the caller from the employee's user.roles,
 * since only the code is stored.
 */
@Component
@Slf4j
public class JurisdictionRoleRowMapper implements ResultSetExtractor<Map<String, List<JurisdictionRole>>> {

	@Override
	public Map<String, List<JurisdictionRole>> extractData(ResultSet rs) throws SQLException, DataAccessException {
		log.trace("JurisdictionRoleRowMapper.extractData invoked");
		Map<String, List<JurisdictionRole>> rolesByJurisdiction = new HashMap<>();
		int rowCount = 0;

		while(rs.next()) {
			rowCount++;
			String jurisdictionId = rs.getString("jurisdictionid");

			AuditDetails auditDetails = AuditDetails.builder()
					.createdBy(rs.getString("createdby"))
					.createdDate(rs.getLong("createddate"))
					.lastModifiedBy(rs.getString("lastmodifiedby"))
					.lastModifiedDate(rs.getLong("lastmodifieddate"))
					.build();

			JurisdictionRole role = JurisdictionRole.builder()
					.id(rs.getString("uuid"))
					.jurisdictionId(jurisdictionId)
					.code(rs.getString("rolecode"))
					.tenantId(rs.getString("tenantid"))
					.auditDetails(auditDetails)
					.build();

			rolesByJurisdiction.computeIfAbsent(jurisdictionId, key -> new ArrayList<>()).add(role);
		}

		log.debug("Jurisdiction role row mapper processed {} rows across {} jurisdiction(s)",
				rowCount, rolesByJurisdiction.size());
		return rolesByJurisdiction;
	}

}
