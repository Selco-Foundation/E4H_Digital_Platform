package org.egov.field_planner.util;

import java.util.List;
import java.util.Set;

public final class AssessmentConstants {

    private AssessmentConstants() {
    }

    public static final String PLAN_TYPE_ASSESSMENT = "ASSESSMENT";
    public static final String ACTIVITY_CODE_ASSESSMENT = "ASSESSMENT";

    public static final String PLAN_STATUS_ACTIVE = "ACTIVE";
    public static final String PLAN_STATUS_CLOSED = "CLOSED";

    public static final String PHONE_PENDING = "PENDING";
    public static final String PHONE_PENDING_WRONG_NUMBER = "PENDING_WRONG_NUMBER";
    public static final String PHONE_PENDING_NO_ANSWER = "PENDING_NO_ANSWER";
    public static final String PHONE_QUALIFIED = "QUALIFIED";
    public static final String PHONE_NOT_QUALIFIED = "NOT_QUALIFIED";

    public static final String FIELD_PENDING = "PENDING";
    public static final String FIELD_QUALIFIED = "QUALIFIED";
    public static final String FIELD_NOT_QUALIFIED = "NOT_QUALIFIED";

    public static final String OVERALL_PENDING = "PENDING";
    public static final String OVERALL_ELIGIBLE = "ELIGIBLE";
    public static final String OVERALL_NOT_ELIGIBLE = "NOT_ELIGIBLE";

    public static final String OUTCOME_QUALIFIED = "QUALIFIED";
    public static final String OUTCOME_NOT_QUALIFIED = "NOT_QUALIFIED";

    public static final String COMPLETION_ENROLLED = "ENROLLED";
    public static final String COMPLETION_ELIGIBLE = "ELIGIBLE";
    public static final String COMPLETION_NOT_ELIGIBLE = "NOT_ELIGIBLE";
    public static final String COMPLETION_MOVED_TO_FIELD_PLAN = "MOVED_TO_FIELD_PLAN";
    public static final String COMPLETION_EXPIRED = "EXPIRED";

    public static final String PHASE_PHONE = "PHONE";
    public static final String PHASE_FIELD = "FIELD";

    public static final String CATEGORY_HEALTH = "HEALTH";
    public static final String CATEGORY_ANGANWADI = "ANGANWADI";

    public static final String ROLE_ENUMERATOR = "ENUMERATOR";
    public static final String ROLE_FIELD_POC = "FIELD_POC";
    public static final String ACTOR_SYSTEM = "SYSTEM";

    public static final String WF_ACTION_CREATE = "CREATE";
    public static final String WF_ACTION_SUBMIT_REMOTE = "SUBMIT_REMOTE";
    public static final String WF_ACTION_UNABLE_TO_CONTACT = "UNABLE_TO_CONTACT";
    public static final String WF_ACTION_ASSIGN_FOR_FIELD = "ASSIGN_FOR_FIELD";
    public static final String WF_ACTION_SUBMIT_FIELD = "SUBMIT_FIELD";
    public static final String WF_ACTION_AUTO_ELIGIBLE = "AUTO_ELIGIBLE";
    public static final String WF_ACTION_AUTO_NOT_ELIGIBLE = "AUTO_NOT_ELIGIBLE";
    public static final String WF_ACTION_MARK_ELIGIBLE = "MARK_ELIGIBLE";
    public static final String WF_ACTION_MARK_NOT_ELIGIBLE = "MARK_NOT_ELIGIBLE";

    public static final String UNABLE_REASON_WRONG_NUMBER = "WRONG_NUMBER";
    public static final String UNABLE_REASON_NO_ANSWER = "NO_ANSWER";

    public static final List<String> REMOTE_PENDING_STATUSES = List.of(
            PHONE_PENDING, PHONE_PENDING_WRONG_NUMBER, PHONE_PENDING_NO_ANSWER);
    public static final List<String> REMOTE_DONE_STATUSES = List.of(PHONE_QUALIFIED, PHONE_NOT_QUALIFIED);
    public static final Set<String> FINAL_OVERALL_STATUSES = Set.of(OVERALL_ELIGIBLE, OVERALL_NOT_ELIGIBLE);

    public static final String AUDIT_INCLUDED = "INCLUDED_IN_PLAN";
    public static final String AUDIT_REMOTE_SUBMITTED = "REMOTE_SUBMITTED";
    public static final String AUDIT_UNABLE_TO_CONTACT = "UNABLE_TO_CONTACT";
    public static final String AUDIT_ASSIGNED_FOR_ONSITE = "ASSIGNED_FOR_ONSITE";
    public static final String AUDIT_ONSITE_SUBMITTED = "ONSITE_SUBMITTED";
    public static final String AUDIT_OVERALL_SET_ELIGIBLE = "OVERALL_SET_ELIGIBLE";
    public static final String AUDIT_OVERALL_SET_NOT_ELIGIBLE = "OVERALL_SET_NOT_ELIGIBLE";
    public static final String AUDIT_OVERALL_AUTO_ELIGIBLE = "OVERALL_AUTO_ELIGIBLE";
    public static final String AUDIT_OVERALL_AUTO_NOT_ELIGIBLE = "OVERALL_AUTO_NOT_ELIGIBLE";
    public static final String AUDIT_HANDOFF = "HANDOFF_TO_FIELD_PLAN";

