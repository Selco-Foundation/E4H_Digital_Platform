package digit.service;

import digit.repository.impl.BoundaryRepositoryImpl;
import digit.service.enrichment.BoundaryEntityEnricher;
import digit.service.validator.BoundaryEntityValidator;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class BoundaryService {

    private final BoundaryEntityValidator boundaryEntityValidator;

    private final ResponseUtil responseUtil;

    private final BoundaryRepositoryImpl repository;

    public BoundaryService(BoundaryEntityValidator boundaryEntityValidator , ResponseUtil responseUtil,
                           BoundaryRepositoryImpl repository) {

        this.boundaryEntityValidator = boundaryEntityValidator;
        this.responseUtil = responseUtil;
        this.repository = repository;
    }

    /**
     * This method is used to process a boundary entity creation request
     * @param boundaryRequest is the request object
     * @return boundaryResponse
     */
    public BoundaryResponse createBoundary(BoundaryRequest boundaryRequest) {

        log.info("Received request to create boundary with {} records",
                boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0);

        // validate the request
        log.debug("Validating boundary create request: {}", boundaryRequest);
        boundaryEntityValidator.validateCreateBoundaryRequest(boundaryRequest);

        // enrich the request
        log.debug("Enriching boundary create request...");
        BoundaryEntityEnricher.enrichCreateBoundaryRequest(boundaryRequest);

        // create response
        log.debug("Creating boundary response for request...");
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);

        // delegating the request to repository to further persist in db
        log.info("Persisting boundary request to repository...");
        repository.create(boundaryRequest);

        log.info("Boundary creation successful for {} records",
                boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0);
        return boundaryResponse;
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    public BoundaryResponse searchBoundary(BoundarySearchCriteria boundarySearchCriteria , RequestInfo requestInfo) {
        log.info("Searching boundaries with criteria: {}", boundarySearchCriteria);


        // Search for boundary entity
        List<Boundary> boundaryList = repository.search(boundarySearchCriteria);
        log.info("Found {} boundary records for search criteria",
                boundaryList != null ? boundaryList.size() : 0);

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
        log.info("Received request to update boundary with {} records",
                boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0);

        // validate the request
        log.debug("Validating boundary update request: {}", boundaryRequest);
        boundaryEntityValidator.validateUpdateBoundaryRequest(boundaryRequest);

        // enrich the request
        log.debug("Enriching boundary update request...");
        BoundaryEntityEnricher.enrichUpdateBoundaryRequest(boundaryRequest);

        // create response
        log.debug("Creating boundary response for update request...");
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);

        // delegating the request to repository to update the record in db
        log.info("Updating boundary records in repository...");
        repository.update(boundaryRequest);

        log.info("Boundary update successful for {} records",
                boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0);
        return boundaryResponse;
    }


    public void buildFlatHierarchy(EnrichedBoundary boundary, List<FlatBoundaryResponse> result, List<String> path) {
        log.debug("Building flat hierarchy for boundary: {}", boundary.getCode());
        path.add(boundary.getCode());

        if (boundary.getChildren() == null || boundary.getChildren().isEmpty()) {
            FlatBoundaryResponse flat = getFlatBoundaryResponse(path);
            log.trace("Adding flat boundary response: {}", flat);
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
        log.debug("Generated FlatBoundaryResponse: {}", flat);
        return flat;
    }

    private String getOrNull(List<String> list, int index) {
        return index < list.size() ? list.get(index) : null;
    }
}
