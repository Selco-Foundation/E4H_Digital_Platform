package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class V20251119192800__populate_localizations_for_boundaries extends BaseJavaMigration {

    private static final String TARGET_TENANT_ID = "in";
    private static final String TARGET_MODULE = "rainmaker-in";
    private static final String SOURCE_MODULE = "rainmaker-im";

    // Locale to tenantId mapping for searching existing localizations
    private static final Map<String, String> LOCALE_TO_TENANT_ID;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("gu_IN", "gj");
        m.put("lus_IN", "mz");
        m.put("or_IN", "or");
        m.put("mr_IN", "mh");
        m.put("ka_IN", "pg");
        LOCALE_TO_TENANT_ID = Collections.unmodifiableMap(m);
    }

    // All locales to process
    private static final List<String> ALL_LOCALES = List.of("en_IN", "gu_IN", "lus_IN", "or_IN", "ka_IN", "mr_IN");

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

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: Populating localizations for boundaries");

        RestTemplate restTemplate = createRestTemplateWithTimeouts();
        ObjectMapper objectMapper = new ObjectMapper();

        // Initialize migration log file
        String logFileName = "boundary_localization_migration_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        String logFilePath = "./logs/" + logFileName;
        String absoluteLogPath = Paths.get(logFilePath).toAbsolutePath().normalize().toString();

        try (PrintWriter migrationLogger = initializeMigrationLogger(logFilePath, absoluteLogPath)) {
            migrationLogger.println("========================================");
            migrationLogger.println("BOUNDARY LOCALIZATION MIGRATION LOG");
            migrationLogger.println("Target Tenant: " + TARGET_TENANT_ID);
            migrationLogger.println("Target Module: " + TARGET_MODULE);
            migrationLogger.println("Started at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            String localizationHost = getEnvOrDefault("EGOV_LOCALIZATION_HOST", "http://localhost:8095");
            String mdmsHost = getEnvOrDefault("EGOV_MDMS_HOST", "http://localhost:8094");
            String mdmsSearchEndpoint = "/egov-mdms-service/v1/_search";
            String authToken = getEnvOrDefault("EGOV_AUTH_TOKEN", "");

            log.info("Localization Host: {}", localizationHost);
            log.info("MDMS Host: {}", mdmsHost);

            // Build RequestInfo for MDMS calls
            ObjectNode requestInfo = buildRequestInfoNode(objectMapper, authToken);

            // Step 1: Build localization map for non-English locales
            migrationLogger.println("Step 1: Building localization maps from existing rainmaker-im module\n");
            migrationLogger.flush();

            Map<String, Map<String, String>> localeToLocalizationMap = buildLocalizationMaps(
                restTemplate, objectMapper, localizationHost, authToken, migrationLogger
            );

            // Step 2: Fetch all boundaries from MDMS
            migrationLogger.println("\nStep 2: Fetching boundaries from MDMS\n");
            migrationLogger.flush();

            List<BoundaryInfo> boundaries = fetchAllBoundaries(
                restTemplate, objectMapper, mdmsHost, mdmsSearchEndpoint, requestInfo, context, migrationLogger
            );

            migrationLogger.println("Found " + boundaries.size() + " boundaries to process\n");
            migrationLogger.flush();

            // Step 3: Group boundaries by type
            Map<String, List<BoundaryInfo>> boundariesByType = boundaries.stream()
                .collect(Collectors.groupingBy(b -> b.boundaryType));

            // Step 4: Create localization messages for each locale
            migrationLogger.println("\nStep 3: Creating localization messages\n");
            migrationLogger.flush();

            int totalMessagesCreated = 0;

            for (String locale : ALL_LOCALES) {
                migrationLogger.println("\n--- Processing Locale: " + locale + " ---");
                migrationLogger.flush();
                log.info("Processing locale: {}", locale);

                List<ObjectNode> messages = new ArrayList<>();

                // Process Country
                messages.addAll(createMessagesForBoundaries(
                    objectMapper, boundariesByType.get("Country"), locale, localeToLocalizationMap.get(locale), "Country"
                ));

                // Process States
                messages.addAll(createMessagesForBoundaries(
                    objectMapper, boundariesByType.get("State"), locale, localeToLocalizationMap.get(locale), "State"
                ));

                // Process Districts
                messages.addAll(createMessagesForBoundaries(
                    objectMapper, boundariesByType.get("District"), locale, localeToLocalizationMap.get(locale), "District"
                ));

                // Process Blocks
                messages.addAll(createMessagesForBoundaries(
                    objectMapper, boundariesByType.get("Block"), locale, localeToLocalizationMap.get(locale), "Block"
                ));

                // Process Facilities
                messages.addAll(createMessagesForBoundaries(
                    objectMapper, boundariesByType.get("Facility"), locale, localeToLocalizationMap.get(locale), "Facility"
                ));

                if (!messages.isEmpty()) {
                    // Deduplicate messages by code (tenant + locale + module + code must be unique)
                    List<ObjectNode> deduplicatedMessages = deduplicateMessages(messages, migrationLogger);
                    int duplicateCount = messages.size() - deduplicatedMessages.size();
                    if (duplicateCount > 0) {
                        migrationLogger.println("  Removed " + duplicateCount + " duplicate message(s) for locale: " + locale);
                        migrationLogger.flush();
                    }

                    // Bulk upsert messages for this locale
                    int created = upsertLocalizationMessages(
                        restTemplate, objectMapper, localizationHost, TARGET_MODULE, TARGET_TENANT_ID,
                        deduplicatedMessages, authToken, migrationLogger
                    );
                    totalMessagesCreated += created;
                    migrationLogger.println("Created " + created + " messages for locale: " + locale);
                    migrationLogger.flush();
                } else {
                    migrationLogger.println("No messages to create for locale: " + locale);
                    migrationLogger.flush();
                }
            }

            // Final summary
            migrationLogger.println("\n========================================");
            migrationLogger.println("MIGRATION SUMMARY");
            migrationLogger.println("========================================");
            migrationLogger.println("Total Boundaries Processed: " + boundaries.size());
            migrationLogger.println("Total Messages Created: " + totalMessagesCreated);
            migrationLogger.println("\nCompleted at: " + LocalDateTime.now());
            migrationLogger.println("========================================\n");
            migrationLogger.flush();

            log.info("Migration completed");
            log.info("Total boundaries processed: {}", boundaries.size());
            log.info("Total messages created: {}", totalMessagesCreated);
            log.info("Migration log file: {}", absoluteLogPath);
        }
    }

    /**
     * Build localization maps for non-English locales by searching existing localizations
     */
    private Map<String, Map<String, String>> buildLocalizationMaps(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String localizationHost,
        String authToken,
        PrintWriter migrationLogger
    ) {
        Map<String, Map<String, String>> localeToMap = new HashMap<>();

        for (Map.Entry<String, String> entry : LOCALE_TO_TENANT_ID.entrySet()) {
            String locale = entry.getKey();
            String tenantId = entry.getValue();

            migrationLogger.println("  Building map for locale: " + locale + " (from tenant: " + tenantId + ")");
            migrationLogger.flush();

            try {
                Map<String, String> localizationMap = searchLocalizationMessages(
                    restTemplate, objectMapper, localizationHost, SOURCE_MODULE, tenantId, locale
                );

                if (localizationMap != null && !localizationMap.isEmpty()) {
                    localeToMap.put(locale, localizationMap);
                    migrationLogger.println("    Found " + localizationMap.size() + " existing localizations");
                    migrationLogger.flush();
                } else {
                    migrationLogger.println("    No existing localizations found");
                    migrationLogger.flush();
                }
            } catch (Exception e) {
                log.warn("Error building localization map for locale {}: {}", locale, e.getMessage());
                migrationLogger.println("    Error: " + e.getMessage());
                migrationLogger.flush();
            }
        }

        return localeToMap;
    }

    /**
     * Search for localization messages and build a map of code -> localized message
     */
    private Map<String, String> searchLocalizationMessages(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String localizationHost,
        String module,
        String tenantId,
        String locale
    ) {
        String searchUrl = UriComponentsBuilder.fromHttpUrl(localizationHost + "/localization/messages/v1/_search")
            .queryParam("locale", locale)
            .queryParam("module", module)
            .queryParam("tenantId", tenantId)
            .build()
            .toUriString();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>("{}", headers);

            log.debug("Searching localization messages: module={}, tenantId={}, locale={}", module, tenantId, locale);
            ResponseEntity<String> response = restTemplate.exchange(
                searchUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                ArrayNode messages = (ArrayNode) responseBody.path("messages");

                // Build map: code -> localized message
                Map<String, String> localizationMap = new HashMap<>();
                if (messages != null) {
                    for (int i = 0; i < messages.size(); i++) {
                        JsonNode message = messages.get(i);
                        String code = message.path("code").asText();
                        String localizedMessage = message.path("message").asText();

                        if (code != null && !code.isEmpty() && localizedMessage != null && !localizedMessage.isEmpty()) {
                            localizationMap.put(code, localizedMessage);
                        }
                    }
                }
                return localizationMap;
            } else {
                log.warn(
                        "Localization search failed: module={}, tenantId={}, locale={} - Status: {}",
                        module, tenantId, locale, response.getStatusCode()
                );
                return null;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(
                    "HTTP error searching localization: module={}, tenantId={}, locale={} - {} - {}",
                    module, tenantId, locale, e.getStatusCode(), e.getResponseBodyAsString()
            );
            return null;
        } catch (Exception e) {
            log.error(
                    "Error searching localization: module={}, tenantId={}, locale={} - {}",
                    module, tenantId, locale, e.getMessage(), e
            );
            return null;
        }
    }

    /**
     * Fetch all boundaries from MDMS data
     */
    private List<BoundaryInfo> fetchAllBoundaries(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String mdmsHost,
        String mdmsSearchEndpoint,
        ObjectNode requestInfo,
        Context context,
        PrintWriter migrationLogger
    ) throws Exception {
        List<BoundaryInfo> boundaries = new ArrayList<>();

        // Add Country boundary
        boundaries.add(new BoundaryInfo("India", "Country", "India"));

        // Process each state
        for (Map.Entry<String, String> entry : TENANT_TO_STATE.entrySet()) {
            String tenantId = entry.getKey();
            String stateName = entry.getValue();
            String stateCode = formatBoundaryCode(stateName);

            migrationLogger.println("Processing state: " + stateName + " (tenant: " + tenantId + ")");
            migrationLogger.flush();

            // Add State boundary
            boundaries.add(new BoundaryInfo(stateCode, "State", stateName));

            // Fetch District and Block from MDMS for this tenant
            Map<String, Object> mdmsReq = buildMdmsRequest(objectMapper, requestInfo, tenantId, "Incident", Arrays.asList("District", "Block"));
            JsonNode mdmsRes = postMdms(objectMapper, restTemplate, mdmsHost + mdmsSearchEndpoint, mdmsReq);

            List<JsonNode> districts = getMasterArray(mdmsRes, "Incident", "District");
            List<JsonNode> blocks = getMasterArray(mdmsRes, "Incident", "Block");

            // Process districts
            for (JsonNode districtNode : districts) {
                String districtName = pickField(districtNode, List.of("name"));
                if (districtName == null || districtName.isEmpty()) continue;
                String districtBoundaryCode = formatBoundaryCode(stateName + "_" + districtName);
                boundaries.add(new BoundaryInfo(districtBoundaryCode, "District", districtName));
            }

            // Process blocks
            Map<String, String> mdmsDistrictCodeToName = new HashMap<>();
            for (JsonNode districtNode : districts) {
                String mdmsDistrictCode = pickField(districtNode, List.of("code"));
                String districtName = pickField(districtNode, List.of("name"));
                if (mdmsDistrictCode != null && districtName != null) {
                    mdmsDistrictCodeToName.put(mdmsDistrictCode, districtName);
                }
            }

            for (JsonNode blockNode : blocks) {
                String parentDistrictMdmsCode = pickField(blockNode, List.of("districtCode"));
                String blockName = pickField(blockNode, List.of("name"));
                if (parentDistrictMdmsCode == null || blockName == null || blockName.isEmpty()) continue;
                String districtNameForBlock = mdmsDistrictCodeToName.get(parentDistrictMdmsCode);
                if (districtNameForBlock == null) continue;
                String blockBoundaryCode = formatBoundaryCode(stateName + "_" + districtNameForBlock + "_" + blockName);
                boundaries.add(new BoundaryInfo(blockBoundaryCode, "Block", blockName));
            }
        }

        // Fetch facilities from facility_tenant_id_map table
        migrationLogger.println("Fetching facilities from facility_tenant_id_map table");
        migrationLogger.flush();

        List<BoundaryInfo> facilities = fetchFacilitiesFromDatabase(context);
        boundaries.addAll(facilities);

        return boundaries;
    }

    /**
     * Fetch facilities from facility_tenant_id_map table joined with facility table
     */
    private List<BoundaryInfo> fetchFacilitiesFromDatabase(Context context) throws Exception {
        List<BoundaryInfo> facilities = new ArrayList<>();

        String query = "SELECT b.tenant_id, b.boundary_code, a.facility_name " +
                       "FROM facility a " +
                       "JOIN facility_tenant_id_map b ON a.id = b.facility_id " +
                       "WHERE b.boundary_code IS NOT NULL";

        try (Connection conn = context.getConfiguration().getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tenantId = rs.getString("tenant_id");
                String boundaryCode = rs.getString("boundary_code");
                String facilityName = rs.getString("facility_name");

                if (boundaryCode != null && !boundaryCode.isEmpty() &&
                    facilityName != null && !facilityName.isEmpty() &&
                    tenantId != null && !tenantId.isEmpty()) {
                    facilities.add(new BoundaryInfo(boundaryCode, "Facility", facilityName, tenantId));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching facilities from database: {}", e.getMessage(), e);
        }

        return facilities;
    }

    /**
     * Build MDMS request
     */
    private Map<String, Object> buildMdmsRequest(
        ObjectMapper objectMapper,
        ObjectNode requestInfo,
        String tenantId,
        String moduleName,
        List<String> masters
    ) {
        Map<String, Object> mdmsCriteriaReq = new HashMap<>();

        Map<String, Object> mdmsCriteria = new HashMap<>();
        mdmsCriteria.put("tenantId", (tenantId == null ? "" : tenantId));

        List<Map<String, Object>> masterDetails = masters.stream().distinct().map(name -> {
            Map<String, Object> md = new HashMap<>();
            md.put("name", name);
            return md;
        }).collect(java.util.stream.Collectors.toList());

        Map<String, Object> moduleDetail = new HashMap<>();
        moduleDetail.put("moduleName", moduleName);
        moduleDetail.put("masterDetails", masterDetails);

        mdmsCriteria.put("moduleDetails", Collections.singletonList(moduleDetail));

        mdmsCriteriaReq.put("MdmsCriteria", mdmsCriteria);
        mdmsCriteriaReq.put("RequestInfo", requestInfo);

        return mdmsCriteriaReq;
    }

    /**
     * Post request to MDMS
     */
    private JsonNode postMdms(ObjectMapper objectMapper, RestTemplate restTemplate, String url, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("MDMS call failed: {}", url, e);
            return objectMapper.createObjectNode();
        }
    }

    /**
     * Get master array from MDMS response
     */
    private List<JsonNode> getMasterArray(JsonNode mdmsRes, String module, String master) {
        if (mdmsRes == null || !mdmsRes.has("MdmsRes")) return Collections.emptyList();
        JsonNode moduleNode = mdmsRes.get("MdmsRes").get(module);
        if (moduleNode == null || moduleNode.isMissingNode()) return Collections.emptyList();
        JsonNode arr = moduleNode.get(master);
        if (arr == null || !arr.isArray()) return Collections.emptyList();
        List<JsonNode> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(n);
        return list;
    }

    /**
     * Pick field from JSON node
     */
    private String pickField(JsonNode node, List<String> keys) {
        for (String k : keys) {
            if (node.has(k) && !node.get(k).isNull()) {
                String val = node.get(k).asText();
                if (val != null && !val.isEmpty()) return val;
            }
        }
        return null;
    }

    /**
     * Format boundary code (removes spaces from names)
     */
    private String formatBoundaryCode(String namePath) {
        String code = Arrays.stream(namePath.split("_"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(segment -> segment.replaceAll("\\s+", ""))
            .collect(java.util.stream.Collectors.joining("_"));
        if (!code.startsWith("India_")) code = "India_" + code;
        return code;
    }

    /**
     * Deduplicate messages by code (keeps first occurrence)
     */
    private List<ObjectNode> deduplicateMessages(List<ObjectNode> messages, PrintWriter migrationLogger) {
        Map<String, ObjectNode> uniqueMessages = new LinkedHashMap<>();

        for (ObjectNode message : messages) {
            String code = message.path("code").asText();
            if (code != null && !code.isEmpty()) {
                if (uniqueMessages.containsKey(code)) {
                    migrationLogger.println("    [DUPLICATE] Code: " + code + " (keeping first occurrence)");
                    migrationLogger.flush();
                } else {
                    uniqueMessages.put(code, message);
                }
            }
        }

        return new ArrayList<>(uniqueMessages.values());
    }

    /**
     * Create localization messages for boundaries
     */
    private List<ObjectNode> createMessagesForBoundaries(
        ObjectMapper objectMapper,
        List<BoundaryInfo> boundaries,
        String locale,
        Map<String, String> localizationMap,
        String boundaryType
    ) {
        if (boundaries == null || boundaries.isEmpty()) {
            return new ArrayList<>();
        }

        List<ObjectNode> messages = new ArrayList<>();

        for (BoundaryInfo boundary : boundaries) {
            String code = "Boundary_" + boundary.code;
            String message;

            if ("en_IN".equals(locale)) {
                // For English, use the boundary name directly
                message = boundary.name;
            } else {
                // For other locales, search in localization map
                String lookupKey;
                if ("Facility".equals(boundaryType) && boundary.tenantId != null) {
                    // For facilities, use special key format: TENANTS_TENANTS_{tenant_id.replace(".", "_").toUpperCase()}
                    lookupKey = "TENANTS_TENANTS_" + boundary.tenantId.replace(".", "_").toUpperCase();
                } else {
                    // For other boundaries, use the name directly
                    lookupKey = boundary.name;
                }

                if (localizationMap != null && localizationMap.containsKey(lookupKey)) {
                    message = localizationMap.get(lookupKey);
                } else {
                    // Fallback to English name
                    message = boundary.name;
                }
            }

            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("code", code);
            messageNode.put("message", message);
            messageNode.put("module", TARGET_MODULE);
            messageNode.put("locale", locale);

            messages.add(messageNode);
        }

        return messages;
    }

    /**
     * Upsert localization messages
     */
    private int upsertLocalizationMessages(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        String localizationHost,
        String module,
        String tenantId,
        List<ObjectNode> messages,
        String authToken,
        PrintWriter migrationLogger
    ) {
        if (messages.isEmpty()) {
            return 0;
        }

        String upsertUrl = localizationHost + "/localization/messages/v1/_upsert";

        // Build request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("RequestInfo", buildRequestInfoNode(objectMapper, authToken));
        requestBody.put("tenantId", tenantId);

        ArrayNode messagesArray = objectMapper.createArrayNode();
        for (ObjectNode message : messages) {
            messagesArray.add(message);
        }
        requestBody.set("messages", messagesArray);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Upserting {} messages for module: {}, tenantId: {}", messages.size(), module, tenantId);
            ResponseEntity<String> response = restTemplate.exchange(
                upsertUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug(
                        "✓ Successfully upserted {} messages for module: {}, tenantId: {}",
                        messages.size(), module, tenantId
                );
                return messages.size();
            } else {
                log.warn(
                        "Upsert failed for module: {}, tenantId: {} - Status: {} - {}",
                        module, tenantId, response.getStatusCode(), response.getBody()
                );
                migrationLogger.println("      [FAILED] Upsert - Status: " + response.getStatusCode());
                migrationLogger.flush();
                return 0;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(
                    "HTTP error upserting messages for module: {}, tenantId: {} - {} - {}",
                    module, tenantId, e.getStatusCode(), e.getResponseBodyAsString()
            );
            migrationLogger.println("      [FAILED] Upsert HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            migrationLogger.flush();
            return 0;
        } catch (Exception e) {
            log.error(
                    "Error upserting messages for module: {}, tenantId: {} - {}",
                    module, tenantId, e.getMessage(), e
            );
            migrationLogger.println("      [FAILED] Upsert exception: " + e.getMessage());
            migrationLogger.flush();
            return 0;
        }
    }

    /**
     * Build RequestInfo node for API calls
     */
    private ObjectNode buildRequestInfoNode(ObjectMapper objectMapper, String authToken) {
        ObjectNode requestInfo = objectMapper.createObjectNode();
        requestInfo.put("apiId", "Rainmaker");
        requestInfo.put("ver", "1.0");
        long timestamp = System.currentTimeMillis();
        requestInfo.put("ts", timestamp);
        requestInfo.put("action", "create");
        requestInfo.put("msgId", timestamp + "|en_IN");
        requestInfo.put("authToken", authToken != null ? authToken : "");

        ObjectNode userInfo = objectMapper.createObjectNode();
        userInfo.put("id", 206);
        userInfo.put("uuid", "14d6dbdf-e4d2-45c3-9717-c82ba17a9f19");
        userInfo.put("userName", "SYSTEMUSER");
        userInfo.put("name", "System User");
        userInfo.put("mobileNumber", "1111112111");
        userInfo.put("emailId", "");
        userInfo.put("type", "EMPLOYEE");
        userInfo.put("active", true);
        userInfo.put("tenantId", "in");

        ArrayNode roles = objectMapper.createArrayNode();
        ObjectNode role1 = objectMapper.createObjectNode();
        role1.put("name", "Employee");
        role1.put("code", "EMPLOYEE");
        role1.put("tenantId", "in");
        roles.add(role1);
        ObjectNode role2 = objectMapper.createObjectNode();
        role2.put("name", "System user");
        role2.put("code", "SYSTEM");
        role2.put("tenantId", "in");
        roles.add(role2);
        userInfo.set("roles", roles);

        requestInfo.set("userInfo", userInfo);
        return requestInfo;
    }

    /**
     * Initialize migration logger
     */
    private PrintWriter initializeMigrationLogger(String logFilePath, String absolutePath) throws Exception {
        Files.createDirectories(Paths.get("./logs"));
        FileWriter fileWriter = new FileWriter(logFilePath, true);
        log.info("Migration log file created: {}", absolutePath);
        return new PrintWriter(fileWriter, true);
    }

    /**
     * Create RestTemplate with configured timeouts
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

    private static String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    /**
     * Helper class to hold boundary information
     */
    private static class BoundaryInfo {
        String code;
        String boundaryType;
        String name;
        String tenantId; // For facilities, stores the tenant_id

        BoundaryInfo(String code, String boundaryType, String name) {
            this.code = code;
            this.boundaryType = boundaryType;
            this.name = name;
            this.tenantId = null;
        }

        BoundaryInfo(String code, String boundaryType, String name, String tenantId) {
            this.code = code;
            this.boundaryType = boundaryType;
            this.name = name;
            this.tenantId = tenantId;
        }
    }
}
