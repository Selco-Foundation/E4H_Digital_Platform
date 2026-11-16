import { Card, Header, Loader, Table, Toast } from "@selco/digit-ui-react-components";
import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import DecisionModal from "../../components/mobileChange/DecisionModal";
import { useLabel } from "../../components/mobileChange/labels";

/**
 * CRM approval queue for registered-mobile-number change requests.
 *
 * Only a CRM (role COMPLAINT_ASSESSOR) can reach this page: the route is guarded in pages/employee/index.js,
 * the card link is role-filtered in IMCard, and the backend enforces it again (NOT_AUTHORIZED).
 *
 * Data comes from the shared hooks in the libraries package:
 *   Digit.Hooks.im.useMobileChangeRequests         -> _search (server-side status filter + paging)
 *   Digit.Hooks.im.useMobileChangeRequestActions   -> _approve / _reject (refresh the list by themselves)
 */

const STATUS_TABS = ["PENDING", "APPROVED", "REJECTED", "CANCELLED", "ALL"];
const DEFAULT_TAB = "PENDING"; // the CRM's work queue
const DEFAULT_PAGE_SIZE = 10; // backend max is 100
const TOAST_MS = 5000;

const COLORS = { PENDING: "#F47738", APPROVED: "#00703C", REJECTED: "#D4351C", CANCELLED: "#505A5F", muted: "#505A5F", border: "#D6D5D4", brand: "#7a2829" };

const formatDate = (epochMillis) =>
  epochMillis ? new Date(epochMillis).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" }) : "-";

const formatNumber = (number) => (number ? `+91 ${number}` : "-");

const StatusBadge = ({ status, label }) => (
  <span
    style={{
      background: COLORS[status] || COLORS.muted,
      color: "#fff",
      padding: "2px 12px",
      borderRadius: "12px",
      fontSize: "14px",
      whiteSpace: "nowrap",
    }}
  >
    {label}
  </span>
);

// keep dates and phone numbers on one line; the table scrolls sideways on narrow screens instead
const NO_WRAP = { whiteSpace: "nowrap" };

const solidButton = { background: COLORS.brand, color: "#fff", border: `1px solid ${COLORS.brand}`, padding: "6px 16px", cursor: "pointer", font: "inherit" };
const outlineButton = { background: "#fff", color: COLORS.brand, border: `1px solid ${COLORS.brand}`, padding: "6px 16px", cursor: "pointer", font: "inherit" };

