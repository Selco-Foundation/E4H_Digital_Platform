import React, { useState } from "react";
import { CheckBox, Table } from "@egovernments/digit-ui-react-components";
import Filter from "./component/Filter";
import InfoCard from "./component/InfoCard";
import { Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { setSelectedFacility } from "../../../redux/actions";

const FacilityTable = ({ t, getCellProps, onNextPage, onPrevPage, currentPage, totalRecords, pageSizeLimit, onPageSizeChange }) => {
  const data = [
    {
      id: 1,
      facility: "Alkod",
      projectId : "PROJ/2025/000023",
      facilityId : "123455",
      block: "Allepy",
      district: "Alkrias",
      assigned: "",
      status: "Scheduled",
    },
    {
      id: 2,
      facility: "Alkod",
      projectId : "PROJ/2025/000023",
      facilityId : "123456",
      block: "Allepy",
      district: "Alkrias",
      assigned: "",
      status: "Scheduled",
    },
    {
      id: 3,
      facility: "Chorias",
      projectId : "PROJ/2025/000023",
      facilityId : "123457",
      block: "Konark",
      district: "Raigarh",
      assigned: "Sufi",
      status: "Pending Approval",
    },
    {
      id: 4,
      facility: "Chorias",
      projectId : "PROJ/2025/000023",
      facilityId : "123458",
      block: "Konark",
      district: "Raigarh",
      assigned: "Sufi",
      status: "Pending Approval",
    },
    {
      id: 5,
      facility: "Chorias",
      projectId : "PROJ/2025/000023",
      facilityId : "123459",
      block: "Konark",
      district: "Raigarh",
      assigned: "Sufi",
      status: "Pending Installation",
    },
  ];
  const [mainCheck, setMainCheck] = useState(false);
  const dispatch = useDispatch();
  const selectedFieldPlan = useSelector((state) => state.qc.reports.selectedFieldPlan);
  const [sideCheck, setSideCheck] = useState(
    data
      .filter((row) => row.status.toUpperCase() !== "APPROVED" && row.status.toUpperCase() !== "SCHEDULED")
      .map((filteredRow) => {
        return { [`checkBox-${filteredRow.id}`]: false };
      })
  );
  const [filteredData, setFilteredData] = useState(data);

  const [filters, setFilters] = useState({
    district: null,
    block: null,
    status: [],
  });

  const onFilterApply = () => {
    console.log(filters);
    const filterDistricts = filters.district !== null ? data.filter((row) => row.district === filters.district) : data;
    const filterBlock = filters.block !== null ? filterDistricts.filter((row) => row.block === filters.block) : filterDistricts;
    const filterStatus = filters.status.length !== 0 ? filterBlock.filter((row) => filters.status.includes(row.status)) : filterBlock;
    setFilteredData(filterStatus);
  };

  const cleanFilters = () => {
    setFilters({
      district: null,
      block: null,
      status: [],
    });
    setFilteredData(data);
  };

  const GetCell = (value) => <span className="cell-text">{value}</span>;

  const mainCheckboxChange = () => {
    const prevMainCheck = mainCheck;
    setMainCheck(!prevMainCheck);
    setSideCheck(
      sideCheck.map((side) => {
        return { [Object.keys(side)[0]]: !prevMainCheck };
      })
    );
  };

  const sideChecboxChange = (sideCheckboxId) => {
    setSideCheck((prev) => [
      ...prev.filter((checkBox) => Object.keys(checkBox)[0] !== sideCheckboxId),
      {
        [sideCheckboxId]: !sideCheck.filter((check) => Object.keys(check)[0] === sideCheckboxId)[0][sideCheckboxId],
      },
    ]);
    setMainCheck(false);
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
        return row.original["status"].toUpperCase() !== "APPROVED" && row.original["status"].toUpperCase() !== "SCHEDULED" ? (
          <div style={{ marginTop: "-1.2em" }}>
            <CheckBox
              checked={sideCheck.filter((check) => Object.keys(check)[0] === `checkBox-${row.original["id"]}`)[0][`checkBox-${row.original["id"]}`]}
              onChange={() => sideChecboxChange(`checkBox-${row.original["id"]}`)}
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
            <span className="link" onClick={() => dispatch(setSelectedFacility(row.original["facilityId"]))}>
              <Link
                to={`/${window.contextPath}/employee/qc/field-plan/${encodeURIComponent(row.original["projectId"])}/facilities/${encodeURIComponent(row.original["facilityId"])}`}
                style={{ color: "#C84C0E" }}
              >
                {row.original["facility"]}
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
    return { name: status, count: data.filter((opt) => opt.status === status).length };
  });
  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        Installation | {selectedFieldPlan}
      </div>
      <InfoCard />
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ width: "15%" }}>
          <Filter
            data={data}
            type="desktop"
            installationsWithCount={installationsWithCount}
            onFilter={onFilterApply}
            onFilterClear={cleanFilters}
            filter={filters}
            setFilter={setFilters}
          />
        </div>
        <div style={{ width: "83%" }}>
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
  );
};
export default FacilityTable;
