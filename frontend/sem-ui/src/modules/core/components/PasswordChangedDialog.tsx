import { translateOr, useTranslate } from "@/shared";
import { Button } from "@/ui";
import { CheckCircle2 } from "lucide-react";
import { createPortal } from "react-dom";

interface PasswordChangedDialogProps {
  readonly onConfirm: () => void;
}

export function PasswordChangedDialog({ onConfirm }: PasswordChangedDialogProps) {
  const { t } = useTranslate();

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 p-4 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-3xl border border-border bg-card p-8 text-center shadow-lg">
        <div className="mx-auto mb-4 flex size-16 items-center justify-center rounded-full bg-accent text-primary">
          <CheckCircle2 className="size-8" />
        </div>

        <h2 className="text-xl font-semibold text-foreground">
          {translateOr(t, "CORE_CHANGE_PASSWORD_SUCCESS_TITLE", "Password updated successfully")}
        </h2>

        <p className="mt-2 text-sm text-muted-foreground">
          {translateOr(
            t,
            "CORE_CHANGE_PASSWORD_SUCCESS_DESC",
            "Please log in again using your new password.",
          )}
        </p>

        <div className="mt-8 flex justify-center">
          <Button size="lg" onClick={onConfirm}>
            {translateOr(t, "CORE_COMMON_OK", "OK")}
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  );
}
