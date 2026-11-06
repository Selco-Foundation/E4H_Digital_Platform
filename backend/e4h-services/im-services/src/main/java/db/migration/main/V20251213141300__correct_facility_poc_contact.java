package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class V20251213141300__correct_facility_poc_contact extends BaseJavaMigration {

    private static final int[] RETRY_DELAYS_SECONDS = {1, 7, 15, 25, 30};

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("🚀 Starting migration: correcting facility POC contact information");

        RestTemplate restTemplate = createRestTemplateWithTimeouts();
        ObjectMapper objectMapper = new ObjectMapper();

        // List to track skipped facilities
        List<SkippedFacility> skippedFacilities = new ArrayList<>();

        // Initialize migration log file
        String logFileName = "facility_poc_contact_correction_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        String logFilePath = "./logs/" + logFileName;
        String absoluteLogPath = Paths.get(logFilePath).toAbsolutePath().normalize().toString();
        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath, absoluteLogPath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("FACILITY POC CONTACT CORRECTION LOG");
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            // Get environment configurations
            String facilityHost = getEnvOrDefault("EGOV_FACILITY_HOST", "http://localhost:8080");
            String facilitySearchEndpoint = "/facility-service/v2/facility/search";
            String facilityUpdateEndpoint = "/facility-service/v2/facility/update";

            String hrmsHost = getEnvOrDefault("EGOV_HRMS_HOST", "http://localhost:8090");
            String hrmsSearchEndpoint = "/egov-hrms/employees/_search";

            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");

            // Fetch all entries from facility_tenant_id_map
            List<FacilityMappingEntry> facilityMappings = fetchFacilityMappings(context);
            log.info("Found {} facility mappings to process", facilityMappings.size());

            migrationLogger.println("Total Facilities to Process: " + facilityMappings.size());
            migrationLogger.println("----------------------------------------\n");
            migrationLogger.flush();

            int updatedCount = 0;
            int skippedCount = 0;

            for (FacilityMappingEntry entry : facilityMappings) {
                try {
                    // 1. Fetch user from HRMS with tenant_id and COMPLAINANT role
                    String mobileNumber = fetchUserMobileNumberFromHRMS(
                            restTemplate, objectMapper, hrmsHost + hrmsSearchEndpoint,
                            entry.tenantId, authToken
                    );

                    if (mobileNumber == null || mobileNumber.isEmpty()) {
                        logSkippedFacility(
                                migrationLogger, skippedFacilities, entry.tenantId, entry.facilityId,
                                "Unable to fetch mobile number from HRMS for COMPLAINANT role", null
                        );
                        skippedCount++;
                        continue;
                    }

                    // 2. Search facility with hfrId filter
                    JsonNode facility = searchFacilityByHfrId(
                            restTemplate, objectMapper, facilityHost + facilitySearchEndpoint,
                            entry.hfrOrNinId, authToken
                    );

                    if (facility == null) {
                        logSkippedFacility(
                                migrationLogger, skippedFacilities, entry.tenantId, entry.facilityId,
                                "Facility not found with hfrId: " + entry.hfrOrNinId, null
                        );
                        skippedCount++;
                        continue;
                    }

                    // 3. Update facility_details.pocContact with mobileNumber
                    Map<String, Object> updateRequest = buildFacilityUpdateRequest(
                            facility, mobileNumber, objectMapper
                    );

                    // 4. Make facility update call
                    boolean updated = updateFacility(
                            restTemplate, objectMapper, facilityHost + facilityUpdateEndpoint,
                            authToken, updateRequest, migrationLogger, skippedFacilities,
                            entry.tenantId, entry.facilityId
                    );

                    if (updated) {
                        updatedCount++;
                        log.info("✓ Updated POC contact for facility: {} (tenant: {})", entry.facilityId, entry.tenantId);
                    } else {
                        skippedCount++;
                    }

                } catch (Exception e) {
                    logSkippedFacility(
                            migrationLogger, skippedFacilities, entry.tenantId, entry.facilityId,
                            "Exception during processing: " + e.getMessage(), e.toString()
                    );
                    skippedCount++;
                }
            }

            // Print final summary
            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Total Facilities Processed: " + facilityMappings.size());
            migrationLogger.println("Total Facilities Updated: " + updatedCount);
            migrationLogger.println("Total Facilities Skipped: " + skippedCount);
            migrationLogger.println("\nCompleted at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            log.info("✅ Migration completed: Facility POC contact correction");
            log.info("Total facilities updated: {}", updatedCount);
            log.info("Total facilities skipped: {}", skippedCount);
            log.info("📝 Migration log file: {}", absoluteLogPath);

            migrationLogger.flush();
        }
    }

    private List<FacilityMappingEntry> fetchFacilityMappings(Context context) throws Exception {
        List<FacilityMappingEntry> mappings = new ArrayList<>();
        String query = "SELECT hfr_or_nin_id, tenant_id, facility_id, boundary_code FROM facility_tenant_id_map";

        try (var connection = context.getConfiguration().getDataSource().getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                FacilityMappingEntry entry = new FacilityMappingEntry(
                        resultSet.getString("hfr_or_nin_id"),
                        resultSet.getString("tenant_id"),
                        resultSet.getString("facility_id"),
                        resultSet.getString("boundary_code")
                );
                mappings.add(entry);
            }
        } catch (Exception e) {
            log.error("Error fetching facility mappings from database", e);
            throw e;
        }

        return mappings;
    }

    private String fetchUserMobileNumberFromHRMS(
            RestTemplate restTemplate, ObjectMapper objectMapper,
            String hrmsUrl, String tenantId, String authToken
    ) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "RequestInfo", buildRequestInfo(authToken, "egov.hrms")
            );

            // Build URL with query parameters
            String url = String.format("%s?isActive=true&roles=COMPLAINANT&tenantId=%s", hrmsUrl, tenantId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = executeWithRetry(
                    () -> restTemplate.postForEntity(url, httpEntity, String.class),
                    "fetch user mobile number from HRMS for tenant ID '" + tenantId + "'"
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode employees = root.get("Employees");

                if (employees != null && employees.isArray() && !employees.isEmpty()) {
                    JsonNode firstEmployee = employees.get(0);
                    JsonNode user = firstEmployee.get("user");
                    if (user != null && user.has("mobileNumber")) {
                        String mobileNumber = user.get("mobileNumber").asText();
                        log.debug("Found mobile number: {} for tenant: {}", mobileNumber, tenantId);
                        return mobileNumber;
                    }
                }
            }

            log.warn("No employee with COMPLAINANT role found or mobile number missing for tenant: {}", tenantId);
            return null;

        } catch (Exception e) {
            log.error("Error fetching user mobile number from HRMS for tenant: {}", tenantId, e);
            return null;
        }
    }

    private JsonNode searchFacilityByHfrId(
            RestTemplate restTemplate, ObjectMapper objectMapper,
            String facilityUrl, String hfrId, String authToken
    ) {
        try {
            // Build URL with query parameter
            String url = String.format("%s?hfrId=%s", facilityUrl, hfrId);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = executeWithRetry(
                    () -> restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class),
                    "search facility with hfrId '" + hfrId + "'"
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode facilities = root.get("facilities");

                if (facilities != null && facilities.isArray() && !facilities.isEmpty()) {
                    JsonNode facility = facilities.get(0);
                    log.debug("Found facility with hfrId: {}", hfrId);
                    return facility;
                }
            }

            log.warn("Facility not found with hfrId: {}", hfrId);
            return null;

        } catch (Exception e) {
            log.error("Error searching facility with hfrId: {}", hfrId, e);
            return null;
        }
    }

    private Map<String, Object> buildFacilityUpdateRequest(
            JsonNode facility, String mobileNumber, ObjectMapper objectMapper
    ) {
        Map<String, Object> updateRequest = new HashMap<>();

        // Copy existing facility data - using @JsonProperty names from DTO
        Map<String, Object> facilityUpdate = new HashMap<>();
        facilityUpdate.put("tenant_id", getField(facility, "tenant_id"));
        facilityUpdate.put("facilityId", getField(facility, "facility_id"));
        facilityUpdate.put("facility_type", getField(facility, "facility_type"));
        facilityUpdate.put("facility_subtype", getField(facility, "facility_subtype"));
        facilityUpdate.put("facility_name", getField(facility, "facility_name"));

        // Copy address if present - matching FacilityAddress DTO structure
        JsonNode addressNode = facility.get("address");
        if (addressNode != null && !addressNode.isNull()) {
            Map<String, Object> address = new HashMap<>();
            if (addressNode.has("tenantId") && !addressNode.get("tenantId").isNull()) {
                address.put("tenantId", addressNode.get("tenantId").asText());
            }
            if (addressNode.has("addressId") && !addressNode.get("addressId").isNull()) {
                address.put("addressId", addressNode.get("addressId").asText());
            }
            if (addressNode.has("addressNumber") && !addressNode.get("addressNumber").isNull()) {
                address.put("addressNumber", addressNode.get("addressNumber").asText());
            }
            if (addressNode.has("addressLine1") && !addressNode.get("addressLine1").isNull()) {
                address.put("addressLine1", addressNode.get("addressLine1").asText());
            }
            if (addressNode.has("addressLine2") && !addressNode.get("addressLine2").isNull()) {
                address.put("addressLine2", addressNode.get("addressLine2").asText());
            }
            if (addressNode.has("landmark") && !addressNode.get("landmark").isNull()) {
                address.put("landmark", addressNode.get("landmark").asText());
            }
            if (addressNode.has("doorNo") && !addressNode.get("doorNo").isNull()) {
                address.put("doorNo", addressNode.get("doorNo").asText());
            }
            if (addressNode.has("street") && !addressNode.get("street").isNull()) {
                address.put("street", addressNode.get("street").asText());
            }
            if (addressNode.has("city") && !addressNode.get("city").isNull()) {
                address.put("city", addressNode.get("city").asText());
            }
            if (addressNode.has("district") && !addressNode.get("district").isNull()) {
                address.put("district", addressNode.get("district").asText());
            }
            if (addressNode.has("state") && !addressNode.get("state").isNull()) {
                address.put("state", addressNode.get("state").asText());
            }
            if (addressNode.has("block") && !addressNode.get("block").isNull()) {
                address.put("block", addressNode.get("block").asText());
            }
            if (addressNode.has("pincode") && !addressNode.get("pincode").isNull()) {
                address.put("pincode", addressNode.get("pincode").asText());
            }
            if (addressNode.has("detail") && !addressNode.get("detail").isNull()) {
                address.put("detail", addressNode.get("detail").asText());
            }
            if (addressNode.has("locationAccuracy") && !addressNode.get("locationAccuracy").isNull()) {
                address.put("locationAccuracy", addressNode.get("locationAccuracy").asDouble());
            }
            if (addressNode.has("type") && !addressNode.get("type").isNull()) {
                address.put("type", addressNode.get("type").asText());
            }
            if (addressNode.has("buildingName") && !addressNode.get("buildingName").isNull()) {
                address.put("buildingName", addressNode.get("buildingName").asText());
            }
            if (addressNode.has("localityCode") && !addressNode.get("localityCode").isNull()) {
                address.put("localityCode", addressNode.get("localityCode").asText());
            }
            // Latitude and longitude should be direct fields, not nested in geoLocation
            if (addressNode.has("latitude") && !addressNode.get("latitude").isNull()) {
                address.put("latitude", addressNode.get("latitude").asDouble());
            }
            if (addressNode.has("longitude") && !addressNode.get("longitude").isNull()) {
                address.put("longitude", addressNode.get("longitude").asDouble());
            }
            facilityUpdate.put("address", address);
        }

        // Copy boundaryCode if present
        if (facility.has("boundaryCode") && !facility.get("boundaryCode").isNull()) {
            facilityUpdate.put("boundaryCode", facility.get("boundaryCode").asText());
        }

        // Copy isOnmReady if present
        if (facility.has("isOnmReady") && !facility.get("isOnmReady").isNull()) {
            facilityUpdate.put("isOnmReady", facility.get("isOnmReady").asBoolean());
        }

        // Update facility_details with new pocContact - using @JsonProperty names from HealthFacilityDetails DTO
        JsonNode facilityDetailsNode = facility.get("facility_details");
        Map<String, Object> facilityDetails = new HashMap<>();
        if (facilityDetailsNode != null && !facilityDetailsNode.isNull()) {
            // Copy existing facility_details fields
            if (facilityDetailsNode.has("hfr_id") && !facilityDetailsNode.get("hfr_id").isNull()) {
                facilityDetails.put("hfr_id", facilityDetailsNode.get("hfr_id").asText());
            }
            if (facilityDetailsNode.has("nin_id") && !facilityDetailsNode.get("nin_id").isNull()) {
                facilityDetails.put("nin_id", facilityDetailsNode.get("nin_id").asText());
            }
            if (facilityDetailsNode.has("solar_solution_design_type") && !facilityDetailsNode.get("solar_solution_design_type").isNull()) {
                facilityDetails.put("solar_solution_design_type", facilityDetailsNode.get("solar_solution_design_type").asText());
            }
            if (facilityDetailsNode.has("pocName") && !facilityDetailsNode.get("pocName").isNull()) {
                facilityDetails.put("pocName", facilityDetailsNode.get("pocName").asText());
            }
            if (facilityDetailsNode.has("pocDesignation") && !facilityDetailsNode.get("pocDesignation").isNull()) {
                facilityDetails.put("pocDesignation", facilityDetailsNode.get("pocDesignation").asText());
            }
        }
        // Update pocContact with the mobile number from HRMS
        facilityDetails.put("pocContact", mobileNumber);
        facilityUpdate.put("facility_details", facilityDetails);

        // Copy additionalDetails if present
        JsonNode additionalDetailsNode = facility.get("additionalDetails");
        if (additionalDetailsNode != null && !additionalDetailsNode.isNull()) {
            facilityUpdate.put("additionalDetails", objectMapper.convertValue(additionalDetailsNode, Map.class));
        }

        updateRequest.put("FacilityUpdate", facilityUpdate);

        return updateRequest;
    }

    private boolean updateFacility(
            RestTemplate restTemplate, ObjectMapper objectMapper, String facilityUrl,
            String authToken, Map<String, Object> updateRequest, PrintWriter migrationLogger,
            List<SkippedFacility> skippedFacilities, String tenantId, String facilityId
    ) {
        try {
            // Add RequestInfo to the update request
            Map<String, Object> request = new HashMap<>(updateRequest);
            request.put("RequestInfo", buildRequestInfo(authToken, "org.egov.facility"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

            ResponseEntity<String> response = executeWithRetry(
                    () -> restTemplate.postForEntity(facilityUrl, httpEntity, String.class),
                    "update facility with ID '" + facilityId + "'"
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Successfully updated facility: {}", facilityId);
                return true;
            } else {
                String errorMsg = "API returned status: " + response.getStatusCode();
                logSkippedFacility(
                        migrationLogger, skippedFacilities, tenantId, facilityId,
                        errorMsg, response.getBody()
                );
                return false;
            }

        } catch (HttpClientErrorException e) {
            String errorMsg = "API returned " + e.getStatusCode() + " - " + e.getStatusText();
            String responseBody = e.getResponseBodyAsString();

            log.error("Client error while updating facility {}: {} - {}", facilityId, errorMsg, responseBody);
            logSkippedFacility(
                    migrationLogger, skippedFacilities, tenantId, facilityId,
                    errorMsg, responseBody
            );
            return false;

        } catch (HttpServerErrorException e) {
            String errorMsg = "API returned " + e.getStatusCode() + " - " + e.getStatusText();
            String responseBody = e.getResponseBodyAsString();

            log.error("Server error while updating facility {}: {} - {}", facilityId, errorMsg, responseBody);
            logSkippedFacility(
                    migrationLogger, skippedFacilities, tenantId, facilityId,
                    errorMsg, responseBody
            );
            return false;

        } catch (Exception e) {
            log.error("Exception while updating facility: {}", e.getMessage(), e);
            logSkippedFacility(
                    migrationLogger, skippedFacilities, tenantId, facilityId,
                    "API call exception: " + e.getMessage(), e.toString()
            );
            return false;
        }
    }

    private PrintWriter initializeMigrationLogger(String logFilePath, String absolutePath) throws Exception {
        Files.createDirectories(Paths.get("./logs"));
        FileWriter fileWriter = new FileWriter(logFilePath, true);
        log.info("📝 Migration log file created: {}", absolutePath);
        return new PrintWriter(fileWriter, true);
    }

    private void logSkippedFacility(
            PrintWriter migrationLogger, List<SkippedFacility> skippedFacilities,
            String tenantId, String facilityId, String reason, String additionalInfo
    ) {
        SkippedFacility skipped = new SkippedFacility(tenantId, facilityId, reason, additionalInfo);
        skippedFacilities.add(skipped);

        migrationLogger.println("\n[SKIPPED]");
        migrationLogger.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        migrationLogger.println("Tenant ID: " + (tenantId != null ? tenantId : "N/A"));
        migrationLogger.println("Facility ID: " + (facilityId != null ? facilityId : "N/A"));
        migrationLogger.println("Reason: " + reason);
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            migrationLogger.println("Additional Info: " + additionalInfo);
        }
        migrationLogger.println();
        migrationLogger.flush();

        log.warn("✗ Skipped - {}: {} - {}", tenantId, facilityId, reason);
    }

    private Map<String, Object> buildRequestInfo(String authToken, String apiId) {
        // System User details
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 206);
        userInfo.put("uuid", "14d6dbdf-e4d2-45c3-9717-c82ba17a9f19");
        userInfo.put("userName", "SYSTEMUSER");
        userInfo.put("name", "System User");
        userInfo.put("mobileNumber", "1111112111");
        userInfo.put("emailId", "");
        userInfo.put("type", "EMPLOYEE");
        userInfo.put("active", true);
        userInfo.put("tenantId", "in");

        // Roles
        List<Map<String, Object>> roles = new ArrayList<>();
        roles.add(Map.of("name", "Employee", "code", "EMPLOYEE", "tenantId", "in"));
        roles.add(Map.of("name", "System user", "code", "SYSTEM", "tenantId", "in"));
        userInfo.put("roles", roles);

        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("apiId", apiId);
        requestInfo.put("ver", "1.0");
        long timestamp = System.currentTimeMillis();
        requestInfo.put("ts", timestamp);
        requestInfo.put("action", "create");
        requestInfo.put("msgId", timestamp + "|en_IN");
        requestInfo.put("authToken", authToken != null ? authToken : "");
        requestInfo.put("userInfo", userInfo);

        return requestInfo;
    }

    private String getField(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        String value = node.get(fieldName).asText();
        return (value != null && !value.isEmpty()) ? value : null;
    }

    private static String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    /**
     * Creates a RestTemplate with configured timeouts for internal service communication.
     * Uses HttpComponentsClientHttpRequestFactory for better timeout control.
     */
    @SuppressWarnings("deprecation")
    private RestTemplate createRestTemplateWithTimeouts() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setConnectionRequestTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        log.info("RestTemplate created with timeouts: 30s connect, 30s connection request, 60s read");
        return new RestTemplate(factory);
    }

    /**
     * Executes a supplier function with retry logic.
     * Retries with delays: 1, 7, 15, 25, 30 seconds if the first attempt fails.
     *
     * @param supplier The function to execute
     * @param operationName Name of the operation for logging purposes
     * @return The result from the supplier
     * @throws Exception If all retry attempts fail
     */
    private <T> T executeWithRetry(Supplier<T> supplier, String operationName) throws Exception {
        Exception lastException = null;

        // First attempt (no delay)
        try {
            return supplier.get();
        } catch (Exception e) {
            lastException = e;
            log.warn("Initial attempt to {} was unsuccessful. Error: {}. Will retry with exponential backoff.", operationName, e.getMessage());
        }

        // Retry with delays: 1, 7, 15, 25, 30 seconds
        int attemptNumber = 1;
        for (int delay : RETRY_DELAYS_SECONDS) {
            try {
                Thread.sleep(delay * 1000L);
                log.info("Retrying {} (attempt {}) after waiting {} seconds...", operationName, attemptNumber + 1, delay);
                return supplier.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new Exception("Retry operation was interrupted for " + operationName + " during attempt " + (attemptNumber + 1), ie);
            } catch (Exception e) {
                lastException = e;
                log.warn("Retry attempt {} for {} failed after {} seconds. Error: {}. Will continue with next retry.",
                        attemptNumber + 1, operationName, delay, e.getMessage());
                attemptNumber++;
            }
        }

        // All attempts failed
        int totalDelaySeconds = Arrays.stream(RETRY_DELAYS_SECONDS).sum();
        log.error("All retry attempts exhausted for {}. Operation failed after {} total retry attempts (total wait time: {} seconds). Last error: {}",
                operationName, RETRY_DELAYS_SECONDS.length + 1, totalDelaySeconds, lastException != null ? lastException.getMessage() : "Unknown");
        throw new Exception(String.format("Failed to complete %s after %d attempts (total wait time: %d seconds)",
                operationName, RETRY_DELAYS_SECONDS.length + 1, totalDelaySeconds), lastException);
    }

    // Helper classes
    private static class FacilityMappingEntry {
        final String hfrOrNinId;
        final String tenantId;
        final String facilityId;
        final String boundaryCode;

        FacilityMappingEntry(String hfrOrNinId, String tenantId, String facilityId, String boundaryCode) {
            this.hfrOrNinId = hfrOrNinId;
            this.tenantId = tenantId;
            this.facilityId = facilityId;
            this.boundaryCode = boundaryCode;
        }
    }

    private static class SkippedFacility {
        String tenantId;
        String facilityId;
        String reason;
        String additionalInfo;
        String timestamp;

        SkippedFacility(String tenantId, String facilityId, String reason, String additionalInfo) {
            this.tenantId = tenantId;
            this.facilityId = facilityId;
            this.reason = reason;
            this.additionalInfo = additionalInfo;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
