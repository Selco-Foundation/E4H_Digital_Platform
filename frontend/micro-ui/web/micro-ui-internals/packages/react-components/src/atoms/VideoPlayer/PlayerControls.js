import React, { useEffect, useRef, useState } from "react";
import {
  FullscreenIcon,
  PauseIcon,
  PiPIcon,
  PlaybackSpeedIcon,
  PlayIcon,
  SkipBackwardIcon,
  SkipForwardIcon,
  VolumeOffIcon,
  VolumeUpIcon,
  SettingsIcon,
} from "../svgindex";

const PlayerControls = ({
  isPlaying,
  setIsPlaying,
  volume,
  setVolume,
  muted,
  setMuted,
  playbackRate,
  setPlaybackRate,
  setPip,
  played,
  duration,
  handleSeek,
  showControls,
  bitrateOptions,
  selectedBitrate,
  changeBitrate,
  handleFullscreen,
  originalSrc,
}) => {
  const progressRef = useRef(null);
  const settingsRef = useRef(null);

  const [currentTime, setCurrentTime] = useState(played * duration);
  const [showSettingsMenu, setShowSettingsMenu] = useState(false);
  const [showPlaybackSpeedOptions, setShowPlaybackSpeedOptions] = useState(false);
  const [showQualityOptions, setShowQualityOptions] = useState(false);
  const [hoverTime, setHoverTime] = useState(null);
  const [hoverPosition, setHoverPosition] = useState(0);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    if (currentTime >= duration) {
      setIsPlaying(false);
    }
  }, [currentTime, duration]);

  useEffect(() => {
    function handleClickOutside(event) {
      if (settingsRef.current && !settingsRef.current.contains(event.target)) {
        setShowSettingsMenu(false);
        setShowPlaybackSpeedOptions(false);
        setShowQualityOptions(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    setCurrentTime(played * duration);
  }, [played, duration]);

  const togglePlayPause = () => setIsPlaying((prev) => !prev);
  const handleMuteToggle = () => setMuted((prev) => !prev);

  const handleVolumeChange = (e) => {
    const newVolume = parseFloat(e.target.value);
    setVolume(newVolume);
    setMuted(newVolume === 0);
  };

  const handleSkip = (direction) => {
    const skipAmount = 10;
    let newTime = currentTime + (direction === "forward" ? skipAmount : -skipAmount);
    newTime = Math.max(0, Math.min(newTime, duration));
    setCurrentTime(newTime);
    handleSeek(newTime);
  };

  const handleProgressHover = (e) => {
    if (!progressRef.current || isDragging) return;

    const rect = progressRef.current.getBoundingClientRect();
    const hoverPercent = (e.clientX - rect.left) / rect.width;
    const hoverTimeValue = hoverPercent * duration;

    setHoverTime(hoverTimeValue);
    setHoverPosition(e.clientX - rect.left);
  };

  const handleProgressLeave = () => {
    if (!isDragging) {
      setHoverTime(null);
    }
  };

  const handleSeekMouseDown = (e) => {
    if (!progressRef.current) return;

    setIsDragging(true);
    const rect = progressRef.current.getBoundingClientRect();
    const seekPercent = (e.clientX - rect.left) / rect.width;
    const newTime = seekPercent * duration;

    setCurrentTime(newTime);
    setHoverTime(newTime);
    setHoverPosition(e.clientX - rect.left);
    handleSeek(newTime);

    const onMouseMove = (event) => {
      if (!progressRef.current) return;
      const rect = progressRef.current.getBoundingClientRect();
      const seekPercent = (event.clientX - rect.left) / rect.width;
      const newTime = Math.max(0, Math.min(seekPercent * duration, duration));

      setCurrentTime(newTime);
      setHoverTime(newTime);
      setHoverPosition(event.clientX - rect.left);
      handleSeek(newTime);
    };

    const onMouseUp = () => {
      document.removeEventListener("mousemove", onMouseMove);
      document.removeEventListener("mouseup", onMouseUp);
      setIsDragging(false);
      setHoverTime(null);
    };

    document.addEventListener("mousemove", onMouseMove);
    document.addEventListener("mouseup", onMouseUp);
  };

  const handlePip = () => setPip(true);

  const toggleSettingsMenu = () => {
    setShowSettingsMenu((prev) => !prev);
    setShowPlaybackSpeedOptions(false);
    setShowQualityOptions(false);
  };

  const togglePlaybackSpeedOptions = () => {
    setShowPlaybackSpeedOptions((prev) => !prev);
    setShowQualityOptions(false);
  };

  const toggleQualityOptions = () => {
    setShowQualityOptions((prev) => !prev);
    setShowPlaybackSpeedOptions(false);
  };

  const handlePlaybackSpeed = (speed) => {
    setPlaybackRate(speed);
    setShowPlaybackSpeedOptions(false);
    setShowSettingsMenu(false);
  };

  const handleQualityChange = (bitrate) => {
    changeBitrate(bitrate);
    setShowQualityOptions(false);
    setShowSettingsMenu(false);
  };

  const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);

    const formattedMinutes = minutes.toString().padStart(2, "0");
    const formattedSeconds = secs.toString().padStart(2, "0");

    return hours > 0 ? `${hours}:${formattedMinutes}:${formattedSeconds}` : `${formattedMinutes}:${formattedSeconds}`;
  };

  return (
    <div className="video-wrapper">
      <div
        className="video-timeline"
        ref={progressRef}
        onMouseDown={handleSeekMouseDown}
        onMouseMove={handleProgressHover}
        onMouseLeave={handleProgressLeave}
      >
        <div className="progress-area">
          {hoverTime !== null && <span style={{ left: `${hoverPosition}px` }}>{formatTime(hoverTime)}</span>}
          <div className="progress-bar" style={{ width: `${(currentTime / duration) * 100}%` }}></div>
        </div>
      </div>
      <ul className="video-controls">
        <li className="video-options left">
          <button className="volume" onClick={handleMuteToggle} aria-label="Mute/Unmute">
            {muted ? <VolumeOffIcon /> : <VolumeUpIcon />}
          </button>
          <input type="range" min="0" max="1" step="any" value={muted ? 0 : volume} onChange={handleVolumeChange} aria-label="Adjust Volume" />
          <div className="video-timer">
            <p className="current-time">{formatTime(currentTime)}</p>
            <p className="separator"> / </p>
            <p className="video-duration">{formatTime(duration)}</p>
          </div>
        </li>
        <li className="video-options center">
          <button className="skip-backward" onClick={() => handleSkip("backward")} aria-label="Skip Backward">
            <SkipBackwardIcon />
          </button>
          <button className="play-pause" onClick={togglePlayPause} aria-label="Play/Pause">
            {isPlaying ? <PauseIcon /> : <PlayIcon />}
          </button>
          <button className="skip-forward" onClick={() => handleSkip("forward")} aria-label="Skip Forward">
            <SkipForwardIcon />
          </button>
        </li>
        <li className="video-options right">
          <div className="settings-content" ref={settingsRef}>
            <button className="settings" onClick={toggleSettingsMenu} aria-label="Settings">
              <PlaybackSpeedIcon />
            </button>
            {showSettingsMenu && (
              <ul className="settings-menu">
                <li onClick={togglePlaybackSpeedOptions}>Playback Speed</li>
                <li onClick={toggleQualityOptions}>Quality</li>
                <li>
                  <a href={originalSrc}>Download</a>
                </li>
              </ul>
            )}
            {showPlaybackSpeedOptions && (
              <ul className="speed-options">
                {[0.5, 0.75, 1, 1.5, 2].map((speed) => (
                  <li key={speed} className={speed === playbackRate ? "active" : ""} onClick={() => handlePlaybackSpeed(speed)}>
                    {speed}x
                  </li>
                ))}
              </ul>
            )}
            {showQualityOptions && (
              <ul className="quality-options">
                {bitrateOptions.map((option) => (
                  <li key={option.id} className={option.bitrate === selectedBitrate ? "active" : ""} onClick={() => handleQualityChange(option)}>
                    {option.resolution}
                  </li>
                ))}
              </ul>
            )}
          </div>
          <button className="pic-in-pic" onClick={handlePip} aria-label="Picture in Picture Mode">
            <PiPIcon />
          </button>
          <button className="fullscreen" onClick={() => handleFullscreen()} aria-label="Fullscreen">
            <FullscreenIcon />
          </button>
        </li>
      </ul>
    </div>
  );
};

export default PlayerControls;
