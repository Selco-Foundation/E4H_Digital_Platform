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
public class V20251120123000__update_migrated_user_passwords extends BaseJavaMigration {

	private static final String TARGET_TENANT = "in";
	private static final String DEFAULT_PASSWORD = "Health@2026";
	private static final String VENDOR_FALLBACK_PASSWORD = "Energy@123";
	private static final String VENDOR_ROLE = "COMPLAINT_RESOLVER";
	private static final int USER_SEARCH_LIMIT = 1000; // Fetch 1000 users per batch for faster processing
	private static final int PASSWORD_UPDATE_BATCH_SIZE = 100; // Process 100 users, then log progress
	private static final long DELAY_BETWEEN_UPDATES_MS = 50; // 50ms delay between password updates (reduced for speed)
	
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

	private String hrmsHost;
	private String hrmsSearchEndpoint;
	private String hrmsUpdateEndpoint;

	private RequestInfo requestInfo;
	private PrintWriter migrationLogger;
	private Path logFilePath;

		private int totalUsersEvaluated = 0;
	private int totalUsersUpdated = 0;
	private int totalVendorsUpdated = 0;
	private int totalExistingUsersSkipped = 0;
	private final List<String> failures = new ArrayList<>();
	private final Set<String> processedUserUuids = new HashSet<>(); // Track processed UUIDs to avoid duplicates across roles

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

			// Process vendors first (COMPLAINT_RESOLVER role)
			log.info("Processing vendors with role: {}", VENDOR_ROLE);
			logToFile("Processing vendors with role: %s", VENDOR_ROLE);
			processUsersByRole(VENDOR_ROLE, true);
			
			// Process migrated roles one by one
			for (String role : MIGRATED_ROLES) {
				log.info("Processing users with role: {}", role);
				logToFile("Processing users with role: %s", role);
				processUsersByRole(role, false);
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
		hrmsHost = getEnvOrDefault("EGOV_HRMS_HOST", "http://localhost:8090");
		hrmsSearchEndpoint = getEnvOrDefault("EGOV_HRMS_SEARCH_ENDPOINT", "/egov-hrms/employees/_search");
		hrmsUpdateEndpoint = getEnvOrDefault("EGOV_HRMS_UPDATE_ENDPOINT", "/egov-hrms/employees/_update");
	}

	/**
	 * Helper class to hold batch processing results
	 */
	private static class ProcessBatchResult {
		int newUsers;
		int duplicateUsers;
		int updatedUsers;
		
		ProcessBatchResult(int newUsers, int duplicateUsers, int updatedUsers) {
			this.newUsers = newUsers;
			this.duplicateUsers = duplicateUsers;
			this.updatedUsers = updatedUsers;
		}
	}

