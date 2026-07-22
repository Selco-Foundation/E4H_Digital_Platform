import React, { useEffect, useRef, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import { useDispatch } from "react-redux";
import { useHistory, useLocation } from "react-router-dom";
import Filter from "../../components/AssessmentDetails/Filter";
import InfoCard from "../../components/AssessmentDetails/InfoCard";
import SearchAction from "../../components/AssessmentDetails/SearchAction";
import CustomCheckBox from "../../components/Custom/CustomCheckBox";
import useAssessmentPlan from "../../hooks/useAssessmentPlan";
import useAssessmentFacility from "../../hooks/useAssessmentFacility";
import useProject from "../../hooks/useProject";
import { populateWorkingAssessmentPlan, populateWorkingProject } from "../../redux/actions";
import {useTranslation} from "react-i18next";

const STATUS_BADGE_STYLES = {
  QUALIFIED: { backgroundColor: "#E7F6EC", color: "#1B8354" },
  ELIGIBLE: { backgroundColor: "#E7F6EC", color: "#1B8354" },
  PENDING: { backgroundColor: "#FFF4DE", color: "#B4790E" },
  NOT_INITIATED: { backgroundColor: "#F1F1F1", color: "#6B7280" },
  NOT_ELIGIBLE: { backgroundColor: "#FDEAEA", color: "#B91900" },
};

const AssessmentDetails = () => {

  const { t } = useTranslation();
  const [mainCheck, setMainCheck] = useState(false);
  const dispatch = useDispatch();
  const [assessmentPlan, setAssessmentPlan] = useState({});
  const [selectedFacilityIds, setSelectedFacilityIds] = useState([]);
  const [fetchedData, setData] = useState([]);
  const history = useHistory();
  const location = useLocation();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const assessmentId = url.split("assessment/")[1].split("/")[0];
  const queryParams = new URLSearchParams(window.location.search);

  const [projectQueryFilter, setProjectQueryFilter] = useState((() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : null;
    } catch (error) {
      console.error("Failed to parse filter parameter:", error);
      return null;
    }
  })() || {
    facilityFilter: {
      district: null,
      facilityType: null,
      remoteStatus: null,
      onSiteStatus: null,
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

  const {
    isLoading: projectDataLoading,
    data: projectData,
  } = useProject({
    id: [projectId]
  });

  const {
    isLoading: assessmentPlanDataLoading,
    data: assessmentPlanData,
  } = useAssessmentPlan({
    id: [assessmentId]
  });

  const {
    isLoading,
    isFetching: facilityDataFetching,
    data: facilityData,
  } = useAssessmentFacility(projectQueryFilter, pageSize, pageOffset);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(projectQueryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
    });
  }, [projectQueryFilter, pageSize, pageOffset])

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
    if (facilityData) {
      setData(facilityData.facilities);
      setSelectedFacilityIds([]);
      setMainCheck(false);
    }
  }, [facilityData])

  useEffect(() => {
    if (assessmentPlanData) {
      const plan = assessmentPlanData.assessmentPlans[0];
      setAssessmentPlan(plan);
      dispatch(populateWorkingAssessmentPlan(plan));
    }
  }, [assessmentPlanData])

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
    }
  }, [projectData])

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

  const handleEditAssessmentPlan = () => {
    history.push(`/${window.contextPath}/employee/pm/project/${projectId}/assessment/create?assessmentId=${assessmentPlan.id}`);
  };

  const handleDownload = () => {
    const rowsToExport = selectedFacilityIds.length
      ? fetchedData.filter((facility) => selectedFacilityIds.includes(facility.id))
      : fetchedData;

    const header = [
      t("PM_ASSESSMENT_FACILITY_NAME"),
      t("PM_ASSESSMENT_FACILITY_TYPE"),
      t("CS_DISTRICT"),
      t("CS_BLOCK"),
      t("PM_ASSESSMENT_REMOTE_STATUS"),
      t("PM_ASSESSMENT_ONSITE_STATUS"),
      t("PM_ASSESSMENT_RESULT"),
    ];
    const rows = rowsToExport.map((facility) => [
      facility.name,
      facility.facilityType,
      facility.district,
      facility.block,
      t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.remoteStatus}`),
      t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.onSiteStatus}`),
      t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.result}`),
    ]);

    const csvContent = [header, ...rows]
      .map((row) => row.map((cell) => `"${String(cell ?? "").replace(/"/g, '""')}"`).join(","))
      .join("\n");

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = `${assessmentPlan.name}-facilities.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(downloadUrl);
  };

  const GetCell = (value) => <span className="cell-text" style={{ color: "#000000" }}>{value}</span>;

  const GetStatusBadge = (code) => {
    if (!code) {
      return GetCell("-");
    }
    const style = STATUS_BADGE_STYLES[code] || STATUS_BADGE_STYLES.NOT_INITIATED;
    return (
      <span
        style={{
          ...style,
          borderRadius: "4px",
          padding: "4px 12px",
          fontSize: "13px",
          fontWeight: 500,
          whiteSpace: "nowrap",
          width: "fit-content",
          display: "inline-block",
        }}
      >
        {t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`)}
      </span>
    );
  };

  const mainCheckboxChange = () => {
    const prevMainCheck = mainCheck;
    setMainCheck(!prevMainCheck);
    setSelectedFacilityIds(!prevMainCheck ? fetchedData.map((row) => row.id) : []);
  };

  const sideCheckboxChange = (id) => {
    setMainCheck(false);

    if (selectedFacilityIds.some((facilityId) => facilityId === id)) {
      setSelectedFacilityIds(selectedFacilityIds.filter((facilityId) => facilityId !== id));
    } else {
      setSelectedFacilityIds([...selectedFacilityIds, id]);
    }
  };

  const columns = [
    {
      id: "selection",
      Header: () => (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
          <CustomCheckBox
            checked={mainCheck}
            onChange={mainCheckboxChange}
            styles={{ width: "24px", height: "24px" }}
          />
        </div>
      ),
      Cell: ({ row }) => (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
          <CustomCheckBox
            checked={selectedFacilityIds.some((facilityId) => facilityId === row.original["id"])}
            onChange={() => sideCheckboxChange(row.original["id"])}
            styles={{ width: "24px", height: "24px" }}
          />
        </div>
      ),
    },
    {
      Header: t("PM_ASSESSMENT_FACILITY_NAME"),
      Cell: ({ row }) => (
        <span style={{ fontWeight: 700, color: "#0B0C0C" }}>{row.original["name"]}</span>
      ),
    },
    {
      Header: t("PM_ASSESSMENT_FACILITY_TYPE"),
      Cell: ({ row }) => GetCell(row.original["facilityType"] || "-"),
    },
    {
      Header: t("PM_ASSESSMENT_DISTRICT_BLOCK"),
      Cell: ({ row }) => (
        <div>
          <div>{row.original["district"] || "-"}</div>
          <div style={{ color: "#6B7280", fontSize: "12px" }}>{row.original["block"]}</div>
        </div>
      ),
    },
    {
      Header: t("PM_ASSESSMENT_REMOTE_STATUS"),
      Cell: ({ row }) => GetStatusBadge(row.original["remoteStatus"]),
    },
    {
      Header: t("PM_ASSESSMENT_ONSITE_STATUS"),
      Cell: ({ row }) => GetStatusBadge(row.original["onSiteStatus"]),
    },
    {
      Header: t("PM_ASSESSMENT_RESULT"),
      Cell: ({ row }) => GetStatusBadge(row.original["result"]),
    },
  ];

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("PM_ASSESSMENT_NO_FACILITIES_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div style={{ backgroundColor: "white", padding: "15px 0px 0px 0px" }}>
        <div
          className={"health-facility-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            customTableWrapperClassName={"health-facility-table"}
            data={fetchedData}
            columns={columns}
            getCellProps={() => {
              return {
                style: {
                  maxWidth: "100%",
                  padding: "17.24px 18px",
                  fontSize: "15px",
                },
              };
            }}
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={facilityData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    );
  }

  if (projectDataLoading || assessmentPlanDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
      {(!isLoading && facilityDataFetching) && (
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
      <div style={{ display: "flex", flexWrap: "wrap", gap: "15px", alignItems: "center", justifyContent: "space-between", marginBottom: "20px" }}>
        <div style={{ fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", color: "#0B0C0C" }}>
          {assessmentPlan?.name}
        </div>
        <div style={{ display: "flex", gap: "12px" }}>
          <button
            type="button"
            className={"jk-digit-secondary-btn"}
            style={{ height: "40px", padding: "0px 20px", fontSize: "16px", fontWeight: "500", fontFamily: "Roboto", cursor: "pointer" }}
            onClick={handleEditAssessmentPlan}
          >
            {t("PM_ACTION_EDIT_ASSESSMENT_PLAN")}
          </button>
          <button
            type="button"
            disabled
            style={{
              height: "40px",
              padding: "0px 20px",
              fontSize: "16px",
              fontWeight: "500",
              fontFamily: "Roboto",
              border: "none",
              borderRadius: "4px",
              backgroundColor: "#D6D5D4",
              color: "#6B7280",
              cursor: "default",
            }}
          >
            {t("PM_ACTION_PROCEED_ASSESSMENT_PLAN_CREATION")}
          </button>
        </div>
      </div>
      <InfoCard t={t} selectedAssessmentPlan={assessmentPlan} />
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter
            t={t}
            projectQueryFilter={projectQueryFilter}
            onFilterChange={handleFilterChange}
          />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <SearchAction
              t={t}
              projectQueryFilter={projectQueryFilter}
              selectedFacilityIds={selectedFacilityIds}
              onSearch={handleFilterChange}
              onDownload={handleDownload}
            />
          </div>
          {renderFacilities()}
        </div>
      </div>
    </div>
  );
};

export default AssessmentDetails;
