import { translateOr, useTranslate } from "@/shared";
import { FormControl, FormField, FormItem, FormLabel, FormMessage, Input } from "@/ui";
import { Eye, EyeOff } from "lucide-react";
import { useState, type ReactNode } from "react";
import type { Control, FieldPath, FieldValues } from "react-hook-form";

interface PasswordFormFieldProps<TFieldValues extends FieldValues> {
  readonly control: Control<TFieldValues>;
  readonly name: FieldPath<TFieldValues>;
  readonly label: string;
  readonly placeholder?: string;
  readonly autoComplete?: string;
  readonly headerExtra?: ReactNode;
  readonly disabled?: boolean;
}

export function PasswordFormField<TFieldValues extends FieldValues>({
  control,
  name,
  label,
  placeholder,
  autoComplete = "new-password",
  headerExtra,
  disabled,
}: PasswordFormFieldProps<TFieldValues>) {
  const { t } = useTranslate();
  const [showPassword, setShowPassword] = useState(false);

  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          <div className={headerExtra ? "flex items-baseline justify-between" : undefined}>
            <FormLabel className="text-sm leading-[21px] font-medium text-ink-950">
              {label} <span className="text-destructive">*</span>
            </FormLabel>
            {headerExtra}
          </div>
          <FormControl>
            <div className="relative">
              <Input
                type={showPassword ? "text" : "password"}
                autoComplete={autoComplete}
                placeholder={placeholder}
                disabled={disabled}
                className="h-9 rounded border-ink-300 px-3 py-2 pr-10 text-sm leading-[21px] text-ink-950 placeholder:text-ink-400"
                {...field}
              />
              <button
                type="button"
                onClick={() => setShowPassword((current) => !current)}
                disabled={disabled}
                aria-label={
                  showPassword
                    ? translateOr(t, "CORE_LOGIN_PASSWORD_HIDE", "Hide password")
                    : translateOr(t, "CORE_LOGIN_PASSWORD_SHOW", "Show password")
                }
                className="absolute inset-y-0 right-3 flex cursor-pointer items-center text-ink-400 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {showPassword ? <EyeOff className="size-5" /> : <Eye className="size-5" />}
              </button>
            </div>
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
