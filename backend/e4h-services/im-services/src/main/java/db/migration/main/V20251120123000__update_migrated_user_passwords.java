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
public class V20251120123000__update_migrated_user_passwords extends BaseJavaMigration {

	private static final String TARGET_TENANT = "in";
	private static final String DEFAULT_PASSWORD = "Health@2026";
	private static final String VENDOR_ROLE = "COMPLAINT_RESOLVER";
	private static final int USER_SEARCH_LIMIT = 100;
	private static final int PASSWORD_UPDATE_BATCH_SIZE = 50; // Process 50 users, then log progress
	private static final long DELAY_BETWEEN_UPDATES_MS = 100; // 100ms delay between password updates
	
	// Only update passwords for users with these migrated roles
	private static final List<String> MIGRATED_ROLES = Arrays.asList(
			"COMPLAINANT",
			"COMPLAINT_ASSESSOR",
			"COMPLAINT_FACILITATOR_1",
			"COMPLAINT_FACILITATOR_2"
	);
	
	// Vendor username to production password mapping
	private static final Map<String, String> VENDOR_PASSWORDS;
	static {
		Map<String, String> vendorMap = new LinkedHashMap<>();
		vendorMap.put("selcoindia", "Energy@123");
		vendorMap.put("mediwave", "Mediwave@134#");
		vendorMap.put("techsar", "Techsar@234#");
		vendorMap.put("reapsotar", "Reapsotar@123#");
		vendorMap.put("sukruthi", "Sukruthi@123#");
		vendorMap.put("solarth", "Solarth@123#");
		vendorMap.put("aboriginal", "Aboriginal@915#");
		vendorMap.put("adsolar", "Adsolar@913#");
		vendorMap.put("earthners", "Earthners@123#");
		vendorMap.put("solarworld", "Solarworld@916#");
		vendorMap.put("agni", "Agni@321#");
		vendorMap.put("krishnapower", "Krishna@987#");
		vendorMap.put("thaldo", "Thaldo@342#");
		vendorMap.put("ans", "Ans@334#");
		vendorMap.put("ers", "Ers@123#");
		vendorMap.put("snl", "Snl@786#");
		vendorMap.put("dkk3sns", "Dkk3@123#");
		vendorMap.put("balaji", "Balaji@123#");
		vendorMap.put("onergy", "Onergy@927#");
		vendorMap.put("solarinfra", "Solar@342#");
		vendorMap.put("narayana", "Narayan@987#");
		vendorMap.put("sunpay", "Sun@921#");
		vendorMap.put("pap", "Pap@432#");
		vendorMap.put("tarini", "Tarini@432#");
		vendorMap.put("elite", "Elite@231#");
		vendorMap.put("kumudini", "Kumudini@234#");
		vendorMap.put("greengold", "Greengold@126#");
		vendorMap.put("solaryte", "Solar@332#");
		vendorMap.put("renesys", "Renesys@143#");
		vendorMap.put("kirloskar", "Kirloskar@989!");
		vendorMap.put("eres", "Eres@789#");
		vendorMap.put("gcep", "Gcep@123#");
		vendorMap.put("lits", "Lits@456#");
		vendorMap.put("mangaal", "Mangaal@123#");
		VENDOR_PASSWORDS = Collections.unmodifiableMap(vendorMap);
	}

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String userHost;
	private String userSearchEndpoint;
	private String userUpdateEndpoint;

	private RequestInfo requestInfo;
	private PrintWriter migrationLogger;
	private Path logFilePath;

	private int totalUsersEvaluated = 0;
	private int totalUsersUpdated = 0;
	private int totalVendorsUpdated = 0;
	private int totalExistingUsersSkipped = 0;
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
		String logFileName = "update_user_passwords_" + LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
		logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

