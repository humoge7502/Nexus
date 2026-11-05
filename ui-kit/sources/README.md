# Downloaded source — attribution manifest

Downloaded 2026-11-06. These are **real upstream files**, copied verbatim from each project's
default branch. They are reference material for porting ideas into `core/designsystem`; none of
them is wired into the Android build. Each folder keeps its original license file — **read it
before shipping anything**.

| Folder | Upstream | License file | Files |
|---|---|---|---|
| `magic-ui/` | `magicuidesign/magicui` @ `main` | `LICENSE.md` (MIT) | 11 |
| `react-bits/` | `DavidHDev/react-bits` @ `main` | `LICENSE.md` (**MIT + Commons Clause**) | 9 |
| `motion-primitives/` | `ibelick/motion-primitives` @ `main` | `LICENCE.md` (MIT) | 9 |
| `auto-animate/` | `formkit/auto-animate` @ `master` | `LICENSE` (MIT) | 3 |
| `kibo-ui/` | `shadcnblocks/kibo` @ `main` | `license.md` (MIT-style) | 5 |
| `licenses/` | `motiondivision/motion` @ `main` | `motion-LICENSE.md` (MIT) | 2 |

**Total: 39 files.**

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
```

Plus root `LICENSE.md`.

### react-bits — `DavidHDev/react-bits/main/`

Base path: `src/ts-tailwind/` (TypeScript + Tailwind variants)

```
TextAnimations/CountUp/CountUp.tsx           TextAnimations/SplitText/SplitText.tsx
TextAnimations/BlurText/BlurText.tsx         Animations/AnimatedContent/AnimatedContent.tsx
Animations/FadeContent/FadeContent.tsx       Components/Stepper/Stepper.tsx
Animations/Magnet/Magnet.tsx                 Animations/ClickSpark/ClickSpark.tsx
```

Plus root `LICENSE.md`. **MIT + Commons Clause v1.0:** usable inside an application, website or
product including commercial ones; you may **not** sell, sublicense or redistribute the components
themselves — alone, bundled, or as a ported version. Add a NOTICE entry if any code is ported.

### motion-primitives — `ibelick/motion-primitives/main/`

Base path: `components/core/`

```
sliding-number.tsx   animated-number.tsx   text-morph.tsx   transition-panel.tsx
in-view.tsx          scroll-progress.tsx   morphing-dialog.tsx   cursor.tsx
```

Plus root `LICENCE.md`.

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
```

Plus root `license.md`.

### licenses — `motiondivision/motion/main/`

```
LICENSE.md                  → motion-LICENSE.md   (MIT, © 2024 Motion B.V.)
packages/motion/package.json → motion-package.json (v14.x metadata)
```

GSAP was **not** mirrored: its repository has no `LICENSE.md` at the expected path and GSAP is a
licensed (not OSI-open-source) library, now 100% free thanks to Webflow. See `SOURCES.md` B2 and
<https://gsap.com/pricing/>.

## Not downloaded on purpose

Whole monorepos (hundreds of MB), paid/pro registries, and gallery screenshots gated behind
accounts. See the closing section of `SOURCES.md`.
