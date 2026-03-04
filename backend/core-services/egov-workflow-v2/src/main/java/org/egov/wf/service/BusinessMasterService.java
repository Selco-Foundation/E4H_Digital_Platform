package org.egov.wf.service;

import com.jayway.jsonpath.JsonPath;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.BusinessServiceRepository;
import org.egov.wf.web.models.BusinessService;
import org.egov.wf.web.models.BusinessServiceRequest;
import org.egov.wf.web.models.BusinessServiceSearchCriteria;
import org.egov.wf.web.models.ProcessInstanceSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.wf.util.WorkflowConstants.JSONPATH_BUSINESSSERVICE_STATELEVEL;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BusinessMasterService {

    private Producer producer;

    private WorkflowConfig config;

    private EnrichmentService enrichmentService;

    private BusinessServiceRepository repository;

    private MDMSService mdmsService;

    private CacheManager cacheManager;

    @Autowired
    public BusinessMasterService(Producer producer, WorkflowConfig config, EnrichmentService enrichmentService,
                                 BusinessServiceRepository repository, MDMSService mdmsService, CacheManager cacheManager) {
        this.producer = producer;
        this.config = config;
        this.enrichmentService = enrichmentService;
        this.repository = repository;
        this.mdmsService = mdmsService;
        this.cacheManager = cacheManager;
    }




    /**
     * Enriches and sends the request on kafka to persist
     * @param request The BusinessServiceRequest to be persisted
     * @return The enriched object which is persisted
     */
    public List<BusinessService> create(BusinessServiceRequest request){
        log.trace("Entering create method");
        int businessServiceCount = request.getBusinessServices() != null ? request.getBusinessServices().size() : 0;
        log.info("Creating {} business service(s)", businessServiceCount);
        
        evictAllCacheValues("businessService");
        evictAllCacheValues("roleTenantAndStatusesMapping");
        log.debug("Evicted cache values for business service");
        
        enrichmentService.enrichCreateBusinessService(request);
        String topic = config.getSaveBusinessServiceTopic();
        log.debug("Pushing business service create request to topic: {}", topic);
        producer.push(topic,request);
        log.info("Successfully created business service(s)");
        log.trace("Exiting create method");
        return request.getBusinessServices();
    }

    /**
     * Fetches business service object from db
     * @param criteria The search criteria
     * @return Data fetched from db
     */
    @Cacheable(value = "businessService")
    public List<BusinessService> search(BusinessServiceSearchCriteria criteria){
        log.trace("Entering search method");
        String tenantId = criteria.getTenantId();
        log.info("Searching business services for tenantId: {}", tenantId);
        
        List<BusinessService> businessServices = repository.getBusinessServices(criteria);
        log.debug("Retrieved {} business service(s) from repository", businessServices != null ? businessServices.size() : 0);
        
        enrichmentService.enrichTenantIdForStateLevel(tenantId,businessServices);
        log.info("Business service search completed successfully");
        log.trace("Exiting search method");
        return businessServices;
    }



    public List<BusinessService> update(BusinessServiceRequest request){
        log.trace("Entering update method");
        int businessServiceCount = request.getBusinessServices() != null ? request.getBusinessServices().size() : 0;
        log.info("Updating {} business service(s)", businessServiceCount);
        
        evictAllCacheValues("businessService");
        evictAllCacheValues("roleTenantAndStatusesMapping");
        log.debug("Evicted cache values for business service");
        
        enrichmentService.enrichUpdateBusinessService(request);
        String topic = config.getUpdateBusinessServiceTopic();
        log.debug("Pushing business service update request to topic: {}", topic);
        producer.push(topic,request);
        log.info("Successfully updated business service(s)");
        log.trace("Exiting update method");
        return request.getBusinessServices();
    }


    private void evictAllCacheValues(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }
    
    public Long getMaxBusinessServiceSla(ProcessInstanceSearchCriteria criteria) {
        log.trace("Entering getMaxBusinessServiceSla method");
        String tenantId = criteria.getTenantId();
        String businessService = criteria.getBusinessService();
        log.debug("Fetching max business service SLA for tenantId: {}, businessService: {}", tenantId, businessService);
        
        BusinessServiceSearchCriteria searchCriteria = new BusinessServiceSearchCriteria();
        searchCriteria.setTenantId(tenantId);
        searchCriteria.setBusinessServices(Collections.singletonList(businessService));
        List<BusinessService> businessServices = repository.getBusinessServices(searchCriteria);
        enrichmentService.enrichTenantIdForStateLevel(tenantId,businessServices);

        Long maxSla = businessServices.get(0).getBusinessServiceSla();
        log.debug("Retrieved max business service SLA: {}", maxSla);
        log.trace("Exiting getMaxBusinessServiceSla method");
        return maxSla;
    }



}
