import React, { useEffect, useRef, useState } from "react";
import {CheckBox, Loader, Table} from "@egovernments/digit-ui-react-components";
import Filter from "../../components/FacilityTable/Filter";
import InfoCard from "../../components/FacilityTable/InfoCard";
import { Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { setSelectedFacility } from "../../redux/actions";

const FacilityTable = ({ t, getCellProps, onNextPage, onPrevPage, currentPage, totalRecords, pageSizeLimit, onPageSizeChange }) => {

  const [mainCheck, setMainCheck] = useState(false);
  const dispatch = useDispatch();
  const selectedFieldPlan = useSelector((state) => state.qc.common.selectedFieldPlan);
  const [filters, setFilters] = useState({
    district: null,
    block: null,
    status: [],
  });
  const [selectedFacilities, setSelectedFacilities] = useState([]);
  const [fetchedData, setData] = useState([]);
  const [refactoredData, setRefactoredData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [sideCheck, setSideCheck] = useState({});
  const [projectQueryFilter, setProjectQueryFilter] = useState({
    Project : {
      projectTypeId: "Facility",
      parent: selectedFieldPlan.id,
    }
  });

  const { isLoading, data } = Digit.Hooks.qc.useFacility(projectQueryFilter);
  useEffect(() => {
    if (data) {
      const refactoredDataCopy = data.map((row) => ({
        ...row,
        projectName: selectedFieldPlan?.name,
        status: t(row?.status) || "-",
      }));

      setFilteredData(refactoredDataCopy);
      setRefactoredData(refactoredDataCopy);
      const newSideCheck = {};
      refactoredDataCopy.forEach((row) => {
        if (row?.status === t("SUBMITTED_BY_SUPERVISOR")) {
          newSideCheck[`${row?.id}`] = false;
        }
      })
      setSideCheck(newSideCheck);
      setMainCheck(false);
    }
  }, [isLoading])

  const onFilterApply = () => {
    const filterDistricts = filters.district !== null ? refactoredData.filter((row) => row?.district === filters.district) : refactoredData;
    const filterBlock = filters.block !== null ? filterDistricts.filter((row) => row?.block === filters.block) : filterDistricts;
    const filterStatus = filters.status.length !== 0 ? filterBlock.filter((row) => filters.status.includes(row?.status)) : filterBlock;
    setFilteredData(filterStatus);
  };

  const cleanFilters = () => {
    setFilters({
      district: null,
      block: null,
      status: [],
    });
    setFilteredData(refactoredData);
  };

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
        refactoredData
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

  const installationStatuses = ["Pending Installation", "Approved", "Rejected", "Pending Approval", "Scheduled"];

  const installationsWithCount = installationStatuses.map((status) => {
    return { name: status, count: refactoredData.filter((opt) => opt.status === status).length };
  });

  if (isLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        Installation | {selectedFieldPlan?.name}
      </div>
      <InfoCard />
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ width: "15%" }}>
          <Filter
            data={refactoredData}
            type="desktop"
            installationsWithCount={installationsWithCount}
            onFilter={onFilterApply}
            onFilterClear={cleanFilters}
            filter={filters}
            setFilter={setFilters}
          />
        </div>
        <div style={{ width: "83%", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <div style={{ fontSize: "20px", fontWeight: "bold", marginBottom: "20px" }}>
              Reports
            </div>
            <div style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: "10px",
              width: "90%",
              marginLeft: "auto",
              marginRight: "auto",
              height: "20px",
            }}>
              { selectedFacilities.length > 0 ? (
                <div style={{ fontSize: "16px", fontWeight: "bold", color: "#004d66" }}>
                  {selectedFacilities.length} Health Facilities Selected
                </div>
              ) : (
                <input
                  type="text"
                  placeholder="Search Health Facilities"
                  style={{
                    padding: "8px",
                    width: "250px",
                    border: "1px solid #ccc",
                    borderRadius: "4px",
                  }}
                />
              ) }
              <div style={{ display: "flex", gap: "10px" }}>
                { selectedFacilities.length > 0 && (
                  <button style={{
                    border: "1px solid #d35400",
                    backgroundColor: "#d35400",
                    padding: "6px 12px",
                    cursor: "pointer",
                    color: "white",
                    fontWeight: "bold"
                  }}>
                    Approve
                  </button>
                )}
                <button style={{
                  backgroundColor: "white",
                  border: "1px solid #d35400",
                  color: "#d35400",
                  padding: "6px 12px",
                  cursor: "pointer",
                  fontWeight: "bold"
                }}>
                  Download
                </button>
              </div>
            </div>
          </div>
          <div style={{ width: "90%", marginLeft: "auto", marginRight: "auto", overflowX: "auto" }}>
            <Table
              t={t}
              data={filteredData}
              columns={columns}
              getCellProps={getCellProps}
              onNextPage={onNextPage}
              onPrevPage={onPrevPage}
              currentPage={currentPage}
              totalRecords={totalRecords}
              onPageSizeChange={onPageSizeChange}
              pageSizeLimit={pageSizeLimit}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
export default FacilityTable;
