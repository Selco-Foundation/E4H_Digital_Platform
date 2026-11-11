const manageLocParams = ({searchForm,params}) => {
  
  const updatedParams = {
    ...params,
    codes:searchForm?.codes,
    module:searchForm?.module?.value,
  }
  for (const key in updatedParams) {
    if (updatedParams.hasOwnProperty(key) && !updatedParams[key]) {
      delete updatedParams[key];
    }
  }
  return updatedParams
}

// Selco: search every language (one result column each) instead of one selected locale. A language whose search fails
// just shows an empty column.
const LocalisationSearchUtil = async ({ url, params, body, plainAccessRequest,state,languages = [] } ) => {
  const CustomService = Digit.CustomService
  const updatedParams = manageLocParams({...state,params})
  const responses = await Promise.all(
    languages.map((locale) =>
      CustomService.getResponse({ url, params: { ...updatedParams, locale }, body, plainAccessRequest })
        .then((data) => ({ locale, messages: data?.messages || [] }))
        .catch(() => ({ locale, messages: [] }))
    )
  )

  return Digit?.Customizations?.['commonUiConfig']?.['SearchLocalisationConfig']?.combineData({ responses })
}

export const LocalisationSearch = {
  fetchResults : async ({ url, params, body, plainAccessRequest,state,languages }) => {

    try {
      const response = await LocalisationSearchUtil({ url, params, body, plainAccessRequest,state,languages })
      return response
    } catch (error) {
      throw new Error(error?.response?.data?.Errors[0].message);
    }
  }
}