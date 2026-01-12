package org.egov.asset.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.UserDetailResponse;
import digit.models.coremodels.user.Role;
import digit.models.coremodels.user.User;
import digit.models.coremodels.user.enums.UserType;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.config.Configuration;
import org.egov.asset.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static org.egov.asset.config.ServiceConstants.*;

@Component
@Slf4j
public class UserUtil {

    private final ObjectMapper mapper;

    private final ServiceRequestRepository serviceRequestRepository;

    private final Configuration configs;


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
        log.trace("UserUtil::userCall called");
        String uriString = uri.toString();
        log.info("Calling user service | uri={}", uriString);
        String dobFormat = null;
        if (uriString.contains(configs.getUserSearchEndpoint()) || uriString.contains(configs.getUserUpdateEndpoint()))
            dobFormat = DOB_FORMAT_Y_M_D;
        else if (uriString.contains(configs.getUserCreateEndpoint()))
            dobFormat = DOB_FORMAT_D_M_Y;
        else
            dobFormat = DOB_FORMAT_Y_M_D;

        try {
            log.debug("Fetching user data | uri={} dobFormat={}", uriString, dobFormat);
            LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest, LinkedHashMap.class);
            if (responseMap == null) {
                log.error("Null response from user service | uri={}", uriString);
                throw new CustomException("USER_SERVICE_RESPONSE_ERROR", "Received null response from user service");
            }
            parseResponse(responseMap, dobFormat);
            UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
            log.debug("User data parsed successfully | uri={}", uriString);
            return userDetailResponse;
        } catch (IllegalArgumentException e) {
            log.error("Error parsing user response | uri={} error={}", uriString, e.getMessage(), e);
            throw new CustomException(ILLEGAL_ARGUMENT_EXCEPTION_CODE, OBJECTMAPPER_UNABLE_TO_CONVERT);
        } catch (Exception e) {
            log.error("Unexpected error in user call | uri={} error={}", uriString, e.getMessage(), e);
            throw new CustomException();
        }
    }


    /**
     * Parses date formats to long for all users in responseMap
     *
     * @param responseMap LinkedHashMap got from user api response
     */

    public void parseResponse(LinkedHashMap responseMap, String dobFormat) {
        log.trace("UserUtil::parseResponse called");
        log.debug("Parsing user response | dobFormat={}", dobFormat);
        if (responseMap == null) {
            log.debug("Response map is null, skipping parsing");
            return;
        }
        List<LinkedHashMap> users = (List<LinkedHashMap>) responseMap.get(USER);
        String format1 = DOB_FORMAT_D_M_Y_H_M_S;
        if (users != null) {
            log.debug("Parsing dates for users | usersCount={}", users.size());
            users.forEach(map -> {
                        map.put(CREATED_DATE, dateTolong((String) map.get(CREATED_DATE), format1));
                        if (map.get(LAST_MODIFIED_DATE) != null)
                            map.put(LAST_MODIFIED_DATE, dateTolong((String) map.get(LAST_MODIFIED_DATE), format1));
                        if (map.get(DOB) != null)
                            map.put(DOB, dateTolong((String) map.get(DOB), dobFormat));
                        if (map.get(PWD_EXPIRY_DATE) != null)
                            map.put(PWD_EXPIRY_DATE, dateTolong((String) map.get(PWD_EXPIRY_DATE), format1));
                    }
            );
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
        log.trace("UserUtil::dateTolong called");
        log.debug("Converting date to long | date={} format={}", date, format);
        if (date == null || format == null) {
            log.error("Invalid date input | date={} format={}", date, format);
            throw new CustomException("INVALID_DATE_INPUT", "Date or format is null");
        }
        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            log.warn("Date format parsing error | date={} format={} error={}", date, format, e.getMessage());
            throw new CustomException(INVALID_DATE_FORMAT_CODE, INVALID_DATE_FORMAT_MESSAGE);
        } catch (Exception e) {
            log.error("Error processing date | date={} format={} error={}", date, format, e.getMessage(), e);
            throw new CustomException("DATE_PROCESSING_ERROR", "Error processing date: " + e.getMessage());
        }
        return d.getTime();
    }

    /**
     * enriches the userInfo with statelevel tenantId and other fields
     * The function creates user with username as mobile number.
     *
     * @param mobileNumber
     * @param tenantId
     * @param userInfo
     */
    public void addUserDefaultFields(String mobileNumber, String tenantId, User userInfo, UserType userType) {
        log.trace("UserUtil::addUserDefaultFields called");
        log.debug("Adding default user fields | mobileNumber={} tenantId={}", mobileNumber, tenantId);
        if (userInfo == null) {
            log.error("User info is null");
            throw new CustomException("INVALID_USER_INFO", "User info cannot be null");
        }
        if (userType == null) {
            log.error("User type is null");
            throw new CustomException("INVALID_USER_TYPE", "User type cannot be null");
        }
        Role role = getCitizenRole(tenantId);
        userInfo.setRoles(Collections.singleton(role));
        userInfo.setType(userType);
        userInfo.setUsername(mobileNumber);
        userInfo.setTenantId(getStateLevelTenant(tenantId));
        userInfo.setActive(true);
        log.debug("Default user fields added successfully");
    }

    /**
     * Returns role object for citizen
     *
     * @param tenantId
     * @return
     */
    private Role getCitizenRole(String tenantId) {
        log.trace("UserUtil::getCitizenRole called");
        log.debug("Getting citizen role | tenantId={}", tenantId);
        Role role = Role.builder().build();
        role.setCode(CITIZEN_UPPER);
        role.setName(CITIZEN_LOWER);
        role.setTenantId(getStateLevelTenant(tenantId));
        return role;
    }

    public String getStateLevelTenant(String tenantId) {
        log.trace("UserUtil::getStateLevelTenant called");
        log.debug("Extracting state level tenant | tenantId={}", tenantId);
        if (tenantId == null || tenantId.isEmpty()) {
            log.error("Invalid tenant ID | tenantId={}", tenantId);
            throw new CustomException("INVALID_TENANT_ID", "TenantId cannot be null or empty");
        }
        String[] tenantParts = tenantId.split("\\.");
        if (tenantParts.length == 0) {
            log.error("Invalid tenant ID format | tenantId={}", tenantId);
            throw new CustomException();
        }
        log.debug("State level tenant extracted | tenantId={} stateLevelTenant={}", tenantId, tenantParts[0]);
        return tenantParts[0];
    }

}