		try (PrintWriter logger = initializeMigrationLogger(logFilePath);
		     Connection connection = context.getConfiguration().getDataSource().getConnection()) {
			this.migrationLogger = logger;
			logSectionHeader();

			log.info("Starting password update migration for users in tenant '{}'", TARGET_TENANT);
			logToFile("Starting password update migration for users in tenant '%s'", TARGET_TENANT);

			// Fetch users from tenant "in" with migrated roles and vendor role
			log.info("Fetching users with roles: {}", String.join(", ", MIGRATED_ROLES) + ", " + VENDOR_ROLE);
			logToFile("Fetching users with roles: %s", String.join(", ", MIGRATED_ROLES) + ", " + VENDOR_ROLE);
			
			List<ObjectNode> users = fetchAllUsers();
			log.info("Found {} users with target roles in tenant '{}'", users.size(), TARGET_TENANT);
			logToFile("Found %d users with target roles in tenant '%s'", users.size(), TARGET_TENANT);

			for (ObjectNode userNode : users) {
				totalUsersEvaluated++;
				
				String userUuid = textOrNull(userNode.get("uuid"));
				String userName = textOrNull(userNode.get("userName"));
				
				if (StringUtils.isBlank(userUuid)) {
					recordFailure("User has no UUID - skipping");
					continue;
				}

				// Check if user is a vendor (has COMPLAINT_RESOLVER role)
				boolean isVendor = isVendorUser(userNode);
				
				if (isVendor) {
					// Update vendor password with their specific password
					boolean success = updateVendorPassword(userNode, userUuid, userName);
					if (success) {
						totalVendorsUpdated++;
					}
					// Small delay to avoid overwhelming the API
					Thread.sleep(DELAY_BETWEEN_UPDATES_MS);
					continue;
				}

				// Check if user has any of the migrated roles
				boolean hasMigratedRole = hasMigratedRole(userNode);
				
				if (!hasMigratedRole) {
					totalExistingUsersSkipped++;
					log.info("Skipping existing user {} ({}) - no migrated roles", userName, userUuid);
					logToFile("Skipping existing user %s (%s) - no migrated roles", userName, userUuid);
					continue;
				}

				// Update password for migrated non-vendor users
				boolean success = updateUserPassword(userNode, userUuid, userName, DEFAULT_PASSWORD);
				if (success) {
					totalUsersUpdated++;
				}
				
				// Small delay to avoid overwhelming the API
				Thread.sleep(DELAY_BETWEEN_UPDATES_MS);
				
				// Log progress every batch
				if (totalUsersEvaluated % PASSWORD_UPDATE_BATCH_SIZE == 0) {
					log.info("Progress: Evaluated {}/{} users, Updated {} regular users, Updated {} vendors",
							totalUsersEvaluated, users.size(), totalUsersUpdated, totalVendorsUpdated);
					logToFile("Progress: Evaluated %d/%d users, Updated %d regular users, Updated %d vendors",
							totalUsersEvaluated, users.size(), totalUsersUpdated, totalVendorsUpdated);
				}
			}

			log.info("Password update migration summary: evaluated={}, updated={}, vendors_updated={}, existing_users_skipped={}, failures={}",
					totalUsersEvaluated, totalUsersUpdated, totalVendorsUpdated, totalExistingUsersSkipped, failures.size());
			logToFile("Migration summary: evaluated=%d, updated=%d, vendors_updated=%d, existing_users_skipped=%d, failures=%d",
					totalUsersEvaluated, totalUsersUpdated, totalVendorsUpdated, totalExistingUsersSkipped, failures.size());
			
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
		userHost = getEnvOrDefault("EGOV_USER_HOST", "http://localhost:8284");
		userSearchEndpoint = getEnvOrDefault("EGOV_USER_SEARCH_ENDPOINT", "/user/_search");
		userUpdateEndpoint = getEnvOrDefault("EGOV_USER_UPDATE_ENDPOINT", "/user/users/_updatenovalidate");
	}

	private List<ObjectNode> fetchAllUsers() {
		List<ObjectNode> allUsers = new ArrayList<>();
		int offset = 0;

		while (true) {
			try {
				// Build request body with search criteria
				ObjectNode requestBody = objectMapper.createObjectNode();
				requestBody.set("RequestInfo", buildRequestInfoBody());
				requestBody.put("tenantId", TARGET_TENANT);
				requestBody.put("active", true);
				requestBody.put("pageSize", USER_SEARCH_LIMIT);
				requestBody.put("offset", offset);
				
				// Add role codes to filter only users with migrated roles and vendors
				ArrayNode roleCodes = objectMapper.createArrayNode();
				for (String role : MIGRATED_ROLES) {
					roleCodes.add(role);
				}
				roleCodes.add(VENDOR_ROLE);
				requestBody.set("roleCodes", roleCodes);

				// Make POST request with search criteria in body
				String url = userHost + userSearchEndpoint;
				JsonNode response = postForJson(url, requestBody);
				ArrayNode usersNode = response != null && response.has("user")
						? (ArrayNode) response.get("user") : null;

				if (usersNode == null || usersNode.isEmpty()) {
					break;
				}

				for (JsonNode node : usersNode) {
					if (node instanceof ObjectNode) {
						allUsers.add((ObjectNode) node);
					}
				}

				if (usersNode.size() < USER_SEARCH_LIMIT) {
					break;
				}

				offset += USER_SEARCH_LIMIT;
				
				// Log fetch progress
				log.info("Fetched {} users so far (offset: {})", allUsers.size(), offset);
			} catch (Exception e) {
				recordFailure(String.format("Failed to fetch users at offset %d : %s", offset, e.getMessage()));
				break;
			}
		}

		log.info("Total users fetched with target roles: {}", allUsers.size());
		logToFile("Total users fetched with target roles: %d", allUsers.size());
		return allUsers;
	}

