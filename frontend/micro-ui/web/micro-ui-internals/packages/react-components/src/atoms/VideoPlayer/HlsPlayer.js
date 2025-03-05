import React, { useState, useRef, useEffect } from "react";
import ReactPlayer from "react-player";
import PlayerControls from "./PlayerControls";
import Hls from "hls.js";

const HlsPlayer = ({ src, activeVideoRef }) => {
  const playerRef = useRef(null);
  const containerRef = useRef(null);
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

  // ✅ Manually Attach HLS.js and Modify Chunk URLs
  useEffect(() => {
    const attachHls = () => {
      const video = containerRef.current?.querySelector("video");
      if (!video || !Hls.isSupported()) return;

      const hls = new Hls({
        xhrSetup: (xhr, url) => {
          console.debug("Original Chunk URL:", url);

          // ✅ Modify the chunk URL dynamically
          let modifiedUrl = url;
          if (!url.includes("SignedHeaders")) {
            modifiedUrl =
              url.replace("https://selco-dev.s3.ap-south-1.amazonaws.com/pg.bagalkot", "https://e4h-dev.selcofoundation.org/filestore/v1/files") +
              "?tenantId=pg.bagalkot";
          }

          // ✅ Set Custom Headers
          xhr.setRequestHeader("Authorization", window.localStorage.getItem("token"));
          xhr.open("GET", modifiedUrl, true);
          console.debug("Modified Chunk URL:", modifiedUrl);
        },
      });

      hls.loadSource(src);
      hls.attachMedia(video);

      // ✅ Fetch Available Bitrates
      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        const bitrates = hls.levels.map((level, index) => ({
          id: index,
          bitrate: level.bitrate,
          resolution: level.height ? `${level.height}p` : "Auto",
        }));
        console.debug("Available Bitrates:", bitrates);
        setBitrateOptions(bitrates);
      });

      setHlsInstance(hls);
    };

    setTimeout(attachHls, 500);
    return () => hlsInstance?.destroy();
  }, [src]);

  // ✅ Handle Play/Pause Tracking
  const handleVideoPlay = () => {
    let videoElement = playerRef.current?.getInternalPlayer();
    if (!videoElement || !(videoElement instanceof HTMLVideoElement)) {
      videoElement = containerRef.current?.querySelector("video");
    }
    if (!videoElement) return;

    if (activeVideoRef.current && activeVideoRef.current !== videoElement) {
      activeVideoRef.current.pause();
    }

    activeVideoRef.current = videoElement;
  };

  // ✅ Handle Player Ready Event
  // const handleReady = (player) => {
  //   const internalPlayer = playerRef.current?.getInternalPlayer("hls");
  //   if (internalPlayer) {
  //     setHlsInstance(internalPlayer);

  //     if (internalPlayer.levels?.length > 0) {
  //       const bitrates = internalPlayer.levels.map((level, index) => ({
  //         id: index,
  //         bitrate: level.bitrate,
  //       }));
  //       setBitrateOptions(bitrates);
  //       return;
  //     }

  //     internalPlayer.on(Hls.Events.MANIFEST_PARSED, (_, data) => {
  //       if (data?.levels) {
  //         const bitrates = data.levels.map((level, index) => ({
  //           id: index,
  //           bitrate: level.bitrate,
  //           resolution: `${level.width}x${level.height}`,
  //         }));
  //         setBitrateOptions(bitrates);
  //       }
  //     });
  //   }
  // };

  // ✅ Handle Fullscreen Toggle
  const handleFullscreen = () => {
    if (containerRef.current) {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        containerRef.current.requestFullscreen();
      }
    }
  };

  // ✅ Handle Bitrate Change
  const changeBitrate = (bitrate) => {
    if (hlsInstance) {
      const levelIndex = bitrate !== null ? hlsInstance.levels.findIndex((level) => level.bitrate === bitrate) : -1;
      hlsInstance.currentLevel = levelIndex;
      setSelectedBitrate(bitrate);
    }
  };

  // ✅ Track Play/Pause State
  useEffect(() => {
    const attachEventListeners = () => {
      setTimeout(() => {
        const video = containerRef.current?.querySelector("video");
        if (!video) return;

        const handlePlay = () => setIsPlaying(true);
        const handlePause = () => setIsPlaying(false);

        video.addEventListener("play", handlePlay);
        video.addEventListener("pause", handlePause);

        return () => {
          video.removeEventListener("play", handlePlay);
          video.removeEventListener("pause", handlePause);
        };
      }, 500);
    };

    attachEventListeners();
  }, []);

  return (
    <div ref={containerRef} className="video-container show-controls">
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
        // onReady={handleReady}
        onProgress={({ played }) => setPlayed(played)}
        onDuration={setDuration}
        onDisablePIP={() => setPip(false)}
        onPlay={handleVideoPlay}
        config={{
          file: {
            forceHLS: true, // ✅ Forces HLS but lets our manual HLS instance handle it
          },
        }}
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
        handleFullscreen={handleFullscreen}
      />
    </div>
  );
};

export default HlsPlayer;
