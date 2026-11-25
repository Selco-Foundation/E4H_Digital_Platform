package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.models.core.URLParams;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.HRMSUtils;
import org.egov.util.OrganisationConstant;
import org.egov.util.OrganisationUtil;
import org.egov.util.PasswordPolicy;
import org.egov.util.PasswordScopeGuard;
import org.egov.util.UserUtil;
import org.egov.validator.OrganisationServiceValidator;
import org.egov.validator.OrganisationUserServiceValidator;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * The organisation's admin user, built from its PoC details.
 *
 * One flow for both org types: a vendor org's PoC becomes a VENDOR_ADMIN, a platform org's an
 * ORG_ADMIN. On create the admin is made in HRMS before the organisation is written, so a
 * failure leaves no organisation behind. On update a changed PoC moves the admin role from the
 * previous PoC user to the new one, and the change is audited.
 *
 * The admin is granted a country-wide jurisdiction under the admin role. The org form carries no
 * jurisdictions to derive a narrower one from; the existing anti-escalation check still applies,
 * so only an unscoped caller can create one.
 */
@Service
@Slf4j
public class PocAdminService {

    static final String PLATFORM_ORG_TYPE = "PLATFORM";
    static final String VENDOR_ADMIN_ROLE_NAME = "Vendor Administrator";
    private static final String ROLE_TENANT = "in";
    private static final String COUNTRY_BOUNDARY = "India";
    private static final String COUNTRY_BOUNDARY_TYPE = "Country";
    private static final String ADMIN_HIERARCHY = "ADMIN";

    private final OrganisationUserServiceValidator userValidator;
    private final OrganisationUserEnrichmentService userEnrichment;
    private final OrganisationUserRepository orgUserRepository;
    private final OrganizationProducer producer;
    private final Configuration config;
    private final HRMSUtils hrmsUtils;
    private final UserUtil userUtil;
    private final OrganisationUtil organisationUtil;
    private final CredentialsNotificationService credentialsNotificationService;
    private final PasswordScopeGuard passwordScopeGuard;

    @Autowired
    public PocAdminService(OrganisationUserServiceValidator userValidator, OrganisationUserEnrichmentService userEnrichment,
                           OrganisationUserRepository orgUserRepository, OrganizationProducer producer, Configuration config,
                           HRMSUtils hrmsUtils, UserUtil userUtil, OrganisationUtil organisationUtil,
                           CredentialsNotificationService credentialsNotificationService,
                           PasswordScopeGuard passwordScopeGuard) {
        this.userValidator = userValidator;
        this.userEnrichment = userEnrichment;
        this.orgUserRepository = orgUserRepository;
        this.producer = producer;
        this.config = config;
        this.hrmsUtils = hrmsUtils;
        this.userUtil = userUtil;
        this.organisationUtil = organisationUtil;
        this.credentialsNotificationService = credentialsNotificationService;
        this.passwordScopeGuard = passwordScopeGuard;
    }

    /**
     * An organisation's PoC details in plaintext. Taken before enrichment, which replaces the
     * phone with its encrypted form.
     */
    public record PocDetails(String name, String phone, String email, String userName, String password) {

        public static PocDetails of(Organisation organisation) {
            return new PocDetails(organisation.getOrgPocName(), organisation.getOrgPocPhone(),
                    organisation.getOrgPocEmail(), organisation.getOrgPocUsername(), organisation.getOrgPocPassword());
        }

        /** Field by field, ignoring case where the field is case-insensitive. Password is not a PoC detail. */
        boolean sameAs(PocDetails other) {
            return Objects.equals(StringUtils.trimToNull(name), StringUtils.trimToNull(other.name))
                    && Objects.equals(StringUtils.trimToNull(phone), StringUtils.trimToNull(other.phone))
                    && StringUtils.equalsIgnoreCase(StringUtils.trimToNull(email), StringUtils.trimToNull(other.email))
                    && StringUtils.equalsIgnoreCase(StringUtils.trimToNull(userName), StringUtils.trimToNull(other.userName));
        }

        @Override
        public String toString() {
            return "PocDetails[userName=" + userName + "]";
        }
    }

