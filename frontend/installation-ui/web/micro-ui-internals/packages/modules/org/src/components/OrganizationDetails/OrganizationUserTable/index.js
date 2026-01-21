import React, {useEffect, useState} from 'react';
import { Loader, Table, Toast } from "@egovernments/digit-ui-react-components";
import useOrganizationUser from "../../../hooks/useOrganizationUser";
import OrganizationUserAdminActions from "./OrganizationUserAdminActions";
import CustomEditIcon from "../../Custom/CustomEditIcon";
import CustomDustbinIcon from "../../Custom/CustomDustbinIcon";
import ConfirmationAlert from "./ConfirmationAlert";
import {useQueryClient} from "react-query";
import UserModal from "./UserModal";
import {VendorService} from "../../../services/Vendor";

const OrganizationUserTable = ({ t, organizationId, organizationType }) => {

  const [fetchedData, setFetchedData] = useState([]);
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);
  const [alert, setAlert] = useState(null);
  const [toast, setToast] = useState(null);
  const [formToast, setFormToast] =  useState(null);
  const [userToEdit, setUserToEdit] = useState(null);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [blockUI, setBlockUI] = useState(null);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (toast) {
      setTimeout(() => {
        setToast(null);
      }, 2500);
    }
  }, [toast]);

  useEffect(() => {
    if (formToast) {
      setTimeout(() => {
        setFormToast(null);
      }, 2500);
    }
  }, [formToast]);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { isLoading: organizationUserDataLoading, data: organizationUserData } = useOrganizationUser(
    {organizationIds: [organizationId]}, pageSize, pageOffset
  );

  useEffect(() => {
    if (organizationUserData) {
      setFetchedData(organizationUserData?.organizationUsers || []);
    }
  }, [organizationUserData]);

  const handleUserDeletion = async (orgUserId, userUUID) => {
    try {
      setBlockUI(true);
      const payload = {
        id: orgUserId,
        userId: userUUID,
        organizationId: organizationId,
      }
      await VendorService.deleteOrganizationUser(payload);
      await queryClient.invalidateQueries(["ORGANISATION_USER"]);

      setBlockUI(false);
      setToast({
        key: "success",
        label: t("ORGANIZATION_USER_DELETION_SUCCESS"),
      })
    } catch (e) {
      console.error("Failed to delete user", e);
      setBlockUI(false);
      setToast({
        key: "error",
        label: t("ORGANIZATION_USER_DELETION_FAILED"),
      });
    }
  }

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
        return GetCell(
          <div style={{ display: "flex", gap: "5px" }}>
            <button
              type="button"
              style={{
                paddingTop: "3px",
                backgroundColor: "white",
              }}
              onClick={() => setUserToEdit(row.original)}
            >
              <CustomEditIcon width={"18"} height={"18"} viewBox={"0 0 20 20"} />
            </button>
            <button
              type="button"
              style={{
                background: "none"
              }}
              onClick={() => {
                setAlert({
                  continueAction: () => handleUserDeletion(row.original["orgUserId"], row.original["uuid"]),
                  message: t("DELETE_ORG_USER_CONFIRMATION_MSG"),
                })
              }}
            >
              <CustomDustbinIcon colourFill={"#bc210a"} />
            </button>
          </div>
        );
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

  const renderOrganizationUsers = () => {
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

  const handleOrganizationUserEdit = async (createdUser, userFormData, jurisdictions) => {
    try {
      setBlockUI(true);
      const organizationUser = {
        organizationId: organizationId,
        id: createdUser.orgUserId,
        user: {
          name: userFormData.name,
          userName: userFormData.userName,
          mobileNumber: userFormData.contact,
          emailId: userFormData.email,
          tenantId: Digit.ULBService.getCurrentTenantId(),
          roles: userFormData.roles.map(role => ({...role, tenantId: Digit.ULBService.getCurrentTenantId()})),
          jurisdiction: jurisdictions
        }
      }
      await VendorService.editOrganizationUser(organizationUser)
      await queryClient.invalidateQueries(["ORGANISATION_USER"]);

      setBlockUI(false);
      setUserToEdit(null);
      setToast({
        key: "success",
        label: t("ORGANIZATION_USER_UPDATION_SUCCESS"),
      })
    } catch (e) {
      console.error("Failed to create facility", e);
      setBlockUI(false);
      setFormToast({
        key: "error",
        label: t("ORGANIZATION_USER_UPDATION_FAILED"),
      });
    }
  }

  return (
    <div>
      {blockUI && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000005,
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
      <OrganizationUserAdminActions t={t} organizationId={organizationId} organizationType={organizationType} />
      {renderOrganizationUsers()}
      {!!alert && (
        <ConfirmationAlert t={t} alert={alert} setAlert={setAlert} />
      )}
      {userToEdit && (
        <UserModal
          t={t}
          title={"EDIT_USER"}
          onClose={() => setUserToEdit(null)}
          onSubmit={(formData, jurisdictions) => handleOrganizationUserEdit(userToEdit, formData, jurisdictions)}
          organizationType={organizationType}
          createdUser={userToEdit}
          formToast={formToast}
          setFormToast={setFormToast}
        />
      )}
      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          style={{
            zIndex: 100000000,
            ...(toast.key === "error" ? { backgroundColor: "#B91900" } : {}),
            ...(mobileView ? { bottom: "120px" } : {}),
          }}
          label={toast.label}
          isDleteBtn={true}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
};

export default OrganizationUserTable;