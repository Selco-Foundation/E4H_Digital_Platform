import React from "react";
import PropTypes from "prop-types";
import HlsPlayer from "./VideoPlayer/HlsPlayer";

const VideoThumbnail = ({ source, index, onClick }) => {
  console.debug("Rendering video thumbnail for:", source);
  return (
    <div key={index} className="video-thumbnail" onClick={() => onClick(source, index)}>
      <HlsPlayer src={source} />
    </div>
  );
};

const DisplayVideos = (props) => {
  return (
    <div className="videos-wrap" style={{ ...props.style, maxWidth: "auto", display: "flex", flexWrap: "wrap", gap: "10px" }}>
      {props.srcs.map((source, index) => {
        return <VideoThumbnail key={index} source={source} index={index} onClick={props.onClick} />;
      })}
    </div>
  );
};

DisplayVideos.propTypes = {
  srcs: PropTypes.array,
  onClick: PropTypes.func,
};

DisplayVideos.defaultProps = {
  srcs: [],
  onClick: () => {},
};

export default DisplayVideos;
