package org.egov.wf.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.querybuilder.BusinessServiceQueryBuilder;
import org.egov.wf.repository.rowmapper.BusinessServiceRowMapper;
import org.egov.wf.service.MDMSService;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Repository
public class BusinessServiceRepository {


    private BusinessServiceQueryBuilder queryBuilder;

    private JdbcTemplate jdbcTemplate;

    private BusinessServiceRowMapper rowMapper;

    private WorkflowConfig config;

    private MDMSService mdmsService;


    @Autowired
    public BusinessServiceRepository(BusinessServiceQueryBuilder queryBuilder, JdbcTemplate jdbcTemplate,
                                     BusinessServiceRowMapper rowMapper, WorkflowConfig config, MDMSService mdmsService) {
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.config = config;
        this.mdmsService = mdmsService;
    }






    public List<BusinessService> getBusinessServices(BusinessServiceSearchCriteria criteria){
        log.trace("Entering getBusinessServices method");
        String tenantId = criteria.getTenantId();
        log.info("Fetching business services for tenantId: {}", tenantId);
        
        String query;

        List<String> stateLevelBusinessServices = new LinkedList<>();
        List<String> tenantBusinessServices = new LinkedList<>();

        Map<String, Boolean> stateLevelMapping = mdmsService.getStateLevelMapping();

        if(!CollectionUtils.isEmpty(criteria.getBusinessServices())){

            criteria.getBusinessServices().forEach(businessService -> {
                if(stateLevelMapping.get(businessService)==null || stateLevelMapping.get(businessService))
                    stateLevelBusinessServices.add(businessService);
                else
                    tenantBusinessServices.add(businessService);
            });
        }

        List<BusinessService> searchResults = new LinkedList<>();

        if(!CollectionUtils.isEmpty(stateLevelBusinessServices)){
            BusinessServiceSearchCriteria stateLevelCriteria = new BusinessServiceSearchCriteria();
            stateLevelCriteria.setTenantId(criteria.getTenantId().split("\\.")[0]);
            stateLevelCriteria.setBusinessServices(stateLevelBusinessServices);
            List<Object> stateLevelPreparedStmtList = new ArrayList<>();
            query = queryBuilder.getBusinessServices(stateLevelCriteria, stateLevelPreparedStmtList);
            searchResults.addAll(jdbcTemplate.query(query, stateLevelPreparedStmtList.toArray(), rowMapper));
        }
        if(!CollectionUtils.isEmpty(tenantBusinessServices)){
            BusinessServiceSearchCriteria tenantLevelCriteria = new BusinessServiceSearchCriteria();
            tenantLevelCriteria.setTenantId(criteria.getTenantId());
            tenantLevelCriteria.setBusinessServices(tenantBusinessServices);
            List<Object> tenantLevelPreparedStmtList = new ArrayList<>();
            query = queryBuilder.getBusinessServices(tenantLevelCriteria, tenantLevelPreparedStmtList);
            searchResults.addAll(jdbcTemplate.query(query, tenantLevelPreparedStmtList.toArray(), rowMapper));
        }

        log.info("Retrieved {} business service(s) from database", searchResults != null ? searchResults.size() : 0);
        log.trace("Exiting getBusinessServices method");
        return searchResults;
    }


    /**
     * Creates map of roles vs tenantId vs List of status uuids from all the avialable businessServices
     * @return
     */
    @Cacheable(value = "roleTenantAndStatusesMapping")
    public Map<String,Map<String,List<String>>> getRoleTenantAndStatusMapping(){
        log.trace("Entering getRoleTenantAndStatusMapping method");
        log.info("Building role tenant and status mapping from all business services");

        Map<String, Map<String,List<String>>> roleTenantAndStatusMapping = new HashMap();

        List<BusinessService> businessServices = getAllBusinessService();
        log.debug("Retrieved {} business service(s) for mapping", businessServices != null ? businessServices.size() : 0);

        for(BusinessService businessService : businessServices){

            String tenantId = businessService.getTenantId();

            for(State state : businessService.getStates()){

                String uuid = state.getUuid();

                if(!CollectionUtils.isEmpty(state.getActions())){

                    for(Action action : state.getActions()){

                        List<String> roles = action.getRoles();

                        if(!CollectionUtils.isEmpty(roles)){
                            for(String role : roles){

                                Map<String, List<String>> tenantToStatusMap;

                                if (roleTenantAndStatusMapping.containsKey(role))
                                    tenantToStatusMap = roleTenantAndStatusMapping.get(role);
                                else tenantToStatusMap = new HashMap();

                                List<String> statuses;

                                if(tenantToStatusMap.containsKey(tenantId))
                                    statuses = tenantToStatusMap.get(tenantId);
                                else statuses = new LinkedList<>();

                                statuses.add(uuid);

                                tenantToStatusMap.put(tenantId, statuses);
                                roleTenantAndStatusMapping.put(role, tenantToStatusMap);
                            }
                        }
                    }

                }

            }

        }

        log.info("Successfully built role tenant and status mapping with {} role(s)", roleTenantAndStatusMapping.size());
        log.trace("Exiting getRoleTenantAndStatusMapping method");
        return roleTenantAndStatusMapping;

    }

    /**
     * Returns all the avialable businessServices
     * @return
     */
    private List<BusinessService> getAllBusinessService(){
        log.trace("Entering getAllBusinessService method");

        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getBusinessServices(new BusinessServiceSearchCriteria(), preparedStmtList);
        log.debug("Query for all business services: {} with params: {}", query, preparedStmtList);

        List<BusinessService> businessServices = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        log.debug("Retrieved {} business service(s) before filtering", businessServices != null ? businessServices.size() : 0);
        
        List<BusinessService> filterBusinessServices = filterBusinessServices((businessServices));
        log.debug("Filtered to {} business service(s)", filterBusinessServices != null ? filterBusinessServices.size() : 0);
        log.trace("Exiting getAllBusinessService method");
        return filterBusinessServices;
    }


    /**
     * Will filter out configurations which are not in sync with MDMS master data
     * @param businessServices
     * @return
     */
    private List<BusinessService> filterBusinessServices(List<BusinessService> businessServices){
        log.trace("Entering filterBusinessServices method");
        int inputSize = businessServices != null ? businessServices.size() : 0;
        log.debug("Filtering {} business service(s) based on MDMS state level mapping", inputSize);

        Map<String, Boolean> stateLevelMapping = mdmsService.getStateLevelMapping();
        List<BusinessService> filteredBusinessService = new LinkedList<>();

        for(BusinessService businessService : businessServices){

            String code = businessService.getBusinessService();
            String tenantId = businessService.getTenantId();
            Boolean isStatelevel = stateLevelMapping.get(code);

            if(isStatelevel == null){
                isStatelevel = true;
               // throw new CustomException("INVALID_MDMS_CONFIG","The master data is missing for businessService: "+code);
            }

            if(isStatelevel){
                if(tenantId.equalsIgnoreCase(config.getStateLevelTenantId())){
                    filteredBusinessService.add(businessService);
                }
            }
            else {
                if(!tenantId.equalsIgnoreCase(config.getStateLevelTenantId())){
                    filteredBusinessService.add(businessService);
                }
            }
        }
        
        int filteredSize = filteredBusinessService.size();
        if(inputSize != filteredSize) {
            log.debug("Filtered {} business service(s) out, {} remaining", inputSize - filteredSize, filteredSize);
        }
        log.trace("Exiting filterBusinessServices method");
        return filteredBusinessService;
    }





}
