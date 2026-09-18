import { Request } from "@egovernments/digit-ui-libraries";
import { FilestoreService } from "./Filestore";

export const InstallationReportService = {
  fetchApprovedReports: async (tenantId, projectId) => {
    const response = await Request({
      url: "/activity/v1/activities/installation-report/project/_search",
      method: "POST",
      data: {},
      params: { tenantId, projectId },
      userService: true,
      auth: true,
    });
    if (!Array.isArray(response?.InstallationReportDocuments)) {
      throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    }
    return response.InstallationReportDocuments;
  },

  fetchReportDocument: async (report) => {
    if (!report.filestoreId || !report.facilityName || !report.projectId) {
      throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    }
    const files = await FilestoreService.fetchDocumentFromFilestore(report.filestoreId);
    const url = Digit.Utils.getFileUrl(files[report.filestoreId]);
    if (!url) throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    // Filestore returns a direct object-storage URL, not an ingestion-service endpoint.
    const response = await fetch(url);
    if (!response.ok) throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    const blob = await response.blob();
    // Some stores serve PDFs as octet-stream; check the actual PDF header.
    if (!(await blob.slice(0, 5).text()).startsWith("%PDF-")) {
      throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    }
    return blob.arrayBuffer();
  },
};
