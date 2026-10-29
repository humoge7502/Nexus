# NEXUS Design System

> Source of truth in code: `core/designsystem/`.
> This document explains **why** the tokens exist and how to use them, so new
> screens look intentional rather than assembled.

## 1. Design philosophy

NEXUS should feel like a **precision instrument for a student's career**, not a
social feed. Three principles drive every decision:

1. **Signal over decoration.** Color and motion are reserved for meaning:
   readiness, progress, risk, achievement. If an effect does not communicate
   state, remove it.
2. **Calm density.** Career data is dense; the interface is not. Generous
   spacing, one primary action per surface, and progressive disclosure.
3. **Earned delight.** Celebration (confetti, supernova, arc glow) is rare and
   tied to real milestones — a streak, a completed module, an offer.

## 2. Color

### Brand / signal palette (`NexusColors`)

| Token | Hex | Use |
|---|---|---|
| `NexusBlue` | `#4F8EF7` | Primary action, focus, links |
| `ElectricIndigo` | `#7B5EA7` | Secondary brand, gradients |
| `AuroraGreen` | `#00D9A3` | Success, growth, "on track" |
| `PlasmaOrange` | `#FF6B35` | Energy, streaks, warnings that motivate |
| `StellarPink` | `#F72585` | Celebration, highlights |
| `Rose500` | `#F43F5E` | Error / destructive |
| `Amber500` | `#F59E0B` | Warning / attention |

### Semantic mapping

Always use Material theme roles (`MaterialTheme.colorScheme.primary`,
`onSurface`, `error`, …) inside composables — never raw hex. Raw tokens are for
building the scheme and for data visualization, where a series needs a stable
identity.

### Theming modes

- **Material You (default, Android 12+):** wallpaper-derived palette. Respects
  the user's device identity.
- **Brand fallback (older OS or explicit accent):** `LightColorScheme` /
  `DarkColorScheme` with an accent override (`CYAN`, `EMERALD`, `AMBER`,
  `ROSE`).
- **AMOLED:** true-black background (`#000000`) and near-black surfaces, opt-in
  and independent of dark mode.

### Contrast rules

- Body text ≥ **4.5:1** against its surface; large text ≥ **3:1**.
- Never encode state by color alone — pair with an icon or label (e.g. a red
  score also reads "At risk").
- On gradients, place text on a scrim or a solid surface; do not rely on the
  midpoint color.

## 3. Typography

**Pairing:** *Plus Jakarta Sans* for display/headings (geometric, confident) and
*Inter* for body/labels (maximum legibility at small sizes). Both are bundled
variable fonts (OFL), so they render offline with full weight axes.

| Style | Face | Size / line | Use |
|---|---|---|---|
| `DisplayLarge` | Jakarta ExtraBold | 40 / 48, −1.5 tracking | Hero score, big moments |
| `DisplayMedium` | Jakarta Bold | 36 / 44 | Section heroes |
| `HeadlineLarge` | Jakarta Bold | 28 / 36 | Screen titles |
| `HeadlineMedium` | Jakarta SemiBold | 24 / 32 | Card group titles |
| `TitleLarge` | Jakarta SemiBold | 18 / 26 | Card titles |
| `BodyLarge` | Inter Regular | 16 / 26 | Primary reading text |
| `BodyMedium` | Inter Regular | 14 / 22 | Secondary text |
| `BodySmall` | Inter Regular | 12 / 18 | Metadata |
| `LabelLarge` | Inter SemiBold | 14 / 20 | Buttons, tabs |
| `LabelSmall` | Inter Medium | 10 / 14, 0.8 tracking | Overlines, badges |

Rules: one `Display`/`Headline` per screen region; body copy never below
`BodySmall`; numeric scores use tabular figures where alignment matters.

## 4. Spacing, shape, elevation

- **Spacing scale** (`NexusSpacing`): 2, 4, 8, 12, 16, 20, 24, 32, 40, 48, 64 dp.
  Use the scale; no arbitrary `13.dp`.
- **Screen gutter:** 20 dp. **Card padding:** 16–20 dp. **Section gap:** 24–32 dp.
- **Shapes** (`NexusShapes`): 4 → 28 dp, plus `pill` and `topSheet`. Cards use
  `lg`/`xl`; chips use `pill`; inputs use `sm`/`md`.
- **Elevation** (`NexusElevation`): prefer hairline strokes + tonal surfaces over
  heavy shadows. Shadows are for floating actions and sheets only.

## 5. Motion

Motion must be **purposeful, interruptible, and skippable**.

| Pattern | Spec | Use |
|---|---|---|
| Navigation transition | spring, medium-low stiffness, medium bounce | Screen changes |
| Press feedback | scale to ~0.97, 90–120 ms | Tappable surfaces |
| Reveal | fade + slight scale, 300 ms | Content entering |
| Sheen sweep | linear, one pass | Premium cards, CTAs |
| Score pulse | slow infinite, low amplitude | Live readiness score |
| Celebration | particle burst, one-shot | Milestone only |

Rules:
- Respect **reduce-motion** (`Settings.Global.ANIMATOR_DURATION_SCALE == 0` or
  accessibility setting): disable infinite/decorative animation, keep state
  changes instant.
- Never animate more than two properties on the same element at once.
- No animation may block input or delay the first meaningful frame.

## 6. Components (`core/designsystem/component`)

`NexusCard`, `GlassCard`, `NexusButton`, `NexusChip`, `NexusBottomBar`,
`SectionHeader`, `StatCard`, `IntelligenceCard`, `EmptyState`, `LoadingState`,
`ScorePulseOverlay`, `PremiumEffects`.

Composition rules:
- A screen has **one** primary `NexusButton`; secondary actions are outlined or
  text buttons.
- `EmptyState` always offers the next action, not just an explanation.
- `LoadingState` uses skeletons for content and spinners only for actions.
- Cards never nest more than one level deep.

## 7. Accessibility checklist

- Every `Image` has a `contentDescription` (or `null` if purely decorative).
- Touch targets ≥ **48×48 dp**.
- TalkBack order follows visual order; group related semantics with
  `Modifier.semantics(mergeDescendants = true)`.
- Dynamic type: layouts must survive 200% font scale (no fixed-height text
  containers).
- All interactive elements are reachable without color perception.
- Strings live in `strings.xml`; no hardcoded user-facing copy in composables.

## 8. "Not vibe-coded" review checklist

Before shipping a screen, confirm:

- [ ] It uses theme roles, not raw hex.
- [ ] Spacing values come from `NexusSpacing`.
- [ ] Type styles come from `NexusTypography`.
- [ ] There is exactly one primary action.
- [ ] Empty, loading, and error states are designed — not afterthoughts.
- [ ] Motion is purposeful and respects reduce-motion.
- [ ] It passes the accessibility checklist.
- [ ] It has a Compose UI test or a screenshot in the PR.
