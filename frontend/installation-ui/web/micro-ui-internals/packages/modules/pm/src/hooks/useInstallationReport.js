import { useQuery, useQueryClient } from "react-query";
import { InstallationReportService } from "../services/InstallationReport";
import { createInstallationReportZip } from "../utilities/installationReportDownload";

const useInstallationReport = (projectId) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const queryClient = useQueryClient();
  const queryKey = ["PM_APPROVED_INSTALLATION_REPORTS", tenantId, projectId];
  const { isLoading, isFetching, isError, error, data, refetch } = useQuery(
    queryKey,
    () => InstallationReportService.fetchApprovedReports(tenantId, projectId),
    { enabled: !!projectId, retry: false, cacheTime: 0 }
  );

  const { refetch: refetchZip } = useQuery(
    ["PM_INSTALLATION_REPORT_ZIP", tenantId, projectId],
    async () => {
      // Refresh the approved list inside the export query before reading any files.
      const result = await refetch({ throwOnError: true });
      if (!result.data.length) return null;
      const documents = [];
      // Sequential reads avoid downloading all PDFs concurrently.
      for (const report of result.data) {
        documents.push({ report, data: await InstallationReportService.fetchReportDocument(report) });
      }
      return createInstallationReportZip(documents);
    },
    // Binary exports run only on an explicit download, never on mount or window focus.
    { enabled: false, retry: false, cacheTime: 0 }
  );

  const fetchZip = async () => {
    try {
      const result = await refetchZip({ throwOnError: true });
      return result.data;
    } finally {
      // Release the potentially large ZIP from the query cache after the action.
      queryClient.removeQueries(["PM_INSTALLATION_REPORT_ZIP", tenantId, projectId], { exact: true });
    }
  };

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(queryKey),
    fetchZip,
  };
};

export default useInstallationReport;
