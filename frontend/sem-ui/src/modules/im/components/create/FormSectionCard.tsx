import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";

interface FormSectionCardProps {
  icon: LucideIcon;
  title: string;
  description?: string;
  titleClassName?: string;
  divider?: boolean;
  children: ReactNode;
}

export function FormSectionCard({
  icon: Icon,
  title,
  description,
  titleClassName,
  divider,
  children,
}: FormSectionCardProps) {
  const alignmentClass = description ? "items-start" : "items-center";

  return (
    <section className="livelihood-card p-6">
      <div className={divider ? `flex ${alignmentClass} gap-3` : `mb-6 flex ${alignmentClass} gap-3`}>
        <div className="flex size-11 shrink-0 items-center justify-center rounded-lg bg-accent text-primary">
          <Icon className="size-5" />
        </div>
        <div>
          <h2 className={titleClassName ?? "text-lg font-semibold text-foreground"}>{title}</h2>
          {description ? (
            <p className="text-sm text-muted-foreground">{description}</p>
          ) : null}
        </div>
      </div>
      {divider ? <hr className="my-4 border-t border-border" /> : null}
      {children}
    </section>
  );
}
