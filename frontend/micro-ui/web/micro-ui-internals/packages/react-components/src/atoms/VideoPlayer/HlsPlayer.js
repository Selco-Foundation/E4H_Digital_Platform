import React, { useState, useRef, useEffect } from "react";
import ReactPlayer from "react-player";
import PlayerControls from "./PlayerControls";

const HlsPlayer = ({ src }) => {
  const playerRef = useRef(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [volume, setVolume] = useState(0.8);
  const [muted, setMuted] = useState(false);
  const [played, setPlayed] = useState(0);
  const [playbackRate, setPlaybackRate] = useState(1);
  const [duration, setDuration] = useState(0);
  const [pip, setPip] = useState(false);
  const [bitrateOptions, setBitrateOptions] = useState([]);
  const [selectedBitrate, setSelectedBitrate] = useState(null);
  const [hlsInstance, setHlsInstance] = useState(null);

  const handleReady = (player) => {
    const internalPlayer = playerRef.current?.getInternalPlayer("hls");
    console.debug("HLS Internal Player:", internalPlayer);

    if (internalPlayer) {
      setHlsInstance(internalPlayer);

      // ✅ If levels are already available, use them immediately
      if (internalPlayer.levels && internalPlayer.levels.length > 0) {
        console.debug("Levels already available, fetching bitrates...");
        const bitrates = internalPlayer.levels.map((level, index) => ({
          id: index,
          bitrate: level.bitrate,
          //   resolution: `${level.width}x${level.height}`,
        }));
        console.debug("Available Bitrates:", bitrates);
        setBitrateOptions(bitrates);
        return; // ✅ No need to wait for MANIFEST_PARSED
      }

      // ✅ Register event listener before it's too late
      internalPlayer.on(Hls.Events.MANIFEST_PARSED, (_, data) => {
        console.debug("MANIFEST_PARSED Event Triggered:", data);
        if (data?.levels) {
          const bitrates = data.levels.map((level, index) => ({
            id: index,
            bitrate: level.bitrate,
            resolution: `${level.width}x${level.height}`,
          }));
          console.debug("Available Bitrates:", bitrates);
          setBitrateOptions(bitrates);
        }
      });
    }
  };

  const changeBitrate = (bitrate) => {
    if (hlsInstance) {
      const levelIndex = bitrate !== null ? hlsInstance.levels.findIndex((level) => level.bitrate === bitrate) : -1;
      hlsInstance.currentLevel = levelIndex;
      setSelectedBitrate(bitrate);
    }
  };

  return (
    <div className="video-container show-controls">
      <ReactPlayer
        ref={playerRef}
        url={src}
        playing={isPlaying}
        volume={volume}
        muted={muted}
        playbackRate={playbackRate}
        pip={pip}
        width="100%"
        height="100%"
        onReady={handleReady}
        onProgress={({ played }) => setPlayed(played)}
        onDuration={setDuration}
        onDisablePIP={() => setPip(false)}
      />
      <PlayerControls
        isPlaying={isPlaying}
        setIsPlaying={setIsPlaying}
        volume={volume}
        setVolume={setVolume}
        muted={muted}
        setMuted={setMuted}
        playbackRate={playbackRate}
        setPlaybackRate={setPlaybackRate}
        setPip={setPip}
        played={played}
        duration={duration}
        handleSeek={(time) => playerRef.current.seekTo(time, "seconds")}
        bitrateOptions={bitrateOptions}
        selectedBitrate={selectedBitrate}
        changeBitrate={changeBitrate}
      />
    </div>
  );
};

export default HlsPlayer;