    /**
     * A PoC admin whose HRMS writes are done, waiting for the organisation to be written before
     * its org link is pushed and its email sent.
     */
    public static final class Pending {
        private final OrgUserRequest linkRequest;
        private final String password;
        private final boolean reset;
        private final String organisationName;
        private final PocAdmin result;
        private final User user;

        Pending(OrgUserRequest linkRequest, String password, boolean reset, String organisationName, PocAdmin result, User user) {
            this.linkRequest = linkRequest;
            this.password = password;
            this.reset = reset;
            this.organisationName = organisationName;
            this.result = result;
            this.user = user;
        }

        PocAdmin result() {
            return result;
        }
    }

    /** The admin role for an org type. */
    public static Role adminRoleFor(String orgType) {
        if (PLATFORM_ORG_TYPE.equalsIgnoreCase(StringUtils.trimToEmpty(orgType))) {
            return Role.builder().code(OrganisationConstant.ORG_ADMIN_ROLE_CODE)
                    .name(OrganisationConstant.ORG_ADMIN_ROLE_NAME).tenantId(ROLE_TENANT).build();
        }
        return Role.builder().code(OrganisationConstant.VENDOR_ADMIN)
                .name(VENDOR_ADMIN_ROLE_NAME).tenantId(ROLE_TENANT).build();
    }

    // ------------------------------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------------------------------

    /**
     * Refuses an organisation whose PoC username, contact or email already belongs to a user,
     * naming every field that clashed. Run before anything is written.
     */
    public void checkCreateConflicts(RequestInfo requestInfo, List<PocDetails> pocs) {
        Map<String, String> errors = new LinkedHashMap<>();
        Set<String> seenUserNames = new HashSet<>();
        for (PocDetails poc : pocs) {
            if (!seenUserNames.add(poc.userName().toLowerCase(Locale.ROOT))) {
                errors.put("ORG_POC_USERNAME_EXISTS", "Two organisations in this request share the PoC username " + poc.userName());
            }
            if (!CollectionUtils.isEmpty(hrmsUtils.getUserByUsername(wrap(requestInfo), poc.userName()))) {
                errors.put("ORG_POC_USERNAME_EXISTS", "A user with the PoC username " + poc.userName() + " already exists");
            }
            if (!CollectionUtils.isEmpty(hrmsUtils.getUserByPhoneNumber(wrap(requestInfo), poc.phone()))) {
                errors.put("ORG_POC_PHONE_EXISTS", "A user with the PoC contact number already exists");
            }
            if (emailInUse(requestInfo, poc.email(), null)) {
                errors.put("ORG_POC_EMAIL_EXISTS", "A user with the PoC email " + poc.email() + " already exists");
            }
        }
        if (!errors.isEmpty()) {
            log.error("Organisation PoC clashes with existing users: {}", errors.keySet());
            throw new CustomException(errors);
        }
    }

    /**
     * Creates each organisation's admin user in HRMS and egov-user. The organisations must be
     * enriched (they need their ids) and not yet written.
     */
    public List<Pending> createAdmins(RequestInfo requestInfo, List<Organisation> organisations, List<PocDetails> pocs) {
        List<Pending> pending = new ArrayList<>();
        for (int i = 0; i < organisations.size(); i++) {
            Organisation organisation = organisations.get(i);
            PocDetails poc = pocs.get(i);
            pending.add(createNewAdmin(requestInfo, organisation, poc));
        }
        return pending;
    }

    /**
     * Pushes each admin's org link and sends their email. Call after the organisations have been
     * pushed: an email must never go out for an account whose organisation was not written.
     */
    public List<PocAdmin> publish(RequestInfo requestInfo, List<Pending> pending) {
        List<PocAdmin> results = new ArrayList<>();
        for (Pending p : pending) {
            if (p.linkRequest != null) {
                producer.push(config.getCreateOrgUserTopic(), p.linkRequest);
            }
            if (p.password != null) {
                CredentialsNotificationService.Credentials credentials = new CredentialsNotificationService.Credentials(
                        p.user.getEmailId(), p.user.getName(), p.organisationName, p.result.getRole(),
                        p.user.getUserName(), p.password, p.reset);
                p.result.setCredentialsEmailSent(credentialsNotificationService.send(credentials));
            }
            results.add(p.result);
        }
        return results;
    }

