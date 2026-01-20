import {useQuery, useQueryClient} from "react-query";
import { VendorService } from "../services/Vendor";

const formatOrganizationUsers = (organizationUsers) => {
  return organizationUsers.map((organizationUser) => ({
    ...organizationUser.user,
    roles: (organizationUser.user.roles || []).filter((role) => (role.code && role.name)),
    organizationId: organizationUser.organizationId,
  }))
}

const fetchOrganizationUsers = async (filter, limit, offset) => {
  const response = await VendorService.fetchOrganizationUsers(filter, limit, offset);
  return {
    organizationUsers: formatOrganizationUsers(response?.OrgUsers),
    totalCount: response.TotalCount,
  };
}

const useOrganizationUser = (queryFilter = {}, limit = 1000, offset = 0) => {

  const { tenantId, organizationIds } = queryFilter;

  const filter = {
    OrgUser: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  }

  if (tenantId) {
    filter.OrgUser.tenantId = tenantId;
  }

  if (organizationIds) {
    filter.OrgUser.organizationIds = organizationIds;
  }

  const queryClient = useQueryClient();
  const {isLoading, isError, error, data} = useQuery(
    ["ORGANISATION_USER", filter, limit, offset],
    () => fetchOrganizationUsers(filter, limit, offset),
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ORGANISATION_USER"])
  }
}

export default useOrganizationUser;