import {
  LANGUAGE_SELECT,
  FACILITY_SELECT,
  PROJECT_SELECT,
  VISIT_SELECT
} from "../../constants/ReduxActions";

export const commonReducer = (defaultData) => (state = defaultData, action) => {
  switch (action.type) {
    case LANGUAGE_SELECT:
      return { ...state, selectedLanguage: action.payload };

    case PROJECT_SELECT:
      return { ...state, workingProject: action.payload };

    case FACILITY_SELECT:
      return { ...state, selectedFacility: action.payload };

    case VISIT_SELECT:
      return { ...state, workingVisit: action.payload };

    default:
      return state;
  }
};
