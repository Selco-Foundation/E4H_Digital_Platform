package facility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import facility.util.HRMSUtils;
import facility.web.models.Employee;
import facility.web.models.EmployeeRequest;
import facility.web.models.FacilityCreate;
import facility.web.models.FacilityVendorMappingResponse.VendorOrganisation;
import facility.web.models.HealthFacilityDetails;
import facility.web.models.Jurisdiction;
import facility.web.models.User;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Vendor mapping: organisations resolve to their first active Complaint Resolver, and the mapping
 * endpoints add to one resolver but remove from every holder.
 */
@ExtendWith(MockitoExtension.class)
class VendorOrganisationServiceTest {

    private static final String BOUNDARY = "India_Karnataka_Dharwad_Hubli_F1";
    private static final RequestInfo REQUEST_INFO = new RequestInfo();

    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private HRMSUtils hrmsUtils;

    private VendorOrganisationService service;

    /** code -> {id, name}; and each organisation's users. */
    private final Map<String, String[]> organisations = new HashMap<>();
    private final Map<String, String> organisationOfUser = new HashMap<>();

    @BeforeEach
    void setUp() {
        Configuration configs = new Configuration();
        configs.setVendorHost("http://vendor");
        configs.setVendorOrganisationSearchPath("/vendor/organisation/v1/_search");
        configs.setVendorOrganisationUserSearchPath("/vendor/organisation/v1/user/_search");
        service = new VendorOrganisationService(serviceRequestRepository, configs, hrmsUtils, new ObjectMapper());

        organisation("X", "ORG-X", "Xylem Solar");
        organisation("Y", "ORG-Y", "Yellow Power");
        organisation("Z", "ORG-Z", "Zenith Energy");
        organisation("N", "ORG-N", "No Resolver Ltd");
        lenient().when(serviceRequestRepository.fetchResult(any(StringBuilder.class), any()))
                .thenAnswer(call -> vendorRegistry(call.getArgument(0).toString(), call.getArgument(1)));
    }

    private void organisation(String code, String id, String name) {
        organisations.put(code, new String[]{id, name});
    }

    @SuppressWarnings("unchecked")
    private Object vendorRegistry(String uri, Object body) {
        Map<String, Object> request = (Map<String, Object>) body;
        if (uri.contains("/user/_search")) {
            Map<String, Object> criteria = (Map<String, Object>) request.get("OrgUser");
            List<Map<String, Object>> users = new ArrayList<>();
            organisationOfUser.forEach((userId, organisationId) -> {
                boolean byOrg = criteria.containsKey("organizationIds")
                        && ((List<String>) criteria.get("organizationIds")).contains(organisationId);
                boolean byUser = criteria.containsKey("userIds")
                        && ((List<String>) criteria.get("userIds")).contains(userId);
                if (byOrg || byUser) {
                    users.add(Map.of("userId", userId, "organizationId", organisationId, "isDeleted", false));
                }
            });
            return Map.of("OrgUsers", users);
        }
        Map<String, Object> criteria = (Map<String, Object>) request.get("SearchCriteria");
        List<Map<String, Object>> found = new ArrayList<>();
        organisations.forEach((code, idAndName) -> {
            boolean byCode = code.equals(criteria.get("code"));
            boolean byIds = criteria.containsKey("ids") && ((List<String>) criteria.get("ids")).contains(idAndName[0]);
            if (byCode || byIds) {
                found.add(Map.of("id", idAndName[0], "code", code, "name", idAndName[1]));
            }
        });
        return Map.of("organisations", found);
    }

    private static Employee employee(String uuid, boolean active, String... boundaries) {
        Employee employee = new Employee();
        employee.setUuid(uuid);
        employee.setCode(uuid);
        employee.setIsActive(active);
        User user = new User();
        user.setUuid(uuid);
        user.setUserName(uuid);
        user.setName("Name of " + uuid);
        employee.setUser(user);
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        for (String boundary : boundaries) {
            Jurisdiction jurisdiction = new Jurisdiction();
            jurisdiction.setBoundary(boundary);
            jurisdiction.setIsActive(true);
            jurisdictions.add(jurisdiction);
        }
        employee.setJurisdictions(jurisdictions);
        return employee;
    }

