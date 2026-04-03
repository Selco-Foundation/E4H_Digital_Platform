package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class V20251121123000__migrate_escalation_users extends BaseJavaMigration {

	private static final String TARGET_TENANT = "in";
	private static final int MDMS_LIMIT = 300;
	private static final int HRMS_PAGE_SIZE = 200;
	private static final String TENANT_MODULE_NAME = "tenant";
	private static final String TENANT_MASTER_NAME = "tenants";

	// State-level roles to migrate
	private static final List<String> STATE_POC_ROLES = Arrays.asList(
			"STATE_POC",
			"CENTRAL_POC",
			"CENTRAL_ONM_PROJECT_MANAGER",
			"SENIOR_PROGRAM_MANAGER"
	);

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
		String logFileName = "escalation_users_migration_" + LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
		logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

		try (PrintWriter logger = initializeMigrationLogger(logFilePath);
		     Connection connection = context.getConfiguration().getDataSource().getConnection()) {
			this.migrationLogger = logger;
			logSectionHeader();

			Set<String> activeStateTenants = new HashSet<>(fetchActiveStateTenants(TARGET_TENANT));
			if (activeStateTenants.isEmpty()) {
				String message = "No active tenants returned by MDMS for base tenant " + TARGET_TENANT;
				logFailure(message);
				throw new IllegalStateException(message);
			}
			log.info("Active state tenants from MDMS: {}", activeStateTenants);
			logToFile("Active state tenants from MDMS: %s", activeStateTenants);

			// Process each state tenant
			for (String stateTenant : activeStateTenants) {
				log.info("Processing state tenant: {}", stateTenant);
				logToFile("Processing state tenant: %s", stateTenant);
				processStateLevelUsers(stateTenant);
			}

			log.info("State POC users migration summary: evaluated={}, migrated={}, failures={}",
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
		hrmsCreateEndpoint = getEnvOrDefault("EGOV_HRMS_UPDATE_ENDPOINT", "/egov-hrms/employees/_create");
	}

	private void processStateLevelUsers(String stateTenant) {
		String stateName = TENANT_TO_STATE.get(stateTenant);
		if (stateName == null) {
			// Fallback: use tenant code as state name
			stateName = stateTenant.toUpperCase();
			log.warn("No state mapping found for tenant {}, using {} as state name", stateTenant, stateName);
			logToFile("No state mapping found for tenant %s, using %s as state name", stateTenant, stateName);
		}

		String boundary = "India_" + stateName.replace(" ", "_");

		// Search for employees with any of the POC roles in this state tenant
		for (String roleCode : STATE_POC_ROLES) {
			List<ObjectNode> employees = searchEmployeesByRole(stateTenant, roleCode);
			if (employees == null || employees.isEmpty()) {
				continue;
			}

			log.info("Found {} employees with role {} in tenant {}", employees.size(), roleCode, stateTenant);
			logToFile("Found %d employees with role %s in tenant %s", employees.size(), roleCode, stateTenant);

			for (ObjectNode employee : employees) {
				totalEmployeesEvaluated++;
				if (updateEmployeeForMigration(employee, boundary, stateTenant)) {
					if (submitEmployeeUpdate(employee)) {
						totalEmployeesMigrated++;
					}
				}
			}
		}
	}

	private List<ObjectNode> searchEmployeesByRole(String tenantId, String roleCode) {
		List<ObjectNode> employees = new ArrayList<>();
		int offset = 0;

		while (true) {
			try {
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsSearchEndpoint)
						.queryParam("tenantId", tenantId)
						.queryParam("roles", roleCode)
						.queryParam("isActive", true)
						.queryParam("limit", HRMS_PAGE_SIZE)
						.queryParam("offset", offset);

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
						employees.add((ObjectNode) node);
					}
				}

				if (employeesNode.size() < HRMS_PAGE_SIZE) {
					break;
				}
				offset += HRMS_PAGE_SIZE;
			} catch (Exception e) {
				log.warn("Failed to fetch employees for tenant {} role {} : {}", tenantId, roleCode, e.getMessage());
				logToFile("Failed to fetch employees for tenant %s role %s : %s", tenantId, roleCode, e.getMessage());
				break;
			}
		}

		return employees;
	}

	private boolean updateEmployeeForMigration(ObjectNode employee, String boundary, String sourceStateTenant) {
		ArrayNode jurisdictions = (ArrayNode) employee.get("jurisdictions");
		if (jurisdictions == null || jurisdictions.isEmpty()) {
			recordFailure(String.format("Employee %s has no jurisdictions – skipping",
					textOrNull(employee.get("uuid"))));
			return false;
		}

		logToFile("Migrating employee %s from %s with target boundary %s",
				textOrNull(employee.get("uuid")), sourceStateTenant, boundary);

		if (jurisdictions != null) {
			for (JsonNode jurisdictionNode : jurisdictions) {
				if (jurisdictionNode instanceof ObjectNode) {
					ObjectNode node = (ObjectNode) jurisdictionNode;
					node.put("tenantId", TARGET_TENANT);
					node.put("boundary", boundary);
					node.put("boundaryType", "State");
					if (node.has("isActive")) {
						node.put("isActive", true);
					}
				}
			}
		}

		updateTenantFields(employee);
		return true;
	}

	private boolean updateTenantFields(ObjectNode employee) {
		boolean mutated = false;

		String currentTenant = textOrNull(employee.get("tenantId"));
		if (!TARGET_TENANT.equalsIgnoreCase(currentTenant)) {
			employee.put("tenantId", TARGET_TENANT);
			mutated = true;
		}
		if (employee.has("tenantid") && !TARGET_TENANT.equalsIgnoreCase(textOrNull(employee.get("tenantid")))) {
			employee.put("tenantid", TARGET_TENANT);
			mutated = true;
		}

		ObjectNode userNode = (ObjectNode) employee.get("user");
		if (userNode != null) {
			if (setTenantId(userNode)) {
				mutated = true;
			}

			ArrayNode roles = (ArrayNode) userNode.get("roles");
			if (roles != null) {
				for (JsonNode roleNode : roles) {
					if (roleNode instanceof ObjectNode) {
						if (setTenantId((ObjectNode) roleNode)) {
							mutated = true;
						}
					}
				}
				if (dedupeUserRoles(userNode)) {
					mutated = true;
				}
			}
		}

		mutated |= updateTenantIdForArray(employee, "assignments");
		mutated |= updateTenantIdForArray(employee, "education");
		mutated |= updateTenantIdForArray(employee, "tests");
		mutated |= updateTenantIdForArray(employee, "documents");
		mutated |= updateTenantIdForArray(employee, "serviceHistory");
		mutated |= updateTenantIdForArray(employee, "deactivationDetails");
		mutated |= updateTenantIdForArray(employee, "reactivationDetails");

		return mutated;
	}

	private boolean updateTenantIdForArray(ObjectNode employee, String fieldName) {
		ArrayNode array = (ArrayNode) employee.get(fieldName);
		if (array == null) {
			return false;
		}

		boolean mutated = false;
		for (JsonNode node : array) {
			if (node instanceof ObjectNode) {
				if (setTenantId((ObjectNode) node)) {
					mutated = true;
				}
			}
		}
		return mutated;
	}

	private boolean setTenantId(ObjectNode node) {
		if (node == null) {
			return false;
		}
		boolean mutated = false;
		if (!TARGET_TENANT.equalsIgnoreCase(textOrNull(node.get("tenantId")))) {
			node.put("tenantId", TARGET_TENANT);
			mutated = true;
		}
		if (node.has("tenantid") && !TARGET_TENANT.equalsIgnoreCase(textOrNull(node.get("tenantid")))) {
			node.put("tenantid", TARGET_TENANT);
			mutated = true;
		}
		return mutated;
	}

	private boolean dedupeUserRoles(ObjectNode userNode) {
		ArrayNode roles = (ArrayNode) userNode.get("roles");
		if (roles == null || roles.size() <= 1) {
			return false;
		}

		LinkedHashMap<String, ObjectNode> uniqueByCode = new LinkedHashMap<>();
		for (JsonNode roleNode : roles) {
			if (!(roleNode instanceof ObjectNode)) {
				continue;
			}
			ObjectNode roleObj = (ObjectNode) roleNode;
			String code = textOrNull(roleObj.get("code"));
			if (StringUtils.isBlank(code)) {
				continue;
			}
			String key = code.trim().toUpperCase(Locale.ROOT);
			uniqueByCode.putIfAbsent(key, roleObj);
		}

		if (uniqueByCode.size() == roles.size()) {
			return false;
		}

		ArrayNode dedupedRoles = objectMapper.createArrayNode();
		for (ObjectNode role : uniqueByCode.values()) {
			dedupedRoles.add(role);
		}
		userNode.set("roles", dedupedRoles);
		return true;
	}

	private boolean submitEmployeeUpdate(ObjectNode employee) {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.set("RequestInfo", buildRequestInfoBody());
		ArrayNode array = objectMapper.createArrayNode();
		array.add(employee);
		payload.set("Employees", array);

		try {
			String employeeUuid = textOrNull(employee.get("uuid"));
			updateEmployeeWithRetry(payload, employeeUuid);
			log.info("Updated employee {}", employeeUuid);
			logToFile("Updated employee %s", employeeUuid);
			return true;
		} catch (Exception e) {
			String employeeUuid = textOrNull(employee.get("uuid"));
			recordFailure(String.format("Failed to update employee %s : %s", employeeUuid, e.getMessage()));
			return false;
		}
	}

	private void updateEmployeeWithRetry(ObjectNode updatePayload, String employeeUuid) throws Exception {
		int maxRetries = 6;
		int[] delaysInSeconds = {1, 7, 15, 25, 30};

		Exception lastException = null;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				postForJsonWithoutRecording(hrmsHost + hrmsCreateEndpoint, updatePayload);
				if (attempt > 1) {
					log.info("Successfully updated employee {} on attempt {}", employeeUuid, attempt);
					logToFile("Successfully updated employee %s on attempt %d", employeeUuid, attempt);
				}
				return; // Success
			} catch (Exception e) {
				lastException = e;
				String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

				// Check if user already exists - skip retry logic for these specific errors
				if (errorMessage.contains("err_hrms_user_exist_mob") ||
					errorMessage.contains("err_hrms_user_exist_username") ||
					errorMessage.contains("already exists for the entered mobile number") ||
					errorMessage.contains("already exists for the entered user name") ||
					errorMessage.contains("user already exists with same username and mobile number") ||
					errorMessage.contains("user already exists with the same mobile number") ||
					errorMessage.contains("user already exists with the same username") ||
					errorMessage.contains("duplicate key") ||
					errorMessage.contains("unique constraint violation")) {
					log.warn("User already exists for employee {} (detected duplicate user error), skipping retry", employeeUuid);
					logToFile("User already exists for employee %s (detected duplicate user error), skipping retry", employeeUuid);
					throw e; // Don't retry, propagate the exception
				}

				// If this was the last attempt, throw the exception
				if (attempt >= maxRetries) {
					log.error("Failed to update employee {} after {} attempts", employeeUuid, maxRetries);
					logToFile("Failed to update employee %s after %d attempts", employeeUuid, maxRetries);
					throw e;
				}

				// Otherwise, log and retry with delay
				int delaySeconds = delaysInSeconds[attempt - 1];
				log.warn("Attempt {} failed for employee {}: {}. Retrying in {} seconds...",
						attempt, employeeUuid, e.getMessage(), delaySeconds);
				logToFile("Attempt %d failed for employee %s: %s. Retrying in %d seconds...",
						attempt, employeeUuid, e.getMessage(), delaySeconds);

				try {
					Thread.sleep(delaySeconds * 1000L);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Migration interrupted during retry delay", ie);
				}
			}
		}

		// This should never be reached due to the throw in the loop, but just in case
		throw lastException;
	}

	private List<String> fetchActiveStateTenants(String tenantId) {
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

			List<String> tenantIds = new ArrayList<>();
			JsonNode tenantsNode = response.at("/MdmsRes/tenant/tenants");
			if (tenantsNode != null && tenantsNode.isArray()) {
				for (JsonNode tenantNode : tenantsNode) {
					String code = textOrNull(tenantNode.path("code"));
					if (StringUtils.isBlank(code) || TARGET_TENANT.equalsIgnoreCase(code)) {
						continue;
					}
					tenantIds.add(code);
				}
			}
			return tenantIds;
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch tenants from MDMS: " + e.getMessage(), e);
		}
	}

	private String textOrNull(JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return null;
		}
		String value = node.asText(null);
		return value == null ? null : value.trim().isEmpty() ? null : value.trim();
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

	private JsonNode postForJsonWithoutRecording(String url, JsonNode body) {
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
			// Don't record failure here - let the retry logic handle it
			throw new RuntimeException(message, e);
		}
	}

	private ObjectNode buildRequestInfoBody() {
		return objectMapper.valueToTree(requestInfo);
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

	private void logFailure(String message) {
		recordFailure(message);
	}

	private void logSectionHeader() {
		logToFile("========================================");
		logToFile("STATE POC USERS MIGRATION LOG");
		logToFile("Log File: %s", logFilePath);
		logToFile("Start Time: %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		logToFile("Target Tenant: %s", TARGET_TENANT);
		logToFile("Target Roles: %s", String.join(", ", STATE_POC_ROLES));
		logToFile("Boundary Type: State Level");
		logToFile("========================================");
	}

	private void logSectionFooter() {
		logToFile("----------------------------------------");
		logToFile("End Time: %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		logToFile("Log File: %s", logFilePath);
		logToFile("========================================");
	}

	private RequestInfo buildRequestInfo() {
		RequestInfo info = new RequestInfo();
		info.setApiId("STATE-POC-USERS-MIGRATION");
		info.setVer("1.0");
		info.setTs(Instant.now().toEpochMilli());
		info.setAction("_update");
		info.setDid("state-poc-migration-job");
		info.setKey("");
		info.setMsgId(UUID.randomUUID().toString());
		info.setAuthToken(getEnvOrDefault("EGOV_AUTH_TOKEN", ""));

		User user = new User();
		user.setUuid(getEnvOrDefault("EGOV_AUTH_USER_UUID", "00000000-0000-0000-0000-000000000001"));
		user.setUserName(getEnvOrDefault("EGOV_AUTH_USERNAME", "state-poc-migration"));
		user.setName("State POC Migration User");
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

	private String getEnvOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		return (value == null || value.isEmpty()) ? defaultValue : value;
	}
}

