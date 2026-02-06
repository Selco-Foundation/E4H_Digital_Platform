package digit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import digit.repository.impl.BoundaryRepositoryImpl;
import digit.service.enrichment.BoundaryEntityEnricher;
import digit.service.validator.BoundaryEntityValidator;
import digit.util.HierarchyUtil;
import digit.util.ResponseUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static digit.constants.BoundaryConstants.MASTER_STATE_INFO;
import static digit.constants.BoundaryConstants.MDMS_COMMON_MASTERS_MODULE_NAME;

@Service
@Slf4j
public class BoundaryService {

    private final BoundaryEntityValidator boundaryEntityValidator;

    private final ResponseUtil responseUtil;

    private final BoundaryRepositoryImpl repository;
    private HierarchyUtil hierarchyUtil;

    public BoundaryService(BoundaryEntityValidator boundaryEntityValidator , ResponseUtil responseUtil,
                           BoundaryRepositoryImpl repository, HierarchyUtil hierarchyUtil) {

        this.boundaryEntityValidator = boundaryEntityValidator;
        this.responseUtil = responseUtil;
        this.repository = repository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * This method is used to process a boundary entity creation request
     * @param boundaryRequest is the request object
     * @return boundaryResponse
     */
    public BoundaryResponse createBoundary(BoundaryRequest boundaryRequest) {
        log.trace("createBoundary method invoked");
        int boundaryCount = boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0;
        log.info("Starting boundary creation process, boundary count={}", boundaryCount);
        log.debug("Boundary request details: tenantId from first boundary={}", 
                boundaryCount > 0 && boundaryRequest.getBoundary().get(0) != null ? 
                boundaryRequest.getBoundary().get(0).getTenantId() : null);

        // validate the request
        log.debug("Validating boundary create request");
        boundaryEntityValidator.validateCreateBoundaryRequest(boundaryRequest);
        log.debug("Boundary create request validation completed successfully");

        // enrich the request
        log.debug("Enriching boundary create request");
        BoundaryEntityEnricher.enrichCreateBoundaryRequest(boundaryRequest);
        log.debug("Boundary create request enrichment completed");

        // create response
        log.debug("Creating boundary response");
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);
        log.debug("Boundary response created, boundary count in response={}", 
                boundaryResponse.getBoundary() != null ? boundaryResponse.getBoundary().size() : 0);

        // delegating the request to repository to further persist in db
        log.info("Persisting boundaries to database");
        repository.create(boundaryRequest);
        log.info("Boundary creation process completed successfully, created {} boundaries", boundaryCount);

        // Check if new state code code or state boundary code already exist in MDMS common-master.StateInfo module. Only call for state
        boolean isValidSateCode = boundaryEntityValidator.validateStateCode(boundaryRequest);
        if (isValidSateCode)
            // If new state do not exist in MDMS common-master.StateInfo module, then create the new state in mdms
            createMdmsStateInfo(boundaryRequest);

        return boundaryResponse;
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    public BoundaryResponse searchBoundary(BoundarySearchCriteria boundarySearchCriteria , RequestInfo requestInfo) {
        log.trace("searchBoundary method invoked");
        log.info("Starting boundary search, tenantId={}", boundarySearchCriteria.getTenantId());
        log.debug("Boundary search criteria: tenantId={}, codes count={}, offset={}, limit={}", 
                boundarySearchCriteria.getTenantId(),
                boundarySearchCriteria.getCodes() != null ? boundarySearchCriteria.getCodes().size() : 0,
                boundarySearchCriteria.getOffset(),
                boundarySearchCriteria.getLimit());

        // Search for boundary entity
        log.debug("Executing boundary search query");
        List<Boundary> boundaryList = repository.search(boundarySearchCriteria);
        log.debug("Boundary search query executed, found {} boundaries", boundaryList.size());

        // create response info
        log.debug("Creating response info");
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo , Boolean.TRUE);

        log.info("Boundary search completed successfully, found {} boundaries", boundaryList.size());
        return BoundaryResponse.builder()
                .boundary(boundaryList)
                .responseInfo(responseInfo)
                .build();
    }

