import { tenantId, useAuthStore } from "@/shared";
import { useQuery } from "@tanstack/react-query";
import { searchPausedFacilities } from "../services/rms";
import type { PausedFacilityFilters } from "../types/rms";

export function useRmsPausedFacilities(
  filters: PausedFacilityFilters,
  limit: number,
  offset: number,
) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const stateTenantId = tenantId();

  return useQuery({
    queryKey: ["rms-paused-facilities", stateTenantId, filters, limit, offset],
    enabled: Boolean(accessToken),
    queryFn: () =>
      searchPausedFacilities(filters, stateTenantId, limit, offset, accessToken!, user),
  });
}
