import { translateOr, useTranslate } from "@/shared";
import { Button } from "@/ui";
import { Loader2, X } from "lucide-react";
import { createPortal } from "react-dom";
import { usePolicyDocument, type PolicyDocumentType } from "../hooks/use-policy-document";
import { PolicyDocumentContent } from "./PolicyDocumentContent";

const FALLBACK_TITLES: Record<PolicyDocumentType, string> = {
  privacy: "Privacy Policy",
  terms: "Terms of Use",
};

interface PolicyConsentModalProps {
  readonly type: PolicyDocumentType;
  readonly module?: string;
  readonly onClose: () => void;
  readonly onAccept: () => void;
  readonly onReject: () => void;
}

export function PolicyConsentModal({ type, module = "E4H", onClose, onAccept, onReject }: PolicyConsentModalProps) {
  const { t } = useTranslate();
  const { data: documentData, isLoading } = usePolicyDocument({ type, module });

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 p-4 backdrop-blur-sm">
      <div className="flex max-h-[calc(100vh-64px)] w-full max-w-[960px] flex-col rounded bg-card shadow-lg">
        <div className="flex shrink-0 items-center gap-4 border-b border-border p-4 sm:p-6">
          <h1 className="flex-1 text-xl font-bold text-ink-950 sm:text-2xl">
            {translateOr(t, documentData?.header ?? "", FALLBACK_TITLES[type])}
          </h1>
          <button
            type="button"
            onClick={onClose}
            aria-label={translateOr(t, "CS_COMMON_CLOSE", "Close")}
            className="shrink-0 cursor-pointer text-muted-foreground hover:text-foreground"
          >
            <X className="size-6" />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto p-4 sm:p-6">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="size-6 animate-spin text-muted-foreground" />
            </div>
          ) : (
            <PolicyDocumentContent documentData={documentData} />
          )}
        </div>
        <div className="flex shrink-0 flex-col gap-3 border-t border-border p-4 sm:flex-row sm:justify-end">
          <Button type="button" variant="outline" onClick={onReject} className="sm:min-w-[180px]">
            {translateOr(t, "DIGIT_I_DO_NOT_ACCEPT", "I do not accept")}
          </Button>
          <Button type="button" onClick={onAccept} className="sm:min-w-[180px]">
            {translateOr(t, "DIGIT_I_ACCEPT", "I accept")}
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  );
}
