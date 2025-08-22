import React, { useState, useRef, useEffect } from "react";
import ReactPlayer from "react-player";
import PlayerControls from "./PlayerControls";
import Hls from "hls.js";

const HlsPlayer = ({ src, originalSrc, fileStoreId, activeVideoRef }) => {
  const playerRef = useRef(null);
  const containerRef = useRef(null);
  const hlsRef = useRef(null);

  const [isPlaying, setIsPlaying] = useState(false);
  const [volume, setVolume] = useState(0.8);
  const [muted, setMuted] = useState(false);
  const [played, setPlayed] = useState(0);
  const [playbackRate, setPlaybackRate] = useState(1);
  const [duration, setDuration] = useState(0);
  const [pip, setPip] = useState(false);
  const [bitrateOptions, setBitrateOptions] = useState([]);
  const [selectedBitrate, setSelectedBitrate] = useState("Auto");
  const [playerError, setPlayerError] = useState(false);

  // Explicitly requested quality level (-1 = Auto)
  const requestedLevelRef = useRef(-1);

  useEffect(() => {
    const attachHls = async () => {
      if (!Hls.isSupported()) return;

      const video = containerRef.current?.querySelector("video");
      if (!video) return;

      // ensure any previous instance is destroyed before creating a new one
      hlsRef.current?.destroy();

      const srcURL = new URL(src);
      const tenantId = srcURL.pathname.split("/")[1] || "default-tenant";

      const hls = new Hls({
        autoStartLoad: true,
        startLevel: -1,
        xhrSetup: (xhr, url) => {
          if (url.endsWith(".m3u8")) {
            // Manifest route
            const qualityMatch = url.match(/\/hls\/(\d+p)\//);
            const quality = qualityMatch ? qualityMatch[1] : "original";
            const modifiedUrl = `/filestore/v1/files/get-hls?tenantId=${tenantId}&fileStoreId=${fileStoreId}&filename=playlist.m3u8&quality=${quality}`;
            xhr.open("GET", modifiedUrl, true);
          } else if (url.endsWith(".ts")) {
            // Segment route
            const tsFilename = url.split("/").pop();

            let quality = "original";
            const requestedLevel = requestedLevelRef.current;

            if (!hls.levels || hls.levels.length === 0) {
              // No levels known yet—fallback to original
              quality = "original";
            } else {
              // Highest level index by height
              const maxLevel = hls.levels.reduce(
                (max, level) => (level.height > max.height ? level : max),
                { height: 0 }
              );
              const maxLevelIndex = hls.levels.findIndex((level) => level.height === maxLevel.height);

              if (requestedLevel === -1) {
                // Auto/adaptive
                const loadLevel = hls.loadLevel !== -1 ? hls.loadLevel : hls.currentLevel;
                const autoLevel =
                  loadLevel !== -1 && hls.levels[loadLevel] ? hls.levels[loadLevel] : hls.levels[0];

                if (autoLevel) {
                  quality = hls.levels.indexOf(autoLevel) === maxLevelIndex ? "original" : `${autoLevel.height}p`;
                }
              } else if (requestedLevel >= 0 && hls.levels[requestedLevel]) {
                // Manual selection
                quality = requestedLevel === maxLevelIndex ? "original" : `${hls.levels[requestedLevel].height}p`;
              }
            }

            const modifiedTsUrl = `/filestore/v1/files/get-hls?tenantId=${tenantId}&fileStoreId=${fileStoreId}&filename=${tsFilename}&quality=${quality}`;
            xhr.open("GET", modifiedTsUrl, true);
          }
        },
      });

      hlsRef.current = hls;

      hls.loadSource(src);
      hls.attachMedia(video);

      hls.on(Hls.Events.LEVEL_SWITCHED, (_, data) => {
        const currentLevel = data.level;
        if (requestedLevelRef.current === -1) {
          setSelectedBitrate("Auto");
        } else if (hls.levels[currentLevel]) {
          setSelectedBitrate(`${hls.levels[currentLevel].height}p`);
        }
      });

      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        const levels = (hls.levels || []).map((level, index) => ({
          id: index,
          bitrate: level.bitrate,
          resolution: level.height ? `${level.height}p` : "Auto",
        }));
        setBitrateOptions([{ id: -1, bitrate: "Auto", resolution: "Auto" }, ...levels]);

        // Default to Auto
        hls.nextLevel = -1;
        requestedLevelRef.current = -1;
        setSelectedBitrate("Auto");
      });
    };

    const timer = setTimeout(attachHls, 500);

    return () => {
      clearTimeout(timer);
      hlsRef.current?.destroy();
      hlsRef.current = null;
    };
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

    const p = playerRef.current;
    let t = 0;
    if (p && typeof p.getCurrentTime === "function") {
      const ct = p.getCurrentTime();
      t = ct ? ct : 0;
    }
    Digit.Utils.analytics?.trackButtonClick(t > 1 ? "video_resume" : "video_play", {
      page_path: window.location?.pathname || "/",
      page_title: typeof document !== "undefined" && document.title ? document.title : "Video",
      video_src: src,
      current_time: t,
    });
  };

  const handleVideoPause = () => {
    const p = playerRef.current;
    let t = 0;
    if (p && typeof p.getCurrentTime === "function") {
      const ct = p.getCurrentTime();
      t = ct ? ct : 0;
    }
    Digit.Utils.analytics?.trackButtonClick("video_pause", {
      page_path: window.location?.pathname || "/",
      page_title: typeof document !== "undefined" && document.title ? document.title : "Video",
      video_src: src,
      current_time: t,
    });
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
    const hls = hlsRef.current;
    if (hls) {
      // Immediately update requested level
      requestedLevelRef.current = option.id;

      hls.nextLevel = option.id;

      // Reload the current segment at the new quality
      try {
        const currentTime = playerRef.current?.getCurrentTime() || 0;
        hls.startLoad(Math.floor(currentTime));
      } catch (error) {
        console.error("Error reloading stream:", error);
      }

      setSelectedBitrate(option.resolution);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      const video = containerRef.current?.querySelector("video");
      if (!video) return;

      const handlePlayEv = () => setIsPlaying(true);
      const handlePauseEv = () => setIsPlaying(false);

      video.addEventListener("play", handlePlayEv);
      video.addEventListener("pause", handlePauseEv);

      return () => {
        video.removeEventListener("play", handlePlayEv);
        video.removeEventListener("pause", handlePauseEv);
      };
    }, 500);

    return () => clearTimeout(timer);
  }, []);

  if (playerError) {
    return (
      <div className="video-error-fallback">
        <p>This video is still being processed and will be available shortly.</p>
        <p>
          In the meantime, you can{" "}
          <a
            style={{ color: "revert", textDecoration: "revert" }}
            href={originalSrc}
            target="_blank"
            rel="noopener noreferrer"
          >
            download the original file
          </a>
          .
        </p>
      </div>
    );
  }

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
        onPause={handleVideoPause}
        onError={(_, e) => {
          console.debug(e);
          if (e?.type === "networkError" && (e?.details?.includes?.("LoadError") || e?.details?.includes?.("LoadTimeOut"))) {
            setPlayerError(true);
          }
        }}
        config={{
          file: {
            // 👇 avoid double HLS (we manage Hls ourselves)
            forceHLS: false,
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
        handleSeek={(time) => playerRef.current?.seekTo(time, "seconds")}
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
