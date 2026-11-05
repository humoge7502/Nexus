# Compose ports — the part that can actually ship

**Status: proposals for review. No file under `app/`, `core/` or `feature/` was modified.**

These are translations of the web patterns that survived the curation in
[`SOURCES.md`](SOURCES.md). They are written against the APIs that **actually exist** in
`core/designsystem/` — verified by reading `theme/Animation.kt`,
`theme/EditorialTransitions.kt` and `theme/animation/NexusSpring.kt`.

Real APIs used (no invention):

| API | Where it lives |
|---|---|
| `LocalReducedMotion`, `motionMillis(Int)` | `theme/Animation.kt` |
| `NexusAnimation.Duration.{micro,fast,standard,emphasized,long,dramatic}` | `theme/Animation.kt` |
| `NexusAnimation.Easing.{emphasized,emphasizedDecelerate,emphasizedAccelerate,enter,exit}` | `theme/Animation.kt` |
| `NexusAnimation.Springs.{button,card,list,achievementUnlock}`, `NexusAnimation.Tween.numberCount` | `theme/Animation.kt` |
| `NexusSpring.{Heavy,Medium,Light,Playful,Snappy}` | `theme/animation/NexusSpring.kt` |
| `revealClock()`, `revealFraction()`, `revealSettled()` | `theme/EditorialTransitions.kt` |
| `NexusReveal.Window.{hero,headline,readings,footnote}` | `theme/EditorialTransitions.kt` |
| `sweepValue(target, settled, durationMillis)` | `theme/EditorialTransitions.kt` |
| `NexusTransitions.{peerEnter,peerExit,forwardEnter,forwardExit,backwardEnter,backwardExit,plateEnter,plateExit}` | `theme/EditorialTransitions.kt` |

House rules these ports obey: features depend on `:core` only (ARCH-001); **no raw hex in feature
code** — use `MaterialTheme.nexus.*` roles; spacing from `NexusSpacing`, type from
`NexusTypography`; reduced motion via `LocalReducedMotion` / `motionMillis`, never per call site;
**one animated property per element where possible, never more than two**; no new infinite
animation outside designsystem; one primary action per surface; every new business rule ships with
a unit test and every UI change with a Compose test or archived screenshot.

---

## Port 1 · Number Ticker / CountUp / Sliding Number → `SweepReadout`

**From:** Magic UI `number-ticker.tsx`, React Bits `CountUp.tsx`, Motion Primitives
`sliding-number.tsx` + `animated-number.tsx`, Animate UI Sliding Number.
**Why SHIP:** all four animate a numeric readout on arrival. Sagitta already has the correct
abstraction — `sweepValue()` — which guarantees the arc and the digits are the same value drawn
twice. A direct port of `NumberTicker` would introduce a *second* clock and could disagree with
`InstrumentGauge`.

```kotlin
// core/designsystem/.../component/SweepReadout.kt  (designsystem owns motion)
package com.nexus.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.nexus.designsystem.theme.NexusReveal
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.revealClock
import com.nexus.designsystem.theme.revealFraction
import com.nexus.designsystem.theme.revealSettled
import com.nexus.designsystem.theme.sweepValue
import kotlin.math.roundToInt

/**
 * A reading that counts exactly once, on the screen's shared reveal clock.
 * The gauge arc reads the same `value`; there is no second animation to drift.
 */
@Composable
fun SweepReadout(
    score: Float,
    modifier: Modifier = Modifier,
    onArc: @Composable (Float) -> Unit = {},
) {
    val clock = revealClock()                       // one 0→1 timeline, linear
    val settled = revealSettled(clock, NexusReveal.Window.hero)
    val value = sweepValue(score, settled)          // emphasisedDecelerate, 480ms, motionMillis-aware

    // Optional: a staggered caption that belongs to the same clock, not its own timer.
    val captionAlpha = revealFraction(clock, NexusReveal.Window.headline)

    onArc(value)

    androidx.compose.material3.Text(
        text = "%.1f".format(value),
        modifier = modifier,
        style = NexusTypography.TelemetryDisplay,   // tabular mono — a metric is never prose
        color = MaterialTheme.nexus.signal,         // theme role, never a hex literal
        textAlign = TextAlign.Start,
    )
    // captionAlpha is consumed by the plate's footnote, not by a second animation spec.
}
```

**Unit test (required):** assert that for `LocalReducedMotion = true`, `sweepValue(score, true)`
returns `score` on the first frame and no animation is scheduled; assert the readout string and
the arc fraction are derived from the identical float.

---

## Port 2 · AutoAnimate / Motion `layoutId` / GSAP Flip → `animateItemPlacement`

