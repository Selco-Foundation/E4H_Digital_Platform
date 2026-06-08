package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Backfills existing project names to revised format via project-service APIs.
*/
@Slf4j
public class V20260604140000__migrate_project_names_to_revised_format extends BaseJavaMigration {

    private static final Pattern REVISED_PROJECT_ID_PATTERN =
            Pattern.compile("^([A-Z]{2})-(\\d{4})-(\\d+)-([0-9]+(-[0-9]+)*)$");
    private static final String TENANT_ID = "in";
    private static final String MIGRATION_USER_UUID = "2be2bec7-908d-4984-8368-cecda98fb961";
    private static final String DEFAULT_SUB_PROJECT_TYPE_ID = "PROJECT";
    private static final int SEARCH_LIMIT = 100;
    private static final long DELAY_BETWEEN_UPDATES_MS = 50L;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private String projectHost;
    private String projectSearchEndpoint;
    private String projectUpdateEndpoint;
    private String authToken;
    private String subProjectTypeId;
    private ObjectNode requestInfo;

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        initializeEnv();
        requestInfo = buildRequestInfoBody();

        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        String logFileName = "project_name_migration_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        int migrated = 0;
        int skipped = 0;
        int scanned = 0;
        int alreadyRevised = 0;
        List<String> failures = new ArrayList<>();

