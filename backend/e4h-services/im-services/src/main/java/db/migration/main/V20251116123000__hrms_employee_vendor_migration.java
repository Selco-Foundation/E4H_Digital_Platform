package db.migration.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;

@Slf4j
public class V20251116123000__hrms_employee_vendor_migration extends BaseJavaMigration {

	private static final String TARGET_TENANT = "in";
	private static final String COMPLAINT_RESOLVER_ROLE = "COMPLAINT_RESOLVER";
	private static final int MDMS_LIMIT = 300;
	private static final int HRMS_PAGE_SIZE = 200;
	private static final String TENANT_MODULE_NAME = "tenant";
	private static final String TENANT_MASTER_NAME = "tenants";
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
	private final Map<String, String> facilityTenantBoundaryMap = new LinkedHashMap<>();
	private final List<String> failures = new ArrayList<>();
	private PrintWriter migrationLogger;
	private Path logFilePath;

	private int totalEmployeesEvaluated = 0;
	private int totalEmployeesMigrated = 0;

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
		String logFileName = "hrms_resolver_migration_" + LocalDateTime.now()
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

			loadFacilityTenantBoundaryMap(connection);

			List<ResolverUserInfo> resolverUsers = loadResolverUsers(connection, activeStateTenants);
			log.info("Identified {} COMPLAINT_RESOLVER users to evaluate", resolverUsers.size());
			logToFile("Identified %d COMPLAINT_RESOLVER users to evaluate", resolverUsers.size());

			for (ResolverUserInfo info : resolverUsers) {
				totalEmployeesEvaluated++;
				if (info.getRoleTenantIds().isEmpty()) {
					recordFailure(String.format("User %s has no tenant assignments for COMPLAINT_RESOLVER – skipping",
							info.getUserUuid()));
					continue;
				}

				ObjectNode employee = fetchEmployee(info);
				if (employee == null) {
					recordFailure(String.format("Unable to fetch employee profile for user %s (search tenants: %s)",
							info.getUserUuid(), buildCandidateTenantIds(info)));
					continue;
				}

				boolean changed = updateResolverEmployee(employee, info);
				if (!changed) {
					continue;
				}

				if (submitEmployeeUpdate(employee)) {
					totalEmployeesMigrated++;
				}
			}

			log.info("COMPLAINT_RESOLVER migration summary: evaluated={}, migrated={}, failures={}",
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

	private void loadFacilityTenantBoundaryMap(Connection connection) {
		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("SELECT tenant_id, boundary_code FROM facility_tenant_id_map")) {
			while (resultSet.next()) {
				String tenantId = resultSet.getString("tenant_id");
				String boundary = resultSet.getString("boundary_code");
				if (StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(boundary)) {
					facilityTenantBoundaryMap.put(tenantId.trim(), boundary.trim());
				}
			}
			log.info("Loaded {} facility boundary mappings", facilityTenantBoundaryMap.size());
		} catch (Exception e) {
			log.error("Failed to load facility_tenant_id_map data", e);
			recordFailure("Failed to load facility_tenant_id_map data: " + e.getMessage());
		}
	}

