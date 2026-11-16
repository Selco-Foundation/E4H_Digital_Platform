import React from "react";
import { ActionBar } from "@selco/digit-ui-react-components";

const ReportingActions = ({ back, skip, next }) => <React.Fragment>
  <style>{`
    .action-bar-wrap.im-report-actions {
      display: flex; align-items: center; gap: 16px;
      left: 64px; right: 0; width: auto; max-width: none;
      padding: 12px 24px; box-sizing: border-box;
    }
    .im-report-actions > button { margin: 0; }
    .im-report-actions > button:first-of-type { margin-left: auto; }
    .im-report-actions > button.im-report-back { margin-left: 0; margin-right: auto; }
    .im-report-actions .im-report-back,
    .im-report-actions .im-report-skip {
      background: white; color: #82212b; border: 1px solid #82212b;
      box-shadow: none;
    }
    .im-report-actions .im-report-back header,
    .im-report-actions .im-report-skip header { color: inherit; }
    .im-report-actions .im-report-skip { border-color: transparent; }
    .im-report-actions button:disabled { opacity: .5; }
    @media (max-width: 780px) {
      .action-bar-wrap.im-report-actions { left: 0; gap: 8px; padding: 12px; }
      .im-report-actions > button { width: auto; min-width: 0; flex: 1; }
      .im-report-actions > button header { padding: 8px; white-space: normal; }
    }
  `}</style>
  <ActionBar className="im-report-actions">
    {back}
    {skip}
    {next}
  </ActionBar>
</React.Fragment>;

export default ReportingActions;
