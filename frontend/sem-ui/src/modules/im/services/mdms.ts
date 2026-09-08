import { fetchMdmsMasters, tenantId, translateOr, type AuthUser } from "@/shared";
import type { ComplaintTypeOption } from "../types/inbox";
import { SelectOption } from "../types/create-incident";

interface ServiceDef {
  deprecated?: boolean;
  menuPath?: string;
  serviceCode?: string;
}

interface SystemFunctionalityDef {
  code?: string;
  name?: string;
  active?: boolean;
}

interface FacilityTypeDef {
  code?: string;
  name?: string;
  active?: boolean;
}

/**
 * The inbox "Asset Type" filter is actually the facility's own type
 * (`incident.phcSubType`, backend field `phcSubType`, sourced from the
 * facility's `facilityType`) — sourced from MDMS `facility.FacilityType`.
 */
export async function fetchAssetTypes(
  accessToken: string,
  user: AuthUser | null | undefined,
): Promise<SelectOption[]> {
  const stateTenantId = tenantId();
  const masters = await fetchMdmsMasters(
    stateTenantId,
    "facility",
    ["FacilityType"],
    accessToken,
    user,
  );
  const items = (masters.FacilityType as FacilityTypeDef[]) ?? [];

  return items
    .filter((item) => item.active !== false && item.code)
    .map((item) => ({ code: item.code!, name: item.name ?? item.code! }))
    .sort((a, b) => a.name.localeCompare(b.name));
}

/**
 * "Ticket Type" dropdown (DIGIT-UI's im/CreateComplaint: `GetServiceDefinitions.getMenu`) —
 * one option per distinct, non-deprecated `menuPath` on the Incident.ServiceDefs master.
 * Selecting one of these then filters fetchServiceDefsForMenuPath's list (Ticket Subtype).
 */
export async function fetchTicketTypeMenu(
  accessToken: string,
  user: AuthUser | null | undefined,
  t: (key: string) => string,
): Promise<SelectOption[]> {
  const stateTenantId = tenantId();
  const masters = await fetchMdmsMasters(
    stateTenantId,
    "Incident",
    ["ServiceDefs"],
    accessToken,
    user,
  );
  const serviceDefs = (masters.ServiceDefs as ServiceDef[]) ?? [];
  const seen = new Set<string>();
  const options: SelectOption[] = [];

  for (const def of serviceDefs) {
    if (def.deprecated || !def.menuPath || seen.has(def.menuPath)) {
      continue;
    }
    seen.add(def.menuPath);
    options.push({
      code: def.menuPath,
      key: def.menuPath,
      menuPath: def.menuPath,
      name: translateOr(t, `SERVICEDEFS.${def.menuPath.toUpperCase()}`, def.menuPath),
    });
  }

  return options.sort((a, b) => a.name.localeCompare(b.name));
}

/**
 * "Is the Solar System Working?" dropdown (DIGIT-UI's im/CreateComplaint: module
 * "Incident", master "SystemFunctionality"). Option codes are typically FUNCTIONAL /
 * NON_FUNCTIONAL; DIGIT-UI translates each record's own `name` field directly rather
 * than building a key, so this does the same.
 */
export async function fetchSystemFunctionalityOptions(
  accessToken: string,
  user: AuthUser | null | undefined,
  t: (key: string) => string,
): Promise<SelectOption[]> {
  const stateTenantId = tenantId();
  const masters = await fetchMdmsMasters(
    stateTenantId,
    "Incident",
    ["SystemFunctionality"],
    accessToken,
    user,
  );
  const defs = (masters.SystemFunctionality as SystemFunctionalityDef[]) ?? [];

  return defs
    .filter((def) => def.active !== false && def.code)
    .map((def) => ({
      code: def.code!,
      key: def.code!,
      name: translateOr(t, def.name ?? def.code!, def.name ?? def.code!),
    }));
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