**From:** AutoAnimate `autoAnimate(parent)`, Motion layout animations, GSAP Flip.
**Why SHIP:** three independent libraries converged on the same rule — *adding, removing and
reordering children should animate automatically, without per-item choreography*. Compose's
equivalent is one modifier. AutoAnimate's documented caveat ("`key` must be unique or animation
misbehaves") is the same requirement as stable `LazyColumn` keys.

**Step 1 — add the spec once, in designsystem** (feature code must not redefine motion):

```kotlin
// core/designsystem/.../theme/Animation.kt — addition to NexusAnimation.Springs
val itemPlacement = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
)
// import androidx.compose.ui.unit.IntOffset
```

**Step 2 — consume it in any feature list:**

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(NexusSpacing.screenGutter),
) {
    items(
        items = readings,
        key = { it.id },                            // stable unique key — AutoAnimate's rule
    ) { reading ->
        TelemetryStrip(
            reading = reading,
            modifier = Modifier.animateItemPlacement(
                animationSpec = NexusAnimation.Springs.itemPlacement,
            ),
        )
    }
}
```

Removals use `AnimatedVisibility(visible = …, exit = NexusTransitions.plateExit())` — the exit is
140ms / 220ms against a 280ms / 480ms entrance, satisfying Animate UI's "exits quicker than
entrances" rule **with the existing tokens**.

**Compose test:** assert that removing an item leaves the remaining item count correct and that
under `LocalReducedMotion = true` no placement animation spec is attached.

---

## Port 3 · `AnimatePresence` / Morphing Dialog → `AnimatedVisibility`

**From:** Motion `AnimatePresence`, Motion Primitives `morphing-dialog.tsx`,
`transition-panel.tsx`.
**Why SHIP:** exit animations are already solved. `NexusTransitions.plateEnter()` /
`plateExit()` implement transform + opacity only — never layout — and are interruptible by
construction with Compose's `AnimatedVisibility`.

```kotlin
AnimatedVisibility(
    visible = showDetail,
    enter = NexusTransitions.forwardEnter(),   // parent → child: 28% slide + fade
    exit = NexusTransitions.forwardExit(),     // 16% slide, shorter than the entrance
) {
    InstrumentPlate(/* … */)
}
```

Do **not** add a `layoutId`-style shared-element transition until the project's Compose version
supports `SharedTransitionLayout` / `sharedBounds` and a design-system wrapper exists. If added,
it belongs in `core/designsystem`, not in a feature, and must collapse to an instant swap under
`LocalReducedMotion`.

---

## Port 4 · GSAP timeline / Blur Fade / SplitText → `revealClock` windows

**From:** GSAP timeline choreography + SplitText, Magic UI `blur-fade.tsx`,
`text-animate.tsx`, React Bits `SplitText.tsx` / `BlurText.tsx`, 21st.dev hero reveals.
**Why TRANSLATE, not SHIP:** GSAP's *model* (one master timeline, per-element windows) is
already `revealClock()` + `revealFraction()`. The React implementations animate blur, per-character
opacity and transform simultaneously — that violates §5 ("never animate more than two properties
on one element") and would look wrong on a serif editorial line.

```kotlin
val clock = revealClock()   // linear 700ms master; returns 1f immediately under reduced motion

val heroProgress     = revealFraction(clock, NexusReveal.Window.hero)
val headlineProgress = revealFraction(clock, NexusReveal.Window.headline)
val footnoteProgress = revealFraction(clock, NexusReveal.Window.footnote)

