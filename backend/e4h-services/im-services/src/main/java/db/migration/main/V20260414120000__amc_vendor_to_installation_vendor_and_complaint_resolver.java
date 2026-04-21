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

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 1) For each {@code eg_org_user} linked to an organisation with {@code org_type = VENDOR}
 * and {@code org_subtype = AMC_VENDOR}, adds the COMPLAINT_RESOLVER role (tenant {@code in}) in HRMS.
 * 2) Updates all matching {@code eg_org} rows to {@code org_subtype = INSTALLATION_VENDOR}.
 */
@Slf4j
public class V20260414120000__amc_vendor_to_installation_vendor_and_complaint_resolver extends BaseJavaMigration {

	private static final String TARGET_TENANT = "in";
	private static final String ORG_TYPE_VENDOR = "VENDOR";
	private static final String ORG_SUBTYPE_AMC = "AMC_VENDOR";
	private static final String ORG_SUBTYPE_INSTALLATION = "INSTALLATION_VENDOR";
	private static final String COMPLAINT_RESOLVER_CODE = "COMPLAINT_RESOLVER";
	private static final String COMPLAINT_RESOLVER_NAME = "Complaint Resolver";

	private static final String SELECT_AMC_VENDOR_USER_IDS =
			"SELECT DISTINCT TRIM(ou.userid) AS uid "
					+ "FROM eg_org_user ou "
					+ "INNER JOIN eg_org org ON org.id = ou.organizationid "
					+ "WHERE org.org_type = ? "
					+ "AND org.org_subtype = ? "
					+ "AND (ou.isdeleted = false OR ou.isdeleted IS NULL) "
					+ "AND ou.userid IS NOT NULL "
					+ "AND TRIM(ou.userid) <> ''";

	private static final String UPDATE_ORG_SUBTYPE =
			"UPDATE eg_org SET org_subtype = ? "
					+ "WHERE org_type = ? AND org_subtype = ?";

	private static final long DELAY_BETWEEN_HRMS_MS = 50L;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String hrmsHost;
	private String hrmsSearchEndpoint;
	private String hrmsUpdateEndpoint;
	private RequestInfo requestInfo;
	private PrintWriter migrationLogger;
	private Path logFilePath;

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
		String logFileName = "amc_vendor_complaint_resolver_migration_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
		logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

