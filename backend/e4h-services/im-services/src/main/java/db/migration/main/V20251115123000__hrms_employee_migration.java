package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class V20251115123000__hrms_employee_migration extends BaseJavaMigration {

	private static final String TARGET_TENANT = "in";
	private static final int MDMS_LIMIT = 300;
	private static final int HRMS_PAGE_SIZE = 200;
	private static final String TENANT_MODULE_NAME = "tenant";
	private static final String TENANT_MASTER_NAME = "tenants";

	private static final List<String> COMPLAINANT_ROLES = Collections.singletonList("COMPLAINANT");
	private static final List<String> STATE_LEVEL_ROLES = Arrays.asList("COMPLAINT_ASSESSOR", "COMPLAINT_FACILITATOR_1", "COMPLAINT_FACILITATOR_2");

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

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String mdmsHost;
	private String mdmsSearchEndpoint;
	private String hrmsHost;
	private String hrmsSearchEndpoint;
	private String hrmsCreateEndpoint;

	private RequestInfo requestInfo;
	private Map<String, String> facilityTenantBoundaryMap = new HashMap<>();

	private PrintWriter migrationLogger;
	private Path logFilePath;

	private int totalEmployeesEvaluated = 0;
	private int totalEmployeesMigrated = 0;
	private final List<String> failures = new ArrayList<>();

	@Override
	public boolean canExecuteInTransaction() {
		return false;
	}

	@Override
	public void migrate(Context context) throws Exception {
		initializeEnv();
		requestInfo = buildRequestInfo();

		Path logsDir = Paths.get("logs");
		Files.createDirectories(logsDir);
		String logFileName = "hrms_employee_migration_" + LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
		logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

		try (PrintWriter logger = initializeMigrationLogger(logFilePath);
		     Connection connection = context.getConnection()) {
			this.migrationLogger = logger;
			logSectionHeader();

			loadFacilityTenantBoundaryMap(connection);

			List<String> tenants = fetchTenants(TARGET_TENANT);
			log.info("Identified {} active state tenants from MDMS", tenants.size());
			logToFile("Identified %d active state tenants from MDMS", tenants.size());

			for (String state : tenants) {
				log.info("Processing state tenant {}", state);
				logToFile("Processing state tenant %s", state);
				List<String> childTenants = fetchTenants(state);

				processComplainantsForState(childTenants);
				processStateLevelRoles(state);
			}

			log.info("HRMS tenant migration summary: evaluated={}, migrated={}, failures={}",
					totalEmployeesEvaluated, totalEmployeesMigrated, failures.size());
			logToFile("Migration summary: evaluated=%d, migrated=%d, failures=%d",
					totalEmployeesEvaluated, totalEmployeesMigrated, failures.size());
			if (!failures.isEmpty()) {
				failures.forEach(failure -> log.warn("Migration failure: {}", failure));
				failures.forEach(failure -> logToFile("FAILURE: %s", failure));
			}
			logSectionFooter();
		} finally {
			if (migrationLogger != null) {
				migrationLogger.flush();
				migrationLogger.close();
				migrationLogger = null;
			}
		}
	}

	private void initializeEnv() {
		mdmsHost = getEnvOrDefault("EGOV_MDMS_HOST", "http://localhost:8088");
		mdmsSearchEndpoint = getEnvOrDefault("EGOV_MDMS_SEARCH_ENDPOINT", "/egov-mdms-service/v1/_search");
		hrmsHost = getEnvOrDefault("EGOV_HRMS_HOST", "http://localhost:9999");
		hrmsSearchEndpoint = getEnvOrDefault("EGOV_HRMS_SEARCH_ENDPOINT", "/egov-hrms/employees/_search");
		hrmsCreateEndpoint = getEnvOrDefault("EGOV_HRMS_CREATE_ENDPOINT", "/egov-hrms/employees/_create");
	}

	private void loadFacilityTenantBoundaryMap(Connection connection) {
		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("SELECT tenant_id, boundary_code FROM facility_tenant_id_map")) {
			while (resultSet.next()) {
				facilityTenantBoundaryMap.put(resultSet.getString("tenant_id"), resultSet.getString("boundary_code"));
			}
			log.info("Loaded {} tenant boundary mappings from facility_tenant_id_map", facilityTenantBoundaryMap.size());
			logToFile("Loaded %d tenant boundary mappings from facility_tenant_id_map", facilityTenantBoundaryMap.size());
		} catch (Exception e) {
			String message = "Failed to load facility_tenant_id_map data: " + e.getMessage();
			log.error(message, e);
			recordFailure(message);
		}
	}

	private void processComplainantsForState(List<String> childTenants) {
		for (String tenant : childTenants) {
			String boundary = facilityTenantBoundaryMap.get(tenant);
			if (boundary == null || boundary.trim().isEmpty()) {
				recordFailure(String.format("No boundary mapping found for tenant %s", tenant));
				continue;
			}

			List<ObjectNode> employees = fetchEmployees(tenant, COMPLAINANT_ROLES);
			if (CollectionUtils.isEmpty(employees)) {
				continue;
			}

			log.info("Migrating {} complainant users for tenant {}", employees.size(), tenant);
			for (ObjectNode employee : employees) {
				migrateEmployee(employee, boundary, tenant);
			}
		}
	}

	private void processStateLevelRoles(String stateTenant) {
		String stateName = TENANT_TO_STATE.get(stateTenant);
		if (stateName == null) {
			recordFailure(String.format("No state mapping configured for tenant %s", stateTenant));
			return;
		}

		String boundary = "India_" + stateName.replace(" ", "_");
		List<ObjectNode> employees = fetchEmployees(stateTenant, STATE_LEVEL_ROLES);
		if (CollectionUtils.isEmpty(employees)) {
			return;
		}

		log.info("Migrating {} state-level users for tenant {}", employees.size(), stateTenant);
		for (ObjectNode employee : employees) {
			migrateEmployee(employee, boundary, stateTenant);
		}
	}

	private List<ObjectNode> fetchEmployees(String tenantId, List<String> roles) {
		List<ObjectNode> result = new ArrayList<>();
		int offset = 0;

		while (true) {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsSearchEndpoint)
					.queryParam("tenantId", tenantId)
					.queryParam("isActive", true)
					.queryParam("limit", HRMS_PAGE_SIZE)
					.queryParam("offset", offset);

			for (String role : roles) {
				builder.queryParam("roles", role);
			}

			try {
				ObjectNode body = objectMapper.createObjectNode();
				body.set("RequestInfo", buildRequestInfoBody());
				JsonNode response = postForJson(builder.toUriString(), body);
				ArrayNode employeesNode = response != null && response.has("Employees")
						? (ArrayNode) response.get("Employees") : null;

				if (employeesNode == null || employeesNode.isEmpty()) {
					break;
				}

				for (JsonNode node : employeesNode) {
					if (node instanceof ObjectNode) {
						result.add((ObjectNode) node);
					}
				}

				if (employeesNode.size() < HRMS_PAGE_SIZE) {
					break;
				}

				offset += HRMS_PAGE_SIZE;
			} catch (Exception e) {
				recordFailure(String.format("Failed to fetch employees for tenant %s roles %s : %s",
						tenantId, roles, e.getMessage()));
				break;
			}
		}

		return result;
	}

	private void migrateEmployee(ObjectNode employee, String boundary, String sourceTenant) {
		totalEmployeesEvaluated++;
		ArrayNode jurisdictions = (ArrayNode) employee.get("jurisdictions");
		if (jurisdictions == null || jurisdictions.isEmpty()) {
			recordFailure(String.format("Employee %s has no jurisdictions – skipping", employee.path("uuid").asText()));
			return;
		}

		logToFile("Migrating employee %s from %s with target boundary %s",
				employee.path("uuid").asText(), sourceTenant, boundary);

		updateTenantFields(employee, boundary);

		ObjectNode updatePayload = objectMapper.createObjectNode();
		updatePayload.set("RequestInfo", buildRequestInfoBody());
		ArrayNode employeesArray = objectMapper.createArrayNode();
		employeesArray.add(employee);
		updatePayload.set("Employees", employeesArray);

		try {
			postForJson(hrmsHost + hrmsCreateEndpoint, updatePayload);
			totalEmployeesMigrated++;
			log.info("Migrated employee {} from {} to {}", employee.path("uuid").asText(), sourceTenant, TARGET_TENANT);
			logToFile("Migrated employee %s from %s to %s",
					employee.path("uuid").asText(), sourceTenant, TARGET_TENANT);
		} catch (Exception e) {
			recordFailure(String.format("Failed to update employee %s : %s",
					employee.path("uuid").asText(), e.getMessage()));
		}
	}

	private void updateTenantFields(ObjectNode employee, String boundary) {
		employee.put("tenantId", TARGET_TENANT);

		ObjectNode userNode = (ObjectNode) employee.get("user");
		if (userNode != null) {
			userNode.put("tenantId", TARGET_TENANT);
			ArrayNode roles = (ArrayNode) userNode.get("roles");
			if (roles != null) {
				// Update all role tenants to TARGET_TENANT
				for (JsonNode roleNode : roles) {
					if (roleNode instanceof ObjectNode) {
						((ObjectNode) roleNode).put("tenantId", TARGET_TENANT);
					}
				}
				// Deduplicate roles by code to avoid "role tenant combination already exists" errors
				deduplicateRolesByCode(userNode, roles);
			}
		}

		ArrayNode jurisdictions = (ArrayNode) employee.get("jurisdictions");
		if (jurisdictions != null) {
			for (JsonNode jurisdictionNode : jurisdictions) {
				if (jurisdictionNode instanceof ObjectNode) {
					ObjectNode node = (ObjectNode) jurisdictionNode;
					node.put("tenantId", TARGET_TENANT);
					node.put("boundary", boundary);
				}
			}
		}

		updateTenantIdForArray(employee, "assignments");
		updateTenantIdForArray(employee, "education");
		updateTenantIdForArray(employee, "tests");
		updateTenantIdForArray(employee, "documents");
		updateTenantIdForArray(employee, "serviceHistory");
		updateTenantIdForArray(employee, "deactivationDetails");
		updateTenantIdForArray(employee, "reactivationDetails");
	}

	private void updateTenantIdForArray(ObjectNode employee, String fieldName) {
		ArrayNode arrayNode = (ArrayNode) employee.get(fieldName);
		if (arrayNode == null) {
			return;
		}
		for (JsonNode node : arrayNode) {
			if (node instanceof ObjectNode) {
				((ObjectNode) node).put("tenantId", TARGET_TENANT);
			}
		}
	}

	private void deduplicateRolesByCode(ObjectNode userNode, ArrayNode roles) {
		Set<String> seenRoleCodes = new HashSet<>();
		ArrayNode deduplicatedRoles = objectMapper.createArrayNode();
		
		for (JsonNode roleNode : roles) {
			if (roleNode instanceof ObjectNode) {
				JsonNode codeNode = roleNode.get("code");
				if (codeNode != null && !codeNode.isNull()) {
					String roleCode = codeNode.asText();
					if (!seenRoleCodes.contains(roleCode)) {
						seenRoleCodes.add(roleCode);
						deduplicatedRoles.add(roleNode);
					}
				}
			}
		}
		
		userNode.set("roles", deduplicatedRoles);
	}

	private List<String> fetchTenants(String tenantId) {
		List<String> activeTenantIds = new ArrayList<>();
		try {
			ObjectNode request = objectMapper.createObjectNode();
			request.set("RequestInfo", buildRequestInfoBody());

			ObjectNode criteria = objectMapper.createObjectNode();
			criteria.put("tenantId", tenantId);
			criteria.put("limit", MDMS_LIMIT);

			ArrayNode moduleDetails = objectMapper.createArrayNode();
			ObjectNode moduleDetail = objectMapper.createObjectNode();
			moduleDetail.put("moduleName", TENANT_MODULE_NAME);

			ArrayNode masterDetails = objectMapper.createArrayNode();
			ObjectNode masterDetail = objectMapper.createObjectNode();
			masterDetail.put("name", TENANT_MASTER_NAME);
			masterDetails.add(masterDetail);

			moduleDetail.set("masterDetails", masterDetails);
			moduleDetails.add(moduleDetail);
			criteria.set("moduleDetails", moduleDetails);

			request.set("MdmsCriteria", criteria);

			JsonNode response = postForJson(mdmsHost + mdmsSearchEndpoint, request);
			if (response == null) {
				return Collections.emptyList();
			}

			JsonNode tenantsNode = response.at("/MdmsRes/tenant/tenants");
			if (tenantsNode != null && tenantsNode.isArray()) {
				for (JsonNode tenantNode : tenantsNode) {
					String stateTenantId = textOrNull(tenantNode.path("code"));
					if (Strings.isBlank(stateTenantId) || "in".equalsIgnoreCase(stateTenantId)) {
						continue;
					}
					activeTenantIds.add(stateTenantId);
				}
				log.info("Found {} active state-level tenants: {}", activeTenantIds.size(), activeTenantIds);
				return activeTenantIds;
			}
			return activeTenantIds;
		} catch (Exception e) {
			String message = String.format("Failed to fetch tenants for %s : %s", tenantId, e.getMessage());
			recordFailure(message);
			return Collections.emptyList();
		}
	}

	private String textOrNull(JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return null;
		}
		String value = node.asText(null);
		return (value == null || value.trim().isEmpty()) ? null : value.trim();
	}

	private JsonNode postForJson(String url, JsonNode body) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
			ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				throw new IllegalStateException("Non-success response " + response.getStatusCode());
			}
			return objectMapper.readTree(response.getBody());
		} catch (Exception e) {
			String message = "HTTP POST failed for url " + url + " : " + e.getMessage();
			recordFailure(message);
			throw new RuntimeException(message, e);
		}
	}

	private ObjectNode buildRequestInfoBody() {
		return objectMapper.valueToTree(requestInfo);
	}

	private RequestInfo buildRequestInfo() {
		RequestInfo info = new RequestInfo();
		info.setApiId("HRMS-TENANT-MIGRATION");
		info.setVer("1.0");
		info.setTs(Instant.now().toEpochMilli());
		info.setAction("_update");
		info.setDid("migration-job");
		info.setKey("");
		info.setMsgId(UUID.randomUUID().toString());
		info.setAuthToken(getEnvOrDefault("EGOV_AUTH_TOKEN", ""));

		User user = new User();
		user.setUuid(getEnvOrDefault("EGOV_AUTH_USER_UUID", "00000000-0000-0000-0000-000000000001"));
		user.setUserName(getEnvOrDefault("EGOV_AUTH_USERNAME", "tenant-migration"));
		user.setName("Tenant Migration User");
		user.setMobileNumber(getEnvOrDefault("EGOV_AUTH_MOBILE", "9999999999"));
		user.setType("EMPLOYEE");
		user.setTenantId(TARGET_TENANT);

		Role role = new Role();
		role.setCode(getEnvOrDefault("EGOV_AUTH_ROLE_CODE", "ADMIN"));
		role.setName(getEnvOrDefault("EGOV_AUTH_ROLE_NAME", "Admin"));

		user.setRoles(Collections.singletonList(role));
		info.setUserInfo(user);
		return info;
	}

	private PrintWriter initializeMigrationLogger(Path path) throws IOException {
		return new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
	}

	private void logToFile(String format, Object... args) {
		if (migrationLogger == null) {
			return;
		}
		migrationLogger.printf((format) + "%n", args);
		migrationLogger.flush();
	}

	private void recordFailure(String message) {
		failures.add(message);
		logToFile("FAILURE: %s", message);
	}

	private void logSectionHeader() {
		logToFile("========================================");
		logToFile("HRMS EMPLOYEE MIGRATION LOG");
		logToFile("Log File: %s", logFilePath);
		logToFile("Start Time: %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		logToFile("========================================");
	}

	private void logSectionFooter() {
		logToFile("----------------------------------------");
		logToFile("End Time: %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		logToFile("Log File: %s", logFilePath);
		logToFile("========================================");
	}

	private String getEnvOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		return (value == null || value.isEmpty()) ? defaultValue : value;
	}
}

