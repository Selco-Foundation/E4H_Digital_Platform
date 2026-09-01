package facility.util;

import facility.web.models.FacilityKibanaIndex;

/**
 * Carries AMC fields from one {@link FacilityKibanaIndex} to another.
 *
 * <p>AMC data is owned by amc-scheduler-service and lives <em>only</em> on the health facility index -
 * it is never persisted in the facility table (see {@code FacilityService#updateAmcIndexFields}).
 * That makes it the one part of the document facility-registry cannot rebuild from its own database,
 * so any code path that constructs a fresh document for an already-indexed facility must copy the
 * indexed values forward or they would be silently dropped on the next re-index.
 *
 * <p>{@code systemType} rides along for the same reason. It is not AMC data - field-planner owns it,
 * captured on the facility's installation plan - but it reaches the index through the same AMC push
 * and is equally unrebuildable from facility-registry's own tables, so it has to be carried forward
 * here too.
 */
public final class FacilityAmcFieldsHelper {

    private FacilityAmcFieldsHelper() {
    }

    /**
     * Copies every AMC field, plus {@code systemType}, from {@code from} onto {@code to}. No-op when
     * either side is null.
     */
    public static void copyAmcFields(FacilityKibanaIndex from, FacilityKibanaIndex to) {
        if (from == null || to == null) {
            return;
        }

        to.setSystemType(from.getSystemType());

        to.setAmcInstallationDate(from.getAmcInstallationDate());
        to.setAmcApplicable(from.getAmcApplicable());
        to.setAmcApplicableYears(from.getAmcApplicableYears());
        to.setAmcFrequencyMonths(from.getAmcFrequencyMonths());
        to.setAmcValidTill(from.getAmcValidTill());
        to.setAmcMappedVendorName(from.getAmcMappedVendorName());
        to.setAmcMappedVendorUserName(from.getAmcMappedVendorUserName());

        to.setAmcDueDate1(from.getAmcDueDate1());
        to.setAmcDueDate2(from.getAmcDueDate2());
        to.setAmcDueDate3(from.getAmcDueDate3());
        to.setAmcDueDate4(from.getAmcDueDate4());
        to.setAmcDueDate5(from.getAmcDueDate5());
        to.setAmcDueDate6(from.getAmcDueDate6());
        to.setAmcDueDate7(from.getAmcDueDate7());
        to.setAmcDueDate8(from.getAmcDueDate8());
        to.setAmcDueDate9(from.getAmcDueDate9());
        to.setAmcDueDate10(from.getAmcDueDate10());

        to.setAmcVisitDate1(from.getAmcVisitDate1());
        to.setAmcVisitDate2(from.getAmcVisitDate2());
        to.setAmcVisitDate3(from.getAmcVisitDate3());
        to.setAmcVisitDate4(from.getAmcVisitDate4());
        to.setAmcVisitDate5(from.getAmcVisitDate5());
        to.setAmcVisitDate6(from.getAmcVisitDate6());
        to.setAmcVisitDate7(from.getAmcVisitDate7());
        to.setAmcVisitDate8(from.getAmcVisitDate8());
        to.setAmcVisitDate9(from.getAmcVisitDate9());
        to.setAmcVisitDate10(from.getAmcVisitDate10());
    }
}
