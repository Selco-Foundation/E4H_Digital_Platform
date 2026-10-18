import { Link } from "@tanstack/react-router";
import type { ReactNode } from "react";
import { cn } from "../lib/utils";
import { Card } from "./ui/card";

export interface StatTileProps {
  icon: ReactNode;
  iconClassName?: string;
  label: string;
  value: string | number;
  link?: string;
}

export function StatTile({ icon, iconClassName, label, value, link }: StatTileProps) {
  const card = (
    <Card className="livelihood-card min-w-0 flex-1 flex-col items-center gap-2 border-border px-3 py-4 text-center shadow-sm lg:max-w-xs lg:min-w-64 lg:flex-none lg:flex-row lg:items-center lg:gap-3 lg:px-4 lg:py-5 lg:text-left">
      <div
        className={cn(
          "flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary lg:h-12 lg:w-12",
          iconClassName,
        )}
      >
        {icon}
      </div>
      <div>
        <p className="text-xs leading-4 font-normal text-ink-600 lg:text-base lg:leading-6">
          {label}
        </p>
        <p className="text-xl leading-7 font-normal text-ink-950 lg:text-2xl lg:leading-9">
          {value}
        </p>
      </div>
    </Card>
  );

  if (!link) {
    return card;
  }

  return (
    <Link
      to={link}
      className="block min-w-0 flex-1 transition-opacity hover:opacity-90 lg:max-w-xs lg:min-w-64 lg:flex-none"
    >
      {card}
    </Link>
  );
}
