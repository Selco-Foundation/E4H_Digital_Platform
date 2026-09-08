import { translateOr, useTranslate } from "@/shared";
import type { PolicyDescription, PolicyDocument } from "../hooks/use-policy-document";

interface PolicyDocumentContentProps {
  documentData?: PolicyDocument;
}

function scrollToElement(id: string) {
  if (typeof document === "undefined") {
    return;
  }
  document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
}

function DescriptionMarker({ type, index }: { type: PolicyDescription["type"]; index: number }) {
  if (type === "points") {
    return <span className="mr-2 shrink-0">&#8226;</span>;
  }
  if (type === "step") {
    return <span className="mr-2 shrink-0">{index + 1}.</span>;
  }
  return null;
}

/**
 * Matches DIGIT-UI's PolicyDocumentContent.js exactly: a table of contents
 * built from each content's `header`, then each section rendered with its
 * descriptions — bullet points (`type: "points"`), numbered steps
 * (`type: "step"`), plain text, and one level of nested sub-descriptions.
 */
export function PolicyDocumentContent({ documentData }: PolicyDocumentContentProps) {
  const { t } = useTranslate();
  const contents = documentData?.contents ?? [];

  if (!contents.length) {
    return (
      <p className="text-sm text-muted-foreground">
        {translateOr(t, "ES_COMMON_NO_DATA", "No data")}
      </p>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <p className="mb-2 text-sm font-semibold text-ink-950">
          {translateOr(t, "DIGIT_TABLE_OF_CONTENTS", "Table of Contents")}
        </p>
        <ul className="space-y-1">
          {contents.map((content, index) => (
            <li key={index} className="flex items-center gap-2">
              <span>{index + 1}.</span>
              <button
                type="button"
                onClick={() => scrollToElement(content.header)}
                className="cursor-pointer text-left text-destructive underline"
              >
                {translateOr(t, content.header, content.header)}
              </button>
            </li>
          ))}
        </ul>
      </div>

      {contents.map((content, index) => (
        <div key={index} id={content.header}>
          <div className={`font-bold text-ink-950 ${content.isSpaceRequired ? "pl-4" : ""}`}>
            {translateOr(t, content.header, content.header)}
          </div>
          {(content.descriptions ?? []).map((description, subIndex) => (
            <div
              key={subIndex}
              className={`mb-2 ${description.isSpaceRequired ? "pl-4" : ""}`}
            >
              <div className={`flex items-center ${description.isBold ? "font-bold" : ""}`}>
                <DescriptionMarker type={description.type} index={subIndex} />
                {translateOr(t, description.text, description.text)}
              </div>
              {description.subDescriptions && description.subDescriptions.length > 0 ? (
                <div className="pl-4">
                  {description.subDescriptions.map((subDesc, subSubIndex) => (
                    <div key={subSubIndex} className="flex items-center pl-4">
                      <DescriptionMarker type={subDesc.type} index={subSubIndex} />
                      {translateOr(t, subDesc.text, subDesc.text)}
                    </div>
                  ))}
                </div>
              ) : null}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
