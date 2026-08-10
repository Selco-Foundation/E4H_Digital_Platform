package org.egov.field_planner.util;

import org.egov.field_planner.web.models.AssessmentAuditEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AssessmentAdditionalDetailsHelper {

    private static final String ASSESSMENT_KEY = "assessment";
    private static final String AUDIT_TRAIL_KEY = "auditTrail";

    private AssessmentAdditionalDetailsHelper() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getAssessmentSection(Map<String, Object> additionalDetails) {
        if (additionalDetails == null) {
            return new HashMap<>();
        }
        Object section = additionalDetails.get(ASSESSMENT_KEY);
        if (section instanceof Map<?, ?> map) {
            return new HashMap<>((Map<String, Object>) map);
        }
        return new HashMap<>();
    }

    public static boolean isOverallManuallySet(Map<String, Object> additionalDetails) {
        return Boolean.TRUE.equals(getAssessmentSection(additionalDetails).get("overallManuallySet"));
    }

    public static String getPhoneOutcome(Map<String, Object> additionalDetails) {
        Object value = getAssessmentSection(additionalDetails).get("phoneOutcome");
        return value != null ? value.toString() : null;
    }

    public static String getFieldOutcome(Map<String, Object> additionalDetails) {
        Object value = getAssessmentSection(additionalDetails).get("fieldOutcome");
        return value != null ? value.toString() : null;
    }

    public static String getEligibleReason(Map<String, Object> additionalDetails) {
        Object value = getAssessmentSection(additionalDetails).get("eligibleReason");
        return value != null ? value.toString() : null;
    }

    public static String getIneligibleReason(Map<String, Object> additionalDetails) {
        Object value = getAssessmentSection(additionalDetails).get("ineligibleReason");
        return value != null ? value.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public static List<AssessmentAuditEvent> getAuditTrail(Map<String, Object> additionalDetails) {
        Object trail = getAssessmentSection(additionalDetails).get(AUDIT_TRAIL_KEY);
        if (!(trail instanceof List<?> events)) {
            return new ArrayList<>();
        }
        List<AssessmentAuditEvent> result = new ArrayList<>();
        for (Object event : events) {
            if (event instanceof Map<?, ?> map) {
                result.add(AssessmentAuditEvent.builder()
                        .event(map.get("event") != null ? map.get("event").toString() : null)
                        .timestamp(map.get("timestamp") instanceof Number n ? n.longValue() : null)
                        .actor(map.get("actor") != null ? map.get("actor").toString() : null)
                        .assessmentPlanId(map.get("assessmentPlanId") != null
                                ? map.get("assessmentPlanId").toString() : null)
                        .build());
            }
        }
        return result;
    }

    public static Map<String, Object> mergeAssessmentUpdates(Map<String, Object> additionalDetails,
                                                              Map<String, Object> assessmentUpdates) {
        Map<String, Object> root = additionalDetails != null ? new HashMap<>(additionalDetails) : new HashMap<>();
        Map<String, Object> assessment = getAssessmentSection(root);
        assessment.putAll(assessmentUpdates);
        root.put(ASSESSMENT_KEY, assessment);
        return root;
    }

    public static Map<String, Object> appendAuditEvent(Map<String, Object> additionalDetails,
                                                        AssessmentAuditEvent event) {
        Map<String, Object> assessment = getAssessmentSection(additionalDetails);
        List<Map<String, Object>> trail = new ArrayList<>();
        Object existing = assessment.get(AUDIT_TRAIL_KEY);
        if (existing instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    trail.add(new HashMap<>((Map<String, Object>) map));
                }
            }
        }
        Map<String, Object> entry = new HashMap<>();
        entry.put("event", event.getEvent());
        entry.put("timestamp", event.getTimestamp());
        entry.put("actor", event.getActor());
        if (event.getAssessmentPlanId() != null) {
            entry.put("assessmentPlanId", event.getAssessmentPlanId());
        }
        trail.add(entry);
        assessment.put(AUDIT_TRAIL_KEY, trail);
        return mergeAssessmentUpdates(additionalDetails, assessment);
    }
}