	private List<ResolverUserInfo> loadResolverUsers(Connection connection, Set<String> activeStates) {
		Map<String, ResolverUserInfo> users = new LinkedHashMap<>();
		LinkedHashSet<String> tenantsToQuery = new LinkedHashSet<>(activeStates);

		for (String tenant : tenantsToQuery) {
			List<ObjectNode> employees = searchResolverEmployees(tenant);
			for (ObjectNode employee : employees) {
				ObjectNode userNode = (ObjectNode) employee.get("user");
				if (userNode == null) {
					continue;
				}

				String userUuid = textOrNull(userNode.get("uuid"));
				if (StringUtils.isBlank(userUuid)) {
					continue;
				}

				Long userId = userNode.hasNonNull("id") ? userNode.get("id").asLong() : null;
				String userTenant = textOrNull(userNode.get("tenantId"));
				if (userTenant == null) {
					userTenant = textOrNull(userNode.get("tenantid"));
				}
				final String resolvedUserTenant = userTenant;

				ResolverUserInfo info = users.computeIfAbsent(userUuid,
						uuid -> new ResolverUserInfo(userId, uuid, resolvedUserTenant));
				info.setUserIdIfAbsent(userId);

				ArrayNode rolesNode = (ArrayNode) userNode.get("roles");
				if (rolesNode == null || rolesNode.isEmpty()) {
					continue;
				}

				for (JsonNode roleNode : rolesNode) {
					if (!(roleNode instanceof ObjectNode)) {
						continue;
					}
					String roleCode = textOrNull(roleNode.get("code"));
					if (!COMPLAINT_RESOLVER_ROLE.equalsIgnoreCase(roleCode)) {
						continue;
					}
					String roleTenant = textOrNull(roleNode.get("tenantId"));
					if (roleTenant == null) {
						roleTenant = textOrNull(roleNode.get("tenantid"));
					}
					if (StringUtils.isBlank(roleTenant)) {
						continue;
					}
					String stateTenant = extractStateTenant(roleTenant);
					if (stateTenant == null || !activeStates.contains(stateTenant)) {
						continue;
					}
					info.getRoleTenantIds().add(roleTenant);
				}
			}
		}

		List<ResolverUserInfo> filtered = new ArrayList<>();
		for (ResolverUserInfo info : users.values()) {
			info.getRoleTenantIds().removeIf(tenant -> {
				String state = extractStateTenant(tenant);
				return state == null || !activeStates.contains(state);
			});
			if (info.getRoleTenantIds().isEmpty()) {
				log.debug("Excluding user {} as no role tenants remain after filtering by active tenants", info.getUserUuid());
				continue;
			}
			if (info.getUserId() == null) {
				Long fetchedId = fetchUserId(connection, info.getUserUuid());
				info.setUserIdIfAbsent(fetchedId);
				if (info.getUserId() == null) {
					recordFailure(String.format("Could not resolve numeric userId for user %s", info.getUserUuid()));
					continue;
				}
			}
			filtered.add(info);
		}
		return filtered;
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

	private List<ObjectNode> searchResolverEmployees(String tenantId) {
		List<ObjectNode> employees = new ArrayList<>();
		int offset = 0;

		while (true) {
			try {
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsSearchEndpoint)
						.queryParam("tenantId", tenantId)
						.queryParam("roles", COMPLAINT_RESOLVER_ROLE)
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
				log.warn("Failed to fetch resolver employees for tenant {} : {}", tenantId, e.getMessage());
				logToFile("Failed to fetch resolver employees for tenant %s : %s", tenantId, e.getMessage());
				break;
			}
		}

		return employees;
	}

