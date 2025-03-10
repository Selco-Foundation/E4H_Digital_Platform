import React, { useState, useRef, useEffect } from "react";
import ReactPlayer from "react-player";
import PlayerControls from "./PlayerControls";
import Hls from "hls.js";

const HlsPlayer = ({ src, originalSrc, fileStoreId, activeVideoRef }) => {
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
  const [selectedBitrate, setSelectedBitrate] = useState("Auto");
  const [hlsInstance, setHlsInstance] = useState(null);

  // Reference to store the explicitly requested quality level
  const requestedLevelRef = useRef(-1);

  useEffect(() => {
    const attachHls = async () => {
      if (!Hls.isSupported()) return;

      const video = containerRef.current?.querySelector("video");
      if (!video) return;

      // const baseDomain = process.env.REACT_APP_PROXY_ASSETS || "";
      const tenantMatch = src.match(/\/(pg(?:\.[^/]*)?)\//);
      const tenantId = tenantMatch ? tenantMatch[1] : "default-tenant";

      const hls = new Hls({
        autoStartLoad: true,
        startLevel: -1,

        xhrSetup: (xhr, url) => {
          if (url.endsWith(".m3u8")) {
            // Handle manifest URLs
            const qualityMatch = url.match(/\/hls\/(\d+p)\//);
            const quality = qualityMatch ? qualityMatch[1] : "original";

            const modifiedUrl = `/filestore/v1/files/get-hls?tenantId=${tenantId}&fileStoreId=${fileStoreId}&filename=playlist.m3u8&quality=${quality}`;
            // console.debug("Modified Manifest URL:", modifiedUrl);
            xhr.open("GET", modifiedUrl, true);
          } else if (url.endsWith(".ts")) {
            // Extract .ts filename
            const tsFilename = url.split("/").pop();

            // Use the requested level (which changes immediately when user selects a quality)
            let quality = "original";
            const requestedLevel = requestedLevelRef.current;

            // If auto mode is explicitly selected
            if (requestedLevel === -1) {
              // Use the level HLS.js has adaptively selected
              const loadLevel = hls.loadLevel !== -1 ? hls.loadLevel : hls.currentLevel;
              const autoLevel = loadLevel !== -1 && hls.levels[loadLevel] ? hls.levels[loadLevel] : hls.levels[0];

              if (autoLevel) {
                quality = `${autoLevel.height}p`;
              }
            } else if (requestedLevel >= 0 && hls.levels[requestedLevel]) {
              // For manual quality selection, use the explicitly selected level
              quality = `${hls.levels[requestedLevel].height}p`;

              // If this is the highest quality, use "original"
              const maxLevel = hls.levels.reduce((max, level) => (level.height > max.height ? level : max), { height: 0 });

              if (hls.levels[requestedLevel].height === maxLevel.height) {
                quality = "original";
              }
            }

            // console.debug(
            //   "TS Request Quality:",
            //   quality,
            //   "Requested Level:",
            //   requestedLevel,
            //   "HLS Current Level:",
            //   hls.currentLevel,
            //   "HLS Load Level:",
            //   hls.loadLevel
            // );

            const modifiedTsUrl = `/filestore/v1/files/get-hls?tenantId=${tenantId}&fileStoreId=${fileStoreId}&filename=${tsFilename}&quality=${quality}`;
            // console.debug("Modified TS Chunk URL:", modifiedTsUrl);
            xhr.open("GET", modifiedTsUrl, true);
          }
        },
      });

      hls.loadSource(src);
      hls.attachMedia(video);

      // Monitor fragment loading to ensure we're using the correct quality
      hls.on(Hls.Events.FRAG_LOADING, (event, data) => {
        // console.debug("Loading fragment for level:", data.frag.level, "Requested level:", requestedLevelRef.current);
      });

      // Update the UI when quality changes
      hls.on(Hls.Events.LEVEL_SWITCHED, (event, data) => {
        const currentLevel = data.level;
        // console.debug("Level switched to:", currentLevel);

        if (requestedLevelRef.current === -1) {
          setSelectedBitrate("Auto");
        } else if (hls.levels[currentLevel]) {
          setSelectedBitrate(`${hls.levels[currentLevel].height}p`);
        }
      });

      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        if (hls.levels.length === 0) {
          console.warn("No quality levels detected! Make sure you're loading a master playlist.");
        }

        const levels = hls.levels.map((level, index) => ({
          id: index,
          bitrate: level.bitrate,
          resolution: level.height ? `${level.height}p` : "Auto",
        }));

        setBitrateOptions([{ id: -1, bitrate: "Auto", resolution: "Auto" }, ...levels]);

        // Start with Auto quality by default
        hls.nextLevel = -1;
        requestedLevelRef.current = -1;
        setSelectedBitrate("Auto");
      });

      // Monitor level loading to debug issues
      // hls.on(Hls.Events.LEVEL_LOADING, (event, data) => {
      //   console.debug("Loading level:", data.level);
      // });

      setHlsInstance(hls);
    };

    setTimeout(attachHls, 500);

    return () => hlsInstance?.destroy();
  }, [src, fileStoreId]);

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

  const handleFullscreen = () => {
    if (containerRef.current) {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        containerRef.current.requestFullscreen();
      }
    }
  };

  const changeBitrate = (option) => {
    if (hlsInstance) {
      // Immediately update our requested level reference
      requestedLevelRef.current = option.id;

      hlsInstance.nextLevel = option.id;

      // Force a reload of the current segment at the new quality level
      try {
        const currentTime = playerRef.current?.getCurrentTime() || 0;
        hlsInstance.startLoad(Math.floor(currentTime));
      } catch (error) {
        console.error("Error reloading stream:", error);
      }

      setSelectedBitrate(option.resolution);
      // console.debug("Quality changed to:", option.resolution, "Level ID:", option.id);
    }
  };

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
        onProgress={({ played }) => setPlayed(played)}
        onDuration={setDuration}
        onDisablePIP={() => setPip(false)}
        onPlay={handleVideoPlay}
        config={{
          file: {
            forceHLS: true,
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
        originalSrc={originalSrc}
      />
    </div>
  );
};

export default HlsPlayer;
