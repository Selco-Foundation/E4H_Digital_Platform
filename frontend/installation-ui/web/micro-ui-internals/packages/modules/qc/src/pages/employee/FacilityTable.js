import React, { useEffect, useRef, useState } from "react";
import {CheckBox, Loader, Table} from "@egovernments/digit-ui-react-components";
import Filter from "../../components/FacilityTable/Filter";
import InfoCard from "../../components/FacilityTable/InfoCard";
import { Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { setSelectedFacility } from "../../redux/actions";
import SearchCentre from "../../components/FacilityTable/Search";

const FacilityTable = ({ t, getCellProps }) => {

  const [mainCheck, setMainCheck] = useState(false);
  const dispatch = useDispatch();
  const selectedFieldPlan = useSelector((state) => state.qc.common.selectedFieldPlan);
  const [filters, setFilters] = useState({
    district: [],
    block: [],
    status: []
  });
  const [selectedFacilities, setSelectedFacilities] = useState([]);
  const [fetchedData, setData] = useState([]);
  const [sideCheck, setSideCheck] = useState({});
  const [projectQueryFilter, setProjectQueryFilter] = useState({
    Project : {
      projectTypeId: "Facility",
      parent: selectedFieldPlan.id,
    }
  });
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);

  const { isLoading, data } = Digit.Hooks.qc.useFacility(projectQueryFilter, pageSize, pageOffset);

  useEffect(() => {
    if (data) {
      const refactoredDataCopy = data?.facilities.map((row) => ({
        ...row,
        projectName: selectedFieldPlan?.name,
        status: t(row?.status) || "-",
      }));

      setData(refactoredDataCopy);
      const newSideCheck = {};
      refactoredDataCopy.forEach((row) => {
        if (row?.status === t("SUBMITTED_BY_SUPERVISOR")) {
          newSideCheck[`${row?.id}`] = false;
        }
      })
      setSideCheck(newSideCheck);
      setMainCheck(false);
    }
  }, [data])

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

  const onFilterApply = () => {
    console.debug("onFilterApply", filters);
  };

  const clearFilters = () => {
    setFilters({
      district: [],
      block: [],
      status: [],
    });
  };

  const onSearch = (textToSearch) => {
    console.debug("onSearch", textToSearch);
  }

  const onSearchClear = () => {
    console.debug("onSearchClear");
  }

  const GetCell = (value) => <span className="cell-text">{value}</span>;

  const mainCheckboxChange = () => {
    const prevMainCheck = mainCheck;
    setMainCheck(!prevMainCheck);
    const newSideCheck = sideCheck;
    Object.keys(newSideCheck).forEach((side) => {
      newSideCheck[`${side}`] = !prevMainCheck;
    })
    setSideCheck(newSideCheck);
    if(!prevMainCheck) {
      setSelectedFacilities(
        fetchedData
          .filter((row) => row?.status === t("SUBMITTED_BY_SUPERVISOR"))
          .map((row) => row.id)
      );
    } else {
      setSelectedFacilities([]);
    }
  };

  const sideCheckboxChange = (sideCheckboxId, id) => {
    const newSideCheck = sideCheck;
    Object.keys(newSideCheck).forEach((side) => {
      if(side === sideCheckboxId)
        newSideCheck[`${side}`] = !newSideCheck[`${side}`];
    })

    setSideCheck(newSideCheck);
    setMainCheck(false);

    if (selectedFacilities.some((facilityId) => facilityId === id)) {
      setSelectedFacilities(selectedFacilities.filter((facilityId) => facilityId !== id));
    } else {
      setSelectedFacilities([...selectedFacilities, id]);
    }
  };

  const columns = [
    {
      id: "selection",
      Header: () => (
        <div style={{ marginTop: "-1.2em" }}>
          <CheckBox checked={mainCheck} onChange={mainCheckboxChange} />
        </div>
      ),
      Cell: ({ row }) => {
        return row.original["status"] === t("SUBMITTED_BY_SUPERVISOR") ? (
          <div style={{ marginTop: "-1.2em" }}>
            <CheckBox
              checked={sideCheck[`${row.original["id"]}`]}
              onChange={() => sideCheckboxChange(`${row.original["id"]}`, row.original["id"])}
            />
          </div>
        ) : (
          <div></div>
        );
      },
    },
    {
      Header: "Health Facility",
      Cell: ({ row }) => {
        return (
          <div>
            <span className="link" onClick={() => dispatch(setSelectedFacility(row.original))}>
              <Link
                to={`/${window.contextPath}/employee/qc/field-plan/${encodeURIComponent(row.original["projectName"])}/facilities/${encodeURIComponent(row.original["facilityName"])}`}
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
      Header: "Block",
      Cell: ({ row }) => {
        return GetCell(`${row.original["block"].toUpperCase()}`);
      },
    },
    {
      Header: "District",
      Cell: ({ row }) => {
        return GetCell(`${row.original["district"].toUpperCase()}`);
      },
    },
    {
      Header: "Assigned To",
      Cell: ({ row }) => {
        return GetCell(`${row.original["assigned"].toUpperCase()}`);
      },
    },
    {
      Header: "Status",
      Cell: ({ row }) => {
        return GetCell(`${row.original["status"]}`);
      },
    },
  ];

  const getStatusCount = (status) => {
    return selectedFieldPlan?.projectFacilityInfo?.[status] || 0;
  }

  //todo: fetch all possible statuses from backend??
  const statusesWithCount = [
    {
      name: t("CS_UNASSIGNED"),
      code: "UNASSIGNED",
      count: getStatusCount("UNASSIGNED")
    },
    {
      name: t("CS_PENDING_INSTALLATION"),
      code: "PENDING_INSTALLATION",
      count: getStatusCount("ASSIGNED_TO_SUPERVISOR") + getStatusCount("ASSIGNED_TO_FIELD_STAFF")
    },
    {
      name: t("CS_SUBMITTED_BY_FIELD_STAFF"),
      code: "SUBMITTED_BY_FIELD_STAFF",
      count: getStatusCount("SUBMITTED_BY_FIELD_STAFF")
    },
    {
      name: t("CS_REJECTED_BY_SUPERVISOR"),
      code: "REJECTED_BY_SUPERVISOR",
      count: getStatusCount("REJECTED_BY_SUPERVISOR")
    },
    {
      name: t("CS_SUBMITTED_BY_SUPERVISOR"),
      code: "SUBMITTED_BY_SUPERVISOR",
      count: getStatusCount("SUBMITTED_BY_SUPERVISOR")
    },
    {
      name: t("CS_APPROVED_BY_QC_SPOC"),
      code: "APPROVED_BY_QC_SPOC",
      count: getStatusCount("APPROVED_BY_QC_SPOC")
    },
    {
      name: t("CS_REJECTED_BY_QC_SPOC"),
      code: "REJECTED_BY_QC_SPOC",
      count: getStatusCount("REJECTED_BY_QC_SPOC")
    }
  ];

  if (isLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        Installation | {selectedFieldPlan?.name}
      </div>
      <InfoCard selectedFieldPlan={selectedFieldPlan} />
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ width: "15%" }}>
          <Filter
            filter={projectQueryFilter}
            type="desktop"
            statusesWithCount={statusesWithCount}
            onFilter={onFilterApply}
            onFilterClear={clearFilters}
            setFilter={setFilters}
          />
        </div>
        <div style={{ width: "83%", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <div style={{ fontSize: "20px", fontWeight: "bold", marginBottom: "40px" }}>
              Reports
            </div>
            <SearchCentre
              mainCheckBox={mainCheck}
              selectedFacilities={selectedFacilities}
              filter={projectQueryFilter}
              onSearch={onSearch}
              onClear={onSearchClear}
            />
          </div>
          <div style={{ width: "90%", marginLeft: "auto", marginRight: "auto", overflowX: "auto" }}>
            <Table
              t={t}
              data={fetchedData}
              columns={columns}
              getCellProps={getCellProps}
              onNextPage={onNextPage}
              onPrevPage={onPrevPage}
              currentPage={Math.floor(pageOffset / pageSize)}
              totalRecords={data?.totalCount}
              onPageSizeChange={onPageSizeChange}
              pageSizeLimit={pageSize}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
export default FacilityTable;
