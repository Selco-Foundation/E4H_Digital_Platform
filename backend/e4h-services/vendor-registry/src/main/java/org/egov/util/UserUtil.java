package org.egov.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.contract.user.enums.UserType;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
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
	public void addUserDefaultFields(String mobileNumber, String tenantId, User userInfo, UserType userType) {
		log.trace("UserUtil::addUserDefaultFields entry");
		log.debug("Setting default fields for user with mobile: {}, tenant: {}, type: {}", mobileNumber, tenantId, userType);
		Role role = getCitizenRole(tenantId);
		userInfo.setRoles(Collections.singletonList(role));
		userInfo.setType(userType.toString());
		userInfo.setUserName(mobileNumber);
		userInfo.setTenantId(getStateLevelTenant(tenantId));
		log.debug("Default fields set successfully");
	}

	/**
	 * Returns role object for citizen
	 * 
	 * @param tenantId
	 * @return
	 */
	private Role getCitizenRole(String tenantId) {
		log.trace("UserUtil::getCitizenRole entry");
		Role role = Role.builder().build();
		role.setCode("CITIZEN");
		role.setName("Citizen");
		role.setTenantId(getStateLevelTenant(tenantId));
		log.debug("Created citizen role for tenant: {}", tenantId);
		return role;
	}

	public String getStateLevelTenant(String tenantId) {
		log.trace("UserUtil::getStateLevelTenant entry");
		String stateLevelTenant = tenantId.split("\\.")[0];
		log.debug("Extracted state level tenant: {} from tenant: {}", stateLevelTenant, tenantId);
		return stateLevelTenant;
	}

}