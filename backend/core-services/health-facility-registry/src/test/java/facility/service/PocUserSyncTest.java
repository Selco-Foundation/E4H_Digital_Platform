package facility.service;

import facility.config.Configuration;
import facility.repository.FacilityRepository;
import facility.util.HRMSUtils;
import facility.web.models.Employee;
import facility.web.models.EmployeeRequest;
import facility.web.models.Facility;
import facility.web.models.FacilityUpdateRequest;
import facility.web.models.HealthFacilityDetails;
import facility.web.models.User;
import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The PoC (HCR) user rules: it exists with the facility, its username follows the identifier
 * whatever the ONM state, and its password is set only when it is created or activated.
 */
@ExtendWith(MockitoExtension.class)
class PocUserSyncTest {

    private static final String BOUNDARY = "IN_KA_BLR_BLK1_F1";
    private static final RequestInfo REQUEST_INFO = new RequestInfo();

    @Mock private HRMSUtils hrmsUtils;
    @Mock private HRMSService hrmsService;
    @Mock private FacilityRepository facilityRepository;
    @Mock private FacilityKibanaMapper facilityKibanaMapper;
    @Mock private Configuration configs;

    @InjectMocks private FacilityService facilityService;

    private static Facility facility(String hfrId) {
        return Facility.builder()
                .facilityId("F1").tenantId("in").facilityCategory("HEALTH")
                .boundaryCode(BOUNDARY).hfrId(hfrId)
                .facilityPocName("Asha Devi").facilityPocPhone("9876543210")
                .facilityDetails(HealthFacilityDetails.builder().pocDesignation("Doctor").build())
                .build();
    }

    private static Employee employee(String code, boolean active) {
        Employee employee = new Employee();
        employee.setUuid("E1");
        employee.setCode(code);
        employee.setIsActive(active);
        employee.setEmployeeStatus(active ? "EMPLOYED" : "INACTIVE");
        User user = new User();
        user.setUuid("U1");
        user.setUserName(code);
        user.setActive(active);
        employee.setUser(user);
        return employee;
    }

    @Test
    void missingUserIsCreatedAndDeactivatedWhenNotOnmReady() {
        Employee created = employee("HFR1", true);
        when(hrmsUtils.getEmployeeByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(null, created);
        when(hrmsService.createFacilityPOCEmployee(any(), any())).thenReturn(true);
        when(hrmsUtils.updateHRMSUser(any())).thenReturn(List.of(created));

        facilityService.syncPocUser(facility("HFR1"), "in", REQUEST_INFO, "9876543210", false);

        ArgumentCaptor<EmployeeRequest> update = ArgumentCaptor.forClass(EmployeeRequest.class);
        verify(hrmsUtils).updateHRMSUser(update.capture());
        assertEquals(Boolean.FALSE, update.getValue().getEmployees().get(0).getIsActive());
        verify(hrmsService, never()).setUserPassword(any(), anyString(), any());
    }

    @Test
    void identifierChangeOnInactiveUserRenamesWithoutActivatingOrSettingPassword() {
        when(hrmsUtils.getEmployeeByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(employee("HFR-OLD", false));

        facilityService.syncPocUser(facility("HFR-NEW"), "in", REQUEST_INFO, "9876543210", false);

        verify(hrmsUtils).updateHrmsUsername(any(), eq("E1"), eq("HFR-NEW"), eq("in"));
        verify(hrmsUtils, never()).updateHRMSUser(any());
        verify(hrmsService, never()).setUserPassword(any(), anyString(), any());
    }

    @Test
    void identifierChangeOnLiveUserKeepsItsPassword() {
        when(hrmsUtils.getEmployeeByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(employee("HFR-OLD", true));

        facilityService.syncPocUser(facility("HFR-NEW"), "in", REQUEST_INFO, "9876543210", true);

        verify(hrmsUtils).updateHrmsUsername(any(), eq("E1"), eq("HFR-NEW"), eq("in"));
        verify(hrmsUtils, never()).updateHRMSUser(any());
        verify(hrmsService, never()).setUserPassword(any(), anyString(), any());
    }

    @Test
    void activationSetsTheDerivedPasswordOnce() {
        Employee inactive = employee("HFR1", false);
        when(hrmsUtils.getEmployeeByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(inactive);
        when(hrmsUtils.updateHRMSUser(any())).thenReturn(List.of(inactive));

        facilityService.syncPocUser(facility("HFR1"), "in", REQUEST_INFO, "9876543210", true);

        verify(hrmsUtils, never()).updateHrmsUsername(any(), anyString(), anyString(), anyString());
        verify(hrmsService).setUserPassword(any(), eq("Asha@3210"), any());
        assertEquals(Boolean.TRUE, inactive.getIsActive());
    }

    @Test
    void renameNeverWritesPocFieldsIntoFacilityDetails() {
        when(hrmsUtils.getEmployeeByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(employee("HFR-OLD", true));

        facilityService.syncPocUser(facility("HFR-NEW"), "in", REQUEST_INFO, "9876543210", false);

        ArgumentCaptor<FacilityUpdateRequest> push = ArgumentCaptor.forClass(FacilityUpdateRequest.class);
        verify(facilityRepository).pushUpdateFacility(push.capture());
        HealthFacilityDetails details = push.getValue().getFacilityUpdate().getFacilityDetails();
        assertNull(details.getPocContact());
        assertNull(details.getPocName());
        assertNull(details.getHfrId());
        assertEquals("Doctor", details.getPocDesignation());
        assertEquals("HFR-NEW", push.getValue().getFacilityUpdate().getHfrId());
    }

    @Test
    void staleDetailsCopyDoesNotOverrideTheFacilityColumns() {
        Facility facility = facility("HFR1");
        facility.getFacilityDetails().setPocContact("9000000001");
        when(hrmsUtils.getEmployeeByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(null);
        when(hrmsService.createFacilityPOCEmployee(any(), any())).thenReturn(false);

        facilityService.syncPocUser(facility, "in", REQUEST_INFO, "9876543210", true);

        ArgumentCaptor<Facility> sent = ArgumentCaptor.forClass(Facility.class);
        verify(hrmsService).createFacilityPOCEmployee(sent.capture(), any());
        assertEquals("9876543210", sent.getValue().getFacilityDetails().getPocContact());
        assertFalse(sent.getValue().getFacilityDetails().getPocContact().startsWith("9000"));
    }
}
