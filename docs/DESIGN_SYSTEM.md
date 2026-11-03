# Sagitta Design System — Engineering Reference

> **Reconciliation note.** The product's identity, philosophy and visual law
> live in [`DESIGN_DIRECTION.md`](DESIGN_DIRECTION.md) — that document is the
> authority on *why*. This document is the authority on *what and how*: the
> tokens, type ladder, motion specs and component catalog as actually shipped
> in `core/designsystem/`. Where older documents (this file's previous
> revision, NEXUS20 notes) disagree with either, they are out of date.

Source of truth in code: `core/designsystem/`.

## 1. Philosophy (summary — full version in DESIGN_DIRECTION.md)

Sagitta is a **precision instrument with an editorial voice**, not a social
feed:

1. **One signal per screen.** The acid-lime signal carries attention; a
   second hue is earned only by convention (podium rank) or identity
   (vendor marks).
2. **Nothing measured is ever prose.** Numbers render in tabular mono;
   narrative never carries a metric.
3. **The voice is the serif.** Where the product speaks, it speaks in
   Newsreader. The instrument never speaks; the voice never measures.

## 2. Color

### Grounds

| Family | Tokens | Role |
|---|---|---|
| Ink (dark) | `VoidBlack #08060B`, `DeepSpace`, `Carbon`, `Graphite`, `Slate` | Plum-black surfaces — a violet trace, never neutral grey |
| Paper (light) | `PearlWhite #FAF7F3`, `Cloud`, `Mist`, `Silver`, `LavenderSmoke`, `Bone` | Warm paper — never clinical white |

### Signals

| Token | Hex | Role |
|---|---|---|
| `Signal` / `SignalBright` | `#C3F53C` / `#D6FF5E` | The one loud hue: primary actions, progress, navigation |
| `SignalDeep` | `#6B8A10` | Signal on paper (light-theme text/icons) |
| `SignalInk` | `#0F1405` | Content ink on lime grounds |
| `Structure` / `StructureBright` | `#4B5BD6` / `#7C8CFF` | Secondary data, links, charts — the counterweight |
| `Attention` | `#FF6A3D` | Ember — error-adjacent and streaks; never decorative |
| `AuroraGreen` | `#3FE0B0` | Semantic success states only |

**Exempt hues** (`NexusColors.Vendor.*`, `Podium*`): fixed external brand
marks and podium rank. Single-sourced in `Color.kt`; feature files carry no
hex literals.

### Theme-aware extended tokens (`MaterialTheme.nexus.*`)

`signal`, `structure`, `attention`, `positive`, `bone`, `ink`, `hairline`,
`hairlineStrong`, `innerLight`, `gridLine`, plus `signalContainer`,
`attentionContainer`, `structureContainer` pairs — everything that must flip
with the theme lives here, not in feature code.

### Contrast contract

- Lime is a *light* signal: it is only ever filled with ink (≈15:1).
  White-on-lime is a bug, not a style.
- Body text ≥ 4.5:1; large text ≥ 3:1 against its surface.
- Never encode state by color alone — pair with an icon or label.

### Theming modes

- **Light / Dark** — the two first-class schemes (`LightColorScheme`,
  `DarkColorScheme`), warm paper vs plum-black ink.
- **AMOLED** — opt-in true black: background `#000000`, near-black surfaces;
  independent of the dark setting (`NexusTheme(amoled = …)`).
- **Material You** — `dynamicColor` is **opt-in and off by default**:
  wallpaper-derived palettes would erase the recognisable signal identity.
- **Accent override** — `Signal, Structure, Emerald, Ember, Rose, Plum`
  repoint `primary` for accessibility or preference; the identity survives
  because containers/ink are derived, not swapped.

### Gradients

Reserved for signature surfaces — **at most one per screen**
(`NexusGradients`); the Ascent daybreak ramp is the brand's own. Generic
indigo-violet ramps are banned.

## 3. Typography — the four voices

Five bundled variable families (OFL, offline), one job each:

| Family | Voice | Styles (`NexusTypography`) |
|---|---|---|
| **Plus Jakarta Sans** | Instrument — headings, UI hierarchy | `DisplayLarge` 42sp XB, `DisplayMedium` 35sp B, `Headline*`, `Title*` |
| **Inter** | Prose, labels | `Body*`, `Label*`, `EditorialLead` 17/27, `EditorialBody` |
| **JetBrains Mono** | What the machine measures (tnum) | `Stat`, `TelemetryDisplay/Large/Micro`, `DataLabel`, `Readout` |
| **Newsreader roman** | Narrative statements | `EditorialDisplay` 44sp, `EditorialHeadline` 32sp, `EditorialTitle` 21sp |
| **Newsreader italic** | Human asides | `EditorialQuote` 22sp, `Voice` 16sp |

Rules:
- One `Display`/`EditorialDisplay` statement per screen; never two.
- Mono never carries prose; serif never carries a metric.
- `Voice` is the aside that explains *why a dial is empty* — caption scale.
- `MaterialTheme.typography.*` maps the m3 roles; `NexusTypography.*` names
  are PascalCase by design.

## 4. Spacing, shape, elevation

- **Spacing** (`NexusSpacing`): 2, 4, 8, 12, 16, 20, 24, 32, 40, 48, 64 dp.
  No arbitrary values.
- **Screen gutter:** 20–24 dp. **Card padding:** 16–20 dp. **Section gap:**
  24–32 dp.
