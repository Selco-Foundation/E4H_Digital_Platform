package digit.web.controllers;

import digit.service.BoundaryHierarchyDefinitionService;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/boundary-hierarchy-definition")
@Slf4j
public class HierarchyDefinitionController {

    private BoundaryHierarchyDefinitionService boundaryHierarchyDefinitionService;

    @Autowired
    public HierarchyDefinitionController(BoundaryHierarchyDefinitionService boundaryHierarchyDefinitionService) {
        this.boundaryHierarchyDefinitionService = boundaryHierarchyDefinitionService;
    }

    /**
     * Request handler for serving hierarchy definition create requests.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryTypeHierarchyResponse> create(@Valid @RequestBody BoundaryTypeHierarchyRequest body) {
        log.trace("create method invoked");
        log.info("Processing boundary hierarchy definition create request, tenantId={}, hierarchyType={}", 
                body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getTenantId() : null,
                body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getHierarchyType() : null);
        
        BoundaryTypeHierarchyResponse boundaryTypeHierarchyResponse = boundaryHierarchyDefinitionService.createBoundaryHierarchyDefinition(body);
        
        log.info("Boundary hierarchy definition create request processed successfully");
        return new ResponseEntity<>(boundaryTypeHierarchyResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving hierarchy definition search requests.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundaryTypeHierarchyResponse> search(@Valid @RequestBody BoundaryTypeHierarchySearchRequest body) {
        log.trace("search method invoked");
        log.info("Processing boundary hierarchy definition search request, tenantId={}, hierarchyType={}", 
                body.getBoundaryTypeHierarchySearchCriteria() != null ? body.getBoundaryTypeHierarchySearchCriteria().getTenantId() : null,
                body.getBoundaryTypeHierarchySearchCriteria() != null ? body.getBoundaryTypeHierarchySearchCriteria().getHierarchyType() : null);
        
        BoundaryTypeHierarchyResponse boundaryTypeHierarchyResponse = boundaryHierarchyDefinitionService.searchBoundaryHierarchyDefinition(body);
        
        log.info("Boundary hierarchy definition search request completed, found {} hierarchies", 
                boundaryTypeHierarchyResponse.getBoundaryHierarchy() != null ? boundaryTypeHierarchyResponse.getBoundaryHierarchy().size() : 0);
        return new ResponseEntity<>(boundaryTypeHierarchyResponse, HttpStatus.OK);
    }

}
