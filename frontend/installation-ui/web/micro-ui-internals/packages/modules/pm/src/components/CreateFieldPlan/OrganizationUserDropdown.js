import React, { useEffect, useState } from "react";
import { Dropdown } from "@egovernments/digit-ui-react-components";
import useOrganizationUser from "../../hooks/useOrganizationUser";

const OrganizationUserDropdown = ({ t, organizationIds = [], selected = {}, onSelect, style = {} }) => {

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
    <Dropdown
      t={t}
      option={userOptions}
      optionKey={"emailKey"}
      selected={{
        ...selected,
        emailKey: selected.emailId,
      }}
      select={onSelect}
      style={{
        ...style
      }}
    />
  )
}

export default OrganizationUserDropdown;