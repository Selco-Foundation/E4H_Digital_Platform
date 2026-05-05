import { PAUSE_RMS } from "../actions/types";

function pauseRMSReducer(state = {}, action) {
  switch (action.type) {
    case PAUSE_RMS:
      return { ...state, response: action.payload };
    default:
      return state;
  }
}

export default pauseRMSReducer;
