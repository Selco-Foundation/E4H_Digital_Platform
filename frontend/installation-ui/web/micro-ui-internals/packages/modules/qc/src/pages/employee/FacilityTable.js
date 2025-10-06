import React, { useEffect, useRef, useState } from "react";
import {CheckBox, Loader, Table} from "@egovernments/digit-ui-react-components";
import Filter from "../../components/FacilityTable/Filter";
import InfoCard from "../../components/FacilityTable/InfoCard";
import { Link, useHistory, useLocation } from "react-router-dom";
import { useDispatch } from "react-redux";
import { setSelectedFacility, setSelectedFieldPlan } from "../../redux/actions";
import SearchActionCentre from "../../components/FacilityTable/SearchAction";
import useFieldPlan from "../../hooks/useFieldPlan";
import useFacility from "../../hooks/useFacility";
import CustomCheckBox from "../../components/Custom/CustomCheckBox";

const FacilityTable = ({ t }) => {

  const [mainCheck, setMainCheck] = useState(false);
  const dispatch = useDispatch();
  const [fieldPlan, setFieldPlan] = useState({});
  const [selectedFacilities, setSelectedFacilities] = useState([]);
  const [fetchedData, setData] = useState([]);
  const history = useHistory();
  const location = useLocation();
  const url = window.location.href;
  const fieldPlanId = url.split("field-plan/")[1].split("/")[0];
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
      projectTypeId: "Facility",
      parent: fieldPlanId,
    },
    facilityFilter: {
      district: [],
      block: [],
      status: []
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
    isLoading: fieldPlanDataLoading,
    isFetching: fieldPlanDataFetching,
    data: fieldPlanData,
    revalidate: revalidateFieldPlans,
  } = useFieldPlan({
    Project : {
      projectTypeId: "FieldPlan",
      id: [fieldPlanId]
    }
  });

  const {
    isLoading,
    isFetching: facilityDataFetching,
    data: facilityData,
    revalidate: revalidateFacilities,
    revalidateFacilityDetails
  } = useFacility(projectQueryFilter, pageSize, pageOffset);

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
      const refactoredDataCopy = facilityData?.facilities.map((row) => ({
        ...row,
        projectName: fieldPlan?.name,
        status: row?.status,
      }));

      setData(refactoredDataCopy);
      setSelectedFacilities([]);
      setMainCheck(false);
    }
  }, [facilityData, fieldPlan])

  useEffect(() => {
    if (fieldPlanData) {
      setFieldPlan(fieldPlanData.fieldPlans[0]);
      dispatch(setSelectedFieldPlan(fieldPlanData.fieldPlans[0]));
    }
  }, [fieldPlanData])

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

  const GetCell = (value) => <span className="cell-text">{value}</span>;

  const mainCheckboxChange = () => {
    const prevMainCheck = mainCheck;
    setMainCheck(!prevMainCheck);
    if(!prevMainCheck) {
      setSelectedFacilities(
        fetchedData
          .filter((row) => row?.status === "SUBMITTED_BY_SUPERVISOR")
          .map((row) => row.id)
      );
    } else {
      setSelectedFacilities([]);
    }
  };

  const sideCheckboxChange = (id) => {
    setMainCheck(false);

    if (selectedFacilities.some((facilityId) => facilityId === id)) {
      setSelectedFacilities(selectedFacilities.filter((facilityId) => facilityId !== id));
    } else {
      setSelectedFacilities([...selectedFacilities, id]);
    }
  };

  const revalidateData = () => {
    setMainCheck(false);
    setSelectedFacilities([]);
    revalidateFieldPlans();
    revalidateFacilities();
    revalidateFacilityDetails();
  }

  const columns = [
    {
      id: "selection",
      Header: () => (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", top: 0 }}>
          <CustomCheckBox
            checked={mainCheck}
            onChange={mainCheckboxChange}
            styles={{ width: "24px", height: "24px" }}
          />
        </div>
      ),
      Cell: ({ row }) => {
        return row.original["status"] === "SUBMITTED_BY_SUPERVISOR" ? (
          <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
            <CustomCheckBox
              checked={selectedFacilities.some((facilityId) => facilityId === row.original["id"])}
              onChange={() => sideCheckboxChange(row.original["id"])}
              styles={{ width: "24px", height: "24px" }}
            />
          </div>
        ) : (
          <div></div>
        );
      },
    },
    {
      Header: t("CS_HEALTH_FACILITY"),
      Cell: ({ row }) => {
        return (
          <div>
            <span className="link" onClick={() => dispatch(setSelectedFacility(row.original))}>
              <Link
                to={`/${window.contextPath}/employee/qc/field-plan/${fieldPlanId}/facilities/${row.original["id"]}--${encodeURIComponent(row.original["facilityId"])}`}
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
        return GetCell(row.original["block"] ? t(`BLOCK_${row.original["block"].toUpperCase()}`) : "-");
      },
    },
    {
      Header: t("CS_DISTRICT"),
      Cell: ({ row }) => {
        return GetCell(row.original["district"] ? t(`DISTRICT_${row.original["district"].toUpperCase()}`) : "-");
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
      name: t("CS_PENDING_INSTALLATION"),
      code: "PENDING_INSTALLATION"
    },
    {
      name: t("CS_SUBMITTED_BY_FIELD_STAFF"),
      code: "SUBMITTED_BY_FIELD_STAFF"
    },
    {
      name: t("CS_REJECTED_BY_FIELD_SUPERVISOR"),
      code: "REJECTED_BY_FIELD_SUPERVISOR"
    },
    {
      name: t("CS_SUBMITTED_BY_SUPERVISOR"),
      code: "SUBMITTED_BY_SUPERVISOR"
    },
    {
      name: t("CS_PENDING_APPROVAL_FLAGGED_FOR_QC"),
      code: "PENDING_APPROVAL_FLAGGED_FOR_QC"
    },
    {
      name: t("CS_APPROVED_BY_QC_SPOC"),
      code: "APPROVED_BY_QC_SPOC"
    },
    {
      name: t("CS_REJECTED_BY_QC_SPOC"),
      code: "REJECTED_BY_QC_SPOC"
    }
  ];

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100%" }}>
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
        <div style={{
          margin: "0px 20px",
          overflow: "auto",
        }}>
          <Table
            t={t}
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

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      {(updatingWorkflow || (!fieldPlanDataLoading && fieldPlanDataFetching) || (!isLoading && facilityDataFetching)) && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 5,
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
      <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
        Installation | {fieldPlan?.name}
      </div>
      <InfoCard t={t} selectedFieldPlan={fieldPlan} />
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter
            t={t}
            type="desktop"
            fieldPlan={fieldPlan}
            projectQueryFilter={projectQueryFilter}
            onFilterChange={handleFilterChange}
            statusesList={statusesList}
          />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <div style={{ fontSize: "20px", fontWeight: "bold", marginBottom: "40px" }}>
              Reports
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
export default FacilityTable;
