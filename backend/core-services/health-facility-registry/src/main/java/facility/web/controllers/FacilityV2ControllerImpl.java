package facility.web.controllers;

import facility.api.FacilityV2Controller;
import facility.service.FacilityService;
import facility.web.models.Facility;
import facility.web.models.FacilityCreateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
