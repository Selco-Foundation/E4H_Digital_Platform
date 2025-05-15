package facility_v2.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import facility_v2.web.models.*;
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

@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class V2ApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    public V2ApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @RequestMapping(value = "/v2/facility/create", method = RequestMethod.POST)
    public ResponseEntity<Facility> createFacility(@Parameter(in = ParameterIn.DEFAULT, description = "Health facility data to add to the registry", required = true, schema = @Schema()) @Valid @RequestBody FacilityCreateRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Facility>(objectMapper.readValue("{  \"tenant_id\" : \"state1.phc1\",  \"address\" : {    \"pincode\" : \"\",    \"city\" : \"\",    \"latitude\" : \"\",    \"tenantId\" : \"\",    \"addressNumber\" : \"\",    \"addressLine1\" : \"\",    \"addressLine2\" : \"\",    \"detail\" : \"\",    \"landmark\" : \"\",    \"longitude\" : \"\",    \"addressId\" : \"\"  },  \"additionalDetails\" : \"\",  \"isActive\" : \"\",  \"facility_name\" : \"Gejjalgetta PHC\",  \"facility_details\" : \"\",  \"facility_region\" : \"RURAL\",  \"facility_category\" : \"\",  \"facility_ownership\" : \"\",  \"facility_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"facility_type\" : \"\",  \"wfStatus\" : \"\",  \"facility_subtype\" : \"\"}", Facility.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<Facility>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Facility>(HttpStatus.NOT_IMPLEMENTED);
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

    @RequestMapping(value = "/v2/facility/search", method = RequestMethod.GET)
    public ResponseEntity<Object> searchFacilities(@Parameter(in = ParameterIn.QUERY, description = "Filter facilities by tenant", schema = @Schema()) @Valid @RequestParam(value = "tenant_id", required = false) Object tenantId, @Parameter(in = ParameterIn.QUERY, description = "Filter facilities by id", schema = @Schema()) @Valid @RequestParam(value = "facility_id", required = false) Object facilityId, @Parameter(in = ParameterIn.QUERY, description = "Filter facilities by name (partial or full match)", schema = @Schema()) @Valid @RequestParam(value = "facility_name", required = false) Object facilityName, @Parameter(in = ParameterIn.QUERY, description = "Filter facilities by HFR Id", schema = @Schema()) @Valid @RequestParam(value = "hfr_id", required = false) Object hfrId, @Parameter(in = ParameterIn.QUERY, description = "Filter facilities by NIN id", schema = @Schema()) @Valid @RequestParam(value = "nin_id", required = false) Object ninId) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Object>(objectMapper.readValue("\"\"", Object.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Object>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/v2/facility/update", method = RequestMethod.POST)
    public ResponseEntity<Facility> updateFacility(@Parameter(in = ParameterIn.DEFAULT, description = "Health facility data with updates (facility_id must be provided in the payload)", required = true, schema = @Schema()) @Valid @RequestBody FacilityUpdateRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Facility>(objectMapper.readValue("{  \"tenant_id\" : \"state1.phc1\",  \"address\" : {    \"pincode\" : \"\",    \"city\" : \"\",    \"latitude\" : \"\",    \"tenantId\" : \"\",    \"addressNumber\" : \"\",    \"addressLine1\" : \"\",    \"addressLine2\" : \"\",    \"detail\" : \"\",    \"landmark\" : \"\",    \"longitude\" : \"\",    \"addressId\" : \"\"  },  \"additionalDetails\" : \"\",  \"isActive\" : \"\",  \"facility_name\" : \"Gejjalgetta PHC\",  \"facility_details\" : \"\",  \"facility_region\" : \"RURAL\",  \"facility_category\" : \"\",  \"facility_ownership\" : \"\",  \"facility_id\" : \"44e128a5-ac7a-4c9a-be4c-224b6bf81b20\",  \"facility_type\" : \"\",  \"wfStatus\" : \"\",  \"facility_subtype\" : \"\"}", Facility.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<Facility>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Facility>(HttpStatus.NOT_IMPLEMENTED);
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

}
