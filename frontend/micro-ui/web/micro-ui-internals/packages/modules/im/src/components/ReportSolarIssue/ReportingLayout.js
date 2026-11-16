import React from "react";
import { useTranslation } from "react-i18next";

const ReportingLayout = ({ facilityName, children }) => {
  const { t } = useTranslation();
  return <section className="im-report-layout" style={{ width: "100%", boxSizing: "border-box", paddingBottom: "120px" }}>
    <style>{`
      .im-report-layout .employeeCard, .im-report-layout .card { padding: 24px; }
      .im-report-layout .im-report-question {
        display: block; font-size: 28px; font-weight: 700; line-height: 1.3;
        margin: 0 0 20px; color: #0b0c0c; overflow-wrap: anywhere;
      }
      .im-report-layout .im-report-input { width: 100%; max-width: 760px; }
      .im-report-layout .im-report-upload { width: 100%; max-width: 960px; }
      .im-report-layout textarea { min-height: 140px; box-sizing: border-box; }
      .im-report-layout input[type="radio"] { accent-color: #82212b; flex-shrink: 0; }
      .im-report-layout .im-report-review {
        display: grid; grid-template-columns: minmax(120px, 30%) minmax(0, 1fr);
        gap: 12px 16px; margin: 16px 0; max-width: 960px;
      }
      .im-report-layout .im-report-review dt { font-weight: 600; overflow-wrap: anywhere; }
      .im-report-layout .im-report-review dd { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; }
      @media (max-width: 780px) {
        .im-report-layout .employeeCard, .im-report-layout .card { padding: 16px; }
        .im-report-layout .im-report-question { font-size: 24px; }
      }
    `}</style>
    <h1 style={{ fontSize: "40px", fontWeight: 700, margin: "0 0 20px", color: "#0b0c0c", overflowWrap: "anywhere" }}>
      {t("IM_REPORT_SOLAR_ISSUE_TITLE", { defaultValue: "Report an issue with your solar" })}
    </h1>
    {facilityName && <p style={{ fontSize: "20px", margin: "0 0 24px", color: "#505a5f" }}>
      {t("IM_REPORT_STEP_FACILITY", { defaultValue: "Facility" })}: {facilityName}
    </p>}
    {children}
  </section>;
};

export default ReportingLayout;
