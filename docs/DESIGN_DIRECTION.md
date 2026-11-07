# Sagitta — Design Direction

*The single source of truth for how this product looks, moves, and speaks.
Every screen is a page of the same publication; every reading is an
instrument. If a change cannot be argued from this document, it is
decoration, and decoration does not ship.*

---

## 1. Philosophy — the calibrated instrument

Sagitta reports on one thing: a person's career, measured. It is built like
a **precision instrument with an editorial voice** — the way a serious
scientific journal presents data: ink on paper, one loud signal, numbers
that never lie, and prose that speaks only where a human needs to hear a
human.

Three laws follow from this and are enforced across the app:

1. **One signal per screen.** The acid-lime signal carries attention. A
   second hue is earned only by convention (podium rank) or identity
   (vendor marks) — see `NexusColors.Vendor` / `Podium*`.
2. **Nothing measured is ever prose.** Numbers, deltas, and states render
   in tabular mono (tnum) so columns never wobble; narrative never carries
   a metric.
3. **The voice is the serif.** Wherever the product *speaks* — a greeting,
   an aside explaining why a dial is empty, a first-run promise — it speaks
   in Newsreader. The instrument never speaks; the voice never measures.

---

## §2. Color — ink, paper, one loud signal

| Layer | Token family | Role |
|---|---|---|
| Grounds | `VoidBlack → Slate` (plum-black ink) | Dark surfaces; violet trace, never neutral grey |
| Paper | `PearlWhite, Cloud, Mist, Silver, Bone` | Light surfaces; warm, never clinical |
| **Signal** | `Signal #C3F53C`, `SignalDeep` (light theme) | The one loud hue: primary actions, progress, navigation |
| Structure | `Structure #4B5BD6` | Secondary data, links, charts — the counterweight |
| Attention | `Attention #FF6A3D` | Ember; error-adjacent, never decorative |
| Extended | `MaterialTheme.nexus.*` | `hairline`, `gridLine`, `innerLight`, container pairs — the theme-flipping machinery |

Contrast contract (enforced in `Color.kt`): lime is a *light* signal — it is
only ever filled with ink (≈15:1). White-on-lime is a bug, not a style.

Gradients are signature surfaces: **at most one per screen** (`NexusGradients`
— the Ascent daybreak ramp is the brand's own; indigo-violet gradients are
explicitly banned).

Vendor marks (`NexusColors.Vendor.*`) and podium rank (`Podium*`) are the
only exempt hues: fixed external identities and a universal convention,
single-sourced, never tinting chrome.

---

## 3. Typography — the four voices

Five families, each with one job (all variable, bundled in
`core/designsystem/res/font`):

| Family | Role | Styles |
|---|---|---|
| **Plus Jakarta Sans** | The instrument's voice — headlines, titles, UI | `DisplayLarge/Medium`, `Headline*`, `Title*` |
| **Inter** | Prose and labels | `Body*`, `Label*`, `EditorialLead/Body` |
| **JetBrains Mono** | What the machine measures | `Stat`, `Telemetry*`, `DataLabel`, `DataLabel` readouts |
| **Newsreader roman** | The narrative statement | `EditorialDisplay`, `EditorialHeadline`, `EditorialTitle` |
| **Newsreader italic** | The human aside | `EditorialQuote`, `Voice` |

### Where each voice is expressed

- **`EditorialTitle` (Newsreader roman 21sp)** — the *byline*: one narrative
  sentence per screen. Live on the Home greeting ("Good night" above the
  user's name — the serif line leads, the sans ExtraBold name follows like
  a byline).
- **`Voice` (Newsreader italic 16sp)** — the aside: the sentence that says
  *why the dial is empty*. Live on the momentum empty state, the
  skill-breakdown note, the no-goals line, and `EmptyState(voice = …)`.
  If it ever carries a number, it has stopped being a voice.
- **`EditorialQuote` (Newsreader italic 22sp)** — pull quotes and the AI's
  spoken answers.
- **`EditorialDisplay/Headline`** — the statements that open a composition.
  One per screen, never two.

Rules: a serif line never carries a metric; mono never carries prose;
DisplayLarge is rationed (a page gets at most one).

---

## 4. Layout language

- **Editorial asymmetry over card grids.** Surfaces separate by tone and a
  1px hairline — not by drop shadows or stacked rounded boxes. `NexusCard`
  is the only card; a screen uses the fewest cards that structure allows.
- **The instrument plate.** Data lives on plates with corner ticks
  (`InstrumentPlate`, corner brackets like a viewfinder) and measurement
  grids — the page reads as lab equipment on paper.
- **Numbered sections.** `01 / 02 / 03` section indices in DataLabel mono
  are the app's structural signature (Home, Explore, AI Hub, Profile).
- **Hairline dividers with dot leaders** between label and value
  (TelemetryStrip rows) — table-of-contents syntax for data.
- **Corner brackets + `innerLight`** on hero plates: the paper catches
  light at its top edge.
- Whitespace is rationed *inside* plates and generous *between* them —
  density is a deliberate instrument reading, not a cramped default.

---

## 5. Motion identity

All motion lives in `NexusAnimation` / `NexusTransitions` / `NexusReveal`
(designsystem/theme) and is **reduced-motion aware by construction**
(`motionMillis` collapses to 0; nav and boot transitions swap instantly
under `LocalReducedMotion`).

