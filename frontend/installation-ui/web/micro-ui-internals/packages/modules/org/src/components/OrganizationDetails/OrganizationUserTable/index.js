import React, {useEffect, useState} from 'react';
import { Loader, Table, Button, Toast } from "@egovernments/digit-ui-react-components";
import useOrganizationUser from "../../../hooks/useOrganizationUser";
import OrganizationUserAdminActions from "./OrganizationUserAdminActions";

const OrganizationUserTable = ({ t, organizationId, organizationType }) => {

  const [fetchedData, setFetchedData] = useState([]);
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);

  const { isLoading: organizationUserDataLoading, data: organizationUserData } = useOrganizationUser(
    {organizationIds: [organizationId]}, pageSize, pageOffset
  );

  useEffect(() => {
    if (organizationUserData) {
      setFetchedData(organizationUserData?.organizationUsers || []);
    }
  }, [organizationUserData]);

  const GetCell = (value) => (
    <span className="cell-text" style={{ color: "#000000" }}>
      {value}
    </span>
  );

  const GetRoleList = (roles) => (
    <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
      {roles?.map((role) => (
        <span
          key={role}
          style={{
            backgroundColor: "#F1FFF8",
            color: "#00703C",
            width: "fit-content",
            padding: "5px 10px",
          }}
        >
          {role}
        </span>
      ))}
    </div>
  );

  const columns = [
    {
      Header: t("ORG_USER_NAME"),
      Cell: ({ row }) => {
        return GetCell(row.original["name"] ? row.original["name"] : "-");
      },
    },
    {
      Header: t("ORG_USER_CONTACT"),
      Cell: ({ row }) => {
        return GetCell(row.original["mobileNumber"] ? row.original["mobileNumber"] : "-");
      },
    },
    {
      Header: t("ORG_USER_EMAIL"),
      Cell: ({ row }) => {
        return GetCell(row.original["emailId"] ? row.original["emailId"] : "-");
      },
    },
    {
      Header: t("ORG_USER_ROLES"),
      Cell: ({ row }) => {
        return GetCell(row.original["roles"]?.length ? GetRoleList(row.original["roles"].map((role) => role.name)) : "-");
      },
    },
    {
      Header: t("ORG_USER_ACTIONS"),
      Cell: ({ row }) => {
        return GetCell(row.original["activityEndDate"] ? row.original["activityEndDate"] : "-");
      },
    }
  ];

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  };

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  };

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  };

  const renderFacilities = () => {
    if (organizationUserDataLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%", minHeight: "300px" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>{t("CS_NO_ORG_USERS_FOUND")}</div>
        </div>
      );
    }

    return (
      <div
        style={{
          backgroundColor: "white",
          padding: "15px 0px 0px 0px",
        }}
      >
        <div
          className={"health-facility-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            customTableWrapperClassName={"org-users-table"}
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
            totalRecords={organizationUserData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    );
  };

  return (
    <div>
      <OrganizationUserAdminActions t={t} organizationId={organizationId} organizationType={organizationType} />
      {renderFacilities()}
    </div>
  );
};

export default OrganizationUserTable;