import { MdmsService } from "../elements/MDMS";
import { Storage } from "../atoms/Utils/Storage";

export const GetServiceDefinitions = {
  get: async (tenantId, filter) => {
    const criteria = {
      type: "serviceDefs",
      details: {
        tenantId: tenantId,
        moduleDetails: [
          {
            moduleName: "Incident",
            masterDetails: [
              {
                name: "ServiceDefs",
                ...(filter ? { filter } : {}),
              },
            ],
          },
        ],
      },
    };
    const serviceDefs = await MdmsService.getDataByCriteria(tenantId, criteria, "Incident");
    Storage.set("serviceDefinitions", serviceDefs);
    return serviceDefs;
  },
  getMenu: async (stateCode, t, filter) => {
    var Menu = [];
    const response = await GetServiceDefinitions.get(stateCode, filter);
    await Promise.all(
      response.map((def) => {
        if (!def.deprecated && !Menu.find((e) => e.key === def.menuPath)) {
          def.menuPath === ""
            ? Menu.push({
                name: t("SERVICEDEFS.OTHERS"),
                key: def.menuPath,
              })
            : Menu.push({
                name: t("SERVICEDEFS." + def.menuPath.toUpperCase()),
                key: def.menuPath,
              });
        }
      })
    );
    return Menu;
  },

  getSubMenu: async (tenantId, selectedType, t, filter) => {
    const fetchServiceDefs = await GetServiceDefinitions.get(tenantId, filter);
    return fetchServiceDefs
      .filter((def) => !def.deprecated && def.menuPath === selectedType.key)
      .map((id) => ({
        key: id.serviceCode,
        name: t("SERVICEDEFS." + id.serviceCode.toUpperCase()),
      }));
  },
};
