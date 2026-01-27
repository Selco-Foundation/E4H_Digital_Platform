package org.egov.repository;

import org.apache.commons.lang3.StringUtils;
import org.egov.repository.querybuilder.*;
import org.egov.repository.rowmapper.*;
import org.egov.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.util.OrganisationConstant.ORGANISATION_ENCRYPT_KEY;

@Repository
@Slf4j
public class OrganisationRepository {

    private final OrganisationFunctionQueryBuilder organisationFunctionQueryBuilder;
    private final OrganisationFunctionRowMapper organisationFunctionRowMapper;
    private final AddressQueryBuilder addressQueryBuilder;
    private final AddressRowMapper addressRowMapper;
    private final DocumentQueryBuilder documentQueryBuilder;
    private final DocumentRowMapper documentRowMapper;
    private final ContactDetailsQueryBuilder contactDetailsQueryBuilder;
    private final ContactDetailsRowMapper contactDetailsRowMapper;
    private final JurisdictionQueryBuilder jurisdictionQueryBuilder;
    private final JurisdictionRowMapper jurisdictionRowMapper;
    private final TaxIdentifierQueryBuilder taxIdentifierQueryBuilder;
    private final TaxIdentifierRowMapper taxIdentifierRowMapper;
    private final AddressOrgIdsRowMapper addressOrgIdsRowMapper;
    private final TaxIdentifierOrgIdsRowMapper taxIdentifierOrgIdsRowMapper;

    private final ContactDetailsOrgIdsRowMapper contactDetailsOrgIdsRowMapper;

    private final EncryptionService encryptionService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OrganisationRepository(AddressQueryBuilder addressQueryBuilder, OrganisationFunctionQueryBuilder organisationFunctionQueryBuilder, OrganisationFunctionRowMapper organisationFunctionRowMapper, AddressOrgIdsRowMapper addressOrgIdsRowMapper, AddressRowMapper addressRowMapper, DocumentQueryBuilder documentQueryBuilder, DocumentRowMapper documentRowMapper, ContactDetailsQueryBuilder contactDetailsQueryBuilder, TaxIdentifierRowMapper taxIdentifierRowMapper, ContactDetailsRowMapper contactDetailsRowMapper, ContactDetailsOrgIdsRowMapper contactDetailsOrgIdsRowMapper, JdbcTemplate jdbcTemplate, JurisdictionQueryBuilder jurisdictionQueryBuilder, TaxIdentifierOrgIdsRowMapper taxIdentifierOrgIdsRowMapper, JurisdictionRowMapper jurisdictionRowMapper, TaxIdentifierQueryBuilder taxIdentifierQueryBuilder, EncryptionService encryptionService) {
        this.addressQueryBuilder = addressQueryBuilder;
        this.organisationFunctionQueryBuilder = organisationFunctionQueryBuilder;
        this.organisationFunctionRowMapper = organisationFunctionRowMapper;
        this.addressOrgIdsRowMapper = addressOrgIdsRowMapper;
        this.addressRowMapper = addressRowMapper;
        this.documentQueryBuilder = documentQueryBuilder;
        this.documentRowMapper = documentRowMapper;
        this.contactDetailsQueryBuilder = contactDetailsQueryBuilder;
        this.taxIdentifierRowMapper = taxIdentifierRowMapper;
        this.contactDetailsRowMapper = contactDetailsRowMapper;
        this.contactDetailsOrgIdsRowMapper = contactDetailsOrgIdsRowMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.jurisdictionQueryBuilder = jurisdictionQueryBuilder;
        this.taxIdentifierOrgIdsRowMapper = taxIdentifierOrgIdsRowMapper;
        this.jurisdictionRowMapper = jurisdictionRowMapper;
        this.taxIdentifierQueryBuilder = taxIdentifierQueryBuilder;
        this.encryptionService = encryptionService;
    }

