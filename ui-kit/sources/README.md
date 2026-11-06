# Downloaded source — attribution manifest

Downloaded 2026-11-06 (first sweep: 39 files) + 2026-11-06 re-visit (17 more
files, 56 total, ~298 KB). These are **real upstream files**, copied verbatim
from each project's default branch. They are reference material for porting
ideas into `core/designsystem`; none of them is wired into the Android build.
Each folder keeps its original license file — **read it before shipping
anything**. No React code is compiled into the APK; the shippable code is
original Kotlin in `core/designsystem` (see `../COMPOSE_PORTS.md`).

| Folder | Upstream | License file | Files (now) |
|---|---|---|---|
| `magic-ui/` | `magicuidesign/magicui` @ `main` | `LICENSE.md` (MIT) | 15 |
| `react-bits/` | `DavidHDev/react-bits` @ `main` | `LICENSE.md` (**MIT + Commons Clause**) | 10 |
| `motion-primitives/` | `ibelick/motion-primitives` @ `main` | `LICENCE.md` (MIT) | 12 |
| `auto-animate/` | `formkit/auto-animate` @ `master` | `LICENSE` (MIT) | 3 |
| `kibo-ui/` | `shadcnblocks/kibo` @ `main` | `license.md` (MIT-style) | 9 |
| `licenses/` | `motiondivision/motion` @ `main` | `motion-LICENSE.md` (MIT) | 2 |
| `animate-ui/` | `imskyleen/animate-ui` @ `main` | `LICENSE.md` (**MIT + Commons Clause**) | 3 |
| `coss/` | `cosscom/ui` @ `main` (originui successor) | `LICENSE.md` (MIT) | 2 |

**Total: 56 files (+ this README = 57 files on disk).**

## Exact upstream URLs

`https://raw.githubusercontent.com/<repo>/<branch>/<path>` — all files below were fetched from
these URLs with HTTP 200.

### magic-ui — `magicuidesign/magicui/main/`

Base path: `apps/www/registry/magicui/`

```
number-ticker.tsx                  animated-circular-progress-bar.tsx
blur-fade.tsx                      text-animate.tsx
scroll-progress.tsx                animated-list.tsx
word-rotate.tsx                    bento-grid.tsx
animated-beam.tsx                  grid-pattern.tsx
# second visit (2026-11-06):
border-beam.tsx                    dot-pattern.tsx
marquee.tsx                        scroll-based-velocity.tsx
```

Plus root `LICENSE.md`. `border-beam.tsx` is the upstream for
`Modifier.borderBeam()`; `dot-pattern.tsx` informs `instrumentGrid()`;
`marquee.tsx` / `scroll-based-velocity.tsx` are downloaded as REJECTED
references (infinite marquee is banned by DESIGN_DIRECTION §5/§7) —
kept so the rejection is provable, not forgotten.

### react-bits — `DavidHDev/react-bits/main/`

Base path: `src/ts-tailwind/` (TypeScript + Tailwind variants)

```
TextAnimations/CountUp/CountUp.tsx           TextAnimations/SplitText/SplitText.tsx
TextAnimations/BlurText/BlurText.tsx         Animations/AnimatedContent/AnimatedContent.tsx
Animations/FadeContent/FadeContent.tsx       Components/Stepper/Stepper.tsx
Animations/Magnet/Magnet.tsx                 Animations/ClickSpark/ClickSpark.tsx
# second visit (2026-11-06):
TextAnimations/TrueFocus/TrueFocus.tsx
```

Plus root `LICENSE.md`. **MIT + Commons Clause v1.0:** usable inside an application, website or
product including commercial ones; you may **not** sell, sublicense or redistribute the components
themselves — alone, bundled, or as a ported version. A repo-root `NOTICE.md` entry covers this
folder; the Kotlin ports are original implementations of the *ideas*, not redistributed components.

### motion-primitives — `ibelick/motion-primitives/main/`

Base path: `components/core/`

```
sliding-number.tsx   animated-number.tsx   text-morph.tsx   transition-panel.tsx
in-view.tsx          scroll-progress.tsx   morphing-dialog.tsx   cursor.tsx
# second visit (2026-11-06):
border-trail.tsx     toolbar-dynamic.tsx   infinite-slider.tsx
```

Plus root `LICENCE.md`. `border-trail.tsx` is the Motion Primitives sibling
of Magic UI's `border-beam` (already ported as `Modifier.borderBeam()`);
`infinite-slider.tsx` is a REJECTED reference (infinite slider, §5).

### auto-animate — `formkit/auto-animate/master/`

```
src/index.ts         LICENSE         package.json
```

`src/index.ts` is the entire library — one function, `autoAnimate(parent)`, animating add /
remove / move of immediate children and honouring `prefers-reduced-motion`. This is the single
most transferable idea in the whole sweep.

### kibo-ui — `shadcnblocks/kibo/main/`

Base path: `apps/docs/examples/`

```
gantt.tsx   marquee.tsx   code-block.tsx   color-picker.tsx
# second visit (2026-11-06):
contribution-graph.tsx   ticker.tsx   rating.tsx   comparison.tsx
```

Plus root `license.md`. `contribution-graph` maps to `LearningHeatmap`;
`ticker` / `rating` / `comparison` are behavioural references for
`TelemetryStrip`, feedback and cohort comparison — all web chrome that
has no direct Android equivalent, so they are stored, not ported.

### licenses — `motiondivision/motion/main/`

```
LICENSE.md                  → motion-LICENSE.md   (MIT, © 2024 Motion B.V.)
packages/motion/package.json → motion-package.json (v14.x metadata)
```

GSAP was **not** mirrored: its repository has no `LICENSE.md` at the expected path and GSAP is a
licensed (not OSI-open-source) library, now 100% free thanks to Webflow. See `SOURCES.md` B2 and
<https://gsap.com/pricing/>.

### animate-ui — `imskyleen/animate-ui/main/` (second visit)

```
packages/react/src/primitives/texts/sliding-number/index.tsx → sliding-number.tsx
packages/react/src/primitives/radix-ui/accordion/accordion.tsx → accordion.tsx
LICENSE.md (MIT + Commons Clause, Elliot Sutton 2025)
```

The sliding-number source backs the "no separate roller" decision in
`SweepReadout.kt`: tabular mono already fixes digit width. The accordion
source backs `NexusTransitions.plateEnter/Exit` (exits quicker than
entrances, transform + opacity only).

### coss — `cosscom/ui/main/` (second visit; `originui.com` successor)

```
packages/ui/src/components/command.tsx → command.tsx
LICENSE.md (MIT, coss.com and Origin UI, 2025)
```

Stored as a behaviour spec for `NexusCommandPalette` +
`NexusDestinationIndex` (Autocomplete-backed command dialog, keyboard
first). Not compiled into the APK.

## Not downloaded on purpose

Whole monorepos (hundreds of MB), paid/pro registries, and gallery screenshots gated behind
accounts. See the closing section of `SOURCES.md`.
