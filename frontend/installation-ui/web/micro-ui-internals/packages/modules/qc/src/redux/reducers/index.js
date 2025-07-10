import { combineReducers } from "redux";
import { commonReducer } from "./commonreducer";
import { rejectionReasonsReducer } from "./rejectionreasonsreducer";

const loadInitialState = () => {
  try {
    const persisted = sessionStorage.getItem("qcStore");
    return persisted ? JSON.parse(persisted) : {};
  } catch {
    return {};
  }
};

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
    common: commonReducer(loadInitialState()),
    rejectionReasons: rejectionReasonsReducer(loadInitialRejectionReasonsState()),
  });

export default getRootReducer;
