---
name: Academic Assessment System
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#444653'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#757684'
  outline-variant: '#c4c5d5'
  surface-tint: '#3755c3'
  primary: '#00288e'
  on-primary: '#ffffff'
  primary-container: '#1e40af'
  on-primary-container: '#a8b8ff'
  inverse-primary: '#b8c4ff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#2000b1'
  on-tertiary: '#ffffff'
  tertiary-container: '#382ad0'
  on-tertiary-container: '#b6b4ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dde1ff'
  primary-fixed-dim: '#b8c4ff'
  on-primary-fixed: '#001453'
  on-primary-fixed-variant: '#173bab'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#e2dfff'
  tertiary-fixed-dim: '#c3c0ff'
  on-tertiary-fixed: '#0f0069'
  on-tertiary-fixed-variant: '#3323cc'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

The design system projects authority, academic rigor, and absolute operational clarity. Built for institutional assessment bodies, faculty evaluators, and test candidates, the visual language avoids frivolous ornament in favor of a modern, institutional precision that builds confidence under high-stakes circumstances.

The design movement combines **Corporate Modern** with **High-Density Utility**:
- **Clarity over novelty:** High-contrast text hierarchies, uncompromised readability, and structured data tables ensure cognitive load remains minimal during proctoring, exam authoring, and timed tests.
- **Calm, institutional authority:** Deep navy anchors navigation and administrative structures, while royal blue signals active interaction points and decisive forward progress.
- **Focus states:** Workspaces prioritize deep focus via clean surface boundaries, crisp division lines, and purposeful status color coding that conveys state instantly without ambiguous iconography.

## Colors

The palette establishes an unambiguous hierarchy between foundational structure, actionable items, and operational exam statuses.

### Core Swatches
- **Primary (`#1E40AF` / `#1D4ED8`):** Royal Blue drives primary button actions, active navigation triggers, assessment timers, and verified badges.
- **Secondary (`#0F172A`):** Deep Navy anchors high-tier chrome, sidebars, main headings, and structural frame elements.
- **Tertiary (`#4F46E5` / `#6366F1`):** Subdued Indigo/Violet accents guide contextual aids, secondary data metrics, and analytical visual tags.
- **Neutral Canvas (`#F8FAFC` to `#F1F5F9`):** Slate tinted off-whites that prevent glare during long testing sessions while giving stark separation against pure white (`#FFFFFF`) card surfaces.

### Semantic & Exam Status Colors
- **Success / Approved (`#059669`):** Emerald for passed, verified, and approved assessments. Background tint: `#ECFDF5`. Border: `#A7F3D0`.
- **Warning / Pending Approval (`#D97706`):** Amber for review-required, pending approval, and flagged submissions. Background tint: `#FFFBEB`. Border: `#FDE68A`.
- **In Progress (`#2563EB`):** Dynamic blue indicating active sessions. Background tint: `#EFF6FF`. Border: `#BFDBFE`.
- **Draft (`#475569`):** Slate gray for inactive or staging configurations. Background tint: `#F1F5F9`. Border: `#CBD5E1`.
- **Critical / Closed (`#DC2626`):** Crimson red for expired, closed, or integrity violation states. Background tint: `#FEF2F2`. Border: `#FECACA`.

## Typography

The type scale combines **Plus Jakarta Sans** for crisp, geometric headers and **Inter** for data density, assessment prompts, questions, and operational microcopy.

- **Headers:** Rendered in Plus Jakarta Sans with subtle negative tracking (`-0.02em` to `-0.01em`) to create structured, institutional anchoring in titles, test codes, and summary scorecards.
- **Body & Questions:** Inter delivers maximum legibility with neutral vertical metrics, rendering multi-line examination passages, formula notation, and instructions fatigue-free.
- **Labels & Status Indicators:** Inter Semibold (600) with slight positive tracking ensures badge classifications like `PENDING APPROVAL` and `IN PROGRESS` are instantly decodable at compact sizes.

## Layout & Spacing

The layout is built upon an 8pt-derived rhythm to maintain mathematical order across question grids, proctor dashboards, and student interfaces.

- **Grid Strategy:**
  - **Desktop (1280px+):** 12-column fluid grid, `2rem` outer margins, `1.5rem` gutters. Constrained maximum container width of `1440px` for portals, with administrative tables allowed to stretch within `1600px`.
  - **Tablet (768px - 1279px):** 8-column layout, `1.5rem` outer margins and gutters. Sidebars fold into an overlay drawer.
  - **Mobile (< 768px):** 4-column layout, `1rem` outer margins and gutters. Single-column stacked cards for assessments and test navigation bars pinned to viewports.
- **Component Padding Scale:**
  - `space-xs` (4px) for badge inner paddings and icon offsets.
  - `space-sm` (8px) for control element paddings and tight metadata pairs.
  - `space-md` (16px) for form inputs, table cell heights, and standard container insets.
  - `space-lg` (24px) for card body padding and modal section separation.
  - `space-xl` (32px) for page header offsets and distinct module blocks.

