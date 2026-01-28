package org.egov.wf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.ServiceRequestRepository;
import org.egov.wf.web.models.user.UserDetailResponse;
import org.egov.wf.web.models.user.UserSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Slf4j
public class UserService {


    private WorkflowConfig config;

    private ServiceRequestRepository serviceRequestRepository;

    private ObjectMapper mapper;


    @Autowired
    public UserService(WorkflowConfig config, ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
    }


    /**
     * Calls search api of user to fetch the user for the given uuid
     * @param uuids The list of uuid of the user's
     * @return OwnerInfo of the user with the given uuid
     */
    public Map<String,User> searchUser(RequestInfo requestInfo,List<String> uuids){
        log.trace("Entering searchUser method");
        int uuidCount = uuids != null ? uuids.size() : 0;
        log.debug("Searching for {} user(s) by UUID", uuidCount);
        
        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setUuid(uuids);
        StringBuilder url = new StringBuilder(config.getUserHost());
        url.append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userCall(userSearchRequest,url);
        if(CollectionUtils.isEmpty(userDetailResponse.getUser())) {
            log.error("No users found for the provided UUIDs");
            throw new CustomException("INVALID USER","No user found for the uuids: "+uuids);
        }
        
        Map<String,User> idToUserMap = new HashMap<>();
        userDetailResponse.getUser().forEach(user -> {
            idToUserMap.put(user.getUuid(),user);
        });
        
        log.debug("Successfully retrieved {} user(s)", idToUserMap.size());
        log.trace("Exiting searchUser method");
        return idToUserMap;
    }

    public List<String> searchUserUuidsBasedOnRoleCodes(UserSearchRequest userSearchRequest){
        log.trace("Entering searchUserUuidsBasedOnRoleCodes method");
        log.debug("Searching user UUIDs based on role codes");
        
        StringBuilder url = new StringBuilder(config.getUserHost());
        url.append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userCall(userSearchRequest,url);
        if(CollectionUtils.isEmpty(userDetailResponse.getUser())) {
            log.error("No users found for roleCodes: {}", userSearchRequest.getRoleCodes());
            throw new CustomException("INVALID USER","No user found for the roleCodes: " + userSearchRequest.getRoleCodes());
        }
        
        List<String> roleSpecificUsersUuids = new ArrayList<>();
        userDetailResponse.getUser().forEach(user -> {
           roleSpecificUsersUuids.add(user.getUuid());
        });
        
        log.debug("Found {} user UUID(s) for role codes", roleSpecificUsersUuids.size());
        log.trace("Exiting searchUserUuidsBasedOnRoleCodes method");
        return roleSpecificUsersUuids;
    }



    /**
     * Returns UserDetailResponse by calling user service with given uri and object
     * @param userRequest Request object for user service
     * @param uri The address of the endpoint
     * @return Response from user service as parsed as userDetailResponse
     */
    private UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
        log.trace("Entering userCall method");
        String dobFormat = "yyyy-MM-dd";

        try{
            LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, userRequest);
            parseResponse(responseMap,dobFormat);
            UserDetailResponse userDetailResponse = mapper.convertValue(responseMap,UserDetailResponse.class);
            log.trace("Exiting userCall method");
            return userDetailResponse;
        }
        catch(IllegalArgumentException  e)
        {
            log.error("IllegalArgumentException while converting response in userCall", e);
            throw new CustomException("IllegalArgumentException","ObjectMapper not able to convertValue in userCall");
        }
    }


    /**
     * Parses date formats to long for all users in responseMap
     * @param responeMap LinkedHashMap got from user api response
     */
    private void parseResponse(LinkedHashMap responeMap,String dobFormat){
        List<LinkedHashMap> users = (List<LinkedHashMap>)responeMap.get("user");
        String format1 = "dd-MM-yyyy HH:mm:ss";
        if(users!=null){
            users.forEach( map -> {
                        map.put("createdDate",dateTolong((String)map.get("createdDate"),format1));
                        if((String)map.get("lastModifiedDate")!=null)
                            map.put("lastModifiedDate",dateTolong((String)map.get("lastModifiedDate"),format1));
                        if((String)map.get("dob")!=null)
                            map.put("dob",dateTolong((String)map.get("dob"),dobFormat));
                        if((String)map.get("pwdExpiryDate")!=null)
                            map.put("pwdExpiryDate",dateTolong((String)map.get("pwdExpiryDate"),format1));
                    }
            );
        }
    }



    /**
     * Converts date to long
     * @param date date to be parsed
     * @param format Format of the date
     * @return Long value of date
     */
    private Long dateTolong(String date,String format){
        log.trace("Entering dateTolong method");
        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            log.error("Error while parsing user date: {} with format: {}", date, format, e);
        }
        Long result = d != null ? d.getTime() : null;
        log.trace("Exiting dateTolong method");
        return result;
    }




}
