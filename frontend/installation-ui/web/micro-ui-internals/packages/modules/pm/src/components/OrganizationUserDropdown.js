import React, { useEffect, useState } from "react";
import useOrganizationUser from "../hooks/useOrganizationUser";
import CustomDropdown from "./Custom/CustomDropdown";

const OrganizationUserDropdown = ({ t, organizationIds = [], selected, onSelect, style = {}, roleCode = "" }) => {

  const [userOptions, setUserOptions] = useState([]);

  const { data: organizationUserData } = useOrganizationUser({
    organizationIds,
  });

  useEffect(() => {
    if (roleCode) {
      setUserOptions((prevState) => (
        prevState.filter((user) => user.roles.some((role) => role.code === roleCode))
      ))
      if (selected && selected.roles.every((role) => role.code !== roleCode)) {
        onSelect(null);
      }
    }
  }, [roleCode]);

  useEffect(() => {
    if (organizationUserData) {
      setUserOptions(
        organizationUserData.organizationUsers
          .filter((organizationUser) => (!roleCode ? true : organizationUser.roles.some((role) => role.code === roleCode)))
          .map((organizationUser) => ({
          ...organizationUser,
          emailKey: `${organizationUser.name} [${organizationUser.emailId}]`
        }))
      );
    }
  }, [organizationUserData])

  return (
    <CustomDropdown
      t={t}
      option={userOptions}
      optionKey={"emailKey"}
      selected={ selected ? { ...selected, emailKey: selected.emailId, } : null }
      select={onSelect}
      style={{
        ...style
      }}
    />
  )
}

export default OrganizationUserDropdown;