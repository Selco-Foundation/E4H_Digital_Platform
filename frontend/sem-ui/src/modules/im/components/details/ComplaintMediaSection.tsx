import { translateOr, useTranslate } from "@/shared";
import { ImageIcon } from "lucide-react";
import { FormSectionCard } from "../create/FormSectionCard";
import { ComplaintMediaList, type ComplaintVideoEntry } from "./ComplaintMediaList";

interface ComplaintMediaSectionProps {
  images: string[];
  videos: ComplaintVideoEntry[];
}

export function ComplaintMediaSection({
  images,
  videos,
}: ComplaintMediaSectionProps) {
  const { t } = useTranslate();

  if (!images.length && !videos.length) {
    return null;
  }

  return (
    <FormSectionCard
      icon={ImageIcon}
      title={translateOr(t, "CS_COMMON_ATTACHMENTS", "Attachments")}
      titleClassName="text-base font-semibold text-ink-950"
      divider
    >
      <ComplaintMediaList images={images} videos={videos} />
    </FormSectionCard>
  );
}
