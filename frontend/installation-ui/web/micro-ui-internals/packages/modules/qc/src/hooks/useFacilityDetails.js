import {useQuery, useQueryClient} from "react-query";
import { QCService } from "../services/QC";
import { ActivityService } from "../services/Activity";
import { FilestoreService } from "../services/Filestore";

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
  const documentAggregation = {
    images: {},
    videos: {},
    installationReportDocuments: [],
    installationCompletionCertificate: [],
    assetHandoverDocument: [],
  };
  const installationImages = [];
  const workflowDocuments = [];

  if (
    ["SUBMIT_REPORT_A", "SUBMIT_REPORT_B", "APPROVE", "REJECT_AND_ASSIGN_FOR_FIELD_QC", "FLAG_FOR_QC"].includes(workflow?.[0]?.action)
    && Array.isArray(workflow[0].documents)
  ) {
    for (const document of workflow[0].documents) {

      let fileUrl, fileDetails;
      try {
        const fileStoreResponse = await FilestoreService.fetchDocumentFromFilestore(document.fileStoreId);
        fileUrl = Digit.Utils.getFileUrl(fileStoreResponse[document.fileStoreId]);
        fileDetails = await QCService.fetchDocumentDetails(fileUrl);
      } catch (error) {
        console.error(`Failed to fetch document ${document.fileStoreId}:`, error);
        continue;
      }

      const documentType = document.documentType;
      let documentRequired = false;

      if (documentType.toUpperCase().includes("INSTALLATION_IMAGE")) {
        documentRequired = true;
        installationImages.push({
          imageCode: documentType.split("-")[1],
          fileUrl,
          ...fileDetails
        });
      } else if (documentType.toUpperCase().includes("IMAGE")) {
        documentRequired = true;
        const assetType = documentType.split("-")[0].toUpperCase();
        if (documentAggregation.images[assetType]) {
          documentAggregation.images[assetType].push(fileUrl);
        } else {
          documentAggregation.images[assetType] = [fileUrl];
        }
      } else if (documentType.toUpperCase().includes("VIDEO")) {
        documentRequired = true;
        const assetType = documentType.split("-")[0].toUpperCase();

        if (documentAggregation.videos[assetType]) {
          documentAggregation.videos[assetType].push({
            fileUrl,
            size: fileDetails.size,
          });
        } else {
          documentAggregation.videos[assetType] = [{
            fileUrl,
            size: fileDetails.size
          }];
        }
      } else if (documentType.toUpperCase() === "INSTALLATION_REPORT") {
        documentRequired = true;
        documentAggregation.installationReportDocuments = [
          ...documentAggregation.installationReportDocuments,
          {
            fileUrl,
            ...fileDetails
          }
        ];
      } else if (documentType.toUpperCase() === "INSTALLATION_REPORT_BOM") {
        documentRequired = true;
        documentAggregation.bomCompletionReport = {
          fileUrl,
          ...fileDetails
        };
      } else if (documentType.toUpperCase() === "INSTALLATION_COMPLETION_CERTIFICATE") {
        documentRequired = true;
        documentAggregation.installationCompletionCertificate = [
          ...documentAggregation.installationCompletionCertificate,
          {
            fileUrl,
            ...fileDetails,
          }
        ];
      } else if (documentType.toUpperCase() === "ASSET_HANDOVER_DOCUMENT") {
        documentRequired = true;
        documentAggregation.assetHandoverDocument = [
          ...documentAggregation.assetHandoverDocument,
          {
            fileUrl,
            ...fileDetails,
          }
        ];
      }

      if (documentRequired) workflowDocuments.push(document);
    }
  }

  return {
    documentAggregation,
    installationImages,
    workflowDocuments,
  };
}

const fetchFacilityDetails = async (filter, limit, offset) => {

  const activityFacilitiesResponse = await ActivityService.fetchActivityFacilities(filter, limit, offset);
  const activityFacilityData = activityFacilitiesResponse?.facility?.[0];

  const facility = activityFacilityData?.activityFacility?.facility || {};
  const assigneeDetails = activityFacilityData?.activityFacility?.assignedEmployeeUser || {};
  const auditTrail = generateAuditTrail(activityFacilityData.workflow, activityFacilityData.transactions);
  const { documentAggregation, installationImages, workflowDocuments } = await getAssetAggregation(activityFacilityData.workflow);

  return {
    facilityDetails: {
      id: activityFacilityData?.activityFacility?.id,
      facilityName: activityFacilityData?.activityFacility?.facility?.facility_name,
      facilityId: activityFacilityData?.activityFacility?.facilityId,
      facilityType: facility.facility_type,
      status: activityFacilityData?.activityFacility?.status,
      block: activityFacilityData?.activityFacility?.facility?.boundary?.block,
      district: activityFacilityData?.activityFacility?.facility?.boundary?.district,
      assigned: assigneeDetails.name,
    },
    auditTrail,
    documentAggregation,
    installationImages,
    workflowDocuments,
  }
}

const useFacilityDetails = (facilityAssignmentId) => {

  const filter = {
    ActivityFacility: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      ids: [facilityAssignmentId],
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
