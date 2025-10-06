import React, { useEffect, useState } from "react";
import useOrganizationUser from "../../hooks/useOrganizationUser";
import CustomDropdown from "../Custom/CustomDropdown";

const OrganizationUserDropdown = ({ t, organizationIds = [], selected, onSelect, style = {} }) => {

  const [userOptions, setUserOptions] = useState([]);

  const { data: organizationUserData } = useOrganizationUser({
    organizationIds,
  });

  useEffect(() => {
    if (organizationUserData) {
      setUserOptions(
        organizationUserData.organizationUsers.map((organizationUser) => ({
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