package org.egov.id.service;

import java.io.IOException;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import org.egov.id.model.IdRequest;
import org.egov.id.model.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.egov.mdms.model.ModuleDetail;
import org.egov.mdms.service.MdmsClientService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
@Log4j2
public class MdmsService {

    @Autowired
    MdmsClientService mdmsClientService;

    // 'tenants' & 'citymodule' are the JSON files inside the folder 'tenant'.
    private static final String tenantMaster = "tenants";
    private static final String cityMaster = "citymodule";
    private static final String tenantModule = "tenant";

    //'IdFormat' is the JSON file in the Folder 'common-masters'.
    private static final String formatMaster = "IdFormat";
    private static final String formatModule = "common-masters";


    public MdmsResponse getMasterData(RequestInfo requestInfo, String tenantId,
                                      Map<String, List<MasterDetail>> masterDetails) {
        log.trace("getMasterData method invoked with tenantId: {}", tenantId);
        
        MdmsResponse mdmsResponse = null;
        try {
            log.debug("Fetching MDMS data for tenantId: {}, masterDetails size: {}", tenantId, masterDetails != null ? masterDetails.size() : 0);
            mdmsResponse = mdmsClientService.getMaster(RequestInfo.toCommonRequestInfo(requestInfo), tenantId,
                    masterDetails);
            log.debug("Successfully retrieved MDMS data for tenantId: {}", tenantId);
        } catch (IOException e) {
            log.error("Error occurred while fetching MDMS data for tenantId: {}", tenantId, e);
        }
        return mdmsResponse;
    }

    /**
     * Description : This method to get CityCode from Mdms
     *
     * @param idRequest
     * @param requestInfo
     * @return cityCode
     * @throws Exception
     */

    public String getCity(RequestInfo requestInfo, IdRequest idRequest) {
        log.trace("getCity method invoked with tenantId: {}", idRequest != null ? idRequest.getTenantId() : null);
        
        try {
            log.info("Fetching city code from MDMS for tenantId: {}", idRequest != null ? idRequest.getTenantId() : null);
            Map<String, String> getCity = doMdmsServiceCall(requestInfo, idRequest);
            String cityCode = null;
            if (getCity != null) {
                cityCode = getCity.get(tenantMaster);
            }
            if(cityCode == null){
                log.warn("City code not found in MDMS response for tenantId: {}", idRequest != null ? idRequest.getTenantId() : null);
                throw new CustomException("PARSING ERROR", "City code is Null/not valid");
            }
            log.info("Successfully retrieved city code: {} for tenantId: {}", cityCode, idRequest != null ? idRequest.getTenantId() : null);
            log.debug("City code retrieved from MDMS: {}", cityCode);
            return cityCode;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while fetching city code for tenantId: {}", idRequest != null ? idRequest.getTenantId() : null, e);
            throw new CustomException("PARSING ERROR", "Failed to get citycode from MDMS");
        }
    }

    /**
     * Description : This method to get IdFormat from Mdms
     *
     * @param idRequest
     * @param requestInfo
     * @return IdFormat
     * @throws Exception
     */

