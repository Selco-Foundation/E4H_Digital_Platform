import React, {useEffect, useRef, useState, useMemo, useCallback} from "react";
import {useTranslation} from "react-i18next";
import {Loader, TextInput, Table, SubmitBar} from "@egovernments/digit-ui-react-components";
import {ArrowUpward, ArrowDownward, ImportExport} from "@egovernments/digit-ui-svg-components";
import {Link, useHistory, useLocation} from "react-router-dom";
import useProject from "../../hooks/useProject";

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const month = date.toLocaleString("en-US", { month: "long" });
  const day = String(date.getDate()).padStart(2, "0");
  const year = date.getFullYear();
  return `${day} ${month} ${year}`;
};

const SORT_DIR = {
  ASC: "ASC",
  DESC: "DESC",
};

const ProjectTable = () => {
  const {t} = useTranslation();
  const history = useHistory();
  const location = useLocation();
  const queryParams = new URLSearchParams(window.location.search);

  const initialFilter = useMemo(() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : {
        subProjectTypeId: "PROJECT"
      };
    } catch (error) {
      return {
        subProjectTypeId: "PROJECT"
      };
    }
  }, []);

  const [queryFilter, setQueryFilter] = useState(initialFilter);
  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const [searchText, setSearchText] = useState(queryFilter.name || "");
  const [sortBy, setSortBy] = useState(queryParams.get("sortBy") || "");
  const [sortDir, setSortDir] = useState(queryParams.get("sortDir") || SORT_DIR.DESC);

  const prevSearchParamsRef = useRef(JSON.stringify(queryFilter));
  const prevPageSizeRef = useRef(pageSize);

  const {isLoading, data} = useProject(
    queryFilter,
    pageSize,
    pageOffset,
    sortBy,
    sortDir
  );

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(queryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}&sortBy=${sortBy}&sortDir=${sortDir}`
    });
  }, [queryFilter, pageSize, pageOffset, sortBy, sortDir]);

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(queryFilter);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [queryFilter, pageSize]);

  const handleSearch = () => {
    setQueryFilter({
      name: searchText,
      subProjectTypeId: "PROJECT"
    });
  };

  const handleClear = () => {
    setSearchText("");
    setQueryFilter({
      subProjectTypeId: "PROJECT"
    });
    setSortBy("");
    setSortDir(SORT_DIR.DESC);
  };

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  };

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  };

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value, 10));
  };

  const toggleSort = (field) => {
    if (sortBy !== field) {
      setSortBy(field);
      setSortDir(SORT_DIR.DESC);
    } else {
      setSortDir((d) => (d === SORT_DIR.DESC ? SORT_DIR.ASC : SORT_DIR.DESC));
    }
  };

  const DefaultHeader = ({label}) => (
    <span style={{color: '#0B0C0C', fontSize: "16px"}}>
            {label}
        </span>
  );

  const SortHeader = ({label, field}) => {
    const iconStyle = {width: 24, height: 24};
    const getSortIcon = () => {
      if (sortBy !== field) {
        return <ImportExport style={{...iconStyle}} fill={"#0B0C0C"}/>;
      }
      return sortDir === SORT_DIR.DESC ?
        <ArrowDownward style={{...iconStyle, width: 20}} fill={"#0B0C0C"}/> :
        <ArrowUpward style={{...iconStyle, width: 20}} fill={"#0B0C0C"}/>;
    };

    return (
      <div
        onClick={() => toggleSort(field)}
        style={{
          cursor: "pointer",
          userSelect: "none",
          color: '#0B0C0C',
          fontSize: "16px",
          display: "flex",
          alignItems: "center",
          gap: "2px"
        }}
        title="Click to sort"
      >
        {label}
        {getSortIcon()}
      </div>
    );
  };

  const GetCell = (value) => (
    <span className="cell-text" style={{color: "#0B0C0C"}}>
            {value}
        </span>
  );

  const columns = [
    {
      Header: <DefaultHeader label={t("PM_LABEL_PROJECT_NAME")}/>,
      accessor: "projectName",
      Cell: ({row}) => (
        <div>
          <Link
            to={`/${window.contextPath}/employee/pm/project/${row.original.id}/field-plans`}
            className="link"
            style={{color: "#C84C0E", textDecoration: "none"}}
          >
            {row.original.name || "-"}
          </Link>
        </div>
      )
    },
    {
      Header: <DefaultHeader label={t("PM_PROJECT_INFO_STATE")}/>,
      accessor: "state",
      Cell: ({row}) => GetCell(
        row.original.additionalDetails?.geographyDetails?.state?.code ?
          t(`STATE_${row.original.additionalDetails?.geographyDetails?.state?.code.toUpperCase()}`) :
          "-"
      ),
    },
    {
      Header: <DefaultHeader label={t("PM_LABEL_PROJECT_TYPE")}/>,
      accessor: "projectType",
      Cell: ({row}) => GetCell(row.original.projectType || "-"),
    },
    {
      Header: <SortHeader label={t("CORE_COMMON_START_DATE")} field="startDate"/>,
      accessor: "startDate",
      Cell: ({row}) => GetCell(row.original.startDate ? formatDate(row.original.startDate) : "-"),
    },
    {
      Header: <SortHeader label={t("CS_END_DATE")} field="endDate"/>,
      accessor: "endDate",
      Cell: ({row}) => GetCell(row.original.endDate ? formatDate(row.original.endDate) : "-"),
    },
    {
      Header: <DefaultHeader label={t("CORE_COMMON_STATUS")}/>,
      accessor: "status",
      Cell: ({row}) => GetCell(t(`PM_PROJECT_STATUS_${row.original.status ? row.original.status.toUpperCase() : "DRAFT"}`)),
    },
    {
      Header: <DefaultHeader label={t("PM_LABEL_PROJECT_NO_OF_HFS")}/>,
      accessor: "numberOfHealthFacilities",
      Cell: ({row}) => GetCell(row.original.additionalDetails?.hlsCount || 0),
    },
  ];

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
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
          <Loader/>
        </div>
      )}

      <div
        style={{
          fontSize: "40px",
          fontWeight: "bold",
          fontFamily: "Roboto Condensed",
          marginBottom: "20px",
          color: "#0B0C0C",
        }}
      >
        {t("PM_LABEL_MY_PROJECTS")}
      </div>

      <div
        style={{
          backgroundColor: "white",
          padding: "24px",
          marginBottom: "16px",
          borderRadius: "2px",
          minWidth: "700px",
        }}>
        <div style={{
          marginBottom: "8px",
          color: "#0B0C0C",
          fontWeight: 400,
          fontSize: "16px"
        }}>
          {t("PM_LABEL_SEARCH_PROJECT_ID")}
        </div>
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            width: "100%",
          }}>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSearch();
            }}
          >
            <TextInput
              name="serviceRequestId"
              value={searchText}
              onChange={(e) => {
                setSearchText(e.target.value);
              }}
              style={{ width: "300px", marginBottom: "0" }}
            ></TextInput>
          </form>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "12px"
            }}>
            <button
              type="button"
              onClick={handleClear}
              style={{
                border: "none",
                background: "transparent",
                color: "#C84C0E",
                fontSize: "16px",
                fontFamily: "Roboto",
                fontWeight: 400,
                cursor: "pointer",
              }}>
              {t("CORE_COMMON_CLEAR_SEARCH")}
            </button>
            <SubmitBar
              label={t("CORE_COMMON_SEARCH")}
              onSubmit={handleSearch}
            />
          </div>
        </div>
      </div>

      <div style={{backgroundColor: "white", padding: "20px", minWidth: "700px"}}>
        {data?.projects && data.projects.length > 0 ? (
          <div style={{margin: "0 0px", overflow: "auto"}}>
            <Table
              t={t}
              data={data.projects}
              columns={columns}
              customTableWrapperClassName={"pm-projects-table"}
              getCellProps={() => ({
                style: {
                  maxWidth: "100%",
                  padding: "20px 18px",
                  fontSize: "16px"
                },
              })}
              onNextPage={onNextPage}
              onPrevPage={onPrevPage}
              currentPage={Math.floor(pageOffset / pageSize)}
              totalRecords={data.totalCount || 0}
              onPageSizeChange={onPageSizeChange}
              pageSizeLimit={pageSize}
            />
          </div>
        ) : (
          <div style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "200px",
            fontSize: "18px",
            color: "#666"
          }}>
            {!isLoading && t("CS_NO_PROJECTS_FOUND")}
          </div>
        )}
      </div>
    </div>
  );
};

export default ProjectTable;