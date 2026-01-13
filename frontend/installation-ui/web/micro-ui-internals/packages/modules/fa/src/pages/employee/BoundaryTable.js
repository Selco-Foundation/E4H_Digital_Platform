import React, { useCallback, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Loader, Table, Button } from "@egovernments/digit-ui-react-components";
import { useHistory, useLocation } from "react-router-dom";

import useBoundary from "../../hooks/useBoundary";
import Filter from "../../components/BoundaryTable/Filter";

const ROOT_BOUNDARY_TYPE = "Block";

const BoundaryTable = () => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();

  const queryParams = new URLSearchParams(window.location.search);

  const [boundaryQueryFilter, setBoundaryQueryFilter] = useState(
    (() => {
      try {
        const filterParam = queryParams.get("filter");
        return filterParam ? JSON.parse(filterParam) : null;
      } catch (e) {
        console.error("Failed to parse filter parameter:", e);
        return null;
      }
    })() || {
      boundaryFilter: { state: [], district: [], block: [] },
      boundaryFilterQuery: {},
    }
  );

  const prevSearchParamsRef = useRef(JSON.stringify(boundaryQueryFilter));

  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const prevPageSizeRef = useRef(pageSize);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(boundaryQueryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`,
    });
  }, [boundaryQueryFilter, pageSize, pageOffset]);

  useEffect(() => {
    const prev = prevSearchParamsRef.current;
    const current = JSON.stringify(boundaryQueryFilter);

    if (prev !== current || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = current;
      prevPageSizeRef.current = pageSize;
    }
  }, [boundaryQueryFilter, pageSize]);

  const handleFilterChange = useCallback((filters) => {
    setBoundaryQueryFilter((prev) => {
      const next = { ...prev, ...filters };
      if (JSON.stringify(prev) === JSON.stringify(next)) return prev;
      return next;
    });
  }, []);

  const boundaryFilter = boundaryQueryFilter?.boundaryFilter || { state: [], district: [], block: [] };

  const tableFilter = {
    stateCodes: (boundaryFilter.state || []).map((s) => s.code),
    districtCodes: (boundaryFilter.district || []).map((d) => d.code),
    blockCodes: (boundaryFilter.block || []).map((b) => b.code),
  };

  const { isLoading, data } = useBoundary(ROOT_BOUNDARY_TYPE, undefined, pageSize, pageOffset, {
    _tableFilter: tableFilter,
  });

  const totalCount = data?.totalCount || 0;
  const rows = data?.boundaries || [];

  const onNextPage = () => {
    if (pageOffset + pageSize < totalCount) setPageOffset(pageOffset + pageSize);
  };

  const onPrevPage = () => {
    if (pageOffset - pageSize >= 0) setPageOffset(pageOffset - pageSize);
  };

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value, 10));
  };

  const DefaultHeader = ({ label }) => <span style={{ color: "#0B0C0C", fontSize: "16px" }}>{label}</span>;

  const displayNameOrCode = (name, code) => t(`Boundary_${name || code || "-"}`);

  const CellText = ({ value }) => (
    <span className="cell-text" style={{ color: "#0B0C0C" }}>
      {value || "-"}
    </span>
  );

  const columns = [
    {
      Header: <DefaultHeader label={t("CS_COUNTRY")} />,
      accessor: "country",
      Cell: ({ row }) => <CellText value={displayNameOrCode(null, row?.original?.countryCode)} />,
    },
    {
      Header: <DefaultHeader label={t("CS_STATE")} />,
      accessor: "state",
      Cell: ({ row }) => <CellText value={displayNameOrCode(row?.original?.stateName, row?.original?.stateCode)} />,
    },
    {
      Header: <DefaultHeader label={t("CS_DISTRICT")} />,
      accessor: "district",
      Cell: ({ row }) => <CellText value={displayNameOrCode(row?.original?.districtName, row?.original?.districtCode)} />,
    },
    {
      Header: <DefaultHeader label={t("CS_BLOCK")} />,
      accessor: "block",
      Cell: ({ row }) => <CellText value={displayNameOrCode(row?.original?.blockName, row?.original?.blockCode)} />,
    },
    {
      Header: <DefaultHeader label={t("CS_CODE")} />,
      accessor: "code",
      Cell: ({ row }) => <CellText value={row?.original?.code} />,
    },
  ];

  const renderBoundaries = () => {
    if (rows && rows.length > 0) {
      return (
        <div style={{ backgroundColor: "white", padding: "20px", minWidth: "700px" }}>
          <div style={{ margin: "0 0px", overflow: "auto" }}>
            <Table
              t={t}
              data={rows}
              columns={columns}
              customTableWrapperClassName={"fa-boundary-table"}
              getCellProps={() => ({
                style: { maxWidth: "100%", padding: "20px 18px", fontSize: "16px" },
              })}
              onNextPage={onNextPage}
              onPrevPage={onPrevPage}
              currentPage={Math.floor(pageOffset / pageSize)}
              totalRecords={totalCount}
              onPageSizeChange={onPageSizeChange}
              pageSizeLimit={pageSize}
            />
          </div>
        </div>
      );
    }

    return (
      <div
        style={{
          backgroundColor: "white",
          padding: "20px",
          minWidth: "700px",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "200px",
          fontSize: "18px",
          color: "#666",
        }}
      >
        {!isLoading && t("CORE_COMMON_NO_BOUNDARIES_FOUND")}
      </div>
    );
  };

  return (
    <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
      {isLoading && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 5,
            backgroundColor: "rgba(255, 255, 255, 0.7)",
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
        </div>
      )}

      {/* ✅ RESTORED HEADER + ACTIONS (same as your old code) */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: "12px",
          flexWrap: "wrap",
          marginBottom: "20px",
        }}
      >
        <div
          style={{
            fontSize: "40px",
            fontWeight: "bold",
            fontFamily: "Roboto Condensed",
            color: "#0B0C0C",
            lineHeight: 1.1,
          }}
        >
          {t("FA_LABEL_BOUNDARIES")}
        </div>

        <div style={{ display: "flex", gap: "12px", justifyContent: "flex-end", flexWrap: "wrap" }}>
          <Button
            variation={"secondary"}
            label={t("FA_ADD_BOUNDARY")}
            onButtonClick={() =>
              history.push(`${location.pathname.replace(/\/boundaries\/?$/, "")}/boundary/create`)
            }
          />
          <Button
            variation={"secondary"}
            label={t("FA_BULK_ADD")}
            onButtonClick={() =>
              history.push(`${location.pathname.replace(/\/boundaries\/?$/, "")}/boundary/upload`)
            }
          />
        </div>
      </div>

      {/* ✅ CURRENT LAYOUT: left filter + right table */}
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter t={t} type="desktop" boundaryQueryFilter={boundaryQueryFilter} onFilterChange={handleFilterChange} />
        </div>

        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>{renderBoundaries()}</div>
      </div>
    </div>
  );
};

export default BoundaryTable;