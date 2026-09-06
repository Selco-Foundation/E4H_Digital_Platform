import { translateOr, useTranslate } from "@/shared";
import { Card } from "@/ui";
import { Package } from "lucide-react";
import { useState } from "react";
import type { EndUserAsset } from "../hooks/use-end-user-assets";

interface EndUserAssetsListProps {
  assets: EndUserAsset[];
  isLoading: boolean;
}

function assetSubtitle(asset: EndUserAsset) {
  const category = asset.modelNumber ?? asset.assetTypeId;
  const serial = asset.serialNumber ? `#${asset.serialNumber}` : undefined;
  return [category, serial].filter(Boolean).join(" • ");
}

function AssetThumbnail({ asset, name }: { asset: EndUserAsset; name: string }) {
  const [failed, setFailed] = useState(false);

  if (asset.imageUrl && !failed) {
    return (
      <img
        src={asset.imageUrl}
        alt={name}
        className="h-14 w-14 shrink-0 rounded-lg object-cover lg:h-10 lg:w-10"
        onError={() => setFailed(true)}
      />
    );
  }

  return (
    <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-lg bg-muted text-muted-foreground lg:h-10 lg:w-10">
      <Package className="h-6 w-6 lg:h-5 lg:w-5" />
    </div>
  );
}

function AssetBlock({ asset }: { asset: EndUserAsset }) {
  const { t } = useTranslate();
  const name = translateOr(t, `ASSETTYPE_${asset.assetTypeId}`, asset.name);
  const subtitle = assetSubtitle(asset);

  return (
    <Card className="livelihood-card w-full flex-row items-center gap-3 border-border p-3 shadow-none lg:max-w-fit">
      <AssetThumbnail asset={asset} name={name} />
      <div className="min-w-0 flex-1">
        <p className="truncate text-base leading-5 font-semibold text-ink-950 lg:text-sm">
          {name}
        </p>
        {subtitle ? (
          <p className="truncate text-sm leading-4 text-ink-600 lg:text-xs">{subtitle}</p>
        ) : null}
      </div>
    </Card>
  );
}

export function EndUserAssetsList({ assets, isLoading }: EndUserAssetsListProps) {
  const { t } = useTranslate();

  return (
    <div className="space-y-3">
      <h3 className="text-[20px] leading-[30px] font-semibold text-ink-950">
        {translateOr(t, "ES_IM_MY_REGISTERED_ASSETS", "My Registered Assets")}
      </h3>
      <div className="flex flex-col items-stretch gap-3 lg:rounded-lg lg:border lg:border-border lg:bg-card lg:p-4 lg:py-5 lg:shadow-sm">
        {isLoading ? (
          <p className="text-sm text-muted-foreground">
            {translateOr(t, "CS_COMMON_LOADING", "Loading...")}
          </p>
        ) : assets.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            {translateOr(t, "ES_IM_NO_ASSETS_FOUND", "No assets found")}
          </p>
        ) : (
          <div className="flex flex-col gap-3 lg:flex-row lg:flex-wrap">
            {assets.map((asset) => (
              <AssetBlock key={asset.assetId} asset={asset} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
