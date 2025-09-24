import { POPULATE_RESPONSE, POPULATE_WORKING_FIELD_PLAN, POPULATE_WORKING_PROJECT } from "../../constants/ReduxActions";

export const commonReducer = (defaultData) => (state = defaultData, action) => {

  switch (action.type) {

    case POPULATE_RESPONSE:
      return {...state, responseData: action.payload};

    case POPULATE_WORKING_PROJECT:
      return {...state, workingProject: action.payload};

    case POPULATE_WORKING_FIELD_PLAN:
      return {...state, workingFieldPlan: action.payload};

    default:
      return state;
  }

};
