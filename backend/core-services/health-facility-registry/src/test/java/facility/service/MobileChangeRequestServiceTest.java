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
import facility.web.models.MobileChangeRequestSearchCriteria;
import facility.web.models.MobileChangeRequestSearchRequest;
import facility.web.models.MobileChangeRequestStatus;
import facility.web.models.User;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileChangeRequestServiceTest {

    private static final String USER_UUID = "user-1";
    private static final String CRM_UUID = "crm-1";
    private static final String CURRENT = "9876543210";
    private static final String NEW = "9123456780";

    @Mock
    private MobileChangeRequestRepository repository;
    @Mock
    private HRMSUtils hrmsUtils;
    @Mock
    private PocPhoneCipher pocPhoneCipher;

    @InjectMocks
    private MobileChangeRequestService service;

    @BeforeEach
    void setUp() {
        // Identity "cipher" with a marker so tests can tell stored values from plaintext.
        lenient().when(pocPhoneCipher.encrypt(anyString())).thenAnswer(i -> "enc:" + i.getArgument(0));
        lenient().when(pocPhoneCipher.decrypt(anyString())).thenAnswer(i -> ((String) i.getArgument(0)).replace("enc:", ""));
    }

    @Test
    void createStoresEncryptedPendingRequestForUsersFacility() {
        when(hrmsUtils.getUserById(any(), eq(USER_UUID))).thenReturn(facilityEmployee());
        when(repository.findFacilitiesByBoundaryCode("BLK_F1")).thenReturn(
                List.of(Map.of("id", "F1", "tenant_id", "in", "facility_name", "PHC One")));

        MobileChangeRequest created = service.create(createRequest(NEW));

        ArgumentCaptor<MobileChangeRequest> stored = ArgumentCaptor.forClass(MobileChangeRequest.class);
        verify(repository).insert(stored.capture());
        assertEquals("enc:" + NEW, stored.getValue().getNewMobileNumber());
        assertEquals("enc:" + CURRENT, stored.getValue().getCurrentMobileNumber());
        assertEquals(MobileChangeRequestStatus.PENDING, stored.getValue().getStatus());
        assertEquals("F1", stored.getValue().getFacilityId());
        // Response carries plaintext.
        assertEquals(NEW, created.getNewMobileNumber());
        assertEquals(CURRENT, created.getCurrentMobileNumber());
    }

    @Test
    void createRejectsInvalidNumber() {
        for (String bad : new String[]{"12345", "5123456789", "98765432101", "98765abcde", null}) {
            CustomException e = assertThrows(CustomException.class, () -> service.create(createRequest(bad)));
            assertEquals("INVALID_MOBILE_NUMBER", e.getCode());
        }
        verify(repository, never()).insert(any());
    }

    @Test
    void createRejectsSameNumber() {
        when(hrmsUtils.getUserById(any(), eq(USER_UUID))).thenReturn(facilityEmployee());
        CustomException e = assertThrows(CustomException.class, () -> service.create(createRequest(CURRENT)));
        assertEquals("SAME_MOBILE_NUMBER", e.getCode());
    }

    @Test
    void createRejectsWhilePending() {
        when(repository.hasPending(USER_UUID)).thenReturn(true);
        CustomException e = assertThrows(CustomException.class, () -> service.create(createRequest(NEW)));
        assertEquals("MOBILE_CHANGE_REQUEST_ALREADY_PENDING", e.getCode());
    }

    @Test
    void createMapsConcurrentDuplicateToPending() {
        when(hrmsUtils.getUserById(any(), eq(USER_UUID))).thenReturn(facilityEmployee());
        when(repository.findFacilitiesByBoundaryCode("BLK_F1")).thenReturn(
                List.of(Map.of("id", "F1", "tenant_id", "in", "facility_name", "PHC One")));
        org.mockito.Mockito.doThrow(new DuplicateKeyException("dup")).when(repository).insert(any());

        CustomException e = assertThrows(CustomException.class, () -> service.create(createRequest(NEW)));
        assertEquals("MOBILE_CHANGE_REQUEST_ALREADY_PENDING", e.getCode());
    }

    @Test
    void createRejectsNonFacilityUser() {
        Employee employee = facilityEmployee();
        employee.getJurisdictions().get(0).setBoundaryType("Block");
        when(hrmsUtils.getUserById(any(), eq(USER_UUID))).thenReturn(employee);

        CustomException e = assertThrows(CustomException.class, () -> service.create(createRequest(NEW)));
        assertEquals("NOT_A_FACILITY_USER", e.getCode());
    }

    @Test
    void cancelOnlyByRequester() {
        when(repository.findById("r1")).thenReturn(pending());
        CustomException e = assertThrows(CustomException.class,
                () -> service.cancel(action(requestInfo("someone-else"), "r1")));
        assertEquals("NOT_AUTHORIZED", e.getCode());
        verify(repository, never()).closePending(anyString(), any(), any(), anyString(), anyLong());
    }

    @Test
    void cancelClosesPending() {
        when(repository.findById("r1")).thenReturn(pending());
        when(repository.closePending(eq("r1"), eq(MobileChangeRequestStatus.CANCELLED), any(), eq(USER_UUID), anyLong()))
                .thenReturn(true);

        MobileChangeRequest cancelled = service.cancel(action(requestInfo(USER_UUID), "r1"));
        assertEquals(MobileChangeRequestStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void decisionOnAlreadyClosedRequestFails() {
        MobileChangeRequest closed = pending();
        closed.setStatus(MobileChangeRequestStatus.CANCELLED);
        when(repository.findById("r1")).thenReturn(closed);

        CustomException e = assertThrows(CustomException.class,
                () -> service.approve(action(requestInfo(CRM_UUID, "COMPLAINT_ASSESSOR"), "r1")));
        assertEquals("MOBILE_CHANGE_REQUEST_NOT_PENDING", e.getCode());
    }

    @Test
    void decisionLosingRaceFails() {
        when(repository.findById("r1")).thenReturn(pending());
        when(repository.closePending(anyString(), any(), any(), anyString(), anyLong())).thenReturn(false);

        CustomException e = assertThrows(CustomException.class,
                () -> service.reject(action(requestInfo(CRM_UUID, "COMPLAINT_ASSESSOR"), "r1")));
        assertEquals("MOBILE_CHANGE_REQUEST_NOT_PENDING", e.getCode());
    }

    @Test
    void approveAndRejectRequireCrm() {
        CustomException e = assertThrows(CustomException.class,
                () -> service.approve(action(requestInfo(USER_UUID, "COMPLAINANT"), "r1")));
        assertEquals("NOT_AUTHORIZED", e.getCode());
        e = assertThrows(CustomException.class,
                () -> service.reject(action(requestInfo(USER_UUID, "COMPLAINANT"), "r1")));
        assertEquals("NOT_AUTHORIZED", e.getCode());
    }

    @Test
    void approveUpdatesFacilityAndOnlyHrmsMobileNumber() {
        when(repository.findById("r1")).thenReturn(pending());
        when(repository.closePending(eq("r1"), eq(MobileChangeRequestStatus.APPROVED), any(), eq(CRM_UUID), anyLong()))
                .thenReturn(true);
        when(repository.updateFacilityPocPhone(eq("F1"), eq("in"), eq("enc:" + NEW), eq(CRM_UUID), anyLong())).thenReturn(1);
        Employee employee = facilityEmployee();
        when(hrmsUtils.getUserById(any(), eq(USER_UUID))).thenReturn(employee);
        when(hrmsUtils.updateHRMSUser(any())).thenReturn(List.of(employee));

        MobileChangeRequest approved = service.approve(action(requestInfo(CRM_UUID, "COMPLAINT_ASSESSOR"), "r1"));

        assertEquals(MobileChangeRequestStatus.APPROVED, approved.getStatus());
        ArgumentCaptor<EmployeeRequest> sent = ArgumentCaptor.forClass(EmployeeRequest.class);
        verify(hrmsUtils).updateHRMSUser(sent.capture());
        Employee sentEmployee = sent.getValue().getEmployees().get(0);
        assertEquals(NEW, sentEmployee.getUser().getMobileNumber());
        assertEquals("PHC_USER", sentEmployee.getUser().getUserName());
        assertEquals(1, sentEmployee.getJurisdictions().size());
    }

    @Test
    void approveFailsWhenHrmsUpdateReturnsNothing() {
        when(repository.findById("r1")).thenReturn(pending());
        when(repository.closePending(anyString(), any(), any(), anyString(), anyLong())).thenReturn(true);
        when(repository.updateFacilityPocPhone(anyString(), anyString(), anyString(), anyString(), anyLong())).thenReturn(1);
        when(hrmsUtils.getUserById(any(), eq(USER_UUID))).thenReturn(facilityEmployee());
        when(hrmsUtils.updateHRMSUser(any())).thenReturn(null);

        CustomException e = assertThrows(CustomException.class,
                () -> service.approve(action(requestInfo(CRM_UUID, "COMPLAINT_ASSESSOR"), "r1")));
        assertEquals("HRMS_UPDATE_FAILED", e.getCode());
    }

    @Test
    void searchByNonCrmIsScopedToOwnRequests() {
        MobileChangeRequestSearchCriteria criteria = MobileChangeRequestSearchCriteria.builder().userUuid("other").build();
        when(repository.search(any(), anyInt(), anyInt())).thenReturn(new ArrayList<>());

        service.search(MobileChangeRequestSearchRequest.builder()
                .requestInfo(requestInfo(USER_UUID, "COMPLAINANT")).criteria(criteria).build());

        assertEquals(USER_UUID, criteria.getUserUuid());
    }

    @Test
    void searchByCrmSeesQueue() {
        MobileChangeRequestSearchCriteria criteria = MobileChangeRequestSearchCriteria.builder()
                .status(List.of(MobileChangeRequestStatus.PENDING)).build();
        when(repository.search(any(), anyInt(), anyInt())).thenReturn(new ArrayList<>(List.of(pending())));
        when(repository.count(any())).thenReturn(1);

        var response = service.search(MobileChangeRequestSearchRequest.builder()
                .requestInfo(requestInfo(CRM_UUID, "COMPLAINT_ASSESSOR")).criteria(criteria).build());

        assertEquals(null, criteria.getUserUuid());
        assertEquals(1, response.getTotalCount());
        assertEquals(NEW, response.getMobileChangeRequests().get(0).getNewMobileNumber());
    }

    private static MobileChangeRequestCreateRequest createRequest(String newMobile) {
        return MobileChangeRequestCreateRequest.builder()
                .requestInfo(requestInfo(USER_UUID, "COMPLAINANT"))
                .mobileChangeRequest(MobileChangeRequest.builder().newMobileNumber(newMobile).build())
                .build();
    }

    private static MobileChangeRequestActionRequest action(RequestInfo requestInfo, String id) {
        return MobileChangeRequestActionRequest.builder().requestInfo(requestInfo).id(id).build();
    }

    private static RequestInfo requestInfo(String uuid, String... roles) {
        List<Role> roleList = new ArrayList<>();
        for (String r : roles) {
            roleList.add(Role.builder().code(r).build());
        }
        return RequestInfo.builder()
                .userInfo(org.egov.common.contract.request.User.builder().uuid(uuid).roles(roleList).build())
                .build();
    }

    private static Employee facilityEmployee() {
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        jurisdictions.add(Jurisdiction.builder().hierarchy("ADMIN").boundary("BLK_F1").boundaryType("Facility").isActive(true).build());
        return Employee.builder()
                .uuid(USER_UUID)
                .jurisdictions(jurisdictions)
                .user(User.builder().uuid(USER_UUID).userName("PHC_USER").mobileNumber(CURRENT).build())
                .build();
    }

    private static MobileChangeRequest pending() {
        return MobileChangeRequest.builder()
                .id("r1").tenantId("in").userUuid(USER_UUID).facilityId("F1")
                .currentMobileNumber("enc:" + CURRENT).newMobileNumber("enc:" + NEW)
                .status(MobileChangeRequestStatus.PENDING)
                .auditDetails(AuditDetails.builder().createdBy(USER_UUID).createdDate(1L).build())
                .build();
    }
}
