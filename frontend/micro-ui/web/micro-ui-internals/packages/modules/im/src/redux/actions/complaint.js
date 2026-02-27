import { CREATE_COMPLAINT } from "./types";

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

export {
  createComplaint,
  populateCreateResponse,
};