- **One clock per reveal.** A screen's arrival is a single 0→1 timeline
  (`revealClock`); each element owns a window (`NexusReveal.Window`) and a
  rank stagger. A gauge sweep and the number inside it are the *same value
  drawn twice* (`sweepValue`).
- **Order carries meaning.** Stagger only on sequences meant to be read
  top-down (hero → readings → footnote). Peers arrive together.
- **Peers fade-lift; details slide.** Tab switches use `peerEnter/peerExit`
  (fade + 12% lift); only opening/leaving a detail slides horizontally.
- **Press language.** Every control answers with the same scale-and-settle
  (NexusButton 0.97, NexusIconButton 0.94) plus a light haptic tick.
- **Progress wipes, never spins** where possible: MomentumChart
  progressive wipe, CalibratedRule tick fill; skeletons shimmer
  (`LoadingState`) with a static reduced-motion fallback.
- No infinite animation exists outside designsystem, and every infinite
  one gates on reduced motion.

---

## 6. Component conventions

- One button each for primary (`NexusButton`, gradient-optional, ink-on-lime),
  secondary (`NexusOutlineButton`, hairline border), tertiary (m3
  `TextButton` — the hierarchy's third voice, used for quiet inline
  actions and dialog footers). `expand` chooses full-bleed vs inline.
- One field (`NexusTextField` — mono-tracked uppercase label, hairline that
  lights signal on focus, inset well), one chip (`NexusChip`), one card,
  one top bar (`NexusTopBar`), one icon button (`NexusIconButton` —
  description required by signature).
- Loading: page-level = shimmer `LoadingState`; inline = themed m3 shell with brand content; linear progress = `CalibratedRule` (graduated ticks, lit span).
- **Search is a command surface, not a form**: the Explore search keeps its tuned m3 `OutlinedTextField` shell. The one-field rule targets form inputs; the shell carries its own tuned focus/hairline colors and a label would be furniture. Sanctioned exception, re-reviewed each design pass.
- Touch floor: every interactive surface clears **48dp** (`NexusSpacing.touchTarget`, `AccessibilityDefaults.MIN_TOUCH_TARGET_DP = 48`).
- Empty states are voice-bearing: `EmptyState(title, description, voice…)`.
  An empty state explains itself in the serif aside; it never shows a bare
  spinner or a naked icon.
- Dialogs keep the m3 shell (platform familiarity) with brand content.
- m3 `TextButton` is the sanctioned tertiary primitive; m3 `IconButton` and
  raw `Button`/`Card`/`TextField` are not used in feature code.

---

## 7. Anti-generic commitments

Concretely banned (each maps to a decision already enforced in code):

- Generic purple-blue gradients → signature ramps only, one per screen.
- Glassmorphism everywhere → `glassEffect` reserved for genuinely layered
  surfaces (bottom-bar lens).
- Identical icon-title-description cards → numbered sections, plates,
  hairline tables, editorial compositions.
- Decorative floating shapes / random glows → ticks mark real scale
  divisions; a tick is lit only when the value has passed it.
- Fake terminals, AI-slop decoration → the AI surfaces speak in the serif
  voice with cited data, not chrome.
- White-on-lime, indigo-violet ramps, unexplained shadows, pill-everything
  → contrast contract + `NexusShapes` scale + hierarchy above.

---

## 8. Verification ritual

A design change ships only after:

1. `assembleDebug` green; detekt 0 findings; JVM test suites green.
2. Emulator walkthrough — both themes, the five tabs, plus the touched
   screens; 0 fatal exceptions; screenshots archived under
   `screenshots/ui-sweep/`.
3. An independent critic pass against this document (the critic's question
   is always: *can this change be argued from the direction, or is it
   decoration?*).
---

## 9. Trust metadata and the state system

*Added with the design-direction pack pass (2026-11-08). These rules govern
what a listing shows and what a screen does when the world is imperfect.*

**Trust on every listing, as data.** A surfaced opportunity carries, in the
mono telemetry register:

- **Deadline instrumentation** — `DeadlineIndicator` renders the door's
  remaining time (ROLLING / CLOSED / CLOSES TODAY / CLOSES IN N DAYS), with a
  non-colour urgency tick that survives greyscale and TalkBack. Null deadline
  means rolling by the domain contract.
- **Verification provenance** — "LINK CONFIRMED yyyy-mm-dd" only when the
  pipeline actually confirmed the apply link; an unverified listing shows no
  date, because "link may be dead" is not a fact anyone checked.
- **Match as counts, not percentages**, and a "why shown" note where the
  pipeline ranked the listing.

Absence renders as absence. An unpublished stipend is absent, not a zero; a
missing verification is absent, not a promise. Nothing here is invented.

**The state system.** Every data screen owns loading (geometry-matched
skeleton), empty (voice-bearing), error (EditorialErrorState), and — when the
catalog can age or the device can disconnect — a one-line
`EditorialStatusBand` directly above the content it describes:

- **Offline** (Fault tone): the cached catalog is shown, not a wall; refresh
  returns when connectivity does.
- **Source check** (Neutral → Fault as the freshest `verifiedAt` ages past
  48h → 7 days): links last confirmed <date>; past 7 days, deadlines may have
  moved.

A fresh catalog is silence. The band speaks only when the reader should weigh
what they read against its age.

**Reduced motion is a contract, not a habit.** Every infinite animation gates
on `LocalReducedMotion`; one-shot reveals settle on the first frame. This now
covers Lottie empty-state art, the streaming cursor, and entrance reveals —
audited 2026-11-08.
