import { FACILITY_SELECT, FIELD_PLAN_SELECT } from "../../constants/ReduxActions";

const setSelectedFieldPlan = (fieldPlanId) => (dispatch) => {
  dispatch({
    type: FIELD_PLAN_SELECT,
    payload: fieldPlanId,
  })
}

const setSelectedFacility = (fieldPlanId) => (dispatch) => {
  dispatch({
    type: FACILITY_SELECT,
    payload: fieldPlanId,
  })
}

export { setSelectedFieldPlan, setSelectedFacility };