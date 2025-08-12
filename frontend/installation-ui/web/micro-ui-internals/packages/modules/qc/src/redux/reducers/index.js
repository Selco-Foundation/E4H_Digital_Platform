import { combineReducers } from "redux";
import { commonReducer } from "./commonreducer";
import { rejectionReasonsReducer } from "./rejectionreasonsreducer";

const loadInitialRejectionReasonsState = () => {
  try {
    const persisted = sessionStorage.getItem("rejectionReasons");
    return persisted ? JSON.parse(persisted) : {};
  } catch {
    return {};
  }
}

const getRootReducer = () =>
  combineReducers({
    common: commonReducer({}),
    rejectionReasons: rejectionReasonsReducer(loadInitialRejectionReasonsState()),
  });

export default getRootReducer;
