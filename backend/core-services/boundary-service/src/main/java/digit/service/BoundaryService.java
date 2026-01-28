package digit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import digit.repository.impl.BoundaryRepositoryImpl;
import digit.service.enrichment.BoundaryEntityEnricher;
import digit.service.validator.BoundaryEntityValidator;
import digit.util.HierarchyUtil;
import digit.util.ResponseUtil;
import digit.web.models.*;
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

        // validate the request
        boundaryEntityValidator.validateCreateBoundaryRequest(boundaryRequest);

        // enrich the request
        BoundaryEntityEnricher.enrichCreateBoundaryRequest(boundaryRequest);

        // create response
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);

        // delegating the request to repository to further persist in db
        repository.create(boundaryRequest);

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

        // Search for boundary entity
        List<Boundary> boundaryList = repository.search(boundarySearchCriteria);

        // create response info
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo , Boolean.TRUE);

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

        // validate the request
        boundaryEntityValidator.validateUpdateBoundaryRequest(boundaryRequest);

        // enrich the request
        BoundaryEntityEnricher.enrichUpdateBoundaryRequest(boundaryRequest);

        // create response
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);

        // delegating the request to repository to update the record in db
        repository.update(boundaryRequest);

        return boundaryResponse;
    }


    public void buildFlatHierarchy(EnrichedBoundary boundary, List<FlatBoundaryResponse> result, List<String> path) {
        path.add(boundary.getCode());

        if (boundary.getChildren() == null || boundary.getChildren().isEmpty()) {
            FlatBoundaryResponse flat = getFlatBoundaryResponse(path);
            result.add(flat);
        } else {
            for (EnrichedBoundary child : boundary.getChildren()) {
                buildFlatHierarchy(child, result, new ArrayList<>(path));
            }
        }
    }

    @NotNull
    private FlatBoundaryResponse getFlatBoundaryResponse(List<String> path) {
        FlatBoundaryResponse flat = new FlatBoundaryResponse();

        String country = getOrNull(path, 0);
        String state = getOrNull(path, 1);
        String district = getOrNull(path, 2);
        String block = getOrNull(path, 3);

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
