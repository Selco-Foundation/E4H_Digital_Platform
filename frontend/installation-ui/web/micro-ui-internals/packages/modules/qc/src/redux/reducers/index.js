import { combineReducers } from "redux";
import { commonReducer } from "./commonreducer";
import { rejectionReasonsReducer } from "./rejectionreasonsreducer";

const getRootReducer = () =>
  combineReducers({
    common: commonReducer({}),
    rejectionReasons: rejectionReasonsReducer({}),
  });

export default getRootReducer;
