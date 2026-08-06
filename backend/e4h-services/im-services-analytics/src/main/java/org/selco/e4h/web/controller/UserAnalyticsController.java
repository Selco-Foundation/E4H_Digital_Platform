package org.selco.e4h.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.service.UserAnalyticsExcelService;
import org.selco.e4h.service.UserAnalyticsMailService;
import org.selco.e4h.service.UserAnalyticsReportService;
import org.selco.e4h.web.models.RequestInfoWrapper;
import org.selco.e4h.web.models.UserAnalyticsReport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.selco.e4h.util.UserAnalyticsConstants.XLSX_CONTENT_TYPE;

/**
 * Weekly user-analytics report over the {@code user-analytics-report} index, downloaded as an Excel
 * workbook holding active users, logins split by application, and week-on-week active-user growth,
 * all-up and broken down by state and by primary role.
 * <p>
 * Generating the workbook also mails it, attached, to every HRMS holder of the
 * {@code USER_ANALYTICS_REPORT} role.
 */
@Slf4j
@RestController
@RequestMapping("/v1/user-analytics")
@RequiredArgsConstructor
public class UserAnalyticsController {

    private final UserAnalyticsReportService reportService;
    private final UserAnalyticsExcelService excelService;
    private final UserAnalyticsMailService mailService;

    /**
     * @param weekStartDate the Monday the reported week starts on, as an ISO date such as
     *                      {@code 2026-07-27}. Omit it to report the last completed week.
     * @param requestInfoWrapper the standard {@code RequestInfo} envelope. The report itself is not
     *                           user-scoped, so the body is optional and only present to keep the
     *                           call shaped like every other POST behind the gateway.
     */
    @PostMapping("/_report")
    public ResponseEntity<byte[]> downloadWeeklyReport(
            @RequestParam(name = "weekStartDate", required = false) String weekStartDate,
            @RequestBody(required = false) RequestInfoWrapper requestInfoWrapper) {
        log.info("User analytics: weekly report requested for weekStartDate={} by user {}", weekStartDate,
                (requestInfoWrapper != null && requestInfoWrapper.getRequestInfo() != null
                        && requestInfoWrapper.getRequestInfo().getUserInfo() != null)
                        ? requestInfoWrapper.getRequestInfo().getUserInfo().getUuid() : "unknown");

        UserAnalyticsReport report = reportService.buildReport(weekStartDate);
        byte[] workbook = excelService.generate(report);
        String fileName = "user-analytics-report-" + report.getWeekStartDate() + ".xlsx";

        // Mail the same workbook to the USER_ANALYTICS_REPORT role holders. This never throws, so a
        // caller downloading the sheet is unaffected by an HRMS or Kafka outage.
        mailService.sendReport(
                requestInfoWrapper != null ? requestInfoWrapper.getRequestInfo() : null,
                report, workbook, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .contentLength(workbook.length)
                .body(workbook);
    }
}
