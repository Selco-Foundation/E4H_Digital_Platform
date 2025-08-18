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

const getAssetAggregation = async (workflow) => {
  const assetAggregation = {
    images: {},
    videos: {}
  };

  if (workflow) {
    for (const row of workflow) {
      const action = row.action;

      if (action === "SUBMIT_REPORT_A" || action === "SUBMIT_REPORT_B") {
        for (const document of row.documents) {
          const documentType = document.documentType;
          const fileStoreResponse = await Digit.QCService.fetchImageFromFileStore(document.fileStoreId);
          const fileUrl = Digit.Utils.getFileUrl(fileStoreResponse[document.fileStoreId]);
          const size = await Digit.QCService.fetchDocumentSize(fileUrl);

          if (documentType.toUpperCase().includes("IMAGE")) {
            const assetType = documentType.split("-")[0].toUpperCase();
            if (assetAggregation.images[assetType]) {
              assetAggregation.images[assetType].push(fileUrl);
            } else {
              assetAggregation.images[assetType] = [fileUrl];
            }
          } else if (documentType.toUpperCase().includes("VIDEO")) {
            const assetType = documentType.split("-")[0].toUpperCase();

            if (assetAggregation.videos[assetType]) {
              assetAggregation.videos[assetType].push(fileUrl);
            } else {
              assetAggregation.videos[assetType] = [fileUrl];
            }
          } else if (documentType.toUpperCase() === "INSTALLATION_REPORT") {
            assetAggregation.installationReport = {
              fileUrl,
              size: size
            };
          }
        }
      }
    }
  }

  return assetAggregation;
}

const fetchFacilityDetails = async (filter, limit, offset) => {

  const projectsResponse = await Digit.QCService.fetchProjects(filter, limit, offset);
  const projectData = projectsResponse?.Project?.[0];

  const facility = projectData.project.additionalDetails.facility || {};
  const address = projectData.project.address || {};
  const additionalDetails = projectData.project.additionalDetails || {};
  const assigneeDetails = projectData.project.additionalDetails.assignedTo || {};
  const auditTrail = generateAuditTrail(projectData.workflow);
  const assetAggregation = await getAssetAggregation(projectData.workflow);

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
    assetAggregation
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