	private ObjectNode fetchEmployee(ResolverUserInfo info) {
		List<String> candidates = buildCandidateTenantIds(info);
		for (String tenant : candidates) {
			try {
				ObjectNode body = objectMapper.createObjectNode();
				body.set("RequestInfo", buildRequestInfoBody());

				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hrmsHost + hrmsSearchEndpoint)
						.queryParam("tenantId", tenant)
						.queryParam("uuids", info.getUserUuid())
						.queryParam("limit", 1)
						.queryParam("offset", 0);

				JsonNode response = postForJson(builder.toUriString(), body);
				ArrayNode employeesNode = response != null && response.has("Employees")
						? (ArrayNode) response.get("Employees") : null;

				if (employeesNode != null && !employeesNode.isEmpty()) {
					return (ObjectNode) employeesNode.get(0);
				}
			} catch (Exception e) {
				log.warn("Failed to fetch employee {} for tenant {} : {}", info.getUserUuid(), tenant, e.getMessage());
				logToFile("Failed to fetch employee %s for tenant %s : %s",
						info.getUserUuid(), tenant, e.getMessage());
			}
		}
		return null;
	}

	private Long fetchUserId(Connection connection, String userUuid) {
		if (StringUtils.isBlank(userUuid)) {
			return null;
		}
		String sql = "SELECT id FROM eg_user WHERE uuid = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, userUuid);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong("id");
				}
			}
		} catch (Exception e) {
			recordFailure(String.format("Failed to resolve user id for uuid %s : %s", userUuid, e.getMessage()));
		}
		return null;
	}

	private boolean updateResolverEmployee(ObjectNode employee, ResolverUserInfo info) {
		ArrayNode jurisdictions = (ArrayNode) employee.get("jurisdictions");
		ObjectNode template = deriveJurisdictionTemplate(jurisdictions);

		List<JurisdictionTarget> targets = buildJurisdictionTargets(info);
		if (targets.isEmpty()) {
			recordFailure(String.format("Could not resolve jurisdiction boundaries for user %s", info.getUserUuid()));
			return false;
		}
		logToFile("Updating jurisdictions for user %s with targets %s", info.getUserUuid(), targets);

		Map<String, ObjectNode> byTenant = mapJurisdictionsByTenant(jurisdictions);
		Map<String, ObjectNode> byBoundary = mapJurisdictionsByBoundary(jurisdictions);

		ArrayNode updatedJurisdictions = objectMapper.createArrayNode();
		Set<String> boundariesAdded = new LinkedHashSet<>();
		boolean mutated = false;

		for (JurisdictionTarget target : targets) {
			if (!boundariesAdded.add(target.getBoundary())) {
				continue;
			}

			ObjectNode jurisdictionNode = findJurisdictionNode(byTenant, byBoundary, target);
				if (jurisdictionNode == null) {
					jurisdictionNode = createJurisdictionNode(template);
					if (jurisdictionNode == null) {
						recordFailure(String.format(
								"Unable to construct jurisdiction entry for user %s (tenant %s boundary %s)",
								info.getUserUuid(), target.getRoleTenantId(), target.getBoundary()));
						continue;
					}
					mutated = true;
				}

			if (setTenantId(jurisdictionNode)) {
				mutated = true;
			}
			String currentBoundary = textOrNull(jurisdictionNode.get("boundary"));
			if (!Objects.equals(currentBoundary, target.getBoundary())) {
				jurisdictionNode.put("boundary", target.getBoundary());
				mutated = true;
			}

			if (jurisdictionNode.has("isActive") && !jurisdictionNode.get("isActive").asBoolean()) {
				jurisdictionNode.put("isActive", true);
				mutated = true;
			}

			updatedJurisdictions.add(jurisdictionNode);
		}

		if (updatedJurisdictions.isEmpty()) {
			recordFailure(String.format("No jurisdictions mapped for user %s – skipping update", info.getUserUuid()));
			return false;
		}

		for (ObjectNode leftover : byTenant.values()) {
			if (leftover == null) continue;
			if (setTenantId(leftover)) {
				mutated = true;
			}
			String boundary = textOrNull(leftover.get("boundary"));
			if (boundary == null || boundariesAdded.contains(boundary)) {
				continue;
			}
			boundariesAdded.add(boundary);
			updatedJurisdictions.add(leftover);
		}
		for (ObjectNode leftover : byBoundary.values()) {
			if (leftover == null) continue;
			String boundary = textOrNull(leftover.get("boundary"));
			if (boundary == null || boundariesAdded.contains(boundary)) {
				continue;
			}
			if (setTenantId(leftover)) {
				mutated = true;
			}
			boundariesAdded.add(boundary);
			updatedJurisdictions.add(leftover);
		}

		employee.set("jurisdictions", updatedJurisdictions);

		boolean tenantMutations = updateTenantFields(employee);
		return mutated || tenantMutations;
	}

	private List<JurisdictionTarget> buildJurisdictionTargets(ResolverUserInfo info) {
		LinkedHashMap<String, String> orderedTargets = new LinkedHashMap<>();
		for (String tenantId : info.getRoleTenantIds()) {
			String normalized = tenantId.trim();
			String boundary = resolveBoundary(normalized);
			if (StringUtils.isBlank(boundary)) {
				recordFailure(String.format("No boundary mapping found for tenant %s (user %s)", normalized, info.getUserUuid()));
				continue;
			}
			orderedTargets.putIfAbsent(normalized, boundary);
		}
		List<JurisdictionTarget> targets = new ArrayList<>(orderedTargets.size());
		for (Map.Entry<String, String> entry : orderedTargets.entrySet()) {
			targets.add(new JurisdictionTarget(entry.getKey(), entry.getValue()));
		}
		return targets;
	}

	private Map<String, ObjectNode> mapJurisdictionsByTenant(ArrayNode jurisdictions) {
		Map<String, ObjectNode> map = new LinkedHashMap<>();
		if (jurisdictions == null) {
			return map;
		}
		for (JsonNode node : jurisdictions) {
			if (!(node instanceof ObjectNode)) {
				continue;
			}
			ObjectNode obj = (ObjectNode) node;
			String tenantId = textOrNull(obj.get("tenantId"));
			if (tenantId != null) {
				map.putIfAbsent(tenantId, obj);
			}
			String legacyTenant = textOrNull(obj.get("tenantid"));
			if (legacyTenant != null) {
				map.putIfAbsent(legacyTenant, obj);
			}
		}
		return map;
	}

	private Map<String, ObjectNode> mapJurisdictionsByBoundary(ArrayNode jurisdictions) {
		Map<String, ObjectNode> map = new LinkedHashMap<>();
		if (jurisdictions == null) {
			return map;
		}
		for (JsonNode node : jurisdictions) {
			if (node instanceof ObjectNode) {
				ObjectNode obj = (ObjectNode) node;
				String boundary = textOrNull(obj.get("boundary"));
				if (boundary != null) {
					map.putIfAbsent(boundary, obj);
				}
			}
		}
		return map;
	}

	private ObjectNode findJurisdictionNode(Map<String, ObjectNode> byTenant, Map<String, ObjectNode> byBoundary,
											JurisdictionTarget target) {
		ObjectNode node = byTenant.remove(target.getRoleTenantId());
		if (node != null) {
			return node;
		}

		String stateTenant = extractStateTenant(target.getRoleTenantId());
		if (stateTenant != null) {
			node = byTenant.remove(stateTenant);
			if (node != null) {
				return node;
			}
		}

		node = byBoundary.remove(target.getBoundary());
		if (node != null) {
			return node;
		}

		return null;
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

	private ObjectNode deriveJurisdictionTemplate(ArrayNode jurisdictions) {
		if (jurisdictions == null || jurisdictions.isEmpty()) {
			return null;
		}
		JsonNode first = jurisdictions.get(0);
		if (!(first instanceof ObjectNode)) {
			return null;
		}
		return ((ObjectNode) first).deepCopy();
	}

	private ObjectNode createJurisdictionNode(ObjectNode template) {
		ObjectNode node;
		if (template != null) {
			node = template.deepCopy();
			resetJurisdictionNode(node);
		} else {
			node = objectMapper.createObjectNode();
			node.put("hierarchy", "ADMIN");
			node.put("boundaryType", "State");
			node.put("isActive", true);
		}
		node.put("tenantId", TARGET_TENANT);
		node.remove("tenantid");
		return node;
	}

	private void resetJurisdictionNode(ObjectNode node) {
		if (node == null) {
			return;
		}
		if (node.has("id")) {
			node.putNull("id");
		}
		if (node.has("uuid")) {
			node.putNull("uuid");
		}
		if (node.has("auditDetails")) {
			node.remove("auditDetails");
		}
		if (node.has("isActive") && !node.get("isActive").asBoolean()) {
			node.put("isActive", true);
		} else {
			node.put("isActive", true);
		}
	}

	private boolean submitEmployeeUpdate(ObjectNode employee) {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.set("RequestInfo", buildRequestInfoBody());
		ArrayNode array = objectMapper.createArrayNode();
		array.add(employee);
		payload.set("Employees", array);

		try {
			updateEmployeeWithRetry(payload, employee.path("uuid").asText());
			log.info("Updated employee {}", employee.path("uuid").asText());
			logToFile("Updated employee %s", employee.path("uuid").asText());
			return true;
		} catch (Exception e) {
			recordFailure(String.format("Failed to update employee %s : %s",
					employee.path("uuid").asText(), e.getMessage()));
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
				// These are permanent errors that won't be resolved by retrying
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
				
				// For other errors (like "user creation failed at user service"), retry
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

	private String resolveBoundary(String tenantId) {
		if (StringUtils.isBlank(tenantId)) {
			return null;
		}

		if (tenantId.contains(".")) {
			return facilityTenantBoundaryMap.get(tenantId);
		}

		String state = TENANT_TO_STATE.get(tenantId);
		if (state != null) {
			return "India_" + state.replace(' ', '_');
		}

		return "India_" + tenantId.toUpperCase();
	}

	private String extractStateTenant(String tenantId) {
		if (StringUtils.isBlank(tenantId)) {
			return null;
		}
		tenantId = tenantId.trim();
		if (tenantId.contains(".")) {
			return tenantId.substring(0, tenantId.indexOf('.'));
		}
		return tenantId;
	}

	private List<String> buildCandidateTenantIds(ResolverUserInfo info) {
		LinkedHashSet<String> candidates = new LinkedHashSet<>();
		if (StringUtils.isNotBlank(info.getUserTenantId())) {
			String userTenant = info.getUserTenantId().trim();
			candidates.add(userTenant);
			String stateTenant = extractStateTenant(userTenant);
			if (StringUtils.isNotBlank(stateTenant)) {
				candidates.add(stateTenant);
			}
		}
		for (String roleTenant : info.getRoleTenantIds()) {
			if (StringUtils.isBlank(roleTenant)) {
				continue;
			}
			String normalized = roleTenant.trim();
			candidates.add(normalized);
			String stateTenant = extractStateTenant(normalized);
			if (StringUtils.isNotBlank(stateTenant)) {
				candidates.add(stateTenant);
			}
		}
		candidates.add(TARGET_TENANT);
		return new ArrayList<>(candidates);
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
		logToFile("HRMS RESOLVER MIGRATION LOG");
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

	private RequestInfo buildRequestInfo() {
		RequestInfo info = new RequestInfo();
		info.setApiId("HRMS-RESOLVER-MIGRATION");
		info.setVer("1.0");
		info.setTs(Instant.now().toEpochMilli());
		info.setAction("_update");
		info.setDid("resolver-migration-job");
		info.setKey("");
		info.setMsgId(UUID.randomUUID().toString());
		info.setAuthToken(getEnvOrDefault("EGOV_AUTH_TOKEN", ""));

		User user = new User();
		user.setUuid(getEnvOrDefault("EGOV_AUTH_USER_UUID", "00000000-0000-0000-0000-000000000001"));
		user.setUserName(getEnvOrDefault("EGOV_AUTH_USERNAME", "resolver-migration"));
		user.setName("Resolver Migration User");
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

	@RequiredArgsConstructor
	private static class JurisdictionTarget {
		@Getter
		private final String roleTenantId;
		@Getter
		private final String boundary;
	}

	private static class ResolverUserInfo {
		@Getter
		private Long userId;
		@Getter
		private final String userUuid;
		@Getter
		private final String userTenantId;
		private final LinkedHashSet<String> roleTenantIds = new LinkedHashSet<>();

		private ResolverUserInfo(Long userId, String userUuid, String userTenantId) {
			this.userId = userId;
			this.userUuid = userUuid;
			this.userTenantId = userTenantId;
		}

		public Set<String> getRoleTenantIds() {
			return roleTenantIds;
		}

		public void setUserIdIfAbsent(Long userId) {
			if (this.userId == null && userId != null) {
				this.userId = userId;
			}
		}
	}
}

