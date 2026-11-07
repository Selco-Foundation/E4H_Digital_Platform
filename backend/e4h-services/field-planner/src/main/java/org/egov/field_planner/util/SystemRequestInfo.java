package org.egov.field_planner.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A RequestInfo identifying this service rather than the person whose request triggered it.
 *
 * Identical copies live in field-planner, field-planner-activity and amc-scheduler-service - keep
 * them in sync. (These services share no library; the convention here is a copy per module.)
 *
 * <h3>Why validation lookups must not run as the user</h3>
 * Facility search is scoped to the caller's own jurisdiction. The lookups in these services are
 * not the user browsing - they resolve facilities by id to check that a plan, activity or AMC
 * configuration refers to something real, and they legitimately touch facilities the triggering
 * user does not hold. Worse, every one of them reads a missing row as "does not exist" and aborts
 * the user's entire batch with that message. Calling as the service keeps these lookups unscoped,
 * which is both correct and the reason those error messages need no change.
 *
 * <h3>Why this is not a privilege escalation</h3>
 * The gateway re-attaches userInfo from the authenticated token on anything arriving from outside,
 * so a client cannot claim this identity for itself. Service-to-service calls run behind the
 * gateway and keep the identity they build, which is exactly the intent.
 */
public final class SystemRequestInfo {

    public static final String SYSTEM_USER_ROLE = "SYSTEM_USER";
    public static final String SYSTEM_USER_TYPE = "SYSTEM";

    private SystemRequestInfo() {
    }

    /** Copies the caller's identity but presents it as this service, preserving the auth token. */
    public static RequestInfo of(RequestInfo requestInfo) {
        User caller = requestInfo == null ? null : requestInfo.getUserInfo();
        String tenantId = caller == null ? null : caller.getTenantId();

        List<Role> roles = new ArrayList<>();
        roles.add(Role.builder().code(SYSTEM_USER_ROLE).name("System User").tenantId(tenantId).build());

        User system = User.builder()
                .uuid(caller == null ? null : caller.getUuid())
                .userName(caller == null ? null : caller.getUserName())
                .type(SYSTEM_USER_TYPE)
                .tenantId(tenantId)
                .roles(roles)
                .build();

        return RequestInfo.builder()
                .apiId(requestInfo == null ? null : requestInfo.getApiId())
                .authToken(requestInfo == null ? null : requestInfo.getAuthToken())
                .userInfo(system)
                .build();
    }
}
