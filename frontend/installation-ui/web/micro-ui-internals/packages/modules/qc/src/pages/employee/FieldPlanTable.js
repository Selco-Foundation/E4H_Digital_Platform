import React, { useEffect, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import SearchCentre from "../../components/FieldPlanTable/Search";
import { setSelectedFieldPlan } from "../../redux/actions";
import { Link, useRouteMatch } from "react-router-dom";
import { useDispatch } from "react-redux";

const FieldPlanTable = ({ t, getCellProps }) => {

  const [fetchedData, setData] = useState([]);
  const dispatch = useDispatch();
  const { path } = useRouteMatch();
  const [queryFilter, setQueryFilter] = useState({
    Project : {
      projectTypeId: "FieldPlan"
    }
  });
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);

  const { isLoading, data } = Digit.Hooks.qc.useFieldPlan(queryFilter, pageSize, pageOffset);

  const onSearch = (textToSearch) => {
    setQueryFilter({
      Project : {
        projectTypeId: "FieldPlan",
        name: textToSearch
      }
    });
  }

  const onClear = () => {
    setQueryFilter({
      Project : {
        projectTypeId: "FieldPlan"
      }
    });
  }

  useEffect(() => {
    if (data) {
      setData(data.fieldPlans);
    }
  }, [data]);

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

  const GetPendingApprovalCount = (projectFacilityInfo) => {
    const value = projectFacilityInfo["SUBMITTED_BY_SUPERVISOR"];
    if (value) {
      return (
        <div
          style={{
            minWidth: "30px",
            width: "fit-content",
            height: "20px",
            padding: "0 5px",
            background: "red",
            borderRadius: "10px",
            color: "white",
            textAlign: "center"
          }}
        >
          <div>{value}</div>
        </div>
      )
    }
  }

  const columnsList = [
    {
      Header: "Field Plan Code",
      Cell: ({ row }) => {
        return (
          <div style={{display: "flex", alignItems: "center", gap: "10px"}}>
            <span className="link" onClick={() => dispatch(setSelectedFieldPlan(row.original))}>
              <Link to={`${path}/${encodeURIComponent(row.original["name"])}/facilities`} style={{ color: "#C84C0E" }}>
                {row.original["name"]}
              </Link>
            </span>
            {GetPendingApprovalCount(row.original["projectFacilityInfo"])}
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

  if (isLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        Inbox
      </div>
      <SearchCentre queryFilter={queryFilter} onSearch={onSearch} onClear={onClear} />
      <Table
        t={t}
        data={fetchedData}
        columns={columnsList}
        getCellProps={getCellProps}
        onNextPage={onNextPage}
        onPrevPage={onPrevPage}
        currentPage={Math.floor(pageOffset / pageSize)}
        totalRecords={data.totalCount}
        onPageSizeChange={onPageSizeChange}
        pageSizeLimit={pageSize}
      />
    </div>
  );
};

export default FieldPlanTable;
