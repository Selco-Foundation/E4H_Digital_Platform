package org.egov.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.contract.user.enums.UserType;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.RoleRequest;
import org.egov.web.models.User;
import org.egov.web.models.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class UserUtil {

	private ObjectMapper mapper;

	private ServiceRequestRepository serviceRequestRepository;

	private final Configuration configs;
	private static final String LAST_MODIFIED_DATE = "lastModifiedDate";
	private static final String PWD_EXPIRY_DATE = "pwdExpiryDate";
	@Autowired
	public UserUtil(ObjectMapper mapper, ServiceRequestRepository serviceRequestRepository, Configuration configs) {
		this.mapper = mapper;
		this.serviceRequestRepository = serviceRequestRepository;
		this.configs = configs;
	}

	/**
	 * Returns UserDetailResponse by calling user service with given uri and object
	 * 
	 * @param userRequest Request object for user service
	 * @param uri         The address of the endpoint
	 * @return Response from user service as parsed as userDetailResponse
	 */

	public UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
		log.trace("UserUtil::userCall entry");
		String dobFormat = null;
		if (uri.toString().contains(configs.getUserSearchEndpoint())
				|| uri.toString().contains(configs.getUserUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(configs.getUserCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		log.debug("Calling user service with URI: {}, date format: {}", uri.toString(), dobFormat);
		try {
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
            UserDetailResponse response = mapper.convertValue(responseMap, UserDetailResponse.class);
            log.debug("User service call completed successfully");
            return response;
		} catch (IllegalArgumentException e) {
			log.error("Error converting user service response", e);
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		}
	}

	public UserDetailResponse updateUserPassword(Object userRequest, StringBuilder uri) {
		String dobFormat = "yyyy-MM-dd";
		try {
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
			return mapper.convertValue(responseMap, UserDetailResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		}
	}

	/** PII fields egov-user may return masked to a caller without plain access. */
	private static final List<String> MASKABLE_FIELDS = List.of(
			"name", "mobileNumber", "emailId", "altContactNumber", "alternatemobilenumber", "pan", "aadhaarNumber",
			"fatherOrHusbandName", "permanentAddress", "correspondenceAddress");
	private static final String UPDATE_DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
	private static final String SEARCH_DOB_FORMAT = "yyyy-MM-dd";
	private static final String UPDATE_DOB_FORMAT = "dd/MM/yyyy";

	/**
	 * Sets a user's password and password expiry, changing nothing else.
	 *
	 * `_updatenovalidate` replaces the user: every field it is not given is written as blank. So
	 * the user is read from egov-user as stored, only the two fields are overridden, and the whole
	 * record is sent back - never a partial object built from another service's view of the user.
	 *
	 * Refuses rather than writes when the stored record comes back masked: sending a masked value
	 * back would replace the real one.
	 */
	public void setPasswordPreservingUser(Object requestInfo, String userUuid, String password, Date pwdExpiryDate) {
		Map<String, Object> stored = fetchStoredUser(requestInfo, userUuid);
		for (String field : MASKABLE_FIELDS) {
			Object value = stored.get(field);
			if (value instanceof String text && text.contains("*")) {
				log.error("egov-user returned field {} of user {} masked; not writing it back", field, userUuid);
				throw new CustomException("USER_RECORD_MASKED",
						"The user's record could not be read in full, so their password was not changed");
			}
		}
		// The search and update contracts format dob differently.
		Object dob = stored.get("dob");
		if (dob instanceof String text && !text.isBlank()) {
			stored.put("dob", reformat(text, SEARCH_DOB_FORMAT, UPDATE_DOB_FORMAT));
		}
		stored.put("password", password);
		SimpleDateFormat expiryFormat = new SimpleDateFormat(UPDATE_DATE_TIME_FORMAT);
		expiryFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		stored.put("pwdExpiryDate", expiryFormat.format(pwdExpiryDate));

		Map<String, Object> request = new HashMap<>();
		request.put("RequestInfo", requestInfo);
		request.put("user", stored);
		updateUserPassword(request, new StringBuilder(configs.getUserHost() + configs.getUserUpdateEndpoint()));
	}

	/**
	 * The user exactly as egov-user returns it. Searched as active first, then inactive: egov-user
	 * defaults an unspecified `active` to true, which would miss a deactivated user.
	 */
	@SuppressWarnings("unchecked")
	Map<String, Object> fetchStoredUser(Object requestInfo, String userUuid) {
		for (boolean active : new boolean[]{true, false}) {
			Map<String, Object> search = new HashMap<>();
			search.put("RequestInfo", requestInfo);
			search.put("tenantId", "in");
			search.put("uuid", List.of(userUuid));
			search.put("active", active);
			Object response = serviceRequestRepository.fetchResult(
					new StringBuilder(configs.getUserHost() + configs.getUserSearchEndpoint()), search);
			if (response instanceof Map<?, ?> map && map.get("user") instanceof List<?> users && !users.isEmpty()) {
				return new LinkedHashMap<>((Map<String, Object>) users.get(0));
			}
		}
		throw new CustomException("USER_NOT_FOUND", "User " + userUuid + " was not found in egov-user");
	}

	private static String reformat(String value, String from, String to) {
		try {
			return new SimpleDateFormat(to).format(new SimpleDateFormat(from).parse(value));
		} catch (ParseException e) {
			// Already in the update format, or something egov-user will reject on its own.
			return value;
		}
	}

	/**
	 * Parses date formats to long for all users in responseMap
	 * 
	 * @param responseMap LinkedHashMap got from user api response
	 */

	public void parseResponse(LinkedHashMap responseMap, String dobFormat) {
		log.trace("UserUtil::parseResponse entry");
		List<LinkedHashMap> users = (List<LinkedHashMap>) responseMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (users != null) {
			log.debug("Parsing dates for {} users", users.size());
			users.forEach(map -> {
				map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get(LAST_MODIFIED_DATE) != null)
					map.put(LAST_MODIFIED_DATE, dateTolong((String) map.get(LAST_MODIFIED_DATE), format1));
				if ((String) map.get("dob") != null)
					map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get(PWD_EXPIRY_DATE) != null)
					map.put(PWD_EXPIRY_DATE, dateTolong((String) map.get(PWD_EXPIRY_DATE), format1));
			});
		}
	}

	/**
	 * Converts date to long
	 * 
	 * @param date   date to be parsed
	 * @param format Format of the date
	 * @return Long value of date
	 */
	private Long dateTolong(String date, String format) {
		log.trace("UserUtil::dateTolong entry");
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
			log.error("Failed to parse date: {} with format: {}", date, format, e);
			throw new CustomException("INVALID_DATE_FORMAT", "Failed to parse date format in user");
		}
		return d.getTime();
	}

	/**
	 * enriches the userInfo with statelevel tenantId and other fields The function
	 * creates user with username as mobile number.
	 * 
	 * @param mobileNumber
	 * @param tenantId
	 * @param userInfo
	 */
//	public void addUserDefaultFields(String mobileNumber, String tenantId, User userInfo, UserType userType) {
//		Role role = getCitizenRole(tenantId);
//		userInfo.setRoles(Collections.singletonList(role));
//		userInfo.setType(userType.toString());
//		userInfo.setUserName(mobileNumber);
//		userInfo.setTenantId(getStateLevelTenant(tenantId));
//	}

	/**
	 * Returns role object for citizen
	 * 
	 * @param tenantId
	 * @return
	 */
//	private Role getCitizenRole(String tenantId) {
//		Role role = Role.builder().build();
//		role.setCode("CITIZEN");
//		role.setName("Citizen");
//		role.setTenantId(getStateLevelTenant(tenantId));
//		return role;
//	}

	public String getStateLevelTenant(String tenantId) {
		log.trace("UserUtil::getStateLevelTenant entry");
		String stateLevelTenant = tenantId.split("\\.")[0];
		log.debug("Extracted state level tenant: {} from tenant: {}", stateLevelTenant, tenantId);
		return stateLevelTenant;
	}

	public UserRequest mapToUserRequest(User user) {

		if (user == null) return null;

		return UserRequest.builder()
				.id(user.getId())
				.uuid(user.getUuid())
				.userName(user.getUserName())
				.salutation(user.getSalutation())
				.name(user.getName())
				.gender(user.getGender())
				.mobileNumber(user.getMobileNumber())
				.emailId(user.getEmailId())
				.altContactNumber(user.getAltContactNumber())
				.pan(user.getPan())
				.aadhaarNumber(user.getAadhaarNumber())
				.permanentAddress(user.getPermanentAddress())
				.permanentCity(user.getPermanentCity())
				.permanentPinCode(user.getPermanentPincode())
				.correspondenceAddress(user.getCorrespondenceAddress())
				.correspondenceCity(user.getCorrespondenceCity())
				.correspondencePinCode(user.getCorrespondencePincode())
				.active(user.getActive())
				.locale(user.getLocale())
				.accountLocked(user.getAccountLocked())
				.fatherOrHusbandName(user.getFatherOrHusbandName())
				.signature(user.getSignature())
				.bloodGroup(user.getBloodGroup())
				.photo(user.getPhoto())
				.identificationMark(user.getIdentificationMark())
				.password(user.getPassword())
				.otpReference(user.getOtpReference())
				.tenantId(user.getTenantId())

				// Long → Date
				.createdDate(user.getCreatedDate() != null
						? new Date(user.getCreatedDate())
						: null)
				.lastModifiedDate(user.getLastModifiedDate() != null
						? new Date(user.getLastModifiedDate())
						: null)
				.dob(user.getDob() != null
						? new Date(user.getDob())
						: null)
				.pwdExpiryDate(user.getPwdExpiryDate() != null
						? new Date(user.getPwdExpiryDate())
						: null)

				// Roles
				.roles(mapRoleRequests(user.getRoles()))

				.build();
	}

	private Set<RoleRequest> mapRoleRequests(List<org.egov.web.models.Role> roles) {

		if (roles == null) return new HashSet<>();

		return roles.stream()
				.map(r -> RoleRequest.builder()
						.code(r.getCode())
						.name(r.getName())
						.tenantId(r.getTenantId())
						.build())
				.collect(Collectors.toSet());
	}


}