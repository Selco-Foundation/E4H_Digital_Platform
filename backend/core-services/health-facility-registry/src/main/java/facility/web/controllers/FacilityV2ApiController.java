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

        List<Facility> facility = facilityService.createFacility(facilityCreateRequest);
        if (facility != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(facility);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @RequestMapping(value = "/assessment/create", method = RequestMethod.POST)
    public ResponseEntity<FacilityAssessment> createHFAssessment(@Parameter(in = ParameterIn.DEFAULT, description = "Health facility assessment data created", required = true, schema = @Schema()) @Valid @RequestBody FacilityAssessmentCreateRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<FacilityAssessment>(objectMapper.readValue("{  \"tenant_id\" : \"state1.phc1\",  \"date_of_assessment\" : \"\",  \"assessed_by\" : \"\",  \"rowVersion\" : \"\",  \"final_result\" : \"GO\",  \"facility_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_type\" : \"\",  \"isActive\" : \"\"}", FacilityAssessment.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<FacilityAssessment>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<FacilityAssessment>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ResponseEntity<FacilitySummary> getFacilitiesSummary(@Parameter(in = ParameterIn.PATH, description = "System generated unique identifier for a PHC", required = true, schema = @Schema()) @PathVariable("facilityId") String facilityId) {
        return new ResponseEntity<FacilitySummary>(
                facilityService.getFacilitySummary(facilityId),
                HttpStatus.OK
        );
    }

    @PostMapping("/update")
    public ResponseEntity<Facility> updateFacility(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Health facility data with updates (facility_id must be provided in the payload)",
                    required = true
            )
            @Valid @RequestBody FacilityUpdateRequest facilityUpdateRequest) {

        Facility updated = facilityService.updateFacility(facilityUpdateRequest);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @RequestMapping(value = "/assessment/_update", method = RequestMethod.POST)
    public ResponseEntity<FacilityAssessment> updateHFAssessment(@Parameter(in = ParameterIn.DEFAULT, description = "Health facility assessment data updated", required = true, schema = @Schema()) @Valid @RequestBody FacilityAssessmentCreateRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<FacilityAssessment>(objectMapper.readValue("{  \"tenant_id\" : \"state1.phc1\",  \"date_of_assessment\" : \"\",  \"assessed_by\" : \"\",  \"rowVersion\" : \"\",  \"final_result\" : \"GO\",  \"facility_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"assessment_type\" : \"\",  \"isActive\" : \"\"}", FacilityAssessment.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<FacilityAssessment>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<FacilityAssessment>(HttpStatus.NOT_IMPLEMENTED);
    }


    @GetMapping("/search")
    public ResponseEntity<FacilitySearchResponse> searchFacilities(
            @ModelAttribute FacilitySearchRequest searchRequest) {
        List<Facility> facilities = facilityService.searchFacilities(searchRequest);
        int totalCount = facilityService.countFacilities(searchRequest);
        return ResponseEntity.ok(new FacilitySearchResponse(facilities, totalCount));
    }


    @PostMapping("/_bulk-search")
    public ResponseEntity<FacilitySearchResponse> bulkSearchFacilities(
            @RequestBody FacilityBulkSearchRequest searchRequest
    ) {
        List<Facility> facilities = facilityService.bulkSearchFacilities(searchRequest);
        int totalCount = facilityService.countFacilitiesForBulkSearch(searchRequest);
        return ResponseEntity.ok(new FacilitySearchResponse(facilities, totalCount));
    }

    @GetMapping("/migrate_data")
    public String migrateFacilityDB() {
        facilityService.migrateFacilityData();
        return "Script done";
    }



}
