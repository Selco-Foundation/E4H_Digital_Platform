package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class V20251014140000__populate_im_services_priority_from_mdms extends BaseJavaMigration {

    private static final String HOST_URL = System.getenv("EGOV_MDMS_HOST");

    private static final String MDMS_URL =  HOST_URL + "/egov-mdms-service/v1/_search";

    private static final List<String> TENANT_IDS = Arrays.asList("mz", "sk", "ml", "mn", "nl", "as", "gj", "or", "mh", "pg");

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("🚀 Starting migration: inserting im_services_priority data from MDMS");

        String insertSQL = """
                INSERT INTO im_services_priority (tenantId, incidentType, incidentSubType, systemFunctional, priority)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = context.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL)) {

            for (String tenantId : TENANT_IDS) {
                log.info("Processing tenant {}", tenantId);

                try {
                    JsonNode serviceDefs = fetchServiceDefs(tenantId);
                    if (serviceDefs != null && serviceDefs.isArray() && !serviceDefs.isEmpty()) {
                        for (JsonNode def : serviceDefs) {
                            String menuPath = def.path("menuPath").asText(null);
                            String serviceCode = def.path("serviceCode").asText(null);
                            String priority = def.path("priority").asText(null);

                            if (menuPath == null || serviceCode == null || priority == null) continue;

                            ps.setString(1, tenantId);
                            ps.setString(2, menuPath);
                            ps.setString(3, serviceCode);
                            ps.setNull(4, java.sql.Types.VARCHAR); // systemFunctional = NULL
                            ps.setString(5, priority.trim().toUpperCase());
                            ps.addBatch();
                        }
                        log.info("Added {} MDMS records for tenant {}", serviceDefs.size(), tenantId);
                    } else {
                        log.warn("No ServiceDefs found for tenant {}", tenantId);
                    }

                    // Add NON_FUNCTIONAL record for this tenant
                    ps.setString(1, tenantId);
                    ps.setNull(2, java.sql.Types.VARCHAR);
                    ps.setNull(3, java.sql.Types.VARCHAR);
                    ps.setString(4, "NON_FUNCTIONAL");
                    ps.setString(5, "HIGH");
                    ps.addBatch();

                    log.info("Added NON_FUNCTIONAL record for tenant {}", tenantId);

                } catch (Exception e) {
                    log.error("Error fetching/inserting for tenant {}: {}", tenantId, e.getMessage(), e);
                    throw e; // rethrow so Flyway can rollback automatically
                }
            }

            int[] results = ps.executeBatch();
            log.info("Migration complete — inserted {} records.", Arrays.stream(results).sum());
        }
    }

    private JsonNode fetchServiceDefs(String tenantId) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "RequestInfo", Map.of("authToken", ""),
                    "MdmsCriteria", Map.of(
                            "tenantId", tenantId,
                            "moduleDetails", List.of(
                                    Map.of(
                                            "moduleName", "Incident",
                                            "masterDetails", List.of(Map.of("name", "ServiceDefs"))
                                    )
                            )
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    MDMS_URL, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("MDMS fetch failed for tenant {} — status {}", tenantId, response.getStatusCode());
                return null;
            }

            JsonNode root = mapper.readTree(response.getBody());
            return root.path("MdmsRes").path("Incident").path("ServiceDefs");

        } catch (Exception e) {
            log.error("Exception while fetching MDMS for tenant {}: {}", tenantId, e.getMessage(), e);
            return null;
        }
    }
}
