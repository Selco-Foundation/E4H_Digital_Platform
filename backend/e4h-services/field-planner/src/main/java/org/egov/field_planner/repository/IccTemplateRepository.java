package org.egov.field_planner.repository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.web.models.ICCReportUploadResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class IccTemplateRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<ICCReportUploadResponse> search(
            String systemType,
            String totalSystemCapacity) {

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM icc_templates WHERE 1=1 "
        );

        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotBlank(systemType)) {
            sql.append(" AND system_type = :systemType");
            params.put("systemType", systemType);
        }

        if (StringUtils.isNotBlank(totalSystemCapacity)) {
            sql.append(" AND total_system_capacity = :capacity");
            params.put("capacity", totalSystemCapacity);
        }

        return jdbcTemplate.query(
                sql.toString(),
                params,
                (rs, rowNum) ->
                        ICCReportUploadResponse.builder()
                                .id(rs.getString("id"))
                                .systemType(rs.getString("system_type"))
                                .totalSystemCapacity(rs.getString("total_system_capacity"))
                                .fileStoreId(rs.getString("filestoreid"))
                                .build()
        );
    }
}
