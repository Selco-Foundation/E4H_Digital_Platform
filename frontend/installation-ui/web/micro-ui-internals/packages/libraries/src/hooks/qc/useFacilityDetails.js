import {useQuery, useQueryClient} from "react-query";

const generateAuditTrail = (workflow) => {
  const auditTrail = [];

  if (workflow) {
    workflow.forEach((row) => {

      const date = new Date(row.auditDetails?.lastModifiedTime);
      const day = String(date.getDate()).padStart(2, "0");
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const year = date.getFullYear();

      const formattedDate = `${day}/${month}/${year}`;

      auditTrail.push({
        status: row.state.state,
        date: formattedDate
      })
    })
  }

  return auditTrail;
}

const fetchFacilityDetails = async (filter, limit, offset) => {

  const projectsResponse = await Digit.QCService.fetchProjects(filter, limit, offset);
  const projectData = projectsResponse?.Project?.[0];

  const facility = projectData.project.additionalDetails.facility || {};
  const address = projectData.project.address || {};
  const additionalDetails = projectData.project.additionalDetails || {};
  const assigneeDetails = projectData.project.additionalDetails.assignedTo || {};
  const auditTrail = generateAuditTrail(projectData.workflow);

  return {
    facilityDetails: {
      id: projectData.project.id,
      facilityName: facility.facility_name || projectData.project.name,
      facilityId: facility.facility_id,
      facilityType: facility.facility_type,
      status: projectData.project.additionalDetails.status,
      projectId: projectData.project.id,
      block: address.boundary,
      district: additionalDetails.district,
      assigned: assigneeDetails.name,
    },
    auditTrail,
  }
}

const useFacilityDetails = (facilityProjectId) => {

  const filter = {
    Project: {
      projectTypeId: "Facility",
      id: [facilityProjectId],
    }
  }

  const limit = 1;
  const offset = 0;

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
      ['facilityDetails', filter, limit, offset],
      () => fetchFacilityDetails(filter, limit, offset)
  )

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(['facilityDetails', filter, limit, offset])
  }
}

export default useFacilityDetails;