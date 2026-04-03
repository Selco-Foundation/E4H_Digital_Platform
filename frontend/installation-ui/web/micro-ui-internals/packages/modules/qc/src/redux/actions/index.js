import {
  CLEAR_REJECTION_REASONS,
  FACILITY_SELECT,
  FIELD_PLAN_SELECT, SET_REJECTION_REASONS
} from "../../constants/ReduxActions";

const setSelectedFieldPlan = (fieldPlan) => (dispatch) => {
  dispatch({
    type: FIELD_PLAN_SELECT,
    payload: fieldPlan,
  })
}

const setSelectedFacility = (fieldPlan) => (dispatch) => {
  dispatch({
    type: FACILITY_SELECT,
    payload: fieldPlan,
  })
}

const setRejectionReasons = (sectionName, reasons) => (dispatch) => {
  dispatch({
    type: SET_REJECTION_REASONS,
    payload: { sectionName, reasons },
  })
}

const clearRejectionReasons = () => (dispatch) => {
  console.debug("clearing rejection reasons");
  dispatch({
    type: CLEAR_REJECTION_REASONS
  })
}

export { setSelectedFieldPlan, setSelectedFacility, setRejectionReasons, clearRejectionReasons };