    private Pending createNewAdmin(RequestInfo requestInfo, Organisation organisation, PocDetails poc) {
        Role adminRole = adminRoleFor(organisation.getOrgType());
        User user = User.builder()
                .tenantId(organisation.getTenantId())
                .name(poc.name())
                .userName(poc.userName())
                .mobileNumber(poc.phone())
                .emailId(poc.email())
                .password(poc.password())
                .active(true)
                .roles(new ArrayList<>(List.of(adminRole)))
                .jurisdictions(new ArrayList<>(List.of(countryJurisdiction(adminRole))))
                .build();
        OrgUserRequest linkRequest = OrgUserRequest.builder()
                .requestInfo(requestInfo)
                .organizationId(organisation.getId())
                .user(user)
                .build();
        String password = userValidator.createPocAdmin(linkRequest, organisation);
        User created = linkRequest.getUser();
        // createPocAdmin returns with an id only when it re-linked an existing row; the conflict
        // checks make that impossible here, so this is always a new link.
        userEnrichment.enrichOrgUserRequestOnCreate(linkRequest, requestInfo);
        log.info("PoC admin {} created for organisation {}", created.getUuid(), organisation.getId());
        PocAdmin result = PocAdmin.builder()
                .organisationId(organisation.getId())
                .userId(created.getUuid())
                .userName(created.getUserName())
                .role(adminRole.getCode())
                .created(true)
                .build();
        return new Pending(linkRequest, password, false, organisation.getName(), result, created);
    }

    // ------------------------------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------------------------------

    /** True when any PoC field differs. A payload that merely carries the same values is not a change. */
    public static boolean pocChanged(Organisation incoming, Organisation existing) {
        return !PocDetails.of(incoming).sameAs(PocDetails.of(existing));
    }

    /**
     * Moves the PoC admin role to the user built from the new PoC details, and takes it from the
     * previous PoC user. Call only when {@link #pocChanged} is true, with `existing` read from the
     * database (phone decrypted) and `incoming` not yet enriched (phone in plaintext).
     *
     * The new PoC is matched to an existing user by username, then by contact number. If none
     * exists one is created, with the supplied or derived password. An existing user keeps their
     * password unless a PoC password was supplied.
     *
     * @return the pending org link and email, and the audit record to push with it
     */
    public PocChangeResult applyChange(RequestInfo requestInfo, Organisation incoming, Organisation existing) {
        OrganisationServiceValidator.validatePocDetails(incoming);
        PocDetails newPoc = PocDetails.of(incoming);
        if (StringUtils.isNotEmpty(newPoc.password())) {
            PasswordPolicy.validate(newPoc.password());
        }
        Role adminRole = adminRoleFor(incoming.getOrgType());

        Employee previous = StringUtils.isBlank(existing.getOrgPocUsername()) ? null
                : first(hrmsUtils.getUserByUsername(wrap(requestInfo), existing.getOrgPocUsername()));
        Employee target = resolveTarget(requestInfo, newPoc);

        Pending pending;
        if (target == null) {
            pending = createNewAdmin(requestInfo, incoming, newPoc);
        } else {
            pending = pointToExisting(requestInfo, incoming, newPoc, target, adminRole,
                    previous != null && sameUser(previous, target));
        }

        // Only someone still in this organisation was its admin: a username can outlive the link.
        if (previous != null && !sameUser(previous, target)
                && linkedTo(requestInfo, previous.getUser().getUuid(), incoming.getId())) {
            revokeAdminRole(requestInfo, previous, adminRole);
        }

        PocChangeAudit audit = PocChangeAudit.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(incoming.getTenantId())
                .organisationId(incoming.getId())
                .oldPocName(existing.getOrgPocName())
                .oldPocPhone(encrypt(existing.getOrgPocPhone()))
                .oldPocEmail(existing.getOrgPocEmail())
                .oldPocUsername(existing.getOrgPocUsername())
                .newPocName(newPoc.name())
                .newPocPhone(encrypt(newPoc.phone()))
                .newPocEmail(newPoc.email())
                .newPocUsername(newPoc.userName())
                .previousUserId(previous != null && previous.getUser() != null ? previous.getUser().getUuid() : null)
                .newUserId(pending.result().getUserId())
                .changedBy(requestInfo.getUserInfo().getUuid())
                .changedTime(System.currentTimeMillis())
                .build();
        return new PocChangeResult(pending, audit);
    }

