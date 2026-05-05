import { CREATE_COMPLAINT, PAUSE_RMS } from "./types";

const createComplaint = ({
  complaintType,
 comments,
 healthcentre,
 subType,
 systemFunctionality,
 district,
 block,
 uploadImages,
 uploadedFile,
  tenantId
}) => async (dispatch, getState) => {
  const response = await Digit.Complaint.create({
    comments,
    complaintType,
    district,
    block,
    uploadedFile,
    healthcentre,
    subType,
    systemFunctionality,
    uploadImages,
    tenantId

    
  });
  console.log("res", response)
  dispatch({
    type: CREATE_COMPLAINT,
    payload: response,
  });
};

const populateCreateResponse = (response) => (dispatch) => {
  dispatch({
    type: CREATE_COMPLAINT,
    payload: response,
  });
}

const populatePauseRMSResponse = (response) => (dispatch) => {
  dispatch({
    type: PAUSE_RMS,
    payload: response,
  });
}

export {
  createComplaint,
  populateCreateResponse,
  populatePauseRMSResponse,
};
