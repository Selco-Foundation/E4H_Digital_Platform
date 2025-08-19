package org.egov.wf.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.wf.web.models.PauseState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
@Slf4j
public class PauseStateRepository {

    private JdbcTemplate jdbcTemplate;
    private static ObjectMapper objectMapper;

    @Autowired
    public PauseStateRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Finds pause state by businessId and businessService
     * @param businessId The business ID
     * @param businessService The business service
     * @return The pause state if found, null otherwise
     */
    public PauseState findByBusinessIdAndBusinessService(String businessId, String businessService) {
        String query = "SELECT * FROM eg_wf_pause_state WHERE businessId = ? AND businessService = ?";
        List<PauseState> results = jdbcTemplate.query(query, new PauseStateRowMapper(), businessId, businessService);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Finds all pause states for given business IDs and business service
     * @param businessIds List of business IDs
     * @param businessService The business service
     * @return List of pause states
     */
    public List<PauseState> findByBusinessIdsAndBusinessService(List<String> businessIds, String businessService) {
        if (businessIds == null || businessIds.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(businessIds.size(), "?"));
        String query = "SELECT * FROM eg_wf_pause_state WHERE businessId IN (" + placeholders + ") AND businessService = ?";
        
        List<Object> params = new ArrayList<>(businessIds);
        params.add(businessService);
        
        return jdbcTemplate.query(query, new PauseStateRowMapper(), params.toArray());
    }

    /**
     * Row mapper for PauseState
     */
    private static class PauseStateRowMapper implements RowMapper<PauseState> {
        @Override
        public PauseState mapRow(ResultSet rs, int rowNum) throws SQLException {
            return PauseState.builder()
                    .id(rs.getLong("id"))
                    .businessId(rs.getString("businessId"))
                    .businessService(rs.getString("businessService"))
                    .isPaused(rs.getBoolean("isPaused"))
                    .comments(rs.getString("comments") != null ? 
                            parseJson(rs.getString("comments")) : null)
                    .auditDetails(org.egov.wf.web.models.AuditDetails.builder()
                            .createdBy(rs.getString("createdBy"))
                            .lastModifiedBy(rs.getString("lastModifiedBy"))
                            .createdTime(rs.getLong("createdTime"))
                            .lastModifiedTime(rs.getLong("lastModifiedTime"))
                            .build())
                    .build();
        }
    }

    /**
     * Parse JSON string to Map
     * @param jsonString The JSON string to parse
     * @return Map representation of JSON
     */
    private static Map<String, Object> parseJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            log.error("Error parsing JSON: {}", jsonString, e);
            return null;
        }
    }
}
