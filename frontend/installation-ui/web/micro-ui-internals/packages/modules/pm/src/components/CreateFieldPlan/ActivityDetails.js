import React, { useEffect, useMemo, useState } from "react";
import FormattedDateInput from "../Custom/FormattedDateInput";
import { Dropdown, SubmitBar, Table, TextInput } from "@egovernments/digit-ui-react-components";
import { CheckCircleOutline } from "@egovernments/digit-ui-svg-components";
import CustomCloseSvg from "../Custom/CustomCloseSvg";

const ActivityDetails = ({ setError, setValue, clearErrors, props }) => {

  const { t } = props;
  const [data, setData] = useState([]);
  const [organizationOptions, setOrganizationOptions] = useState([]);
  const [roleOptions, setRoleOptions] = useState([]);

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${year}-${month}-${day}`;
  };

  useEffect(() => {
    setData([
      {
        activity: "Installation",
        users: [
          {
            startDate: {
              value: formatDate(1886889600000),
              error: "",
            },
            endDate: {
              value: formatDate(1886889600000),
              error: "",
            },
            poNumber: {
              value: "1234567890",
              error: "",
            },
            organization: {
              value: {
                code: "Selco",
                name: "Selco",
              },
              error: "",
            },
            role: {
              value: {
                code: "SPOC",
                name: "SPOC",
              },
              error: "",
            },
            email: {
              value: "abcd@abcd.com",
              error: "",
            },
            isEmailSent: false,
          }
        ]
      },
      {
        activity: "Assessment",
        users: [
          {
            startDate: {
              value: formatDate(1886889600000),
              error: "",
            },
            endDate: {
              value: formatDate(1886889600000),
              error: "",
            },
            poNumber: {
              value: "1234567890",
              error: "",
            },
            organization: {
              value: {
                code: "Selco",
                name: "Selco",
              },
              error: "",
            },
            role: {
              value: {
                code: "SPOC",
                name: "SPOC",
              },
              error: "",
            },
            email: {
              value: "abcd@abcd.com",
              error: "",
            },
            isEmailSent: true,
          }
        ]
      }
    ])

    setOrganizationOptions([
      {
        code: "Selco",
        name: "Selco",
      },
      {
        code: "Beehyv",
        name: "Beehyv",
      }
    ])

    setRoleOptions([
      {
        code: "SPOC",
        name: "SPOC",
      },
      {
        code: "Reviewer",
        name: "Reviewer",
      }
    ])
  }, []);

  const addUserEntry = (activity) => {
    setData((prevState) => prevState?.map((dataEntry) => {
      if (dataEntry.activity !== activity) return dataEntry;

      return  {
        ...dataEntry,
        users: [
          ...dataEntry.users,
          {
            startDate: { value: "", error: "", },
            endDate: { value: "", error: "", },
            poNumber: { value: "", error: "", },
            organization: { value: null, error: "", },
            role: { value: null, error: "", },
            email: { value: "", error: "", },
            isEmailSent: false,
          }
        ],
      };
    }));
  }

  const removeUserEntry = (activity, index) => {
    setData((prevState) => prevState?.map((dataEntry) => {
      if (dataEntry.activity !== activity) return dataEntry;
      return {
        ...dataEntry,
        users: dataEntry.users.filter((useEntry, i) => i !== index),
      }
    }))
  }

  const handleUserDataChange = (activity, index, fieldName, fieldValue) => {

    setData((prevState) => prevState?.map((dataEntry) => {
      if (dataEntry.activity !== activity) return dataEntry;

      return  {
        ...dataEntry,
        users: dataEntry.users.map((userEntry, i) => {
          if (i !== index) return userEntry;

          return {
            ...userEntry,
            [fieldName] : {
              value: fieldValue,
              error: "",
            }
          };
        }),
      };
    }));
  }

  const GetHead = (value) => (
    <div style={{ height: "38px", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <span>{value}</span>
    </div>
  );

  const GetCell = (value) => (
    <span style={{ fontSize: "16px", fontWeight: "400", fontFamily: "Roboto", color: "#363636" }}>
      {value}
    </span>
  );

  const ActivityCell = (activity) => (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
      }}
    >
      <span
        style={{
          fontSize: "16px",
          fontWeight: "400",
          fontFamily: "Roboto",
          color: "#505A5F",
          lineHeight: "24px",
        }}
      >
        {activity}
      </span>
      <button
        type="button"
        className={"jk-digit-secondary-btn"}
        style={{
          display: "flex",
          gap: "5px",
          alignItems: "center",
          width: "fit-content",
          height: "fit-content",
          padding: "0px 20px",
          border: "none"
        }}
        onClick={() => addUserEntry(activity)}
      >
          <span
            style={{
              width: "12px",
              height: "12px",
              borderRadius: "2px",
              background: "#C84C0E",
              color: "white",
              fontSize: "10px",
              fontWeight: "bold",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              textAlign: "center"
            }}
          >
            +
          </span>
        <span
          style={{
            fontSize: "13px",
            fontWeight: "500",
            fontFamily: "Roboto",
            color: "#C84C0E"
          }}
        >
            {t("CORE_COMMON_ADD_NEW")}
          </span>
      </button>
    </div>
  )

  const UserDateInput = (activity, index, fieldName, fieldValue, isLast) => (
    <div
      style={{
        padding: "21px 20px 6px 20px",
        borderBottom: isLast ? "none" : "1px solid #EEEEEE",
      }}
    >
      <FormattedDateInput
        value={fieldValue.value}
        onChange={(e) => handleUserDataChange(activity, index, fieldName, e.target.value)}
        className={"employee-card-input"}
        style={{
          minWidth: "190px",
        }}
      />
      <span
        style={{
          fontSize: "14px",
          color: "rgba(212, 53, 28)",
          height: "14px",
          marginTop: "1px",
          display: "block"
        }}
      >
        {fieldValue.error}
      </span>
    </div>
  )

  const UserTextInput = (activity, index, fieldName, fieldValue, isLast) => (
    <div
      style={{
        padding: "21px 20px 6px 20px",
        borderBottom: isLast ? "none" : "1px solid #EEEEEE",
      }}
    >
      <TextInput
        value={fieldValue.value}
        onChange={(e) => handleUserDataChange(activity, index, fieldName, e.target.value)}
      />
      <span
        style={{
          fontSize: "14px",
          color: "rgba(212, 53, 28)",
          height: "14px",
          marginTop: "1px",
          display: "block"
        }}
      >
        {fieldValue.error}
      </span>
    </div>
  )

  const UserDropDown = (options, optionKey = "name", activity, index, fieldName, fieldValue, isLast) => (
    <div
      style={{
        padding: "21px 20px 6px 20px",
        borderBottom: isLast ? "none" : "1px solid #EEEEEE",
      }}
    >
      <Dropdown
        t={t}
        option={options}
        optionKey={optionKey}
        selected={fieldValue.value}
        select={(option) => handleUserDataChange(activity, index, fieldName, option)}
      />
      <span
        style={{
          fontSize: "14px",
          color: "rgba(212, 53, 28)",
          height: "14px",
          marginTop: "1px",
          display: "block"
        }}
      >
        {fieldValue.error}
      </span>
    </div>
  )

  const UserEmailSentCheck = (isSent, activity, index, isLast, disableRemoval) => (
    <div
      style={{
        padding: "21px 20px 6px 20px",
        borderBottom: isLast ? "none" : "1px solid #EEEEEE",
        position: "relative",
      }}
    >
      <div
        className={"employee-card-input"}
        style={{
          border: "none",
          padding: 0,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <CheckCircleOutline fill={isSent ? "#00703C" : "#505A5F"} />
      </div>
      <span
        style={{
          fontSize: "14px",
          color: "rgba(212, 53, 28)",
          height: "14px",
          marginTop: "1px",
          display: "block"
        }}
      >

      </span>
      <button
        type={"button"}
        disabled={disableRemoval}
        style={{
          cursor: disableRemoval ? "default" : "pointer",
          position: "absolute",
          top: "1px",
          right: "1px",
          backgroundColor: "transparent",
          display: "flex",
          alignItems: "center",
          padding: "0",
          borderRadius: "3px",
          opacity: disableRemoval ? "0.5" : "1",
        }}
        onClick={() => removeUserEntry(activity, index)}
      >
        <CustomCloseSvg fill={"transparent"} height={"20px"} width={"20px"} />
      </button>
    </div>
  );

  const columns = useMemo(
    () => [
      {
        id: "activity",
        Header: () => GetHead("Activity"),
        Cell: ({ row }) => (
          ActivityCell(row.original["activity"])
        ),
      },
      {
        id: "startDate",
        Header: () => GetHead("Start Date"),
        Cell: ({ row }) => GetCell(
          row.original["users"]?.map((userEntry, i, usersArray) => {
            return UserDateInput(row.original["activity"], i, "startDate", userEntry?.startDate, usersArray.length - 1 === i)
          })
        ),
      },
      {
        id: "endDate",
        Header: () => GetHead("End Date"),
        Cell: ({ row }) => GetCell(
          row.original["users"]?.map((userEntry, i, usersArray) => {
            return UserDateInput(row.original["activity"], i, "endDate", userEntry?.endDate, usersArray.length - 1 === i);
          })
        ),
      },
      {
        id: "poNumber",
        Header: () => GetHead("PO Number"),
        Cell: ({ row }) => GetCell(
          row.original["users"]?.map((userEntry, i, usersArray) => {
            return UserTextInput(row.original["activity"], i, "poNumber", userEntry?.poNumber, usersArray.length - 1 === i);
          })
        ),
      },
      {
        id: "organization",
        Header: () => GetHead("Organization"),
        Cell: ({ row }) => GetCell(
          row.original["users"]?.map((userEntry, i, usersArray) => {
            return UserDropDown(organizationOptions, "name", row.original["activity"], i, "organization", userEntry?.organization, usersArray.length - 1 === i);
          })
        ),
      },
      {
        id: "role",
        Header: () => GetHead("Role"),
        Cell: ({ row }) => GetCell(
          row.original["users"]?.map((userEntry, i, usersArray) => {
            return UserDropDown(roleOptions, "name", row.original["activity"], i, "role", userEntry?.role, usersArray.length - 1 === i);
          })
        ),
      },
      {
        id: "email",
        Header: () => GetHead("Activity SPOC Email"),
        Cell: ({ row }) => GetCell(
          row.original["users"].map((userEntry, i, usersArray) => {
            return UserTextInput(row.original["activity"], i, "email", userEntry?.email, usersArray.length - 1 === i);
          })
        ),
      },
      {
        id: "emailSent",
        Header: () => GetHead(""),
        Cell: ({ row }) => GetCell(
          row.original["users"].map((userEntry, i, usersArray) => {
            return UserEmailSentCheck(userEntry.isEmailSent, row.original["activity"], i, usersArray.length - 1 === i, usersArray.length === 1);
          })
        ),
      },
    ],
    [organizationOptions, roleOptions]
  );

  const handleActivityDataClear = () => {
    setData((prevState) => prevState.map((dataEntry) => ({
      ...dataEntry,
      users: [
        {
          startDate: { value: "", error: "", },
          endDate: { value: "", error: "", },
          poNumber: { value: "", error: "", },
          organization: { value: null, error: "", },
          role: { value: null, error: "", },
          email: { value: "", error: "", },
          isEmailSent: false,
        }
      ]
    })))
  }

  const handleActivityDataSave = (activityData) => {
    let faultyData = false;
    const validatedData = activityData.map((dataEntry, i) => ({
      ...dataEntry,
      users: dataEntry.users.map((userEntry) => {
        const newUserEntry = {}

        Object.keys(userEntry).forEach((key) => {
          if (key === "isEmailSent") {
            newUserEntry[key] =  userEntry[key];
          }
          else if (!userEntry[key].value) {
            faultyData = true;
            newUserEntry[key] = {
              ...userEntry[key],
              error: t("CORE_COMMON_REQUIRED")
            };
          } else {
            newUserEntry[key] =  userEntry[key];
          }
        })

        return newUserEntry;
      })
    }))

    console.debug("faultyData", faultyData);
    if (faultyData) {
      setData(validatedData);
    } else {
      console.debug("data", data);
    }
  }

  return (
    <div>
      <Table
        t={t}
        data={data}
        columns={columns}
        customTableWrapperClassName={"activity-details-table"}
        getCellProps={() => {
          return {
            style: {
              height: "70px",
              minHeight: "fit-content",
              padding: 0,
            }
          };
        }}
        isPaginationRequired={false}
        styles={{minWidth: "300px", overflow: "auto"}}
      />
      <div
        style={{
          padding: "20px 0px",
          display: "flex",
          justifyContent: "end",
          gap: "10px",
        }}
      >
        <button
          type="button"
          onClick={handleActivityDataClear}
          style={{
            border: "none",
            background: "transparent",
            color: "#C84C0E",
            fontSize: "16px",
            fontFamily: "Roboto",
            fontWeight: 400,
            cursor: "pointer",
          }}
        >
          {t("CORE_COMMON_CLEAR")}
        </button>
        <SubmitBar
          label={t("CORE_COMMON_SAVE")}
          style={{
            width: "220px",
            maxWidth: "50%",
          }}
          onSubmit={() => handleActivityDataSave(data)}
        />
      </div>
    </div>
  );
}

export default ActivityDetails;