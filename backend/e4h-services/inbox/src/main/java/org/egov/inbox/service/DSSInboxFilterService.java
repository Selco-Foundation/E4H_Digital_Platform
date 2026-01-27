package org.egov.inbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.util.ErrorConstants;
import org.egov.inbox.web.model.dss.*;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static org.egov.inbox.util.DSSConstants.*;
@Slf4j
@Service
public class DSSInboxFilterService {

    @Autowired
    private InboxConfiguration config;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Value("${egov.dashboard.analytics.host}")
    private String dashboardAnalyticsHost;

    @Value("${egov.dashboard.analytics.getchartv2.path}")
    private String dashboardAnalyticsEndPoint;

    public Map<String, BigDecimal> getAggregateData(InboxMetricCriteria request) {
        log.trace("Method invoked: getAggregateData - module: {}", request.getModule());
        Map<String, BigDecimal> result = new HashMap<>();
        try {
            AggregateRequestDto aggregateRequestDto = initializeAggregateRequestDto();
            AggregationRequest aggRequest = initializeAggregationRequest();
            Object mdmsData = fetchAggregationDataFromMdms();
            processAggregationData(request, mdmsData, aggregateRequestDto, aggRequest, result);
            log.info("Aggregate data retrieved successfully - resultCount: {}", result.size());
        } catch (Exception e) {
            log.error("Error occurred while fetching aggregate data", e);
            throw new CustomException(ErrorConstants.INVALID_MODULE_DATA, e.getMessage());
        }
        return result;
    }

    private AggregateRequestDto initializeAggregateRequestDto() {
        log.trace("Method invoked: initializeAggregateRequestDto");
        RequestDate dateReq = new RequestDate();
        dateReq.setInterval(DSS_INTERVAL);
        
        AggregateRequestDto aggregateRequestDto = new AggregateRequestDto();
        aggregateRequestDto.setRequestDate(dateReq);
        aggregateRequestDto.setVisualizationType(DSS_VISUALIZATIONTYPE);
        aggregateRequestDto.setModuleLevel("");
        
        Map<String, Object> filters = new HashMap<>();
        filters.put(TENANT_ID, new ArrayList<>());
        aggregateRequestDto.setFilters(filters);
        log.debug("Aggregate request DTO initialized");
        return aggregateRequestDto;
    }

    private AggregationRequest initializeAggregationRequest() {
        log.trace("Method invoked: initializeAggregationRequest");
        Map<String, Object> headers = new HashMap<>();
        String tenantId = config.getStateLevelTenantId();
        headers.put(TENANT_ID, tenantId);
        
        AggregationRequest aggRequest = new AggregationRequest();
        aggRequest.setHeaders(headers);
        log.debug("Aggregation request initialized - tenantId: {}", tenantId);
        return aggRequest;
    }

    private Object fetchAggregationDataFromMdms() {
        log.trace("Method invoked: fetchAggregationDataFromMdms");
        String tenantId = config.getStateLevelTenantId();
        log.debug("Fetching aggregation data from MDMS - tenantId: {}", tenantId);
        Object mdmsData = mdmsCall(tenantId, AGGREGATE_MASTER_CODE);
        log.debug("MDMS data fetched successfully");
        return mdmsData;
    }

    private void processAggregationData(InboxMetricCriteria request, Object mdmsData,
                                       AggregateRequestDto aggregateRequestDto,
                                       AggregationRequest aggRequest,
                                       Map<String, BigDecimal> result) {
        log.trace("Method invoked: processAggregationData");
        List<Map> aggregationData = JsonPath.read(mdmsData, MDMS_AGGREGATE_PATH);
        log.debug("Processing aggregation data - entryCount: {}", aggregationData.size());

        aggregationData.forEach(aggData -> {
            if(aggData.get(MDMS_VISUALIZATION_MODULE_KEY).toString().equals(request.getModule())) {
                processModuleAggregationData(aggData, aggregateRequestDto, aggRequest, result, request);
            }
        });
        log.debug("Aggregation data processing completed");
    }

