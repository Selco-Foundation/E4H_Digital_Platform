import { useQuery, useQueryClient } from "react-query";
import { QCService } from "../services/QC";
import {ActivityService} from "../services/Activity";

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const month = String(date.getMonth() + 1).padStart(2, "0"); // months are 0-based
  const day = String(date.getDate()).padStart(2, "0");
  const year = date.getFullYear();
  return `${month}/${day}/${year}`;
};

const formatProjectFacilityInfo = (projectFacilityInfo) => {
  const formattedProjectFacilityInfo = {};

  projectFacilityInfo?.statusAgregation?.forEach((row) => {
    formattedProjectFacilityInfo[row?.status] = row?.occurrences;
  })

  return formattedProjectFacilityInfo;
}

const formatFieldPlans = (projects) => {
  return projects?.map((row) => {

    const totalProjectFacilities = row?.additionalDetails?.countFieldPlanFacilities || 0;
    const projectFacilityInfo = formatProjectFacilityInfo(row?.additionalDetails);
    const completionRate = totalProjectFacilities !== 0 ? (Math.ceil(projectFacilityInfo["APPROVED_BY_QC_SPOC"]/totalProjectFacilities * 100) || 0) : 0;

    return {
      id: row?.id,
      name: row?.fieldPlan?.name,
      fieldPlan: row?.fieldPlan,
      activityType: row?.activityName,
      activityId: row?.activityId,
      activityCode: row?.activityCode,
      facilitiesCount: totalProjectFacilities,
      startDate: formatDate(row?.startDate),
      endDate: formatDate(row?.endDate),
      completionRate: completionRate,
      status: row?.status,
      stateBoundaryCode: row?.fieldPlan?.geographyDetails?.state,
      projectFacilityInfo
    };
  })
}

const fetchFieldPlans = async (filter, limit, offset) => {
  const fieldPlansResponse = await ActivityService.fetchActivityAssignments(filter, limit, offset);

  return {
    fieldPlans: formatFieldPlans(fieldPlansResponse?.ActivityAssignment),
    totalCount: fieldPlansResponse?.TotalCount
  }
}

const useFieldPlan = (queryFilter, pageSize, pageOffset) => {

  const { tenantId, name, id } = queryFilter;

  const filter = {
    ActivityAssignment : {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      roles: ["INSTALLATION_REPORT_APPROVER_QC_TEAM"],
    }
  };

  if (tenantId) {
    filter.ActivityAssignment.tenantId = tenantId;
  }

  if (name) {
    filter.ActivityAssignment.fieldPlanCode = name;
  }

  if (id?.length) {
    filter.ActivityAssignment.ids = id;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["FIELD_PLAN", filter, limit, offset],
    () => fetchFieldPlans(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data ,
    revalidate: () => queryClient.invalidateQueries(["FIELD_PLAN"])
  };
}

export default useFieldPlan;