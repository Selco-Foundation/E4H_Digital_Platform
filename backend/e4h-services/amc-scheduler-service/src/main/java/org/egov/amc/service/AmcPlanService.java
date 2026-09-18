package org.egov.amc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.AmcPlanRepository;
import org.egov.amc.service.enrichment.AmcPlanEnrichment;
import org.egov.amc.util.MDMSUtils;
import org.egov.amc.validator.AmcPlanValidator;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationRequest;
import org.egov.amc.web.models.AmcPlan;
import org.egov.amc.web.models.AmcPlanRequest;
import org.egov.amc.web.models.AmcPlanSearchCriteria;
import org.egov.amc.web.models.AmcPlanSearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class AmcPlanService {

    private static final String FALLBACK_STATE_CODE = "NA";

    private final AmcPlanValidator amcPlanValidator;
    private final AmcPlanRepository amcPlanRepository;
    private final Producer producer;
    private final AmcPlanEnrichment amcPlanEnrichment;
    private final AMCServiceConfiguration amcServiceConfiguration;
    private final MDMSUtils mdmsUtils;

    @Autowired
    public AmcPlanService(AmcPlanValidator amcPlanValidator, AmcPlanRepository amcPlanRepository, Producer producer,
                           AmcPlanEnrichment amcPlanEnrichment, AMCServiceConfiguration amcServiceConfiguration,
                           MDMSUtils mdmsUtils) {
        this.amcPlanValidator = amcPlanValidator;
        this.amcPlanRepository = amcPlanRepository;
        this.producer = producer;
        this.amcPlanEnrichment = amcPlanEnrichment;
        this.amcServiceConfiguration = amcServiceConfiguration;
        this.mdmsUtils = mdmsUtils;
    }

    public AmcPlanRequest createAmcPlan(AmcPlanRequest request) {
        log.trace("Entering createAmcPlan method");
        amcPlanValidator.validateCreateAmcPlanRequest(request);

        for (AmcPlan amcPlan : request.getAmcPlans()) {
            String stateCode = resolveStateCode(request.getRequestInfo(), amcPlan.getGeographyScope(), amcPlan.getTenantId());
            amcPlan.setName(buildName(stateCode, amcPlan.getStartDate(), amcPlan.getHealthFacilityNumber()));
            amcPlanEnrichment.enrichAmcPlanOnCreate(amcPlan, request.getRequestInfo());
        }

        log.info("Pushing {} AMC plan(s) to kafka", request.getAmcPlans().size());
        producer.push(amcServiceConfiguration.getSaveAmcPlanTopic(), request);
        return request;
    }

    public AmcPlanRequest updateAmcPlan(AmcPlanRequest request) {
        log.trace("Entering updateAmcPlan method");
        amcPlanValidator.validateUpdateRequestIdentifiers(request);

        List<AmcPlan> amcPlansFromDB = searchAmcPlan(
                getSearchAmcPlanRequest(request.getAmcPlans(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getAmcPlans().get(0).getTenantId(), false, null);

        amcPlanValidator.validateUpdateAgainstDB(request.getAmcPlans(), amcPlansFromDB);

        for (AmcPlan amcPlan : request.getAmcPlans()) {
            AmcPlan amcPlanFromDB = findAmcPlanById(amcPlan.getId(), amcPlansFromDB);
            if (amcPlanFromDB == null) {
                continue;
            }

            // The plan name is derived from healthFacilityNumber/startDate - recompute it whenever
            // either changes, the same "recompute derived field when its inputs change" pattern
            // already used for AmcConfiguration.configurationEndDate (applyDurationDrivenEndDates).
            boolean facilityCountChanged = amcPlan.getHealthFacilityNumber() != null
                    && !Objects.equals(amcPlan.getHealthFacilityNumber(), amcPlanFromDB.getHealthFacilityNumber());
            boolean startDateChanged = amcPlan.getStartDate() != null
                    && !Objects.equals(amcPlan.getStartDate(), amcPlanFromDB.getStartDate());
            if (facilityCountChanged || startDateChanged) {
                Integer healthFacilityNumber = amcPlan.getHealthFacilityNumber() != null
                        ? amcPlan.getHealthFacilityNumber() : amcPlanFromDB.getHealthFacilityNumber();
                Long startDate = amcPlan.getStartDate() != null ? amcPlan.getStartDate() : amcPlanFromDB.getStartDate();
                Map<String, Object> geographyScope = amcPlan.getGeographyScope() != null
                        ? amcPlan.getGeographyScope() : amcPlanFromDB.getGeographyScope();
                String stateCode = resolveStateCode(request.getRequestInfo(), geographyScope, amcPlanFromDB.getTenantId());
                amcPlan.setName(buildName(stateCode, startDate, healthFacilityNumber));
                log.info("Recomputed name for planId: {} ({} -> {})", amcPlan.getId(), amcPlanFromDB.getName(), amcPlan.getName());
            } else {
                amcPlan.setName(amcPlanFromDB.getName());
            }

            // The persister writes every column in the UPDATE query unconditionally, so any field left
            // null here is not "left unchanged" - it overwrites the DB value with NULL. A caller (e.g.
            // ingestion-service's /amcConfigurationBulkIngest) is not required to resend every field on
            // every update, so anything omitted from the request must be carried forward from the DB
            // row explicitly. projectId/tenantId are additionally immutable (validated above).
            if (amcPlan.getProjectId() == null) {
                amcPlan.setProjectId(amcPlanFromDB.getProjectId());
            }
            if (amcPlan.getTenantId() == null) {
                amcPlan.setTenantId(amcPlanFromDB.getTenantId());
            }
            if (amcPlan.getHealthFacilityNumber() == null) {
                amcPlan.setHealthFacilityNumber(amcPlanFromDB.getHealthFacilityNumber());
            }
            if (amcPlan.getStartDate() == null) {
                amcPlan.setStartDate(amcPlanFromDB.getStartDate());
            }
            if (amcPlan.getEndDate() == null) {
                amcPlan.setEndDate(amcPlanFromDB.getEndDate());
            }
            if (amcPlan.getGeographyScope() == null) {
                amcPlan.setGeographyScope(amcPlanFromDB.getGeographyScope());
            }
            if (amcPlan.getSelectedActivities() == null) {
                amcPlan.setSelectedActivities(amcPlanFromDB.getSelectedActivities());
            }
            if (amcPlan.getStatus() == null) {
                amcPlan.setStatus(amcPlanFromDB.getStatus());
            }
            if (amcPlan.getAdditionalDetails() == null) {
                amcPlan.setAdditionalDetails(amcPlanFromDB.getAdditionalDetails());
            }

            amcPlanEnrichment.enrichAmcPlanOnUpdate(amcPlan, amcPlanFromDB, request.getRequestInfo());
        }

        log.info("Pushing {} AMC plan update(s) to kafka", request.getAmcPlans().size());
        producer.push(amcServiceConfiguration.getUpdateAmcPlanTopic(), request);
        return request;
    }

    public List<AmcPlan> searchAmcPlan(AmcPlanSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("Entering searchAmcPlan method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        amcPlanValidator.validateSearchAmcPlanRequest(request, limit, offset, tenantId);
        List<AmcPlan> amcPlanList = amcPlanRepository.getAmcPlan(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        log.debug("Found {} AMC plan(s) matching search criteria", amcPlanList.size());
        return amcPlanList;
    }

    public Integer countAllAmcPlan(AmcPlanSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return amcPlanRepository.getAmcPlanCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    private AmcPlanSearchRequest getSearchAmcPlanRequest(List<AmcPlan> amcPlans, RequestInfo requestInfo) {
        List<String> ids = amcPlans.stream().map(AmcPlan::getId).toList();
        AmcPlanSearchCriteria criteria = AmcPlanSearchCriteria.builder().ids(ids).tenantId(amcPlans.get(0).getTenantId()).build();
        return AmcPlanSearchRequest.builder().RequestInfo(requestInfo).searchCriteria(criteria).build();
    }

    private AmcPlan findAmcPlanById(String id, List<AmcPlan> amcPlansFromDB) {
        return amcPlansFromDB.stream().filter(p -> Objects.equals(id, p.getId())).findFirst().orElse(null);
    }

    /** StateCode-AMC-StartYear-NoOfFacility, e.g. "KA-AMC-26-42". */
    private String buildName(String stateCode, Long startDate, Integer healthFacilityNumber) {
        return stateCode + "-AMC-" + twoDigitYear(startDate) + "-" + healthFacilityNumber;
    }

    /** Mirrors FieldPlannerServiceUtil.getDuration()'s "year % 100" 2-digit-year formatting. */
    private String twoDigitYear(Long epochMillis) {
        int year = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).getYear();
        return String.format("%02d", year % 100);
    }

    /**
     * Resolves the 2-letter state code from MDMS common-masters.StateInfo, matched by the plan's
     * geographyScope.state boundary code (e.g. "India_Maharashtra") - the same MDMS lookup
     * ProjectNameGenerationService.resolveStateCode uses for the revised project id, just keyed
     * directly off the already-known state boundary code instead of re-deriving it from an address.
     * Best-effort: a missing geographyScope.state, no MDMS match, or the call failing all fall back
     * to a placeholder rather than blocking AmcPlan creation.
     */
    private String resolveStateCode(RequestInfo requestInfo, Map<String, Object> geographyScope, String tenantId) {
        try {
            Object stateValue = geographyScope != null ? geographyScope.get("state") : null;
            if (!(stateValue instanceof String stateBoundaryCode) || StringUtils.isBlank(stateBoundaryCode)) {
                log.warn("No 'state' key in geographyScope, falling back to state code '{}'", FALLBACK_STATE_CODE);
                return FALLBACK_STATE_CODE;
            }

            AmcConfigurationRequest dummyRequest = AmcConfigurationRequest.builder()
                    .requestInfo(requestInfo)
                    .amcConfigurations(List.of(AmcConfiguration.builder().tenantId(tenantId).build()))
                    .build();
            String rootTenantId = tenantId != null ? tenantId.split("\\.")[0] : "in";
            Object mdmsResponse = mdmsUtils.mDMSCall(dummyRequest, rootTenantId);
            String code = extractStateCodeFromMDMSResponse(mdmsResponse, stateBoundaryCode);
            if (StringUtils.isNotBlank(code)) {
                return code.toUpperCase();
            }
            log.warn("No MDMS StateInfo match for boundaryCode: {}, falling back to state code '{}'",
                    stateBoundaryCode, FALLBACK_STATE_CODE);
        } catch (Exception e) {
            log.error("Error resolving state code from MDMS for geographyScope: {}", geographyScope, e);
        }
        return FALLBACK_STATE_CODE;
    }

    @SuppressWarnings("unchecked")
    private String extractStateCodeFromMDMSResponse(Object mdmsResponse, String stateBoundaryCode) {
        if (!(mdmsResponse instanceof Map)) {
            return null;
        }
        Map<String, Object> responseMap = (Map<String, Object>) mdmsResponse;
        Object mdmsRes = responseMap.get("MdmsRes");
        if (!(mdmsRes instanceof Map)) {
            return null;
        }
        Object commonMasters = ((Map<String, Object>) mdmsRes).get("common-masters");
        if (!(commonMasters instanceof Map)) {
            return null;
        }
        Object stateInfoObj = ((Map<String, Object>) commonMasters).get("StateInfo");
        if (!(stateInfoObj instanceof List)) {
            return null;
        }
        for (Object entry : (List<?>) stateInfoObj) {
            if (!(entry instanceof Map)) {
                continue;
            }
            LinkedHashMap<String, Object> item = new LinkedHashMap<>((Map<String, Object>) entry);
            if (!Boolean.TRUE.equals(item.get("active"))) {
                continue;
            }
            Object boundaryCodeObj = item.get("boundaryCode");
            if (boundaryCodeObj instanceof String boundaryCode
                    && StringUtils.isNotBlank(boundaryCode)
                    && (stateBoundaryCode.equalsIgnoreCase(boundaryCode)
                    || stateBoundaryCode.equalsIgnoreCase(StringUtils.substringAfterLast(boundaryCode, "_")))) {
                Object code = item.get("code");
                if (code instanceof String codeStr && StringUtils.isNotBlank(codeStr)) {
                    return codeStr;
                }
            }
        }
        return null;
    }
}
