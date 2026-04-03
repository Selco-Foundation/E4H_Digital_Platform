import { LANGUAGE_SELECT, FIELD_PLAN_SELECT, FACILITY_SELECT } from "../../constants/ReduxActions";

export const commonReducer = (defaultData) => (state = defaultData, action) => {
  switch (action.type) {
    case LANGUAGE_SELECT:
      return { ...state, selectedLanguage: action.payload };

    case FIELD_PLAN_SELECT:
      return { ...state, selectedFieldPlan: action.payload };

    case FACILITY_SELECT:
      return { ...state, selectedFacility: action.payload };

    default:
      return state;
  }
};
