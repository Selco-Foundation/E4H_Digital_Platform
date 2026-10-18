import { useQuery } from "@tanstack/react-query";
import { fetchFacilities } from "../api/facility";
import { useAuthStore } from "../stores/auth-store";

export function useFacility(boundaryCodes: string[]) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const stableCodes = boundaryCodes.filter(Boolean).sort().join(",");

  return useQuery({
    queryKey: ["facility", employeeTenantId, stableCodes],
    enabled: Boolean(accessToken && employeeTenantId) && boundaryCodes.length > 0,
    queryFn: () => fetchFacilities(boundaryCodes, employeeTenantId!, accessToken!, user),
  });
}
