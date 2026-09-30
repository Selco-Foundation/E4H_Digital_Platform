import { setLocale, translateOr, useLanguages, useLocaleStore, useTranslate } from "@/shared";
import { useState } from "react";
import { ChevronDown, Languages } from "lucide-react";
import { Button } from "./ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";
import { toast } from "sonner";

interface LanguageSwitcherProps {
  compact?: boolean;
}

export function LanguageSwitcher({ compact = false }: LanguageSwitcherProps = {}) {
  const currentLocale = useLocaleStore((state) => state.locale);
  const languages = useLanguages();
  const [isSwitching, setIsSwitching] = useState(false);
  const { t } = useTranslate();

  const current =
    languages.find((language) => language.code === currentLocale) ?? languages[0];

  const handleSelect = async (code: string) => {
    if (code === currentLocale || isSwitching) {
      return;
    }

    setIsSwitching(true);
    try {
      await setLocale(code);
    } catch (error) {
      toast.error(translateOr(t, "CORE_LANGUAGE_SWITCH_FAILURE_TOAST", "Failed to change language"), {
        description: error instanceof Error
          ? error.message
          : translateOr(t, "CORE_LANGUAGE_SWITCH_ERROR_GENERIC", "Please try again."),
      });
    } finally {
      setIsSwitching(false);
    }
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        {compact ? (
          <Button
            variant="ghost"
            size="icon-sm"
            disabled={isSwitching}
            aria-label={current?.nativeLabel ?? translateOr(t, "CORE_LANGUAGE_FALLBACK", "Language")}
            className="text-current hover:bg-white/10 hover:text-current"
          >
            <Languages className="size-5" />
          </Button>
        ) : (
          <Button variant="outline" size="sm" disabled={isSwitching} className="gap-1">
            <Languages className="size-4 opacity-60" />
            <span className="hidden lg:inline">{current?.nativeLabel ?? translateOr(t, "CORE_LANGUAGE_FALLBACK", "Language")}</span>
            <ChevronDown className="size-4 opacity-60" />
          </Button>
        )}
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        {languages.map((language) => (
          <DropdownMenuItem
            key={language.code}
            className="cursor-pointer"
            onClick={() => void handleSelect(language.code)}
          >
            {language.nativeLabel}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
