package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import org.egov.field_planner.repository.AssessmentFacilityRepository;
import org.egov.field_planner.web.models.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EligibleFacilitiesService {

    private final AssessmentFacilityRepository facilityRepository;

    public EligibleFacilitiesSearchResponse search(EligibleFacilitiesSearchRequest request) {
        List<EligibleFacility> facilities = facilityRepository.findEligibleFacilities(
                request.getProjectId(), request.getAssessmentPlanIds());
        return EligibleFacilitiesSearchResponse.builder()
                .facilities(facilities)
                .total(facilities.size())
                .build();
    }

    public EligibleFacilitiesSearchResponse searchPassedFacilities(PassedFacilitiesSearchRequest request) {
        return search(EligibleFacilitiesSearchRequest.builder()
                .requestInfo(request.getRequestInfo())
                .projectId(request.getProjectId())
                .tenantId(request.getTenantId())
                .assessmentPlanIds(List.of(request.getPlanId()))
                .build());
    }
}
