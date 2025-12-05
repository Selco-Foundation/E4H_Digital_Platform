import React, { useEffect, useRef, useState } from "react";
import {Loader, Table} from "@egovernments/digit-ui-react-components";
import Filter from "../../components/VisitTable/Filter";
import InfoCard from "../../components/VisitTable/InfoCard";
import { Link, useHistory, useLocation } from "react-router-dom";
import { useDispatch } from "react-redux";
import {populateWorkingProject} from "../../redux/actions";
import SearchActionCentre from "../../components/VisitTable/SearchAction";
import useVisit from "../../hooks/useVisit";
import useProject from "@selco/digit-ui-module-pm/src/hooks/useProject";

const VisitTable = ({ t }) => {

  const [mainCheck, setMainCheck] = useState(false);
  const dispatch = useDispatch();
  const [createdProject, setCreatedProject] = useState(null);
  const [selectedFacilities, setSelectedFacilities] = useState([]);
  const [fetchedData, setData] = useState([]);
  const history = useHistory();
  const location = useLocation();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const queryParams = new URLSearchParams(window.location.search);
  const [updatingWorkflow, setUpdatingWorkflow] = useState(false);

  const [projectQueryFilter, setProjectQueryFilter] = useState((() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : null;
    } catch (error) {
      console.error("Failed to parse filter parameter:", error);
      return null;
    }
  })() || {
    project : {
      projectId: [projectId]
    },
    facilityFilter: {
      status: ["PENDING_APPROVAL"],
    },
    facilitySearch: {
      name: ""
    },
    facilityFilterQuery: {},
    facilitySearchQuery: {},
  });
  const prevSearchParamsRef = useRef(JSON.stringify(projectQueryFilter));

  const [pageSize, setPageSize] = useState(queryParams.get("pageSize") || 10);
  const [pageOffset, setPageOffset] = useState(queryParams.get("pageOffset") || 0);
  const prevPageSizeRef = useRef(pageSize);

  const { data: projectData } = useProject({
    id: [projectId],
  });

  const {
    isLoading,
    data: visitData,
    revalidate: revalidateVisits,
  } = useVisit(projectQueryFilter, pageSize, pageOffset);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(projectQueryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
    });
  }, [projectQueryFilter, pageSize, pageOffset])

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData])

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(projectQueryFilter);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [projectQueryFilter, pageSize]);

  useEffect(() => {
    if (visitData) {
      setData(visitData.visits);
      setSelectedFacilities([]);
      setMainCheck(false);
    }
  }, [visitData])

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  }

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  }

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  }

  const handleFilterChange = (filters) => {
    setProjectQueryFilter({
      ...projectQueryFilter,
      ...filters,
    });
  };

  const GetCell = (value) => <span className="cell-text" style={{ color: "#000000" }}>{value}</span>;

  const revalidateData = () => {
    setMainCheck(false);
    setSelectedFacilities([]);
    revalidateVisits();
  }

  const columns = [
    {
      Header: t("CS_HEALTH_FACILITY"),
      Cell: ({ row }) => {
        return (
          <div>
            <span className="link">
              <Link
                to={`/${window.contextPath}/employee/amc/project/${projectId}/amc-visits/${row.original["id"]}`}
                style={{ color: "#C84C0E" }}
              >
                {row.original["facilityName"]}
              </Link>
            </span>
          </div>
        );
      },
    },
    {
      Header: t("CS_BLOCK"),
      Cell: ({ row }) => {
        return GetCell(row.original["block"] ? t(`Boundary_${row.original["block"]}`) : "-");
      },
    },
    {
      Header: t("CS_DISTRICT"),
      Cell: ({ row }) => {
        return GetCell(row.original["district"] ? t(`Boundary_${row.original["district"]}`) : "-");
      },
    },
    {
      Header: t("CS_ASSIGNED_TO"),
      Cell: ({ row }) => {
        return GetCell(row.original["assigned"] ? `${row.original["assigned"]}` : "-");
      },
    },
    {
      Header: t("CS_STATUS"),
      Cell: ({ row }) => {
        return GetCell(row.original["status"] ? t(`CS_${row.original["status"]}`) : "-");
      },
    },
  ];

  //todo: fetch all possible statuses from backend??
  const statusesList = [
    {
      name: t("CS_SCHEDULED"),
      code: "SCHEDULED"
    },
    {
      name: t("CS_PENDING_APPROVAL"),
      code: "PENDING_APPROVAL"
    },
    {
      name: t("CS_APPROVED"),
      code: "APPROVED"
    },
    {
      name: t("CS_EXPIRED"),
      code: "EXPIRED"
    }
  ];

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("CS_NO_FACILITIES_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div style={{
        backgroundColor: "white",
        padding: "15px 0px 0px 0px",
      }}>
        <div
          className={"health-facility-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            customTableWrapperClassName={"visit-table"}
            data={fetchedData}
            columns={columns}
            getCellProps={() => {
              return {};
            }}
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={visitData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    );
  }

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      {updatingWorkflow && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000000,
            backgroundColor: "gray",
            opacity: 0.5,
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
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
        {createdProject?.name || "-"}
      </div>
      {createdProject && (<InfoCard t={t} project={createdProject} />)}
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter
            t={t}
            type="desktop"
            projectQueryFilter={projectQueryFilter}
            onFilterChange={handleFilterChange}
            statusesList={statusesList}
          />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <div
              style={{
                fontSize: "20px",
                fontWeight: "bold",
                marginBottom: "40px",
                marginLeft: "10px",
              }}
            >
              {t("AMC_VISITS")}
            </div>
            <SearchActionCentre
              t={t}
              mainCheckBox={mainCheck}
              selectedFacilities={selectedFacilities}
              projectQueryFilter={projectQueryFilter}
              onSearch={handleFilterChange}
              revalidateData={revalidateData}
              setUpdatingWorkflow={setUpdatingWorkflow}
            />
          </div>
          {renderFacilities()}
        </div>
      </div>
    </div>
  );
};
export default VisitTable;
