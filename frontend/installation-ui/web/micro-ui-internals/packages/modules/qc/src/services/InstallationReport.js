import { Request } from "@egovernments/digit-ui-libraries";
import { FilestoreService } from "./Filestore";

export const InstallationReportService = {
  fetchApprovedReports: async (tenantId, fieldPlanId) => {
    const response = await Request({
      url: "/activity/v1/activities/installation-report/fieldplan/_search",
      method: "POST",
      data: {},
      params: { tenantId, fieldPlanId },
      userService: true,
      auth: true,
    });
    if (!Array.isArray(response?.InstallationReportDocuments)) {
      throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    }
    return response.InstallationReportDocuments;
  },

  fetchReportDocument: async (report) => {
    if (!report.filestoreId || !report.facilityName || !report.projectName) {
      throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    }
    const files = await FilestoreService.fetchDocumentFromFilestore(report.filestoreId);
    const url = Digit.Utils.getFileUrl(files[report.filestoreId]);
    if (!url) throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    const blob = await InstallationReportService.fetchReportPdf(url);
    return blob.arrayBuffer();
  },

  // Share PDF fetching and validation between individual downloads and ZIP exports.
  fetchReportPdf: async (url) => {
    // Filestore returns a direct object-storage URL, not an ingestion-service endpoint.
    const response = await fetch(url);
    if (!response.ok) throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    const blob = await response.blob();
    // Some stores serve PDFs as octet-stream; check the actual PDF header.
    if (!(await blob.slice(0, 5).text()).startsWith("%PDF-")) {
      throw new Error("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED");
    }
    return new Blob([blob], { type: "application/pdf" });
  },
};
