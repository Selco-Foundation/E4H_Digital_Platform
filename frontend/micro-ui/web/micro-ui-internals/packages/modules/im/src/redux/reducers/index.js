import { combineReducers } from "redux";
import complaintReducer from "./complaintReducer";
import pauseRMSReducer from "./pauseRMSReducer";

const getRootReducer = () =>
  combineReducers({
    complaints: complaintReducer,
    rms: pauseRMSReducer,
  });

export default getRootReducer;
