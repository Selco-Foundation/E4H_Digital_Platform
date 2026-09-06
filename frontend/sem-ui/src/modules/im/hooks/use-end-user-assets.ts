import { useAuthStore, useJurisdictionStore } from "@/shared";
import { useQuery } from "@tanstack/react-query";
import { searchAssetsForFacility } from "../services/asset-search";
import { searchFacilitiesByJurisdiction } from "../services/facility-search";
import { fetchFileUrls, getOriginalFileUrl } from "../services/incident-details";
import type { LivelihoodAsset } from "../types/facility-asset";
import { buildFacilitySearchCriteria } from "../utils/jurisdiction-facility-criteria";

export interface EndUserAsset extends LivelihoodAsset {
  imageUrl?: string;
}

interface UseEndUserAssetsOptions {
  enabled: boolean;
}

export function useEndUserAssets({ enabled }: UseEndUserAssetsOptions) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const boundaries = useJurisdictionStore((state) => state.boundaries);

  const query = useQuery({
    queryKey: ["im-end-user-assets", employeeTenantId, boundaries],
    enabled: Boolean(enabled && accessToken && employeeTenantId),
    staleTime: 30_000,
    queryFn: async () => {
      const criteria = buildFacilitySearchCriteria(boundaries, employeeTenantId!);
      const { facilities } = await searchFacilitiesByJurisdiction(
        criteria,
        accessToken!,
        user,
      );

      const facility = facilities[0];
      if (!facility) {
        return [] as EndUserAsset[];
      }

      const assets = await searchAssetsForFacility(
        facility.facilityId,
        employeeTenantId!,
        accessToken!,
        user,
      );

      const fileStoreIds = Array.from(
        new Set(
          assets
            .map((asset) => asset.documentFileStoreId)
            .filter((id): id is string => Boolean(id)),
        ),
      );

      if (!fileStoreIds.length) {
        return assets;
      }

      const fileUrlResponse = await fetchFileUrls(
        fileStoreIds,
        employeeTenantId!,
        accessToken!,
        user,
      );
      const imageUrlById = new Map(
        (fileUrlResponse.fileStoreIds ?? [])
          .filter((entry): entry is { id: string; url: string } =>
            Boolean(entry.id && entry.url),
          )
          .map((entry) => [entry.id, getOriginalFileUrl(entry.url)]),
      );

      return assets.map((asset) => ({
        ...asset,
        imageUrl: asset.documentFileStoreId
          ? imageUrlById.get(asset.documentFileStoreId)
          : undefined,
      }));
    },
  });

  return {
    assets: query.data ?? [],
    isLoading: query.isLoading,
  };
}
