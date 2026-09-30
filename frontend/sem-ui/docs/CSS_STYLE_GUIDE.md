# SELCO Livelihood UI — CSS Style Guide

Source of truth: Figma "Style Guidelines" page (cover + typography + color sections). This document
translates the Figma spec into rules for this codebase (`src/ui/globals.css`, Tailwind v4 `@theme`
tokens, shadcn components).

Figma reference: [SELCO Redesign Handoff — Style Guidelines](https://www.figma.com/design/Z55rzTBbtJuEamNIXF6SAZ/SELCO-Redesign-Handoff?node-id=258-66&p=f&t=Q5X0LvCu82A227Ke-0)

> The Figma export used to write this guide was cut off after the **Body Text** scale (13/13) and the
> start of a **Paragraph Texts** section. The left-nav in the Figma file also lists `03. Grid System`,
> `04. Icons`, `05. Buttons & Selectors`, `06. Spacing`, `07. Input & Search`, `08. Navigation` and
> `09. Ui Kits` — none of those sections' specs were present in the exported CSS, so they are **not**
> covered below. Re-export those sections from Figma and append them here before treating this guide
> as complete.

## 1. Font

- Typeface: **Poppins** for all text (headings, body, UI). No secondary/fallback typeface is defined
  in the source file — use `ui-sans-serif, system-ui, sans-serif` only as the browser fallback stack.
- Weights used in the spec: `300` (Light), `400` (Regular), `500` (Medium), `600` (SemiBold).
- ⚠️ **Action needed:** [`index.html`](../index.html) only loads weights `400;500;600` from Google
  Fonts. The cover subtext ("Typography" tagline, 32px/48px) is specified at **weight 300**, which
  isn't loaded — the browser will synthesize/fake-bold it instead of rendering true Light. Add `300` to
  the Google Fonts URL if that cover treatment is implemented:
  `family=Poppins:wght@300;400;500;600`.
- Use the existing `.font-poppins` utility class (`src/ui/globals.css`) rather than repeating
  `font-family: 'Poppins', ...` inline.

## 2. Type Scale

All line-heights below are the Figma "identical to box height" values — always set `line-height`
explicitly per size, don't rely on a global default.

### 2.1 Display / one-off sizes

| Element | Size / Line-height | Weight | Color |
|---|---|---|---|
| Logotype ("SELCO" hero, homepage) | 100px / 150px | 500 (Medium) | `#134738` |
| Section divider heading ("Style Guidelines") | 52px / 78px | 600 (SemiBold) | `#134738` |
| Page index number ("Index.") | 92px / 138px | 600 (SemiBold) | `#1C1C1C` |
| Category nav list items | 36px / 54px (one item at 33.33px / 54px) | 400 (Regular) | `#4F4F4F` |
| Cover heading | 64px / 96px | 400 (Regular) | `#FFFFFF` (on dark bg) |
| Cover tagline | 32px / 48px | 300 (Light) | `#FFFFFF` (on dark bg) |
| Section label ("Titles - Headlines", "Body Text", "Paragraph Texts") | 60px / 90px | 600 (SemiBold) | `#1C1C1C` |

### 2.2 Headings (H1–H7)

Each heading sample in the spec is literally the word "SELCO" set in the target style — use these
size/weight/line-height triples for `h1`–`h6` (Figma only goes to H7; treat H7 as an optional small
heading/eyebrow style):

| Token | Size | Line-height | Weight | Color |
|---|---|---|---|---|
| H1 | 64px | 96px | 600 (SemiBold) | `#1C1C1C` |
| H2 | 48px | 72px | 600 (SemiBold) | `#1C1C1C` |
| H3 | 40px | 60px | 600 (SemiBold) | `#1C1C1C` |
| H4 | 36px | 54px | 600 (SemiBold) | `#1C1C1C` |
| H5 | 32px | 48px | 600 (SemiBold) | `#1C1C1C` |
| H6 | 24px | 36px | 600 (SemiBold) | `#1C1C1C` |
| H7 (eyebrow, optional) | 20px | 30px | 600 (SemiBold) | `#1C1C1C` |

Rule: headings are always **SemiBold (600)** — never Regular/Medium — and always `#1C1C1C` on light
surfaces.

### 2.3 Body text

Figma's "Body Text" scale pairs each size with specific weights (not every weight exists at every
size — don't invent combinations that aren't in this table):

