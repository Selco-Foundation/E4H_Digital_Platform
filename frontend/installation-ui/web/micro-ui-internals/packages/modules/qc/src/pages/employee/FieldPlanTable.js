import React, { useEffect, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import SearchCentre from "../../components/FieldPlanTable/Search";
import { setSelectedFieldPlan } from "../../redux/actions";
import { Link, useRouteMatch } from "react-router-dom";
import { useDispatch } from "react-redux";

const FieldPlanTable = ({ t, getCellProps, onNextPage, onPrevPage, currentPage, totalRecords, pageSizeLimit, onPageSizeChange }) => {

  const [centreNameToSearch, setCentreNameToSearch] = useState("");
  const [fetchedData, setData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const dispatch = useDispatch();
  const { path } = useRouteMatch();
  const [queryFilter, setQueryFilter] = useState({
    Project : {
      projectTypeId: "FieldPlan"
    }
  });
  const { isLoading, data} = Digit.Hooks.qc.useFieldPlan(queryFilter);

  const submitFunc = () => {
    setFilteredData(fetchedData.filter((row) => row.code.includes(centreNameToSearch)));
  };

  const clearFunc = () => {
    setCentreNameToSearch("");
    setFilteredData(fetchedData);
  };

  useEffect(() => {
    if (data) {
      setData(data.Project);
    }
  }, [isLoading]);

  const GetCell = (value) => <span className="cell-text">{value}</span>;

  const GetProgress = (value) => {
    return (
      <div style={{ display: "flex", gap: `${value > 99 ? "10px" : "20px"}` }}>
        <div>{value}%</div>
        <div style={{ width: "100px", height: "20px", background: "#E0E0E0", borderRadius: "5px" }}>
          <div style={{ position: "absolute", height: "20px", width: `${value}px`, background: "#00703C", borderRadius: "5px" }}></div>
        </div>
      </div>
    );
  };

  const columnsList = [
    {
      Header: "Field Plan Code",
      Cell: ({ row }) => {
        return (
          <div>
            <span className="link" onClick={() => dispatch(setSelectedFieldPlan(row.original))}>
              <Link to={`${path}/${encodeURIComponent(row.original["name"])}/facilities`} style={{ color: "#C84C0E" }}>
                {row.original["name"]}
              </Link>
            </span>
          </div>
        );
      },
    },
    {
      Header: "Activity Type",
      Cell: ({ row }) => {
        return GetCell(`${row.original["projectType"]}`);
      },
    },
    {
      Header: "Health Facilities",
      Cell: ({ row }) => {
        return GetCell(`${row.original["facilitiesCount"]}`);
      },
    },
    {
      Header: "Start Date",
      Cell: ({ row }) => {
        return GetCell(`${row.original["startDate"]}`);
      },
    },
    {
      Header: "End Date",
      Cell: ({ row }) => {
        return GetCell(`${row.original["endDate"]}`);
      },
    },
    {
      Header: "Completion Rate",
      Cell: ({ row }) => {
        return GetProgress(`${row.original["completionRate"]}`);
      },
    },
  ];

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0"); // months are 0-based
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${month}/${day}/${year}`;
  };

  const refactoredData = fetchedData?.map((row) => {
    return {
      id: row?.project?.id,
      name: row?.project?.name || row?.project?.projectNumber,
      projectType: row?.project?.projectType,
      facilitiesCount: row?.project?.additionalDetails?.countFacilities,
      startDate: formatDate(row?.project?.startDate),
      endDate: formatDate(row?.project?.endDate),
      completionRate: 30,
      status: row?.status,
      transactions: row?.transactions
    };
  })

  if (isLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        Inbox
      </div>
      <SearchCentre centreName={centreNameToSearch} setCentreName={setCentreNameToSearch} onSubmit={submitFunc} onClear={clearFunc} />
      <Table
        t={t}
        data={refactoredData}
        columns={columnsList}
        getCellProps={getCellProps}
        onNextPage={onNextPage}
        onPrevPage={onPrevPage}
        currentPage={currentPage}
        totalRecords={totalRecords}
        onPageSizeChange={onPageSizeChange}
        pageSizeLimit={pageSizeLimit}
      />
    </div>
  );
};

export default FieldPlanTable;
