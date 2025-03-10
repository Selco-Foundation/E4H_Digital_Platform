const getFileTypeFromFileStoreURL = (filestoreURL) => {
  if (typeof filestoreURL === "object") {
    if (filestoreURL?.master?.includes(".m3u8")) return "hls";
  }
  if (filestoreURL.includes(".pdf")) return "pdf";
  if (filestoreURL.includes(".jpg") || filestoreURL.includes(".jpeg") || filestoreURL.includes(".png") || filestoreURL.includes(".webp"))
    return "image";
  else return "image";
};

export default getFileTypeFromFileStoreURL;
