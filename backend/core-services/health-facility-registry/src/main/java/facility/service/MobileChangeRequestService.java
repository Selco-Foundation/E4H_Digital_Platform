package facility.service;

import facility.repository.MobileChangeRequestRepository;
import facility.util.HRMSUtils;
import facility.util.PocPhoneCipher;
import facility.web.models.AuditDetails;
import facility.web.models.Employee;
import facility.web.models.EmployeeRequest;
import facility.web.models.Jurisdiction;
import facility.web.models.MobileChangeRequest;
import facility.web.models.MobileChangeRequestActionRequest;
import facility.web.models.MobileChangeRequestCreateRequest;
import facility.web.models.MobileChangeRequestResponse;
import facility.web.models.MobileChangeRequestSearchCriteria;
import facility.web.models.MobileChangeRequestSearchRequest;
import facility.web.models.MobileChangeRequestStatus;
import facility.web.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static facility.config.ServiceConstants.ROLE_COMPLAINT_ASSESSOR;

/**
 * Registered-mobile-number change requests raised by facility POC users and decided by the CRM.
 *
 * <p>The POC user is the HRMS employee holding a {@code Facility} jurisdiction on the facility's
 * boundary code. The CRM is any {@code COMPLAINT_ASSESSOR}: the role is state level with no
 * facility mapping, so every CRM in the tenant sees the whole queue.
 *
 * <p>Nothing changes for the user until approval. Approval then writes the new number to that
 * user's HRMS/egov-user record and to the POC phone of that one facility. The facility copy has to
 * move too: a later facility edit pushes {@code facility_poc_phone} back onto the HRMS user (see
 * {@link FacilityService#updatedHRMSUser}) and would otherwise undo the change.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MobileChangeRequestService {

    /** 10-digit Indian mobile number. */
    private static final Pattern INDIAN_MOBILE = Pattern.compile("^[6-9][0-9]{9}$");

    private static final String FACILITY_BOUNDARY_TYPE = "Facility";
    private static final String REQUEST_INFO_KEY = "RequestInfo";
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final MobileChangeRequestRepository repository;
    private final HRMSUtils hrmsUtils;
    private final PocPhoneCipher pocPhoneCipher;

    public MobileChangeRequest create(MobileChangeRequestCreateRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String userUuid = requireUserUuid(requestInfo);

        String newMobile = request.getMobileChangeRequest().getNewMobileNumber();
        newMobile = newMobile == null ? null : newMobile.trim();
        if (newMobile == null || !INDIAN_MOBILE.matcher(newMobile).matches()) {
            throw new CustomException("INVALID_MOBILE_NUMBER", "New mobile number must be a valid 10-digit Indian mobile number");
        }

        if (repository.hasPending(userUuid)) {
            throw pendingExists();
        }

        Employee employee = hrmsUtils.getUserById(Map.of(REQUEST_INFO_KEY, requestInfo), userUuid);
        User user = employee.getUser();
        if (user == null) {
            throw new CustomException("USER_NOT_FOUND", "No user found for the logged-in employee");
        }
        if (newMobile.equals(user.getMobileNumber())) {
            throw new CustomException("SAME_MOBILE_NUMBER", "New mobile number must be different from the registered mobile number");
        }

        String boundaryCode = facilityBoundaryCode(employee);
        Map<String, Object> facility = findFacility(boundaryCode);

        long now = System.currentTimeMillis();
        MobileChangeRequest changeRequest = MobileChangeRequest.builder()
                .id(UUID.randomUUID().toString())
                .tenantId((String) facility.get("tenant_id"))
                .userUuid(userUuid)
                .userName(user.getUserName())
                .facilityId((String) facility.get("id"))
                .facilityName((String) facility.get("facility_name"))
                .boundaryCode(boundaryCode)
                .currentMobileNumber(user.getMobileNumber())
                .newMobileNumber(newMobile)
                .status(MobileChangeRequestStatus.PENDING)
                .auditDetails(AuditDetails.builder()
                        .createdBy(userUuid).createdDate(now)
                        .lastModifiedBy(userUuid).lastModifiedDate(now)
                        .build())
                .build();

        try {
            repository.insert(encrypted(changeRequest));
        } catch (DuplicateKeyException e) {
            // Lost a race with another create from the same user.
            throw pendingExists();
        }
        log.info("Mobile change request {} raised by user {} for facility {}",
                changeRequest.getId(), userUuid, changeRequest.getFacilityId());
        return changeRequest;
    }

    /** The requesting user withdraws their own request while the CRM has not yet acted on it. */
    public MobileChangeRequest cancel(MobileChangeRequestActionRequest request) {
        String userUuid = requireUserUuid(request.getRequestInfo());
        MobileChangeRequest existing = requirePending(request.getId());
        if (!userUuid.equals(existing.getUserUuid())) {
            throw new CustomException("NOT_AUTHORIZED", "Only the user who raised the request can cancel it");
        }
        return close(existing, MobileChangeRequestStatus.CANCELLED, request.getComment(), userUuid);
    }

    public MobileChangeRequest reject(MobileChangeRequestActionRequest request) {
        String crmUuid = requireCrm(request.getRequestInfo());
        MobileChangeRequest existing = requirePending(request.getId());
        return close(existing, MobileChangeRequestStatus.REJECTED, request.getComment(), crmUuid);
    }

    /**
     * Closes the request as approved and applies the new number. One transaction: if the HRMS
     * update fails, the request is still pending and the facility row is untouched. HRMS goes last
     * because it is the only write that cannot be rolled back.
     */
    @Transactional(rollbackFor = Exception.class)
    public MobileChangeRequest approve(MobileChangeRequestActionRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String crmUuid = requireCrm(requestInfo);
        MobileChangeRequest existing = requirePending(request.getId());
        MobileChangeRequest approved = close(existing, MobileChangeRequestStatus.APPROVED, request.getComment(), crmUuid);

        long now = approved.getActionTime();
        if (approved.getFacilityId() != null) {
            int rows = repository.updateFacilityPocPhone(approved.getFacilityId(), approved.getTenantId(),
                    pocPhoneCipher.encrypt(approved.getNewMobileNumber()), crmUuid, now);
            if (rows == 0) {
                log.warn("Facility {} not found while approving mobile change request {}; POC phone not updated",
                        approved.getFacilityId(), approved.getId());
            }
        }

        Employee employee = hrmsUtils.getUserById(Map.of(REQUEST_INFO_KEY, requestInfo), approved.getUserUuid());
        // Only the mobile number changes; roles, jurisdictions and everything else go back as read.
        employee.getUser().setMobileNumber(approved.getNewMobileNumber());
        List<Employee> updated = hrmsUtils.updateHRMSUser(EmployeeRequest.builder()
                .requestInfo(requestInfo)
                .employees(List.of(employee))
                .build());
        if (updated == null || updated.isEmpty()) {
            throw new CustomException("HRMS_UPDATE_FAILED", "Could not update the registered mobile number in HRMS");
        }
        log.info("Mobile change request {} approved by {}; registered mobile updated for user {}",
                approved.getId(), crmUuid, approved.getUserUuid());
        return approved;
    }

    /**
     * The CRM sees every request (the approval queue is {@code status=[PENDING]}); anyone else sees
     * only their own, which is how the profile page shows the current or last request's status.
     */
    public MobileChangeRequestResponse search(MobileChangeRequestSearchRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String userUuid = requireUserUuid(requestInfo);
        MobileChangeRequestSearchCriteria criteria = request.getCriteria() != null
                ? request.getCriteria() : new MobileChangeRequestSearchCriteria();
        if (!hasRole(requestInfo, ROLE_COMPLAINT_ASSESSOR)) {
            criteria.setUserUuid(userUuid);
        }

        int limit = criteria.getLimit() == null || criteria.getLimit() <= 0 ? DEFAULT_LIMIT : Math.min(criteria.getLimit(), MAX_LIMIT);
        int offset = criteria.getOffset() == null || criteria.getOffset() < 0 ? 0 : criteria.getOffset();

        List<MobileChangeRequest> results = repository.search(criteria, limit, offset);
        results.forEach(this::decrypt);
        return MobileChangeRequestResponse.builder()
                .mobileChangeRequests(results)
                .totalCount(repository.count(criteria))
                .build();
    }

    private MobileChangeRequest close(MobileChangeRequest existing, MobileChangeRequestStatus status, String comment, String actionBy) {
        long now = System.currentTimeMillis();
        if (!repository.closePending(existing.getId(), status, comment, actionBy, now)) {
            throw notPending();
        }
        existing.setStatus(status);
        existing.setComment(comment);
        existing.setActionBy(actionBy);
        existing.setActionTime(now);
        existing.getAuditDetails().setLastModifiedBy(actionBy);
        existing.getAuditDetails().setLastModifiedDate(now);
        return existing;
    }

    private MobileChangeRequest requirePending(String id) {
        MobileChangeRequest existing = repository.findById(id);
        if (existing == null) {
            throw new CustomException("MOBILE_CHANGE_REQUEST_NOT_FOUND", "No mobile number change request found with id " + id);
        }
        if (existing.getStatus() != MobileChangeRequestStatus.PENDING) {
            throw notPending();
        }
        return decrypt(existing);
    }

    private String facilityBoundaryCode(Employee employee) {
        if (employee.getJurisdictions() != null) {
            for (Jurisdiction j : employee.getJurisdictions()) {
                if (j != null && !Boolean.FALSE.equals(j.getIsActive())
                        && FACILITY_BOUNDARY_TYPE.equalsIgnoreCase(Objects.toString(j.getBoundaryType(), ""))
                        && j.getBoundary() != null && !j.getBoundary().isBlank()) {
                    return j.getBoundary().trim();
                }
            }
        }
        throw new CustomException("NOT_A_FACILITY_USER", "Only facility users can request a mobile number change");
    }

    private Map<String, Object> findFacility(String boundaryCode) {
        List<Map<String, Object>> facilities = repository.findFacilitiesByBoundaryCode(boundaryCode);
        if (facilities.isEmpty()) {
            throw new CustomException("FACILITY_NOT_FOUND", "No facility found for the logged-in user");
        }
        if (facilities.size() > 1) {
            log.error("Boundary code {} maps to {} facilities; refusing to guess", boundaryCode, facilities.size());
            throw new CustomException("MULTIPLE_FACILITIES_FOUND", "The logged-in user maps to more than one facility");
        }
        return facilities.get(0);
    }

    /** A copy with both numbers encrypted for storage; the original keeps plaintext for the response. */
    private MobileChangeRequest encrypted(MobileChangeRequest request) {
        return request.toBuilder()
                .currentMobileNumber(pocPhoneCipher.encrypt(request.getCurrentMobileNumber()))
                .newMobileNumber(pocPhoneCipher.encrypt(request.getNewMobileNumber()))
                .build();
    }

    private MobileChangeRequest decrypt(MobileChangeRequest request) {
        request.setCurrentMobileNumber(pocPhoneCipher.decrypt(request.getCurrentMobileNumber()));
        request.setNewMobileNumber(pocPhoneCipher.decrypt(request.getNewMobileNumber()));
        return request;
    }

    private String requireCrm(RequestInfo requestInfo) {
        String uuid = requireUserUuid(requestInfo);
        if (!hasRole(requestInfo, ROLE_COMPLAINT_ASSESSOR)) {
            throw new CustomException("NOT_AUTHORIZED", "Only a CRM can approve or reject a mobile number change request");
        }
        return uuid;
    }

    private static String requireUserUuid(RequestInfo requestInfo) {
        if (requestInfo == null || requestInfo.getUserInfo() == null
                || requestInfo.getUserInfo().getUuid() == null || requestInfo.getUserInfo().getUuid().isBlank()) {
            throw new CustomException("USERINFO_UUID", "Logged-in user details are missing from RequestInfo");
        }
        return requestInfo.getUserInfo().getUuid();
    }

    private static boolean hasRole(RequestInfo requestInfo, String roleCode) {
        return requestInfo.getUserInfo().getRoles() != null && requestInfo.getUserInfo().getRoles().stream()
                .anyMatch(role -> role != null && roleCode.equalsIgnoreCase(role.getCode()));
    }

    private static CustomException pendingExists() {
        return new CustomException("MOBILE_CHANGE_REQUEST_ALREADY_PENDING",
                "A mobile number change request is already pending; cancel it or wait for the CRM's decision");
    }

    private static CustomException notPending() {
        return new CustomException("MOBILE_CHANGE_REQUEST_NOT_PENDING",
                "This mobile number change request has already been processed or cancelled");
    }
}
