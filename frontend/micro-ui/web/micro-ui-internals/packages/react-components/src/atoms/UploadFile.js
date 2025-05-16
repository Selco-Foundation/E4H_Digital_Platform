import React, { useEffect, useRef, useState, Fragment } from "react";
import ButtonSelector from "./ButtonSelector";
import { Close, CloseSvg, PlayIcon, UploadIcon, UploadIconOrange } from "./svgindex";
import { useTranslation } from "react-i18next";
import RemoveableTag from "./RemoveableTag";
import { DeleteBtn } from "./svgindex";
import { Button, Loader } from "@egovernments/digit-ui-react-components";
const randomId = Math.floor((Math.random() || 1) * 139);

const getCitizenStyles = (value) => {
  let citizenStyles = {};
  if (value == "propertyCreate") {
    citizenStyles = {
      textStyles: {
        whiteSpace: "nowrap",
        width: "100%",
        overflow: "hidden",
        textOverflow: "ellipsis",
        width: "80%",
      },
      tagStyles: {
        width: "90%",
        flexWrap: "nowrap",
      },
      inputStyles: {
        width: "44%",
        minHeight: "2rem",
        maxHeight: "3rem",
        top: "20%",
        paddingLeft: "0px !important",
        paddingRight: "0px !important",
      },
      buttonStyles: {
        height: "auto",
        minHeight: "2rem",
        width: "40%",
        maxHeight: "3rem",
      },
      tagContainerStyles: {
        width: "60%",
        display: "flex",
        marginTop: "0px",
      },
      closeIconStyles: {
        width: "20px",
      },
      containerStyles: {
        padding: "10px",
        marginTop: "0px",
      },
    };
  } else if (value == "IP") {
    citizenStyles = {
      textStyles: {
        whiteSpace: "nowrap",
        maxWidth: "250px",
        overflow: "hidden",
        textOverflow: "ellipsis",
      },
      tagStyles: {
        marginLeft: "-30px",
      },
      inputStyles: {},
      closeIconStyles: {
        position: "absolute",
        marginTop: "-12px",
      },
      buttonStyles: {},
      tagContainerStyles: {},
    };
  } else if (value == "OBPS") {
    citizenStyles = {
      containerStyles: {
        display: "flex",
        justifyContent: "flex-start",
        alignItems: "center",
        flexWrap: "wrap",
        margin: "0px",
        padding: "0px",
      },
      tagContainerStyles: {
        margin: "0px",
        padding: "0px",
        maxWidth: "100%",
      },
      tagStyles: {
        height: "auto",
        padding: "5px",
        margin: 0,
        width: "100%",
        margin: "5px",
      },
      textStyles: {
        wordBreak: "break-all",
        height: "auto",
        lineHeight: "16px",
        overflow: "hidden",
        // minHeight: "35px",
        maxHeight: "34px",
        maxWidth: "100%",
      },
      inputStyles: {
        width: "42%",
        minHeight: "42px",
        maxHeight: "42px",
        top: "12px",
        left: "12px",
      },
      buttonStyles: {
        height: "auto",
        minHeight: "40px",
        width: "43%",
        maxHeight: "40px",
        margin: "5px",
        padding: "0px",
      },
      closeIconStyles: {
        width: "20px",
      },
      uploadFile: {
        maxHeight: "35px",
      },
    };
  } else {
    citizenStyles = {
      textStyles: {},
      tagStyles: {},
      inputStyles: {},
      buttonStyles: {},
      tagContainerStyles: {},
    };
  }
  return citizenStyles;
};

