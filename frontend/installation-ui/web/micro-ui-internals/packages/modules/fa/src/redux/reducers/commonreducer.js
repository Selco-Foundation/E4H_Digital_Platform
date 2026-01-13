import { LANGUAGE_SELECT, POPULATE_WORKING_ACTIVITY, POPULATE_WORKING_FACILITY } from "../../constants/ReduxActions";

export const commonReducer = (defaultData) => (state = defaultData, action) => {
  switch (action.type) {
    case LANGUAGE_SELECT:
      return { ...state, selectedLanguage: action.payload };

    case POPULATE_WORKING_FACILITY:
      return { ...state, workingFacility: action.payload };

    case POPULATE_WORKING_ACTIVITY:
      return { ...state, workingActivity: action.payload };

    default:
      return state;
  }
};