// Peers share a window and are staggered by rank — but only where reading order is real.
readings.forEachIndexed { rank, reading ->
    val p = revealFraction(clock, NexusReveal.Window.readings, rank = rank)
    ReadingRow(reading, alpha = p, lift = (1f - p) * 12f)   // exactly two properties
}
```

**Rule:** one `EditorialDisplay` per screen; a serif line never carries a metric. Use
`NexusTypography.EditorialTitle` for the byline, `NexusTypography.Voice` for the aside, and never
animate the metric text with a character-level effect — it is mono, it is measured, it arrives
with the gauge.

---

## Port 5 · Spring velocity / drag momentum → `NexusSpring.Snappy`

**From:** Motion `useSpring` (stiffness / damping / mass carrying velocity), Animate UI's spring
policy, React Bits `Magnet.tsx` (pointer only — **rejected**, keep the physics only).
**Why SHIP:** Sagitta already has `NexusSpring.Snappy` (damping 0.8, StiffnessHigh) and
`NexusAnimation.Springs.button` (0.82, StiffnessHigh). These are near-critically damped, i.e.
exactly Animate UI's "arrives fast, settles, no bounce". No new spring is warranted.

```kotlin
val scale by animateFloatAsState(
    targetValue = if (pressed) 0.97f else 1f,
    animationSpec = NexusAnimation.Springs.button,   // 90ms feel, velocity-preserving
    label = "press",
)
```

Bounce remains reserved for `NexusAnimation.Springs.achievementUnlock` and `AchievementBurst`
(one-shot, skippable, real milestones only). Never spring a card appearing.

---

## Port 6 · Confetti / Cool Mode / ClickSpark → `AchievementBurst`

**From:** Magic UI `Confetti`/`Cool Mode`, React Bits `ClickSpark`, Kibo UI `Marquee`.
**Verdict: do not port.** `AchievementBurst` already exists, is one-shot, skippable, gated on
`LocalReducedMotion`, and reserved for genuine milestones (§5). A click spark on every tap would
turn a celebration into decoration and violate §7. Keep the existing component; do not add a
second celebration language.

---

## Port 7 · Command palette / Data Grid / semantic icon index → behaviour spec

**From:** ReUI (free MCP server + agent skills, Command 8, Data Grid 34, Sparkline 11,
Empty State 20), Origin UI → coss ui (`Command`, `Composer`, `Meter`, `Empty`),
Kibo UI (`Code Block`, `Dropzone`, `Gantt`), 21st.dev semantic icon search.
**Why TRANSLATE:** these are *information architecture* wins, not visual ones.

- `NexusCommandPalette` already exists. Borrow ReUI's **MCP-style destination index**: a stable,
  typed list of destinations with semantic aliases so an agent (or a user typing "where did my
  streak go") resolves to `Growth` rather than a string match. Add this as data in `:core`, with
  a unit test over alias→destination resolution — this is a **business rule**, so it needs a test.
- `EmptyState(title, description, voice)` already matches ReUI / coss `Empty`. Their 20 variants
  are a useful checklist, not a component to port.
- `CalibratedRule` already covers `Meter` / `Progress` / `Scroll Progress`: graduated ticks, lit
  only where the value has passed. A tick is real scale, never decoration (§7).
- Kibo's **Gantt/Roadmap** is worth studying for `MomentumChart` and learning timelines, but only
  if it renders real application data — §6 forbids invented numbers.

---

## Explicitly rejected — do not port

| Web pattern | Sites | Reason |
|---|---|---|
| WebGL / shader / aurora / dither / liquid metal heroes | 21st.dev, Aceternity, Magic UI, Godly | GPU compositor, infinite animation; §7 bans decorative glows and generic ramps |
| Bento grids & identical icon-title-description cards | 21st.dev, Aceternity, Magic UI | §4 demands editorial asymmetry, plates, hairlines, numbered sections |
| Hover spotlight, 3D tilt, magnetic buttons, wobble cards | 21st.dev, Aceternity, React Bits | touch has no hover; a pointer-only effect has no honest Android equivalent |
| Marquee, meteors, orbiting circles, spinning text | Magic UI, Kibo UI, React Bits | infinite motion outside designsystem is forbidden (§5) |
| Glassmorphism surfaces | Aceternity, 21st.dev | §7 reserves `GlassCard` for genuinely layered chrome (the bottom-bar lens) |
| Indigo-violet gradients | most libraries | §2 / §7: signature ramps only, at most one per screen |
| Fake terminals / AI-slop chrome | Magic UI `Terminal`, ReUI AI blocks | §7: AI surfaces speak in the serif voice with cited data |

---

## Verification ritual before any of this ships

Per `docs/DESIGN_DIRECTION.md` §8 and `docs/DESIGN_SYSTEM.md` §9:

1. `./gradlew :app:assembleDebug` green; `./scripts/detekt.sh` 0 findings (baseline
   `config/detekt/baseline.xml`); `./gradlew testDebugUnitTest` green; `./gradlew runAudit` —
   feature→feature edges would fail ARCH-001.
2. Emulator walkthrough in **both themes**, all five tabs, plus touched screens; 0 fatal
   exceptions; screenshots archived under `screenshots/ui-sweep/`.
3. Independent critic pass: *can this change be argued from DESIGN_DIRECTION.md, or is it
   decoration?*
4. Reduced motion on: every port collapses to an instant state change. AutoAnimate, Animate UI
   and Motion all gate on it by default — Sagitta must too.
5. Attribution: if any `sources/react-bits/` code is ported, add its **MIT + Commons Clause**
   notice to the app's `NOTICE`; same for the MIT libraries' copyright lines.