    /**
     * What {@link #applyChange} or {@link #resetPassword} leaves to publish once the organisation
     * update is pushed. `audit` is null for a password reset, which does not change the PoC.
     */
    public record PocChangeResult(Pending pending, PocChangeAudit audit) {
    }

    /** Pushes the audit row if any, then the org link and email. Call after the organisation update is pushed. */
    public PocAdmin publishChange(RequestInfo requestInfo, PocChangeResult change) {
        if (change.audit() != null) {
            producer.push(config.getPocChangeAuditTopic(), change.audit());
        }
        return publish(requestInfo, List.of(change.pending())).get(0);
    }

    /**
     * Resets the PoC admin's password: an org update that keeps the PoC but carries
     * `orgPocPassword`. The PoC is the user named by the stored PoC username.
     */
    public PocChangeResult resetPassword(RequestInfo requestInfo, Organisation incoming, Organisation existing) {
        String password = incoming.getOrgPocPassword();
        PasswordPolicy.validate(password);
        Employee poc = StringUtils.isBlank(existing.getOrgPocUsername()) ? null
                : first(hrmsUtils.getUserByUsername(wrap(requestInfo), existing.getOrgPocUsername()));
        if (poc == null || poc.getUser() == null) {
            throw new CustomException("ORG_POC_USER_NOT_FOUND", "The organisation's PoC has no user account to reset");
        }
        User user = poc.getUser();
        passwordScopeGuard.require(requestInfo, incoming.getId(), user.getUuid(), user.getRoles());
        userValidator.setPassword(requestInfo, user, password);
        log.info("PoC admin {} of organisation {} had their password reset", user.getUuid(), incoming.getId());
        PocAdmin result = PocAdmin.builder()
                .organisationId(incoming.getId())
                .userId(user.getUuid())
                .userName(user.getUserName())
                .role(adminRoleFor(incoming.getOrgType()).getCode())
                .created(false)
                .build();
        return new PocChangeResult(new Pending(null, password, true, incoming.getName(), result, user), null);
    }

    /**
     * The existing user the new PoC details identify, or null when there is none. Username and
     * contact number must not lead to two different people, and the email must not belong to
     * someone else.
     */
    private Employee resolveTarget(RequestInfo requestInfo, PocDetails poc) {
        Employee byUserName = first(hrmsUtils.getUserByUsername(wrap(requestInfo), poc.userName()));
        Employee byPhone = first(hrmsUtils.getUserByPhoneNumber(wrap(requestInfo), poc.phone()));
        if (byUserName != null && byPhone != null && !sameUser(byUserName, byPhone)) {
            throw new CustomException("ORG_POC_PHONE_EXISTS",
                    "The PoC contact number belongs to a different user than the PoC username " + poc.userName());
        }
        Employee target = byUserName != null ? byUserName : byPhone;
        String targetUuid = target != null && target.getUser() != null ? target.getUser().getUuid() : null;
        if (emailInUse(requestInfo, poc.email(), targetUuid)) {
            throw new CustomException("ORG_POC_EMAIL_EXISTS", "A user with the PoC email " + poc.email() + " already exists");
        }
        return target;
    }

