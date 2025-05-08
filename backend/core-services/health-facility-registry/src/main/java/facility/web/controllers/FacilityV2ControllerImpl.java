package facility.web.controllers;

import facility.api.FacilityV2Controller;
import facility.service.FacilityService;
import facility.web.models.Facility;
import facility.web.models.FacilityCreateRequest;
import facility.web.models.FacilitySummary;
import facility.web.models.FacilityUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping
@Tag(name = "Facilities", description = "Endpoints related to the health facility registry")
public class FacilityV2ControllerImpl implements FacilityV2Controller {

    @Autowired
    private FacilityService facilityService;

    @Override
    public ResponseEntity<Facility> createFacility(@Valid @RequestBody FacilityCreateRequest facilityCreateRequest) {
        Facility facility = facilityService.createFacility(facilityCreateRequest);
        if (facility != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(facility);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Facility> updateFacility(@Valid @RequestBody FacilityUpdateRequest facilityUpdateRequest) {
        Facility updated = facilityService.updateFacility(facilityUpdateRequest);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<List<Facility>> searchFacilities(
            @RequestParam(value = "tenant_id", required = false) String tenantId,
            @RequestParam(value = "facility_id", required = false) String facilityId,
            @RequestParam(value = "facility_name", required = false) String facilityName,
            @RequestParam(value = "hfr_id", required = false) String hfrId,
            @RequestParam(value = "nin_id", required = false) String ninId) {

        List<Facility> facilities = facilityService.searchFacilities(tenantId, facilityId, facilityName, hfrId, ninId);
        return ResponseEntity.ok(facilities);
    }

    @Override
    public ResponseEntity<FacilitySummary> getFacilitiesSummary(@PathVariable("facilityId") String facilityId) {
        FacilitySummary summary = facilityService.getFacilitySummary(facilityId);
        if (summary != null) {
            return ResponseEntity.ok(summary);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
