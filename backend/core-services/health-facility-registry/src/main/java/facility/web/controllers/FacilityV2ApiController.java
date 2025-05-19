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
@RequestMapping("")
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

    @PostMapping("/v2/facility/create")
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


    @RequestMapping(value = "/v2/facility/assessment/create", method = RequestMethod.POST)
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

    @RequestMapping(value = "/v2/facility/summary", method = RequestMethod.GET)
    public ResponseEntity<FacilitySummary> getFacilitiesSummary(@Parameter(in = ParameterIn.PATH, description = "System generated unique identifier for a PHC", required = true, schema = @Schema()) @PathVariable("facilityId") Object facilityId) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<FacilitySummary>(objectMapper.readValue("{  \"summary\" : \"\"}", FacilitySummary.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<FacilitySummary>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<FacilitySummary>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/v2/facility/update")
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


    @RequestMapping(value = "/v2/facility/assessment/_update", method = RequestMethod.POST)
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


    @GetMapping("/v2/facility/search")
    public ResponseEntity<List<Facility>> searchFacilities(
            @Parameter(description = "Tenant ID to filter facilities")
            @RequestParam(value = "tenant_id", required = false) String tenantId,

            @Parameter(description = "Facility ID to filter a specific facility")
            @RequestParam(value = "facility_id", required = false) String facilityId,

            @Parameter(description = "Facility name to search by (partial or full match)")
            @RequestParam(value = "facility_name", required = false) String facilityName,

            @Parameter(description = "Boundary code to filter facilities by location")
            @RequestParam(value = "boundary_code", required = false) String boundaryCode,

            @Parameter(description = "HFR ID to filter facilities")
            @RequestParam(value = "hfr_id", required = false) String hfrId,

            @Parameter(description = "NIN ID to filter facilities")
            @RequestParam(value = "nin_id", required = false) String ninId,

            @Parameter(description = "Maximum number of results to return")
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,

            @Parameter(description = "Number of results to skip for pagination")
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        List<Facility> facilities = facilityService.searchFacilities(
                tenantId, facilityId, facilityName, hfrId, ninId, boundaryCode, limit, offset
        );
        return ResponseEntity.ok(facilities);
    }


}
