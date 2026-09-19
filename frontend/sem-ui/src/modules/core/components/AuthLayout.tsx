import { getConfig, translateOr, useLoginBannerImages, useTranslate } from "@/shared";
import { LanguageSwitcher } from "@/ui";
import type { ReactNode } from "react";
import { LoginCarousel } from "./LoginCarousel";

interface AuthLayoutProps {
  readonly title: string;
  readonly subtitle: ReactNode;
  readonly children: ReactNode;
}

export function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  const bannerImages = useLoginBannerImages();
  const logos = getConfig("LOGO_LIST") as Array<{ url: string; alt: string }> | undefined;
  const logo = logos?.[0];
  const { t } = useTranslate();
  const logoAlt = logo?.alt ?? translateOr(t, "CORE_LOGO_ALT", "Selco Foundation Logo");

  return (
    <div className="font-poppins flex min-h-screen bg-white">
      <div className="relative flex min-h-screen w-full flex-col items-center px-6 py-10 lg:w-[60%] lg:min-w-[480px] lg:justify-center lg:px-8 lg:py-8">
        <div className="absolute inset-x-8 top-8 hidden items-center justify-between lg:flex">
          <img
            src={logo?.url}
            alt={logoAlt}
            className="h-[68px] w-auto object-contain"
          />
          <LanguageSwitcher />
        </div>

        <div className="absolute top-4 right-4 lg:hidden">
          <LanguageSwitcher />
        </div>

        <div className="mt-20 flex w-full max-w-[360px] flex-col gap-5 lg:mt-0">
          <img
            src={logo?.url}
            alt={logoAlt}
            className="mx-auto h-28 w-auto object-contain lg:hidden"
          />

          <div className="flex flex-col gap-1 text-center lg:text-left">
            <h1 className="text-[28px] font-semibold leading-[40px] text-ink-950 lg:text-[32px] lg:leading-[48px]">
              {title}
            </h1>
            <p className="text-sm leading-[21px] text-ink-600">{subtitle}</p>
          </div>

          {children}
        </div>
      </div>

      <div className="hidden py-6 pr-6 lg:block lg:w-[40%] lg:min-w-[520px]">
        <LoginCarousel slides={bannerImages} />
      </div>
    </div>
  );
}
