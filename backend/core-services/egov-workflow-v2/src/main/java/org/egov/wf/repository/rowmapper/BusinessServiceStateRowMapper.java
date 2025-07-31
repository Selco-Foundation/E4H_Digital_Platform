package org.egov.wf.repository.rowmapper;


import org.egov.common.contract.request.User;
import org.egov.wf.web.models.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
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
                        .build();
            }
            bussinessServiceStateMap.put(id,businessServiceStateMigration);
        }
        return new ArrayList<>(bussinessServiceStateMap.values());
    }



}
