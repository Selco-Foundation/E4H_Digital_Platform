import { FACILITY_SELECT, FIELD_PLAN_SELECT } from "../../constants/ReduxActions";

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

export { setSelectedFieldPlan, setSelectedFacility };