	private boolean hasMigratedRole(ObjectNode userNode) {
		ArrayNode roles = (ArrayNode) userNode.get("roles");
		if (roles == null || roles.isEmpty()) {
			return false;
		}

		for (JsonNode roleNode : roles) {
			String roleCode = textOrNull(roleNode.get("code"));
			if (roleCode != null) {
				for (String migratedRole : MIGRATED_ROLES) {
					if (migratedRole.equalsIgnoreCase(roleCode)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isVendorUser(ObjectNode userNode) {
		ArrayNode roles = (ArrayNode) userNode.get("roles");
		if (roles == null || roles.isEmpty()) {
			return false;
		}

		for (JsonNode roleNode : roles) {
			String roleCode = textOrNull(roleNode.get("code"));
			if (VENDOR_ROLE.equalsIgnoreCase(roleCode)) {
				return true;
			}
		}
		return false;
	}

	private boolean updateVendorPassword(ObjectNode userNode, String userUuid, String userName) {
		// Guard against null or blank userName
		if (StringUtils.isBlank(userName)) {
			recordFailure(String.format("Vendor user has null or blank userName (uuid: %s) - cannot lookup password mapping, skipping",
					userUuid));
			log.warn("Skipping vendor password update: userName is null or blank for user {}", userUuid);
			logToFile("Skipping vendor password update: userName is null or blank for user %s", userUuid);
			return false;
		}
		
		// Get vendor password from the mapping
		String vendorPassword = VENDOR_PASSWORDS.get(userName.toLowerCase());
		
		if (vendorPassword == null) {
			recordFailure(String.format("No password mapping found for vendor %s (%s) - skipping",
					userName, userUuid));
			return false;
		}

		// Update password using common method
		return updatePassword(userNode, userUuid, userName, vendorPassword, "vendor");
	}

	private boolean updateUserPassword(ObjectNode userNode, String userUuid, String userName, String password) {
		// Update password using common method
		return updatePassword(userNode, userUuid, userName, password, "user");
	}

	/**
	 * Common method to update user password via API
	 * @param userNode The user object node
	 * @param userUuid The user UUID
	 * @param userName The username
	 * @param password The password to set
	 * @param userType The type of user ("vendor" or "user") for logging purposes
	 * @return true if successful, false otherwise
	 */
	private boolean updatePassword(ObjectNode userNode, String userUuid, String userName, String password, String userType) {
		try {
			// Set the password
			userNode.put("password", password);

			// Create update payload
			ObjectNode payload = objectMapper.createObjectNode();
			payload.set("RequestInfo", buildRequestInfoBody());
			payload.set("user", userNode);

			log.info("Updating password for {} {} ({})", userType, userName, userUuid);
			logToFile("Updating password for %s %s (%s)", userType, userName, userUuid);

			// Call user update API
			updateUserWithRetry(payload, userUuid, userName);
			
			log.info("Successfully updated password for {} {} ({})", userType, userName, userUuid);
			logToFile("Successfully updated password for %s %s (%s)", userType, userName, userUuid);
			return true;

		} catch (Exception e) {
			recordFailure(String.format("Failed to update password for %s %s (%s) : %s",
					userType, userName, userUuid, e.getMessage()));
			return false;
		}
	}

	private void updateUserWithRetry(ObjectNode payload, String userUuid, String userName) throws Exception {
		int maxRetries = 3;
		int[] delaysInSeconds = {2, 5, 10};
		
		Exception lastException = null;
		
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				postForJsonWithoutRecording(userHost + userUpdateEndpoint, payload);
				if (attempt > 1) {
					log.info("Successfully updated user {} on attempt {}", userUuid, attempt);
					logToFile("Successfully updated user %s on attempt %d", userUuid, attempt);
				}
				return; // Success
			} catch (Exception e) {
				lastException = e;
				String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
				
				// Check for permanent errors that shouldn't be retried
				if (errorMessage.contains("user not found") ||
					errorMessage.contains("invalid user") ||
					errorMessage.contains("does not exist")) {
					log.warn("User {} not found, skipping", userUuid);
					logToFile("User %s not found, skipping", userUuid);
					throw e; // Don't retry, propagate the exception
				}
				
				// If this was the last attempt, throw the exception
				if (attempt >= maxRetries) {
					log.error("Failed to update user {} after {} attempts", userUuid, maxRetries);
					logToFile("Failed to update user %s after %d attempts", userUuid, maxRetries);
					throw e;
				}
				
				// Otherwise, log and retry with delay
				int delaySeconds = delaysInSeconds[attempt - 1];
				log.warn("Attempt {} failed for user {}: {}. Retrying in {} seconds...", 
						attempt, userUuid, e.getMessage(), delaySeconds);
				logToFile("Attempt %d failed for user %s: %s. Retrying in %d seconds...", 
						attempt, userUuid, e.getMessage(), delaySeconds);
				
				try {
					Thread.sleep(delaySeconds * 1000L);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Migration interrupted during retry delay", ie);
				}
			}
		}
		
		throw lastException;
	}

	private String textOrNull(JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return null;
		}
		String value = node.asText(null);
		return value == null ? null : value.trim().isEmpty() ? null : value.trim();
	}

	private JsonNode postForJson(String url, JsonNode body) {
		return postForJson(url, body, true);
	}

	private JsonNode postForJsonWithoutRecording(String url, JsonNode body) {
		return postForJson(url, body, false);
	}

	/**
	 * Common method to perform HTTP POST request
	 * @param url The URL to POST to
	 * @param body The request body
	 * @param recordFailure Whether to record failures in the failures list
	 * @return The JSON response node
	 */
	private JsonNode postForJson(String url, JsonNode body, boolean recordFailure) {
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
			if (recordFailure) {
				recordFailure(message);
			}
			throw new RuntimeException(message, e);
		}
	}

	private ObjectNode buildRequestInfoBody() {
		return objectMapper.valueToTree(requestInfo);
	}

	private RequestInfo buildRequestInfo() {
		RequestInfo info = new RequestInfo();
		info.setApiId("PASSWORD-UPDATE-MIGRATION");
		info.setVer("1.0");
		info.setTs(Instant.now().toEpochMilli());
		info.setAction("_update");
		info.setDid("password-migration-job");
		info.setKey("");
		info.setMsgId(UUID.randomUUID().toString());
		info.setAuthToken(getEnvOrDefault("EGOV_AUTH_TOKEN", ""));

		User user = new User();
		user.setUuid(getEnvOrDefault("EGOV_AUTH_USER_UUID", "00000000-0000-0000-0000-000000000001"));
		user.setUserName(getEnvOrDefault("EGOV_AUTH_USERNAME", "password-migration"));
		user.setName("Password Migration User");
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
		logToFile("USER PASSWORD UPDATE MIGRATION LOG");
		logToFile("Log File: %s", logFilePath);
		logToFile("Start Time: %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		logToFile("Default Password: [MASKED] (Default password will be set for migrated users)");
		logToFile("Target Roles: %s", String.join(", ", MIGRATED_ROLES));
		logToFile("Vendor Role: %s (with specific passwords - passwords masked in logs)", VENDOR_ROLE);
		logToFile("Total Vendors in mapping: %d", VENDOR_PASSWORDS.size());
		logToFile("User Search Endpoint: %s%s", userHost, userSearchEndpoint);
		logToFile("Batch Size: %d users", PASSWORD_UPDATE_BATCH_SIZE);
		logToFile("Delay Between Updates: %d ms", DELAY_BETWEEN_UPDATES_MS);
		logToFile("Note: Fetching only users with target roles (optimized query)");
		logToFile("Note: Processing with rate limiting to avoid API overload");
		logToFile("Security Note: Passwords are masked in logs for security");
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