    public static final String ASSESSMENT_PLAN_NOT_FOUND = "ASSESSMENT_PLAN_NOT_FOUND";
    public static final String ASSESSMENT_PLAN_NAME_DUPLICATE = "ASSESSMENT_PLAN_NAME_DUPLICATE";
    public static final String ASSESSMENT_INVALID_DATE_RANGE = "ASSESSMENT_INVALID_DATE_RANGE";
    public static final String ASSESSMENT_PROJECT_NOT_FOUND = "ASSESSMENT_PROJECT_NOT_FOUND";
    public static final String ASSESSMENT_PLAN_HAS_PENDING_FACILITIES = "ASSESSMENT_PLAN_HAS_PENDING_FACILITIES";
    public static final String ASSESSMENT_FACILITY_ALREADY_ON_PLAN = "ASSESSMENT_FACILITY_ALREADY_ON_PLAN";
    public static final String ASSESSMENT_FACILITY_NOT_ON_PROJECT = "ASSESSMENT_FACILITY_NOT_ON_PROJECT";
    public static final String ASSESSMENT_FACILITY_ELIGIBLE_ACTIVE = "ASSESSMENT_FACILITY_ELIGIBLE_ACTIVE";
    public static final String ASSESSMENT_PLAN_NOT_COMPLETE = "ASSESSMENT_PLAN_NOT_COMPLETE";
    public static final String ASSESSMENT_FACILITY_ONGOING = "ASSESSMENT_FACILITY_ONGOING";
    public static final String ASSESSMENT_ACTIVITY_NOT_FOUND = "ASSESSMENT_ACTIVITY_NOT_FOUND";
    public static final String ASSESSMENT_ASSESSOR_NOT_FOUND = "ASSESSMENT_ASSESSOR_NOT_FOUND";
    public static final String ASSESSMENT_ASSESSOR_ROLE_REQUIRED = "ASSESSMENT_ASSESSOR_ROLE_REQUIRED";
    public static final String WORKFLOW_TRANSITION_FAILED = "WORKFLOW_TRANSITION_FAILED";

    public static final String ASSESSMENT_PLAN_FACILITY_NOT_FOUND = "ASSESSMENT_PLAN_FACILITY_NOT_FOUND";
    public static final String ASSESSMENT_REMOTE_PENDING = "ASSESSMENT_REMOTE_PENDING";
    public static final String ASSESSMENT_ASSIGN_FIELD_INVALID = "ASSESSMENT_ASSIGN_FIELD_INVALID";
    public static final String ASSESSMENT_RESULT_ALREADY_SET = "ASSESSMENT_RESULT_ALREADY_SET";
    public static final String ASSESSMENT_INELIGIBLE_REASON_REQUIRED = "ASSESSMENT_INELIGIBLE_REASON_REQUIRED";
    public static final String ASSESSMENT_ELIGIBLE_REASON_REQUIRED = "ASSESSMENT_ELIGIBLE_REASON_REQUIRED";
    public static final String ASSESSMENT_DUPLICATE_PHONE_SUBMISSION = "ASSESSMENT_DUPLICATE_PHONE_SUBMISSION";
    public static final String ASSESSMENT_DUPLICATE_FIELD_SUBMISSION = "ASSESSMENT_DUPLICATE_FIELD_SUBMISSION";
    public static final String ASSESSMENT_INVALID_FORM_DATA = "ASSESSMENT_INVALID_FORM_DATA";
    public static final String ASSESSMENT_UNAUTHORIZED_ASSESSOR = "ASSESSMENT_UNAUTHORIZED_ASSESSOR";
    public static final String ASSESSMENT_CATEGORY_MISMATCH = "ASSESSMENT_CATEGORY_MISMATCH";
    public static final String ASSESSMENT_FORM_NOT_AVAILABLE = "ASSESSMENT_FORM_NOT_AVAILABLE";
    public static final String ASSESSMENT_FIELD_NOT_ASSIGNED = "ASSESSMENT_FIELD_NOT_ASSIGNED";
    public static final String ASSESSMENT_FACILITY_NOT_ELIGIBLE = "ASSESSMENT_FACILITY_NOT_ELIGIBLE";
    public static final String ASSESSMENT_FACILITY_ALREADY_ON_FIELD_PLAN = "ASSESSMENT_FACILITY_ALREADY_ON_FIELD_PLAN";
    public static final String ASSESSMENT_HANDOFF_FIELD_PLAN_FACILITY_REQUIRED =
            "ASSESSMENT_HANDOFF_FIELD_PLAN_FACILITY_REQUIRED";
}
