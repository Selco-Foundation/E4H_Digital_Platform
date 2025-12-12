export const commonReducer = (defaultData) => (state = defaultData, action) => {
  switch (action.type) {
    case "LANGUAGE_SELECT":
      return { ...state, selectedLanguage: action.payload };
    case "ADD_LANGUAGE_OPTIONS": {
      const existingLanguageValues = state.languages?.map(language => language?.value) || [];
      const newLanguages = action.payload.filter((language) => !existingLanguageValues.includes(language.value));
      return {...state, languages: [...(state.languages || []), ...newLanguages]};
    }
    case "ADD_STATE_LOGOS":
      return { ...state, stateLogos: action.payload };
    case "SET_CRM_HELPLINE_NUMBER":
      return { ...state, crmHelplineNumber: action.payload };
    default:
      return state;
  }
};
