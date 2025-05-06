import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { CameraSvg } from "./svgindex";
import { DeleteBtn } from "./svgindex";
import Toast from "./Toast";
import { useTranslation } from "react-i18next";

const MiniUpload = (props) => {
  return (
    <div className="upload-img-container">
      <CameraSvg className="upload-camera-img" />
      <input type="file" id="upload" accept="image/*" style={{ marginLeft: `calc(-50% - 23px)` }} onChange={(e) => props.onUpload(e)} />
    </div>
  );
};

const UploadImages = (props) => {
  const [error, setError] = useState("");
  const { t } = useTranslation();

  const UploadImagesNew = (e) => {
    if (props.disabled) {
      setError(t("SET_PHC_TYPE"));
    } else {
      props.onUpload(e);
    }
  };

  if (props.thumbnails && props.thumbnails.length > 0) {
    return (
      <div className="multi-upload-wrap">
        {props.thumbnails.map((thumbnail, index) => {
          return (
            <div key={index}>
              <DeleteBtn onClick={() => props.onDelete(thumbnail)} className="delete" fill="#d4351c" />
              <img src={thumbnail} alt="uploaded thumbnail" />
            </div>
          );
        })}
        {props.thumbnails.length < 3 ? <MiniUpload onUpload={props.onUpload} /> : null}
      </div>
    );
  } else {
    return (
      <div>
        {error && <Toast error={true} label={error} onClose={() => setError(null)} />}
        <div className="upload-wrap" onClick={(e) => UploadImagesNew(e)}>
          <CameraSvg />
          <input type="file" id="upload" accept="image/*" onChange={(e) => UploadImagesNew(e)} disabled={props.disabled} />
        </div>
      </div>
    );
  }
};

UploadImages.propTypes = {
  thumbnail: PropTypes.array,
  onUpload: PropTypes.func,
};

UploadImages.defaultProps = {
  thumbnail: [],
  onUpload: undefined,
};

export default UploadImages;
