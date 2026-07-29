import { POPULATE_RESPONSE, POPULATE_WORKING_FIELD_PLAN, POPULATE_WORKING_PROJECT, POPULATE_WORKING_ASSESSMENT_PLAN, POPULATE_WORKING_FACILITY } from "../../constants/ReduxActions";

export const commonReducer = (defaultData) => (state = defaultData, action) => {

  switch (action.type) {

    case POPULATE_RESPONSE:
      return {...state, responseData: action.payload};

    case POPULATE_WORKING_PROJECT:
      return {...state, workingProject: action.payload};

    case POPULATE_WORKING_FIELD_PLAN:
      return {...state, workingFieldPlan: action.payload};

    case POPULATE_WORKING_ASSESSMENT_PLAN:
      return {...state, workingAssessmentPlan: action.payload};

    case POPULATE_WORKING_FACILITY:
      return {...state, workingFacility: action.payload};

    default:
      return state;
  }

};