	/**
	 * Fetches employees for a specific role at the given offset using HRMS API
	 * @param roleCode The role code to search for
	 * @param offset The pagination offset
	 * @return ArrayNode containing employees, or null if error
	 */
	private ArrayNode fetchEmployeesByRole(String roleCode, int offset) {
		try {
			// Build HRMS search URL with query parameters
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsSearchEndpoint)
					.queryParam("tenantId", TARGET_TENANT)
					.queryParam("roles", roleCode)
					.queryParam("isActive", true)
					.queryParam("limit", USER_SEARCH_LIMIT)
					.queryParam("offset", offset);

			// Build request body with RequestInfo
			ObjectNode requestBody = objectMapper.createObjectNode();
			requestBody.set("RequestInfo", buildRequestInfoBody());

			// Make POST request to HRMS search endpoint
			String url = builder.toUriString();
			JsonNode response = postForJson(url, requestBody);
			
			// HRMS returns Employees array (not user array)
			return response != null && response.has("Employees") ? (ArrayNode) response.get("Employees") : null;
		} catch (Exception e) {
			recordFailure(String.format("Failed to fetch employees for role %s at offset %d : %s", 
					roleCode, offset, e.getMessage()));
			log.error("Error fetching employees for role {} at offset {}: {}", roleCode, offset, e.getMessage());
			return null;
		}
	}

	/**
	 * Processes a batch of employees and updates their user passwords
	 * Employees have a nested user object that contains the user details
	 * @param employeesNode The array of employee nodes to process
	 * @param roleCode The role code being processed (for logging)
	 * @param isVendorRole Whether this is a vendor role
	 * @return ProcessBatchResult containing processing statistics
	 */
	private ProcessBatchResult processEmployeeBatch(ArrayNode employeesNode, String roleCode, boolean isVendorRole) {
		int newUsers = 0;
		int duplicateUsers = 0;
		int updatedUsers = 0;
		
		// Process employees as they're fetched (streaming approach)
		for (JsonNode node : employeesNode) {
			if (!(node instanceof ObjectNode)) {
				continue;
			}
			
			ObjectNode employeeNode = (ObjectNode) node;
			
			// Get user object from employee (employees have nested user object)
			JsonNode userNodeObj = employeeNode.get("user");
			if (userNodeObj == null || !(userNodeObj instanceof ObjectNode)) {
				recordFailure(String.format("Employee %s has no user object - skipping", 
						textOrNull(employeeNode.get("uuid"))));
				continue;
			}
			
			ObjectNode userNode = (ObjectNode) userNodeObj;
			String userUuid = textOrNull(userNode.get("uuid"));
			
			if (StringUtils.isBlank(userUuid)) {
				recordFailure(String.format("Employee user with role %s has no UUID - skipping", roleCode));
				continue;
			}
			
			// Skip if already processed (user might have multiple roles)
			if (processedUserUuids.contains(userUuid)) {
				duplicateUsers++;
				totalExistingUsersSkipped++;
				continue;
			}
			
			// Mark as processed
			processedUserUuids.add(userUuid);
			newUsers++;
			totalUsersEvaluated++;
			
			String userName = textOrNull(userNode.get("userName"));
			
			// Process the employee's user password based on role type
			boolean success = processEmployeePassword(employeeNode, userNode, userUuid, userName, isVendorRole);
			
			if (success) {
				updatedUsers++;
				if (isVendorRole) {
					totalVendorsUpdated++;
				} else {
					totalUsersUpdated++;
				}
			}
			
			// Small delay to avoid overwhelming the API
			try {
				Thread.sleep(DELAY_BETWEEN_UPDATES_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				recordFailure("Migration interrupted during employee processing delay");
				break;
			}
			
			// Log progress every batch
			if (totalUsersEvaluated % PASSWORD_UPDATE_BATCH_SIZE == 0) {
				log.info("Progress: Evaluated {} users, Updated {} regular users, Updated {} vendors",
						totalUsersEvaluated, totalUsersUpdated, totalVendorsUpdated);
				logToFile("Progress: Evaluated %d users, Updated %d regular users, Updated %d vendors",
						totalUsersEvaluated, totalUsersUpdated, totalVendorsUpdated);
			}
		}
		
		return new ProcessBatchResult(newUsers, duplicateUsers, updatedUsers);
	}

	/**
	 * Processes employee password update via HRMS API (consolidates vendor and regular user logic)
	 * @param employeeNode The employee node (has nested user object)
	 * @param userNode The user node from employee
	 * @param userUuid The user UUID
	 * @param userName The username
	 * @param isVendorRole Whether this is a vendor role
	 * @return true if successful, false otherwise
	 */
	private boolean processEmployeePassword(ObjectNode employeeNode, ObjectNode userNode, String userUuid, String userName, boolean isVendorRole) {
		if (isVendorRole) {
			return updateVendorPasswordViaHRMS(employeeNode, userNode, userUuid, userName);
		} else {
			return updateEmployeePasswordViaHRMS(employeeNode, userNode, userUuid, userName, DEFAULT_PASSWORD);
		}
	}

	/**
	 * Processes users role-by-role: fetches and updates passwords as users are found
	 * @param roleCode The role code to search for
	 * @param isVendorRole Whether this is a vendor role (COMPLAINT_RESOLVER)
	 */
	private void processUsersByRole(String roleCode, boolean isVendorRole) {
		int offset = 0;
		int consecutiveEmptyPages = 0;
		int maxConsecutiveEmptyPages = 3; // Safety limit to prevent infinite loops
		int roleUsersProcessed = 0;
		int roleUsersUpdated = 0;
		
		log.info("Starting to process users with role: {} (vendor: {})", roleCode, isVendorRole);
		logToFile("Starting to process users with role: %s (vendor: %s)", roleCode, isVendorRole);

		while (true) {
			try {
				// Build and fetch employees for this role at current offset using HRMS API
				ArrayNode employeesNode = fetchEmployeesByRole(roleCode, offset);
				
				if (employeesNode == null || employeesNode.isEmpty()) {
					consecutiveEmptyPages++;
					if (consecutiveEmptyPages >= maxConsecutiveEmptyPages) {
						log.info("Received {} consecutive empty pages for role {}, stopping fetch", consecutiveEmptyPages, roleCode);
						logToFile("Received %d consecutive empty pages for role %s, stopping fetch", consecutiveEmptyPages, roleCode);
						break;
					}
					offset += USER_SEARCH_LIMIT;
					continue;
				}

				// Reset empty pages counter when we get results
				consecutiveEmptyPages = 0;

				// Process batch of employees (employees have nested user objects)
				ProcessBatchResult result = processEmployeeBatch(employeesNode, roleCode, isVendorRole);
				
				// Update counters
				roleUsersProcessed += result.newUsers;
				roleUsersUpdated += result.updatedUsers;
				
				// Log progress for this role
				if (result.newUsers > 0 || result.duplicateUsers > 0) {
					log.info("Role {}: Processed {} users so far (offset: {}, new: {}, updated: {}, duplicates: {})", 
							roleCode, roleUsersProcessed, offset, result.newUsers, result.updatedUsers, result.duplicateUsers);
					logToFile("Role %s: Processed %d users so far (offset: %d, new: %d, updated: %d, duplicates: %d)", 
							roleCode, roleUsersProcessed, offset, result.newUsers, result.updatedUsers, result.duplicateUsers);
				}

				// Break if we got fewer results than requested (last page)
				if (employeesNode.size() < USER_SEARCH_LIMIT) {
					log.info("Received {} employees (less than page size {}) for role {}, stopping fetch", 
							employeesNode.size(), USER_SEARCH_LIMIT, roleCode);
					break;
				}

				offset += USER_SEARCH_LIMIT;
				
			} catch (Exception e) {
				recordFailure(String.format("Failed to fetch employees for role %s at offset %d : %s", 
						roleCode, offset, e.getMessage()));
				log.error("Error processing employees for role {} at offset {}: {}", roleCode, offset, e.getMessage());
				break;
			}
		}

		log.info("Completed processing role {}: Processed {} users, Updated {} users", 
				roleCode, roleUsersProcessed, roleUsersUpdated);
		logToFile("Completed processing role %s: Processed %d users, Updated %d users", 
				roleCode, roleUsersProcessed, roleUsersUpdated);
	}


	private boolean updateVendorPasswordViaHRMS(ObjectNode employeeNode, ObjectNode userNode, String userUuid, String userName) {
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
			// Fallback: If vendor not found in mapping, use vendor fallback password
			log.warn("No password mapping found for vendor {} ({}), using fallback password", userName, userUuid);
			logToFile("No password mapping found for vendor %s (%s), using fallback password Energy@123", userName, userUuid);
			vendorPassword = VENDOR_FALLBACK_PASSWORD;
		}

		// Update password via HRMS API
		return updateEmployeePasswordViaHRMS(employeeNode, userNode, userUuid, userName, vendorPassword);
	}

	private boolean updateEmployeePasswordViaHRMS(ObjectNode employeeNode, ObjectNode userNode, String userUuid, String userName, String password) {
		// Update password via HRMS API using common method
		return updateEmployeePassword(employeeNode, userNode, userUuid, userName, password, "user");
	}

	/**
	 * Common method to update employee password via HRMS API
	 * @param employeeNode The employee node (has nested user object)
	 * @param userNode The user node from employee
	 * @param userUuid The user UUID
	 * @param userName The username
	 * @param password The password to set
	 * @param userType The type of user ("vendor" or "user") for logging purposes
	 * @return true if successful, false otherwise
	 */
	private boolean updateEmployeePassword(ObjectNode employeeNode, ObjectNode userNode, String userUuid, String userName, String password, String userType) {
		try {
			// Update password in the nested user object
			userNode.put("password", password);

			// Create HRMS update payload with employee object
			// HRMS expects Employees array with RequestInfo
			ObjectNode payload = objectMapper.createObjectNode();
			payload.set("RequestInfo", buildRequestInfoBody());
			
			// Wrap employee in Employees array
			ArrayNode employeesArray = objectMapper.createArrayNode();
			employeesArray.add(employeeNode); // Employee already has updated user.password
			payload.set("Employees", employeesArray);

			log.info("Updating password via HRMS for {} {} ({})", userType, userName, userUuid);
			logToFile("Updating password via HRMS for %s %s (%s)", userType, userName, userUuid);

			// Call HRMS update API (uses create endpoint for updates)
			updateEmployeeWithRetry(payload, userUuid, userName);
			
			log.info("Successfully updated password via HRMS for {} {} ({})", userType, userName, userUuid);
			logToFile("Successfully updated password via HRMS for %s %s (%s)", userType, userName, userUuid);
			return true;

		} catch (Exception e) {
			recordFailure(String.format("Failed to update password via HRMS for %s %s (%s) : %s",
					userType, userName, userUuid, e.getMessage()));
			return false;
		}
	}


	private void updateEmployeeWithRetry(ObjectNode payload, String userUuid, String userName) throws Exception {
		int maxRetries = 3;
		int[] delaysInSeconds = {2, 5, 10};
		
		Exception lastException = null;
		
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				// HRMS uses create endpoint for updates
				postForJsonWithoutRecording(hrmsHost + hrmsUpdateEndpoint, payload);
				if (attempt > 1) {
					log.info("Successfully updated employee user {} on attempt {}", userUuid, attempt);
					logToFile("Successfully updated employee user %s on attempt %d", userUuid, attempt);
				}
				return; // Success
			} catch (Exception e) {
				lastException = e;
				String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
				
				// Check for permanent errors that shouldn't be retried
				if (errorMessage.contains("employee not found") ||
					errorMessage.contains("user not found") ||
					errorMessage.contains("invalid") ||
					errorMessage.contains("does not exist")) {
					log.warn("Employee user {} not found, skipping", userUuid);
					logToFile("Employee user %s not found, skipping", userUuid);
					throw e; // Don't retry, propagate the exception
				}
				
				// If this was the last attempt, throw the exception
				if (attempt >= maxRetries) {
					log.error("Failed to update employee user {} after {} attempts", userUuid, maxRetries);
					logToFile("Failed to update employee user %s after %d attempts", userUuid, maxRetries);
					throw e;
				}
				
				// Otherwise, log and retry with delay
				int delaySeconds = delaysInSeconds[attempt - 1];
				log.warn("Attempt {} failed for employee user {}: {}. Retrying in {} seconds...", 
						attempt, userUuid, e.getMessage(), delaySeconds);
				logToFile("Attempt %d failed for employee user %s: %s. Retrying in %d seconds...", 
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
		logToFile("HRMS Search Endpoint: %s%s", hrmsHost, hrmsSearchEndpoint);
		logToFile("HRMS Update Endpoint: %s%s", hrmsHost, hrmsUpdateEndpoint);
		logToFile("Batch Size: %d users", PASSWORD_UPDATE_BATCH_SIZE);
		logToFile("Delay Between Updates: %d ms", DELAY_BETWEEN_UPDATES_MS);
			logToFile("Note: Using HRMS APIs for employee search and password updates");
			logToFile("Note: Fetching employees by role with optimized query");
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

