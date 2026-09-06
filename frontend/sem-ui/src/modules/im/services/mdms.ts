import { fetchMdmsMasters, tenantId, translateOr, type AuthUser } from "@/shared";
import type { ComplaintTypeOption } from "../types/inbox";
import { SelectOption } from "../types/create-incident";

interface ServiceDef {
  deprecated?: boolean;
  menuPath?: string;
  serviceCode?: string;
}

interface ItemCode {
  code?: string;
  category?: string;
  active?: boolean;
}

export async function fetchAssetTypes(
  accessToken: string,
  user: AuthUser | null | undefined,
): Promise<SelectOption[]> {
  const stateTenantId = tenantId();
  const masters = await fetchMdmsMasters(
    stateTenantId,
    "livelihood",
    ["ItemCode"],
    accessToken,
    user,
  );
  const items = (masters.ItemCode as ItemCode[]) ?? [];
  const categories = new Set<string>();

  for (const item of items) {
    if (item.active === false || !item.category) {
      continue;
    }
    categories.add(item.category);
  }

  return [...categories]
    .sort((a, b) => a.localeCompare(b))
    .map((category) => ({ code: category, name: category }));
}

export async function fetchServiceDefsForMenuPath(
  accessToken: string,
  user: AuthUser | null | undefined,
  menuPath: string,
  t: (key: string) => string,
): Promise<ComplaintTypeOption[]> {
  const stateTenantId = tenantId();
  const masters = await fetchMdmsMasters(
    stateTenantId,
    "Incident",
    ["ServiceDefs"],
    accessToken,
    user,
  );
  const serviceDefs = (masters.ServiceDefs as ServiceDef[]) ?? [];

  return serviceDefs
    .filter((def) => !def.deprecated && def.menuPath === menuPath)
    .map((def) => ({
      key: def.serviceCode ?? "",
      serviceCode: def.serviceCode,
      menuPath: def.menuPath,
      name: translateOr(
        t,
        `SERVICEDEFS.${(def.serviceCode ?? "").toUpperCase()}`,
        def.serviceCode ?? "",
      ),
    }))
    .filter((item) => item.key)
    .sort((a, b) => a.name.localeCompare(b.name));
}
