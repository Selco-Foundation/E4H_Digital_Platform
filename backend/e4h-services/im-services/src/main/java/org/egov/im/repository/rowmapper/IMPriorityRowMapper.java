package org.egov.im.repository.rowmapper;

import org.egov.im.web.models.Priority;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IMPriorityRowMapper implements RowMapper<Priority> {

    @Override
    public Priority mapRow(ResultSet rs, int rowNum) throws SQLException {
        String priorityStr = rs.getString("priority");
        return Priority.fromString(priorityStr); // use your enum parsing method
    }
}
