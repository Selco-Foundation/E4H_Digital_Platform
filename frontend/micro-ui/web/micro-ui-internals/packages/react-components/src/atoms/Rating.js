import React, { useRef } from "react";
import { StarFilled } from "./svgindex";
import { StarEmpty } from "./svgindex";
import PropTypes from "prop-types";

const Rating = (props) => {
  var stars = [];
  const star = useRef(null);

  for (var i = 1; i <= props.maxRating; i++) {
    const index = i;
    let starElement;
    if (i - props.currentRating <= 0) {
      // stars.push(<img key={i} src={starfilled} className="rating-star" alt="star filled" ref={star} onClick={(e,ref)=>props.onFeedback(e,ref)}/>)
      starElement = (<StarFilled key={i} id={`${props.id}gradient${i}`} className="rating-star" styles={props.starStyles} onClick={(e) => props.onFeedback(e, star, index)} />);
    } else if (i - props.currentRating > 0 && i - props.currentRating < 1) {
      starElement = (<StarFilled key={i} id={`${props.id}gradient${i}`} className="rating-star" styles={props.starStyles} onClick={(e) => props.onFeedback(e, star, index)} percentage={Math.round(((props.currentRating - parseInt(props.currentRating)) * 100))} />)
    } else {
      // stars.push(<img key={i} src={starempty} className="rating-star" alt="star empty" ref={star} onClick={(e,ref)=>props.onFeedback(e,ref)}/>)
      starElement = (<StarEmpty key={i} className="rating-star" styles={props.starStyles} onClick={(e) => props.onFeedback(e, star, index)} />);
    }

    stars.push(
      <div
        key={`wrapper-${i}`}
        className="rating-star-column"
        style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}
      >
        {!props?.rated && <span className="rating-star-index">{index}</span>}
        {starElement}
      </div>
    );
  }

  return (
    <div className={`${props.withText ? "rating-with-text" : "rating-star-wrap"}`} style={{ ...props.styles }}>
      {props.text ? props.text : ""} {stars}
    </div>
  );
};

Rating.propTypes = {
  maxRating: PropTypes.number,
  currentRating: PropTypes.number,
  onFeedback: PropTypes.func,
};

Rating.defaultProps = {
  maxRating: 5,
  id: '0',
  currentRating: 0,
  onFeedback: () => {},
};

export default Rating;
