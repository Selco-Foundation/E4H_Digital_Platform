const VideoService = {
  download: async (videoUrl, videoName) => {
    const response = await fetch(videoUrl);
    if (!response.ok) throw new Error(`Video download failed: ${response.status}`);

    const blob = await response.blob();
    const extension = new URL(videoUrl, window.location.href).pathname.match(/\.(mp4|webm|mov|m4v|avi|mkv)$/i)?.[1] || "mp4";
    const objectUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");
    try {
      link.href = objectUrl;
      link.download = `${videoName}.${extension}`;
      document.body.appendChild(link);
      link.click();
    } finally {
      link.remove();
      // Allow the browser to start saving before releasing the downloaded data.
      setTimeout(() => URL.revokeObjectURL(objectUrl), 60000);
    }
  },
};

export default VideoService;
