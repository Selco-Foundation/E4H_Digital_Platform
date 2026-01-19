import React from 'react';
import useOrganizationUser from "../../../hooks/useOrganizationUser";

const OrganizationUserTable = ({ t, organizationId }) => {

  const { isLoading: organizationUserDataLoading, data: organizationUser } = useOrganizationUser({organizationIds: [organizationId]});

  return (
    <div>
      TABLE
    </div>
  );
};

export default OrganizationUserTable;