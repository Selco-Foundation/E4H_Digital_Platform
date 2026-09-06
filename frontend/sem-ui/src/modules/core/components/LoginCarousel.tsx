import { translateOr, useTranslate, type LoginBannerImage } from "@/shared";
import { cn } from "@/ui";
import { ChevronLeft, ChevronRight, ImageIcon } from "lucide-react";
import { useEffect, useState } from "react";

interface LoginCarouselProps {
  readonly slides: LoginBannerImage[];
}

const MAX_VISIBLE_DOTS = 4;

function getVisibleDotIndices(count: number, activeIndex: number, max: number): number[] {
  if (count <= max) {
    return Array.from({ length: count }, (_, index) => index);
  }

  const start = Math.min(Math.max(activeIndex - Math.floor(max / 2), 0), count - max);
  return Array.from({ length: max }, (_, offset) => start + offset);
}

interface OutgoingSlide {
  slide: LoginBannerImage;
  direction: "next" | "prev";
  key: number;
}

export function LoginCarousel({ slides }: LoginCarouselProps) {
  const { t } = useTranslate();
  const [activeIndex, setActiveIndex] = useState(0);
  const [direction, setDirection] = useState<"next" | "prev">("next");
  const [outgoing, setOutgoing] = useState<OutgoingSlide | null>(null);
  const slideCount = slides.length;
  const activeSlide = slideCount > 0 ? slides[activeIndex % slideCount] : undefined;
  const visibleDotIndices = getVisibleDotIndices(slideCount, activeIndex, MAX_VISIBLE_DOTS);

  const transitionTo = (index: number, nextDirection: "next" | "prev") => {
    if (activeSlide) {
      setOutgoing({ slide: activeSlide, direction: nextDirection, key: activeIndex });
    }
    setDirection(nextDirection);
    setActiveIndex(index);
  };

  const goToPrevious = () => {
    transitionTo((activeIndex - 1 + slideCount) % slideCount, "prev");
  };

  const goToNext = () => {
    transitionTo((activeIndex + 1) % slideCount, "next");
  };

  const goToSlide = (index: number) => {
    transitionTo(index, index > activeIndex ? "next" : "prev");
  };

  useEffect(() => {
    if (slideCount <= 1) {
      return;
    }

    const id = setInterval(goToNext, 7000);
    return () => clearInterval(id);
  }, [activeIndex, slideCount]);

  useEffect(() => {
    slides.forEach((slide) => {
      const preload = new Image();
      preload.src = slide.image;
    });
  }, [slides]);

  return (
    <div className="relative h-full w-full overflow-hidden rounded-[20px] bg-muted">
      {activeSlide ? (
        <>
          {outgoing && (
            <img
              key={outgoing.key}
              src={outgoing.slide.image}
              alt={outgoing.slide.discription}
              onAnimationEnd={() => setOutgoing(null)}
              className={cn(
                "absolute inset-0 h-full w-full animate-out object-cover duration-1500 ease-out",
                outgoing.direction === "next" ? "slide-out-to-left" : "slide-out-to-right",
              )}
            />
          )}
          <img
            key={activeIndex}
            src={activeSlide.image}
            alt={activeSlide.discription}
            className={cn(
              "absolute inset-0 h-full w-full animate-in object-cover duration-1500 ease-out",
              direction === "next" ? "slide-in-from-right" : "slide-in-from-left",
            )}
          />
        </>
      ) : (
        <div className="flex h-full w-full items-center justify-center">
          <ImageIcon className="size-16 text-ink-400" aria-hidden="true" />
        </div>
      )}

      {slideCount > 1 && (
        <div className="absolute inset-x-8 bottom-6 flex items-center justify-center gap-10">
          <button
            type="button"
            aria-label={translateOr(t, "CORE_LOGIN_CAROUSEL_PREVIOUS", "Previous slide")}
            onClick={goToPrevious}
            className="flex size-9 cursor-pointer items-center justify-center text-ink-950 transition-colors hover:text-primary"
          >
            <ChevronLeft className="size-6" strokeWidth={2} />
          </button>

          <div className="flex items-center gap-3">
            {visibleDotIndices.map((index) => (
              <button
                key={index}
                type="button"
                aria-label={translateOr(
                  t,
                  "CORE_LOGIN_CAROUSEL_GOTO",
                  "Go to slide {SLIDE_NUMBER}",
                ).replace("{SLIDE_NUMBER}", String(index + 1))}
                onClick={() => goToSlide(index)}
                className={cn(
                  "size-3 cursor-pointer rounded-full transition-colors",
                  index === activeIndex ? "bg-white" : "bg-primary",
                )}
              />
            ))}
          </div>

          <button
            type="button"
            aria-label={translateOr(t, "CORE_LOGIN_CAROUSEL_NEXT", "Next slide")}
            onClick={goToNext}
            className="flex size-9 cursor-pointer items-center justify-center text-ink-950 transition-colors hover:text-primary"
          >
            <ChevronRight className="size-6" strokeWidth={2} />
          </button>
        </div>
      )}
    </div>
  );
}
