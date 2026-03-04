package org.egov.infra.mdms.service;

import java.util.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.util.ObjectUtils;

import static org.egov.infra.mdms.utils.MDMSConstants.*;

@Service
@Slf4j
public class MDMSService {

	private MdmsDataValidator mdmsDataValidator;

	private MdmsDataEnricher mdmsDataEnricher;

	private MdmsDataRepository mdmsDataRepository;

	private SchemaUtil schemaUtil;

	private MultiStateInstanceUtil multiStateInstanceUtil;

	@Autowired
	public MDMSService(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
					   MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil, MultiStateInstanceUtil multiStateInstanceUtil) {
		this.mdmsDataValidator = mdmsDataValidator;
		this.mdmsDataEnricher = mdmsDataEnricher;
		this.mdmsDataRepository = mdmsDataRepository;
		this.schemaUtil = schemaUtil;
		this.multiStateInstanceUtil = multiStateInstanceUtil;
	}

	/**
	 * This method processes the requests that come for master data creation.
	 * @param mdmsRequest
	 * @return
	 */
	public List<Mdms> create(MdmsRequest mdmsRequest) {
		log.trace("MDMSService.create: method invoked");
		String tenantId = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getTenantId() : "null";
		String schemaCode = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getSchemaCode() : "null";
		log.info("Processing MDMS create request for tenant: {}, schemaCode: {}", tenantId, schemaCode);

		// Fetch schema against which data is getting created
		log.debug("Fetching schema definition for schemaCode: {}", schemaCode);
		JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);

		// Validate incoming request
		log.debug("Validating MDMS create request");
		mdmsDataValidator.validateCreateRequest(mdmsRequest, schemaObject);

		// Enrich incoming request
		log.debug("Enriching MDMS create request");
		mdmsDataEnricher.enrichCreateRequest(mdmsRequest, schemaObject);

		// Emit mdms creation request event
		log.debug("Publishing MDMS create request to Kafka");
		mdmsDataRepository.create(mdmsRequest);
		log.info("MDMS create request processed successfully for tenant: {}, schemaCode: {}", tenantId, schemaCode);

		return Arrays.asList(mdmsRequest.getMdms());
	}

	/**
	 * This method processes the requests that come for master data search.
	 * @param mdmsCriteriaReq
	 * @return
	 */
	public Map<String, Map<String, JSONArray>> search(MdmsCriteriaReq mdmsCriteriaReq) {
		log.trace("MDMSService.search: method invoked");
		Map<String, Map<String, JSONArray>> tenantMasterMap = new HashMap<>();

		/*
		 * Set incoming tenantId as state level tenantId for fallback in case master data for
		 * concrete tenantId does not exist.
		 */
		String tenantId = new StringBuilder(mdmsCriteriaReq.getMdmsCriteria().getTenantId()).toString();
		String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);
		log.debug("Converting tenantId: {} to state level tenantId: {}", tenantId, stateLevelTenantId);
		mdmsCriteriaReq.getMdmsCriteria().setTenantId(stateLevelTenantId);

		Map<String, String> schemaCodes = getSchemaCodes(mdmsCriteriaReq.getMdmsCriteria());
		log.debug("Extracted schema codes count: {}", schemaCodes != null ? schemaCodes.size() : 0);
		mdmsCriteriaReq.getMdmsCriteria().setSchemaCodeFilterMap(schemaCodes);

		// Make a call to the repository layer to fetch data as per given criteria
		log.info("Fetching MDMS data from repository");
		tenantMasterMap = mdmsDataRepository.search(mdmsCriteriaReq.getMdmsCriteria());
		log.debug("Repository returned data for tenant count: {}", tenantMasterMap != null ? tenantMasterMap.size() : 0);

		// Apply filters to incoming data
		log.debug("Applying filters to fetched data");
		tenantMasterMap = applyFilterToData(tenantMasterMap, mdmsCriteriaReq.getMdmsCriteria().getSchemaCodeFilterMap());

		// Perform fallback
		log.debug("Performing fallback for tenantId: {}", tenantId);
		Map<String, JSONArray> masterDataMap = FallbackUtil.backTrackTenantMasterDataMap(tenantMasterMap, tenantId);

		// Return response in MDMS v1 search response format for backward compatibility
		log.info("MDMS search request processed successfully");
		return getModuleMasterMap(masterDataMap);
	}

	private Map<String, Map<String, JSONArray>> applyFilterToData(Map<String, Map<String, JSONArray>> tenantMasterMap, Map<String, String> schemaCodeFilterMap) {
		log.trace("MDMSService.applyFilterToData: method invoked");
		Map<String, Map<String, JSONArray>> tenantMasterMapPostFiltering = new HashMap<>();

		tenantMasterMap.keySet().forEach(tenantId -> {
			Map<String, JSONArray> schemaCodeVsFilteredMasters = new HashMap<>();
			tenantMasterMap.get(tenantId).keySet().forEach(schemaCode -> {
				JSONArray masters = tenantMasterMap.get(tenantId).get(schemaCode);
				if(!ObjectUtils.isEmpty(schemaCodeFilterMap.get(schemaCode))) {
					schemaCodeVsFilteredMasters.put(schemaCode, filterMasters(masters, schemaCodeFilterMap.get(schemaCode)));
				} else {
					schemaCodeVsFilteredMasters.put(schemaCode, masters);
				}
			});
			tenantMasterMapPostFiltering.put(tenantId, schemaCodeVsFilteredMasters);
		});

		return tenantMasterMapPostFiltering;
	}

	private JSONArray filterMasters(JSONArray masters, String filterExp) {
		log.trace("MDMSService.filterMasters: method invoked with filter expression");
		log.debug("Filtering masters array of size: {} with expression", masters != null ? masters.size() : 0);
		JSONArray filteredMasters = JsonPath.read(masters, filterExp);
		log.debug("Filtered masters count: {}", filteredMasters != null ? filteredMasters.size() : 0);
		return filteredMasters;
	}

	private Map<String, Map<String, JSONArray>> getModuleMasterMap(Map<String, JSONArray> masterMap) {
		log.trace("MDMSService.getModuleMasterMap: method invoked");
		Map<String, Map<String, JSONArray>> moduleMasterMap = new HashMap<>();

		for (Map.Entry<String, JSONArray> entry : masterMap.entrySet()) {
			String[] moduleMaster = entry.getKey().split(DOT_REGEX);
			String moduleName = moduleMaster[0];
			String masterName = moduleMaster[1];

			moduleMasterMap.computeIfAbsent(moduleName, k -> new HashMap<>())
					.put(masterName, entry.getValue());
		}
		log.debug("Created module master map with module count: {}", moduleMasterMap.size());
		return moduleMasterMap;
	}

	private Map<String, String> getSchemaCodes(MdmsCriteria mdmsCriteria) {
		log.trace("MDMSService.getSchemaCodes: method invoked");
		Map<String, String> schemaCodesFilterMap = new HashMap<>();
		for (ModuleDetail moduleDetail : mdmsCriteria.getModuleDetails()) {
			for (MasterDetail masterDetail : moduleDetail.getMasterDetails()) {
				String key = moduleDetail.getModuleName().concat(DOT_SEPARATOR).concat(masterDetail.getName());
				String value = masterDetail.getFilter();
				schemaCodesFilterMap.put(key, value);
			}
		}
		log.debug("Extracted schema codes count: {}", schemaCodesFilterMap.size());
		return schemaCodesFilterMap;
	}
}