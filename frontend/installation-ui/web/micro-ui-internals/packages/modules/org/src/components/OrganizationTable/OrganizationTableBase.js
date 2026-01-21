import React, { useEffect, useRef, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import { useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";

import Filter from "./Filter";
import OrganizationAdminActions from "./OrganizationAdminActions";
import useOrganizations from "../../hooks/useOrganization";

const normalizeOrgFilter = (raw) => {
  const r = raw || {};
  const n1 = r.organizationSearch && typeof r.organizationSearch.name === "string" ? r.organizationSearch.name : "";
  const n2 = r.organizationSearchQuery && typeof r.organizationSearchQuery.name === "string" ? r.organizationSearchQuery.name : "";
  const name = n1 || n2 || "";
  const trimmed = (name || "").trim();

  return {
    organizationSearch: { name: name || "" },
    organizationSearchQuery: trimmed ? { name: trimmed } : {},
  };
};

const OrganizationTableBase = ({ orgType }) => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();

  const queryParams = new URLSearchParams(window.location.search);
  const [fetchedData, setFetchedData] = useState([]);

  const [projectQueryFilter, setProjectQueryFilter] = useState(() => {
    try {
      const filterParam = queryParams.get("filter");
      const parsed = filterParam ? JSON.parse(filterParam) : null;
      return normalizeOrgFilter(parsed);
    } catch (error) {
      return normalizeOrgFilter(null);
    }
  });

  const prevSearchParamsRef = useRef(JSON.stringify(projectQueryFilter));
  const [pageSize, setPageSize] = useState(queryParams.get("pageSize") || 10);
  const [pageOffset, setPageOffset] = useState(queryParams.get("pageOffset") || 0);
  const prevPageSizeRef = useRef(pageSize);

  useEffect(() => {
    const encodedFilter = encodeURIComponent(JSON.stringify(projectQueryFilter));
    history.replace({
      pathname: location.pathname,
      search: `filter=${encodedFilter}&pageSize=${pageSize}&pageOffset=${pageOffset}`,
    });
  }, [projectQueryFilter, pageSize, pageOffset]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(projectQueryFilter);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [projectQueryFilter, pageSize]);

  const { isLoading, isError, error, data: orgData } = useOrganizations(
    projectQueryFilter,
    Number(pageSize),
    Number(pageOffset),
    orgType
  );

  useEffect(() => {
    if (orgData && orgData.organizations) setFetchedData(orgData.organizations || []);
  }, [orgData]);

  const handleFilterChange = (filters) => {
    setProjectQueryFilter(normalizeOrgFilter(filters));
  };

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  };

  const onNextPage = () => setPageOffset(Number(pageOffset) + Number(pageSize));
  const onPrevPage = () => setPageOffset(Math.max(0, Number(pageOffset) - Number(pageSize)));

  const GetCell = (value) => (
    <span className="cell-text" style={{ color: "#000000" }}>
      {value}
    </span>
  );

  const columns = [
    { Header: t("ORG_NAME") || "Name", Cell: ({ row }) => GetCell((row && row.original && row.original.name) || "-") },
    { Header: t("ORG_CODE") || "Code", Cell: ({ row }) => GetCell((row && row.original && row.original.code) || "-") },
    { Header: t("ORG_TYPE") || "Type", Cell: ({ row }) => GetCell((row && row.original && row.original.orgType) || "-") },
    { Header: t("ORG_POC_NAME") || "POC Name", Cell: ({ row }) => GetCell((row && row.original && row.original.pocName) || "-") },
  ];

  const renderOrganizations = () => {
    if (isLoading) return <Loader />;

    if (isError) {
      const msg =
        (error &&
          (error.message ||
            (error.response &&
              error.response.data &&
              error.response.data.Errors &&
              error.response.data.Errors[0] &&
              error.response.data.Errors[0].message))) ||
        "Unknown error";

      return (
        <div style={{ padding: "20px" }}>
          <div style={{ fontSize: "18px", fontWeight: "bold", marginBottom: "8px" }}>
            {t("ORG_FETCH_FAILED") || "Failed to load organizations"}
          </div>
          <div style={{ color: "#B91900" }}>{msg}</div>
        </div>
      );
    }

    if (!fetchedData || fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("CS_NO_ORGANIZATIONS_FOUND") || "No organizations found"}
          </div>
        </div>
      );
    }

    return (
      <div style={{ backgroundColor: "white", width: "100%" }}>
        <div className={"admin-org-table-wrapper"} style={{ margin: "20px", overflow: "auto" }}>
          <Table
            t={t}
            data={fetchedData}
            columns={columns}
            customTableWrapperClassName={"admin-org-table"}
            getCellProps={() => ({
              style: { maxWidth: "100%", padding: "17.24px 18px", fontSize: "15px" },
            })}
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(Number(pageOffset) / Number(pageSize))}
            totalRecords={(orgData && orgData.total) || 0}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
            onClickRow={(row) => {
              const id = row && row.original && row.original.id ? String(row.original.id) : "";
              history.push(`/${window.contextPath}/employee/org/organizations/${encodeURIComponent(id)}`);
            }}
          />
        </div>
      </div>
    );
  };

  return (
    <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
      {/*<div style={{ padding: "20px" }}>*/}
      {/*  <h1 style={{ fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", margin: "0", color: "#0B0C0C" }}>*/}
      {/*    {orgType === "PLATFORM" ? t("PLATFORM_ORGANIZATIONS") || t("ORGANIZATIONS") : t("VENDOR_ORGANIZATIONS") || t("ORGANIZATIONS")}*/}
      {/*  </h1>*/}

      {/*  /!* ✅ passes orgType into popup form + payload *!/*/}
      {/*  <OrganizationAdminActions orgType={orgType} />*/}
      {/*</div>*/}

      <div
        style={{
          padding: "20px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: "12px",
        }}
      >
        <h1
          style={{
            fontSize: "40px",
            fontWeight: "bold",
            fontFamily: "Roboto Condensed",
            margin: 0,
            color: "#0B0C0C",
            flex: 1, // keeps title left and lets button stay right
          }}
        >
          {orgType === "PLATFORM"
            ? t("PLATFORM_ORGANIZATIONS") || t("ORGANIZATIONS")
            : t("VENDOR_ORGANIZATIONS") || t("ORGANIZATIONS")}
        </h1>

        <div style={{ flexShrink: 0 }}>
          <OrganizationAdminActions orgType={orgType} />
        </div>
      </div>

      <div style={{ width: "100%", display: "flex", gap: "15px", alignItems: "stretch" }}>
        <div style={{ minWidth: "300px", display: "flex" }}>
          <Filter t={t} type="desktop" projectQueryFilter={projectQueryFilter} onFilterChange={handleFilterChange} />
        </div>

        <div style={{ flex: 1, minWidth: "750px", backgroundColor: "white" }}>{renderOrganizations()}</div>
      </div>
    </div>
  );
};

export default OrganizationTableBase;