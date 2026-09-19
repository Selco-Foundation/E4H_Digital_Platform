import { I18nProvider, QueryProvider } from "@/shared";
import { Toaster, TooltipProvider } from "@/ui";
import { RouterProvider } from "@tanstack/react-router";
import { createAppRouter } from "./router";

const router = createAppRouter();

export function App() {
  return (
    <TooltipProvider>
      <I18nProvider>
        <QueryProvider>
          <RouterProvider router={router} />
          <Toaster />
        </QueryProvider>
      </I18nProvider>
    </TooltipProvider>
  );
}
