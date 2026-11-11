export const Config = {
  label: "WBH_LOCALISATION_SEARCH_HEADER",
  type: "search",
  actionLabel: "WBH_ADD_LOCALISATION",
  // Selco: MDMS_ADMIN (the workbench admin) also gets the Add Localisation button
  actionRoles: ["LOC_ADMIN", "MDMS_ADMIN"],
  actionLink: "workbench/localisation-add",
  apiDetails: {
    serviceName: "/localization/messages/v1/_search",
    requestParam: {},
    requestBody: {
      
    },
    minParametersForSearchForm: 1,
    masterName: "commonUiConfig",
    moduleName: "SearchLocalisationConfig",
    tableFormJsonPath: "requestBody.custom",
    filterFormJsonPath: "requestBody.custom",
    searchFormJsonPath: "requestParam"
  },
  sections: {
    search: {
      uiConfig: {
        searchWrapperStyles:{
          flexDirection:"column-reverse",
          marginTop:"2rem",
          alignItems:"center",
          justifyContent:"end",
          gridColumn:"4"
        },
        headerStyle: null,
        formClassName: "", //"custom-both-clear-search",
        primaryLabel: "ES_COMMON_SEARCH",
        secondaryLabel: "ES_COMMON_CLEAR_SEARCH",
        minReqFields: 1,
        // Selco: no language filter; every language is shown as its own result column (see LocalisationSearch.js)
        defaultValues: {
          module: "",
          codes: ""
        },
        fields: [
          {
            label: "WBH_LOC_MODULE",
            type: "dropdown",
            isMandatory: false,
            disable: false,
            populators: {
              name: "module",
              optionsKey: "label",
              optionsCustomStyle: { top: "2.3rem" },
              mdmsConfig: {
                masterName: "LocalizationModules",
                moduleName: "common-masters",
                // Selco: StateInfo in the Selco tenants has no localizationModules; use the LocalizationModules master
                select:
                    "(data)=>{ return data['common-masters'].LocalizationModules }"
                } 
            },
          },
          {
            label: "WBH_LOC_CODE",
            type: "text",
            isMandatory: false,
            disable: false,
            populators: { 
                name: "codes",
            }
          },
          // {
          //   label: "WBH_LOC_MESSAGE",
          //   type: "text",
          //   isMandatory: false,
          //   disable: false,
          //   populators: { 
          //       name: "message",
          //   }
          // },
        ],
      },
      label: "",
      children: {},
      show: true,
    },
    searchResult: {
      label: "",
      uiConfig: {
        tableClassName:"table-fixed-last-column table",
        resultsWrapperClassName:"localization-search-table",
        // Selco: LocalisationSearch.js inserts one column per language (common-masters.Languages) after the module name
        columns: [
          {
            label: "WBH_LOC_HEADER_CODE",
            jsonPath: "code",
          },
          {
            label: "WBH_LOC_HEADER_MODULENAME",
            jsonPath: "module",
          },
          {
            label: "CS_COMMON_ACTION",
            // jsonPath: "message",
            // type:"action",
            svg:"EditIcon",
          },
        ],
        enableGlobalSearch: true,
        enableColumnSort: true,
        resultsJsonPath: "messages",
        manualPagination:false
      },
      children: {},
      show: true,
    },
  },
  additionalSections: {},
  customHookName:"workbench.useLocalisationSearch"
};