    private void resolversAre(String organisationId, Employee... resolvers) {
        List<String> userIds = organisationOfUser.entrySet().stream()
                .filter(entry -> entry.getValue().equals(organisationId)).map(Map.Entry::getKey).toList();
        lenient().when(hrmsUtils.findActiveComplaintResolvers(any(), eq(userIds))).thenReturn(List.of(resolvers));
    }

    @Test
    void anOrganisationResolvesToItsActiveComplaintResolverNotItsFirstUser() {
        organisationOfUser.put("x-admin", "ORG-X");
        organisationOfUser.put("x-resolver", "ORG-X");
        Employee resolver = employee("x-resolver", true);
        when(hrmsUtils.findActiveComplaintResolvers(any(), anyCollection())).thenReturn(List.of(resolver));

        Map<String, VendorOrganisationService.ResolvedVendor> resolved =
                service.resolveVendors(List.of(" X "), "in", REQUEST_INFO);

        assertEquals("x-resolver", resolved.get("X").resolver().getUuid());
        assertEquals("Xylem Solar", resolved.get("X").organisation().name());
    }

    @Test
    void unknownAndResolverlessOrganisationsAreAllReportedAtOnce() {
        organisationOfUser.put("n-admin", "ORG-N");
        when(hrmsUtils.findActiveComplaintResolvers(any(), anyCollection())).thenReturn(List.of());

        CustomException e = assertThrows(CustomException.class,
                () -> service.resolveVendors(List.of("NOPE", "N"), "in", REQUEST_INFO));

        assertTrue(e.getErrors().containsKey(VendorOrganisationService.ERR_NOT_FOUND), e.getErrors().toString());
        assertTrue(e.getErrors().get(VendorOrganisationService.ERR_NOT_FOUND).contains("NOPE"));
        assertTrue(e.getErrors().get(VendorOrganisationService.ERR_NO_RESOLVER).contains("No Resolver Ltd"));
    }

