package digit.web.controllers;

import digit.service.BoundaryService;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/boundary")
public class BoundaryController {

    private final BoundaryService boundaryService;

    @Autowired
    public BoundaryController(BoundaryService boundaryService) {
        this.boundaryService = boundaryService;
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
    public ResponseEntity<PaginatedBoundaryResponse> getAllBoundaries(
            @RequestParam(required = false) List<String> parentBoundaryCodes,
            @RequestParam(required = false) String boundaryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        BoundarySearchCriteria criteria = new BoundarySearchCriteria();
        criteria.setCodes(parentBoundaryCodes);
        criteria.setBoundaryType(boundaryType);
        criteria.setOffset(page * size);
        criteria.setLimit(size);

        PaginatedBoundaryResponse response = boundaryService.getAllBoundaries(criteria);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
