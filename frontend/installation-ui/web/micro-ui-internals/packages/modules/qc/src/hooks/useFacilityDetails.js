import {useQuery, useQueryClient} from "react-query";
import { QCService } from "../services/QC";

const generateAuditTrail = (workflow, transactions) => {
  const auditTrail = [];

  const transactionsMap = new Map();
  transactions?.forEach((row) => {
    transactionsMap.set(row.processInstanceId, row);
  })

  workflow?.forEach((row) => {

    const date = new Date(row.auditDetails?.lastModifiedTime);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    const formattedDate = `${day}/${month}/${year}`;

    const transaction = transactionsMap.get(row.id);
    const comments = [];
    if (transaction) {
      const assetTypeReasonsMap = new Map();
      transaction.comments?.forEach(comment => {
        const assetType = comment.assetType.toUpperCase();
        let reason = comment.commentMessage;
        try {
          reason = JSON.parse(comment.commentMessage);
        } catch (err) {
          console.error("Error parsing comment:", err);
        }

        if (assetTypeReasonsMap.has(assetType)) {
          assetTypeReasonsMap.set(assetType, [...assetTypeReasonsMap.get(assetType), reason]);
        } else {
          assetTypeReasonsMap.set(assetType, [reason]);
        }
      })

      assetTypeReasonsMap.forEach((value, key) => {
        comments.push({
          name: key,
          reasons: value
        });
      })
    }

    auditTrail.push({
      status: row.state.state,
      date: formattedDate,
      reasons: comments
    })
  })

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
          const fileStoreResponse = await QCService.fetchImageFromFileStore(document.fileStoreId);
          const fileUrl = Digit.Utils.getFileUrl(fileStoreResponse[document.fileStoreId]);
          const size = await QCService.fetchDocumentSize(fileUrl);

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
              assetAggregation.videos[assetType].push({
                fileUrl,
                size
              });
            } else {
              assetAggregation.videos[assetType] = [{
                fileUrl,
                size
              }];
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

  const projectsResponse = await QCService.fetchProjects(filter, limit, offset);
  const projectData = projectsResponse?.Project?.[0];

  const facility = projectData.project.additionalDetails.facility || {};
  const address = projectData.project.address || {};
  const additionalDetails = projectData.project.additionalDetails || {};
  const assigneeDetails = projectData.project.additionalDetails.assignedTo || {};
  const auditTrail = generateAuditTrail(projectData.workflow, projectData.transactions);
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
  const { isLoading, isFetching, isError, error, data } = useQuery(
      ["FACILITY_DETAILS", filter, limit, offset],
      () => fetchFacilityDetails(filter, limit, offset)
  )

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY_DETAILS"]),
    revalidateFacilities: () => queryClient.invalidateQueries(["FACILITY"])
  }
}

export default useFacilityDetails;