import { useQuery, useQueryClient } from "react-query";
import { QCService } from "../services/QC";

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

    const totalProjectFacilities = row?.project?.additionalDetails?.countProjectFacilities || 0;
    const projectFacilityInfo = formatProjectFacilityInfo(row?.project?.additionalDetails);
    const completionRate = totalProjectFacilities !== 0 ? (Math.ceil(projectFacilityInfo["APPROVED_BY_QC_SPOC"]/totalProjectFacilities * 100) || 0) : 0;

    return {
      id: row?.project?.id,
      name: row?.project?.name || row?.project?.projectNumber,
      projectType: "Installation",
      facilitiesCount: totalProjectFacilities,
      startDate: formatDate(row?.project?.startDate),
      endDate: formatDate(row?.project?.endDate),
      completionRate: completionRate,
      status: row?.status,
      transactions: row?.transactions,
      address: row?.project?.address,
      projectFacilityInfo
    };
  })
}

const fetchFieldPlans = async (filter, limit, offset) => {
  const fieldPlansResponse = await QCService.fetchProjects(filter, limit, offset);

  return {
    fieldPlans: formatFieldPlans(fieldPlansResponse?.Project),
    totalCount: fieldPlansResponse?.totalCount
  }
}

const useFieldPlan = (queryFilter, pageSize, pageOffset) => {

  const { projectTypeId, name, id } = queryFilter?.Project;
  const filter = {
    Project: {}
  };

  if (projectTypeId) {
    filter.Project.projectTypeId = projectTypeId;
  }

  if (name) {
    filter.Project.name = name;
  }

  if (id && id.length > 0) {
    filter.Project.id = id;
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