import {useQuery, useQueryClient} from "react-query";
import { AMCService } from "../services/AMC";
import { FilestoreService } from "../services/Filestore";
import {VisitService} from "../services/VisitService";
import {amcConfig} from "../constants/AMCConfig";

const generateAuditTrail = (processInstances) => {
  const auditTrail = [];

  processInstances?.forEach((row) => {
    const date = new Date(row.auditDetails?.lastModifiedTime);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    const formattedDate = `${day}/${month}/${year}`;

    let comments = [];
    try {
      comments = JSON.parse(row.comment);
    } catch (err) {
      console.error("Error parsing comment:", err);
    }

    auditTrail.push({
      status: row.state.state,
      date: formattedDate,
      reasons: comments
    })
  })

  return auditTrail;
}

const fetchDocument = async (fileStoreId, fetchFileDetails = true) => {
  try {
    const fileStoreResponse = await FilestoreService.fetchDocumentFromFilestore(fileStoreId);
    const fileUrl = Digit.Utils.getFileUrl(fileStoreResponse[fileStoreId]);
    let fileDetails;
    if (fetchFileDetails) {
      fileDetails = await AMCService.fetchDocumentDetails(fileUrl);
    }
    return { fileUrl, fileDetails };
  } catch (error) {
    console.error(`Failed to fetch document ${fileStoreId}:`, error);
  }
}

const getFormattedValue = (value) => {
  if (value === true || value === "YES") return "Yes";
  if (value === false || value === "NO") return "No";
  if (value === "NOT_COMPLETED") return "";

  // Detect only proper ISO date strings like "2025-12-25T18:30:00.000Z"
  const isoDateRegex = /^\d{4}-\d{2}-\d{2}T/;

  if (typeof value === "string" && isoDateRegex.test(value)) {
    const date = new Date(value);

    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
  }

  return value;
}

const generateVisitReport = (userResponses, format) => {
  if (!userResponses) return;
  if (format?.properties?.length) {
    format.properties = format.properties.map((prop) => ({
      ...prop,
      value: getFormattedValue(userResponses[prop.field])
    }))
  }
  if (format?.children?.length) {
    format.children.forEach((child) => {
      generateVisitReport(userResponses, child);
    })
  }
}

const getDocumentAggregation = async (processInstances) => {
  const reportDocumentAggregation = {};
  const workflowDocuments = [];

  const recentProcessInstance = processInstances?.[0];
  if (Array.isArray(recentProcessInstance?.documents)) {
    for (const document of recentProcessInstance.documents) {
      const { fileUrl, fileDetails } = await fetchDocument(document.fileStoreId);
      if (!fileUrl) continue;

      if (document.documentType.toUpperCase() === "AMC_INSTALLATION_FORM") {
        reportDocumentAggregation.amcInstallationForm = {
          fileUrl,
          ...fileDetails
        };
      }

      workflowDocuments.push(document);
    }
  }

  return {
    reportDocumentAggregation,
    workflowDocuments,
  };
}

const fetchVisitImages = async (visitImageDocuments) => {
  const visitImages = [];

  if (visitImageDocuments?.length) {
    for (const document of visitImageDocuments) {
      const { fileUrl } = await fetchDocument(document.fileStoreId, false);
      if (fileUrl) {
        visitImages.push(fileUrl);
      }
    }
  }

  return visitImages;
}

const fetchVisitDetails = async (filter, limit, offset) => {

  const visitsResponse = await VisitService.fetchVisits(filter, limit, offset);
  const visitData = visitsResponse?.ScheduledVisits?.[0];

  const facility = visitData?.facility || {};
  const assigneeDetails = visitData?.processInstances?.[0]?.assignes?.[0] || {};
  const auditTrail = generateAuditTrail(visitData.processInstances);
  const { reportDocumentAggregation, workflowDocuments } = await getDocumentAggregation(visitData.processInstances);
  const format = amcConfig;
  generateVisitReport(visitData?.visitReport?.responses, format);
  const visitImages = await fetchVisitImages(visitData?.visitReport?.documents);

  return {
    id: visitData?.id,
    facilityDetails: {
      facilityName: facility.facility_name,
      facilityId: facility.id,
      facilityType: facility.facility_type,
      block: facility.additionalDetails?.block,
      district: facility.additionalDetails?.district,
      status: visitData?.status,
      assigned: assigneeDetails.name,
    },
    visitReport: format,
    visitImages,
    auditTrail,
    reportDocumentAggregation,
    workflowDocuments,
  }
}

const useVisitDetails = (visitId) => {

  const filter = {
    searchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      ids: [visitId],
    }
  }

  const limit = 1;
  const offset = 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
      ["VISIT_DETAILS", filter, limit, offset],
      () => fetchVisitDetails(filter, limit, offset)
  )

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["VISIT_DETAILS"]),
    revalidateFacilities: () => queryClient.invalidateQueries(["VISIT"])
  }
}

export default useVisitDetails;