import { combineReducers } from "redux";
import { commonReducer } from "./commonreducer";

const getRootReducer = () =>
  combineReducers({
    common: commonReducer({}),
  });

export default getRootReducer;
