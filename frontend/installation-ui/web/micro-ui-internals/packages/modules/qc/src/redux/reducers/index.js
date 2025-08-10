import { combineReducers } from "redux";
import { commonReducer } from "./commonreducer";

const loadInitialState = () => {
  try {
    const persisted = localStorage.getItem("qcStore");
    return persisted ? JSON.parse(persisted) : {};
  } catch {
    return {};
  }
};

const getRootReducer = () =>
  combineReducers({
    reports: commonReducer(loadInitialState()),
  });

export default getRootReducer;
