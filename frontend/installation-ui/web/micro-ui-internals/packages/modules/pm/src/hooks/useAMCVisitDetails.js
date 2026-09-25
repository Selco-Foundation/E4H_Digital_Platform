import {useQuery, useQueryClient} from "react-query";
import { DocumentService } from "../services/Document";
import { FilestoreService } from "../services/Filestore";
import { AMCVisitService } from "../services/AMCVisit";
import { getFacilityGeography } from "../utilities/GeographyUtils";

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
    if (!fileUrl || !fetchFileDetails) return { fileUrl };

    const fileDetails = await DocumentService.fetchDocumentDetails(fileUrl).catch(() => undefined);
    return { fileUrl, fileDetails };
  } catch (error) {
    console.error(`Failed to fetch document ${fileStoreId}:`, error);
    return {};
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
  const workflowDocuments = (processInstances || []).flatMap((processInstance) => processInstance?.documents || []);
  const installationFormDocuments = (processInstances || [])
    .flatMap((processInstance) =>
      (processInstance?.documents || []).map((document) => ({
        ...document,
        processModifiedTime: processInstance?.auditDetails?.lastModifiedTime || 0,
        workflowAction: processInstance?.action,
      }))
    )
    .filter(
      (document) =>
        document.documentType?.toUpperCase() === "AMC_INSTALLATION_FORM"
    )
    .sort((first, second) => Number(second.processModifiedTime) - Number(first.processModifiedTime));

  const installationForm =
    installationFormDocuments.find((document) => document.workflowAction?.toUpperCase() === "SUBMIT_VISIT_REPORT") ||
    installationFormDocuments[0];

  if (installationForm) {
    const { fileUrl, fileDetails } = await fetchDocument(installationForm.fileStoreId);
    if (fileUrl) {
      reportDocumentAggregation.amcInstallationForm = {
        fileUrl,
        ...fileDetails,
      };
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

const formatAmcNumbers = (amcNumbers) => {
  if (!Array.isArray(amcNumbers) || !amcNumbers.length) return "-";
  return amcNumbers.join(", ");
}

const getVisitAmcNumber = (visitData) => {
  const visitNumber = Number(visitData?.visitNumber);
  const durationMonths = Number(visitData?.amcConfiguration?.durationMonths);
  const visitFrequencyMonths = Number(visitData?.amcConfiguration?.visitFrequencyMonths);

  if (!visitNumber || !durationMonths || !visitFrequencyMonths) return "-";

  const totalVisits = durationMonths / visitFrequencyMonths;
  if (!Number.isFinite(totalVisits) || totalVisits <= 0) return "-";

  return `${visitNumber}/${totalVisits}`;
}

const fetchFacilityAmcSummary = async (facilityId) => {
  if (!facilityId) return {};

  try {
    const amcSummaryResponse = await AMCVisitService.fetchAmcSummary({
      searchCriteria: {
        tenantId: Digit.ULBService.getCurrentTenantId(),
        facilityIds: [facilityId],
      },
    });
    const amcSummary = amcSummaryResponse?.FacilitiesAmcSummary?.[0] || {};

    return {
      amcNumber: amcSummary?.amcNumber || "-",
      completedAmcNumbers: formatAmcNumbers(amcSummary?.completedAmcNumbers),
      lapsedAmcNumbers: formatAmcNumbers(amcSummary?.lapsedAmcNumbers),
    };
  } catch (error) {
    console.error(`Failed to fetch AMC summary for facility ${facilityId}:`, error);
    return {};
  }
}

const fetchVisitDetails = async (filter, limit, offset) => {

  const visitsResponse = await AMCVisitService.fetchVisits(filter, limit, offset);
  const visitData = visitsResponse?.ScheduledVisits?.[0];

  const facility = visitData?.facility || {};
  const facilityAmcSummary = await fetchFacilityAmcSummary(facility.id);
  const geography = getFacilityGeography(facility);
  const auditTrail = generateAuditTrail(visitData.processInstances);
  const { reportDocumentAggregation, workflowDocuments } = await getDocumentAggregation(visitData.processInstances);
  const mdmsConfigResponse = await Digit.MDMSService.getMultipleTypes(Digit.ULBService.getCurrentTenantId(), "AMC", ["FormConfig"]);
  const format = mdmsConfigResponse?.["AMC"]?.["FormConfig"]?.[0] || {};
  generateVisitReport(visitData?.visitReport?.responses, format);
  const visitImages = await fetchVisitImages(visitData?.visitReport?.documents);

  return {
    id: visitData?.id,
    facilityDetails: {
      facilityName: facility.facility_name,
      facilityId: facility.id,
      facilityType: facility.facility_type,
      state: geography.state,
      block: geography.block,
      district: geography.district,
      status: visitData?.status,
      ...facilityAmcSummary,
      amcNumber: getVisitAmcNumber(visitData),
      assigned: visitData?.assignments?.[0]?.user?.name,
    },
    visitReport: format,
    visitImages,
    auditTrail,
    reportDocumentAggregation,
    workflowDocuments,
  }
}

const useAMCVisitDetails = (visitId) => {

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
      ["AMC_VISIT_DETAILS", filter, limit, offset],
      () => fetchVisitDetails(filter, limit, offset)
  )

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["AMC_VISIT_DETAILS"]),
    revalidateFacilities: () => queryClient.invalidateQueries(["AMC_VISIT_LIST"])
  }
}

export default useAMCVisitDetails;
