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
    videos: {},
    installationReportDocuments: []
  };
  const documentAggregation = [];

  if (workflow) {
    for (let i = 0; i < Math.min(workflow.length, 2); i++) {
      const row = workflow[i];
      const action = row.action;

      if (action === "SUBMIT_REPORT_A" || action === "SUBMIT_REPORT_B") {
        for (const document of row.documents) {
          const documentType = document.documentType;
          const fileStoreResponse = await QCService.fetchImageFromFileStore(document.fileStoreId);
          const fileUrl = Digit.Utils.getFileUrl(fileStoreResponse[document.fileStoreId]);
          const fileDetails = await QCService.fetchDocumentDetails(fileUrl);
          let documentRequired = false;

          if (documentType.toUpperCase().includes("IMAGE")) {
            documentRequired = true;
            const assetType = documentType.split("-")[0].toUpperCase();
            if (assetAggregation.images[assetType]) {
              assetAggregation.images[assetType].push(fileUrl);
            } else {
              assetAggregation.images[assetType] = [fileUrl];
            }
          } else if (documentType.toUpperCase().includes("VIDEO")) {
            documentRequired = true;
            const assetType = documentType.split("-")[0].toUpperCase();

            if (assetAggregation.videos[assetType]) {
              assetAggregation.videos[assetType].push({
                fileUrl,
                size: fileDetails.size,
              });
            } else {
              assetAggregation.videos[assetType] = [{
                fileUrl,
                size: fileDetails.size
              }];
            }
          } else if (i === 0 && documentType.toUpperCase() === "INSTALLATION_REPORT") {
            documentRequired = true;
            assetAggregation.installationReportDocuments = [
              ...assetAggregation.installationReportDocuments,
              {
                fileUrl,
                ...fileDetails
              }
            ];
          } else if (i === 0 && documentType.toUpperCase() === "INSTALLATION_REPORT_BOM") {
            documentRequired = true;
            assetAggregation.bomCompletionReport = {
              fileUrl,
              ...fileDetails
            };
          }

          if (documentRequired) documentAggregation.push(document);
        }
      }
    }
  }

  return {
    assetAggregation,
    documentAggregation,
  };
}

const fetchFacilityDetails = async (filter, limit, offset) => {

  const projectsResponse = await QCService.fetchProjects(filter, limit, offset);
  const projectData = projectsResponse?.Project?.[0];

  const facility = projectData.project.additionalDetails.facility || {};
  const address = projectData.project.address || {};
  const additionalDetails = projectData.project.additionalDetails || {};
  const assigneeDetails = projectData.project.additionalDetails.assignedTo || {};
  const auditTrail = generateAuditTrail(projectData.workflow, projectData.transactions);
  const { assetAggregation, documentAggregation } = await getAssetAggregation(projectData.workflow);

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
    assetAggregation,
    documentAggregation,
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