const MobileChangeRequests = () => {
  const tr = useLabel();
  const { t } = useTranslation(); // plain t for the shared Table (its own pager labels are existing keys)
  const utils = Digit.Utils?.mobileChangeRequest;
  const isCrm = !!utils?.isCrmUser(Digit.UserService.getUser()?.info);
  const isMobile = Digit.Utils.browser.isMobile();

  const [tab, setTab] = useState(DEFAULT_TAB);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [pageOffset, setPageOffset] = useState(0);
  const [decision, setDecision] = useState(null); // { type: "APPROVE" | "REJECT", request }
  const [toast, setToast] = useState(null); // { text, isError }

  // Changing the tab or the page size always starts again from the first page.
  const changeTab = (next) => {
    setTab(next);
    setPageOffset(0);
  };

  const criteria = useMemo(
    () => ({ ...(tab === "ALL" ? {} : { status: [tab] }), limit: pageSize, offset: pageOffset }),
    [tab, pageSize, pageOffset]
  );

  const { data, isLoading, isError, revalidate } = Digit.Hooks.im.useMobileChangeRequests({ criteria, enabled: isCrm });
  const { approve, reject } = Digit.Hooks.im.useMobileChangeRequestActions();

  const requests = data?.requests || [];
  const totalCount = data?.totalCount || 0;

  // Deleting/processing the last row of a page can leave us on a page that no longer exists: step back.
  useEffect(() => {
    if (data && pageOffset > 0 && requests.length === 0) {
      setPageOffset(Math.max(0, pageOffset - pageSize));
    }
  }, [data]);

  // Auto-hide the toast.
  useEffect(() => {
    if (!toast) return undefined;
    const timer = setTimeout(() => setToast(null), TOAST_MS);
    return () => clearTimeout(timer);
  }, [toast]);

  const submitting = approve.isLoading || reject.isLoading;

  const handleConfirm = (comment) => {
    const { type, request } = decision;
    const mutation = type === "APPROVE" ? approve : reject;
    mutation.mutate(
      { id: request.id, comment: comment || undefined },
      {
        onSuccess: () => {
          setDecision(null);
          setToast({ text: tr(type === "APPROVE" ? "MOBILE_CHANGE_APPROVED_TOAST" : "MOBILE_CHANGE_REJECTED_TOAST"), isError: false });
        },
        onError: (error) => {
          // e.g. NOT_PENDING when another CRM got there first: the list refetches itself, show why.
          setDecision(null);
          setToast({ text: tr(utils.getMobileChangeErrorKey(error)), isError: true });
        },
      }
    );
  };

  const facilityOf = (request) => {
    if (request.facilityName) return request.facilityName;
    if (request.boundaryCode) {
      const translated = tr(`Boundary_${request.boundaryCode}`);
      return translated === `Boundary_${request.boundaryCode}` ? request.boundaryCode : translated;
    }
    return request.facilityId || "-";
  };

  // What goes in the last cell: buttons while PENDING, otherwise the CRM's remark and decision date.
  // (a render function, not an inline component, so React does not remount the cell on every render)
  const renderActions = (request) =>
    request.isPending ? (
      <div style={{ display: "flex", gap: "8px", flexWrap: isMobile ? "wrap" : "nowrap" }}>
        <button type="button" style={solidButton} onClick={() => setDecision({ type: "APPROVE", request })}>
          {tr("MOBILE_CHANGE_APPROVE")}
        </button>
        <button type="button" style={outlineButton} onClick={() => setDecision({ type: "REJECT", request })}>
          {tr("MOBILE_CHANGE_REJECT")}
        </button>
      </div>
    ) : (
      <div style={{ overflowWrap: "anywhere" }}>
        {request.comment ? <div>{request.comment}</div> : null}
        {request.actionTime ? <div style={{ color: COLORS.muted, fontSize: "14px" }}>{tr("MOBILE_CHANGE_DECIDED_ON", { date: formatDate(request.actionTime) })}</div> : null}
        {!request.comment && !request.actionTime ? "-" : null}
      </div>
    );

  const columns = useMemo(
    () => [
      { Header: tr("MOBILE_CHANGE_COL_RAISED_ON"), Cell: ({ row }) => <span className="cell-text" style={NO_WRAP}>{formatDate(row.original.createdTime)}</span> },
      { Header: tr("MOBILE_CHANGE_COL_REQUESTED_BY"), Cell: ({ row }) => <span className="cell-text">{row.original.userName || "-"}</span> },
      { Header: tr("MOBILE_CHANGE_COL_FACILITY"), Cell: ({ row }) => <span className="cell-text">{facilityOf(row.original)}</span> },
      { Header: tr("MOBILE_CHANGE_COL_CURRENT"), Cell: ({ row }) => <span className="cell-text" style={NO_WRAP}>{formatNumber(row.original.currentMobileNumber)}</span> },
      { Header: tr("MOBILE_CHANGE_COL_NEW"), Cell: ({ row }) => <strong className="cell-text" style={NO_WRAP}>{formatNumber(row.original.newMobileNumber)}</strong> },
      {
        Header: tr("MOBILE_CHANGE_COL_STATUS"),
        Cell: ({ row }) => <StatusBadge status={row.original.status} label={tr(`MOBILE_CHANGE_STATUS_${row.original.status}`)} />,
      },
      { Header: tr(tab === "PENDING" ? "MOBILE_CHANGE_COL_ACTION" : tab === "ALL" ? "MOBILE_CHANGE_COL_ACTION_REMARK" : "MOBILE_CHANGE_COL_REMARK"), Cell: ({ row }) => renderActions(row.original) },
    ],
    // tr changes with the language; tab decides the last column's heading
    [tr, tab]
  );

  const emptyText = tr(`MOBILE_CHANGE_EMPTY_${tab}`);

  const tabBar = (
    <div role="tablist" style={{ display: "flex", flexWrap: "wrap", gap: "8px", margin: "8px 0 16px" }}>
      {STATUS_TABS.map((name) => {
        const active = name === tab;
        return (
          <button
            key={name}
            role="tab"
            aria-selected={active}
            type="button"
            onClick={() => changeTab(name)}
            style={{
              background: active ? COLORS.brand : "#fff",
              color: active ? "#fff" : COLORS.brand,
              border: `1px solid ${COLORS.brand}`,
              padding: "8px 20px",
              cursor: "pointer",
              font: "inherit",
            }}
          >
            {tr(`MOBILE_CHANGE_TAB_${name}`)}
          </button>
        );
      })}
    </div>
  );

  const currentPage = Math.floor(pageOffset / pageSize);
  const lastPage = Math.max(0, Math.ceil(totalCount / pageSize) - 1);

  // Mobile: one card per request, with simple Previous / Next paging.
  const mobileList = (
    <div>
      {requests.map((request) => (
        <div key={request.id} style={{ border: `1px solid ${COLORS.border}`, borderLeft: `4px solid ${COLORS[request.status] || COLORS.muted}`, background: "#fff", padding: "12px 16px", marginBottom: "12px" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "8px", marginBottom: "8px" }}>
            <strong>{request.userName || "-"}</strong>
            <StatusBadge status={request.status} label={tr(`MOBILE_CHANGE_STATUS_${request.status}`)} />
          </div>
          <div style={{ color: COLORS.muted, marginBottom: "4px" }}>{facilityOf(request)}</div>
          <div>
            {formatNumber(request.currentMobileNumber)} → <strong>{formatNumber(request.newMobileNumber)}</strong>
          </div>
          <div style={{ color: COLORS.muted, fontSize: "14px", margin: "4px 0 10px" }}>
            {tr("MOBILE_CHANGE_COL_RAISED_ON")}: {formatDate(request.createdTime)}
          </div>
          {renderActions(request)}
        </div>
      ))}
      {totalCount > pageSize ? (
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: "8px" }}>
          <button type="button" style={outlineButton} disabled={currentPage === 0} onClick={() => setPageOffset(pageOffset - pageSize)}>
            ‹
          </button>
          <span>
            {currentPage + 1} / {lastPage + 1}
          </span>
          <button type="button" style={outlineButton} disabled={currentPage >= lastPage} onClick={() => setPageOffset(pageOffset + pageSize)}>
            ›
          </button>
        </div>
      ) : null}
    </div>
  );

  let content;
  if (!isCrm) {
    content = (
      <Card>
        <p style={{ textAlign: "center" }}>{tr("MOBILE_CHANGE_CRM_NO_ACCESS")}</p>
      </Card>
    );
  } else if (isLoading) {
    content = <Loader />;
  } else if (isError) {
    content = (
      <Card>
        <p style={{ textAlign: "center", color: COLORS.REJECTED }}>{tr("MOBILE_CHANGE_CRM_LOAD_ERROR")}</p>
        <p style={{ textAlign: "center" }}>
          <button type="button" style={outlineButton} onClick={revalidate}>
            {tr("MOBILE_CHANGE_CRM_RETRY")}
          </button>
        </p>
      </Card>
    );
  } else if (requests.length === 0) {
    content = (
      <Card>
        <p style={{ textAlign: "center" }}>{emptyText}</p>
      </Card>
    );
  } else if (isMobile) {
    content = mobileList;
  } else {
    content = (
      <Table
        t={t}
        data={requests}
        columns={columns}
        getCellProps={() => ({ style: { maxWidth: "100%", padding: "17.24px 18px", fontSize: "15px" } })}
        onNextPage={() => setPageOffset(pageOffset + pageSize)}
        onPrevPage={() => setPageOffset(Math.max(0, pageOffset - pageSize))}
        onPageSizeChange={(e) => {
          setPageSize(Number(e.target.value));
          setPageOffset(0);
        }}
        currentPage={currentPage}
        totalRecords={totalCount}
        pageSizeLimit={pageSize}
      />
    );
  }

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Header>
          {tr("MOBILE_CHANGE_CRM_TITLE")}
          {isCrm && data ? <span style={{ color: COLORS.muted, fontSize: "18px", marginLeft: "12px" }}>{tr("MOBILE_CHANGE_CRM_TOTAL", { count: totalCount })}</span> : null}
        </Header>
        <div style={{ color: COLORS.brand, marginBottom: "10px", textAlign: "right", marginRight: "15px" }}>
          <Link to={`/${window.contextPath}/employee`}>{tr("MOBILE_CHANGE_CRM_BACK")}</Link>
        </div>
      </div>
      {isCrm ? tabBar : null}
      <div style={{ marginRight: "15px" }}>{content}</div>

      {decision ? (
        <DecisionModal type={decision.type} request={decision.request} submitting={submitting} onConfirm={handleConfirm} onDismiss={() => setDecision(null)} />
      ) : null}
      {toast ? <Toast label={toast.text} error={toast.isError} isDleteBtn={true} onClose={() => setToast(null)} /> : null}
    </div>
  );
};

export default MobileChangeRequests;
