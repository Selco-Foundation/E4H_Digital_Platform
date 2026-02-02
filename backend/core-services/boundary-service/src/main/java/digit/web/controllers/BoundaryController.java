package digit.web.controllers;

import digit.service.BoundaryRelationshipService;
import digit.service.BoundaryService;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/boundary")
@Slf4j
public class BoundaryController {

    private final BoundaryService boundaryService;
    private final BoundaryRelationshipService boundaryRelationshipService;

    @Autowired
    public BoundaryController(BoundaryService boundaryService, BoundaryRelationshipService boundaryRelationshipService) {
        this.boundaryService = boundaryService;
        this.boundaryRelationshipService = boundaryRelationshipService;
    }

    /**
     * Request handler for serving boundary entities create request.
     * @param body
     * @return
     */
    @PostMapping(value = "/_create")
    public ResponseEntity<BoundaryResponse> create(@Valid @RequestBody BoundaryRequest body) {
        BoundaryResponse boundaryResponse = boundaryService.createBoundary(body);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving boundary entities search request.
     * @param boundarySearchCriteria
     * @param requestInfo
     * @return
     */
    @PostMapping(value = "/_search")
    public ResponseEntity<BoundaryResponse> search(@Valid @ModelAttribute BoundarySearchCriteria boundarySearchCriteria, @RequestBody RequestInfo requestInfo) {
        BoundaryResponse boundaryResponse = boundaryService.searchBoundary(boundarySearchCriteria,requestInfo);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary entities search request.
     * @param boundaryV2SearchRequest
     * @return
     */
    @PostMapping(value = "/v2/_search")
    public ResponseEntity<BoundaryResponse> searchWithRequestBody(@Valid @RequestBody BoundaryV2SearchRequest boundaryV2SearchRequest) {
        BoundaryResponse boundaryResponse = boundaryService.searchBoundary(
                boundaryV2SearchRequest.getBoundarySearchCriteria() , boundaryV2SearchRequest.getRequestInfo()
        );
        return new ResponseEntity<>(boundaryResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary entities update request.
     * @param body
     * @return
     */
    @PostMapping(value = "/_update")
    public ResponseEntity<BoundaryResponse> update(@Valid @RequestBody BoundaryRequest body) {
        BoundaryResponse boundaryResponse = boundaryService.updateBoundary(body);
        return new ResponseEntity<>(boundaryResponse,HttpStatus.ACCEPTED);
    }

    @GetMapping("/getAllBoundaries")
    public ResponseEntity<List<FlatBoundaryResponse>> getAllBoundaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String tenantId,
            @RequestParam String hierarchyType,
            @RequestParam String boundaryType,
            @RequestBody RequestInfo requestInfo) {

        BoundaryRelationshipSearchCriteria criteria = new BoundaryRelationshipSearchCriteria();
        criteria.setTenantId(tenantId);
        criteria.setHierarchyType(hierarchyType);
        criteria.setIncludeChildren(false);
        criteria.setIncludeParents(true);
        criteria.setBoundaryType(boundaryType);

        BoundarySearchResponse response = boundaryRelationshipService.getBoundaryRelationships(criteria, requestInfo);
        log.info(String.valueOf(response));

        List<FlatBoundaryResponse> flatList = new ArrayList<>();
        for (HierarchyRelation tenantBoundary : response.getTenantBoundary()) {
            for (EnrichedBoundary country : tenantBoundary.getBoundary()) {
                boundaryService.buildFlatHierarchy(country, flatList, new ArrayList<>());
            }
        }

        // Paginate
        int start = page * size;
        int end = Math.min(start + size, flatList.size());
        List<FlatBoundaryResponse> paginated = (start < flatList.size()) ? flatList.subList(start, end) : Collections.emptyList();

        return ResponseEntity.ok(paginated);
    }

    @PostMapping("/v2/getAllBoundaries")
    public ResponseEntity<BoundaryRelationshipV2Response> getAllBoundariesV2(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit, @Valid @RequestBody BoundaryRelationshipSearchRequest criteria) {

        criteria.getCriteria().setIncludeChildren(false);
        criteria.getCriteria().setIncludeParents(true);
        criteria.getCriteria().setLimit(limit);
        criteria.getCriteria().setOffset(offset);

        BoundarySearchResponse response = boundaryRelationshipService.getBoundaryRelationships(criteria.getCriteria(), null);
        log.info(String.valueOf(response));
        Integer count = boundaryRelationshipService.countBoundaryRelationships(criteria.getCriteria());
        List<FlatBoundaryResponse> flatList = new ArrayList<>();
        for (HierarchyRelation tenantBoundary : response.getTenantBoundary()) {
            for (EnrichedBoundary country : tenantBoundary.getBoundary()) {
                boundaryService.buildFlatHierarchy(country, flatList, new ArrayList<>());
            }
        }

        // Paginate
        int start = offset * limit;
        int end = Math.min(start + limit, flatList.size());
        List<FlatBoundaryResponse> paginated = (start < flatList.size()) ? flatList.subList(start, end) : Collections.emptyList();

        BoundaryRelationshipV2Response response1 = BoundaryRelationshipV2Response.builder()
                .responseInfo(null)
                .totalCount(criteria.getCriteria().getCodes() !=null ? flatList.size() : count)
                .paginated(paginated)
                .build();

        return ResponseEntity.ok(response1);
    }


}
