import { POPULATE_RESPONSE } from "../../constants/ReduxActions";

export const commonReducer = (defaultData) => (state = defaultData, action) => {
  switch (action.type) {

    case POPULATE_RESPONSE:
      return {...state, responseData: action.payload};

    default:
      return state;
  }
};