    public String getIdFormat(RequestInfo requestInfo, IdRequest idRequest) {
        log.trace("getIdFormat method invoked with idName: {}, tenantId: {}", 
                idRequest != null ? idRequest.getIdName() : null, 
                idRequest != null ? idRequest.getTenantId() : null);
        
        try {
            log.info("Fetching ID format from MDMS for idName: {}, tenantId: {}", 
                    idRequest != null ? idRequest.getIdName() : null, 
                    idRequest != null ? idRequest.getTenantId() : null);
            Map<String, String> getIdFormat = doMdmsServiceCall(requestInfo, idRequest);
            String idFormat = null;
            if (getIdFormat != null) {
                idFormat = getIdFormat.get(formatMaster);
                log.debug("ID format retrieved from MDMS: {}", idFormat != null ? "found" : "null");
            }
            if (idFormat == null) {
                log.warn("ID format not found in MDMS for idName: {}, tenantId: {}", 
                        idRequest != null ? idRequest.getIdName() : null, 
                        idRequest != null ? idRequest.getTenantId() : null);
            } else {
                log.info("Successfully retrieved ID format for idName: {}", idRequest != null ? idRequest.getIdName() : null);
            }
            return idFormat;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while fetching id format for idName: {}, tenantId: {}", 
                    idRequest != null ? idRequest.getIdName() : null, 
                    idRequest != null ? idRequest.getTenantId() : null, e);
            throw new CustomException("PARSING ERROR", "Failed to get formatid from MDMS");
        }
    }

    /**
     * Prepares and returns Mdms search request
     *
     * @param requestInfo
     * @return MAP
     */
    private Map<String, String> doMdmsServiceCall(RequestInfo requestInfo, IdRequest idRequest) {
        log.trace("doMdmsServiceCall method invoked");

        String idname = idRequest.getIdName();
        String tenantId = idRequest.getTenantId();

        log.debug("Preparing MDMS call with idName: {}, tenantId: {}", idname, tenantId);

        String idFormatFromMdms = null;
        String cityCodeFromMdms = null;

        Map<String, List<MasterDetail>> masterDetails = new HashMap<String, List<MasterDetail>>();

        List<MasterDetail> masterDetailListCity = new LinkedList();
        List<MasterDetail> masterDetailListFormat = new LinkedList();

        MasterDetail masterDetailForCity = MasterDetail.builder().name(tenantMaster)
                .filter("[?(@.code=='" + tenantId + "')]").build();

        masterDetailListCity.add(masterDetailForCity);

        MasterDetail masterDetailForFormat = MasterDetail.builder().name(formatMaster)
                .filter("[?(@.idname=='" + idname + "')]").build();

        masterDetailListFormat.add(masterDetailForFormat);

        masterDetails.put(tenantModule, masterDetailListCity);
        masterDetails.put(formatModule, masterDetailListFormat);
        MdmsResponse mdmsResponse = null;

        try {
            log.debug("Calling getMasterData with tenantId: {}", tenantId);
            mdmsResponse = getMasterData(requestInfo, tenantId, masterDetails);

            if (mdmsResponse != null && mdmsResponse.getMdmsRes() != null) {
                if (mdmsResponse.getMdmsRes().containsKey(tenantModule)
                        && mdmsResponse.getMdmsRes().get(tenantModule).containsKey(tenantMaster)
                        && mdmsResponse.getMdmsRes().get(tenantModule).get(tenantMaster).size() > 0
                        && mdmsResponse.getMdmsRes().get(tenantModule).get(tenantMaster).get(0) != null) {
                    DocumentContext documentContext = JsonPath
                            .parse(mdmsResponse.getMdmsRes().get(tenantModule).get(tenantMaster).get(0));

                    cityCodeFromMdms = documentContext.read("$.city.code");
                    log.debug("Parsed city code from MDMS response: {}", cityCodeFromMdms);
                }
                if (mdmsResponse.getMdmsRes().containsKey(formatModule)
                        && mdmsResponse.getMdmsRes().get(formatModule).containsKey(formatMaster)
                        && mdmsResponse.getMdmsRes().get(formatModule).get(formatMaster).size() > 0
                        && mdmsResponse.getMdmsRes().get(formatModule).get(formatMaster).get(0) != null) {
                    DocumentContext documentContext = JsonPath
                            .parse(mdmsResponse.getMdmsRes().get(formatModule).get(formatMaster).get(0));
                    idFormatFromMdms = documentContext.read("$.format");
                    log.debug("Parsed ID format from MDMS response: {}", idFormatFromMdms != null ? "found" : "null");
                }
            } else {
                log.warn("MDMS response is null or empty for tenantId: {}", tenantId);
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("MDMS fetch failed for idName: {}, tenantId: {}", idname, tenantId, e);
            throw new CustomException("PARSING ERROR", "Failed to get citycode/formatid from MDMS");
        }
        Map<String, String> mdmsCallMap = new HashMap();
        mdmsCallMap.put(formatMaster, idFormatFromMdms);
        mdmsCallMap.put(tenantMaster, cityCodeFromMdms);

        log.debug("MDMS service call completed. idFormat found: {}, cityCode found: {}", 
                idFormatFromMdms != null, cityCodeFromMdms != null);
        return mdmsCallMap;
    }

}
