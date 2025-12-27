package facility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.user.User;
import facility.config.Configuration;
import facility.repository.FacilityRepository;
import facility.util.EncryptionDecryptionUtil;
import facility.util.IdgenUtil;
import facility.util.QueryBuilderResult;
import facility.util.QueryBuilderUtil;
import facility.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final JdbcTemplate jdbcTemplate;
    private final FacilityRowMapper facilityRowMapper;
    private final IdgenUtil idgenUtil;
    private final FacilityMdmsValidator facilityMdmsValidator;
    private final BoundaryValidator boundaryValidator;
    private final FacilityQueryDao facilityQueryDao;
    private final BoundaryService boundaryService;
    private final Configuration configs;
    private final FacilityKibanaMapper facilityKibanaMapper;
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    public FacilityService(
            FacilityRepository facilityRepository,
            JdbcTemplate jdbcTemplate,
            FacilityRowMapper facilityRowMapper,
            IdgenUtil idgenUtil,
            FacilityMdmsValidator facilityMdmsValidator,
            BoundaryValidator boundaryValidator,
            FacilityQueryDao facilityQueryDao,
            BoundaryService boundaryService,
            Configuration configs,
            FacilityKibanaMapper facilityKibanaMapper,
            EncryptionDecryptionUtil encryptionDecryptionUtil) {
        this.facilityRepository = facilityRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.facilityRowMapper = facilityRowMapper;
        this.idgenUtil = idgenUtil;
        this.facilityMdmsValidator = facilityMdmsValidator;
        this.boundaryValidator = boundaryValidator;
        this.facilityQueryDao = facilityQueryDao;
        this.boundaryService = boundaryService;
        this.configs = configs;
        this.facilityKibanaMapper = facilityKibanaMapper;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
    }

    /**
     * Creates facilities in the system after validation.
     * Validates against MDMS, ensures boundary codes are valid,
     * generates facility IDs and address IDs if missing, and checks for uniqueness.
     *
     * <p><b>Boundary Creation Behavior:</b></p>
     * <ul>
     *   <li>Facility boundaries are created via external boundary service API calls</li>
     *   <li>Boundary codes are validated before creation to ensure parent boundaries exist</li>
     * </ul>
     *
     * @param request FacilityCreateRequest containing a list of facilities
     * @return list of successfully validated and pushed facilities
     * @throws CustomException if validation fails (MDMS, uniqueness, boundary validation)
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Facility> createFacility(FacilityCreateRequest request) {
        List<FacilityCreate> facilities = request.getFacilities();

        // Group facility create requests by tenant ID for batch validation and processing
        Map<String, List<FacilityCreate>> facilitiesByTenant = facilities.stream()
                .collect(Collectors.groupingBy(FacilityCreate::getTenantId));

        List<Facility> validatedFacilities = new ArrayList<>();

        for (Map.Entry<String, List<FacilityCreate>> entry : facilitiesByTenant.entrySet()) {
            String tenantId = entry.getKey();
            List<FacilityCreate> facilityCreateList = entry.getValue();
            List<Facility> tenantFacilities = new ArrayList<>();

            // Validate block boundary codes in bulk
            Set<String> blockBoundaryCodes = facilityCreateList.stream()
                    .map(FacilityCreate::getBlockBoundaryCode)
                    .collect(Collectors.toSet());
            boundaryValidator.validateBoundaries(blockBoundaryCodes, tenantId, request.getRequestInfo());

            List<Boundary> boundaryList = new ArrayList<>();
            List<BoundaryRelation> boundaryRelationList = new ArrayList<>();

            for (FacilityCreate facilityCreate : facilityCreateList) {
                Facility facility = Facility.builder()
                        .tenantId(tenantId)
                        .facilityCategory(facilityCreate.getFacilityCategory())
                        .facilityType(facilityCreate.getFacilityType())
                        .facilitySubtype(facilityCreate.getFacilitySubtype())
                        .facilityName(facilityCreate.getFacilityName())
                        .facilityOwnership(facilityCreate.getFacilityOwnership())
                        .facilityPocName(facilityCreate.getFacilityPocName())
                        .facilityPocEmail(facilityCreate.getFacilityPocEmail())
                        .hfrId(facilityCreate.getHfrId())
                        .ninId(facilityCreate.getNinId())
                        .userId(facilityCreate.getUserId())
                        .facilityStatus(facilityCreate.getFacilityStatus())
                        .facilityRegion(facilityCreate.getFacilityRegion())
                        .address(facilityCreate.getAddress())
                        .facilityDetails(facilityCreate.getFacilityDetails())
                        .wfStatus(facilityCreate.getWfStatus())
                        .additionalDetails(facilityCreate.getAdditionalDetails())
                        .isActive(facilityCreate.getIsActive())
                        .isOnmReady(facilityCreate.getIsOnmReady())
                        .build();

                String encryptedPocMobileNumber = encryptMobileNumber(facilityCreate.getFacilityPocPhone());
                if(encryptedPocMobileNumber!=null && !encryptedPocMobileNumber.isBlank()){
                    facility.setFacilityPocPhone(encryptedPocMobileNumber);
                }

                facility.setFacilityId(idgenUtil.getIdList(
                        request.getRequestInfo(), tenantId, "facility.id", "", 1
                ).get(0));

                String facilityBoundaryCode = facilityCreate.getBlockBoundaryCode() + "_" + facility.getFacilityId();

                boundaryList.add(
                        Boundary.builder()
                                .tenantId(tenantId)
                                .code(facilityBoundaryCode)
                                .build()
                );
                boundaryRelationList.add(
                        BoundaryRelation.builder()
                                .tenantId(tenantId)
                                .boundaryType("Facility")
                                .code(facilityBoundaryCode)
                                .parent(facilityCreate.getBlockBoundaryCode())
                                .hierarchyType("SELCO")
                                .build()
                );
                facility.setBoundaryCode(facilityBoundaryCode);

                // Set default workflow status and activation flag
                if (facility.getWfStatus() == null) facility.setWfStatus("CREATED");
                if (facility.getIsActive() == null) facility.setIsActive(true);

                // Generate address ID if missing
                if (facility.getAddress().getAddressId() == null) {
                    facility.getAddress().setAddressId(UUID.randomUUID().toString());
                }

                // Check uniqueness for HFR ID or NIN ID
                validateHfrOrNinUniqueness(facility, tenantId);

                // Check uniqueness of facility name + boundaryCode
                validateFacilityNameBoundaryCodeUnique(facility, tenantId);

                tenantFacilities.add(facility);
            }

            // Validate facilities against MDMS master data
            facilityMdmsValidator.validateAgainstMDMS(tenantFacilities, tenantId, request.getRequestInfo());

            //todo: handle boundary or boundary relation creation failure??
            BoundaryCreateRequest boundaryCreateRequest = BoundaryCreateRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .boundary(boundaryList)
                    .build();
            boundaryService.createBoundaries(boundaryCreateRequest);

            for (BoundaryRelation boundaryRelation: boundaryRelationList) {
                BoundaryRelationshipRequest boundaryRelationshipRequest = BoundaryRelationshipRequest.builder()
                        .requestInfo(request.getRequestInfo())
                        .boundaryRelationship(boundaryRelation)
                        .build();
                boundaryService.createBoundaryRelationship(boundaryRelationshipRequest);
            }

            for (Facility facility : tenantFacilities) {
                // Push to Kafka topic for persistence
                facilityRepository.pushCreateFacility(facility);
                
                // If facility is ONM ready, push to Kibana for indexing
                if (Boolean.TRUE.equals(facility.getIsOnmReady())) {
                    FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facility, request.getRequestInfo());
                    facilityRepository.pushToKibana(kibanaIndex);
                }
                
                validatedFacilities.add(facility);
            }
        }

        return validatedFacilities;
    }

    /**
     * Checks whether a facility with the same name and boundary already exists
     * in the given tenant. Throws a CustomException if duplicate found.
     */
    private void validateFacilityNameBoundaryCodeUnique(Facility facility, String tenantId) {
        if (facility.getFacilityName() != null && facility.getBoundaryCode() != null) {
            boolean exists = facilityQueryDao.existsByFacilityNameAndBoundary(
                    tenantId, facility.getFacilityName(), facility.getBoundaryCode()
            );

            if (exists) {
                throw new CustomException("FACILITY_DUPLICATE_NAME_LOCATION",
                        "A facility with the same name and boundary already exists in this tenant");
            }
        }
    }

    /**
     * Checks whether the HFR ID or NIN ID already exists for another facility
     * in the same tenant. Throws a CustomException if duplicate found.
     */
    private void validateHfrOrNinUniqueness(Facility facility, String tenantId) {
        if (facility != null) {
            String hfrId = facility.getHfrId();
            String ninId = facility.getNinId();

            if ((hfrId != null && !hfrId.isBlank()) || (ninId != null && !ninId.isBlank())) {
                boolean exists = facilityQueryDao.existsByHfrIdOrNinId(hfrId, ninId, tenantId);
                if (exists) {
                    throw new CustomException("FACILITY_DUPLICATE_ID",
                            "Facility with same HFR ID or NIN ID already exists in tenant " + tenantId);
                }
            }
        }
    }

    /**
     * Updates a facility after validating existence, MDMS values, and boundaries.
     * Pushes the update request to the Kafka topic for persistence.
     *
     * @param request FacilityUpdateRequest
     * @return updated facility data
     */
    public Facility updateFacility(FacilityUpdateRequest request) {
        FacilityUpdateRequestFacilityUpdate update = request.getFacilityUpdate();

        if (update.getFacilityId() == null || update.getTenantId() == null) {
            throw new IllegalArgumentException("facilityId and tenantId must be provided for update");
        }

        // Check if the facility exists in DB before attempting an update
        String checkSql = "SELECT COUNT(*) FROM facility WHERE id = ? AND tenant_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, update.getFacilityId(), update.getTenantId());
        if (count == null || count == 0) {
            return null; // facility not found
        }

        Facility facility = new Facility();
        facility.setFacilityId(update.getFacilityId());
        facility.setTenantId(update.getTenantId());
        facility.setFacilityType(update.getFacilityType());
        facility.setFacilitySubtype(update.getFacilitySubtype());
        facility.setFacilityName(update.getFacilityName());
        facility.setAddress(update.getAddress());
        facility.setAdditionalDetails(update.getAdditionalDetails());
        facility.setBoundaryCode(update.getBoundaryCode());
        facility.setFacilityDetails(update.getFacilityDetails());
        facility.setFacilityPocName(update.getPocName());
        facility.setFacilityPocPhone(update.getPocContact());
        facility.setFacilityPocEmail(update.getPocEmail());
        facility.setHfrId(update.getHfrId());
        facility.setNinId(update.getNinId());
        facility.setFacilityStatus(update.getStatus());
        facility.setUserId(update.getUserId());

        // Validate with MDMS and boundary APIs
        facilityMdmsValidator.validateAgainstMDMS(List.of(facility), update.getTenantId(), request.getRequestInfo());
        if (facility.getBoundaryCode() != null) {
            boundaryValidator.validateBoundaries(
                    Set.of(facility.getBoundaryCode()),
                    update.getTenantId(),
                    request.getRequestInfo());
        }

        if (facility.getWfStatus() == null) facility.setWfStatus("UPDATED");
        if (facility.getIsActive() == null) facility.setIsActive(true);

        facilityRepository.pushUpdateFacility(request);
        
        // If user sent isOnmReady = true, check if facility exists in Kibana, if not then push
        if (Boolean.TRUE.equals(update.getIsOnmReady())) {
            // Check if facility already exists in Kibana
            boolean existsInKibana = facilityKibanaMapper.existsInKibana(
                    update.getFacilityId(), 
                    update.getTenantId(), 
                    request.getRequestInfo()
            );
            
            if (existsInKibana) {
                log.info("Facility {} already exists in Kibana, skipping push", update.getFacilityId());
                return facility;
            }
            
            // Fetch full facility from DB only to get missing fields not in update request (like facilityCategory, facilityOwnership, etc.)
            String fetchFullFacilitySql = "SELECT * FROM facility WHERE id = ? AND tenant_id = ?";
            Facility existingFacility;
            try {
                existingFacility = jdbcTemplate.queryForObject(fetchFullFacilitySql, new Object[]{update.getFacilityId(), update.getTenantId()}, facilityRowMapper.rowMapper);
            } catch (EmptyResultDataAccessException e) {
                log.warn("Facility not found when trying to push to Kibana: {}", update.getFacilityId());
                return facility;
            }
            
            // Merge update request data with existing facility data (prioritize update values)
            Facility facilityForKibana = Facility.builder()
                    .facilityId(facility.getFacilityId())
                    .tenantId(facility.getTenantId())
                    .facilityType(facility.getFacilityType() != null ? facility.getFacilityType() : existingFacility.getFacilityType())
                    .facilitySubtype(facility.getFacilitySubtype() != null ? facility.getFacilitySubtype() : existingFacility.getFacilitySubtype())
                    .facilityName(facility.getFacilityName() != null ? facility.getFacilityName() : existingFacility.getFacilityName())
                    .facilityCategory(existingFacility.getFacilityCategory()) // Not in update request, use existing
                    .facilityOwnership(existingFacility.getFacilityOwnership()) // Not in update request, use existing
                    .facilityRegion(existingFacility.getFacilityRegion()) // Not in update request, use existing
                    .address(facility.getAddress() != null ? facility.getAddress() : existingFacility.getAddress())
                    .facilityDetails(facility.getFacilityDetails() != null ? facility.getFacilityDetails() : existingFacility.getFacilityDetails())
                    .additionalDetails(facility.getAdditionalDetails() != null ? facility.getAdditionalDetails() : existingFacility.getAdditionalDetails())
                    .boundaryCode(facility.getBoundaryCode() != null ? facility.getBoundaryCode() : existingFacility.getBoundaryCode())
                    .isOnmReady(true) // Set from update request
                    .facilityPocName(facility.getFacilityPocName()!=null && !facility.getFacilityPocName().isBlank() ? facility.getFacilityPocName(): existingFacility.getFacilityPocEmail())
                    .facilityPocPhone(facility.getFacilityPocPhone()!=null && !facility.getFacilityPocPhone().isBlank() ? facility.getFacilityPocPhone(): existingFacility.getFacilityPocPhone())
                    .facilityPocEmail(facility.getFacilityPocEmail()!=null && !facility.getFacilityPocEmail().isBlank() ? facility.getFacilityPocEmail(): existingFacility.getFacilityPocEmail())
                    .hfrId(facility.getHfrId()!=null && !facility.getHfrId().isBlank() ? facility.getHfrId(): existingFacility.getHfrId())
                    .ninId(facility.getNinId()!=null && !facility.getNinId().isBlank() ? facility.getNinId(): existingFacility.getNinId())
                    .userId(facility.getUserId()!=null && !facility.getUserId().isBlank() ? facility.getUserId(): existingFacility.getUserId())
                    .build();
            
            // Transform to Kibana index format and push
            FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facilityForKibana, request.getRequestInfo());
            facilityRepository.pushToKibana(kibanaIndex);
            log.info("Facility {} pushed to Kibana successfully", update.getFacilityId());
        }
        
        return facility;
    }

    /**
     * Searches for facilities using filters like tenantId, name, hfrId, ninId, boundary, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> searchFacilities(FacilitySearchRequest request) {
        QueryBuilderResult result = QueryBuilderUtil.buildWhereClause(request);

        StringBuilder query = new StringBuilder("SELECT * FROM facility");
        query.append(result.getWhereClause());
        query.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<Object> allParams = new ArrayList<>(result.getParams());
        allParams.add(request.getLimit());
        allParams.add(request.getOffset());

        List<Facility> facilityList = jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
        for (Facility facility: facilityList){
            String decryptedMobileNumber = decryptMobileNumber(facility.getFacilityPocPhone());
            if(decryptedMobileNumber!=null && !decryptedMobileNumber.isBlank()){
                facility.setFacilityPocPhone(decryptedMobileNumber);
            }
        }

        return facilityList;
    }

    /**
     * Searches for facilities using filters like multiple tenantIds, names, hfrIds, ninIds, boundaries, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> bulkSearchFacilities(FacilityBulkSearchRequest request) {
        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                request.getFacilityBulkSearchCriteria(), request.getRequestInfo(), configs.getOnmNonReadyAllowedRoles()
        );

        StringBuilder query = new StringBuilder("SELECT * FROM facility");
        query.append(result.getWhereClause());

        List<Object> allParams = new ArrayList<>(result.getParams());
        if (!Boolean.TRUE.equals(request.getFacilityBulkSearchCriteria().getSendNonPaginatedResponse())) {
            query.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
            allParams.add(request.getFacilityBulkSearchCriteria().getLimit());
            allParams.add(request.getFacilityBulkSearchCriteria().getOffset());
        }

        log.info("Bulk Search Query: {}", query);
        log.info("Bulk Search Params: {}", allParams);
        List<Facility> facilityList = jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
        for (Facility facility: facilityList){
            String decryptedMobileNumber = decryptMobileNumber(facility.getFacilityPocPhone());
            if(decryptedMobileNumber!=null && !decryptedMobileNumber.isBlank()){
                facility.setFacilityPocPhone(decryptedMobileNumber);
            }
        }
        return facilityList;
    }


    /**
     * Fetches a one-line summary of a facility using its ID.
     * If not found, returns null.
     *
     * @param facilityId unique ID of the facility
     * @return a FacilitySummary object
     */
    public FacilitySummary getFacilitySummary(String facilityId) {
        String sql = "SELECT facility_name, facility_type FROM facility WHERE facility_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{facilityId}, (rs, rowNum) -> {
                String name = rs.getString("facility_name");
                String type = rs.getString("facility_type");
                FacilitySummary summary = new FacilitySummary();
                summary.setSummary("Facility '" + name + "' is of type '" + type + "'.");
                return summary;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int countFacilities(FacilitySearchRequest request) {
        QueryBuilderResult result = QueryBuilderUtil.buildWhereClause(request);
        String query = "SELECT COUNT(*) FROM facility" + result.getWhereClause();
        return jdbcTemplate.queryForObject(query, result.getParams().toArray(), Integer.class);
    }

    public int countFacilitiesForBulkSearch(FacilityBulkSearchRequest request) {
        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                request.getFacilityBulkSearchCriteria(), request.getRequestInfo(), configs.getOnmNonReadyAllowedRoles()
        );
        String query = "SELECT COUNT(*) FROM facility" + result.getWhereClause();
        return jdbcTemplate.queryForObject(query, result.getParams().toArray(), Integer.class);
    }

    public void migrateFacilityData() {
        StringBuilder query = new StringBuilder("SELECT * FROM facility");
        List<Object> allParams = new ArrayList<>();
        List<Facility> facilities = jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
        for (Facility facilityDB : facilities){
            log.info("Before HF to migrate : {}", facilityDB);
            FacilityUpdateRequestFacilityUpdate facility = new FacilityUpdateRequestFacilityUpdate();
            facility.setFacilityId(facilityDB.getFacilityId());
            facility.setTenantId(facilityDB.getTenantId());
            facility.setFacilityType(facilityDB.getFacilityType());
            facility.setFacilitySubtype(facilityDB.getFacilitySubtype());
            facility.setFacilityName(facilityDB.getFacilityName());
            facility.setAddress(facilityDB.getAddress());
            facility.setAdditionalDetails(facilityDB.getAdditionalDetails());
            facility.setBoundaryCode(facilityDB.getBoundaryCode());
            facility.setPocName(facilityDB.getFacilityDetails().getPocName());
            facility.setPocEmail(facilityDB.getFacilityDetails().getPocEmail());
            facility.setHfrId(facilityDB.getFacilityDetails().getHfrId());
            facility.setNinId(facilityDB.getFacilityDetails().getNinId());
            facility.setSolarSolutionDesignType(facilityDB.getFacilityDetails().getSolarSolutionDesignType());
            facility.setStatus("ACTIVE");
            facility.setUserId(facilityDB.getUserId());
            facility.setIsOnmReady(facilityDB.getIsOnmReady());

            if(facilityDB.getFacilityDetails()!=null && facilityDB.getFacilityDetails().getPocContact()!=null && !facilityDB.getFacilityDetails().getPocContact().isBlank()){
                String encryptedMobileNumber = encryptMobileNumber(facilityDB.getFacilityDetails().getPocContact());
                if (encryptedMobileNumber!=null && !encryptedMobileNumber.isBlank()){
                    log.info("mobile number {} encrypted to : {}", facilityDB.getFacilityDetails().getPocContact(), encryptedMobileNumber);
                    facility.setPocContact(encryptedMobileNumber);
                }
            }

            HealthFacilityDetails details = facilityDB.getFacilityDetails();
            details.setPocName(null);
            details.setPocContact(null);
            details.setPocEmail(null);
            details.setNinId(null);
            details.setHfrId(null);
            facility.setFacilityDetails(details);

            FacilityUpdateRequest request = FacilityUpdateRequest.builder()
                    .facilityUpdate(facility)
                    .build();
            log.info("Final HF to migrate : {}", request.getFacilityUpdate());
            facilityRepository.pushUpdateFacility(request);
        }
    }

    public String encryptMobileNumber(String mobileNumber){
        String encryptedMobileNumber = null;
        if(mobileNumber!=null && !mobileNumber.isBlank()){
            EncryptObject object = EncryptObject.builder()
                    .mobileNumber(mobileNumber)
                    .build();
            Map<String, EncryptObject> userMap = new HashMap<>();
            userMap.put("userObject", object);
            EncReqObject encReqObject = EncReqObject.builder()
                    .tenantId(configs.getEncServiceTenantId())
                    .type("Normal")
                    .value(userMap)
                    .build();
            EncryptionRequest encryptionRequest = EncryptionRequest.builder()
                    .encryptionRequests(List.of(encReqObject))
                    .build();
            List<Map<String, EncryptObject>> response = encryptionDecryptionUtil.encryptObject(encryptionRequest);
            for (Map<String, EncryptObject> map : response) {
                EncryptObject user = map.get("userObject"); // clé du JSON
                if (user != null) {
                    log.info("Mobile crypté : {}", user.getMobileNumber());
                    encryptedMobileNumber = user.getMobileNumber();
                }
            }
        }
        return encryptedMobileNumber;
    }

    public String decryptMobileNumber(String mobileNumber){
        String decryptedMobileNumber = null;
        if(mobileNumber!=null && !mobileNumber.isBlank()){
            EncryptObject object = EncryptObject.builder()
                    .mobileNumber(mobileNumber)
                    .build();
            Map<String, EncryptObject> userMap = new HashMap<>();
            userMap.put("userObject", object);
            DecryptionRequest request = DecryptionRequest.builder()
                    .decryptionRequests(List.of(userMap))
                    .build();
            List<Map<String, EncryptObject>> response = encryptionDecryptionUtil.decryptObject(request);
            for (Map<String, EncryptObject> map : response) {
                EncryptObject user = map.get("userObject"); // clé du JSON
                if (user != null) {
                    log.info("Mobile decrypté : {}", user.getMobileNumber());
                    decryptedMobileNumber = user.getMobileNumber();
                }
            }
        }
        return decryptedMobileNumber;
    }

}