    /** This method is used to process the update boundary entity request
     * @param boundaryRequest is the request object
     * @return boundaryResponse
     */
    public BoundaryResponse updateBoundary(BoundaryRequest boundaryRequest) {
        log.trace("updateBoundary method invoked");
        int boundaryCount = boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0;
        log.info("Starting boundary update process, boundary count={}", boundaryCount);
        log.debug("Boundary update request details: tenantId from first boundary={}", 
                boundaryCount > 0 && boundaryRequest.getBoundary().get(0) != null ? 
                boundaryRequest.getBoundary().get(0).getTenantId() : null);

        // validate the request
        log.debug("Validating boundary update request");
        boundaryEntityValidator.validateUpdateBoundaryRequest(boundaryRequest);
        log.debug("Boundary update request validation completed successfully");

        // enrich the request
        log.debug("Enriching boundary update request");
        BoundaryEntityEnricher.enrichUpdateBoundaryRequest(boundaryRequest);
        log.debug("Boundary update request enrichment completed");

        // create response
        log.debug("Creating boundary response");
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);
        log.debug("Boundary response created, boundary count in response={}", 
                boundaryResponse.getBoundary() != null ? boundaryResponse.getBoundary().size() : 0);

        // delegating the request to repository to update the record in db
        log.info("Publishing boundary update request to Kafka");
        repository.update(boundaryRequest);
        log.info("Boundary update process completed successfully, updated {} boundaries", boundaryCount);

        return boundaryResponse;
    }


    public void buildFlatHierarchy(EnrichedBoundary boundary, List<FlatBoundaryResponse> result, List<String> path) {
        log.trace("buildFlatHierarchy method invoked, boundary code={}, path size={}", 
                boundary != null ? boundary.getCode() : null, path.size());
        path.add(boundary.getCode());
        log.debug("Added boundary code to path: {}, current path size={}", boundary.getCode(), path.size());

        if (boundary.getChildren() == null || boundary.getChildren().isEmpty()) {
            log.debug("Boundary has no children, creating flat response");
            FlatBoundaryResponse flat = getFlatBoundaryResponse(path);
            result.add(flat);
            log.debug("Added flat boundary response, total results={}", result.size());
        } else {
            log.debug("Processing {} children for boundary code={}", boundary.getChildren().size(), boundary.getCode());
            for (EnrichedBoundary child : boundary.getChildren()) {
                buildFlatHierarchy(child, result, new ArrayList<>(path));
            }
        }
    }

    @NotNull
    private FlatBoundaryResponse getFlatBoundaryResponse(List<String> path) {
        log.trace("getFlatBoundaryResponse method invoked, path size={}", path.size());
        FlatBoundaryResponse flat = new FlatBoundaryResponse();

        String country = getOrNull(path, 0);
        String state = getOrNull(path, 1);
        String district = getOrNull(path, 2);
        String block = getOrNull(path, 3);

        log.debug("Extracted hierarchy levels: country={}, state={}, district={}, block={}", 
                country, state, district, block);

        flat.setCountry(country);
        flat.setState(state);
        flat.setDistrict(district);
        flat.setBlock(block);

        flat.setCode(block);
        return flat;
    }

    private String getOrNull(List<String> list, int index) {
        return index < list.size() ? list.get(index) : null;
    }

    public void createMdmsStateInfo(BoundaryRequest request){
        for (Boundary boundary : request.getBoundary()){
            boolean isStateBoundaryType = hierarchyUtil.isValidStateBoundaryFormat(boundary.getCode());
            if(!isStateBoundaryType)
                continue;

            String stateCode = boundary.getStateCode()!=null && !boundary.getStateCode().isEmpty() ? boundary.getStateCode() : hierarchyUtil.boundaryCodeToCode(boundary.getCode());
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            String name = hierarchyUtil.boundaryCodeToName(boundary.getCode());
            node.put("code", stateCode);
            node.put("name", name);
            node.put("active", true);
            node.put("boundaryCode", boundary.getCode());
            Mdms mdms = Mdms.builder()
                    .tenantId(boundary.getTenantId())
                    .schemaCode(MDMS_COMMON_MASTERS_MODULE_NAME+"."+MASTER_STATE_INFO)
                    .isActive(true)
                    .data(node)
                    .build();
            MdmsRequest mdmsRequest = MdmsRequest.builder().requestInfo(request.getRequestInfo()).mdms(mdms).build();
            boundaryEntityValidator.createStateInfoData(mdmsRequest);
        }
    }
}
