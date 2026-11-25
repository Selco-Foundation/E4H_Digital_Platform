package facility.web.models;

/**
 * Lifecycle of a registered-mobile-number change request. Only {@link #PENDING} can move, and
 * only once: to {@link #APPROVED} or {@link #REJECTED} by the CRM, or to {@link #CANCELLED} by
 * the requesting user.
 */
public enum MobileChangeRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