		try (PrintWriter logger = new PrintWriter(Files.newBufferedWriter(logFilePath, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
		     Connection connection = context.getConfiguration().getDataSource().getConnection()) {
			this.migrationLogger = logger;
			logToFile("AMC_VENDOR -> INSTALLATION_VENDOR + COMPLAINT_RESOLVER migration");
			logToFile("Log file: %s", logFilePath);

			List<String> userIds = loadDistinctUserIds(connection);
			log.info("Found {} distinct org users linked to AMC_VENDOR organisations", userIds.size());
			logToFile("User ids to process: %d", userIds.size());

			int rolesAdded = 0;
			int skippedAlreadyHadRole = 0;
			int skippedNoEmployee = 0;

			for (String userId : userIds) {
				try {
					ObjectNode employee = searchEmployeeByUserUuid(userId);
					if (employee == null) {
						skippedNoEmployee++;
						recordFailure("No HRMS employee found for user uuid: " + userId);
						continue;
					}
					ObjectNode userNode = (ObjectNode) employee.get("user");
					if (userNode == null) {
						recordFailure("Employee has no user node for uuid: " + userId);
						skippedNoEmployee++;
						continue;
					}
					if (userHasComplaintResolverForIn(userNode)) {
						skippedAlreadyHadRole++;
						continue;
					}
					addComplaintResolverRole(userNode);
					employee.set("user", userNode);
					submitEmployeeUpdate(employee);
					rolesAdded++;
					sleepQuietly(DELAY_BETWEEN_HRMS_MS);
				} catch (Exception e) {
					recordFailure("Failed processing user " + userId + ": " + e.getMessage());
					log.warn("Failed processing user {}", userId, e);
				}
			}

			int orgRowsUpdated = updateOrganisationSubtypes(connection);
			log.info("HRMS: rolesAdded={}, skippedAlreadyHadRole={}, skippedNoEmployee={}, orgRowsUpdated={}",
					rolesAdded, skippedAlreadyHadRole, skippedNoEmployee, orgRowsUpdated);
			logToFile("Summary: rolesAdded=%d, skippedAlreadyHadRole=%d, skippedNoEmployee=%d, orgRowsUpdated=%d, failures=%d",
					rolesAdded, skippedAlreadyHadRole, skippedNoEmployee, orgRowsUpdated, failures.size());
			failures.forEach(f -> logToFile("FAILURE: %s", f));
		} finally {
			if (migrationLogger != null) {
				migrationLogger.flush();
				migrationLogger.close();
				migrationLogger = null;
			}
		}
	}

	private List<String> loadDistinctUserIds(Connection connection) throws Exception {
		Set<String> ordered = new LinkedHashSet<>();
		try (PreparedStatement ps = connection.prepareStatement(SELECT_AMC_VENDOR_USER_IDS)) {
			ps.setString(1, ORG_TYPE_VENDOR);
			ps.setString(2, ORG_SUBTYPE_AMC);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String uid = rs.getString("uid");
					if (StringUtils.isNotBlank(uid)) {
						ordered.add(uid.trim());
					}
				}
			}
		}
		return new ArrayList<>(ordered);
	}

	private int updateOrganisationSubtypes(Connection connection) throws Exception {
		try (PreparedStatement ps = connection.prepareStatement(UPDATE_ORG_SUBTYPE)) {
			ps.setString(1, ORG_SUBTYPE_INSTALLATION);
			ps.setString(2, ORG_TYPE_VENDOR);
			ps.setString(3, ORG_SUBTYPE_AMC);
			return ps.executeUpdate();
		}
	}

	private void initializeEnv() {
		hrmsHost = getEnvOrDefault("EGOV_HRMS_HOST", "http://localhost:8090");
		hrmsSearchEndpoint = getEnvOrDefault("EGOV_HRMS_SEARCH_ENDPOINT", "/egov-hrms/employees/_search");
		hrmsUpdateEndpoint = getEnvOrDefault("EGOV_HRMS_UPDATE_ENDPOINT", "/egov-hrms/employees/_update");
	}

	private ObjectNode searchEmployeeByUserUuid(String userUuid) {
		String url = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsSearchEndpoint)
				.queryParam("tenantId", TARGET_TENANT)
				.queryParam("uuids", userUuid)
				.queryParam("limit", 1)
				.queryParam("offset", 0)
				.toUriString();
		ObjectNode body = objectMapper.createObjectNode();
		body.set("RequestInfo", buildRequestInfoBody());
		try {
			JsonNode response = postForJson(url, body);
			ArrayNode employees = response != null && response.has("Employees")
					? (ArrayNode) response.get("Employees") : null;
			if (employees != null && !employees.isEmpty() && employees.get(0) instanceof ObjectNode) {
				return (ObjectNode) employees.get(0);
			}
		} catch (Exception e) {
			log.debug("HRMS search failed for {}: {}", userUuid, e.getMessage());
		}
		return null;
	}

	private boolean userHasComplaintResolverForIn(ObjectNode userNode) {
		ArrayNode roles = (ArrayNode) userNode.get("roles");
		if (roles == null) {
			return false;
		}
		for (JsonNode r : roles) {
			if (!(r instanceof ObjectNode)) {
				continue;
			}
			String code = textOrNull(r.get("code"));
			String tenant = textOrNull(r.get("tenantId"));
			if (tenant == null) {
				tenant = textOrNull(r.get("tenantid"));
			}
			if (COMPLAINT_RESOLVER_CODE.equalsIgnoreCase(code) && TARGET_TENANT.equalsIgnoreCase(tenant)) {
				return true;
			}
		}
		return false;
	}

	private void addComplaintResolverRole(ObjectNode userNode) {
		ArrayNode roles = (ArrayNode) userNode.get("roles");
		if (roles == null) {
			roles = objectMapper.createArrayNode();
			userNode.set("roles", roles);
		}
		ObjectNode role = objectMapper.createObjectNode();
		role.put("name", COMPLAINT_RESOLVER_NAME);
		role.put("code", COMPLAINT_RESOLVER_CODE);
		role.putNull("description");
		role.put("tenantId", TARGET_TENANT);
		roles.add(role);
		dedupeRolesByCodeAndTenant(userNode);
	}

	private void dedupeRolesByCodeAndTenant(ObjectNode userNode) {
		ArrayNode roles = (ArrayNode) userNode.get("roles");
		if (roles == null || roles.size() <= 1) {
			return;
		}
		Set<String> seen = new LinkedHashSet<>();
		ArrayNode out = objectMapper.createArrayNode();
		for (JsonNode r : roles) {
			if (!(r instanceof ObjectNode)) {
				continue;
			}
			ObjectNode o = (ObjectNode) r;
			String code = textOrNull(o.get("code"));
			String tenant = textOrNull(o.get("tenantId"));
			if (tenant == null) {
				tenant = textOrNull(o.get("tenantid"));
			}
			if (StringUtils.isBlank(code)) {
				out.add(o);
				continue;
			}
			String key = code.trim().toUpperCase(Locale.ROOT) + "|" + StringUtils.defaultString(tenant, "");
			if (seen.add(key)) {
				out.add(o);
			}
		}
		userNode.set("roles", out);
	}

	private void submitEmployeeUpdate(ObjectNode employee) throws Exception {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.set("RequestInfo", buildRequestInfoBody());
		payload.put("key", "UPDATE");
		payload.put("action", "UPDATE");
		ArrayNode employees = objectMapper.createArrayNode();
		employees.add(employee);
		payload.set("Employees", employees);

		String url = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsUpdateEndpoint)
				.queryParam("tenantId", TARGET_TENANT)
				.toUriString();
		postForJsonWithoutRecording(url, payload);
	}

	private JsonNode postForJson(String url, JsonNode body) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("Non-success response " + response.getStatusCode());
		}
		return objectMapper.readTree(response.getBody());
	}

	private JsonNode postForJsonWithoutRecording(String url, JsonNode body) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("Non-success response " + response.getStatusCode());
		}
		return objectMapper.readTree(response.getBody());
	}

	private ObjectNode buildRequestInfoBody() {
		return objectMapper.valueToTree(requestInfo);
	}

	private RequestInfo buildRequestInfo() {
		RequestInfo info = new RequestInfo();
		info.setApiId("AMC-VENDOR-SUBTYPE-MIGRATION");
		info.setVer("1.0");
		info.setTs(Instant.now().toEpochMilli());
		info.setAction("_update");
		info.setDid("amc-vendor-subtype-migration");
		info.setKey("");
		info.setMsgId(UUID.randomUUID().toString());
		info.setAuthToken(getEnvOrDefault("EGOV_AUTH_TOKEN", ""));

		User user = new User();
		user.setUuid(getEnvOrDefault("EGOV_AUTH_USER_UUID", "00000000-0000-0000-0000-000000000001"));
		user.setUserName(getEnvOrDefault("EGOV_AUTH_USERNAME", "amc-vendor-migration"));
		user.setName("AMC vendor migration");
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

	private String textOrNull(JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return null;
		}
		String value = node.asText(null);
		return value == null ? null : value.trim().isEmpty() ? null : value.trim();
	}

	private void recordFailure(String message) {
		failures.add(message);
	}

	private void logToFile(String format, Object... args) {
		if (migrationLogger == null) {
			return;
		}
		migrationLogger.printf((format) + "%n", args);
		migrationLogger.flush();
	}

	private static void sleepQuietly(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
