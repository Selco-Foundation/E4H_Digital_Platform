import React, { useEffect, useMemo, useRef, useState } from "react";
import useProject from "../../hooks/useProject";
import InfoCard from "../../components/ProjectDetails/InfoCard";
import { Button, Loader, Table } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { Link, useHistory, useRouteMatch } from "react-router-dom";

const ProjectDetails = () => {

  const { t } = useTranslation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const { path } = useRouteMatch();
  const history = useHistory();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const queryParams = new URLSearchParams(window.location.search);
  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const prevPageSizeRef = useRef(pageSize);
  const [createdProject, setCreatedProject] = useState(null);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { isLoading: projectDataLoading, data: projectData } = useProject({
    id: [projectId],
  });

  useEffect(() => {
    if (prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevPageSizeRef.current = pageSize;
    }
  }, [pageSize]);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      setCreatedProject(project);
    }
  }, [projectData])

  const data = [
    {
      id: "1234",
      fieldPlanName: "Field Plan 1",
      activities: ["Tag", "Tag", "Tag", "Tag", "Tag", "Tag"],
      startDate: "10/12/25",
      endDate: "10/12/25",
      numberOfHealthFacilities: 1000,
      status: "Scheduled"
    },
    {
      fieldPlanName: "",
      activities: [],
      startDate: "",
      endDate: "",
      numberOfHealthFacilities: "",
      status: ""
    }
  ]

  const GetHead = (value) => (
    <div style={{ height: "38px", width: "100%", display: "flex", alignItems: "center" }}>
      <span>{value}</span>
    </div>
  );

  const GetCell = (value) => (
    <span style={{ fontSize: "16px", fontWeight: "400", fontFamily: "Roboto", color: "#363636" }}>
      {value}
    </span>
  );

  const GetActivityList = (activities) => (
    <div style={{display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center"}}>
      {activities?.map((activity) => (
        <span
          key={activity}
          style={{
            backgroundColor: "#F1FFF8",
            color: "#00703C",
            width: "fit-content",
            padding: "5px 10px",
          }}
        >
          {activity}
        </span>
      ))}
    </div>
  )

  const columns = useMemo(
    () => [
      {
        id: "fieldPlanName",
        Header: () => GetHead("Field Plan Name"),
        Cell: ({ row }) => (
          <Link
            to={`/${window.contextPath}/employee/pm/project/${projectId}/field-plan/create?fieldPlanId=${row.original["id"]}&key=1`}
            style={{ color: "#C84C0E" }}
          >
            {row.original["fieldPlanName"]}
          </Link>
        ),
      },
      {
        id: "activities",
        Header: () => GetHead("Activities"),
        Cell: ({ row }) => GetActivityList(row.original["activities"]),
      },
      {
        id: "startDate",
        Header: () => GetHead("Start Date"),
        Cell: ({ row }) => GetCell(row.original["startDate"]),
      },
      {
        id: "endDate",
        Header: () => GetHead("End Date"),
        Cell: ({ row }) => GetCell(row.original["endDate"]),
      },
      {
        id: "numberOfHealthFacilities",
        Header: () => GetHead("No. of Health Facilities"),
        Cell: ({ row }) => GetCell(row.original["numberOfHealthFacilities"]),
      },
      {
        id: "status",
        Header: () => GetHead("Status"),
        Cell: ({ row }) => GetCell(row.original["status"]),
      },
    ],
    []
  );

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

  const handleFieldPlanCreationNavigation = () => {
    history.push(`/${window.contextPath}/employee/pm/project/${projectId}/field-plan/create`);
  }

  if (projectDataLoading) {
    return <Loader />
  }

  const renderFieldPlanTable = () => {

    return (
      <div style={{ borderRadius: "6px", overflow: "hidden" }}>
        <Table
          t={t}
          data={data}
          columns={columns}
          customTableWrapperClassName={"project-details-table"}
          getCellProps={() => {
            return {
              style: {
                height: "70px",
                minHeight: "fit-content",
              }
            };
          }}
          styles={{minWidth: "300px", overflow: "auto"}}
          onNextPage={onNextPage}
          onPrevPage={onPrevPage}
          currentPage={Math.floor(pageOffset / pageSize)}
          totalRecords={data.length}
          onPageSizeChange={onPageSizeChange}
          pageSizeLimit={pageSize}
        />
      </div>
    )
  }

  return (
    <div style={{padding: mobileView ? "15px" : "0px"}}>
      {createdProject?.name && (
        <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
          {createdProject?.name}
        </div>
      )}
      {createdProject && (<InfoCard t={t} project={createdProject} />)}
      <div style={{display: "flex", gap: "15px", alignItems: "center", marginTop: "20px", marginBottom: "25px"}}>
        <div style={{fontSize: "32px", fontWeight: "bold", fontFamily: "Roboto Condensed", color: "#0B0C0C"}}>
          {t("CS_COMMON_FIELD_PLANS")}
        </div>
        <button
          type="button"
          className={"jk-digit-secondary-btn"}
          style={{
            display: "flex",
            justifyContent: "space-around",
            alignItems: "center",
            height: "32px",
            padding: "0px 20px"
          }}
          onClick={handleFieldPlanCreationNavigation}
        >
          <span
            style={{
              width: "18px",
              height: "18px",
              borderRadius: "5px",
              background: "#C84C0E",
              color: "white",
              fontSize: "20px",
              fontWeight: "bold",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              marginRight: "10px"
            }}
          >
            +
          </span>
          <span
            style={{
              fontSize: "16px",
              fontWeight: "500",
              fontFamily: "Roboto"
            }}
          >
            {t("CORE_COMMON_ADD_NEW")}
          </span>
        </button>
      </div>
      {renderFieldPlanTable()}
    </div>
  )
}

export default ProjectDetails;