- **Shapes** (`NexusShapes`): 4 → 28 dp, plus `pill` and `tag`. Cards use
  `lg`; chips `pill`; inputs `sm`.
- **Elevation** (`NexusElevation`): hairline strokes + tonal surfaces over
  shadows. Shadows are for floating chrome (the bottom bar) and sheets only.

## 5. Motion

Specified once in `theme/Animation.kt` (control timing),
`theme/EditorialTransitions.kt` (page motion), `theme/NexusSpring.kt`
(springs) — feature code consumes, never redefines.

| Pattern | Spec | Use |
|---|---|---|
| Tab switch (peers) | `peerEnter/peerExit` — fade + 12% lift | Bottom-bar destinations |
| Detail open/close | `forwardEnter/Exit`, `backwardEnter/Exit` — 28%/16% slide + fade | Parent ↔ child |
| Boot flow | forward pair; instant swap under reduced motion | Auth → onboarding → app |
| Reveal | `revealClock` — one 0→1 clock; windows via `NexusReveal.Window` | A screen's arrival |
| Press feedback | scale 0.97 (buttons) / 0.94 (icon buttons) + haptic tick | Every tappable |
| Gauges & charts | `sweepValue` / `motionMillis` — one value drawn twice | Readings |
| Skeletons | `shimmer()` — static under reduced motion | Loading content |
| Celebration | `AchievementBurst` — one-shot, skippable | Real milestones only |

Rules:
- Reduced motion (`LocalReducedMotion`) collapses control motion to 0 ms and
  page motion to instant swaps — enforced by construction, not per call site.
- Never animate more than two properties on one element; prefer draw-phase
  or graphics-layer animation.
- No animation blocks input or delays the first meaningful frame.
- No infinite animation exists outside designsystem, and each gates on
  reduced motion.

## 6. Component catalog (`core/designsystem`)

Controls: `NexusButton` (primary, gradient-optional, `expand`),
`NexusOutlineButton` (secondary hairline), m3 `TextButton` (sanctioned
tertiary — inline actions, dialog footers), `NexusIconButton`
(description required by signature), `NexusTextField`, `NexusChip` /
`ChipGroup`, `NexusCard` / `GlassCard` (glass reserved for layered chrome),
`NexusTopBar`, `NexusBottomBar` (spring lens indicator) /
`NexusAdaptiveNavigation` (bar → rail → drawer by width).

Instrument: `InstrumentGauge`, `InstrumentPlate`, `InstrumentPrimitives`
(`SignalTag`, `CalibratedRule`, `Readout`), `TelemetryStrip`, `StatCard`,
`SectionHeader` (numbered `01` sections), `MomentumChart`,
`SkillConstellation`, `LearningHeatmap`, `PremiumEffects`
(`sheen`, `arcGlow`, `instrumentGrid`, `hairline`).

Editorial: `EditorialComposition`, `CareerProgressionReveal`,
`EditorialMetric`, `EditorialFeedback`, `EditorialIntelligence`,
`LearningEditorial`, `OpportunityEditorial(Card)`, `EditorialConversation` /
`EditorialComposer`, `StreamingAIResponse`.

States: `EmptyState` (voice-bearing: `title`, `description`, `voice`),
`LoadingState` / `shimmer()` skeletons, `ScorePulseOverlay`,
`AchievementBurst`, `NexusMark`, `NexusCommandPalette` (global destination
search overlay).

Composition rules:
- One primary `NexusButton` per surface; hierarchy is
  primary → outline → text.
- Raw m3 `Button`/`IconButton`/`Card`/`TextField`/`OutlinedTextField` are
  not used in feature code; dialogs keep the m3 shell with brand content.
- `EmptyState` offers the next action and may speak in the serif voice.
- Page-level loading is a skeleton, never a bare spinner; linear progress
  is `CalibratedRule`.
- Charts render real application data or an honest empty state — never
  invented numbers.

## 7. Accessibility checklist

- Every `Image`/icon has a `contentDescription`, or `null` only when purely
  decorative inside an already-labelled container.
- Touch targets ≥ **48×48 dp**.
- TalkBack order follows visual order; group related semantics with
  `Modifier.semantics(mergeDescendants = true)`.
- Charts carry accessible descriptions (e.g. the constellation summarizes
  every node in `contentDescription`).
- Dynamic type: layouts survive 200% font scale; no fixed-height text.
- State is never color-only.
- Strings live in `strings.xml`; no hardcoded user-facing copy.
- Reduced motion honored app-wide (see §5).

## 8. Previews

`core/designsystem/src/main/kotlin/com/nexus/designsystem/previews/` holds
Compose `@Preview` smoketests for the core controls and instrument pieces,
in light **and** dark, so token changes surface in the IDE before a build.

## 9. "Not vibe-coded" review checklist

Before shipping a screen, confirm:

- [ ] It can be argued from `DESIGN_DIRECTION.md` — or it is decoration.
- [ ] Theme roles / `nexus` extended tokens only; no raw hex in features.
- [ ] Spacing from `NexusSpacing`; type from `NexusTypography`.
- [ ] Exactly one primary action per surface.
- [ ] Empty, loading, and error states are designed — the empty one speaks.
- [ ] Motion is purposeful and respects reduced motion.
- [ ] It passes the accessibility checklist.
- [ ] It has a Compose preview, a UI test, or an archived screenshot.
