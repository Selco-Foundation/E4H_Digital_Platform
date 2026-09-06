import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { Slot } from "radix-ui"

import { cn } from "@/ui/lib/utils"

const buttonVariants = cva(
  "inline-flex shrink-0 cursor-pointer items-center justify-center gap-2 font-poppins font-semibold whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
  {
    variants: {
      variant: {
        // Figma "Button / Primary": brand-primary fill, hover primary-700, pressed primary-800, disabled neutral-300
        default:
          "bg-brand-primary text-neutral-25 hover:bg-primary-700 active:bg-primary-800 disabled:bg-neutral-300 disabled:text-neutral-25",
        destructive:
          "bg-destructive text-white hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:bg-destructive/60 dark:focus-visible:ring-destructive/40 disabled:opacity-50",
        // Figma "Button / Secondary": brand-primary outline, fills primary-400/700 on hover/pressed, disabled neutral-300
        outline:
          "border border-brand-primary bg-neutral-25 text-brand-primary hover:border-primary-700 hover:bg-primary-400 hover:text-neutral-25 active:border-primary-800 active:bg-primary-700 active:text-neutral-25 disabled:border-neutral-300 disabled:bg-neutral-25 disabled:text-neutral-300",
        secondary:
          "bg-secondary text-secondary-foreground hover:bg-secondary/80 disabled:opacity-50",
        ghost:
          "hover:bg-accent hover:text-accent-foreground dark:hover:bg-accent/50 disabled:opacity-50",
        // Figma "Button / Tertiary": brand-primary text, underline on hover/pressed, disabled neutral-300
        link: "text-brand-primary underline-offset-4 hover:underline hover:text-brand-primary active:underline active:text-primary-800 disabled:text-neutral-300",
      },
      size: {
        // Figma "Large Button" / main Primary-Secondary-Tertiary spec: 44px, 10px radius, 8px 24px padding, 18px/600 label
        default: "h-11 rounded-[10px] px-6 py-2 text-lg has-[>svg]:px-6",
        xs: "h-6 gap-1 rounded-md px-2 text-xs has-[>svg]:px-1.5 [&_svg:not([class*='size-'])]:size-3",
        // Figma "Small Button": 32px, 6px radius, 12px padding
        sm: "h-8 gap-1.5 rounded-md px-3 text-sm has-[>svg]:px-2.5",
        // Figma "Medium Button": 40px, 8px radius, 16px padding
        lg: "h-10 rounded-lg px-4 text-sm has-[>svg]:px-4",
        icon: "size-9 rounded-md",
        "icon-xs": "size-6 rounded-md [&_svg:not([class*='size-'])]:size-3",
        "icon-sm": "size-8 rounded-md",
        "icon-lg": "size-10 rounded-md",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

function Button({
  className,
  variant = "default",
  size = "default",
  asChild = false,
  ...props
}: React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean
  }) {
  const Comp = asChild ? Slot.Root : "button"

  return (
    <Comp
      data-slot="button"
      data-variant={variant}
      data-size={size}
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
    />
  )
}

export { Button, buttonVariants }
