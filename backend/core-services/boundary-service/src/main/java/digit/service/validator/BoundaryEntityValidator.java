package digit.service.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import digit.config.ApplicationProperties;
import digit.constants.BoundaryConstants;
import digit.errors.ErrorCodes;
import digit.repository.impl.BoundaryRepositoryImpl;
import digit.service.ServiceRequestRepository;
import digit.util.GeoUtil;
import digit.util.HierarchyUtil;
import digit.util.MDMSUtils;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static digit.constants.BoundaryConstants.MASTER_STATE_INFO;
import static digit.constants.BoundaryConstants.MDMS_COMMON_MASTERS_MODULE_NAME;

@Component
public class BoundaryEntityValidator {

    private final ObjectMapper objectMapper;

    private final BoundaryRepositoryImpl boundaryRepository;
    private final MDMSUtils mdmsUtils;
    private final ApplicationProperties config;
    private final ObjectMapper mapper;
    private final ServiceRequestRepository serviceRequestRepository;

    private HierarchyUtil hierarchyUtil;

    public BoundaryEntityValidator(ObjectMapper objectMapper, BoundaryRepositoryImpl boundaryRepository, MDMSUtils mdmsUtils, ApplicationProperties config, ObjectMapper mapper, ServiceRequestRepository serviceRequestRepository, HierarchyUtil hierarchyUtil) {
        this.objectMapper = objectMapper;
        this.boundaryRepository = boundaryRepository;
        this.mdmsUtils = mdmsUtils;
        this.config = config;
        this.mapper = mapper;
        this.serviceRequestRepository = serviceRequestRepository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * This method performs various validation for the boundary entity create request
     * @1. Validate for valid geometry
     * @2. Validate for unique tenantId and code
     * @3. Validate for unique boundaries in the request
     * @param boundaryRequest
     */
    public void validateCreateBoundaryRequest(BoundaryRequest boundaryRequest) {
        // Check if new state code code or state boundary code already exist in MDMS common-master.StateInfo module. Only call for state
        validateStateCode(boundaryRequest);

        // validate the geometry
        validateBoundaryGeometry(boundaryRequest.getBoundary());

        // validate for duplicate tenantId and code in the request
        checkForDuplicatesInDB(boundaryRequest);

        // validate for unique boundaries in the request
        checkForDuplicatesInRequest(boundaryRequest);
    }

    /**
     * This method is used to validate the update boundary entity request
     * @param boundaryRequest
     */
    public void validateUpdateBoundaryRequest(BoundaryRequest boundaryRequest) {

        // validate for code and tenantId to exist
        validateIfBoundaryEntityExist(boundaryRequest);

        // validate for valid geometry
        validateBoundaryGeometry(boundaryRequest.getBoundary());
    }

    /**
     * This method takes a list of boundary entities and validates geometry of
     * the boundary depending on its type.
     * @param boundaryList
     */
    private void validateBoundaryGeometry(List<Boundary> boundaryList) {

        boundaryList.forEach(boundary -> {
            // Only execute if geometry is present
            if (!boundary.getGeometry().isNull()) {
                try {
                    if (boundary.getGeometry().get(BoundaryConstants.TYPE).asText().equals(BoundaryConstants.POINT)) {
                        GeoUtil.validatePointGeometry(objectMapper.treeToValue(boundary.getGeometry(), PointGeometry.class));

                    } else if (boundary.getGeometry().get(BoundaryConstants.TYPE).asText().equals(BoundaryConstants.POLYGON)) {
                        GeoUtil.validatePolygonGeometry(objectMapper.treeToValue(boundary.getGeometry(), PolygonGeometry.class));

                    } else {
                        throw new CustomException(ErrorCodes.INVALID_GEOMETRY_TYPE_CODE, ErrorCodes.INVALID_GEOMETRY_TYPE_MSG);

                    }
                } catch (JsonProcessingException e) {

                    throw new CustomException(ErrorCodes.INVALID_GEOJSON_CODE, ErrorCodes.INVALID_GEOJSON_MSG);
                }
            }
        });

    }

    /**
     * This method is used to create a map of tenantId to code from the request
     * @param boundaryRequest
     * @return
     */
    public Map<String, Set<String>> createTenantIdtoCodeMap(BoundaryRequest boundaryRequest) {
        return boundaryRequest.getBoundary().stream()
                .collect(Collectors.groupingBy(Boundary::getTenantId, Collectors.mapping(Boundary::getCode, Collectors.toSet())));
    }

    /**
     * This method is used to validate the uniqueness of tenantId and code in the request
     * @param boundaryRequest
     */
    public void checkForDuplicatesInDB(BoundaryRequest boundaryRequest) {

        // create a map of tenantId to code from request
        Map<String, Set<String>> tenantIdToCodeMap = createTenantIdtoCodeMap(boundaryRequest);

        tenantIdToCodeMap.forEach((tenantId, codes) -> {

            // get the list of boundaries with the given tenantId and codes
            List<Boundary> boundaryList = boundaryRepository.search( BoundarySearchCriteria.builder()
                            .tenantId(tenantId)
                            .codes(new ArrayList<>(codes))
                            .limit(codes.size())
                            .build());

            // check if the code already exists in db
            if (!CollectionUtils.isEmpty(boundaryList)) {
                throw new CustomException(ErrorCodes.DUPLICATE_CODE_CODE , ErrorCodes.DUPLICATE_CODE_MSG + BoundaryConstants.OPENING_BRACKET + tenantId + "," + codes + BoundaryConstants.CLOSING_BRACKET);
            }
        });
    }

    /**
     * This method is used to validate if the code and tenantId exist in the db before updating
     * @param boundaryRequest
     */
    public void validateIfBoundaryEntityExist(BoundaryRequest boundaryRequest) {

            // create a map of tenantId to code from request
            Map<String, Set<String>> tenantIdToCodeMap = createTenantIdtoCodeMap(boundaryRequest);

            tenantIdToCodeMap.forEach((tenantId, codes) -> {

                // get the list of boundaries for a given tenantId and codes from db
                List<Boundary> boundaryList = boundaryRepository.search(BoundarySearchCriteria.builder()
                        .tenantId(tenantId)
                        .codes(new ArrayList<>(codes))
                        .limit(codes.size())
                        .build());

                // check if the code does not exists in db
                if (boundaryList.size() != codes.size()) {
                        throw new CustomException(ErrorCodes.NOT_FOUND_CODE_AND_TENANT_ID_CODE , ErrorCodes.NOT_FOUND_CODE_AND_TENANT_ID_MSG + BoundaryConstants.OPENING_BRACKET + tenantId + "," + codes + BoundaryConstants.CLOSING_BRACKET );
                }
            });
    }

    /**
     * This method checks for unique boundaries in the request
     * @param boundaryRequest
     */
    public void checkForDuplicatesInRequest(BoundaryRequest boundaryRequest) {

        Set<Boundary> boundarySet = new HashSet<>(boundaryRequest.getBoundary());

        // check if the size of the set is not equal to the size of the list then there are duplicates
        if (boundarySet.size() != boundaryRequest.getBoundary().size()) {
            throw new CustomException(ErrorCodes.DUPLICATE_BOUNDARY_CODE, ErrorCodes.DUPLICATE_BOUNDARY_MSG);
        }
    }

    private void validateStateCode(BoundaryRequest request){
        for (Boundary boundary : request.getBoundary()){
            boolean isStateBoundaryType = hierarchyUtil.hasOnlyCountryAndState(boundary.getAdditionalDetails());
            if(!isStateBoundaryType)
                continue;

            String stateBoundaryCode = boundary.getCode();
            String stateCode = boundary.getStateCode()!=null && !boundary.getStateCode().isEmpty() ? boundary.getStateCode() : hierarchyUtil.boundaryCodeToCode(boundary.getCode());
            Object mdmsData = mdmsUtils.mDMSCall(request.getRequestInfo(), boundary.getTenantId());
            String mdmsRes = "$.MdmsRes.";
            final String jsonPathForStateInfo = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_STATE_INFO;
            List<Object> stateInfoRes = null;
            stateInfoRes = JsonPath.read(mdmsData, jsonPathForStateInfo);
            for (Object map : stateInfoRes) {
                LinkedHashMap<String, Object> stateInfo = (LinkedHashMap<String, Object>) map;
                String boundaryCode = (String) stateInfo.get("boundaryCode");
                String code = (String) stateInfo.get("code");
                if ((stateBoundaryCode!=null && stateBoundaryCode.equalsIgnoreCase(boundaryCode))) {
                    throw new CustomException("STATE_BOUNDARY_CODE_EXIST", "The State boundary code already exist: " + boundary.getCode());
                }
                if ((stateBoundaryCode!=null && stateBoundaryCode.equalsIgnoreCase(boundaryCode)) || (stateCode!=null && stateCode.equalsIgnoreCase(code))) {
                    throw new CustomException("STATE_BOUNDARY_CODE_EXIST", "The State code already exist: " + boundary.getStateCode());
                }
            }
        }
    }


    public MdmsResponseV2 createStateInfoData(Object request) {
        String url = config.getMdmsHost() + config.getMdmsCreateEndPoint();
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        MdmsResponseV2 mdmsResponse = mapper.convertValue(response, MdmsResponseV2.class);
        if (mdmsResponse == null || mdmsResponse.getMdms() == null || mdmsResponse.getMdms().isEmpty()) {
            return null;
        }
        return mdmsResponse;
    }
}
