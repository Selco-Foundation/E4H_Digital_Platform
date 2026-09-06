export function formatEpochToDate(epoch?: number): string {
  if (!epoch) {
    return "-";
  }
  return new Date(epoch).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}
