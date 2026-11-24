package facility.service;

import facility.util.BoundaryUtil;
import facility.util.HRMSUtils;
import facility.web.models.Employee;
import facility.web.models.EmployeeRequest;
import facility.web.models.Facility;
import facility.web.models.FacilityUpdateRequestFacilityUpdate;
import facility.web.models.Jurisdiction;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** District/Block changes through the update API: which blocks are accepted, and the HRMS move. */
@ExtendWith(MockitoExtension.class)
class FacilityBlockChangeTest {

    private static final String OLD_BLOCK = "India_Karnataka_Dharwad_Hubli";
    private static final String NEW_BLOCK = "India_Karnataka_Belagavi_Gokak";
    private static final String OLD_CODE = OLD_BLOCK + "_F1";
    private static final String NEW_CODE = NEW_BLOCK + "_F1";
    private static final RequestInfo REQUEST_INFO = new RequestInfo();

    @Mock private BoundaryValidator boundaryValidator;
    @Mock private BoundaryUtil boundaryUtil;
    @Mock private HRMSUtils hrmsUtils;

    @InjectMocks private FacilityService facilityService;

    private static Facility stored() {
        return Facility.builder().facilityId("F1").tenantId("in").boundaryCode(OLD_CODE).build();
    }

    private static FacilityUpdateRequestFacilityUpdate moveTo(String block) {
        return FacilityUpdateRequestFacilityUpdate.builder().facilityId("F1").tenantId("in")
                .blockBoundaryCode(block).build();
    }

    private static Map<String, String> chain(String state, String district, String block) {
        Map<String, String> chain = new LinkedHashMap<>();
        chain.put("country", "India");
        chain.put("state", state);
        chain.put("district", district);
        if (block != null) {
            chain.put("block", block);
        }
        return chain;
    }

    @Test
    void noBlockOrTheCurrentBlockMovesNothing() {
        assertNull(facilityService.resolveRequestedBlock(moveTo(null), stored(), REQUEST_INFO));
        assertNull(facilityService.resolveRequestedBlock(moveTo("  "), stored(), REQUEST_INFO));
        assertNull(facilityService.resolveRequestedBlock(moveTo(OLD_BLOCK), stored(), REQUEST_INFO));
        verifyNoInteractions(boundaryValidator, boundaryUtil);
    }

    @Test
    void aBlockInAnotherDistrictOfTheSameStateIsAccepted() throws Exception {
        when(boundaryUtil.ancestryOf(NEW_BLOCK))
                .thenReturn(chain("India_Karnataka", "India_Karnataka_Belagavi", NEW_BLOCK));
        when(boundaryUtil.ancestryOf(OLD_BLOCK))
                .thenReturn(chain("India_Karnataka", "India_Karnataka_Dharwad", OLD_BLOCK));

        assertEquals(NEW_BLOCK, facilityService.resolveRequestedBlock(moveTo(NEW_BLOCK), stored(), REQUEST_INFO));
    }

    @Test
    void aDistrictCodeIsNotABlock() throws Exception {
        when(boundaryUtil.ancestryOf("India_Karnataka_Belagavi"))
                .thenReturn(chain("India_Karnataka", "India_Karnataka_Belagavi", null));
        when(boundaryUtil.ancestryOf(OLD_BLOCK))
                .thenReturn(chain("India_Karnataka", "India_Karnataka_Dharwad", OLD_BLOCK));

        CustomException e = assertThrows(CustomException.class,
                () -> facilityService.resolveRequestedBlock(moveTo("India_Karnataka_Belagavi"), stored(), REQUEST_INFO));
        assertEquals("INVALID_BLOCK_BOUNDARY", e.getCode());
    }

    @Test
    void aBlockInAnotherStateIsRefused() throws Exception {
        String goaBlock = "India_Goa_NorthGoa_Bardez";
        when(boundaryUtil.ancestryOf(goaBlock)).thenReturn(chain("India_Goa", "India_Goa_NorthGoa", goaBlock));
        when(boundaryUtil.ancestryOf(OLD_BLOCK))
                .thenReturn(chain("India_Karnataka", "India_Karnataka_Dharwad", OLD_BLOCK));

        CustomException e = assertThrows(CustomException.class,
                () -> facilityService.resolveRequestedBlock(moveTo(goaBlock), stored(), REQUEST_INFO));
        assertEquals("FACILITY_STATE_CHANGE_NOT_ALLOWED", e.getCode());
    }

    private static Employee holder(String code, String... boundaries) {
        Employee employee = new Employee();
        employee.setUuid(code);
        employee.setCode(code);
        employee.setIsActive(true);
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        for (String boundary : boundaries) {
            Jurisdiction jurisdiction = new Jurisdiction();
            jurisdiction.setBoundary(boundary);
            jurisdiction.setBoundaryType("Facility");
            jurisdiction.setHierarchy("ADMIN");
            jurisdictions.add(jurisdiction);
        }
        employee.setJurisdictions(jurisdictions);
        return employee;
    }

    @Test
    void everyHolderOfTheOldCodeIsMovedAndOnlyThatJurisdictionChanges() {
        Employee poc = holder("HFR1", OLD_CODE);
        Employee resolver = holder("vendor-x", OLD_CODE, "India_Karnataka_Dharwad_Hubli_F9");
        when(hrmsUtils.getEmployeesByBoundaryCode(any(), eq(OLD_CODE))).thenReturn(List.of(poc, resolver));
        when(hrmsUtils.updateHRMSUser(any())).thenAnswer(call ->
                ((EmployeeRequest) call.getArgument(0)).getEmployees());

        facilityService.moveHrmsJurisdictions(OLD_CODE, NEW_CODE, "F1", REQUEST_INFO);

        ArgumentCaptor<EmployeeRequest> updates = ArgumentCaptor.forClass(EmployeeRequest.class);
        verify(hrmsUtils, times(2)).updateHRMSUser(updates.capture());
        assertEquals(NEW_CODE, poc.getJurisdictions().get(0).getBoundary());
        assertEquals(NEW_CODE, resolver.getJurisdictions().get(0).getBoundary());
        assertEquals("India_Karnataka_Dharwad_Hubli_F9", resolver.getJurisdictions().get(1).getBoundary());
        assertEquals("Facility", resolver.getJurisdictions().get(0).getBoundaryType());
        assertEquals(Boolean.FALSE, poc.getReActivateEmployee());
    }

    @Test
    void anHrmsRefusalStopsTheChange() {
        when(hrmsUtils.getEmployeesByBoundaryCode(any(), eq(OLD_CODE))).thenReturn(List.of(holder("HFR1", OLD_CODE)));
        when(hrmsUtils.updateHRMSUser(any())).thenReturn(null);

        CustomException e = assertThrows(CustomException.class,
                () -> facilityService.moveHrmsJurisdictions(OLD_CODE, NEW_CODE, "F1", REQUEST_INFO));
        assertEquals("FACILITY_REKEY_HRMS_FAILED", e.getCode());
    }

    @Test
    void anEmployeeWithoutTheOldCodeIsLeftAlone() {
        when(hrmsUtils.getEmployeesByBoundaryCode(any(), eq(OLD_CODE)))
                .thenReturn(List.of(holder("someone", "India_Karnataka_Dharwad")));

        facilityService.moveHrmsJurisdictions(OLD_CODE, NEW_CODE, "F1", REQUEST_INFO);

        verify(hrmsUtils, never()).updateHRMSUser(any());
    }
}