    public List<Organisation> getOrganisations(OrgSearchRequest orgSearchRequest) {
        log.trace("OrganisationRepository::getOrganisations entry");
        String tenantId = getTenantId(orgSearchRequest);
        log.info("Starting organisation search for tenant: {}", tenantId);
        
        encryptionService.encryptDetails(orgSearchRequest, ORGANISATION_ENCRYPT_KEY);
        log.debug("Search criteria encrypted");
        
        Set<String> orgIds = collectOrgIdsFromSearchCriteria(orgSearchRequest);
        log.debug("Total unique organisation IDs after combining search results: {}", orgIds.size());

        if (shouldReturnEmptyList(orgSearchRequest, orgIds)) {
            log.debug("No organisation IDs found matching search criteria");
            return Collections.emptyList();
        }

        List<Organisation> organisations = getOrganisationsBasedOnSearchCriteria(orgSearchRequest, orgIds);
        log.debug("Fetched {} organisations from database", organisations.size());

        OrganisationRelatedData relatedData = fetchRelatedData(organisations);
        
        log.info("Organisation search completed, returning {} organisations", organisations.size());
        return encryptionService.decrypt(
                buildOrganisationSearchResult(organisations, relatedData.addresses, relatedData.contactDetails,
                        relatedData.documents, relatedData.jurisdictions, relatedData.identifiers),
                ORGANISATION_ENCRYPT_KEY, orgSearchRequest);
    }

    private String getTenantId(OrgSearchRequest orgSearchRequest) {
        return orgSearchRequest.getSearchCriteria() != null 
                ? orgSearchRequest.getSearchCriteria().getTenantId() : "unknown";
    }

    private Set<String> collectOrgIdsFromSearchCriteria(OrgSearchRequest orgSearchRequest) {
        Set<String> orgIdsFromIdentifierSearch = getOrgIdsForIdentifiersBasedOnSearchCriteria(orgSearchRequest);
        log.debug("Found {} organisation IDs from identifier search", orgIdsFromIdentifierSearch.size());
        
        Set<String> orgIdsFromBoundarySearch = getOrgIdsForAddressesBasedOnSearchCriteria(orgSearchRequest);
        log.debug("Found {} organisation IDs from boundary search", orgIdsFromBoundarySearch.size());
        
        Set<String> orgIdsFromContactMobileNumberSearch = getOrgIdsForContactNumberBasedOnSearchCriteria(orgSearchRequest);
        log.debug("Found {} organisation IDs from contact mobile number search", orgIdsFromContactMobileNumberSearch.size());

        Set<String> orgIds = new HashSet<>();
        getOrgIdsForSearch(orgSearchRequest, orgIdsFromIdentifierSearch, orgIdsFromBoundarySearch, 
                orgIdsFromContactMobileNumberSearch, orgIds);
        return orgIds;
    }

    private boolean shouldReturnEmptyList(OrgSearchRequest orgSearchRequest, Set<String> orgIds) {
        if (orgIds.isEmpty()) {
            OrgSearchCriteria criteria = orgSearchRequest.getSearchCriteria();
            return StringUtils.isNotBlank(criteria.getIdentifierType())
                    || StringUtils.isNotBlank(criteria.getIdentifierValue())
                    || StringUtils.isNotBlank(criteria.getBoundaryCode())
                    || StringUtils.isNotBlank(criteria.getContactMobileNumber())
                    || !criteria.getId().isEmpty();
        }
        return false;
    }

    private OrganisationRelatedData fetchRelatedData(List<Organisation> organisations) {
        Set<String> organisationIds = organisations.stream()
                .map(Organisation::getId)
                .collect(Collectors.toSet());

        List<Address> addresses = getAddressBasedOnOrganisationIds(organisationIds);
        log.debug("Fetched {} addresses", addresses.size());

        List<ContactDetails> contactDetails = getContactDetailsBasedOnOrganisationIds(organisationIds);
        log.debug("Fetched {} contact details", contactDetails.size());

        Set<String> functionIds = organisations.stream()
                .flatMap(org -> org.getFunctions().stream())
                .map(Function::getId)
                .collect(Collectors.toSet());
        
        List<Document> documents = getDocumentsBasedOnOrganisationIds(organisationIds, functionIds);
        log.debug("Fetched {} documents", documents.size());

        List<Jurisdiction> jurisdictions = getJurisdictionsBasedOnOrganisationIds(organisationIds);
        log.debug("Fetched {} jurisdictions", jurisdictions.size());

        List<Identifier> identifiers = getIdentifiersBasedOnOrganisationIds(organisationIds);
        log.debug("Fetched {} identifiers", identifiers.size());

        return new OrganisationRelatedData(addresses, contactDetails, documents, jurisdictions, identifiers);
    }

