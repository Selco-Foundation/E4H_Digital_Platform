import { useQuery, useQueryClient } from "react-query";
import { AMCService } from "../services/AMCService";
import { FAService } from "../services/FA";
import { FilestoreService } from "../services/Filestore";

const formatDate = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const month = String(date.getMonth() + 1).padStart(2, "0"); // months are 0-based
  const day = String(date.getDate()).padStart(2, "0");
  const year = date.getFullYear();
  return `${month}/${day}/${year}`;
};

const fetchDocument = async (fileStoreId, fetchFileDetails = true) => {
  try {
    const fileStoreResponse = await FilestoreService.fetchDocumentFromFilestore(fileStoreId);
    const fileUrl = Digit.Utils.getFileUrl(fileStoreResponse[fileStoreId]);
    let fileDetails;
    if (fetchFileDetails) {
      fileDetails = await FAService.fetchDocumentDetails(fileUrl);
    }
    return { fileUrl, fileDetails };
  } catch (error) {
    console.error(`Failed to fetch document ${fileStoreId}:`, error);
  }
};

const fetchVisitReport = async (processInstances) => {
  const recentProcessInstance = processInstances?.[0];
  if (recentProcessInstance?.action === "APPROVE" && Array.isArray(recentProcessInstance.documents)) {
    for (const document of recentProcessInstance.documents) {
      if (document.documentType.toUpperCase() === "AMC_INSTALLATION_FORM") {
        const { fileUrl, fileDetails } = await fetchDocument(document.fileStoreId);
        if (!fileUrl) continue;
        return {
          fileUrl,
          ...fileDetails,
        };
      }
    }
  }
};

const formatAMCVisits = async (amcVisits) => {
  if (!amcVisits?.length) return [];

  const formattedAMCVisits = [];

  for(let amcVisit of amcVisits) {
    formattedAMCVisits.push({
      status: amcVisit?.status,
      scheduledDate: formatDate(amcVisit?.scheduledDate),
      visitReport: await fetchVisitReport(amcVisit?.processInstances),
    });
  }

  return formattedAMCVisits;
};

const fetchAMCVisits = async (filter, limit, offset) => {
  const amcVisitsResponse = await AMCService.fetchAMCVisits(filter, limit, offset);
  return {
    amcVisits: await formatAMCVisits(amcVisitsResponse.ScheduledVisits),
    totalCount: amcVisitsResponse.TotalCount,
  };
};

const useAMCVisit = (amcConfigurationId) => {

  const filter = {
    searchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      amcConfigurationIds: [amcConfigurationId],
    },
  };

  const limit = 100;
  const offset = 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["AMC_VISIT", filter, limit, offset],
    () => fetchAMCVisits(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["AMC_VISIT"]),
  };
};

export default useAMCVisit;