    private void processModuleAggregationData(Map aggData, AggregateRequestDto aggregateRequestDto,
                                             AggregationRequest aggRequest,
                                             Map<String, BigDecimal> result,
                                             InboxMetricCriteria request) {
        log.trace("Method invoked: processModuleAggregationData");
        List<Map<String, String>> vizCodes = JsonPath.read(aggData, MDMS_VISUALIZATION_PATH);
        log.debug("Processing visualization codes - codeCount: {}", vizCodes.size());

        vizCodes.forEach(visualizationcodes -> {
            try {
                processVisualizationCode(visualizationcodes, aggregateRequestDto, aggRequest, result, request);
            } catch (Exception e) {
                log.error("Error processing visualization code", e);
                throw new CustomException(ErrorConstants.INVALID_MODULE_DATA, e.getMessage());
            }
        });
    }

    private void processVisualizationCode(Map<String, String> visualizationcodes,
                                         AggregateRequestDto aggregateRequestDto,
                                         AggregationRequest aggRequest,
                                         Map<String, BigDecimal> result,
                                         InboxMetricCriteria request) throws JsonProcessingException {
        log.trace("Method invoked: processVisualizationCode");
        setDateRangeForVisualization(visualizationcodes, aggregateRequestDto);
        aggregateRequestDto.setVisualizationCode(visualizationcodes.get(MDMS_VISUALIZATION_CODES_KEY).toString());
        aggRequest.setAggregationRequestDto(aggregateRequestDto);
        
        log.info("Request for " + request.getModule() + ": " + mapper.writeValueAsString(aggRequest));
        Object response = getHeaderData(aggRequest);
        MetricResponse metricResponse = mapper.convertValue(response, MetricResponse.class);
        result.put(visualizationcodes.get(MDMS_VISUALIZATION_CODES_KEY), 
                metricResponse.getResponseData().getData().get(0).getHeaderValue());
        log.debug("Visualization code processed successfully");
    }

    private void setDateRangeForVisualization(Map<String, String> visualizationcodes, AggregateRequestDto aggregateRequestDto) {
        log.trace("Method invoked: setDateRangeForVisualization");
        Integer dateInMonths = Integer.parseInt(String.valueOf(visualizationcodes.get(MDMS_VISUALIZATION_DATE_KEY)));
        
        if (dateInMonths > 0) {
            Calendar cal = Calendar.getInstance();
            aggregateRequestDto.getRequestDate().setEndDate(String.valueOf(cal.getTimeInMillis()));
            cal.add(Calendar.MONTH, -dateInMonths);
            aggregateRequestDto.getRequestDate().setStartDate(String.valueOf(cal.getTimeInMillis()));
            log.debug("Date range set - dateInMonths: {}", dateInMonths);
        } else {
            aggregateRequestDto.getRequestDate().setEndDate("0");
            aggregateRequestDto.getRequestDate().setStartDate("0");
            log.debug("Date range set to default (0)");
        }
    }

    public Object mdmsCall(String tenantId, String mastername) {
        MdmsCriteriaReq mdmsCriteriaReq = enrichMdmsRequest(tenantId, mastername);
        StringBuilder url = new StringBuilder(config.getMdmsHost()).append(config.getMdmsSearchEndPoint());
        Object result = serviceRequestRepository.fetchResult(url,mdmsCriteriaReq);
        return result;
    }

    public MdmsCriteriaReq enrichMdmsRequest(String tenantId, String mastername) {
        List<MasterDetail> aggregateMasterDetails = new ArrayList<>();

        aggregateMasterDetails.add(MasterDetail.builder().name(mastername).build());

        ModuleDetail aggregateModuleDtls = ModuleDetail.builder().masterDetails(aggregateMasterDetails)
                .moduleName(AGGREGATE_MODULE_NAME).build();

        List<ModuleDetail> moduleDetails = new ArrayList<>();
        moduleDetails.add(aggregateModuleDtls);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        return MdmsCriteriaReq.builder().requestInfo(new RequestInfo()).mdmsCriteria(mdmsCriteria).build();
    }

    public Object getHeaderData(AggregationRequest request) {
        StringBuilder uri = new StringBuilder(dashboardAnalyticsHost)
                .append(dashboardAnalyticsEndPoint);
        try {
            Object response = serviceRequestRepository.fetchResult(uri, request);
            return response;
        } catch (IllegalArgumentException e) {
            throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in dss call");
        } catch (Exception e) {
            throw new CustomException("ServiceCallException", "Exception while fetching the result for dss");
        }
    }
}