const UploadFile = (props) => {
  const { t } = useTranslation();
  const inputRef = useRef();
  const [hasFile, setHasFile] = useState(false);
  const [prevSate, setprevSate] = useState(null);
  const user_type = Digit.SessionStorage.get("userType");
  let extraStyles = {};
  const handleChange = () => {
    if (inputRef.current.files[0]) {
      setHasFile(true);
      setprevSate(inputRef.current.files[0]);
    } else setHasFile(false);
  };

  // for common aligmnent issues added common styles
  extraStyles = getCitizenStyles("OBPS");

  // if (window.location.href.includes("/obps") || window.location.href.includes("/noc")) {
  //   extraStyles = getCitizenStyles("OBPS");
  // } else {
  //   switch (props.extraStyleName) {
  //     case "propertyCreate":
  //       extraStyles = getCitizenStyles("propertyCreate");
  //       break;
  //     case "IP":
  //       extraStyles = getCitizenStyles("IP");
  //       break;
  //     case "OBPS":
  //       extraStyles = getCitizenStyles("OBPS");
  //     default:
  //       extraStyles = getCitizenStyles("");
  //   }
  // }

  const handleDelete = () => {
    inputRef.current.value = "";
    props.onDelete();
  };

  const handleEmpty = () => {
    if (inputRef.current.files.length <= 0 && prevSate !== null) {
      inputRef.current.value = "";
      props.onDelete();
    }
  };

  if (props.uploadMessage && inputRef.current.value) {
    handleDelete();
    setHasFile(false);
  }

  useEffect(() => handleEmpty(), [inputRef?.current?.files]);

  useEffect(() => handleChange(), [props.message]);

  const showHint = props?.showHint || false;
  const styles = {
    ...props?.textStyles,
    color: "rgba(207,98,55,255)",
  };

  return (
    <Fragment>
      {showHint && <p className="cell-text">{t(props?.hintText)}</p>}
      <div style={{ display: "flex", alignItems: "center" }}>
        <div
          className={`upload-file ${props?.customClass} ${user_type === "employee" ? "" : "upload-file-max-width"} ${
            props.disabled ? " disabled" : ""
          }`}
          style={
            extraStyles?.uploadFile
              ? {
                  ...extraStyles?.uploadFile,
                  padding: "0.5rem",
                  width: "85%",
                  display: "flex",
                  alignItems: "center",
                  color: props?.uploadedFiles?.length === 0 ? "#D5D5D5" : "#000000",
                }
              : {}
          }
        >
          {props?.uploadedFiles?.length === 0 ? t("CS_NO_FILES_SELECTED") : t("CS_FILES_UPLOADED").replace("{}", props?.uploadedFiles?.length)}
        </div>
        <div style={extraStyles ? extraStyles?.containerStyles : null}>
          <input
            className={props.disabled ? "disabled" : "" + "input-mirror-selector-button"}
            style={{
              ...(extraStyles
                ? {
                    ...extraStyles?.inputStyles,
                    ...props?.inputStyles,
                    maxHeight: "56px !important",
                    paddingLeft: "0px !important",
                    paddingRight: "0px !important",
                    display: "none",
                  }
                : { ...props?.inputStyles }),
              cursor: "pointer",
            }}
            ref={inputRef}
            type="file"
            id={props.id || `document-${randomId}`}
            name="file"
            multiple={props.multiple}
            accept={props.accept}
            disabled={props.disabled}
            onChange={(e) => {
              props.onUpload(e);
            }}
            onClick={(event) => {
              if (!props?.enableButton) {
                event.preventDefault();
              } else {
                const { target = {} } = event || {};
                target.value = "";
              }
            }}
          />
          <div
            style={{
              ...(extraStyles ? extraStyles?.buttonStyles : {}),
              ...(!props.enableButton ? { opacity: 0.5 } : {}),
              width: "90px",
              minHeight: "0px",
              height: "35px",
              backgroundColor: "transparent",
              border: "2px solid rgba(207,98,55,255)",
              color: "rgba(207,98,55,255)",
              boxShadow: "none",
              fontSize: "16px",
              fontWeight: 550,
              cursor: "pointer",
            }}
            textStyles={styles}
            type={props.buttonType}
            onClick={() => {
              inputRef.current.click();
            }}
          >
            <div style={{ display: "flex", width: "100%", height: "100%", justifyContent: "space-evenly", alignItems: "center" }}>
              <UploadIconOrange styles={{ height: "25px", width: "25px", color: "rgba(207,98,55,255)" }} />
              {t("CS_UPLOAD_BUTTON")}
            </div>
          </div>
        </div>
      </div>
      {props.isUploading && <Loader />}
      <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
        {props?.uploadedFiles?.map((file, index) => {
          const fileDetailsData = file[1];
          const fileType = fileDetailsData.file.type;
          console.log(fileType, "fileType", file);
          const blob = new Blob([file[1].file], { type: file.type });
          const fileSrc = URL.createObjectURL(blob);
          return (
            <div className="tag-container" style={extraStyles ? extraStyles?.tagContainerStyles : null}>
              {fileType.substring(0, 5) === "image" ? (
                <div style={{ width: "100px", display: "flex", flexDirection: "column", flexWrap: "wrap", marginTop: "10px" }}>
                  <img src={fileSrc} alt="thumbnail" style={{ width: "100px", height: "80px" }} />
                  <div style={{ color: "#8F8F8F", fontSize: "12px", textAlign: "center", width: "100%" }}>
                    {fileDetailsData.file.name.length > 12
                      ? `${fileDetailsData.file.name.substring(0, 7)}...${fileDetailsData.file.name.substring(fileDetailsData.file.name.length - 7)}`
                      : fileDetailsData.file.name}
                  </div>
                </div>
              ) : fileType.substring(0, 5) === "video" ? (
                <div style={{ width: "fit-content", display: "flex", flexDirection: "column", flexWrap: "wrap", marginTop: "10px" }}>
                  <div style={{ position: "relative", height: "250px", width: "300px" }}>
                    <video
                      ref={(el) => (fileDetailsData.videoRef = el)}
                      src={fileSrc}
                      style={{ height: "100%", width: "100%" }}
                      onClick={() => {
                        if (fileDetailsData.videoRef.paused) {
                          fileDetailsData.videoRef.play();
                        } else {
                          fileDetailsData.videoRef.pause();
                        }
                      }}
                    />
                    <div
                      onClick={() => {
                        if (fileDetailsData.videoRef.paused) {
                          fileDetailsData.videoRef.play();
                        } else {
                          fileDetailsData.videoRef.pause();
                        }
                      }}
                      style={{
                        position: "absolute",
                        bottom: "45%",
                        left: "50%",
                        transform: "translateX(-50%)",
                        background: "rgba(0,0,0,0.5)",
                        border: "none",
                        borderRadius: "50%",
                        width: "40px",
                        height: "40px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        color: "white",
                        cursor: "pointer",
                        paddingLeft: "5px",
                      }}
                    >
                      <PlayIcon color="white" />
                    </div>
                  </div>

                  {/* <div
                    style={{ zIndex: 9999, position: "relative", top: "45%", left: '45%' }}
                  >
                    <PlayIcon color={'white'} /></div> */}
                  <div style={{ color: "#8F8F8F", fontSize: "12px", textAlign: "center", width: "100%" }}>
                    {fileDetailsData.file.name.length > 20
                      ? `${fileDetailsData.file.name.substring(0, 10)}...${fileDetailsData.file.name.substring(
                          fileDetailsData.file.name.length - 10
                        )}`
                      : fileDetailsData.file.name}
                  </div>
                </div>
              ) : null}
              {(fileType.substring(0, 5) === "image" || fileType.substring(0, 5) === "video") && (
                <div
                  style={{
                    zIndex: 9999,
                    position: "relative",
                    right: "24px",
                    top: fileType.substring(0, 5) === "video" ? "30px" : "10px",
                    cursor: "pointer",
                    height: "fit-content",
                  }}
                  onClick={(e) => props?.removeTargetedFile(fileDetailsData, e)}
                >
                  <CloseSvg color="white" background="#135067" />
                </div>
              )}
              {fileType.substring(0, 5) !== "image" && fileType.substring(0, 5) !== "video" && (
                <RemoveableTag extraStyles={extraStyles} key={index} text={file[0]} onClick={(e) => props?.removeTargetedFile(fileDetailsData, e)} />
              )}
            </div>
          );
        })}
      </div>
      {props.iserror && <p style={{ color: "red" }}>{props.iserror}</p>}
      {props?.showHintBelow && (
        <p className="cell-text" style={{ paddingTop: "3px" }}>
          {t(props?.hintText)}
        </p>
      )}
    </Fragment>
  );
};

export default UploadFile;
