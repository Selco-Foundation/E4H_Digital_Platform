package org.egov.wf.repository.rowmapper;


import org.egov.common.contract.request.User;
import org.egov.wf.web.models.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Component
public class BusinessServiceStateRowMapper implements ResultSetExtractor<List<BusinessServiceStateMigration>> {


    /**
     * Converts resultset to List of processInstances
     * @param rs The resultSet from db query
     * @return List of ProcessInstances from the resultset
     * @throws SQLException
     * @throws DataAccessException
     */
    public List<BusinessServiceStateMigration> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String,BusinessServiceStateMigration> bussinessServiceStateMap = new LinkedHashMap<>();

        while (rs.next()){
            String id = rs.getString("state_uuid");
            BusinessServiceStateMigration businessServiceStateMigration = bussinessServiceStateMap.get(id);

            if(businessServiceStateMigration==null) {
                Long sla = rs.getLong("sla");
                if (rs.wasNull()) {
                    sla = null;
                }

                Long businessServiceSla = rs.getLong("businessservicesla");
                if (rs.wasNull()) {
                    businessServiceSla = null;
                }

                Boolean docUploadRequired = getBooleanOrNull(rs, "st_docuploadrequired");
                Boolean isStartState = getBooleanOrNull(rs, "st_isstartstate");
                Boolean isTerminateState = getBooleanOrNull(rs, "st_isterminatestate");
                Boolean isStateUpdatable = getBooleanOrNull(rs, "st_isstateupdatable");
                AuditDetails auditDetails = buildAuditDetails(rs);

                String businessServiceId = hasColumn(rs, "st_businessserviceid")
                        ? rs.getString("st_businessserviceid") : rs.getString("businessservice_uuid");
                State stateObject = State.builder()
                        .uuid(rs.getString("state_uuid"))
                        .tenantId(rs.getString("state_tenantid"))
                        .businessServiceId(businessServiceId)
                        .sla(sla)
                        .state(rs.getString("state"))
                        .applicationStatus(rs.getString("applicationstatus"))
                        .docUploadRequired(docUploadRequired)
                        .isStartState(isStartState)
                        .isTerminateState(isTerminateState)
                        .isStateUpdatable(isStateUpdatable)
                        .actions(new ArrayList<>())
                        .auditDetails(auditDetails)
                        .build();

                businessServiceStateMigration = BusinessServiceStateMigration.builder()
                        .businessService(rs.getString("businessservice"))
                        .moduleName(rs.getString("module_name"))
                        .tenantId(rs.getString("tenantid"))
                        .businessServiceUuid(rs.getString("businessservice_uuid"))
                        .businessServiceSla(rs.getString("businessservicesla"))
                        .stateUuid(rs.getString("state_uuid"))
                        .state(rs.getString("state"))
                        .applicationStatus(rs.getString("applicationstatus"))
                        .stateSla(sla)
                        .stateObject(stateObject)
                        .build();
            }
            addActionToState(rs, businessServiceStateMigration.getStateObject());
            bussinessServiceStateMap.put(id,businessServiceStateMigration);
        }
        return new ArrayList<>(bussinessServiceStateMap.values());
    }

    private void addActionToState(ResultSet rs, State stateObject) throws SQLException {
        if (stateObject == null || !hasColumn(rs, "act_uuid")) return;
        String actionUuid = rs.getString("act_uuid");
        if (actionUuid != null) {
            String roles = rs.getString("act_roles");
            Action action = Action.builder()
                    .uuid(actionUuid)
                    .tenantId(rs.getString("act_tenantid"))
                    .currentState(rs.getString("act_currentstate"))
                    .action(rs.getString("act_action"))
                    .nextState(rs.getString("act_nextstate"))
                    .roles(StringUtils.isEmpty(roles) ? Collections.emptyList() : Arrays.asList(roles.split(",")))
                    .active(rs.getBoolean("act_active"))
                    .build();
            stateObject.addActionsItem(action);
        }
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private Boolean getBooleanOrNull(ResultSet rs, String columnName) throws SQLException {
        if (!hasColumn(rs, columnName)) return null;
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    private AuditDetails buildAuditDetails(ResultSet rs) throws SQLException {
        if (!hasColumn(rs, "st_createdby")) return null;
        Long createdTime = rs.getLong("st_createdtime");
        if (rs.wasNull()) createdTime = null;
        Long lastModifiedTime = rs.getLong("st_lastmodifiedtime");
        if (rs.wasNull()) lastModifiedTime = null;
        return AuditDetails.builder()
                .createdBy(rs.getString("st_createdby"))
                .createdTime(createdTime)
                .lastModifiedBy(rs.getString("st_lastmodifiedby"))
                .lastModifiedTime(lastModifiedTime)
                .build();
    }

}