    @Test
    void readListsOrganisationsWithAnActiveHolderAndIgnoresThePocUser() {
        organisationOfUser.put("x-resolver", "ORG-X");
        organisationOfUser.put("y-gone", "ORG-Y");
        when(hrmsUtils.getEmployeesByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(List.of(
                employee("x-resolver", true, BOUNDARY),
                employee("y-gone", false, BOUNDARY),
                employee("facility-poc", true, BOUNDARY)));

        List<VendorOrganisation> mapped = service.mappedOrganisations(BOUNDARY, "in", REQUEST_INFO);

        assertEquals(List.of("X"), mapped.stream().map(VendorOrganisation::code).toList());
    }

    @Test
    void updateAddsToOneResolverAndRemovesFromEveryHolder() {
        organisationOfUser.put("x-resolver", "ORG-X");
        organisationOfUser.put("y-one", "ORG-Y");
        organisationOfUser.put("y-two", "ORG-Y");
        organisationOfUser.put("z-resolver", "ORG-Z");
        Employee x = employee("x-resolver", true, BOUNDARY, "India_Karnataka");
        Employee yOne = employee("y-one", true, BOUNDARY, "India_Karnataka");
        Employee yTwo = employee("y-two", false, BOUNDARY, "India_Goa");
        when(hrmsUtils.getEmployeesByBoundaryCode(any(), eq(BOUNDARY))).thenReturn(List.of(x, yOne, yTwo));
        Employee z = employee("z-resolver", true, "India_Karnataka");
        resolversAre("ORG-Z", z);
        when(hrmsUtils.getUserById(any(), eq("z-resolver"))).thenReturn(z);
        when(hrmsUtils.buildFacilityJurisdiction(BOUNDARY, "in")).thenReturn(employee("j", true, BOUNDARY).getJurisdictions().get(0));
        when(hrmsUtils.mergeFacilityJurisdiction(any(), any())).thenAnswer(call -> {
            List<Jurisdiction> merged = new ArrayList<>(call.getArgument(0));
            merged.add(call.getArgument(1));
            return merged;
        });
        when(hrmsUtils.updateHRMSUser(any())).thenAnswer(call -> ((EmployeeRequest) call.getArgument(0)).getEmployees());

        List<VendorOrganisation> now = service.setMappedOrganisations(BOUNDARY, List.of("X", "Z"), "in", REQUEST_INFO);

        assertEquals(List.of("X", "Z"), now.stream().map(VendorOrganisation::code).toList());
        ArgumentCaptor<EmployeeRequest> updates = ArgumentCaptor.forClass(EmployeeRequest.class);
        verify(hrmsUtils, times(3)).updateHRMSUser(updates.capture());
        List<String> updated = updates.getAllValues().stream().map(r -> r.getEmployees().get(0).getUuid()).toList();
        assertEquals(List.of("y-one", "y-two", "z-resolver"), updated);
        assertEquals(Boolean.FALSE, yOne.getJurisdictions().get(0).getIsActive());
        assertEquals(Boolean.FALSE, yTwo.getJurisdictions().get(0).getIsActive());
        assertEquals(Boolean.TRUE, x.getJurisdictions().get(0).getIsActive());
    }

    @Test
    void aRemovalThatWouldLeaveAUserWithNoJurisdictionIsRefusedBeforeAnyWrite() {
        organisationOfUser.put("y-only", "ORG-Y");
        when(hrmsUtils.getEmployeesByBoundaryCode(any(), eq(BOUNDARY)))
                .thenReturn(List.of(employee("y-only", true, BOUNDARY)));

        CustomException e = assertThrows(CustomException.class,
                () -> service.setMappedOrganisations(BOUNDARY, List.of(), "in", REQUEST_INFO));

        assertEquals(VendorOrganisationService.ERR_LAST_JURISDICTION, e.getCode());
        verify(hrmsUtils, never()).updateHRMSUser(any());
    }

    @Test
    void anUnresolvableAdditionIsRefusedBeforeAnyRemoval() {
        organisationOfUser.put("y-one", "ORG-Y");
        when(hrmsUtils.getEmployeesByBoundaryCode(any(), eq(BOUNDARY)))
                .thenReturn(List.of(employee("y-one", true, BOUNDARY, "India_Karnataka")));

        assertThrows(CustomException.class,
                () -> service.setMappedOrganisations(BOUNDARY, List.of("NOPE"), "in", REQUEST_INFO));

        verify(hrmsUtils, never()).updateHRMSUser(any());
    }

    @Test
    void createTakesTheVendorFromTheFacilityAndFallsBackToTheLegacyDetailsKey() {
        FacilityCreate onFacility = new FacilityCreate();
        onFacility.setVendorCode(" X ");
        FacilityCreate legacy = new FacilityCreate();
        legacy.setFacilityDetails(HealthFacilityDetails.builder().vendorCode("Y").build());
        FacilityCreate none = new FacilityCreate();
        none.setVendorCode("  ");

        assertEquals("X", FacilityService.vendorCodeOf(onFacility));
        assertEquals("Y", FacilityService.vendorCodeOf(legacy));
        assertNull(FacilityService.vendorCodeOf(none));
    }

    @Test
    void theLegacyDetailsKeyIsReadButNeverWrittenBack() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        HealthFacilityDetails details = mapper.readValue("{\"vendor_code\":\"Y\",\"pocDesignation\":\"Doctor\"}",
                HealthFacilityDetails.class);

        assertEquals("Y", details.getVendorCode());
        assertFalse(mapper.writeValueAsString(details).contains("vendor_code"));
    }

}
