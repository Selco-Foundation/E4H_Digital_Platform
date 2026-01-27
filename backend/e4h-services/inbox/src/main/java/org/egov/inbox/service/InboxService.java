package org.egov.inbox.service;

import static org.egov.inbox.util.BpaConstants.BPA;
import static org.egov.inbox.util.BpaConstants.BPAREG;
import static org.egov.inbox.util.BpaConstants.BPA_APPLICATION_NUMBER_PARAM;
import static org.egov.inbox.util.BpaConstants.LOCALITY_PARAM;
import static org.egov.inbox.util.BpaConstants.MOBILE_NUMBER_PARAM;
import static org.egov.inbox.util.BpaConstants.OFFSET_PARAM;
import static org.egov.inbox.util.BpaConstants.STATUS_ID;
import static org.egov.inbox.util.BpaConstants.STATUS_PARAM;
import static org.egov.inbox.util.DSSConstants.*;
import static org.egov.inbox.util.FSMConstants.APPLICATIONSTATUS;
import static org.egov.inbox.util.FSMConstants.CITIZEN_FEEDBACK_PENDING_STATE;
import static org.egov.inbox.util.FSMConstants.COMPLETED_STATE;
import static org.egov.inbox.util.FSMConstants.COUNT;
import static org.egov.inbox.util.FSMConstants.DISPOSED_STATE;
import static org.egov.inbox.util.FSMConstants.DSO_INPROGRESS_STATE;
import static org.egov.inbox.util.FSMConstants.FSM_VEHICLE_TRIP_MODULE;
import static org.egov.inbox.util.FSMConstants.STATUSID;
import static org.egov.inbox.util.FSMConstants.VEHICLE_LOG;
import static org.egov.inbox.util.FSMConstants.WAITING_FOR_DISPOSAL_STATE;
import static org.egov.inbox.util.NocConstants.NOC;
import static org.egov.inbox.util.NocConstants.NOC_APPLICATION_NUMBER_PARAM;
import static org.egov.inbox.util.PTConstants.ACKNOWLEDGEMENT_IDS_PARAM;
import static org.egov.inbox.util.PTConstants.PT;
import static org.egov.inbox.util.TLConstants.APPLICATION_NUMBER_PARAM;
import static org.egov.inbox.util.TLConstants.BUSINESS_SERVICE_PARAM;
import static org.egov.inbox.util.TLConstants.REQUESTINFO_PARAM;
import static org.egov.inbox.util.TLConstants.SEARCH_CRITERIA_PARAM;
import static org.egov.inbox.util.TLConstants.TENANT_ID_PARAM;
import static org.egov.inbox.util.TLConstants.TL;
import static org.egov.inbox.util.SWConstants.SW;
import static org.egov.inbox.util.BSConstants.*;
import static org.egov.inbox.util.WSConstants.WS;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections4.MapUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.model.InboxProcessingContext;
import org.egov.inbox.model.vehicle.VehicleSearchCriteria;
import org.egov.inbox.model.vehicle.VehicleTripDetail;
import org.egov.inbox.model.vehicle.VehicleTripDetailResponse;
import org.egov.inbox.model.vehicle.VehicleTripSearchCriteria;
import org.egov.inbox.repository.ElasticSearchRepository;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.util.BpaConstants;
import org.egov.inbox.util.ErrorConstants;
import org.egov.inbox.util.FSMConstants;
import org.egov.inbox.util.TLConstants;
import org.egov.inbox.web.model.Inbox;
import org.egov.inbox.web.model.InboxResponse;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.inbox.web.model.RequestInfoWrapper;
import org.egov.inbox.web.model.VehicleCustomResponse;
import org.egov.inbox.web.model.workflow.BusinessService;
import org.egov.inbox.web.model.workflow.ProcessInstance;
import org.egov.inbox.web.model.workflow.ProcessInstanceResponse;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.inbox.web.model.workflow.State;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InboxService {

    private InboxConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    private ObjectMapper mapper;

    private WorkflowService workflowService;

    @Autowired
    private PtInboxFilterService ptInboxFilterService;

    @Autowired
    private TLInboxFilterService tlInboxFilterService;

    @Autowired
    private BPAInboxFilterService bpaInboxFilterService;

    @Autowired
    private FSMInboxFilterService fsmInboxFilter;
    
    @Autowired
    private NOCInboxFilterService nocInboxFilterService;

    @Autowired
    private WSInboxFilterService wsInboxFilterService;
    
    @Autowired
    private SWInboxFilterService swInboxFilterService;
    
    @Autowired
    private BillingAmendmentInboxFilterService billInboxFilterService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    ElasticSearchRepository elasticSearchRepository;

    @Autowired
    public InboxService(InboxConfiguration config, ServiceRequestRepository serviceRequestRepository,
            ObjectMapper mapper, WorkflowService workflowService) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
        this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        this.workflowService = workflowService;
    }

    public InboxResponse fetchInboxData(InboxSearchCriteria criteria, RequestInfo requestInfo) {
        log.trace("Method invoked: fetchInboxData");
        log.info("Fetching inbox data - tenantId: {}, module: {}", 
                criteria.getTenantId(), 
                criteria.getProcessSearchCriteria() != null ? criteria.getProcessSearchCriteria().getModuleName() : null);
        
        InboxProcessingContext context = initializeProcessingContext(criteria, requestInfo);
        context = prepareStatusCountMap(context, requestInfo);
        context = processModuleSearchCriteria(context, requestInfo);
        context = processFsmModuleIfNeeded(context, requestInfo);
        
        return buildInboxResponse(context);
    }

    /**
     * Initializes the processing context with basic setup
     */
    private InboxProcessingContext initializeProcessingContext(InboxSearchCriteria criteria, RequestInfo requestInfo) {
        log.trace("Method invoked: initializeProcessingContext");
        log.debug("Initializing processing context - tenantId: {}", criteria.getTenantId());
        
        InboxProcessingContext context = new InboxProcessingContext();
        context.criteria = criteria;
        context.requestInfo = requestInfo;
        context.processCriteria = criteria.getProcessSearchCriteria();
        context.moduleSearchCriteria = criteria.getModuleSearchCriteria();
        context.processCriteria.setTenantId(criteria.getTenantId());
        log.debug("Process search criteria initialized - tenantId: {}", criteria.getTenantId());
        
        context.flag = convertModuleNameIfNeeded(context.processCriteria);
        context.totalCount = getTotalCount(criteria, requestInfo, context.processCriteria);
        context.nearingSlaProcessCount = workflowService.getNearingSlaProcessCount(
                criteria.getTenantId(), requestInfo, context.processCriteria);
        log.debug("Nearing SLA process count retrieved: {}", context.nearingSlaProcessCount);
        
        context.inputStatuses = new ArrayList<>();
        if (!CollectionUtils.isEmpty(context.processCriteria.getStatus())) {
            context.inputStatuses = new ArrayList<>(context.processCriteria.getStatus());
        }
        
        context.dsoId = fetchDsoIdIfNeeded(criteria, requestInfo);
        context.assigneeUuid = handleAssignee(context.processCriteria);
        context.roles = requestInfo.getUserInfo().getRoles().stream()
                .map(Role::getCode).collect(Collectors.toList());
        context.originalModuleName = context.processCriteria.getModuleName();
        
        context.businessServiceName = context.processCriteria.getBusinessService();
        if (CollectionUtils.isEmpty(context.businessServiceName)) {
            log.error("Business service is empty");
            throw new CustomException(ErrorConstants.MODULE_SEARCH_INVLAID, 
                    "Bussiness Service is mandatory for module search");
        }
        
        context.srvMap = fetchAppropriateServiceMap(context.businessServiceName, context.originalModuleName);
        context.inboxes = new ArrayList<>();
        context.response = new InboxResponse();
        context.businessObjects = null;
        context.businessServiceSlaMap = new HashMap<>();
        context.tenantAndApplnNumbersMap = new HashMap<>();
        
        log.debug("Processing context initialized successfully");
        return context;
    }

    /**
     * Prepares status count map by fetching from workflow service
     */
    private InboxProcessingContext prepareStatusCountMap(InboxProcessingContext context, RequestInfo requestInfo) {
        log.trace("Method invoked: prepareStatusCountMap");
        log.debug("Preparing status count map");
        
        // Since we want the whole status count map regardless of the status filter and assignee filter being passed
        context.processCriteria.setAssignee(null);
        context.processCriteria.setStatus(null);
        
        context.statusCountMap = workflowService.getProcessStatusCount(requestInfo, context.processCriteria);
        log.debug("Status count map retrieved - size: {}", context.statusCountMap.size());
        
        context.processCriteria.setModuleName(context.originalModuleName);
        context.processCriteria.setStatus(context.inputStatuses);
        context.processCriteria.setAssignee(context.assigneeUuid.toString());
        
        log.debug("Status count map preparation completed");
        return context;
    }

    /**
     * Processes module search criteria and builds inbox items
     */
    private InboxProcessingContext processModuleSearchCriteria(InboxProcessingContext context, RequestInfo requestInfo) {
        log.trace("Method invoked: processModuleSearchCriteria");
        log.debug("Processing module search criteria");
        
        if (!CollectionUtils.isEmpty(context.moduleSearchCriteria)) {
            context = processNonEmptyModuleSearchCriteria(context, requestInfo);
        } else {
            log.debug("Module search criteria is empty, handling empty criteria scenario");
            handleEmptyModuleSearchCriteria(context.criteria, context.processCriteria, requestInfo, 
                    context.businessServiceName, context.srvMap, context.inboxes);
        }
        
        log.debug("Module search criteria processing completed");
        return context;
    }

    /**
     * Processes non-empty module search criteria
     */
    private InboxProcessingContext processNonEmptyModuleSearchCriteria(InboxProcessingContext context, 
            RequestInfo requestInfo) {
        log.trace("Method invoked: processNonEmptyModuleSearchCriteria");
        log.debug("Processing non-empty module search criteria");
        
        initializeModuleSearchCriteria(context.criteria, context.moduleSearchCriteria);
        List<BusinessService> bussinessSrvs = buildBusinessServicesList(context.criteria, requestInfo, 
                context.businessServiceName, context.businessServiceSlaMap);
        context.statusIdNameMap = workflowService.getActionableStatusesForRole(requestInfo, bussinessSrvs, 
                context.processCriteria);
        String applicationStatusParam = getApplicationStatusParam(context.srvMap);
        context.businessIdParam = context.srvMap.get("businessIdProperty");
        
        applyStatusFilterToModuleSearchCriteria(context.processCriteria, context.statusIdNameMap, 
                context.moduleSearchCriteria, applicationStatusParam);
        
        if (isBpaCitizen(context.processCriteria, context.roles)) {
            log.debug("Processing BPA citizen scenario");
            context.statusCountMap = handleBpaCitizenStatusCount(context.criteria, context.processCriteria, 
                    context.statusIdNameMap, context.moduleSearchCriteria, requestInfo, 
                    context.tenantAndApplnNumbersMap);
        }
        
        if (isBpaModule(context.processCriteria)) {
            log.debug("Processing BPA module locality filtering");
            context.statusCountMap = handleBpaLocalityFiltering(context.criteria, context.processCriteria, 
                    context.statusIdNameMap, context.moduleSearchCriteria, context.statusCountMap, 
                    context.inputStatuses, requestInfo);
        }

        context = applyModuleFiltersAndFetchData(context, requestInfo);
        context = buildInboxItemsFromData(context, requestInfo);
        
        log.debug("Non-empty module search criteria processing completed");
        return context;
    }

    /**
     * Applies module-specific filters and fetches business data
     */
    private InboxProcessingContext applyModuleFiltersAndFetchData(InboxProcessingContext context, 
            RequestInfo requestInfo) {
        log.trace("Method invoked: applyModuleFiltersAndFetchData");
        log.debug("Applying module-specific filters and fetching data");
        
        Boolean[] isSearchResultEmptyRef = new Boolean[] { false };
        List<String> businessKeys = new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        
        Integer updatedTotalCount = applyModuleSpecificFilters(context.criteria, context.processCriteria, 
                context.statusIdNameMap, requestInfo, context.moduleSearchCriteria, context.businessServiceSlaMap, 
                context.flag, context.originalModuleName, result, businessKeys, isSearchResultEmptyRef);
        if (updatedTotalCount != null) {
            context.totalCount = updatedTotalCount;
            log.debug("Total count updated from module filters: {}", updatedTotalCount);
        }
        Boolean isSearchResultEmpty = isSearchResultEmptyRef[0];
        
        context.businessObjects = fetchBusinessObjects(context.moduleSearchCriteria, context.businessServiceName, 
                context.criteria.getTenantId(), requestInfo, context.srvMap, context.processCriteria, 
                isSearchResultEmpty);
        
        log.debug("Module filters applied and data fetched - businessObjects: {}", 
                context.businessObjects != null ? context.businessObjects.length() : 0);
        return context;
    }

    /**
     * Builds inbox items from fetched business data
     */
    private InboxProcessingContext buildInboxItemsFromData(InboxProcessingContext context, RequestInfo requestInfo) {
        log.trace("Method invoked: buildInboxItemsFromData");
        log.debug("Building inbox items from fetched data");
        
        Map<String, Object> businessMap = buildBusinessMap(context.businessObjects, context.businessIdParam);
        ArrayList businessIds = new ArrayList();
        businessIds.addAll(businessMap.keySet());
        context.processCriteria.setBusinessIds(businessIds);
        context.processCriteria.setIsProcessCountCall(false);
        log.debug("Business IDs extracted - count: {}", businessIds.size());

        String businessServiceForAmendment = context.businessServiceName.get(0);
        Boolean isBusinessServiceWSOrSW = businessServiceForAmendment.equalsIgnoreCase(BS_WS_SERVICE)
                || businessServiceForAmendment.equalsIgnoreCase(BS_SW_SERVICE);
        Map<String, Object> serviceSearchMap = fetchServiceSearchObjects(context.criteria, context.processCriteria,
                context.moduleSearchCriteria, context.businessServiceName, requestInfo, context.businessObjects, 
                isBusinessServiceWSOrSW, false, context.originalModuleName);

        ProcessInstanceResponse processInstanceResponse = fetchProcessInstances(context.criteria, 
                context.processCriteria, requestInfo, businessIds, context.tenantAndApplnNumbersMap, context.roles);
        List<ProcessInstance> processInstances = processInstanceResponse.getProcessInstances();
        Map<String, ProcessInstance> processInstanceMap = processInstances.stream()
                .collect(Collectors.toMap(ProcessInstance::getBusinessId, Function.identity()));
        log.debug("Process instances fetched - count: {}", processInstances.size());

        List<String> businessKeys = new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        buildInboxItems(context.inboxes, result, businessMap, businessKeys, processInstanceMap, serviceSearchMap,
                isBusinessServiceWSOrSW, context.originalModuleName);
        
        if (isBusinessServiceWSOrSW && CollectionUtils.isEmpty(businessKeys)) {
            context.totalCount = processInstanceMap.size();
            log.debug("Total count updated for WS/SW: {}", context.totalCount);
        }
        
        log.debug("Inbox items built - count: {}", context.inboxes.size());
        return context;
    }

    /**
     * Processes FSM module if applicable
     */
    private InboxProcessingContext processFsmModuleIfNeeded(InboxProcessingContext context, RequestInfo requestInfo) {
        log.trace("Method invoked: processFsmModuleIfNeeded");
        
        if (!ObjectUtils.isEmpty(context.processCriteria.getModuleName())
                && context.processCriteria.getModuleName().equalsIgnoreCase(FSMConstants.FSM_MODULE)) {
            log.debug("Processing FSM module");
            Integer[] fsmTotalCount = new Integer[] { context.totalCount };
            context.statusCountMap = processFsmModule(context.criteria, context.processCriteria, requestInfo, 
                    context.inputStatuses, context.inboxes, context.moduleSearchCriteria, 
                    context.businessServiceName, context.srvMap, context.statusCountMap, fsmTotalCount);
            context.totalCount = fsmTotalCount[0];
            log.debug("FSM module processing completed - totalCount: {}", context.totalCount);
        } else {
            log.debug("FSM module processing not required");
        }
        
        return context;
    }

    /**
     * Builds and returns the final inbox response
     */
    private InboxResponse buildInboxResponse(InboxProcessingContext context) {
        log.trace("Method invoked: buildInboxResponse");
        log.info("Status count map size: {}", context.statusCountMap.size());
        
        log.debug("Building inbox response");
        context.response.setTotalCount(context.totalCount);
        context.response.setNearingSlaCount(context.nearingSlaProcessCount);
        context.response.setStatusMap(context.statusCountMap);
        context.response.setItems(context.inboxes);
        
        int itemCount = context.inboxes != null ? context.inboxes.size() : 0;
        log.info("Inbox data fetched successfully - totalCount: {}, nearingSlaCount: {}, itemCount: {}, statusMapSize: {}", 
                context.totalCount, context.nearingSlaProcessCount, itemCount, context.statusCountMap.size());
        return context.response;
    }

    /**
     * @param businessServiceSlaMap
     * @param data -- application object
     * @return
     * Description : Calculate ServiceSLA for each application for WS and SW
     */
    private Long getApplicationServiceSla(Map<String, Long> businessServiceSlaMap, Object data) {
        log.trace("Method invoked: getApplicationServiceSla");
        Long currentDate = System.currentTimeMillis(); //current time
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> properties = mapper.convertValue(data, Map.class);
        Map<String, Object> additionalDetails = (Map<String, Object>) properties.get("additionalDetails");
        if (!ObjectUtils.isEmpty(additionalDetails.get("appCreatedDate")) || !Objects.isNull(additionalDetails.get("appCreatedDate"))) {
            Long createdTime = ((Number) additionalDetails.get("appCreatedDate")).longValue();
            Map<String, Object> history = (LinkedHashMap<String, Object>) ((ArrayList) properties.get("history")).get(0);
            String businessService = (String) history.get("businessService");
            Long businessServiceSLA = businessServiceSlaMap.get(businessService);
            Long remainingDays = Long.valueOf(Math.round((businessServiceSLA - (currentDate - createdTime)) / ((double) (24 * 60 * 60 * 1000))));
            log.debug("Application service SLA calculated - businessService: {}, remainingDays: {}", businessService, remainingDays);
            return remainingDays;
        }
        log.debug("Application service SLA could not be calculated - appCreatedDate not found");
        return null;
    }

    public List<String> fetchVehicleStateMap(List<String> inputStatuses, RequestInfo requestInfo, String tenantId,Integer limit,Integer offSet) {
		log.trace("Method invoked: fetchVehicleStateMap - tenantId: {}, statusCount: {}", tenantId, inputStatuses != null ? inputStatuses.size() : 0);
		log.info("Fetching vehicle state map - tenantId: {}, limit: {}, offset: {}", tenantId, limit, offSet);
		VehicleTripSearchCriteria vehicleTripSearchCriteria = new VehicleTripSearchCriteria();
		vehicleTripSearchCriteria.setApplicationStatus(inputStatuses);
		vehicleTripSearchCriteria.setTenantId(tenantId);
		vehicleTripSearchCriteria.setLimit(limit);
		vehicleTripSearchCriteria.setOffset(offSet);
		StringBuilder url = new StringBuilder(config.getFsmHost());
		url.append( config.getFetchApplicationIds());
		log.debug("Calling FSM service to fetch application IDs - URL: {}", url.toString());
		
		Object result = serviceRequestRepository.fetchResult(url, vehicleTripSearchCriteria);
		VehicleCustomResponse response =null;
		try {
			response = mapper.convertValue(result, VehicleCustomResponse.class);
			if(null != response && null != response.getApplicationIdList()) {
				log.info("Vehicle state map fetched - applicationIdCount: {}", response.getApplicationIdList().size());
				return response.getApplicationIdList();
			}
		} catch (IllegalArgumentException e) {
			log.error("Failed to parse vehicle state map response", e);
			throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of ProcessInstance");
		}
		log.warn("Vehicle state map response is null or empty");
		return new ArrayList<>();
	}
    
    /**
	 * @param requiredApplications
	 * @return
	 * Description : Fetch the vehicle_trip_detail by list of reference no.
	 */
	private List<VehicleTripDetail> fetchVehicleStatusForApplication(List<String> requiredApplications,RequestInfo requestInfo, String tenantId) {
		log.trace("Method invoked: fetchVehicleStatusForApplication - tenantId: {}, applicationCount: {}", 
				tenantId, requiredApplications != null ? requiredApplications.size() : 0);
		log.debug("Fetching vehicle status for applications");
		VehicleTripSearchCriteria vehicleTripSearchCriteria = new VehicleTripSearchCriteria();
		vehicleTripSearchCriteria.setApplicationNos(requiredApplications);
		vehicleTripSearchCriteria.setTenantId(tenantId);
		return fetchVehicleTripDetailsByReferenceNo(vehicleTripSearchCriteria,requestInfo);
	}
	
	public List<VehicleTripDetail> fetchVehicleTripDetailsByReferenceNo(VehicleTripSearchCriteria vehicleTripSearchCriteria, RequestInfo requestInfo) {
		log.trace("Method invoked: fetchVehicleTripDetailsByReferenceNo");
		log.info("Fetching vehicle trip details by reference number");
		StringBuilder url = new StringBuilder(config.getVehicleHost());
		url.append( config.getVehicleSearchTripPath());
		log.debug("Calling vehicle service - URL: {}", url.toString());
		Object result = serviceRequestRepository.fetchResult(url, vehicleTripSearchCriteria);
		VehicleTripDetailResponse response =null;
		try {
			response = mapper.convertValue(result, VehicleTripDetailResponse.class);
			if(null != response && null != response.getVehicleTripDetail()) {
				log.info("Vehicle trip details fetched - count: {}", response.getVehicleTripDetail().size());
				return response.getVehicleTripDetail();
			}
		} catch (IllegalArgumentException e) {
			log.error("Failed to parse vehicle trip detail response", e);
			throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of ProcessInstance");
		}
		log.warn("Vehicle trip detail response is null or empty");
		return new ArrayList<>();
	}


	private void populateStatusCountMap(List<HashMap<String, Object>> statusCountMap,
			List<Map<String, Object>> vehicleResponse, BusinessService businessService) {
		log.trace("Method invoked: populateStatusCountMap");
		log.debug("Populating status count map from vehicle response");
		
		if (!CollectionUtils.isEmpty(vehicleResponse) && businessService != null) {
			List<State> appStates = businessService.getStates();
			log.debug("Processing vehicle response - vehicleCount: {}, stateCount: {}", 
					vehicleResponse.size(), appStates.size());

			for (State appState : appStates) {
				
				vehicleResponse.forEach(trip -> {
					
					HashMap<String, Object> vehicleTripStatusMp = new HashMap<>();
					if(trip.get(APPLICATIONSTATUS).equals(appState.getApplicationStatus())) {
						
						vehicleTripStatusMp.put(COUNT, trip.get(COUNT));
						vehicleTripStatusMp.put(APPLICATIONSTATUS, appState.getApplicationStatus());
						vehicleTripStatusMp.put(STATUSID, appState.getUuid());
						vehicleTripStatusMp.put(BUSINESS_SERVICE_PARAM, FSM_VEHICLE_TRIP_MODULE);
					}
					
					if (MapUtils.isNotEmpty(vehicleTripStatusMp))
						statusCountMap.add(vehicleTripStatusMp);
				});
			}
			log.debug("Status count map populated - entries: {}", statusCountMap.size());
		} else {
			log.warn("Vehicle response or business service is null/empty - cannot populate status count map");
		}
	}
    
    private List<Map<String, Object>> fetchVehicleTripResponse(InboxSearchCriteria criteria, RequestInfo requestInfo,List<String> applicationStatus) {
		log.trace("Method invoked: fetchVehicleTripResponse - tenantId: {}, statusCount: {}", 
				criteria.getTenantId(), applicationStatus != null ? applicationStatus.size() : 0);
		log.info("Fetching vehicle trip response - tenantId: {}", criteria.getTenantId());

		VehicleSearchCriteria vehicleTripSearchCriteria = new VehicleSearchCriteria();
		
		vehicleTripSearchCriteria.setApplicationStatus(applicationStatus);

		vehicleTripSearchCriteria.setTenantId(criteria.getTenantId());
		
		log.debug("Fetching application count for vehicle trips");
		List<Map<String, Object>> vehicleResponse = null ;
		VehicleCustomResponse vehicleCustomResponse =  fetchApplicationCount(vehicleTripSearchCriteria, requestInfo);
		if(null != vehicleCustomResponse && null != vehicleCustomResponse.getApplicationStatusCount() ) {
			vehicleResponse =vehicleCustomResponse.getApplicationStatusCount();
			log.info("Vehicle trip response fetched - entryCount: {}", vehicleResponse.size());
		}else {
			log.warn("Vehicle trip response is null or empty");
			vehicleResponse = new ArrayList<Map<String,Object>>();
		}
    	
    	
    	return vehicleResponse;
    }
    
    public VehicleCustomResponse fetchApplicationCount(VehicleSearchCriteria criteria, RequestInfo requestInfo) {
		log.trace("Method invoked: fetchApplicationCount");
		log.info("Fetching application count for vehicle trips");
		StringBuilder url = new StringBuilder(config.getVehicleHost());
		url.append( config.getVehicleApplicationStatusCountPath());
		log.debug("Calling vehicle service for application count - URL: {}", url.toString());
		Object result = serviceRequestRepository.fetchResult(url, criteria);
		VehicleCustomResponse resposne =null;
		try {
			resposne = mapper.convertValue(result, VehicleCustomResponse.class);
			log.debug("Application count response parsed successfully");
		} catch (IllegalArgumentException e) {
			log.error("Failed to parse application count response", e);
			throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of ProcessInstance");
		}
		return resposne;
	}
    
    /*
     * private String fetchUserUUID(String mobileNumber, RequestInfo requestInfo, String tenantId) { StringBuilder uri = new
     * StringBuilder(); uri.append(userHost).append(userSearchEndpoint); Map<String, Object> userSearchRequest = new HashMap<>();
     * userSearchRequest.put("RequestInfo", requestInfo); userSearchRequest.put("tenantId", tenantId);
     * userSearchRequest.put("userType", "CITIZEN"); userSearchRequest.put("userName", mobileNumber); String uuid = ""; try {
     * Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest); if(null != user) { uuid = JsonPath.read(user,
     * "$.user[0].uuid"); }else { log.error("Service returned null while fetching user for username - " + mobileNumber); }
     * }catch(Exception e) { log.error("Exception while fetching user for username - " + mobileNumber);
     * log.error("Exception trace: ", e); } return uuid; }
     */

    private Map<String, String> fetchAppropriateServiceMap(List<String> businessServiceName,String  moduleName) {
        log.trace("Method invoked: fetchAppropriateServiceMap");
        log.debug("Fetching appropriate service map - businessService: {}, module: {}", 
                businessServiceName != null && !businessServiceName.isEmpty() ? businessServiceName.get(0) : null, moduleName);
        
        StringBuilder appropriateKey = new StringBuilder();
        for (String businessServiceKeys : config.getServiceSearchMapping().keySet()) {
            if (businessServiceKeys.contains(businessServiceName.get(0))) {
                appropriateKey.append(businessServiceKeys);
                break;
            }
        }
        if (ObjectUtils.isEmpty(appropriateKey)) {
            log.error("Inbox service not configured for business service: {}", 
                    businessServiceName != null && !businessServiceName.isEmpty() ? businessServiceName.get(0) : null);
            throw new CustomException("EG_INBOX_SEARCH_ERROR",
                    "Inbox service is not configured for the provided business services");
        }
        log.debug("Appropriate service map found - key: {}", appropriateKey.toString());
        //SAN-920: Added check for enabling multiple business services only for FSM module
      		for (String inputBusinessService : businessServiceName) {
      			if (!FSMConstants.FSM_MODULE.equalsIgnoreCase(moduleName)) {
      				if (!appropriateKey.toString().contains(inputBusinessService)) {
      					throw new CustomException("EG_INBOX_SEARCH_ERROR", "Cross module search is NOT allowed.");
      				}
      			}

      		}
        return config.getServiceSearchMapping().get(appropriateKey.toString());
    }

    private JSONArray fetchModuleObjects(HashMap moduleSearchCriteria, List<String> businessServiceName, String tenantId,
            RequestInfo requestInfo, Map<String, String> srvMap) {
        log.trace("Method invoked: fetchModuleObjects");
        log.debug("Fetching module objects - tenantId: {}, businessService: {}", 
                tenantId, businessServiceName != null && !businessServiceName.isEmpty() ? businessServiceName.get(0) : null);
        
        JSONArray resutls = null;
        
        if (CollectionUtils.isEmpty(srvMap) || StringUtils.isEmpty(srvMap.get("searchPath"))) {
            log.error("Search path not configured for business service: {}", businessServiceName);
            throw new CustomException(ErrorConstants.INVALID_MODULE_SEARCH_PATH,
                    "search path not configured for the businessService : " + businessServiceName);
        }
        StringBuilder url = new StringBuilder(srvMap.get("searchPath"));
        url.append("?tenantId=").append(tenantId);
       
        Set<String> searchParams = moduleSearchCriteria.keySet();
        
		searchParams.forEach((param) -> {

			if (!param.equalsIgnoreCase("tenantId")) {

				if (moduleSearchCriteria.get(param) instanceof Collection) {
					url.append("&").append(param).append("=");
					url.append(StringUtils
							.arrayToDelimitedString(((Collection<?>) moduleSearchCriteria.get(param)).toArray(), ","));
				} else if(param.equalsIgnoreCase("appStatus")){
					url.append("&").append("applicationStatus").append("=")
					.append(moduleSearchCriteria.get(param).toString());
				} else if(param.equalsIgnoreCase("consumerNo")){
					url.append("&").append("connectionNumber").append("=")
					.append(moduleSearchCriteria.get(param).toString());
				} else if(null != moduleSearchCriteria.get(param)) {
					url.append("&").append(param).append("=").append(moduleSearchCriteria.get(param).toString());
				}
			}
		});
		
		log.debug("Fetching module objects - URL: {}", url.toString());
		
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        log.debug("Calling service request repository");
        Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
        
        LinkedHashMap responseMap;
        try {
            log.debug("Parsing response from service request repository");
            responseMap = mapper.convertValue(result, LinkedHashMap.class);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse response from service request repository", e);
            throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of ProcessInstance Count");
        }
        
        
        JSONObject jsonObject = new JSONObject(responseMap);
        
        try {
            resutls = (JSONArray) jsonObject.getJSONArray(srvMap.get("dataRoot"));
            log.debug("Module objects retrieved - count: {}", resutls != null ? resutls.length() : 0);
        } catch (Exception e) {
            log.error("Failed to extract data from dataroot: {}", srvMap.get("dataRoot"), e);
            throw new CustomException(ErrorConstants.INVALID_MODULE_DATA,
                    " search api could not find data in dataroot " + srvMap.get("dataRoot"));
        }
        
        
        return resutls;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        if (object == null) {
            return map;
        }
        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

	
	private Map<String, String> fetchAppropriateServiceSearchMap(String businessServiceName, String moduleName) {
		StringBuilder appropriateKey = new StringBuilder();
		for (String businessServiceKeys : config.getBsServiceSearchMapping().keySet()) {
			if (businessServiceKeys.contains(businessServiceName)) {
				appropriateKey.append(businessServiceKeys);
				break;
			}
		}
		if (ObjectUtils.isEmpty(appropriateKey)) {
			throw new CustomException("EG_INBOX_SEARCH_ERROR",
					"Inbox service is not configured for the provided business services");
		}
		return config.getBsServiceSearchMapping().get(appropriateKey.toString());
	}

	private JSONArray fetchModuleSearchObjects(HashMap moduleSearchCriteria, List<String> businessServiceName,
			String tenantId, RequestInfo requestInfo, Map<String, String> srvMap) {
		JSONArray results = null;

		if (CollectionUtils.isEmpty(srvMap) || StringUtils.isEmpty(srvMap.get("searchPath"))) {
			throw new CustomException(ErrorConstants.INVALID_MODULE_SEARCH_PATH,
					"search path not configured for the businessService : " + businessServiceName);
		}
		StringBuilder url = new StringBuilder(srvMap.get("searchPath"));
		url.append("?tenantId=").append(tenantId);

		Set<String> searchParams = moduleSearchCriteria.keySet();

		searchParams.forEach((param) -> {

			if (!param.equalsIgnoreCase("tenantId")) {
				if (param.equalsIgnoreCase("limit"))
				    return;
				if (moduleSearchCriteria.get(param) instanceof Collection) {
					url.append("&").append(param).append("=");
					url.append(StringUtils
							.arrayToDelimitedString(((Collection<?>) moduleSearchCriteria.get(param)).toArray(), ","));
				} else if (null != moduleSearchCriteria.get(param)) {
					url.append("&").append(param).append("=").append(moduleSearchCriteria.get(param).toString());
				}
			}
		});

		log.debug("Fetching module search objects - URL: {}", url.toString());

		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);

		LinkedHashMap responseMap;
		try {
			responseMap = mapper.convertValue(result, LinkedHashMap.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of ProcessInstance Count");
		}

		JSONObject jsonObject = new JSONObject(responseMap);

		try {
			results = (JSONArray) jsonObject.getJSONArray(srvMap.get("dataRoot"));
		} catch (Exception e) {
			throw new CustomException(ErrorConstants.INVALID_MODULE_DATA, " search api could not find data in serviceMap " + srvMap.get("dataRoot"));
		}

		return results;
	}

	/**
	 * Converts module name from BS_WS/BS_SW to their module name equivalents if needed
	 * @param processCriteria Process search criteria
	 * @return flag indicating which conversion was done (1 for BS_WS, 2 for BS_SW, 0 for none)
	 */
	private Integer convertModuleNameIfNeeded(ProcessInstanceSearchCriteria processCriteria) {
		log.trace("Method invoked: convertModuleNameIfNeeded");
		log.debug("Checking if module name conversion needed - moduleName: {}", processCriteria.getModuleName());
		Integer flag = 0;
		if (processCriteria.getModuleName().equalsIgnoreCase(BS_WS)) {
			flag = 1;
			processCriteria.setModuleName(BS_WS_MODULENAME);
			log.debug("Module name converted from BS_WS to BS_WS_MODULENAME");
		} else if (processCriteria.getModuleName().equalsIgnoreCase(BS_SW)) {
			flag = 2;
			processCriteria.setModuleName(BS_SW_MODULENAME);
			log.debug("Module name converted from BS_SW to BS_SW_MODULENAME");
		} else {
			log.debug("No module name conversion needed");
		}
		return flag;
	}

	/**
	 * Gets total count from workflow service if applicable
	 */
	private Integer getTotalCount(InboxSearchCriteria criteria, RequestInfo requestInfo, 
			ProcessInstanceSearchCriteria processCriteria) {
		log.trace("Method invoked: getTotalCount");
		log.debug("Getting total count - moduleName: {}", processCriteria.getModuleName());
		Integer totalCount = 0;
		if (!(processCriteria.getModuleName().equals(SW) || processCriteria.getModuleName().equals(WS))) {
			log.debug("Fetching process count from workflow service");
			totalCount = workflowService.getProcessCount(criteria.getTenantId(), requestInfo, processCriteria);
			log.debug("Process count retrieved: {}", totalCount);
		} else {
			log.debug("Skipping total count fetch for WS/SW module");
		}
		return totalCount;
	}

	/**
	 * Fetches DSO ID if the user has FSM_DSO role
	 */
	private String fetchDsoIdIfNeeded(InboxSearchCriteria criteria, RequestInfo requestInfo) {
		log.trace("Method invoked: fetchDsoIdIfNeeded");
		String dsoId = null;
		if (requestInfo.getUserInfo().getRoles().get(0).getCode().equals(FSMConstants.FSM_DSO)) {
			log.debug("User has FSM_DSO role, fetching DSO ID");
			Map<String, Object> searcherRequestForDSO = new HashMap<>();
			Map<String, Object> searchCriteriaForDSO = new HashMap<>();
			searchCriteriaForDSO.put(TENANT_ID_PARAM, criteria.getTenantId());
			searchCriteriaForDSO.put(FSMConstants.OWNER_ID, requestInfo.getUserInfo().getUuid());
			searcherRequestForDSO.put(REQUESTINFO_PARAM, requestInfo);
			searcherRequestForDSO.put(SEARCH_CRITERIA_PARAM, searchCriteriaForDSO);
			StringBuilder uri = new StringBuilder();
			uri.append(config.getSearcherHost()).append(config.getFsmInboxDSoIDEndpoint());
			log.debug("Fetching DSO ID from searcher - URL: {}", uri.toString());

			Object resultForDsoId = restTemplate.postForObject(uri.toString(), searcherRequestForDSO, Map.class);
			dsoId = JsonPath.read(resultForDsoId, "$.vendor[0].id");
			log.debug("DSO ID fetched: {}", dsoId);
		} else {
			log.debug("User does not have FSM_DSO role, skipping DSO ID fetch");
		}
		return dsoId;
	}

	/**
	 * Handles assignee filtering
	 */
	private StringBuilder handleAssignee(ProcessInstanceSearchCriteria processCriteria) {
		log.trace("Method invoked: handleAssignee");
		StringBuilder assigneeUuid = new StringBuilder();
		if (!ObjectUtils.isEmpty(processCriteria.getAssignee())) {
			log.debug("Assignee filter present: {}", processCriteria.getAssignee());
			assigneeUuid = assigneeUuid.append(processCriteria.getAssignee());
			processCriteria.setStatus(null);
			log.debug("Status cleared due to assignee filter");
		} else {
			log.debug("No assignee filter present");
		}
		return assigneeUuid;
	}

	/**
	 * Initializes module search criteria with tenant, offset, and limit
	 */
	private void initializeModuleSearchCriteria(InboxSearchCriteria criteria, HashMap moduleSearchCriteria) {
		log.trace("Method invoked: initializeModuleSearchCriteria");
		log.debug("Initializing module search criteria - tenantId: {}, offset: {}, limit: {}", 
				criteria.getTenantId(), criteria.getOffset(), criteria.getLimit());
		moduleSearchCriteria.put("tenantId", criteria.getTenantId());
		moduleSearchCriteria.put("offset", criteria.getOffset());
		moduleSearchCriteria.put("limit", criteria.getLimit());
		log.debug("Module search criteria initialized");
	}

	/**
	 * Builds list of business services and populates SLA map
	 */
	private List<BusinessService> buildBusinessServicesList(InboxSearchCriteria criteria, RequestInfo requestInfo,
			List<String> businessServiceName, Map<String, Long> businessServiceSlaMap) {
		log.trace("Method invoked: buildBusinessServicesList");
		log.debug("Building business services list - count: {}", businessServiceName.size());
		List<BusinessService> bussinessSrvs = new ArrayList<BusinessService>();
		for (String businessSrv : businessServiceName) {
			log.debug("Fetching business service: {}", businessSrv);
			BusinessService businessService = workflowService.getBusinessService(criteria.getTenantId(), requestInfo,
					businessSrv);
			bussinessSrvs.add(businessService);
			businessServiceSlaMap.put(businessService.getBusinessService(), businessService.getBusinessServiceSla());
			log.debug("Business service added - name: {}, SLA: {}", 
					businessService.getBusinessService(), businessService.getBusinessServiceSla());
		}
		log.debug("Business services list built - total: {}", bussinessSrvs.size());
		return bussinessSrvs;
	}

	/**
	 * Gets application status parameter from service map, defaults to "applicationStatus"
	 */
	private String getApplicationStatusParam(Map<String, String> srvMap) {
		log.trace("Method invoked: getApplicationStatusParam");
		String applicationStatusParam = srvMap.get("applsStatusParam");
		if (StringUtils.isEmpty(applicationStatusParam)) {
			applicationStatusParam = "applicationStatus";
			log.debug("Application status param not found in service map, using default: {}", applicationStatusParam);
		} else {
			log.debug("Application status param found: {}", applicationStatusParam);
		}
		return applicationStatusParam;
	}

	/**
	 * Applies status filter to module search criteria
	 */
	private void applyStatusFilterToModuleSearchCriteria(ProcessInstanceSearchCriteria processCriteria,
			HashMap<String, String> StatusIdNameMap, HashMap moduleSearchCriteria, String applicationStatusParam) {
		log.trace("Method invoked: applyStatusFilterToModuleSearchCriteria");
		if (StatusIdNameMap.values().size() > 0) {
			if (!CollectionUtils.isEmpty(processCriteria.getStatus())) {
				log.debug("Applying filtered statuses to module search criteria - statusCount: {}", 
						processCriteria.getStatus().size());
				List<String> statuses = new ArrayList<String>();
				processCriteria.getStatus().forEach(status -> {
					statuses.add(StatusIdNameMap.get(status));
				});
				moduleSearchCriteria.put(applicationStatusParam,
						StringUtils.arrayToDelimitedString(statuses.toArray(), ","));
				log.debug("Filtered statuses applied: {}", moduleSearchCriteria.get(applicationStatusParam));
			} else {
				log.debug("No status filter, applying all actionable statuses");
				moduleSearchCriteria.put(applicationStatusParam,
						StringUtils.arrayToDelimitedString(StatusIdNameMap.values().toArray(), ","));
				log.debug("All actionable statuses applied: {}", moduleSearchCriteria.get(applicationStatusParam));
			}
		} else {
			log.debug("No actionable statuses found, skipping status filter application");
		}
	}

	/**
	 * Checks if the request is for BPA citizen
	 */
	private boolean isBpaCitizen(ProcessInstanceSearchCriteria processCriteria, List<String> roles) {
		log.trace("Method invoked: isBpaCitizen");
		boolean result = processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& processCriteria.getModuleName().equals(BPA) && roles.contains(BpaConstants.CITIZEN);
		log.debug("BPA citizen check - result: {}", result);
		return result;
	}

	/**
	 * Checks if the request is for BPA module
	 */
	private boolean isBpaModule(ProcessInstanceSearchCriteria processCriteria) {
		log.trace("Method invoked: isBpaModule");
		boolean result = processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& processCriteria.getModuleName().equals(BPA);
		log.debug("BPA module check - result: {}", result);
		return result;
	}

	/**
	 * Handles BPA citizen status count aggregation across multiple tenants
	 */
	private List<HashMap<String, Object>> handleBpaCitizenStatusCount(InboxSearchCriteria criteria,
			ProcessInstanceSearchCriteria processCriteria, HashMap<String, String> StatusIdNameMap,
			HashMap moduleSearchCriteria, RequestInfo requestInfo,
			Map<String, List<String>> tenantAndApplnNumbersMap) {
		log.trace("Method invoked: handleBpaCitizenStatusCount");
		log.debug("Handling BPA citizen status count aggregation across multiple tenants");
		List<HashMap<String, Object>> bpaCitizenStatusCountMap = new ArrayList<HashMap<String, Object>>();
		List<Map<String, String>> tenantWiseApplns = bpaInboxFilterService
				.fetchTenantWiseApplicationNumbersForCitizenInboxFromSearcher(criteria, StatusIdNameMap, requestInfo);
		log.debug("Fetched tenant-wise applications - count: {}", tenantWiseApplns.size());
		if (moduleSearchCriteria == null || moduleSearchCriteria.isEmpty()) {
			log.debug("Module search criteria is empty, initializing with mobile number");
			moduleSearchCriteria = new HashMap<>();
			moduleSearchCriteria.put(MOBILE_NUMBER_PARAM, requestInfo.getUserInfo().getMobileNumber());
			criteria.setModuleSearchCriteria(moduleSearchCriteria);
		}
		for (Map<String, String> tenantAppln : tenantWiseApplns) {
			String tenant = tenantAppln.get("tenantid");
			String applnNo = tenantAppln.get("applicationno");
			if (tenantAndApplnNumbersMap.containsKey(tenant)) {
				List<String> applnNos = tenantAndApplnNumbersMap.get(tenant);
				applnNos.add(applnNo);
				tenantAndApplnNumbersMap.put(tenant, applnNos);
			} else {
				List<String> l = new ArrayList<>();
				l.add(applnNo);
				tenantAndApplnNumbersMap.put(tenant, l);
			}
		}
		log.debug("Tenant-wise application numbers mapped - tenantCount: {}", tenantAndApplnNumbersMap.size());
		String inputTenantID = processCriteria.getTenantId();
		List<String> inputBusinessIds = processCriteria.getBusinessIds();
		List<String> inputStatus = processCriteria.getStatus();
		if (!StatusIdNameMap.isEmpty())
			processCriteria.setStatus(
					StatusIdNameMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
		for (Map.Entry<String, List<String>> t : tenantAndApplnNumbersMap.entrySet()) {
			log.debug("Processing status count for tenant: {}, applicationCount: {}", t.getKey(), t.getValue().size());
			processCriteria.setTenantId(t.getKey());
			processCriteria.setBusinessIds(t.getValue());
			List<HashMap<String, Object>> tenantWiseStatusCount = workflowService.getProcessStatusCount(requestInfo,
					processCriteria);
			if (bpaCitizenStatusCountMap.isEmpty()) {
				bpaCitizenStatusCountMap.addAll(tenantWiseStatusCount);
				log.debug("Initial status count map populated from tenant: {}", t.getKey());
			} else {
				for (HashMap<String, Object> tenantStatusMap : tenantWiseStatusCount) {
					for (HashMap<String, Object> bpaStatusMap : bpaCitizenStatusCountMap) {
						if (bpaStatusMap.containsValue(tenantStatusMap.get(STATUS_ID))) {
							bpaStatusMap.put(COUNT,
									Integer.parseInt(String.valueOf(bpaStatusMap.get(COUNT)))
											+ Integer.parseInt(String.valueOf(tenantStatusMap.get(COUNT))));
						}
					}
				}
				log.debug("Status counts aggregated for tenant: {}", t.getKey());
			}
		}
		processCriteria.setTenantId(inputTenantID);
		processCriteria.setBusinessIds(inputBusinessIds);
		processCriteria.setStatus(inputStatus);
		log.debug("BPA citizen status count aggregation completed - statusCountMapSize: {}", bpaCitizenStatusCountMap.size());
		return bpaCitizenStatusCountMap;
	}

	/**
	 * Handles BPA locality-based filtering for status counts
	 */
	private List<HashMap<String, Object>> handleBpaLocalityFiltering(InboxSearchCriteria criteria,
			ProcessInstanceSearchCriteria processCriteria, HashMap<String, String> StatusIdNameMap,
			HashMap moduleSearchCriteria, List<HashMap<String, Object>> statusCountMap, List<String> inputStatuses,
			RequestInfo requestInfo) {
		log.trace("Method invoked: handleBpaLocalityFiltering");
		if (moduleSearchCriteria.get(LOCALITY_PARAM) != null) {
			log.debug("Locality filter present, applying locality-based status count filtering");
			for (Map<String, Object> statusWiseCount : statusCountMap) {
				List<String> statusList = new ArrayList<>();
				statusList.add(String.valueOf(statusWiseCount.get(STATUS_ID)));
				criteria.getProcessSearchCriteria().setStatus(statusList);
				Integer count = bpaInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap,
						requestInfo);
				if (count == 0) {
					log.debug("Status count is 0 for statusId: {}, clearing entry", statusWiseCount.get(STATUS_ID));
					statusWiseCount.clear();
				} else {
					log.debug("Status count updated for statusId: {}, count: {}", statusWiseCount.get(STATUS_ID), count);
					statusWiseCount.put(COUNT, count);
				}
			}
			criteria.getProcessSearchCriteria().setStatus(inputStatuses);
			log.debug("Locality-based filtering completed");
		} else {
			log.debug("No locality filter present, skipping locality-based filtering");
		}
		if (!statusCountMap.isEmpty()) {
			List<HashMap<String, Object>> bpaInboxStatusCountMap = new ArrayList<>();
			for (HashMap<String, Object> bpaLoclalityStatusCount : statusCountMap) {
				if (!bpaLoclalityStatusCount.isEmpty())
					bpaInboxStatusCountMap.add(bpaLoclalityStatusCount);
			}
			statusCountMap = bpaInboxStatusCountMap;
			log.debug("Filtered status count map - size: {}", statusCountMap.size());
		}
		return statusCountMap;
	}

	/**
	 * Result class for module filter operations
	 */
	/**
	 * Applies module-specific filters (PT, TL, BPA, NOC, WS/SW, BS_WS, BS_SW)
	 */
	private Integer applyModuleSpecificFilters(InboxSearchCriteria criteria,
			ProcessInstanceSearchCriteria processCriteria, HashMap<String, String> StatusIdNameMap,
			RequestInfo requestInfo, HashMap moduleSearchCriteria, Map<String, Long> businessServiceSlaMap,
			Integer flag, String originalModuleName, List<Map<String, Object>> result, List<String> businessKeys,
			Boolean[] isSearchResultEmptyRef) {
		log.trace("Method invoked: applyModuleSpecificFilters");
		log.debug("Applying module-specific filters - moduleName: {}", processCriteria.getModuleName());
		Integer totalCount = null;

		// PT module filtering
		if (!ObjectUtils.isEmpty(processCriteria.getModuleName()) && processCriteria.getModuleName().equals(PT)) {
			log.debug("Applying PT module filter");
			totalCount = ptInboxFilterService.fetchAcknowledgementIdsCountFromSearcher(criteria, StatusIdNameMap,
					requestInfo);
			List<String> acknowledgementNumbers = ptInboxFilterService.fetchAcknowledgementIdsFromSearcher(criteria,
					StatusIdNameMap, requestInfo);
			if (!CollectionUtils.isEmpty(acknowledgementNumbers)) {
				log.debug("PT acknowledgement numbers fetched - count: {}", acknowledgementNumbers.size());
				moduleSearchCriteria.put(ACKNOWLEDGEMENT_IDS_PARAM, acknowledgementNumbers);
				businessKeys.addAll(acknowledgementNumbers);
				moduleSearchCriteria.remove(LOCALITY_PARAM);
				moduleSearchCriteria.remove(OFFSET_PARAM);
			} else {
				log.debug("PT acknowledgement numbers empty, marking search result as empty");
				isSearchResultEmptyRef[0] = true;
			}
		}

		// TL/BPAREG module filtering
		if (!ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& (processCriteria.getModuleName().equals(TL) || processCriteria.getModuleName().equals(BPAREG))) {
			log.debug("Applying TL/BPAREG module filter");
			totalCount = tlInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap, requestInfo);
			List<String> applicationNumbers = tlInboxFilterService.fetchApplicationNumbersFromSearcher(criteria,
					StatusIdNameMap, requestInfo);
			if (!CollectionUtils.isEmpty(applicationNumbers)) {
				log.debug("TL/BPAREG application numbers fetched - count: {}", applicationNumbers.size());
				moduleSearchCriteria.put(APPLICATION_NUMBER_PARAM, applicationNumbers);
				businessKeys.addAll(applicationNumbers);
				moduleSearchCriteria.remove(TLConstants.STATUS_PARAM);
				moduleSearchCriteria.remove(LOCALITY_PARAM);
				moduleSearchCriteria.remove(OFFSET_PARAM);
			} else {
				log.debug("TL/BPAREG application numbers empty, marking search result as empty");
				isSearchResultEmptyRef[0] = true;
			}
		}

		// BPA module filtering
		if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& processCriteria.getModuleName().equals(BPA)) {
			log.debug("Applying BPA module filter");
			totalCount = bpaInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap, requestInfo);
			List<String> applicationNumbers = bpaInboxFilterService.fetchApplicationNumbersFromSearcher(criteria,
					StatusIdNameMap, requestInfo);
			if (!CollectionUtils.isEmpty(applicationNumbers)) {
				log.debug("BPA application numbers fetched - count: {}", applicationNumbers.size());
				moduleSearchCriteria.put(BPA_APPLICATION_NUMBER_PARAM, applicationNumbers);
				businessKeys.addAll(applicationNumbers);
				moduleSearchCriteria.remove(STATUS_PARAM);
				moduleSearchCriteria.remove(MOBILE_NUMBER_PARAM);
				moduleSearchCriteria.remove(LOCALITY_PARAM);
				moduleSearchCriteria.remove(OFFSET_PARAM);
			} else {
				log.debug("BPA application numbers empty, marking search result as empty");
				isSearchResultEmptyRef[0] = true;
			}
		}

		// NOC module filtering
		if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& processCriteria.getModuleName().equals(NOC)) {
			log.debug("Applying NOC module filter");
			totalCount = nocInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap, requestInfo);
			List<String> applicationNumbers = nocInboxFilterService.fetchApplicationNumbersFromSearcher(criteria,
					StatusIdNameMap, requestInfo);
			if (!CollectionUtils.isEmpty(applicationNumbers)) {
				log.debug("NOC application numbers fetched - count: {}", applicationNumbers.size());
				moduleSearchCriteria.put(NOC_APPLICATION_NUMBER_PARAM, applicationNumbers);
				businessKeys.addAll(applicationNumbers);
				moduleSearchCriteria.remove(STATUS_PARAM);
				moduleSearchCriteria.remove(MOBILE_NUMBER_PARAM);
				moduleSearchCriteria.remove(LOCALITY_PARAM);
				moduleSearchCriteria.remove(OFFSET_PARAM);
			} else {
				log.debug("NOC application numbers empty, marking search result as empty");
				isSearchResultEmptyRef[0] = true;
			}
		}

		// WS/SW ElasticSearch filtering
		if (!ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& (processCriteria.getModuleName().equals(WS) || processCriteria.getModuleName().equals(SW))) {
			log.debug("Applying WS/SW ElasticSearch filter");
			totalCount = fetchElasticSearchData(criteria, businessServiceSlaMap, result);
		}

		// BS_WS (Bill Amendment WS) filtering
		if (!ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& processCriteria.getModuleName().equals(BS_WS_MODULENAME) && flag == 1) {
			log.debug("Applying BS_WS (Bill Amendment WS) filter");
			processCriteria.setModuleName(BS_WS);
			totalCount = billInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap, requestInfo);
			Map<String, List<String>> map = billInboxFilterService.fetchConsumerNumbersFromSearcher(criteria,
					StatusIdNameMap, requestInfo);
			List<String> consumerCodes = map.get("consumerCodes");
			List<String> amendmentIds = map.get("amendmentIds");
			if (!CollectionUtils.isEmpty(consumerCodes)) {
				log.debug("BS_WS consumer codes fetched - count: {}", consumerCodes.size());
				moduleSearchCriteria.put(BS_CONSUMER_NO_PARAM, consumerCodes);
				businessKeys.addAll(amendmentIds);
				moduleSearchCriteria.put(BS_BUSINESS_SERVICE_PARAM, "WS");
				moduleSearchCriteria.remove(MOBILE_NUMBER_PARAM);
				moduleSearchCriteria.remove(ASSIGNEE_PARAM);
				moduleSearchCriteria.remove(LOCALITY_PARAM);
				moduleSearchCriteria.remove(OFFSET_PARAM);
			} else {
				log.debug("BS_WS consumer codes empty, marking search result as empty");
				isSearchResultEmptyRef[0] = true;
			}
			moduleSearchCriteria.put("isPropertyDetailsRequired", true);
			processCriteria.setModuleName(BS_WS_MODULENAME);
		}

		// BS_SW (Bill Amendment SW) filtering
		if (!ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& processCriteria.getModuleName().equals(BS_SW_MODULENAME) && flag == 2) {
			log.debug("Applying BS_SW (Bill Amendment SW) filter");
			processCriteria.setModuleName(BS_SW);
			totalCount = billInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap, requestInfo);
			Map<String, List<String>> map = billInboxFilterService.fetchConsumerNumbersFromSearcher(criteria,
					StatusIdNameMap, requestInfo);
			List<String> consumerCodes = map.get("consumerCodes");
			List<String> amendmentIds = map.get("amendmentIds");
			if (!CollectionUtils.isEmpty(consumerCodes)) {
				log.debug("BS_SW consumer codes fetched - count: {}", consumerCodes.size());
				moduleSearchCriteria.put(BS_CONSUMER_NO_PARAM, consumerCodes);
				businessKeys.addAll(amendmentIds);
				moduleSearchCriteria.put(BS_BUSINESS_SERVICE_PARAM, "SW");
				moduleSearchCriteria.remove(MOBILE_NUMBER_PARAM);
				moduleSearchCriteria.remove(ASSIGNEE_PARAM);
				moduleSearchCriteria.remove(LOCALITY_PARAM);
				moduleSearchCriteria.remove(OFFSET_PARAM);
			} else {
				log.debug("BS_SW consumer codes empty, marking search result as empty");
				isSearchResultEmptyRef[0] = true;
			}
			moduleSearchCriteria.put("isPropertyDetailsRequired", true);
			processCriteria.setModuleName(BS_SW_MODULENAME);
		}

		log.debug("Module-specific filters applied - totalCount: {}, isSearchResultEmpty: {}", 
				totalCount, isSearchResultEmptyRef[0]);
		return totalCount;
	}

	/**
	 * Fetches data from ElasticSearch for WS and SW modules
	 */
	private Integer fetchElasticSearchData(InboxSearchCriteria criteria, Map<String, Long> businessServiceSlaMap,
			List<Map<String, Object>> result) {
		Integer totalCount = 0;
		try {
			JsonNode responseNode = new ObjectMapper()
					.convertValue(elasticSearchRepository.elasticSearchApplications(criteria, (List<String>) null),
							JsonNode.class);
			JsonNode output = responseNode.get(ELASTICSEARCH_HIT_KEY).get(ELASTICSEARCH_HIT_KEY);
			totalCount = responseNode.get(ELASTICSEARCH_HIT_KEY).get("total").intValue();

			if (!isNull(output) && output.isArray()) {
				for (JsonNode objectnode : output) {
					Map<String, Object> data = new HashMap<>();
					data.put("Data", objectnode.get("_source").get("Data"));
					Long applicationServiceSla = getApplicationServiceSla(businessServiceSlaMap, data.get("Data"));
					data.put("serviceSLA", applicationServiceSla);
					result.add(data);
				}
			}
		} catch (HttpClientErrorException e) {
			log.error("Client error while searching ElasticSearch - statusCode: {}, message: {}", e.getStatusCode(),
					e.getMessage(), e);
			throw new CustomException("ELASTICSEARCH_ERROR", "client error while searching ES : " + e.getMessage());
		}
		return totalCount;
	}

	/**
	 * Fetches business objects from module search
	 */
	private JSONArray fetchBusinessObjects(HashMap moduleSearchCriteria, List<String> businessServiceName,
			String tenantId, RequestInfo requestInfo, Map<String, String> srvMap,
			ProcessInstanceSearchCriteria processCriteria, Boolean isSearchResultEmpty) {
		log.trace("Method invoked: fetchBusinessObjects");
		log.debug("Fetching business objects - isSearchResultEmpty: {}, moduleName: {}", 
				isSearchResultEmpty, processCriteria.getModuleName());
		JSONArray businessObjects = new JSONArray();
		if (!isSearchResultEmpty
				&& !(processCriteria.getModuleName().equals(SW) || processCriteria.getModuleName().equals(WS))) {
			businessObjects = fetchModuleObjects(moduleSearchCriteria, businessServiceName, tenantId, requestInfo, srvMap);
			log.debug("Business objects fetched - count: {}", businessObjects.length());
		} else {
			log.debug("Skipping business objects fetch - isSearchResultEmpty: {}, moduleName: {}", 
					isSearchResultEmpty, processCriteria.getModuleName());
		}
		return businessObjects;
	}

	/**
	 * Builds business map from business objects
	 */
	private Map<String, Object> buildBusinessMap(JSONArray businessObjects, String businessIdParam) {
		log.trace("Method invoked: buildBusinessMap");
		log.debug("Building business map from business objects - count: {}, businessIdParam: {}", 
				businessObjects.length(), businessIdParam);
		Map<String, Object> businessMap = StreamSupport.stream(businessObjects.spliterator(), false)
				.collect(Collectors.toMap(s1 -> ((JSONObject) s1).get(businessIdParam).toString(), s1 -> s1,
						(e1, e2) -> e1, LinkedHashMap::new));
		log.debug("Business map built - size: {}", businessMap.size());
		return businessMap;
	}

	/**
	 * Fetches service search objects for bill amendments
	 */
	private Map<String, Object> fetchServiceSearchObjects(InboxSearchCriteria criteria,
			ProcessInstanceSearchCriteria processCriteria, HashMap moduleSearchCriteria,
			List<String> businessServiceName, RequestInfo requestInfo, JSONArray businessObjects,
			Boolean isBusinessServiceWSOrSW, Boolean isSearchResultEmpty, String originalModuleName) {
		log.trace("Method invoked: fetchServiceSearchObjects");
		log.debug("Fetching service search objects - isBusinessServiceWSOrSW: {}, businessObjectsCount: {}", 
				isBusinessServiceWSOrSW, businessObjects != null ? businessObjects.length() : 0);
		Map<String, Object> serviceSearchMap = new LinkedHashMap<>();
		if (businessObjects != null && businessObjects.length() > 0 && isBusinessServiceWSOrSW) {
			log.debug("Fetching service search objects for bill amendments");
			String businessService = moduleSearchCriteria.get(BS_BUSINESS_SERVICE_PARAM).toString();
			Map<String, String> srvSearchMap = fetchAppropriateServiceSearchMap(businessService, originalModuleName);
			if (!isSearchResultEmpty && (processCriteria.getModuleName().equalsIgnoreCase(BS_WS_MODULENAME)
					|| processCriteria.getModuleName().equalsIgnoreCase(BS_SW_MODULENAME))) {
				moduleSearchCriteria.put(srvSearchMap.get("consumerCodeParam"),
						moduleSearchCriteria.get(BS_CONSUMER_NO_PARAM));
				moduleSearchCriteria.remove(BS_CONSUMER_NO_PARAM);
				moduleSearchCriteria.remove(BS_BUSINESS_SERVICE_PARAM);
				moduleSearchCriteria.remove(BS_APPLICATION_NUMBER_PARAM);
				moduleSearchCriteria.remove("status");
				moduleSearchCriteria.put("searchType", "CONNECTION");
				JSONArray serviceSearchObject = fetchModuleSearchObjects(moduleSearchCriteria, businessServiceName,
						criteria.getTenantId(), requestInfo, srvSearchMap);
				moduleSearchCriteria.remove("searchType");
				moduleSearchCriteria.put(BS_BUSINESS_SERVICE_PARAM, businessService);
				serviceSearchMap = StreamSupport.stream(serviceSearchObject.spliterator(), false)
						.collect(Collectors.toMap(s1 -> ((JSONObject) s1).get("connectionNo").toString(), s1 -> s1,
								(e1, e2) -> e1, LinkedHashMap::new));
				log.debug("Service search map built - size: {}", serviceSearchMap.size());
			}
		} else {
			log.debug("Skipping service search objects fetch");
		}
		return serviceSearchMap;
	}

	/**
	 * Fetches process instances, handling BPA citizen multi-tenant scenario
	 */
	private ProcessInstanceResponse fetchProcessInstances(InboxSearchCriteria criteria,
			ProcessInstanceSearchCriteria processCriteria, RequestInfo requestInfo, ArrayList businessIds,
			Map<String, List<String>> tenantAndApplnNumbersMap, List<String> roles) {
		log.trace("Method invoked: fetchProcessInstances");
		log.debug("Fetching process instances - businessIdsCount: {}, moduleName: {}", 
				businessIds.size(), processCriteria.getModuleName());
		if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
				&& processCriteria.getModuleName().equals(BPA) && roles.contains(BpaConstants.CITIZEN)) {
			log.debug("BPA citizen multi-tenant scenario detected, fetching process instances per tenant");
			Map<String, List<String>> tenantAndApplnNoForProcessInstance = new HashMap<>();
			for (Object businessId : businessIds) {
				for (Map.Entry<String, List<String>> tenantAppln : tenantAndApplnNumbersMap.entrySet()) {
					String tenantId = tenantAppln.getKey();
					if (tenantAppln.getValue().contains(businessId)
							&& tenantAndApplnNoForProcessInstance.containsKey(tenantId)) {
						List<String> applnNos = tenantAndApplnNoForProcessInstance.get(tenantId);
						applnNos.add(String.valueOf(businessId));
						tenantAndApplnNoForProcessInstance.put(tenantId, applnNos);
					} else if (tenantAppln.getValue().contains(businessId)) {
						List<String> businesIds = new ArrayList<>();
						businesIds.add(String.valueOf(businessId));
						tenantAndApplnNoForProcessInstance.put(tenantId, businesIds);
					}
				}
			}
			ProcessInstanceResponse processInstanceRes = new ProcessInstanceResponse();
			for (Map.Entry<String, List<String>> appln : tenantAndApplnNoForProcessInstance.entrySet()) {
				log.debug("Fetching process instances for tenant: {}, businessIdsCount: {}", 
						appln.getKey(), appln.getValue().size());
				processCriteria.setTenantId(appln.getKey());
				processCriteria.setBusinessIds(appln.getValue());
				ProcessInstanceResponse processInstance = workflowService.getProcessInstance(processCriteria,
						requestInfo);
				processInstanceRes.setResponseInfo(processInstance.getResponseInfo());
				if (processInstanceRes.getProcessInstances() == null)
					processInstanceRes.setProcessInstances(processInstance.getProcessInstances());
				else
					processInstanceRes.getProcessInstances().addAll(processInstance.getProcessInstances());
				log.debug("Process instances fetched for tenant: {} - count: {}", 
						appln.getKey(), processInstance.getProcessInstances() != null ? processInstance.getProcessInstances().size() : 0);
			}
			log.debug("BPA citizen process instances fetched - totalCount: {}", 
					processInstanceRes.getProcessInstances() != null ? processInstanceRes.getProcessInstances().size() : 0);
			return processInstanceRes;
		} else {
			log.debug("Fetching process instances for standard scenario");
			ProcessInstanceResponse response = workflowService.getProcessInstance(processCriteria, requestInfo);
			log.debug("Process instances fetched - count: {}", 
					response.getProcessInstances() != null ? response.getProcessInstances().size() : 0);
			return response;
		}
	}

	/**
	 * Builds inbox items from business objects and process instances
	 */
	private void buildInboxItems(List<Inbox> inboxes, List<Map<String, Object>> result,
			Map<String, Object> businessMap, List<String> businessKeys,
			Map<String, ProcessInstance> processInstanceMap, Map<String, Object> serviceSearchMap,
			Boolean isBusinessServiceWSOrSW, String originalModuleName) {
		log.trace("Method invoked: buildInboxItems");
		log.debug("Building inbox items - businessMapSize: {}, processInstanceMapSize: {}, businessKeysSize: {}", 
				businessMap != null ? businessMap.size() : 0, 
				processInstanceMap != null ? processInstanceMap.size() : 0, 
				businessKeys != null ? businessKeys.size() : 0);
		// Adding searched Items in Inbox result object for WS and SW
		if (originalModuleName != null && (originalModuleName.equals(WS) || originalModuleName.equals(SW))) {
			log.debug("Building inbox items for WS/SW module");
			if (!CollectionUtils.isEmpty(result)) {
				log.debug("Adding WS/SW items from ElasticSearch result - count: {}", result.size());
				result.forEach(res -> {
					Inbox inbox = new Inbox();
					JsonNode jsonNode = mapper.convertValue(res.get("Data"), JsonNode.class);
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("Data", jsonNode);
					jsonObject.put("serviceSLA", res.get("serviceSLA"));
					inbox.setBusinessObject(toMap(jsonObject));
					inboxes.add(inbox);
				});
				log.debug("WS/SW inbox items added - count: {}", result.size());
			}
		}
		if (businessMap != null && !businessMap.isEmpty() && processInstanceMap != null
				&& !processInstanceMap.isEmpty()) {
			if (CollectionUtils.isEmpty(businessKeys)) {
				log.debug("Building inbox items from business map keys");
				businessMap.keySet().forEach(businessKey -> {
					if (null != processInstanceMap.get(businessKey)) {
						if (!isBusinessServiceWSOrSW) {
							Inbox inbox = new Inbox();
							inbox.setProcessInstance(processInstanceMap.get(businessKey));
							inbox.setBusinessObject(toMap((JSONObject) businessMap.get(businessKey)));
							inboxes.add(inbox);
						} else {
							Inbox inbox = new Inbox();
							inbox.setProcessInstance(processInstanceMap.get(businessKey));
							inbox.setBusinessObject(toMap((JSONObject) businessMap.get(businessKey)));
							Object consumerCode = inbox.getBusinessObject().get("consumerCode");
							if (consumerCode != null && serviceSearchMap.containsKey(consumerCode)) {
								inbox.setServiceObject(toMap((JSONObject) serviceSearchMap.get(consumerCode)));
							}
							inboxes.add(inbox);
						}
					}
				});
				log.debug("Inbox items built from business map - count: {}", 
						businessMap.keySet().size());
			} else {
				log.debug("Building inbox items from business keys - count: {}", businessKeys.size());
				if (!isBusinessServiceWSOrSW) {
					businessKeys.forEach(businessKey -> {
						Inbox inbox = new Inbox();
						inbox.setProcessInstance(processInstanceMap.get(businessKey));
						inbox.setBusinessObject(toMap((JSONObject) businessMap.get(businessKey)));
						inboxes.add(inbox);
					});
				} else {
					for (String businessKey : businessKeys) {
						Inbox inbox = new Inbox();
						inbox.setProcessInstance(processInstanceMap.get(businessKey));
						inbox.setBusinessObject(toMap((JSONObject) businessMap.get(businessKey)));
						Object consumerCode = inbox.getBusinessObject().get("consumerCode");
						if (consumerCode != null && serviceSearchMap.containsKey(consumerCode)) {
							inbox.setServiceObject(toMap((JSONObject) serviceSearchMap.get(consumerCode)));
						}
						inboxes.add(inbox);
					}
				}
				log.debug("Inbox items built from business keys - count: {}", businessKeys.size());
			}
		} else {
			log.debug("Skipping inbox items build - businessMap or processInstanceMap is empty");
		}
		log.debug("Inbox items building completed - totalCount: {}", inboxes.size());
	}

	/**
	 * Handles empty module search criteria scenario
	 */
	private void handleEmptyModuleSearchCriteria(InboxSearchCriteria criteria,
			ProcessInstanceSearchCriteria processCriteria, RequestInfo requestInfo, List<String> businessServiceName,
			Map<String, String> srvMap, List<Inbox> inboxes) {
		log.trace("Method invoked: handleEmptyModuleSearchCriteria");
		log.debug("Handling empty module search criteria scenario");
		processCriteria.setOffset(criteria.getOffset());
		processCriteria.setLimit(criteria.getLimit());
		log.debug("Set offset and limit - offset: {}, limit: {}", criteria.getOffset(), criteria.getLimit());

		ProcessInstanceResponse processInstanceResponse = workflowService.getProcessInstance(processCriteria,
				requestInfo);
		List<ProcessInstance> processInstances = processInstanceResponse.getProcessInstances();
		log.debug("Process instances fetched - count: {}", processInstances != null ? processInstances.size() : 0);
		Map<String, ProcessInstance> processInstanceMap = processInstances.stream()
				.collect(Collectors.toMap(ProcessInstance::getBusinessId, Function.identity()));
		HashMap moduleSearchCriteria = new HashMap<String, String>();
		if (CollectionUtils.isEmpty(srvMap)) {
			log.error("Service map is empty for business service: {}", businessServiceName);
			throw new CustomException(ErrorConstants.INVALID_MODULE,
					"config not found for the businessService : " + businessServiceName);
		}
		String businessIdParam = srvMap.get("businessIdProperty");
		moduleSearchCriteria.put(srvMap.get("applNosParam"),
				StringUtils.arrayToDelimitedString(processInstanceMap.keySet().toArray(), ","));
		moduleSearchCriteria.put("tenantId", criteria.getTenantId());
		moduleSearchCriteria.put("limit", -1);
		log.debug("Fetching business objects for empty module search criteria");
		JSONArray businessObjects = fetchModuleObjects(moduleSearchCriteria, businessServiceName,
				criteria.getTenantId(), requestInfo, srvMap);
		log.debug("Business objects fetched - count: {}", businessObjects.length());
		Map<String, Object> businessMap = StreamSupport.stream(businessObjects.spliterator(), false)
				.collect(Collectors.toMap(s1 -> ((JSONObject) s1).get(businessIdParam).toString(), s1 -> s1));

		if (businessObjects.length() > 0 && processInstances.size() > 0) {
			log.debug("Building inbox items from process instances and business objects");
			processInstanceMap.keySet().forEach(pinstance -> {
				Inbox inbox = new Inbox();
				inbox.setProcessInstance(processInstanceMap.get(pinstance));
				inbox.setBusinessObject(toMap((JSONObject) businessMap.get(pinstance)));
				inboxes.add(inbox);
			});
			log.debug("Inbox items built for empty module search criteria - count: {}", inboxes.size());
		} else {
			log.debug("No inbox items to build - businessObjects: {}, processInstances: {}", 
					businessObjects.length(), processInstances.size());
		}
	}

	/**
	 * Processes FSM module specific logic
	 */
	private List<HashMap<String, Object>> processFsmModule(InboxSearchCriteria criteria,
			ProcessInstanceSearchCriteria processCriteria, RequestInfo requestInfo, List<String> inputStatuses,
			List<Inbox> inboxes, HashMap moduleSearchCriteria, List<String> businessServiceName,
			Map<String, String> srvMap, List<HashMap<String, Object>> statusCountMap, Integer[] totalCountRef) {
		log.trace("Method invoked: processFsmModule");
		log.debug("Processing FSM module specific logic");
		Integer totalCount = totalCountRef[0];
		List<String> applicationStatus = new ArrayList<>();
		applicationStatus.add(WAITING_FOR_DISPOSAL_STATE);
		applicationStatus.add(DISPOSED_STATE);
		log.debug("Fetching vehicle trip response for FSM - applicationStatus: {}", applicationStatus);
		List<Map<String, Object>> vehicleResponse = fetchVehicleTripResponse(criteria, requestInfo, applicationStatus);
		BusinessService businessService = workflowService.getBusinessService(criteria.getTenantId(), requestInfo,
				FSM_VEHICLE_TRIP_MODULE);
		log.debug("Populating status count map from vehicle response");
		populateStatusCountMap(statusCountMap, vehicleResponse, businessService);

		for (HashMap<String, Object> vTripMap : statusCountMap) {
			if ((WAITING_FOR_DISPOSAL_STATE.equals(vTripMap.get(APPLICATIONSTATUS))
					|| DISPOSED_STATE.equals(vTripMap.get(APPLICATIONSTATUS)))
					&& inputStatuses.contains(vTripMap.get(STATUSID))) {
				totalCount += ((int) vTripMap.get(COUNT));
			}
		}
		log.debug("Total count updated from vehicle trips - totalCount: {}", totalCount);

		List<String> requiredApplications = new ArrayList<>();
		inboxes.forEach(inbox -> {
			ProcessInstance inboxProcessInstance = inbox.getProcessInstance();
			if (null != inboxProcessInstance && null != inboxProcessInstance.getState()) {
				String appStatus = inboxProcessInstance.getState().getApplicationStatus();
				if (DSO_INPROGRESS_STATE.equals(appStatus) || CITIZEN_FEEDBACK_PENDING_STATE.equals(appStatus)
						|| COMPLETED_STATE.equals(appStatus)) {
					requiredApplications.add(inboxProcessInstance.getBusinessId());
				}
			}
		});
		log.debug("Required applications identified for vehicle trip details - count: {}", requiredApplications.size());

		List<VehicleTripDetail> vehicleTripDetail = fetchVehicleStatusForApplication(requiredApplications, requestInfo,
				criteria.getTenantId());
		log.debug("Vehicle trip details fetched - count: {}", vehicleTripDetail.size());
		inboxes.forEach(inbox -> {
			if (null != inbox && null != inbox.getProcessInstance()
					&& null != inbox.getProcessInstance().getBusinessId()) {
				List<VehicleTripDetail> vehicleTripDetails = vehicleTripDetail.stream()
						.filter(trip -> inbox.getProcessInstance().getBusinessId().equals(trip.getReferenceNo()))
						.collect(Collectors.toList());
				Map<String, Object> vehicleBusinessObject = inbox.getBusinessObject();
				vehicleBusinessObject.put(VEHICLE_LOG, vehicleTripDetails);
			}
		});
		log.debug("Vehicle trip details added to inbox items");

		if (CollectionUtils.isEmpty(inboxes) && totalCount > 0 && !moduleSearchCriteria.containsKey("applicationNos")) {
			log.debug("Inboxes empty but totalCount > 0, fetching FSM applications from vehicle state map");
			inputStatuses = inputStatuses.stream().filter(x -> x != null).collect(Collectors.toList());
			List<String> fsmApplicationList = fetchVehicleStateMap(inputStatuses, requestInfo, criteria.getTenantId(),
					criteria.getLimit(), criteria.getOffset());
			log.debug("FSM application list fetched - count: {}", fsmApplicationList.size());
			moduleSearchCriteria.put("applicationNos", fsmApplicationList);
			moduleSearchCriteria.put("applicationStatus", requiredApplications);
			processCriteria.setBusinessIds(fsmApplicationList);
			processCriteria.setStatus(null);
			ProcessInstanceResponse processInstanceResponse = workflowService.getProcessInstance(processCriteria,
					requestInfo);
			List<ProcessInstance> vehicleProcessInstances = processInstanceResponse.getProcessInstances();
			log.debug("Vehicle process instances fetched - count: {}", 
					vehicleProcessInstances != null ? vehicleProcessInstances.size() : 0);
			Map<String, ProcessInstance> vehicleProcessInstanceMap = vehicleProcessInstances.stream()
					.collect(Collectors.toMap(ProcessInstance::getBusinessId, Function.identity()));
			JSONArray vehicleBusinessObjects = fetchModuleObjects(moduleSearchCriteria, businessServiceName,
					criteria.getTenantId(), requestInfo, srvMap);
			String businessIdParam = srvMap.get("businessIdProperty");
			Map<String, Object> vehicleBusinessMap = StreamSupport.stream(vehicleBusinessObjects.spliterator(), false)
					.collect(Collectors.toMap(s1 -> ((JSONObject) s1).get(businessIdParam).toString(), s1 -> s1,
							(e1, e2) -> e1, LinkedHashMap::new));
			log.debug("Vehicle business objects fetched - count: {}", vehicleBusinessObjects.length());

			if (vehicleBusinessObjects.length() > 0 && vehicleProcessInstances.size() > 0) {
				log.debug("Building inbox items from vehicle business objects");
				fsmApplicationList.forEach(busiessKey -> {
					Inbox inbox = new Inbox();
					inbox.setProcessInstance(vehicleProcessInstanceMap.get(busiessKey));
					inbox.setBusinessObject(toMap((JSONObject) vehicleBusinessMap.get(busiessKey)));
					inboxes.add(inbox);
				});
				log.debug("Inbox items built from vehicle business objects - count: {}", fsmApplicationList.size());
			}
		}

		// SAN-920: Logic for aggregating the statuses of Pay now and post pay application
		log.debug("Aggregating FSM status count map");
		totalCountRef[0] = totalCount;
		List<HashMap<String, Object>> aggregatedMap = aggregateFsmStatusCountMap(statusCountMap);
		log.debug("FSM module processing completed - aggregatedStatusCountMapSize: {}", aggregatedMap.size());
		return aggregatedMap;
	}

	/**
	 * Aggregates FSM status count map by application status
	 */
	private List<HashMap<String, Object>> aggregateFsmStatusCountMap(
			List<HashMap<String, Object>> statusCountMap) {
		log.trace("Method invoked: aggregateFsmStatusCountMap");
		log.debug("Aggregating FSM status count map - inputSize: {}", statusCountMap.size());
		List<HashMap<String, Object>> aggregateStatusCountMap = new ArrayList<>();
		for (HashMap<String, Object> statusCountEntry : statusCountMap) {
			HashMap<String, Object> tempStatusMap = new HashMap<>();
			boolean matchFound = false;
			for (HashMap<String, Object> aggrMapInstance : aggregateStatusCountMap) {
				String statusMapAppStatus = (String) statusCountEntry.get("applicationstatus");
				String aggrMapAppStatus = (String) aggrMapInstance.get("applicationstatus");

				if (aggrMapAppStatus.equalsIgnoreCase(statusMapAppStatus)) {
					aggrMapInstance.put(COUNT,
							((Integer) statusCountEntry.get(COUNT) + (Integer) aggrMapInstance.get(COUNT)));
					aggrMapInstance.put(APPLICATIONSTATUS, (String) statusCountEntry.get(APPLICATIONSTATUS));
					aggrMapInstance.put(BUSINESS_SERVICE_PARAM,
							(String) statusCountEntry.get(BUSINESS_SERVICE_PARAM) + ","
									+ (String) aggrMapInstance.get(BUSINESS_SERVICE_PARAM));
					aggrMapInstance.put(STATUSID,
							(String) statusCountEntry.get(STATUSID) + "," + (String) aggrMapInstance.get(STATUSID));
					matchFound = true;
					break;
				} else {
					tempStatusMap.put(COUNT, (Integer) statusCountEntry.get(COUNT));
					tempStatusMap.put(APPLICATIONSTATUS, (String) statusCountEntry.get(APPLICATIONSTATUS));
					tempStatusMap.put(BUSINESS_SERVICE_PARAM, (String) statusCountEntry.get(BUSINESS_SERVICE_PARAM));
					tempStatusMap.put(STATUSID, (String) statusCountEntry.get(STATUSID));
				}
			}
			if (ObjectUtils.isEmpty(aggregateStatusCountMap)) {
				aggregateStatusCountMap.add(statusCountEntry);
			} else {
				if (!matchFound) {
					aggregateStatusCountMap.add(tempStatusMap);
				}
			}
		}
		log.debug("FSM status count map aggregation completed - outputSize: {}", aggregateStatusCountMap.size());
		return aggregateStatusCountMap;
	}
}
