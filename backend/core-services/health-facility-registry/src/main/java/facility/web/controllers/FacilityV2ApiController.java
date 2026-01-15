package facility.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import facility.service.FacilityService;
import facility.web.models.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("/v2/facility")
@Slf4j
public class FacilityV2ApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final FacilityService facilityService;

    @Autowired
    public FacilityV2ApiController(ObjectMapper objectMapper, HttpServletRequest request, FacilityService facilityService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.facilityService = facilityService;
    }

    @PostMapping("/create")
    public ResponseEntity<List<Facility>> createFacility(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Health facility data to add to the registry",
                    required = true
            )
            @Valid @RequestBody FacilityCreateRequest facilityCreateRequest) {
        log.trace("Entering createFacility endpoint");
        int facilityCount = facilityCreateRequest.getFacilities() != null ? facilityCreateRequest.getFacilities().size() : 0;
        log.info("Received facility create request for {} facilities", facilityCount);

        List<Facility> facility = facilityService.createFacility(facilityCreateRequest);
        if (facility != null) {
            log.info("Successfully created {} facilities, returning CREATED status", facility.size());
            log.trace("Exiting createFacility endpoint");
            return ResponseEntity.status(HttpStatus.CREATED).body(facility);
        } else {
            log.warn("Facility creation returned null, returning BAD_REQUEST status");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @RequestMapping(value = "/assessment/create", method = RequestMethod.POST)
    public ResponseEntity<FacilityAssessment> createHFAssessment(@Parameter(in = ParameterIn.DEFAULT, description = "Health facility assessment data created", required = true, schema = @Schema()) @Valid @RequestBody FacilityAssessmentCreateRequest body) {
        log.trace("Entering createHFAssessment endpoint");
        log.warn("createHFAssessment endpoint is not implemented, returning NOT_IMPLEMENTED");
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                log.debug("Accept header contains application/json, returning mock response");
                return new ResponseEntity<FacilityAssessment>(objectMapper.readValue("{  \"tenant_id\" : \"state1.phc1\",  \"date_of_assessment\" : \"\",  \"assessed_by\" : \"\",  \"rowVersion\" : \"\",  \"final_result\" : \"GO\",  \"facility_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_type\" : \"\",  \"isActive\" : \"\"}", FacilityAssessment.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Error parsing mock assessment data: {}", e.getMessage(), e);
                return new ResponseEntity<FacilityAssessment>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        log.trace("Exiting createHFAssessment endpoint");
        return new ResponseEntity<FacilityAssessment>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ResponseEntity<FacilitySummary> getFacilitiesSummary(@Parameter(in = ParameterIn.PATH, description = "System generated unique identifier for a PHC", required = true, schema = @Schema()) @PathVariable("facilityId") String facilityId) {
        log.trace("Entering getFacilitiesSummary endpoint");
        log.info("Fetching facility summary for facilityId: {}", facilityId);
        FacilitySummary summary = facilityService.getFacilitySummary(facilityId);
        if (summary != null) {
            log.info("Successfully retrieved facility summary for facilityId: {}", facilityId);
        } else {
            log.warn("Facility summary not found for facilityId: {}", facilityId);
        }
        log.trace("Exiting getFacilitiesSummary endpoint");
        return new ResponseEntity<FacilitySummary>(summary, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<Facility> updateFacility(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Health facility data with updates (facility_id must be provided in the payload)",
                    required = true
            )
            @Valid @RequestBody FacilityUpdateRequest facilityUpdateRequest) {
        log.trace("Entering updateFacility endpoint");
        String facilityId = facilityUpdateRequest.getFacilityUpdate() != null 
                ? facilityUpdateRequest.getFacilityUpdate().getFacilityId() : null;
        log.info("Received facility update request for facilityId: {}", facilityId);

        Facility updated = facilityService.updateFacility(facilityUpdateRequest);
        if (updated != null) {
            log.info("Successfully updated facility: {}", facilityId);
            log.trace("Exiting updateFacility endpoint");
            return ResponseEntity.ok(updated);
        } else {
            log.warn("Facility not found for update, facilityId: {}", facilityId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @RequestMapping(value = "/assessment/_update", method = RequestMethod.POST)
    public ResponseEntity<FacilityAssessment> updateHFAssessment(@Parameter(in = ParameterIn.DEFAULT, description = "Health facility assessment data updated", required = true, schema = @Schema()) @Valid @RequestBody FacilityAssessmentCreateRequest body) {
        log.trace("Entering updateHFAssessment endpoint");
        log.warn("updateHFAssessment endpoint is not implemented, returning NOT_IMPLEMENTED");
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                log.debug("Accept header contains application/json, returning mock response");
                return new ResponseEntity<FacilityAssessment>(objectMapper.readValue("{  \"tenant_id\" : \"state1.phc1\",  \"date_of_assessment\" : \"\",  \"assessed_by\" : \"\",  \"rowVersion\" : \"\",  \"final_result\" : \"GO\",  \"facility_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_type\" : \"\",  \"isActive\" : \"\"}", FacilityAssessment.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Error parsing mock assessment data: {}", e.getMessage(), e);
                return new ResponseEntity<FacilityAssessment>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        log.trace("Exiting updateHFAssessment endpoint");
        return new ResponseEntity<FacilityAssessment>(HttpStatus.NOT_IMPLEMENTED);
    }


    @GetMapping("/search")
    public ResponseEntity<FacilitySearchResponse> searchFacilities(
            @ModelAttribute FacilitySearchRequest searchRequest) {
        log.trace("Entering searchFacilities endpoint");
        log.info("Received facility search request with limit={}, offset={}", 
                searchRequest.getLimit(), searchRequest.getOffset());
        List<Facility> facilities = facilityService.searchFacilities(searchRequest);
        int totalCount = facilityService.countFacilities(searchRequest);
        log.info("Search completed: found {} facilities out of {} total", facilities.size(), totalCount);
        log.trace("Exiting searchFacilities endpoint");
        return ResponseEntity.ok(new FacilitySearchResponse(facilities, totalCount));
    }


    @PostMapping("/_bulk-search")
    public ResponseEntity<FacilitySearchResponse> bulkSearchFacilities(
            @RequestBody FacilityBulkSearchRequest searchRequest
    ) {
        log.trace("Entering bulkSearchFacilities endpoint");
        FacilityBulkSearchCriteria criteria = searchRequest.getFacilityBulkSearchCriteria();
        int criteriaCount = criteria != null ? 
                (criteria.getTenantIds() != null ? criteria.getTenantIds().size() : 0) : 0;
        log.info("Received bulk facility search request with {} tenant criteria", criteriaCount);
        List<Facility> facilities = facilityService.bulkSearchFacilities(searchRequest);
        int totalCount = facilityService.countFacilitiesForBulkSearch(searchRequest);
        log.info("Bulk search completed: found {} facilities out of {} total", facilities.size(), totalCount);
        log.trace("Exiting bulkSearchFacilities endpoint");
        return ResponseEntity.ok(new FacilitySearchResponse(facilities, totalCount));
    }



}
