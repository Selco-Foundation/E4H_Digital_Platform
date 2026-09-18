import { useQuery, useQueryClient } from "react-query";
import { ActivityService } from "../services/Activity";

const formatAddress = (address = {}) => {
  const parts = [
    address.doorNo,
    address.buildingName,
    address.addressLine1,
    address.addressLine2,
    address.street,
    address.landmark,
    address.city,
    address.pincode,
  ].filter(Boolean);

  return parts.length ? parts.join(", ") : null;
}

const formatFacilities = (facilities) => {
  return facilities?.map((row) => {
    const activityFacility = row?.activityFacility || {};
    const facility = activityFacility?.facility || {};
    const address = facility?.address || {};

    return {
      id: activityFacility?.id,
      facilityId: activityFacility?.facilityId,
      facilityName: facility?.facility_name,
      facilityType: facility?.facility_type,
      address: formatAddress(address),
      status: activityFacility?.status,
      block: facility?.boundary?.block || address?.block,
      district: facility?.boundary?.district || address?.district,
      assigned: activityFacility?.staffVendorName,
      activityCode: activityFacility?.activityCode,
      activityType: activityFacility?.activityType,
      pocNumber: activityFacility?.pocNumber,
      startDate: activityFacility?.startDate,
      endDate: activityFacility?.endDate,
      raw: activityFacility,
    };
  });
}

const fetchFacilities = async (filter, limit, offset) => {
  const facilitiesResponse = await ActivityService.fetchActivityFacilities(filter, limit, offset);
  return {
    facilities: formatFacilities(facilitiesResponse?.facility),
    totalCount: facilitiesResponse?.TotalCount,
  }
}

const useActivityFacility = (projectQueryFilter, pageSize, pageOffset) => {

  const { project, facilityFilterQuery, facilitySearchQuery } = projectQueryFilter;

  const filter = {
    ActivityFacility: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    },
    moduleSearchCriteria: {}
  };

  if (project?.fieldPlanId?.length) {
    filter.ActivityFacility.fieldPlanIds = project.fieldPlanId;
  }

  if (project?.activityCode?.length) {
    filter.ActivityFacility.activityCodes = project.activityCode;
  }

  if (project?.tenantId) {
    filter.ActivityFacility.tenantId = project.tenantId;
  }

  if (project?.id?.length) {
    filter.ActivityFacility.ids = project.id;
  }

  if (facilityFilterQuery?.boundary?.length) {
    filter.ActivityFacility.boundaryCodes = facilityFilterQuery.boundary;
  }

  if (facilityFilterQuery?.status?.length) {
    filter.ActivityFacility.statuses = facilityFilterQuery.status;
  }

  if (facilitySearchQuery?.name) {
    filter.ActivityFacility.facilityName = facilitySearchQuery.name;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ACTIVITY_FACILITY", filter, limit, offset],
    () => fetchFacilities(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ACTIVITY_FACILITY"])
  }
}

export default useActivityFacility;