| Token | Size | Line-height | Weight | Name |
|---|---|---|---|---|
| Body/1 | 32px | 48px | 400 | Regular |
| Body/2 | 24px | 36px | 400 | Regular |
| Body/3 | 20px | 30px | 400 | Regular |
| Body/4 | 20px | 30px | 600 | SemiBold |
| Body/5 | 18px | 27px | 600 | SemiBold |
| Body/6 | 16px | 24px | 400 | Regular |
| Body/7 | 16px | 24px | 500 | Medium |
| Body/8 | 16px | 24px | 600 | SemiBold |
| Body/9 | 14px | 21px | 400 | Regular |
| Body/10 | 14px | 21px | 500 | Medium |
| Body/11 | 14px | 21px | 600 | SemiBold |
| Body/12 | 12px | 18px | 400 | Regular |
| Body/13 | 12px | 18px | 500 | Medium |

Color for all body text on light surfaces: `#1C1C1C`.

Default body copy should use **Body/6 (16px/24px, Regular)** unless density constraints call for
14px (Body/9) or smaller (Body/12) in tables/captions.

## 3. Color Palette

| Hex | Role in the spec | Notes |
|---|---|---|
| `#134738` | Primary brand green — logotype, section heading accents, dark header/footer backgrounds | See discrepancy below |
| `#F0AD19` | Accent — the 4px divider under the SELCO logotype on the cover | Use sparingly, as an accent only |
| `#1C1C1C` | Neutral 950 — default heading/body text on white | |
| `#4F4F4F` | Neutral — secondary text (nav/category list) | |
| `#AFAFAF` | Neutral — 1px hairline dividers between typography rows | |
| `#FFFFFF` | Base white — page/section background, text on dark backgrounds | |
| `rgba(255,255,255,0.6)` @ `opacity: 0.15` | Decorative oversized watermark text ("WCAG") on the dark header band | Decorative only, not for real content |

⚠️ **Discrepancy with current implementation:** `--primary` in
[`src/ui/globals.css`](../src/ui/globals.css) is `hsl(152 45% 28%)` (≈ `#27674A`), which is
noticeably lighter/less saturated than the Figma primary `#134738` (≈ `hsl(163 58% 18%)`). Confirm
with design whether the CSS variable should be updated to match `#134738`, or whether the Figma file
is intentionally a shade lighter for accessibility/contrast reasons, before changing shared tokens.

### Rules

- Never hardcode these hex values in component files. Map them into `@theme` tokens in
  `globals.css` (e.g. `--color-primary`, `--color-border`) and consume via Tailwind classes
  (`text-primary`, `border-border`, etc.), the same pattern already used for `--sla` /
  `--sla-foreground`.
- `#AFAFAF` dividers are for internal content separation (e.g. rows in a spec/list), not for the same
  purpose as the app's `--border` token unless confirmed to be the same value.
- The accent color `#F0AD19` is decorative/brand (logo underline), not a semantic "warning" or
  "pending" color — don't reuse it for status badges without an explicit design decision.

## 4. Layout & Spacing

- Design canvas reference width: `1936px` (desktop). Treat this as the max content width for
  full-bleed marketing/cover-style sections, not as a fixed app viewport.
- Section padding: `80px 100px` (vertical 80px, horizontal 100px) for full-width content sections.
- Common auto-layout gaps observed in the spec: `8px`, `16px`, `40px`, `80px`. Prefer these over
  arbitrary spacing values — they should exist as Tailwind spacing scale steps already
  (`gap-2`, `gap-4`, `gap-10`, `gap-20`).
- Divider styling:
  - Content-row separator: `border: 1px solid #AFAFAF`, full width, no radius.
  - Decorative brand divider: `border: 4px solid #F0AD19`, fixed ~419px width under the logotype.
- Layout pattern: sections are built as vertical auto-layout stacks (`flex-direction: column`,
  `align-items: flex-start`) with a fixed `gap`, not manual margins between siblings — mirror this
  with Tailwind's `flex flex-col items-start gap-*` rather than ad-hoc `mt-*`/`mb-*` on children.

## 5. Implementation checklist

- [ ] Load Poppins weight `300` if/when the Light-weight cover treatment is built.
- [ ] Reconcile `--primary` in `globals.css` with Figma's `#134738` (see §3).
- [ ] Add heading (`h1`–`h6`) and body (`Body/1`–`Body/13`) styles from §2 as reusable Tailwind
      utilities or `@layer components` classes instead of inlining font-size/line-height per usage.
- [ ] Re-export and document Grid System, Icons, Buttons & Selectors, Spacing, Input & Search,
      Navigation, and UI Kits sections from Figma (see note at top of this file).
