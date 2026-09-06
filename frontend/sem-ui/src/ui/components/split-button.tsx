import * as React from "react"
import { ChevronDown } from "lucide-react"

import { cn } from "@/ui/lib/utils"

interface SplitButtonProps {
  readonly label: React.ReactNode
  readonly onLabelClick?: React.MouseEventHandler<HTMLButtonElement>
  readonly onTriggerClick?: React.MouseEventHandler<HTMLButtonElement>
  readonly variant?: "default" | "outline"
  readonly size?: "sm" | "default" | "lg"
  readonly disabled?: boolean
  readonly className?: string
  readonly triggerAriaLabel?: string
  readonly triggerAriaExpanded?: boolean
}

// Figma "Button / Primary Split" and "Button / Secondary Split" — two joined
// segments (label + chevron trigger) sharing one pill radius. Sizes mirror
// Button's scale: sm=32px/6px, default=44px/10px (Large), lg=40px/8px (Medium).
const segmentBase =
  "inline-flex cursor-pointer items-center justify-center font-poppins font-semibold whitespace-nowrap transition-all outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:cursor-not-allowed"

const SIZE_CLASSES = {
  sm: { height: "h-8", radiusL: "rounded-l-md", radiusR: "rounded-r-md", labelPx: "px-3", triggerPx: "px-2", text: "text-sm", gap: "gap-1.5" },
  default: { height: "h-11", radiusL: "rounded-l-[10px]", radiusR: "rounded-r-[10px]", labelPx: "px-6", triggerPx: "px-3", text: "text-lg", gap: "gap-2" },
  lg: { height: "h-10", radiusL: "rounded-l-lg", radiusR: "rounded-r-lg", labelPx: "px-4", triggerPx: "px-3", text: "text-sm", gap: "gap-2" },
} as const

function SplitButton({
  label,
  onLabelClick,
  onTriggerClick,
  variant = "default",
  size = "default",
  disabled,
  className,
  triggerAriaLabel = "More actions",
  triggerAriaExpanded,
}: SplitButtonProps) {
  const handleTrigger = onTriggerClick ?? onLabelClick
  const s = SIZE_CLASSES[size]

  // Enabled/Hover/Disabled render as one uniform color across both segments;
  // only Pressed splits into two shades (label vs. a darker trigger segment).
  const labelClasses = cn(
    segmentBase,
    "flex-1",
    s.height,
    s.radiusL,
    s.labelPx,
    s.text,
    s.gap,
    variant === "default"
      ? "bg-brand-primary text-neutral-25 group-hover:bg-primary-700 group-active:bg-primary-700 disabled:bg-neutral-300 disabled:text-neutral-25"
      : "border border-r-0 border-brand-primary bg-neutral-25 text-brand-primary group-hover:border-primary-700 group-hover:bg-primary-400 group-hover:text-neutral-25 group-active:border-primary-700 group-active:bg-primary-400 group-active:text-neutral-25 disabled:border-neutral-300 disabled:text-neutral-300"
  )

  // Wrapper carries the trigger segment's background/border so the 1px
  // divider line doesn't leave a gap in the pill's fill.
  const dividerWrapClasses = cn(
    "flex shrink-0 items-center justify-center",
    s.height,
    variant === "default"
      ? "bg-brand-primary group-hover:bg-primary-700 group-active:bg-primary-800 disabled:bg-neutral-300"
      : "border-y border-brand-primary bg-neutral-25 group-hover:border-primary-700 group-hover:bg-primary-400 group-active:border-primary-800 group-active:bg-primary-700 disabled:border-neutral-300 disabled:bg-neutral-25",
  )

  const dividerClasses = cn(
    "h-6 w-px shrink-0",
    variant === "default" ? "bg-neutral-25" : "bg-neutral-400",
  )

  const triggerClasses = cn(
    segmentBase,
    s.height,
    s.radiusR,
    s.triggerPx,
    variant === "default"
      ? "bg-brand-primary text-neutral-25 group-hover:bg-primary-700 group-active:bg-primary-800 disabled:bg-neutral-300"
      : "border border-l-0 border-brand-primary bg-neutral-25 text-brand-primary group-hover:border-primary-700 group-hover:bg-primary-400 group-hover:text-neutral-25 group-active:border-primary-800 group-active:bg-primary-700 group-active:text-neutral-25 disabled:border-neutral-300 disabled:text-neutral-300"
  )

  return (
    <div data-slot="split-button" className={cn("group inline-flex", className)}>
      <button
        type="button"
        data-slot="split-button-label"
        className={labelClasses}
        onClick={onLabelClick}
        disabled={disabled}
      >
        {label}
      </button>
      <span aria-hidden="true" className={dividerWrapClasses}>
        <span className={dividerClasses} />
      </span>
      <button
        type="button"
        data-slot="split-button-trigger"
        className={triggerClasses}
        onClick={handleTrigger}
        disabled={disabled}
        aria-label={triggerAriaLabel}
        aria-haspopup="menu"
        aria-expanded={triggerAriaExpanded}
      >
        <ChevronDown
          className={cn("size-5 transition-transform", triggerAriaExpanded && "rotate-180")}
        />
      </button>
    </div>
  )
}

export { SplitButton }
export type { SplitButtonProps }
