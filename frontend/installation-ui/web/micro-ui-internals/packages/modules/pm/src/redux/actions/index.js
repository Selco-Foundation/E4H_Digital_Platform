import { POPULATE_RESPONSE, POPULATE_WORKING_FIELD_PLAN, POPULATE_WORKING_PROJECT, POPULATE_WORKING_ASSESSMENT_PLAN, POPULATE_WORKING_FACILITY, POPULATE_WORKING_AMC_CONFIGURATION, POPULATE_WORKING_AMC_VISIT, POPULATE_WORKING_FACILITY_DETAILS, POPULATE_WORKING_ASSESSMENT_FACILITY } from "../../constants/ReduxActions";

const populateResponsePage = (responseData) => (dispatch) => {
  dispatch({
    type: POPULATE_RESPONSE,
    payload: responseData,
  })
}

const populateWorkingProject = (project) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_PROJECT,
    payload: project,
  })
}

const populateWorkingFieldPlan = (fieldPlan) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_FIELD_PLAN,
    payload: fieldPlan,
  })
}

const populateWorkingAssessmentPlan = (assessmentPlan) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_ASSESSMENT_PLAN,
    payload: assessmentPlan,
  })
}

const populateWorkingFacility = (facility) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_FACILITY,
    payload: facility,
  })
}

const populateWorkingAMCConfiguration = (amcConfiguration) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_AMC_CONFIGURATION,
    payload: amcConfiguration,
  })
}

const populateWorkingAMCVisit = (amcVisit) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_AMC_VISIT,
    payload: amcVisit,
  })
}

const populateWorkingFacilityDetails = (facilityDetails) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_FACILITY_DETAILS,
    payload: facilityDetails,
  })
}

const populateWorkingAssessmentFacility = (assessmentFacility) => (dispatch) => {
  dispatch({
    type: POPULATE_WORKING_ASSESSMENT_FACILITY,
    payload: assessmentFacility,
  })
}

export {
  populateResponsePage,
  populateWorkingProject,
  populateWorkingFieldPlan,
  populateWorkingAssessmentPlan,
  populateWorkingFacility,
  populateWorkingAMCConfiguration,
  populateWorkingAMCVisit,
  populateWorkingFacilityDetails,
  populateWorkingAssessmentFacility
};