package org.egov.field_planner.service;

import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.AllowedActions;
import org.egov.field_planner.web.models.PlanFacility;
import org.springframework.stereotype.Service;

@Service
public class AllowedActionsService {

    public AllowedActions compute(PlanFacility facility) {
        if (facility == null) {
            return AllowedActions.builder().build();
        }
        boolean remotePending = isRemotePending(facility.getPhoneStatus());

        if (remotePending) {
            return AllowedActions.builder()
                    .assignForField(false)
                    .markEligible(false)
                    .markNotEligible(false)
                    .build();
        }

        if (AssessmentConstants.OVERALL_ELIGIBLE.equals(facility.getOverallStatus())) {
            return AllowedActions.builder()
                    .assignForField(false)
                    .markEligible(false)
                    .markNotEligible(true)
                    .build();
        }

        if (AssessmentConstants.OVERALL_NOT_ELIGIBLE.equals(facility.getOverallStatus())) {
            return AllowedActions.builder()
                    .assignForField(false)
                    .markEligible(true)
                    .markNotEligible(false)
                    .build();
        }

        boolean assignForField = canAssignForField(facility);
        return AllowedActions.builder()
                .assignForField(assignForField)
                .markEligible(true)
                .markNotEligible(true)
                .build();
    }

    private boolean canAssignForField(PlanFacility facility) {
        if (!AssessmentConstants.REMOTE_DONE_STATUSES.contains(facility.getPhoneStatus())) {
            return false;
        }
        if (StringUtils.isNotBlank(facility.getFieldStatus())) {
            return false;
        }
        return AssessmentConstants.OVERALL_PENDING.equals(facility.getOverallStatus());
    }

    private boolean isRemotePending(String phoneStatus) {
        return phoneStatus == null || AssessmentConstants.REMOTE_PENDING_STATUSES.contains(phoneStatus);
    }
}