    /**
     * Makes an existing user the PoC admin: grants the role and links them to the organisation.
     * Their profile is updated to the new PoC details only when they were already the PoC -
     * otherwise this is someone else's account, and only the role is theirs to change.
     */
    private Pending pointToExisting(RequestInfo requestInfo, Organisation organisation, PocDetails poc, Employee target,
                                    Role adminRole, boolean alreadyPoc) {
        User user = target.getUser();
        requireNoOtherOrg(requestInfo, user.getUuid(), organisation.getId());

        if (alreadyPoc) {
            user.setName(poc.name());
            user.setMobileNumber(poc.phone());
            user.setEmailId(poc.email());
        }
        grantAdminRole(target, adminRole);
        List<Employee> updated = hrmsUtils.updateHRMSUser(EmployeeRequest.builder()
                .requestInfo(requestInfo).employees(List.of(target)).build());
        if (CollectionUtils.isEmpty(updated)) {
            throw new CustomException("HRMS_UPDATE", "Error occurred while making the new PoC the organisation's admin");
        }
        User settled = updated.get(0).getUser();

        String password = null;
        if (StringUtils.isNotEmpty(poc.password())) {
            password = poc.password();
            passwordScopeGuard.require(requestInfo, organisation.getId(), settled.getUuid(), settled.getRoles());
            userValidator.setPassword(requestInfo, settled, password);
        }

        // A link that exists, even soft-deleted, is reactivated in place; only a missing one is pushed.
        OrgUserRequest linkRequest = null;
        if (orgUserRepository.ensureActiveOrgUserLinkOrReactivateDeleted(settled.getUuid(), organisation.getId()).isEmpty()) {
            linkRequest = OrgUserRequest.builder()
                    .requestInfo(requestInfo)
                    .organizationId(organisation.getId())
                    .userId(settled.getUuid())
                    .user(settled)
                    .build();
            userEnrichment.enrichOrgUserRequestOnCreate(linkRequest, requestInfo);
        }
        log.info("Existing user {} is now the PoC admin of organisation {}", settled.getUuid(), organisation.getId());
        PocAdmin result = PocAdmin.builder()
                .organisationId(organisation.getId())
                .userId(settled.getUuid())
                .userName(settled.getUserName())
                .role(adminRole.getCode())
                .created(false)
                .build();
        return new Pending(linkRequest, password, true, organisation.getName(), result, settled);
    }

    private List<OrgUser> activeLinks(RequestInfo requestInfo, String userUuid) {
        OrgUserSearchRequest search = OrgUserSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(OrgUserSearchCriteria.builder().userId(List.of(userUuid)).tenantId(config.getGlobalTenantId()).build())
                .build();
        List<OrgUser> links = orgUserRepository.getOrgUsers(search, URLParams.builder().limit(100).offset(0).build());
        return links == null ? List.of() : links.stream().filter(l -> !Boolean.TRUE.equals(l.getIsDeleted())).toList();
    }

    private boolean linkedTo(RequestInfo requestInfo, String userUuid, String organisationId) {
        return activeLinks(requestInfo, userUuid).stream().anyMatch(l -> organisationId.equals(l.getOrganizationId()));
    }

    private void requireNoOtherOrg(RequestInfo requestInfo, String userUuid, String organisationId) {
        boolean inOtherOrg = activeLinks(requestInfo, userUuid).stream()
                .anyMatch(l -> !organisationId.equals(l.getOrganizationId()));
        if (inOtherOrg) {
            throw new CustomException("ORG_POC_IN_OTHER_ORG", "The new PoC already belongs to another organisation");
        }
    }

