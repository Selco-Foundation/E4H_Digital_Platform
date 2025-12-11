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
import java.util.stream.Collectors;

@Slf4j
public class V20251103170700__migrate_incident_facilities extends BaseJavaMigration {

    // Map of tenant codes to state names
    private static final Map<String, String> TENANT_TO_STATE;

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("as", "Assam");
        m.put("ml", "Meghalaya");
        m.put("mn", "Manipur");
        m.put("nl", "Nagaland");
        m.put("sk", "Sikkim");
        m.put("gj", "Gujarat");
        m.put("mz", "Mizoram");
        m.put("or", "Odisha");
        m.put("pg", "Karnataka");
        m.put("mh", "Maharashtra");
        TENANT_TO_STATE = Collections.unmodifiableMap(m);
    }

    private static final int[] RETRY_DELAYS_SECONDS = {1, 7, 15, 25, 30};

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("🚀 Starting migration: migrating facilities from MDMS to facility table");

        RestTemplate restTemplate = createRestTemplateWithTimeouts();
        ObjectMapper objectMapper = new ObjectMapper();

        // Map to track facility mappings: nin_id -> FacilityMapping
        Map<String, FacilityMapping> facilityMappings = new HashMap<>();

        // List to track skipped facilities
        List<SkippedFacility> skippedFacilities = new ArrayList<>();

        // Initialize migration log file
        String logFileName = "facility_migration_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        String logFilePath = "./logs/" + logFileName;
        String absoluteLogPath = Paths.get(logFilePath).toAbsolutePath().normalize().toString();
        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath, absoluteLogPath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("FACILITY MIGRATION LOG");
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            // Get environment configurations
            String mdmsHost = getEnvOrDefault("EGOV_MDMS_HOST", "http://localhost:8094");
            String mdmsSearchEndpoint = "/egov-mdms-service/v1/_search";

            String facilityHost = getEnvOrDefault("EGOV_FACILITY_HOST", "http://localhost:8080");
            String facilityCreateEndpoint = "/facility-service/v2/facility/create";

            String hrmsHost = getEnvOrDefault("EGOV_HRMS_HOST", "http://localhost:8090");
            String hrmsSearchEndpoint = "/egov-hrms/employees/_search";

            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");

            // Iterate through each state
            for (Map.Entry<String, String> entry : TENANT_TO_STATE.entrySet()) {
                String tenantId = entry.getKey();
                String stateName = entry.getValue();

                log.info("Processing tenant: {} [{}]", tenantId, stateName);

                try {
                    // Fetch tenants from MDMS
                    JsonNode tenantsData = fetchMdmsData(
                            restTemplate, objectMapper, mdmsHost + mdmsSearchEndpoint,
                            tenantId, "tenant", List.of("tenants"), authToken
                    );

                    // Fetch districts and blocks from MDMS in a single call
                    JsonNode incidentData = fetchMdmsData(
                            restTemplate, objectMapper, mdmsHost + mdmsSearchEndpoint,
                            tenantId, "Incident", List.of("District", "Block"), authToken
                    );

                    // Parse the MDMS responses
                    List<JsonNode> tenants = getMasterArray(tenantsData, "tenant", "tenants");
                    List<JsonNode> districts = getMasterArray(incidentData, "Incident", "District");
                    List<JsonNode> blocks = getMasterArray(incidentData, "Incident", "Block");

                    // Build lookup maps for districts and blocks (code -> name)
                    Map<String, String> districtMap = buildDistrictMap(districts);
                    Map<String, String> blockMap = buildBlockMap(blocks);

                    log.info("Found {} tenants, {} districts, {} blocks for tenant {}", tenants.size(), districts.size(), blocks.size(), tenantId);

                    migrationLogger.println("\n========================================");
                    migrationLogger.println("Processing Tenant: " + tenantId + " [" + stateName + "]");
                    migrationLogger.println("========================================");
                    migrationLogger.println("Total Facilities to Process: " + tenants.size());
                    migrationLogger.println("----------------------------------------\n");
                    migrationLogger.flush();

                    int createdCount = 0;
                    int skippedCount = 0;

                    for (JsonNode tenant : tenants) {
                        String facilityName = getField(tenant, "name");
                        String facilityTenantId = getField(tenant, "code");

                        // Skip if facility name is "State"
                        if (facilityName == null || facilityName.equalsIgnoreCase("State")) {
                            logSkippedFacility(
                                    migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                    "Facility name is 'State'", null
                            );
                            skippedCount++;
                            continue;
                        }

                        try {
                            // Get city information
                            JsonNode cityNode = tenant.get("city");
                            if (cityNode == null || cityNode.isNull()) {
                                logSkippedFacility(
                                        migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                        "No city information found in MDMS data", null
                                );
                                skippedCount++;
                                continue;
                            }

                            String districtCode = getField(cityNode, "districtCode");
                            String blockCode = getField(cityNode, "blockCode");

                            // Lookup district and block names
                            String districtName = districtMap.get(districtCode);
                            String blockName = blockMap.get(blockCode);

                            if (districtName == null) {
                                logSkippedFacility(
                                        migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                        "District not found for code: " + districtCode, null
                                );
                                skippedCount++;
                                continue;
                            }

                            if (blockName == null) {
                                logSkippedFacility(
                                        migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                        "Block not found for code: " + blockCode, null
                                );
                                skippedCount++;
                                continue;
                            }

                            // Fetch POC name from HRMS
                            Map<String, String> hcrDetails = fetchHCRDetailsFromHRMS(
                                    restTemplate, objectMapper, hrmsHost + hrmsSearchEndpoint,
                                    facilityTenantId, authToken
                            );

                            String extractedHfrOrNinId = hcrDetails.get("hcrUserName");
                            if (extractedHfrOrNinId == null || extractedHfrOrNinId.isEmpty()) {
                                logSkippedFacility(
                                        migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                        "Unable to extract HFR/NIN ID from HRMS Call", null
                                );
                                skippedCount++;
                                continue;
                            }

                            if (facilityMappings.containsKey(extractedHfrOrNinId)) {
                                logSkippedFacility(
                                        migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                        "A health facility with same HFR/NIN ID is already processed", null
                                );
                                skippedCount++;
                                continue;
                            }

                            // Build facility object
                            Map<String, Object> facility = buildFacilityObject(
                                    tenant, stateName, districtName, blockName, hcrDetails.get("hcrName"), facilityTenantId,
                                    extractedHfrOrNinId, facilityMappings
                            );

                            // Create facility individually
                            boolean created = createSingleFacility(
                                    restTemplate, objectMapper, facilityHost + facilityCreateEndpoint,
                                    authToken, facility, facilityMappings, migrationLogger,
                                    skippedFacilities, facilityTenantId, facilityName
                            );

                            if (created) {
                                createdCount++;
                                log.info("✓ Created facility: {} ({})", facilityName, facilityTenantId);
                            } else {
                                skippedCount++;
                            }

                        } catch (Exception e) {
                            logSkippedFacility(
                                    migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                    "Exception during processing: " + e.getMessage(), e.toString()
                            );
                            skippedCount++;
                        }
                    }

                    log.info("Completed processing tenant {}: {} facilities created, {} skipped", tenantId, createdCount, skippedCount);

                    migrationLogger.println("\n----------------------------------------");
                    migrationLogger.println("Tenant " + tenantId + " Summary:");
                    migrationLogger.println("  ✓ Facilities Created: " + createdCount);
                    migrationLogger.println("  ✗ Facilities Skipped: " + skippedCount);
                    migrationLogger.println("----------------------------------------");
                    migrationLogger.flush();

                } catch (Exception e) {
                    log.error("Error processing tenant: {}", tenantId, e);
                }
            }

            // Persist facility mappings to database
            persistFacilityMappings(context, facilityMappings);

            // Count successfully migrated facilities (those with facilityId set)
            long successfullyMigrated = facilityMappings.values().stream()
                    .filter(mapping -> mapping.facilityId != null)
                    .count();

            // Print final summary to migration log
            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Total Facilities Migrated: " + successfullyMigrated);
            migrationLogger.println("Total Facilities Skipped: " + skippedFacilities.size());
            migrationLogger.println("\nCompleted at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");

            // Log facility mappings summary to console as well
            log.info("✅ Migration completed: Facilities migrated from MDMS to facility table");
            log.info("Total facilities successfully migrated: {}", successfullyMigrated);
            log.info("Total facilities skipped: {}", skippedFacilities.size());
            log.info("📝 Migration log file: {}", absoluteLogPath);

            migrationLogger.flush();
        }

    }

    private void persistFacilityMappings(Context context, Map<String, FacilityMapping> facilityMappings) throws Exception {
        String insertSQL = """
                INSERT INTO facility_tenant_id_map (hfr_or_nin_id, tenant_id, facility_id, boundary_code)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (hfr_or_nin_id) DO UPDATE\s
                SET tenant_id = EXCLUDED.tenant_id,
                    facility_id = EXCLUDED.facility_id,
                    boundary_code = EXCLUDED.boundary_code
                """;

        try (var connection = context.getConfiguration().getDataSource().getConnection();
             var preparedStatement = connection.prepareStatement(insertSQL)) {
            connection.setAutoCommit(false);

            int batchCount = 0;
            for (Map.Entry<String, FacilityMapping> entry : facilityMappings.entrySet()) {
                String ninId = entry.getKey();
                FacilityMapping mapping = entry.getValue();

                // Only persist if we have all required data
                if (mapping.facilityId != null && mapping.boundaryCode != null) {
                    preparedStatement.setString(1, ninId);
                    preparedStatement.setString(2, mapping.tenantCode);
                    preparedStatement.setString(3, mapping.facilityId);
                    preparedStatement.setString(4, mapping.boundaryCode);
                    preparedStatement.addBatch();
                    batchCount++;

                    // Log detailed mapping
                    log.debug(
                            "Persisting Mapping - Tenant: {}, HFR_ID: {}, Facility_ID: {}, Boundary: {}",
                            mapping.tenantCode, mapping.hfrId, mapping.facilityId, mapping.boundaryCode
                    );

                    // Execute batch every 100 records
                    if (batchCount % 100 == 0) {
                        preparedStatement.executeBatch();
                        connection.commit();
                        log.debug("Persisted {} facility mappings to database", batchCount);
                    }
                }
            }

            // Execute remaining batch
            if (batchCount % 100 != 0) {
                preparedStatement.executeBatch();
                connection.commit();
            }

            log.info("Successfully persisted {} facility mappings to facility_tenant_id_map table", batchCount);

        } catch (Exception e) {
            log.error("Error persisting facility mappings to database", e);
            throw e;
        }
    }

    private JsonNode fetchMdmsData(
            RestTemplate restTemplate, ObjectMapper objectMapper,
            String mdmsUrl, String tenantId, String moduleName,
            List<String> masters, String authToken
    ) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "RequestInfo", buildRequestInfo(authToken, "egov.mdms"),
                    "MdmsCriteria", Map.of(
                            "tenantId", tenantId,
                            "moduleDetails", List.of(
                                    Map.of(
                                            "moduleName", moduleName,
                                            "masterDetails", masters.stream()
                                                    .map(name -> Map.of("name", name))
                                                    .collect(Collectors.toList())
                                    )
                            )
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = executeWithRetry(
                    () -> restTemplate.exchange(mdmsUrl, HttpMethod.POST, entity, String.class),
                    "fetch MDMS data for tenant '" + tenantId + "' module '" + moduleName + "'"
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error(
                        "MDMS fetch failed for tenant {} module {} - status {}",
                        tenantId, moduleName, response.getStatusCode()
                );
                return objectMapper.createObjectNode();
            }

            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            log.error(
                    "Exception while fetching MDMS for tenant {} module {}: {}",
                    tenantId, moduleName, e.getMessage(), e
            );
            return objectMapper.createObjectNode();
        }
    }

    private List<JsonNode> getMasterArray(JsonNode mdmsRes, String module, String master) {
        if (mdmsRes == null || !mdmsRes.has("MdmsRes")) {
            return Collections.emptyList();
        }
        JsonNode moduleNode = mdmsRes.get("MdmsRes").get(module);
        if (moduleNode == null || moduleNode.isMissingNode()) {
            return Collections.emptyList();
        }
        JsonNode arr = moduleNode.get(master);
        if (arr == null || !arr.isArray()) {
            return Collections.emptyList();
        }
        List<JsonNode> list = new ArrayList<>();
        for (JsonNode n : arr) {
            list.add(n);
        }
        return list;
    }

    private Map<String, String> buildDistrictMap(List<JsonNode> districts) {
        Map<String, String> map = new HashMap<>();
        for (JsonNode district : districts) {
            String code = getField(district, "code");
            String name = getField(district, "name");
            if (code != null && name != null) {
                map.put(code, name);
            }
        }
        return map;
    }

    private Map<String, String> buildBlockMap(List<JsonNode> blocks) {
        Map<String, String> map = new HashMap<>();
        for (JsonNode block : blocks) {
            String code = getField(block, "code");
            String name = getField(block, "name");
            if (code != null && name != null) {
                map.put(code, name);
            }
        }
        return map;
    }

    private boolean createSingleFacility(
            RestTemplate restTemplate, ObjectMapper objectMapper, String facilityUrl,
            String authToken, Map<String, Object> facility, Map<String, FacilityMapping> facilityMappings,
            PrintWriter migrationLogger, List<SkippedFacility> skippedFacilities,
            String facilityTenantId, String facilityName
    ) {
        try {
            // Build request with single facility in array
            Map<String, Object> request = new HashMap<>();
            request.put("RequestInfo", buildRequestInfo(authToken, "org.egov.facility"));
            request.put("facilities", Collections.singletonList(facility));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

            ResponseEntity<String> response = executeWithRetry(
                    () -> restTemplate.postForEntity(facilityUrl, httpEntity, String.class),
                    "create facility with tenant ID '" + facilityTenantId + "' and name '" + facilityName + "'"
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse response and update facility mapping
                try {
                    JsonNode responseBody = objectMapper.readTree(response.getBody());
                    if (responseBody.isArray() && !responseBody.isEmpty()) {
                        JsonNode facilityNode = responseBody.get(0);
                        String facilityId = getField(facilityNode, "facility_id");
                        String boundaryCode = getField(facilityNode, "boundaryCode");

                        JsonNode facilityDetails = facilityNode.get("facility_details");
                        if (facilityDetails != null) {
                            String ninId = getField(facilityDetails, "nin_id");

                            if (ninId != null && facilityId != null) {
                                // Update existing mapping with facility_id and boundaryCode
                                FacilityMapping existingMapping = facilityMappings.get(ninId);
                                if (existingMapping != null) {
                                    existingMapping.facilityId = facilityId;
                                    existingMapping.boundaryCode = boundaryCode;
                                    log.debug("✓ Updated mapping - Facility ID: {}, Boundary: {}", facilityId, boundaryCode);
                                }
                            }
                        }
                        return true;
                    } else {
                        String errorMsg = "Response is not a valid array or is empty";
                        logSkippedFacility(
                                migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                                errorMsg, response.getBody()
                        );
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Error parsing facility response: {}", e.getMessage(), e);
                    logSkippedFacility(
                            migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                            "Error parsing API response: " + e.getMessage(), response.getBody()
                    );
                    return false;
                }
            } else {
                String errorMsg = "API returned status: " + response.getStatusCode();
                logSkippedFacility(
                        migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                        errorMsg, response.getBody()
                );
                return false;
            }

        } catch (HttpClientErrorException e) {
            // Handle 4xx errors (400, 404, etc.) - these often have detailed error messages
            String errorMsg = "API returned " + e.getStatusCode() + " - " + e.getStatusText();
            String responseBody = e.getResponseBodyAsString();

            log.error("Client error while creating facility {}: {} - {}", facilityTenantId, errorMsg, responseBody);
            logSkippedFacility(
                    migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                    errorMsg, responseBody
            );
            return false;

        } catch (HttpServerErrorException e) {
            // Handle 5xx errors (500, 502, etc.)
            String errorMsg = "API returned " + e.getStatusCode() + " - " + e.getStatusText();
            String responseBody = e.getResponseBodyAsString();

            log.error("Server error while creating facility {}: {} - {}", facilityTenantId, errorMsg, responseBody);
            logSkippedFacility(
                    migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                    errorMsg, responseBody
            );
            return false;

        } catch (Exception e) {
            log.error("Exception while creating facility: {}", e.getMessage(), e);
            logSkippedFacility(
                    migrationLogger, skippedFacilities, facilityTenantId, facilityName,
                    "API call exception: " + e.getMessage(), e.toString()
            );
        }

        return false;
    }

    private PrintWriter initializeMigrationLogger(String logFilePath, String absolutePath) throws Exception {
        Files.createDirectories(Paths.get("./logs"));
        FileWriter fileWriter = new FileWriter(logFilePath, true);
        log.info("📝 Migration log file created: {}", absolutePath);
        return new PrintWriter(fileWriter, true);
    }

    private void logSkippedFacility(
            PrintWriter migrationLogger, List<SkippedFacility> skippedFacilities,
            String tenantId, String facilityName, String reason, String additionalInfo
    ) {
        SkippedFacility skipped = new SkippedFacility(tenantId, facilityName, reason, additionalInfo);
        skippedFacilities.add(skipped);

        // Print immediately in a clean format
        migrationLogger.println("\n[SKIPPED]");
        migrationLogger.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        migrationLogger.println("Tenant ID: " + (tenantId != null ? tenantId : "N/A"));
        migrationLogger.println("Facility Name: " + (facilityName != null ? facilityName : "N/A"));
        migrationLogger.println("Reason: " + reason);
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            migrationLogger.println("Additional Info: " + additionalInfo);
        }
        migrationLogger.println(); // Blank line separator
        migrationLogger.flush();

        log.warn("✗ Skipped - {}: {} - {}", tenantId, facilityName, reason);
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

    private Map<String, String> fetchHCRDetailsFromHRMS(
            RestTemplate restTemplate, ObjectMapper objectMapper,
            String hrmsUrl, String facilityTenantId, String authToken
    ) {

        Map<String, String> hcrDetails = new HashMap<>();
        try {
            Map<String, Object> requestBody = Map.of(
                    "RequestInfo", buildRequestInfo(authToken, "egov.hrms")
            );

            // Build URL with query parameters
            String url = String.format("%s?roles=COMPLAINANT&tenantId=%s", hrmsUrl, facilityTenantId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = executeWithRetry(
                    () -> restTemplate.postForEntity(url, httpEntity, String.class),
                    "fetch POC from HRMS for facility tenant ID '" + facilityTenantId + "'"
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode employees = root.get("Employees");

                if (employees != null && employees.isArray() && !employees.isEmpty()) {
                    JsonNode firstEmployee = employees.get(0);
                    JsonNode user = firstEmployee.get("user");
                    if (user != null) {
                        if (user.has("name")) {
                            String hcrName = user.get("name").asText();
                            log.debug("Found HCR name (POC Name): {} for facility: {}", hcrName, facilityTenantId);
                            hcrDetails.put("hcrName", hcrName);
                        }
                        if (user.has("userName")) {
                            String hcrUserName = user.get("userName").asText();
                            log.debug("Found HCR username (HFR/NIN ID): {} for facility: {}", hcrUserName, facilityTenantId);
                            hcrDetails.put("hcrUserName", hcrUserName);
                        }
                    }
                }
            }

            log.warn("No employee with COMPLAINANT role found for facility: {}", facilityTenantId);
            return hcrDetails;

        } catch (Exception e) {
            log.error("Error fetching POC from HRMS for facility: {}", facilityTenantId, e);
            return hcrDetails;
        }
    }

    private Map<String, Object> buildFacilityObject(
            JsonNode tenant, String stateName, String districtName, String blockName, String pocName,
            String facilityTenantId, String extractedHfrOrNinId, Map<String, FacilityMapping> facilityMappings
    ) {

        // Extract all fields from tenant object
        String facilityName = getField(tenant, "name");
        String centreType = getField(tenant, "centreType");
        String facilityType = getField(tenant, "type");
        String contactNumber = getField(tenant, "contactNumber");
        String pincode = getField(tenant, "pincode");

        // Get city information
        JsonNode cityNode = tenant.get("city");

        // Create block boundary code
        String blockBoundaryCode = String.format("India_%s_%s_%s", stateName, districtName, blockName).replaceAll("\\s+", "");

        // Build facility address - extract all available fields from MDMS
        Map<String, Object> facilityAddress = new HashMap<>();
        facilityAddress.put("tenantId", "in");

        // Extract address fields from city node
        if (cityNode != null && !cityNode.isNull()) {
            // Latitude and Longitude
            if (cityNode.has("latitude") && !cityNode.get("latitude").isNull()) {
                facilityAddress.put("latitude", cityNode.get("latitude").asDouble());
            }
            if (cityNode.has("longitude") && !cityNode.get("longitude").isNull()) {
                facilityAddress.put("longitude", cityNode.get("longitude").asDouble());
            }

            // City name
            String cityName = getField(cityNode, "name");
            if (cityName != null) {
                facilityAddress.put("city", cityName);
            }
        }

        // State, District, Block from the resolved names
        facilityAddress.put("state", stateName);
        facilityAddress.put("district", districtName);
        facilityAddress.put("block", blockName);

        // Pincode
        if (pincode != null && !pincode.isEmpty()) {
            facilityAddress.put("pincode", pincode);
        }

        // Pre-populate facility mapping with tenant code and HFR ID (facility_id and boundaryCode will be added later from response)
        FacilityMapping mapping = new FacilityMapping(facilityTenantId, extractedHfrOrNinId, null, null);
        facilityMappings.put(extractedHfrOrNinId, mapping);
        log.debug("Pre-populated mapping for nin_id: {} -> tenant: {}, hfr: {}", extractedHfrOrNinId, facilityTenantId, extractedHfrOrNinId);

        // Build facility details
        Map<String, Object> facilityDetails = new HashMap<>();
        facilityDetails.put("hfr_id", extractedHfrOrNinId);
        facilityDetails.put("nin_id", extractedHfrOrNinId);

        // Determine solar solution design type based on facility type/centre type
        String solarSolutionDesignType = determineSolarSolutionType(facilityType, centreType);
        facilityDetails.put("solar_solution_design_type", solarSolutionDesignType);

        facilityDetails.put("pocContact", contactNumber);
        facilityDetails.put("pocName", pocName);

        // Build facility object
        Map<String, Object> facility = new HashMap<>();
        facility.put("tenant_id", "in");
        facility.put("facility_name", facilityName);
        facility.put("facility_type", centreType != null ? centreType : (facilityType != null ? facilityType : "PHC"));
        facility.put("isActive", true);
        facility.put("isOnmReady", true);
        facility.put("blockBoundaryCode", blockBoundaryCode);
        facility.put("address", facilityAddress);
        facility.put("facility_details", facilityDetails);

        return facility;
    }

    private String determineSolarSolutionType(String facilityType, String centreType) {
        // Default solar solution design type
        // This can be customized based on facility type if needed
        return "SC_NO_LR_FRIDGE_NO_CCP";
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

    // Helper class to track facility mappings
    private static class FacilityMapping {
        String tenantCode;
        String hfrId;
        String facilityId;
        String boundaryCode;

        FacilityMapping(String tenantCode, String hfrId, String facilityId, String boundaryCode) {
            this.tenantCode = tenantCode;
            this.hfrId = hfrId;
            this.facilityId = facilityId;
            this.boundaryCode = boundaryCode;
        }
    }

    private static class SkippedFacility {
        String tenantId;
        String facilityName;
        String reason;
        String additionalInfo;
        String timestamp;

        SkippedFacility(String tenantId, String facilityName, String reason, String additionalInfo) {
            this.tenantId = tenantId;
            this.facilityName = facilityName;
            this.reason = reason;
            this.additionalInfo = additionalInfo;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Timestamp: ").append(timestamp).append("\n");
            sb.append("Tenant ID: ").append(tenantId != null ? tenantId : "N/A").append("\n");
            sb.append("Facility Name: ").append(facilityName != null ? facilityName : "N/A").append("\n");
            sb.append("Reason: ").append(reason).append("\n");
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                sb.append("Additional Info: ").append(additionalInfo).append("\n");
            }
            return sb.toString();
        }
    }
}