## Elevation & Depth

Depth in this design system is architectural and controlled—relying on pristine card boundaries with soft ambient shadows to keep the workspace clean and unburdened by heavy visual effects.

- **Base Surfaces:** Level 0 sits on `#F8FAFC`. Card surfaces use crisp pure white `#FFFFFF` paired with an ultra-thin boundary border (`1px solid #E2E8F0`).
- **Elevation-1 (Cards & Assessment Tiles):** `box-shadow: 0 1px 3px 0 rgba(15, 23, 42, 0.04), 0 1px 2px -1px rgba(15, 23, 42, 0.04);` with a boundary border of `#E2E8F0`.
- **Elevation-2 (Active Selection / Hover States):** `box-shadow: 0 4px 6px -1px rgba(15, 23, 42, 0.07), 0 2px 4px -2px rgba(15, 23, 42, 0.05);` with boundary border transitioning to `#CBD5E1`.
- **Elevation-3 (Dropdowns & Contextual Flyouts):** `box-shadow: 0 10px 15px -3px rgba(15, 23, 42, 0.08), 0 4px 6px -4px rgba(15, 23, 42, 0.04);` over `#FFFFFF`.
- **Elevation-4 (Modals & Confirmation Sheets):** `box-shadow: 0 20px 25px -5px rgba(15, 23, 42, 0.12), 0 8px 10px -6px rgba(15, 23, 42, 0.06);` accompanied by a backdrop scrim of `rgba(15, 23, 42, 0.45)` with `backdrop-filter: blur(4px)`.

## Shapes

The system relies on balanced roundedness (`roundedness: 2`), blending institutional discipline with a contemporary visual polish:
- **Base Components:** Inputs, buttons, dropdown triggers, and status badges utilize `rounded-md` (`0.5rem` / 8px).
- **Cards & Assessment Containers:** Primary cards, metric panels, and exam question containers utilize `rounded-xl` (`1rem` / 16px).
- **Dialogs & Modals:** Large modals and full-screen drawer panels use `rounded-2xl` (`1.25rem` / 20px).
- **Tags & Indicators:** Circular indicators and pill-style question palette step counters use full circular rounding (`9999px`).

## Components

### Buttons
- **Primary:** Filled `#1E40AF` background with pure white text, transitioning to `#1D4ED8` on hover. Focused with `ring-2 ring-offset-2 ring-[#1E40AF]`. Border radius `0.5rem`.
- **Secondary:** Surface `#FFFFFF` with `#0F172A` text and a precise `1px solid #CBD5E1` border. Hover background shifts to `#F8FAFC`.
- **Destructive:** Borderless or tinted `#FEF2F2` background with `#DC2626` text, escalating to filled `#DC2626` text-white for high-risk actions like "End Exam Session."

### Exam Status Badges
Status badges use a strict 3-tier styling pattern: light tinted background, matching solid border, and high-contrast saturated text.
- **Draft:** Background `#F1F5F9`, border `#CBD5E1`, text `#475569`.
- **Published:** Background `#EEF2FF`, border `#C7D2FE`, text `#4338CA`.
- **In Progress:** Background `#EFF6FF`, border `#BFDBFE`, text `#1D4ED8`.
- **Pending Approval:** Background `#FFFBEB`, border `#FDE68A`, text `#B45309`.
- **Approved:** Background `#ECFDF5`, border `#A7F3D0`, text `#047857`.
- **Closed:** Background `#FEF2F2`, border `#FECACA`, text `#B91C1C`.

### Cards & Assessment Tiles
Cards feature an isolated `#FFFFFF` canvas, `1.5rem` internal padding, `rounded-xl` (`1rem`) corner radius, subtle `1px solid #E2E8F0` border, and Elevation-1 shadow. The card header splits assessment title and course codes on the left from candidate status badges and action menus on the right.

### Input Fields & Selects
- Constructed with a `1px solid #CBD5E1` border, `0.5rem` corner radius, `#FFFFFF` background, and `0.625rem 0.875rem` padding.
- Focused state applies an immediate border recolor to `#1E40AF` with a concurrent outer ring `ring-2 ring-[#BFDBFE]`. Error states leverage `#DC2626` border and ring tint.

### Checkboxes & Multiple Choice Radio Options
Question choice wrappers render as selectable cards:
- Default state: `#FFFFFF` background, `1px solid #E2E8F0` border, `rounded-lg` radius.
- Selected state: Light blue surface `#EFF6FF`, solid `1.5px solid #1E40AF` outline, with the check/radio token populated by `#1E40AF`.

### Assessment Question Palette (Specialized Component)
A compact navigation matrix displayed during tests:
- Grid of square tiles (`36px x 36px`, `rounded-md`) color-coded to express question status: Unvisited (Slate border), Answered (Solid `#059669`), Marked for Review (Solid `#D97706`), and Current Active Item (Solid `#1E40AF` with active ring indicator).