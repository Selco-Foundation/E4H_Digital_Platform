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
        log.info("UserUtil::userCall called | uri={}", uri.toString());
        String dobFormat = null;
        String uriString = uri.toString();
        if (uriString.contains(configs.getUserSearchEndpoint()) || uriString.contains(configs.getUserUpdateEndpoint()))
            dobFormat = DOB_FORMAT_Y_M_D;
        else if (uriString.contains(configs.getUserCreateEndpoint()))
            dobFormat = DOB_FORMAT_D_M_Y;
        else
            dobFormat = DOB_FORMAT_Y_M_D; // Default format

        try {
            LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest, LinkedHashMap.class);
            if (responseMap == null) {
                throw new CustomException("USER_SERVICE_RESPONSE_ERROR", "Received null response from user service");
            }
            parseResponse(responseMap, dobFormat);
            UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
            return userDetailResponse;
        } catch (IllegalArgumentException e) {
            throw new CustomException(ILLEGAL_ARGUMENT_EXCEPTION_CODE, OBJECTMAPPER_UNABLE_TO_CONVERT);
        } catch (Exception e) {
            throw new CustomException();
        }
    }


    /**
     * Parses date formats to long for all users in responseMap
     *
     * @param responseMap LinkedHashMap got from user api response
     */

    public void parseResponse(LinkedHashMap responseMap, String dobFormat) {
        log.info("UserUtil::parseResponse");
        if (responseMap == null) {
            return;
        }
        List<LinkedHashMap> users = (List<LinkedHashMap>) responseMap.get(USER);
        String format1 = DOB_FORMAT_D_M_Y_H_M_S;
        if (users != null) {
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
        log.info("UserUtil::dateTolong called | date={}", date);
        if (date == null || format == null) {
            throw new CustomException("INVALID_DATE_INPUT", "Date or format is null");
        }
        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            throw new CustomException(INVALID_DATE_FORMAT_CODE, INVALID_DATE_FORMAT_MESSAGE);
        } catch (Exception e) {
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
        if (userInfo == null) {
            throw new CustomException("INVALID_USER_INFO", "User info cannot be null");
        }
        if (userType == null) {
            throw new CustomException("INVALID_USER_TYPE", "User type cannot be null");
        }
        Role role = getCitizenRole(tenantId);
        userInfo.setRoles(Collections.singleton(role));
        userInfo.setType(userType);
        userInfo.setUsername(mobileNumber);
        userInfo.setTenantId(getStateLevelTenant(tenantId));
        userInfo.setActive(true);
    }

    /**
     * Returns role object for citizen
     *
     * @param tenantId
     * @return
     */
    private Role getCitizenRole(String tenantId) {
        log.info("UserUtil::getCitizenRole called | tenantId={}", tenantId);
        Role role = Role.builder().build();
        role.setCode(CITIZEN_UPPER);
        role.setName(CITIZEN_LOWER);
        role.setTenantId(getStateLevelTenant(tenantId));
        return role;
    }

    public String getStateLevelTenant(String tenantId) {
        log.info("UserUtil::getStateLevelTenant called | tenantId={}", tenantId);
        if (tenantId == null || tenantId.isEmpty()) {
            throw new CustomException("INVALID_TENANT_ID", "TenantId cannot be null or empty");
        }
        String[] tenantParts = tenantId.split("\\.");
        if (tenantParts.length == 0) {
            throw new CustomException();
        }
        return tenantParts[0];
    }

}