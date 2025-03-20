import React from "react";
import PropTypes from "prop-types";
import { Link } from "react-router-dom";
import CitizenInfoLabel from "../atoms/CitizenInfoLabel";
import  ActionBar from "../atoms/ActionBar";
import SubmitBar from "../atoms/SubmitBar";
export const Details = ({ label, name, onClick}) => {
  return (
    <div className="detail" onClick={onClick}>
      <div className="label">
        <h2>{label}</h2>
      </div>
      <div className="name" style={{overflowWrap:"break-word", color:"black", paddingTop: "16px"}}>{name}</div>
    </div>
  );
};

const DetailsCard = ({ data, serviceRequestIdKey, linkPrefix, handleSelect, selectedItems, keyForSelected, handleDetailCardClick, isTwoDynamicPrefix = false, getRedirectionLink, handleClickEnabled = true, t, showActionBar = true, showCitizenInfoLabel = false,submitButtonLabel }) => {
  if (linkPrefix && serviceRequestIdKey) {
    return (
      <div>
      <style>
      {`
        a{
          text-decoration:none
        }
      `}
      </style>
        {data.map((object, itemIndex) => {
          let key = Object.keys(object)
          const incidentId = object[key[0]].props.children
          return (
            <Link
              key={itemIndex}
              to={window.location.href.includes("im/inbox") ? `/${window.contextPath}/employee/im/complaint/details/` + incidentId + "/" + object["TenantID"]
                : isTwoDynamicPrefix
                  ?
                  `${linkPrefix}${typeof serviceRequestIdKey === "function"
                    ?
                    serviceRequestIdKey(object)
                    :
                    `${getRedirectionLink(object["Application Type"] === "BPA_STAKEHOLDER_REGISTRATION" ? "BPAREG" : "BPA")}/${object[object["Application Type"] === "BPA_STAKEHOLDER_REGISTRATION" ? "applicationNo" : "Application Number"]}`}`
                  :
                  `${linkPrefix}${typeof serviceRequestIdKey === "function"
                    ?
                    serviceRequestIdKey(object)
                      :
                    object[serviceRequestIdKey]}`
                }
                style={{textDecoration:"none !important", color:"black"}}
            >
              <div className="details-container">
                {Object.keys(object).map((name, index) => {
                  if (name === "applicationNo" || name === "Vehicle Log" || name == "TenantID") return null;
                  return <Details label={name} name={object[name]} key={index} />;
                })}
              </div>
            </Link>
          );
        })}
      </div>
    );
  }

  return (
    <div>
  
      {data.map((object, itemIndex) => {
        return (
          <div
            key={itemIndex}
            style={{ border: selectedItems?.includes(object[keyForSelected]) ? "2px solid #7a2829" : "2px solid #fff" }}
            className="details-container"
            onClick={() =>handleClickEnabled && handleSelect(object)}
          >
            {Object.keys(object).filter(rowEle => !(typeof object[rowEle] == "object" && object[rowEle]?.hidden == true)).map((name, index) => {
              return <Details label={name} name={object[name]} key={index} onClick={() =>handleClickEnabled && handleDetailCardClick(object)} />;
            })}
            {showCitizenInfoLabel ?<CitizenInfoLabel
              style={{ margin: " 2rem 0px", padding: "10px", backgroundColor: "#FFE2B5", borderRadius: "0.25rem" }}
              textStyle={{ color: "#CC7B2F" }}
              info={t("ATM_INFO_LABEL")}
              text={t(`ATM_INFO_TEXT`)}
              fill={"#CC7B2F"}
            />:null}
            {showActionBar ? 
              <SubmitBar onSubmit={() => handleDetailCardClick(object)} label={submitButtonLabel} />
            :null}
          </div>
        );
      })}
    </div>
  );
};

DetailsCard.propTypes = {
  data: PropTypes.array,
};

DetailsCard.defaultProps = {
  data: [],
};

export default DetailsCard;
