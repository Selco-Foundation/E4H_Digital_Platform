import React from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { Card, Loader } from "@selco/digit-ui-react-components";
import ComplaintsLink from "./inbox/ComplaintLinks";
import ComplaintTable from "./inbox/ComplaintTable";
import Filter from "./inbox/Filter";
import SearchComplaint from "./inbox/search";
import { LOCALE } from "../constants/Localization";

const DesktopInbox = ({
  data,
  onFilterChange,
  onSearch,
  isLoading,
  searchParams,
  onNextPage,
  onPrevPage,
  currentPage,
  pageSizeLimit,
  onPageSizeChange,
  totalRecords,
}) => {
  const { t } = useTranslation();
  const GetCell = (value) => <span className="cell-text">{value}</span>;
  const GetSlaCell = (value) => {
    return value < 0 ? <span className="sla-cell-error">{value || ""}</span> : <span className="sla-cell-success">{value || ""}</span>;
  };
  const iPadMaxWidth=1024;
  const iPadMinWidth=768
  const [isIpadView, setIsIpadView] = React.useState(window.innerWidth <= iPadMaxWidth && window.innerWidth>=iPadMinWidth);
  const onResize = () => {
    if (window.innerWidth >=iPadMinWidth && window.innerWidth <= iPadMaxWidth ) {
      if (!isIpadView) {
        setIsIpadView(true);
      }
    } else {
      if (isIpadView) {
        setIsIpadView(false);
      }
    }
  };
  React.useEffect(() => {
    window.addEventListener("resize", () => {
      onResize();
    });
    return () => {
      window.addEventListener("resize", () => {
        onResize();
      });
    };
  });


  const columns = React.useMemo(
    () => [
      {
        Header: t("CS_COMMON_TICKET_NO"),
        Cell: ({ row }) => {
          
          return (
            <div>
              <span className="link">
                <Link to={`/${window.contextPath}/employee/im/complaint/details/` + row.original["incidentId"] + "/" + row.original["tenantId"]} style={{color:"#7a2829"}}>{row.original["incidentId"]}</Link>
              </span>
             
            </div>
          );
        },
      },
      {
        Header: t("CS_TICKET_TYPE"),
        Cell: ({ row }) => {
          return GetCell(t(`SERVICEDEFS.${row.original["incidentType"].toUpperCase()}`));
        },
      },
      {
        Header: t("CS_TICKET_SUB_TYPE"),
        Cell: ({ row }) => {
          return GetCell(t(`SERVICEDEFS.${row.original["incidentSubType"].toUpperCase()}`));
        },
      },
      {
        Header: t("CS_TICKET_DETAILS_CURRENT_STATUS"),
        Cell: ({ row }) => {
          return GetCell(t(`CS_COMMON_${row.original["status"]}`));
        },
      },
      {
        Header: t("CS_COMPLAINT_PHC_TYPE"),
        Cell: ({ row }) => {
          return GetCell(t(`TENANT_TENANTS_${row.original["tenantId"].toUpperCase().replace(".","_")}`));
        },
      },
      {
        Header: t("WF_INBOX_HEADER_CURRENT_OWNER"),
        Cell: ({ row }) => {
          return GetCell(row.original["taskOwner"]);
        },
      },
      {
        Header: t("WF_INBOX_HEADER_SLA_DAYS_REMAINING"),
        Cell: ({ row }) => {
          return GetSlaCell(row.original["sla"]);
        },
      },
    ],
    [t]
  );

  let result;
  if (isLoading) {
    result = <Loader />;
  } else if (data && data.combinedRes.length === 0) {
    result = (
      <Card style={{ marginTop: 20 }}>
       <div style={{color:"#7a2824", marginTop:isIpadView? "210px":""}}> {t(LOCALE.NO_COMPLAINTS_EMPLOYEE)
          .split("\\n")
          .map((text, index) => (
            <p key={index} style={{ textAlign: "center" }}>
              {text}
            </p>
          ))}
          </div>
      </Card>
    );
  } else if (data?.combinedRes?.length > 0) {
    result = (
      <ComplaintTable
        t={t}
        data={data?.combinedRes}
        columns={columns}
        getCellProps={(cellInfo) => {
          return {
            style: {
              //minWidth: cellInfo.column.Header === t("CS_COMMON_TICKET_NO") ? "100px" : "",
              maxWidth:"100%",
              padding: "17.24px 18px",
              fontSize: "15px",
            },
          };
        }}
        onNextPage={onNextPage}
        onPrevPage={onPrevPage}
        totalRecords={totalRecords}
        onPageSizeChange={onPageSizeChange}
        currentPage={currentPage}
        pageSizeLimit={pageSizeLimit}
      />
    );
  } else {
    result = (
      <Card style={{ marginTop: 20 }}>
        {t(LOCALE.ERROR_LOADING_RESULTS)
          .split("\\n")
          .map((text, index) => (
            <p key={index} style={{ textAlign: "center" }}>
              {text}
            </p>
          ))}
      </Card>
    );
  }

  return (
    <div className="inbox-container">
      <div className="filters-container">
        <ComplaintsLink />
        <div style={{paddingTop:"5px", paddingBottom:"0px"}}>
          <Filter complaints={data} onFilterChange={onFilterChange} type="desktop" searchParams={searchParams} />
        </div>
      </div>
      <div style={{ flex: 1, overflowX:"scroll", width:"100%" }}>
        <SearchComplaint onSearch={onSearch} type="desktop" />
        <div style={{ marginTop: "21px", marginLeft:"24px", flex: 1 }}>{result}</div>
      </div>
    </div>
  );
};

export default DesktopInbox;