    /**
     * Adds the admin role and a country-wide jurisdiction under it. An active country
     * jurisdiction already present is extended rather than duplicated; one with no roles already
     * applies to every role, so it needs nothing.
     *
     * Someone who already holds the role keeps the jurisdictions they have: they are already an
     * admin with a scope someone chose, and becoming the PoC is no reason to widen it.
     */
    static void grantAdminRole(Employee employee, Role adminRole) {
        User user = employee.getUser();
        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }
        if (user.getRoles().stream().anyMatch(r -> r != null && adminRole.getCode().equalsIgnoreCase(r.getCode()))) {
            return;
        }
        user.getRoles().add(adminRole);
        if (employee.getJurisdictions() == null) {
            employee.setJurisdictions(new ArrayList<>());
        }
        Optional<Jurisdiction> country = employee.getJurisdictions().stream()
                .filter(j -> j != null && COUNTRY_BOUNDARY.equalsIgnoreCase(j.getBoundary()) && !Boolean.FALSE.equals(j.getIsActive()))
                .findFirst();
        if (country.isEmpty()) {
            employee.getJurisdictions().add(countryJurisdiction(adminRole));
            return;
        }
        List<Role> scoped = country.get().getRoles();
        if (CollectionUtils.isEmpty(scoped)) {
            return;
        }
        if (scoped.stream().noneMatch(r -> r != null && adminRole.getCode().equalsIgnoreCase(r.getCode()))) {
            List<Role> extended = new ArrayList<>(scoped);
            extended.add(adminRole);
            country.get().setRoles(extended);
        }
    }

    /**
     * Takes the admin role from the previous PoC user.
     *
     * A jurisdiction scoped only to the admin role is deactivated, not left with an empty role
     * list: an empty list means "every role", so leaving it would turn the admin's country-wide
     * grant into a country-wide grant for everything else they hold.
     */
    private void revokeAdminRole(RequestInfo requestInfo, Employee previous, Role adminRole) {
        if (!stripAdminRole(previous, adminRole)) {
            log.info("Previous PoC user {} did not hold {}; nothing to revoke",
                    previous.getUser().getUuid(), adminRole.getCode());
            return;
        }
        List<Employee> updated = hrmsUtils.updateHRMSUser(EmployeeRequest.builder()
                .requestInfo(requestInfo).employees(List.of(previous)).build());
        if (CollectionUtils.isEmpty(updated)) {
            throw new CustomException("HRMS_UPDATE", "Error occurred while removing the admin role from the previous PoC");
        }
        log.info("Removed {} from previous PoC user {}", adminRole.getCode(), previous.getUser().getUuid());
    }

    /** @return true when the employee held the role and it was removed */
    static boolean stripAdminRole(Employee employee, Role adminRole) {
        String code = adminRole.getCode();
        User user = employee.getUser();
        boolean held = user.getRoles() != null
                && user.getRoles().removeIf(r -> r != null && code.equalsIgnoreCase(r.getCode()));
        if (employee.getJurisdictions() != null) {
            for (Jurisdiction jurisdiction : employee.getJurisdictions()) {
                if (jurisdiction == null || CollectionUtils.isEmpty(jurisdiction.getRoles())) {
                    continue;
                }
                List<Role> retained = new ArrayList<>(jurisdiction.getRoles());
                if (!retained.removeIf(r -> r != null && code.equalsIgnoreCase(r.getCode()))) {
                    continue;
                }
                held = true;
                jurisdiction.setRoles(retained);
                if (retained.isEmpty()) {
                    jurisdiction.setIsActive(false);
                }
            }
        }
        return held;
    }

    private static Jurisdiction countryJurisdiction(Role adminRole) {
        return Jurisdiction.builder()
                .hierarchy(ADMIN_HIERARCHY)
                .boundary(COUNTRY_BOUNDARY)
                .boundaryType(COUNTRY_BOUNDARY_TYPE)
                .tenantId(ROLE_TENANT)
                .isActive(true)
                .roles(new ArrayList<>(List.of(adminRole)))
                .build();
    }

    /** True when an egov user other than `exceptUuid` has this email. */
    private boolean emailInUse(RequestInfo requestInfo, String email, String exceptUuid) {
        Map<String, Object> search = new HashMap<>();
        search.put("RequestInfo", requestInfo);
        search.put("tenantId", ROLE_TENANT);
        search.put("emailId", email);
        UserDetailResponse response = userUtil.userCall(search,
                new StringBuilder(config.getUserHost() + config.getUserSearchEndpoint()));
        return response != null && response.getUser() != null && response.getUser().stream()
                .anyMatch(u -> exceptUuid == null || !exceptUuid.equals(u.getUuid()));
    }

    private String encrypt(String phone) {
        return StringUtils.isBlank(phone) ? phone : organisationUtil.encryptMobileNumber(phone);
    }

    private static boolean sameUser(Employee a, Employee b) {
        return a != null && b != null && a.getUser() != null && b.getUser() != null
                && Objects.equals(a.getUser().getUuid(), b.getUser().getUuid());
    }

    private static Employee first(List<Employee> employees) {
        return CollectionUtils.isEmpty(employees) ? null : employees.get(0);
    }

    private static Map<String, Object> wrap(RequestInfo requestInfo) {
        return Map.of("RequestInfo", requestInfo);
    }
}