    private static class OrganisationRelatedData {
        final List<Address> addresses;
        final List<ContactDetails> contactDetails;
        final List<Document> documents;
        final List<Jurisdiction> jurisdictions;
        final List<Identifier> identifiers;

        OrganisationRelatedData(List<Address> addresses, List<ContactDetails> contactDetails,
                                List<Document> documents, List<Jurisdiction> jurisdictions,
                                List<Identifier> identifiers) {
            this.addresses = addresses;
            this.contactDetails = contactDetails;
            this.documents = documents;
            this.jurisdictions = jurisdictions;
            this.identifiers = identifiers;
        }
    }

    private Set<String> getOrgIdsForContactNumberBasedOnSearchCriteria(OrgSearchRequest orgSearchRequest) {
        if (StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getContactMobileNumber())) {
            List<Object> preparedStmtListTarget = new ArrayList<>();
            String queryAddress = contactDetailsQueryBuilder.getContactDetailsSearchQueryBasedOnCriteria(orgSearchRequest.getSearchCriteria().getContactMobileNumber(), preparedStmtListTarget);
            Set<String> orgIds = jdbcTemplate.query(queryAddress, contactDetailsOrgIdsRowMapper, preparedStmtListTarget.toArray());
            log.trace("Fetched {} Org Ids for contact details based on Contact Mobile Number search", orgIds.size());
            return orgIds;
        }
        return Collections.emptySet();
    }

    /* Fetch organisation ids based on identifierType and identifierValue search criteria */
    private Set<String> getOrgIdsForIdentifiersBasedOnSearchCriteria(OrgSearchRequest orgSearchRequest) {
        if (StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getIdentifierType()) || StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getIdentifierValue())) {
            List<Object> preparedStmtListTarget = new ArrayList<>();
            String queryIdentifier = taxIdentifierQueryBuilder.getTaxIdentifierSearchQueryBasedOnCriteria(orgSearchRequest.getSearchCriteria().getIdentifierType(), orgSearchRequest.getSearchCriteria().getIdentifierValue(), preparedStmtListTarget);
            Set<String> orgIds = jdbcTemplate.query(queryIdentifier, taxIdentifierOrgIdsRowMapper, preparedStmtListTarget.toArray());
            log.trace("Fetched {} Org Ids for identifiers based on Search criteria", orgIds.size());
            return orgIds;
        }
        return Collections.emptySet();
    }

    /* Fetch organisation ids based on boundaryCode search criteria */
    private Set<String> getOrgIdsForAddressesBasedOnSearchCriteria(OrgSearchRequest orgSearchRequest) {
        if (StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getBoundaryCode())) {
            List<Object> preparedStmtListTarget = new ArrayList<>();
            String queryAddress = addressQueryBuilder.getAddressSearchQueryBasedOnCriteria(orgSearchRequest.getSearchCriteria().getBoundaryCode(), orgSearchRequest.getSearchCriteria().getTenantId(), preparedStmtListTarget);
            Set<String> orgIds = jdbcTemplate.query(queryAddress, addressOrgIdsRowMapper, preparedStmtListTarget.toArray());
            log.trace("Fetched {} Org Ids for addresses based on Boundary Code search", orgIds.size());
            return orgIds;
        }
        return Collections.emptySet();
    }

    /* Get organisations list based on search request */
    private List<Organisation> getOrganisationsBasedOnSearchCriteria(OrgSearchRequest orgSearchRequest, Set<String> orgIds) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = organisationFunctionQueryBuilder.getOrganisationSearchQuery(orgSearchRequest, orgIds, preparedStmtList, false);
        List<Organisation> organisations = jdbcTemplate.query(query, organisationFunctionRowMapper, preparedStmtList.toArray());

        log.trace("Fetched organisations list based on given search criteria");
        return organisations;
    }

    /* Get OrgIds for search result from contact details, identifier and boundaryCode search results and orgIds in request */
    private void getOrgIdsForSearch(OrgSearchRequest orgSearchRequest, Set<String> orgIdsFromIdentifierSearch,Set<String> orgIdsFromBoundarySearch,Set<String> orgIdsFromContactMobileNumberSearch, Set<String> orgIds) {
        SearchCriteriaFlags flags = determineSearchCriteriaFlags(orgSearchRequest);
        ensureOrgIdsListInitialized(orgSearchRequest);
        
        if (shouldReturnEarly(flags, orgIdsFromIdentifierSearch, orgIdsFromBoundarySearch, orgIdsFromContactMobileNumberSearch)) {
            return;
        }
        
        combineOrgIds(orgSearchRequest, flags, orgIdsFromIdentifierSearch, orgIdsFromBoundarySearch, 
                orgIdsFromContactMobileNumberSearch, orgIds);
    }

    private SearchCriteriaFlags determineSearchCriteriaFlags(OrgSearchRequest orgSearchRequest) {
        OrgSearchCriteria criteria = orgSearchRequest.getSearchCriteria();
        return new SearchCriteriaFlags(
                StringUtils.isNotBlank(criteria.getIdentifierType()) || StringUtils.isNotBlank(criteria.getIdentifierValue()),
                StringUtils.isNotBlank(criteria.getBoundaryCode()),
                criteria.getId() != null && !criteria.getId().isEmpty(),
                StringUtils.isNotBlank(criteria.getContactMobileNumber())
        );
    }

    private void ensureOrgIdsListInitialized(OrgSearchRequest orgSearchRequest) {
        if (orgSearchRequest.getSearchCriteria().getId() == null) {
            orgSearchRequest.getSearchCriteria().setId(new ArrayList<>());
        }
    }

    private boolean shouldReturnEarly(SearchCriteriaFlags flags, Set<String> orgIdsFromIdentifierSearch,
                                     Set<String> orgIdsFromBoundarySearch, Set<String> orgIdsFromContactMobileNumberSearch) {
        if (orgIdsFromIdentifierSearch.isEmpty() && flags.isIdentifierSearchCriteriaPresent()) {
            return true;
        }
        if (orgIdsFromContactMobileNumberSearch.isEmpty() && flags.isContactMobileNumberSearchCriteriaPresent()) {
            return true;
        }
        if (orgIdsFromBoundarySearch.isEmpty() && flags.isBoundarySearchCriteriaPresent()) {
            return true;
        }
        return false;
    }

    private void combineOrgIds(OrgSearchRequest orgSearchRequest, SearchCriteriaFlags flags,
                               Set<String> orgIdsFromIdentifierSearch, Set<String> orgIdsFromBoundarySearch,
                               Set<String> orgIdsFromContactMobileNumberSearch, Set<String> orgIds) {
        if (flags.isContactMobileNumberSearchCriteriaPresent()) {
            combineOrgIdsWithContactMobileNumber(orgSearchRequest, flags, orgIdsFromIdentifierSearch,
                    orgIdsFromBoundarySearch, orgIdsFromContactMobileNumberSearch, orgIds);
        } else if (flags.isIdentifierSearchCriteriaPresent()) {
            combineOrgIdsWithIdentifier(orgSearchRequest, flags, orgIdsFromIdentifierSearch,
                    orgIdsFromBoundarySearch, orgIds);
        } else if (flags.isBoundarySearchCriteriaPresent()) {
            combineOrgIdsWithBoundary(orgSearchRequest, flags, orgIdsFromBoundarySearch, orgIds);
        } else {
            orgIds.addAll(orgSearchRequest.getSearchCriteria().getId());
        }
    }

    private void combineOrgIdsWithContactMobileNumber(OrgSearchRequest orgSearchRequest, SearchCriteriaFlags flags,
                                                      Set<String> orgIdsFromIdentifierSearch,
                                                      Set<String> orgIdsFromBoundarySearch,
                                                      Set<String> orgIdsFromContactMobileNumberSearch,
                                                      Set<String> orgIds) {
        orgIds.addAll(orgIdsFromContactMobileNumberSearch);
        if (flags.isIdentifierSearchCriteriaPresent()) {
            orgIds.retainAll(orgIdsFromIdentifierSearch);
        }
        if (flags.isBoundarySearchCriteriaPresent()) {
            orgIds.retainAll(orgIdsFromBoundarySearch);
        }
        if (flags.isOrgIdsSearchCriteriaPresent()) {
            orgIds.retainAll(orgSearchRequest.getSearchCriteria().getId());
        }
    }

    private void combineOrgIdsWithIdentifier(OrgSearchRequest orgSearchRequest, SearchCriteriaFlags flags,
                                            Set<String> orgIdsFromIdentifierSearch,
                                            Set<String> orgIdsFromBoundarySearch, Set<String> orgIds) {
        orgIds.addAll(orgIdsFromIdentifierSearch);
        if (flags.isBoundarySearchCriteriaPresent()) {
            orgIds.retainAll(orgIdsFromBoundarySearch);
        }
        if (flags.isOrgIdsSearchCriteriaPresent()) {
            orgIds.retainAll(orgSearchRequest.getSearchCriteria().getId());
        }
    }

    private void combineOrgIdsWithBoundary(OrgSearchRequest orgSearchRequest, SearchCriteriaFlags flags,
                                          Set<String> orgIdsFromBoundarySearch, Set<String> orgIds) {
        orgIds.addAll(orgIdsFromBoundarySearch);
        if (flags.isOrgIdsSearchCriteriaPresent()) {
            orgIds.retainAll(orgSearchRequest.getSearchCriteria().getId());
        }
    }

    private static class SearchCriteriaFlags {
        private final boolean identifierSearchCriteriaPresent;
        private final boolean boundarySearchCriteriaPresent;
        private final boolean orgIdsSearchCriteriaPresent;
        private final boolean contactMobileNumberSearchCriteriaPresent;

        public SearchCriteriaFlags(boolean identifierSearchCriteriaPresent, boolean boundarySearchCriteriaPresent,
                                  boolean orgIdsSearchCriteriaPresent, boolean contactMobileNumberSearchCriteriaPresent) {
            this.identifierSearchCriteriaPresent = identifierSearchCriteriaPresent;
            this.boundarySearchCriteriaPresent = boundarySearchCriteriaPresent;
            this.orgIdsSearchCriteriaPresent = orgIdsSearchCriteriaPresent;
            this.contactMobileNumberSearchCriteriaPresent = contactMobileNumberSearchCriteriaPresent;
        }

        public boolean isIdentifierSearchCriteriaPresent() {
            return identifierSearchCriteriaPresent;
        }

        public boolean isBoundarySearchCriteriaPresent() {
            return boundarySearchCriteriaPresent;
        }

        public boolean isOrgIdsSearchCriteriaPresent() {
            return orgIdsSearchCriteriaPresent;
        }

        public boolean isContactMobileNumberSearchCriteriaPresent() {
            return contactMobileNumberSearchCriteriaPresent;
        }
    }

    /* Get addresses list based on organisation Ids */
    private List<Address> getAddressBasedOnOrganisationIds(Set<String> organisationIds) {
        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryAddress = addressQueryBuilder.getAddressSearchQuery(organisationIds, preparedStmtListTarget);
        List<Address> addresses = jdbcTemplate.query(queryAddress, addressRowMapper, preparedStmtListTarget.toArray());
        log.trace("Fetched addresses based on organisation Ids");
        return addresses;
    }

    /* Get documents list based on organisation Ids */
    private List<Document> getDocumentsBasedOnOrganisationIds(Set<String> organisationIds, Set<String> functionIds) {
        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryDocument = documentQueryBuilder.getDocumentsSearchQuery(organisationIds, functionIds, preparedStmtListTarget);
        List<Document> documents = jdbcTemplate.query(queryDocument, documentRowMapper, preparedStmtListTarget.toArray());
        log.trace("Fetched documents based on organisation Ids");
        return documents;
    }

    /* Get contact details list based on organisation Ids */
    private List<ContactDetails> getContactDetailsBasedOnOrganisationIds(Set<String> organisationIds) {
        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryContactDetails = contactDetailsQueryBuilder.getContactDetailsSearchQuery(organisationIds, preparedStmtListTarget);
        List<ContactDetails> contactDetails = jdbcTemplate.query(queryContactDetails, contactDetailsRowMapper, preparedStmtListTarget.toArray());
        log.trace("Fetched contactDetails based on organisation Ids");
        return contactDetails;
    }

    /* Get identifiers list based on organisation Ids */
    private List<Identifier> getIdentifiersBasedOnOrganisationIds(Set<String> organisationIds) {
        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryIdentifier = taxIdentifierQueryBuilder.getTaxIdentifierSearchQuery(organisationIds, preparedStmtListTarget);
        List<Identifier> identifiers = jdbcTemplate.query(queryIdentifier, taxIdentifierRowMapper, preparedStmtListTarget.toArray());
        log.trace("Fetched identifiers based on organisation Ids");
        return identifiers;
    }

    /* Get jurisdictions list based on organisation Ids */
    private List<Jurisdiction> getJurisdictionsBasedOnOrganisationIds(Set<String> organisationIds) {
        List<Object> preparedStmtListTarget = new ArrayList<>();
        String queryJurisdictions = jurisdictionQueryBuilder.getJurisdictionSearchQuery(organisationIds, preparedStmtListTarget);
        List<Jurisdiction> jurisdictions = jdbcTemplate.query(queryJurisdictions, jurisdictionRowMapper, preparedStmtListTarget.toArray());
        log.trace("Fetched jurisdictions based on organisation Ids");
        return jurisdictions;
    }

    /* Construct organisation search results based on organisations, addresses, contact details, documents, jurisdictions and identifiers*/
    private List<Organisation> buildOrganisationSearchResult(List<Organisation> organisations, List<Address> addresses, List<ContactDetails> contactDetails, List<Document> documents, List<Jurisdiction> jurisdictions, List<Identifier> identifiers) {
        log.trace("OrganisationRepository::buildOrganisationSearchResult entry");
        for (Organisation organisation: organisations) {
            log.trace("Constructing organisation object for organisation ID: {}", organisation.getId());
            constructOrganizationObject(organisation, addresses, contactDetails, documents, jurisdictions, identifiers);
        }
        log.debug("Constructed {} organisation objects", organisations.size());
        return organisations;
    }
    private void constructOrganizationObject(Organisation organisation, List<Address> addresses, List<ContactDetails> contactDetails, List<Document> documents, List<Jurisdiction> jurisdictions, List<Identifier> identifiers){
        log.trace("OrganisationRepository::constructOrganizationObject entry for organisation ID: {}", organisation.getId());
        if (addresses != null && !addresses.isEmpty()) {
            addAddressToOrganisation(organisation, addresses);
            log.trace("Added {} addresses to organisation {}", addresses.stream().filter(a -> a.getOrgId().equals(organisation.getId())).count(), organisation.getId());
        }
        if (documents != null && !documents.isEmpty()) {
            addDocumentToOrganisation(organisation, documents);
            log.trace("Added documents to organisation {}", organisation.getId());
        }
        if (contactDetails != null && !contactDetails.isEmpty()) {
            addContactDetailsToOrganisation(organisation, contactDetails);
            log.trace("Added {} contact details to organisation {}", contactDetails.stream().filter(c -> c.getOrgId().equals(organisation.getId())).count(), organisation.getId());
        }
        if (jurisdictions != null && !jurisdictions.isEmpty()) {
            addJurisdictionsToOrganisation(organisation, jurisdictions);
            log.trace("Added {} jurisdictions to organisation {}", jurisdictions.stream().filter(j -> j.getOrgId().equals(organisation.getId())).count(), organisation.getId());
        }
        if (identifiers != null && !identifiers.isEmpty()) {
            addIdentifiersToOrganisation(organisation, identifiers);
            log.trace("Added {} identifiers to organisation {}", identifiers.stream().filter(i -> i.getOrgId().equals(organisation.getId())).count(), organisation.getId());
        }
    }

    private void addIdentifiersToOrganisation(Organisation organisation, List<Identifier> identifiers) {
        organisation.setIdentifiers(new ArrayList<>());
        for (Identifier identifier: identifiers) {
            if (identifier.getOrgId().equals(organisation.getId()) && organisation.getIdentifiers().stream().noneMatch(i -> i.getId().equals(identifier.getId()))) {
                organisation.getIdentifiers().add(identifier);
            }
        }
    }

    private void addJurisdictionsToOrganisation(Organisation organisation, List<Jurisdiction> jurisdictions) {
        organisation.setJurisdiction(new ArrayList<>());
        for (Jurisdiction jurisdiction: jurisdictions) {
            // Add jurisdiction from list of jurisdictions to organisation based on orgId.
            // Check if orgId in jurisdiction is equal to orgId in organisation and if organisation jurisdictions does not contain the jurisdiction to be added
            if (jurisdiction.getOrgId().equals(organisation.getId()) && organisation.getJurisdiction().stream().noneMatch(i -> i.getId().equals(jurisdiction.getId()))) {
                organisation.getJurisdiction().add(jurisdiction);
            }
        }
    }

    private void addContactDetailsToOrganisation(Organisation organisation, List<ContactDetails> contactDetails) {
        organisation.setContactDetails(new ArrayList<>());
        for (ContactDetails contactDetail: contactDetails) {
            if (contactDetail.getOrgId().equals(organisation.getId()) && organisation.getContactDetails().stream().noneMatch(c -> c.getId().equals(contactDetail.getId()))) {
                organisation.getContactDetails().add(contactDetail);
            }
        }
    }

    private void addDocumentToOrganisation(Organisation organisation, List<Document> documents) {
        organisation.setDocuments(new ArrayList<>());
        if (organisation.getFunctions() != null) {
            organisation.getFunctions().forEach(f -> f.setDocuments(new ArrayList<>()));
        }

        for (Document document: documents) {
            if (StringUtils.isNotBlank(document.getOrgId()) && (document.getOrgId().equals(organisation.getId()) && organisation.getDocuments().stream().noneMatch(d -> d.getId().equals(document.getId())))) {
                    organisation.getDocuments().add(document);

            }
            if (organisation.getFunctions() != null && StringUtils.isNotBlank(document.getOrgFunctionId())) {
                for (Function function: organisation.getFunctions()) {
                    if (document.getOrgFunctionId().equals(function.getId()) && function.getDocuments().stream().noneMatch(d -> d.getId().equals(document.getId()))) {
                        organisation.getDocuments().add(document);
                    }
                }
            }
        }
    }

    private void addAddressToOrganisation(Organisation organisation, List<Address> addresses) {
        organisation.setOrgAddress(new ArrayList<>());
        for (Address address: addresses) {
            if (address.getOrgId().equals(organisation.getId()) && organisation.getOrgAddress().stream().noneMatch(a -> a.getId().equals(address.getId()))) {
                organisation.getOrgAddress().add(address);
            }
        }
    }

    public Integer getOrganisationsCount(OrgSearchRequest orgSearchRequest) {
        List<Object> preparedStatement = new ArrayList<>();


        Set<String> orgIdsFromIdentifierSearch = getOrgIdsForIdentifiersBasedOnSearchCriteria(orgSearchRequest);
        Set<String> orgIdsFromBoundarySearch = getOrgIdsForAddressesBasedOnSearchCriteria(orgSearchRequest);
        Set<String> orgIdsFromContactMobileNumberSearch = getOrgIdsForContactNumberBasedOnSearchCriteria(orgSearchRequest);

        Set<String> orgIds = new HashSet<>();
        getOrgIdsForSearch(orgSearchRequest, orgIdsFromIdentifierSearch, orgIdsFromBoundarySearch, orgIdsFromContactMobileNumberSearch, orgIds);

        // If OrgIds are empty and request is present in search criteria
        if (orgIds.isEmpty() &&
                (StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getIdentifierType())
                        || StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getIdentifierValue())
                        || StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getBoundaryCode())
                        || StringUtils.isNotBlank(orgSearchRequest.getSearchCriteria().getContactMobileNumber())
                        || !orgSearchRequest.getSearchCriteria().getId().isEmpty())) {
            return 0;
        }
        
        String query = organisationFunctionQueryBuilder.getSearchCountQueryString(orgSearchRequest, orgIds, preparedStatement);

        if (query == null)
            return 0;

        Integer count = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
        log.debug("Total organisation count: {}", count);
        return count;
    }
}
