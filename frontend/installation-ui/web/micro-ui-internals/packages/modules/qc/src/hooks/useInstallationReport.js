import { useQuery, useQueryClient } from "react-query";
import { InstallationReportService } from "../services/InstallationReport";
import { createInstallationReportZip } from "../utilities/installationReportDownload";

const useInstallationReport = (fieldPlanId) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const queryClient = useQueryClient();
  const queryKey = ["QC_APPROVED_INSTALLATION_REPORTS", tenantId, fieldPlanId];
  const { isLoading, isFetching, isError, error, data, refetch } = useQuery(
    queryKey,
    () => InstallationReportService.fetchApprovedReports(tenantId, fieldPlanId),
    { enabled: !!fieldPlanId, retry: false, cacheTime: 0 }
  );

  const fetchZip = async () => {
    // Always refresh on download so newly approved reports are included.
    const result = await refetch({ throwOnError: true });
    if (!result.data.length) return null;
    const documents = [];
    // Sequential downloads bound network concurrency and fail the whole export on error.
    for (const report of result.data) {
      documents.push({ report, data: await InstallationReportService.fetchReportDocument(report) });
    }
    return createInstallationReportZip(documents);
  };

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(queryKey),
    fetchZip,
  };
};

export default useInstallationReport;
