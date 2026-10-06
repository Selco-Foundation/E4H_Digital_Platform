package org.egov.field_planner.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.field_planner.repository.querybuilder.FieldPlannerQueryBuilder;
import org.egov.field_planner.web.models.ICCReportUploadResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class IccTemplateRepository {

    private JdbcTemplate jdbcTemplate;
    private final FieldPlannerQueryBuilder queryBuilder;

    public IccTemplateRepository(JdbcTemplate jdbcTemplate, FieldPlannerQueryBuilder queryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder;
    }

    public List<ICCReportUploadResponse> search(String systemType, String totalSystemCapacity) {

        log.info("Searching ICC templates. systemType='{}', totalSystemCapacity='{}'",
                systemType, totalSystemCapacity);

        List<Object> preparedStatement = new ArrayList<>();
        String sql = queryBuilder.getIccTemplateQuery(systemType, totalSystemCapacity, preparedStatement);

        log.info("Executing SQL: {}", sql);
        log.info("SQL Parameters: {}", preparedStatement);

        List<ICCReportUploadResponse> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> ICCReportUploadResponse.builder()
                        .id(rs.getString("id"))
                        .systemType(rs.getString("system_type"))
                        .totalSystemCapacity(rs.getString("total_system_capacity"))
                        .fileStoreId(rs.getString("filestoreid"))
                        .build(),
                preparedStatement.toArray()
        );

        log.info("Found {} ICC template(s).", results.size());

        if (log.isDebugEnabled()) {
            log.debug("Search results: {}", results);
        }

        return results;
    }
}