        try (PrintWriter migrationLogger = new PrintWriter(Files.newBufferedWriter(
                logFilePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {

            migrationLogger.println("PROJECT NAME MIGRATION LOG (API)");
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("Project host: " + projectHost);
            migrationLogger.println("Log file: " + logFilePath);
            migrationLogger.println("----------------------------------------");
            migrationLogger.flush();

            log.info("Migrating project names for tenant {}", TENANT_ID);
            migrationLogger.println("Tenant: " + TENANT_ID);
            migrationLogger.println("Search filter: subProjectTypeId=" + subProjectTypeId);
            migrationLogger.flush();

            int[] counts = processTenant(TENANT_ID, migrationLogger, failures);
            migrated = counts[0];
            skipped = counts[1];
            scanned = counts[2];
            alreadyRevised = counts[3];

            migrationLogger.println("----------------------------------------");
            migrationLogger.printf("Scanned: %d%n", scanned);
            migrationLogger.printf("Already revised format: %d%n", alreadyRevised);
            migrationLogger.printf("Migrated: %d%n", migrated);
            migrationLogger.printf("Skipped: %d%n", skipped);
            migrationLogger.println("Completed at: " + LocalDateTime.now());
            if (!failures.isEmpty()) {
                migrationLogger.println("Failures:");
                failures.forEach(failure -> migrationLogger.println("  " + failure));
            }
            migrationLogger.flush();
        }

        log.info("Project name migration completed. scanned={}, alreadyRevised={}, migrated={}, skipped={}, log={}",
                scanned, alreadyRevised, migrated, skipped, logFilePath);
    }

    private int[] processTenant(String tenantId, PrintWriter migrationLogger, List<String> failures) {
        int migrated = 0;
        int skipped = 0;
        int scanned = 0;
        int alreadyRevised = 0;
        int offset = 0;
        Integer totalCount = null;

        while (true) {
            JsonNode searchResponse = searchProjects(tenantId, offset);
            if (searchResponse == null) {
                log.warn("Project search returned no response for tenant {}", tenantId);
                break;
            }

            if (totalCount == null && searchResponse.has("totalCount") && !searchResponse.get("totalCount").isNull()) {
                totalCount = searchResponse.get("totalCount").asInt();
                migrationLogger.printf("Tenant %s: total projects=%d%n", tenantId, totalCount);
                migrationLogger.flush();
            }

            JsonNode projects = extractProjectsArray(searchResponse);
            if (projects.isEmpty()) {
                break;
            }

            for (JsonNode wrapper : projects) {
                JsonNode project = extractProjectNode(wrapper);
                if (project == null) {
                    continue;
                }
                scanned++;
                String projectId = textOrNull(project, "id");
                String currentName = textOrNull(project, "name");

                if (isAlreadyRevisedFormat(currentName)) {
                    alreadyRevised++;
                    continue;
                }

                try {
                    ObjectNode updatePayload = buildUpdatePayload(project);
                    JsonNode updateResponse = updateProject(updatePayload);
                    String newName = extractNameFromUpdateResponse(updateResponse, projectId);
                    migrated++;
                    log.info("Migrated project {} (tenant {}) name: {} -> {}", projectId, tenantId, currentName, newName);
                    migrationLogger.printf("MIGRATED projectId=%s tenantId=%s oldName=%s newName=%s%n",
                            projectId, tenantId, currentName, newName);
                    Thread.sleep(DELAY_BETWEEN_UPDATES_MS);
                } catch (Exception e) {
                    skipped++;
                    String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    failures.add(projectId + " (" + tenantId + "): " + reason);
                    migrationLogger.printf("SKIPPED projectId=%s tenantId=%s currentName=%s reason=%s%n",
                            projectId, tenantId, currentName, reason);
                    migrationLogger.flush();
                    log.warn("Failed to migrate project {} in tenant {}: {}", projectId, tenantId, reason);
                }
            }

            offset += projects.size();
            if (totalCount != null && offset >= totalCount) {
                break;
            }
            if (projects.size() < SEARCH_LIMIT) {
                break;
            }
        }
        migrationLogger.printf("Tenant %s summary: scanned=%d, alreadyRevised=%d, migrated=%d, skipped=%d%n",
                tenantId, scanned, alreadyRevised, migrated, skipped);
        migrationLogger.flush();
        return new int[] {migrated, skipped, scanned, alreadyRevised};
    }

    private JsonNode searchProjects(String tenantId, int offset) {
        String url = UriComponentsBuilder.fromHttpUrl(projectHost + projectSearchEndpoint)
                .queryParam("tenantId", tenantId)
                .queryParam("limit", SEARCH_LIMIT)
                .queryParam("offset", offset)
                .queryParam("includeAncestors", false)
                .queryParam("includeDescendants", false)
                .toUriString();

        ObjectNode request = objectMapper.createObjectNode();
        request.set("RequestInfo", requestInfo.deepCopy());
        ObjectNode projectCriteria = objectMapper.createObjectNode();
        projectCriteria.put("subProjectTypeId", subProjectTypeId);
        request.set("Project", projectCriteria);

        return postForJson(url, request);
    }

    private JsonNode extractProjectsArray(JsonNode searchResponse) {
        JsonNode projects = searchResponse.path("Project");
        if (projects.isArray()) {
            return projects;
        }
        projects = searchResponse.path("project");
        return projects.isArray() ? projects : objectMapper.createArrayNode();
    }

    private JsonNode extractProjectNode(JsonNode wrapper) {
        if (wrapper == null || wrapper.isNull()) {
            return null;
        }
        if (wrapper.has("project") && !wrapper.get("project").isNull()) {
            return wrapper.get("project");
        }
        return wrapper.has("id") ? wrapper : null;
    }

    private JsonNode updateProject(ObjectNode updatePayload) {
        String url = projectHost + projectUpdateEndpoint;
        ObjectNode request = objectMapper.createObjectNode();
        request.set("RequestInfo", requestInfo.deepCopy());
        ArrayNode projects = objectMapper.createArrayNode();
        projects.add(updatePayload);
        request.set("Projects", projects);
        return postForJson(url, request);
    }

    private ObjectNode buildUpdatePayload(JsonNode project) {
        ObjectNode updateProject = objectMapper.createObjectNode();
        copyTextField(project, updateProject, "id");
        copyTextField(project, updateProject, "tenantId");
        copyLongField(project, updateProject, "startDate");
        copyLongField(project, updateProject, "endDate");

        if (project.has("address") && !project.get("address").isNull()) {
            updateProject.set("address", buildAddressPayload(project.get("address")));
        }

        if (project.has("additionalDetails") && !project.get("additionalDetails").isNull()) {
            ObjectNode additionalDetails = project.get("additionalDetails").deepCopy();
            additionalDetails.remove("legacyProject");
            updateProject.set("additionalDetails", additionalDetails);
        }

        return updateProject;
    }

    private ObjectNode buildAddressPayload(JsonNode address) {
        ObjectNode addressPayload = objectMapper.createObjectNode();
        copyTextField(address, addressPayload, "id");
        copyTextField(address, addressPayload, "tenantId");
        copyTextField(address, addressPayload, "boundary");
        copyTextField(address, addressPayload, "boundaryType");
        return addressPayload;
    }

    private String extractNameFromUpdateResponse(JsonNode response, String projectId) {
        if (response == null) {
            return null;
        }
        JsonNode projects = response.path("Project");
        if (projects.isArray()) {
            for (JsonNode project : projects) {
                if (projectId.equals(textOrNull(project, "id"))) {
                    return textOrNull(project, "name");
                }
            }
            if (!projects.isEmpty()) {
                return textOrNull(projects.get(0), "name");
            }
        }
        return null;
    }

    private JsonNode postForJson(String url, JsonNode body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("API call failed with status " + response.getStatusCode());
            }
            return objectMapper.readTree(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalStateException("API error " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new IllegalStateException("API call failed for " + url + ": " + e.getMessage(), e);
        }
    }

    private boolean isAlreadyRevisedFormat(String name) {
        return name != null && !name.isBlank()
                && REVISED_PROJECT_ID_PATTERN.matcher(name.trim().toUpperCase(Locale.ROOT)).matches();
    }

    private void initializeEnv() {
        projectHost = trimTrailingSlash(getEnvOrDefault("EGOV_PROJECT_HOST", "http://localhost:8080"));
        projectSearchEndpoint = getEnvOrDefault("EGOV_PROJECT_SEARCH_ENDPOINT", "/project/v2/_search");
        projectUpdateEndpoint = getEnvOrDefault("EGOV_PROJECT_UPDATE_ENDPOINT", "/project/v1/_update");
        authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");
        subProjectTypeId = getEnvOrDefault("EGOV_PROJECT_SEARCH_SUB_PROJECT_TYPE_ID", DEFAULT_SUB_PROJECT_TYPE_ID);
    }

    private void addRole(ArrayNode roles, String name, String code) {
        ObjectNode role = objectMapper.createObjectNode();
        role.put("name", name);
        role.put("code", code);
        role.put("tenantId", TENANT_ID);
        roles.add(role);
    }

    private ObjectNode buildRequestInfoBody() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        userInfo.put("uuid", MIGRATION_USER_UUID);
        userInfo.put("tenantId", TENANT_ID);
        userInfo.put("active", true);
        ArrayNode roles = objectMapper.createArrayNode();
        addRole(roles, "Employee", "EMPLOYEE");
        addRole(roles, "Project manager", "PROJECT_MANAGER");
        userInfo.set("roles", roles);

        ObjectNode requestInfoNode = objectMapper.createObjectNode();
        requestInfoNode.put("apiId", "Rainmaker");
        requestInfoNode.put("authToken", authToken);
        requestInfoNode.set("userInfo", userInfo);
        return requestInfoNode;
    }

    private static void copyTextField(JsonNode source, ObjectNode target, String field) {
        String value = textOrNull(source, field);
        if (value != null) {
            target.put(field, value);
        }
    }

    private static void copyLongField(JsonNode source, ObjectNode target, String field) {
        if (source.has(field) && !source.get(field).isNull()) {
            target.put(field, source.get(field).asLong());
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String getEnvOrDefault(String key, String def) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? def : value;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

}
