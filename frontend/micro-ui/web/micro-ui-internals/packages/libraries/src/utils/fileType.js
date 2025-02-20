const getFileTypeFromFileStoreURL = (filestoreURL) => {
  if (filestoreURL.includes(".pdf")) return "pdf";
  if (filestoreURL.includes(".mp4")) return "video/mp4";
  if (filestoreURL.includes(".avi")) return "video/avi";
  if (filestoreURL.includes(".mov")) return "video/mov";
  if (filestoreURL.includes(".wmv")) return "video/wmv";
  if (filestoreURL.includes(".jpg") || filestoreURL.includes(".jpeg") || filestoreURL.includes(".png") || filestoreURL.includes(".webp"))
    return "image";
  else return "image";
};

export default getFileTypeFromFileStoreURL;
