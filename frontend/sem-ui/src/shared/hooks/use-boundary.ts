import { useQuery } from "@tanstack/react-query";
import { fetchBoundaryRelations } from "../api/boundary";
import { useAuthStore } from "../stores/auth-store";

export function useBoundary(codes: string[]) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const stableCodes = codes.filter(Boolean).sort().join(",");

  return useQuery({
    queryKey: ["boundary", stableCodes],
    enabled: Boolean(accessToken) && codes.length > 0,
    queryFn: () => fetchBoundaryRelations(codes, accessToken!, user),
  });
}
