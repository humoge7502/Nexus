# Sagitta / Nexus — UI Research Sweep

**Date:** 2026-11-06 · **Sites requested:** 17 · **Sites visited:** 17 · **Skipped:** 0

All 17 URLs were fetched. Real open-source component source was downloaded (39 files, ~330 KB)
and 12 reference images were saved (660 KB). Licenses were read from the downloaded files, not
assumed. The interactive companion is [`index.html`](index.html); the shippable code is
[`COMPOSE_PORTS.md`](COMPOSE_PORTS.md).

**Second visit (2026-11-06, same-day re-verify):** all 17 URLs fetched again live.
17 additional open-source files downloaded (`border-beam`, `dot-pattern`,
`marquee`, `scroll-based-velocity`, `TrueFocus`, `border-trail`,
`toolbar-dynamic`, `infinite-slider`, `contribution-graph`, `ticker`,
`rating`, `comparison`, Animate UI `sliding-number` + `accordion` +
LICENSE, coss `command` + MIT LICENSE) → 56 source files total (~300 KB).
Ports 1, 2 and 7-alias shipped as original Kotlin (see below); no React
code is compiled into the APK. Live deltas recorded in the appendix at
the end of this file.

## The one thing to know first

`docs/DESIGN_DIRECTION.md` §7 ("Anti-generic commitments") **bans** generic purple-blue gradients,
glassmorphism everywhere, identical icon-title-description cards, decorative floating shapes /
random glows, fake terminals and pill-everything. Most of what these nine component libraries sell
is exactly that. They are also all **React / Tailwind / web** libraries, while Sagitta is a
**Jetpack Compose Android app** with its own `core/designsystem`.

So this sweep is not "copy the prettiest components". It is: **take the motion engineering and
component behaviour, translate it into the existing design system, and reject the rest.** The
verdict column says which is which.

- **ADOPT (6)** — open source, transferable source downloaded; port it.
- **ADAPT (5)** — borrow the pattern/spec; do not ship the code.
- **REFERENCE (6)** — visual research only; no shippable code.

---

## A. Advanced components (9)

### A1 · 21st.dev — <https://21st.dev/> · ADAPT

- **What it is:** 12,000+ community components, 371 templates, 965 themes, 366 live shaders,
  29,000 icons, 138 open-source apps. React + Tailwind on shadcn/ui conventions; installs via
  MCP / CLI / AI agent ("real source lands in your repo").
- **Best things today:** Liquid Metal Hero, Hover Preview, Hero ASCII One, Vercel Hero, Waitlist
  Hero, Shader Animation, WebGL Shader, Background Paths. Categories: Heroes & motion (1.2K hero
  sections, 617 shaders, 530 backgrounds), Buttons 1.7K, Cards & grids 1.3K, AI chats 315,
  Navigation 509.
- **License:** mixed free tier + paid plan; components are community-submitted — check each item.
- **Keep:** semantic icon search (search by meaning, not name), the shader builder, and the
  "install real source into your repo via agent" workflow.
- **Drop:** WebGL/shader heroes. They need a GPU compositor, infinite animation and a mouse.
- **Local:** 4 posters in `references/`.

### A2 · Aceternity UI — <https://ui.aceternity.com/> · REFERENCE

- **What it is:** 118+ free components, 183+ blocks (All-Access), 17 Next.js templates, 5 full
  landing pages. React + Tailwind + Motion; shadcn CLI or MCP install.
- **Best things:** 3D Card Effect, Apple Cards Carousel, Wobble Card, Hero Highlight, Background
  Beams, 3D Marquee, Text Flipping Board, Liquid Glass, Cloud Shader, Dither Shader hero.
- **License:** 118 components free; blocks/templates paid ($79 per template, All-Access for blocks).
- **Verdict:** visually the strongest of the nine and the least shippable here. 3D tilt, beams,
  liquid glass and bento grids conflict with §4 (editorial asymmetry, hairlines, fewest cards) and
  §7. Use it as a craft benchmark, not a component source.
- **Local:** 5 reference images in `references/`.

### A3 · Magic UI — <https://magicui.design/> · **ADOPT**

- **What it is:** 150+ free and open-source animated components. React + TypeScript + Tailwind +
  Motion, shadcn/ui companion. `npx shadcn@latest add https://magicui.design/r/<component>`.
- **License:** **MIT** — downloaded `LICENSE.md` confirms.
- **Full component index captured:** Android, Animated Beam, Animated Circular Progress Bar,
  Animated Gradient Text, Animated Grid Pattern, Animated List, Animated Shiny Text, Theme Toggler,
  Aurora Text, Avatar Circles, Backlight, Bento Grid, Blur Fade, Border Beam, Code Comparison,
  Comic Text, Confetti, Cool Mode, Dia Text Reveal, Dither Field, Dock, Dot Pattern, Dotted Map,
  File Tree, Flickering Grid, Floating 3D Particles, Glare Hover, Globe, Glyph Matrix, Grid Pattern,
  Hero Video Dialog, Hexagon Pattern, Text Highlighter, Hyper Text, Icon Cloud, Interactive Grid
  Pattern, Interactive Hover Button, iPhone, Kinetic Text, Lens, Light Rays, Line Shadow Text,
  Magic Card, Marquee, Meteors, Morphing Text, Neon Gradient Card, Noise Texture, Number Ticker,
  Orbiting Circles, Particles, Pixel Image, Pointer, Progressive Blur, Pulsating Button, Rainbow
  Button, Retro Grid, Ripple Button, Ripple, Safari, Scroll Based Velocity, Scroll Progress,
  Shimmer Button, Shine Border, Shiny Button, Smooth Cursor, Sparkles Text, Spinning Text, Striped
  Pattern, Terminal, Text 3D Flip, Text Animate, Text Reveal, Tweet Card, Typing Animation, Video
  Text, Warp Background, Word Rotate.
- **Best for Sagitta:** `Number Ticker`, `Animated Circular Progress Bar`, `Blur Fade`,
  `Text Animate`, `Scroll Progress`, `Animated List`, `Grid Pattern`, `Animated Beam`,
  `Word Rotate`. These map to `TelemetryDisplay`, `InstrumentGauge`, `revealClock`,
  `CalibratedRule`, `instrumentGrid` and `StreamingAIResponse`.
- **Drop:** Meteors, Aurora, Sparkles, Confetti-as-decoration, Warp, Neon Gradient, Marquee.
- **Local:** `sources/magic-ui/` — 10 `.tsx` files + `LICENSE.md`.

### A4 · React Bits — <https://reactbits.dev/> · **ADOPT** (with a license caveat)

- **What it is:** free animated React components and backgrounds installed as source you own.
  JS or TS × plain CSS or Tailwind; copy source or use the CLI. Categories: Text Animations,
  Animations, Components, Backgrounds, Micro Interactions.
- **License:** **MIT + Commons Clause v1.0** (David Haz, 2026). Permits use, modification and
  distribution *as part of an application, website or product*, including commercial ones.
  **Forbids selling, sublicensing or redistributing the components themselves — alone, in a
  bundle, or as a ported version.** Fine for Sagitta; would matter if you ever shipped a
  "Nexus UI kit" as a product. Add it to `NOTICE` if any code is ported.
- **Best things:** `CountUp`, `SplitText`, `BlurText`, `AnimatedContent`, `FadeContent`, `Stepper`,
  `Magnet`, `ClickSpark`, `TrueFocus`. New: Flying Button, Gradient ASCII, Flux Lines, Holo Card,
  Crystalized Ball, Pattern Waves, Circular Carousel, Flex Carousel, Dither Veil, Micro Slats,
  Shredder, Paper Crumple, Tear Ticket, Bell Toggle, Branched Menu, Tech Text.
- **Best for Sagitta:** `CountUp` and `SplitText` for the serif editorial line; `AnimatedContent`
  for tab/detail transitions; `Stepper` for onboarding. Skip `Magnet` / `ClickSpark` (pointer).
- **Local:** `sources/react-bits/` — 8 `.tsx` (TS + Tailwind variants) + `LICENSE.md`.

### A5 · Motion Primitives — <https://motion-primitives.com/> · **ADOPT**

- **What it is:** copy-paste, customizable, open-source motion components built on Motion +
  Tailwind (by ibelick / Edouard Wilfrid Buquet).
- **License:** **MIT** — downloaded `LICENCE.md` confirms.
- **Best things:** `sliding-number`, `animated-number`, `text-morph`, `in-view`,
  `transition-panel`, `scroll-progress`, `morphing-dialog`, `cursor`. Also accordion, toolbar,
  animated background, glow effect, spotlight, image compare, growing text, segmented control.
- **Why it wins:** the most restrained library in the list — small primitives that answer a real
  state change rather than hero spectacle. `in-view` + `sliding-number` is essentially Sagitta's
  reveal-then-sweep model.
- **Local:** `sources/motion-primitives/` — 8 `.tsx` + `LICENCE.md`.

### A6 · Animate UI — <https://animate-ui.com/> · **ADOPT** (the spec, not the code)

- **What it is:** animated drop-in primitives for **Radix UI, Base UI and React Aria**, built with
  Motion. It animates the *primitive*, not the component — swap one import and keep your API,
  styles and accessibility. `npx @animate-ui/cli@latest animate all`. Ships an AI "skill".
- **License:** free CLI; exact license not published on the site — treat as free-to-use, verify
  before redistribution.
- **Components:** Switch, Accordion, Progress, Sliding Number, plus animated primitives for the
  three base libraries.
- **The valuable part — its motion rules, verbatim in spirit:**
  1. Springs and short tweens on a strong ease-out.
  2. **Exits quicker than entrances.**
  3. Transform + opacity, never layout.
  4. Interruptible animations.
  5. **Nothing on keyboard navigation.**
  6. Reduced motion respected everywhere, without setup.
  7. "A menu opened fifty times a day has to feel quick, not impressive."
- **Verdict:** these seven lines are a near-perfect restatement of `docs/DESIGN_SYSTEM.md` §5.
  Adopt them as a review checklist. Do not port React primitives into Compose.
- **Local:** spec captured here and in `COMPOSE_PORTS.md`.

### A7 · Origin UI — <https://originui.com/> · ADAPT · **REDIRECT**

- **Finding:** `originui.com` now **301-redirects to <https://coss.com/ui>**, "A new, modern UI
  component library built on top of Base UI". The Origin UI brand/content is no longer at that URL.
- **What is there now:** 55+ Base UI primitives — Accordion, Alert, Autocomplete, Avatar, Badge,
  Breadcrumb, Button, Calendar, Card, Checkbox, ComboBox, **Command**, **Composer** (AI chat input),
  Context Menu, Date Picker, Dialog, Drawer, **Empty**, Field, Form, Frame, Input, Kbd, Label,
  **Meter**, Number Field, OTP Field, Pagination, Popover, Preview Card, Progress, Radio Group,
  Scroll Area, Select, **Segmented Control**, Separator, Sheet, Skeleton, Slider, Spinner, Switch,
  Table, Tabs, Toast, Toggle, Toolbar, Tooltip.
- **License:** free Base UI primitives; not stated on the landing page.
- **Verdict:** use as an **accessibility and behaviour specification** for `NexusCommandPalette`,
  `EmptyState`, `NexusAdaptiveNavigation` and `CalibratedRule` (Meter/Progress). No visual source.

### A8 · Kibo UI — <https://www.kibo-ui.com/> · ADAPT

- **What it is:** "High quality shadcn/ui components" — a composable custom registry. Free and
  open source, forever. `npx kibo-ui add`. GitHub: `shadcnblocks/kibo` (3.7k stars).
- **License:** MIT-style permission grant — downloaded `license.md` confirms.
- **Best things:** Color Picker (Figma-modeled), Image Zoom, QR Code, Code Block, Dropzone,
  Marquee; blocks: Collaborative Canvas, Form, **Roadmap / Gantt** (Gantt / Calendar / List /
  Kanban / Table views); 1000+ production patterns.
- **Verdict:** the Roadmap/Gantt block is a legitimate pattern candidate for `MomentumChart` and
  learning timelines. Everything else is desktop-web chrome.
- **Local:** `sources/kibo-ui/` — 4 `.tsx` + `license.md`.

### A9 · ReUI — <https://reui.io/> · ADAPT

- **What it is:** "the agent-ready layer for shadcn/ui" — **1,264 free components across 87
  categories**, **678 Pro blocks** (7 groups, 79 categories), **667 hand-crafted animated icons**,
  multi-page starter templates, a **free MCP server** and agent skills. Endorsed by shadcn's creator.
  All-Access: $249 once, lifetime.
- **Biggest categories:** Button 61, Button Group 57, Stepper 44, Input Group 40, Avatar 35,
  Data Grid 34, Select 33, Input 31, Calendar 30, Combobox 28, Code Block 27, Badge 25, Chart 25,
  Trace 22, Sparkline 11, Gantt 7, Empty State 20.
- **Pro block groups:** Application 305, eCommerce 87, Solutions 82, Marketing 67, Data Grid 61,
  Charts 54, AI & Agents 22.
- **Templates:** Signal (TanStack Start analytics), Strata (Next.js observability), Mesh
  (whiteboard), Flowly (workflow), Echo (AI chat), Pulse (help desk), Filespace (cloud storage).
- **License:** free tier + paid all-access; exact license not published on the page — verify.
- **Verdict:** the **MCP / agent registry** idea is the most strategically interesting item in this
  entire list for an AI-assisted repo. For UI, borrow Data Grid filtering, Sparkline and Empty State
  patterns. Also note: their Data Grid is the most praised feature in their own wall of love.

---

## B. Animation (3)

### B1 · Motion — <https://motion.dev/> · **ADOPT**

- **What it is:** the animation library formerly called Framer Motion. Layout, scroll, gestures,
  3D and hardware acceleration for React, Vue and plain JS. **Free and open source**, MIT
  (downloaded `LICENSE.md`, © 2024 Motion B.V.). `npm install motion && npx motion-ai`.
  Current version 14.9.0 (30 Oct 2026). Motion+ (components, examples vault, Motion Studio visual
  editor, AI Kit, Performance Doctor) is paid.
- **Best capabilities:** `layoutId` shared-element layout animations; `AnimatePresence` exit
  animations; physics springs that carry velocity (`stiffness / damping / mass`); scroll-linked
  values using native `ScrollTimeline`; gestures (press works from keyboard, hover ignores touch,
  drag throws with momentum); hardware acceleration; 3.2kb mini `animate` + LazyMotion.
- **Web → Compose:** `layoutId` → shared-element bounds / `LookaheadScope`; `AnimatePresence` →
  `AnimatedVisibility`; `useSpring` → `NexusSpring.Snappy`; `useScroll` → `CalibratedRule`.
- **Local:** `sources/licenses/motion-LICENSE.md` + `motion-package.json`.

### B2 · GSAP — <https://gsap.com/> · ADAPT

- **What it is:** professional JS animation — "animate anything JS can touch". Timeline
  choreography, easings, SVG / UI / text toolsets.
- **License (verified on gsap.com/pricing, 2026):** **"GSAP is now 100% free for all users,
  thanks to Webflow's support."** The entire library including formerly Club-only plugins is free:
  Draggable, Inertia, ScrollTrigger, ScrollSmoother, Observer, Flip, SplitText, ScrambleText,
  Physics2D/Props, CustomBounce/Wiggle, DrawSVG, MorphSVG, MotionPath/Helper, GSDevTools.
  Maintained by the original GSAP team at Webflow. **It is a licensed library, not OSI open
  source** — read the current license before redistributing. `npm install gsap`.
- **Best for Sagitta:** the **timeline model** (one master clock, per-element windows) is exactly
  `revealClock()` + `revealFraction()`; `Flip` is the layout-change equivalent of
  `animateItemPlacement`; `SplitText` informs the serif line reveal.
- **Verdict:** port the choreography discipline. No reason to bring a JS runtime into Compose.

### B3 · AutoAnimate — <https://auto-animate.formkit.com/> · **ADOPT**

- **What it is:** "a zero-config, drop-in animation utility that adds smooth transitions to your
  web app" — **one function**, `autoAnimate(parentElement)`, that animates a parent's immediate
  children whenever a child is **added, removed or moved**. React (`useAutoAnimate`), Vue,
  Svelte, Solid, Preact, Angular, Marko, plain JS. `yarn add @formkit/auto-animate` (v0.10.1).
- **License:** MIT (FormKit Inc., 2022) — downloaded `LICENSE` confirms.
- **Accessibility:** respects `prefers-reduced-motion` and disables itself automatically.
- **Caveat:** list `key` values must be unique or the animation misbehaves — same requirement as
  Compose `LazyColumn` keys.
- **Verdict:** the single most transferable idea in the whole sweep. In Compose this is
  `Modifier.animateItemPlacement()` + `AnimatedVisibility` — one modifier replaces every
  hand-written list transition, and `LocalReducedMotion` already supplies the off switch.
- **Local:** `sources/auto-animate/` — `index.ts`, `LICENSE`, `package.json`.

---

## C. Design inspiration (5)

### C1 · Godly — <https://godly.website/> · REFERENCE · **REDIRECT**

- **Finding:** `godly.website` redirects to **<https://recent.design/?ref=godly>**. The product is
  now a design feed: motion inspo slides, brand identity posts, community links, design jobs
  (Generalist Engineer at pen.dev, Visual Designer at MOMENT, Product Designer at Paper…).
- **Verdict:** no stable component catalog and nothing downloadable. Recorded so the URL isn't
  silently dropped. Use as a trend feed only.

### C2 · Awwwards — <https://www.awwwards.com/> · REFERENCE

- **What it is:** website awards / best web design trends. **Site of the Day 05 Nov 2026:
  "Agrumea Farm" by Studio K95, score 7.33/10.** Recent winners: Grail, DeepBook, Grids (Obys),
  Dont Look Up game, MECHA, Spyker Cars. Nominees: Fine Acts, Griflan Labs, Ethiack.
- **Structure:** Winners, Nominees, Collections (e.g. "Pastel colors" +171 followers), Directory
  (6,123 agencies/professionals), Academy courses, Market templates, Blog.
- **Verdict:** art-direction benchmark. Winning sites are scroll-theatre, pointer-driven experiences —
  the opposite of a measured mobile instrument. Do not treat as a UI spec.

### C3 · Mobbin — <https://mobbin.com/> · REFERENCE (**most useful of the five**)

- **What it is:** "The design reference library for apps and sites. Over **600,000 shipped
  screens**, new every week." Organised into **Apps, Sites, Screens, Flows, Animations, UI
  Elements**. Featured apps include Linear, Strava, Perplexity, Duolingo, Cash App, Headspace,
  Claude, ChatGPT, Notion, Spotify, Coinbase, Airbnb, Wise, Arc Search, Cursor, Granola.
- **Why it matters here:** it is the only inspiration source on this list showing **real production
  mobile screens and real flows** — including empty, loading and error states, which
  `docs/DESIGN_SYSTEM.md` §9 requires. Use it to benchmark the five tabs.
- **Limitation:** screen downloads are gated behind an account; direct image CDN requests returned
  404/400. Curated links are recorded instead of scraped assets.

### C4 · Dribbble — <https://dribbble.com/> · REFERENCE

- **What it is:** community shot platform. Popular feed captured on 06 Nov 2026: Bovine (George
  Bokhua, 14.1k), Pet Merch ID, Negative Space Marks Posters, **Cricket Sports Mobile App — Match
  Schedule UI**, Healthcare Dashboard – Physiotherapy Platform, AI Creative Platform Mobile App,
  Mobile Ecommerce Shopping Cart Flow, Orvyn AI Workflow Platform, Shora landing page animation.
- **Useful tags:** mobile app design, dashboard, ui ux, product design, animation, 3D.
- **Verdict:** high visual polish, low production truth — shots rarely show error/empty/loading
  states and the current popular feed is logo/branding-heavy. Filter hard; never treat a shot as
  a spec. Root URL returns HTTP 202 with an empty body; `/shots/popular` works.
- **Local:** 2 reference images in `references/`.

### C5 · Refero — <https://refero.design/> · REFERENCE

- **What it is:** "Design Research for the AI Era" — real product screens, flows and patterns to
  help designers, builders and AI make better design decisions.
- **Verdict:** useful for pattern research on onboarding, empty states and AI conversation
  surfaces (`EditorialConversation`, `StreamingAIResponse`). Content is gated; nothing
  downloadable. Captured as a curated research link.

---

## Licensing checklist (read before shipping)

| Source | License | Verified from | OK inside the app? | Redistributable as a kit? |
|---|---|---|---|---|
| Magic UI | MIT | downloaded `LICENSE.md` | Yes | Yes, with notice |
| Motion Primitives | MIT | downloaded `LICENCE.md` | Yes | Yes, with notice |
| AutoAnimate | MIT | downloaded `LICENSE` | Yes | Yes, with notice |
| Kibo UI | MIT-style | downloaded `license.md` | Yes | Yes, with notice |
| Motion | MIT | downloaded `LICENSE.md` | Yes | Yes, with notice |
| **React Bits** | **MIT + Commons Clause** | downloaded `LICENSE.md` | **Yes, incl. commercial** | **No** — not alone, bundled or ported |
| GSAP | 100% free (Webflow), proprietary-license | `gsap.com/pricing` | Yes | Verify current license text |
| 21st.dev / Aceternity / Animate UI / coss ui / ReUI | mixed free + paid, per-item | site content | Free tiers only | Varies — verify per item |
| Godly / Awwwards / Mobbin / Dribbble / Refero | creators' / platform terms | — | Inspiration only | No |

## What I deliberately did not download

1. **Whole repositories.** These are multi-hundred-MB monorepos; I pulled the specific component
   files that map to Sagitta, plus their licenses. `sources/README.md` lists every file and its
   exact upstream URL.
2. **Gated galleries.** Mobbin screens, Refero flows and Awwwards full-size screenshots require
   accounts. Links are curated instead of scraped.
3. **Pro/paid assets.** Aceternity blocks, ReUI Pro blocks, 21st.dev paid themes, Motion+ vault.
   Not purchasable or licensable from here.
4. **Anything into `core/designsystem/` (first visit).** No app source file was modified on
   the first visit; `COMPOSE_PORTS.md` then held proposals.
   On the **second visit (2026-11-06)** the remaining shippable ports
   were implemented as original Kotlin: `SweepReadout.kt`,
   `NexusListPlacement.kt`, `NexusDestinationIndex.kt`, plus
   `NexusAnimation.Springs.itemPlacement` and a `NexusCommand.aliases`
   addition. They build (`:core:designsystem:assembleDebug` green) and
   their pure logic is covered by 13 JVM tests. React source stays in
   `sources/` only; nothing under `app/` / `feature/` / `core/` contains
   `.tsx`.

## Appendix — second visit live deltas (2026-11-06)

| Site (live fetch) | What changed / confirmed live |
|---|---|
| 21st.dev | 12,000+ components, MCP + CLI (`npx @21st-dev/cli init`), 2 free copies/day, registry format confirmed. Behaviour: keep semantic icon search. |
| Aceternity | Now **200+ components** (was 118), 183+ blocks, 17 templates, 5 pages; Liquid Glass + Text Flipping Board still headline. Still REFERENCE. |
| Magic UI | 150+ components, 23.1k GitHub stars, MIT confirmed. Downloaded 4 more (`border-beam` ships as `Modifier.borderBeam()`; `marquee`/`scroll-based-velocity` stored as rejected). |
| React Bits | Text Animations / Animations / Components / Backgrounds / Micro. Downloaded `TrueFocus.tsx` (REJECTED for ship: infinite focus carousel violates §5; stored for review). MIT+Commons Clause still applies — repo-root `NOTICE.md` added. |
| Motion Primitives | MIT. Downloaded `border-trail` (ships as `borderBeam` sibling), `toolbar-dynamic`, `infinite-slider` (rejected reference). |
| Animate UI | Now ships Radix + Base UI + React Aria primitives via CLI + AI skill. Downloaded `sliding-number` + Radix `accordion` + LICENSE (**MIT+Commons Clause**, Elliot Sutton). Motion rules adopted as review checklist. |
| Origin → coss.com | Still 301 → `coss.com/ui`, 530 particles, 10.8k stars, MIT (LICENSE at repo root). Downloaded `command.tsx` as behaviour spec for palette/index. |
| Kibo UI | 41 components, 28 blocks, 1,101 patterns, 3.8k stars, free/open-source. Downloaded `contribution-graph`, `ticker`, `rating`, `comparison`. |
| ReUI | **1,264 free components** (87 categories), 678 Pro blocks, 667 icons, MCP + skills; free MIT confirmed on page. Borrow: Data Grid, Sparkline, Empty State. |
| Motion (motion.dev) | **v14.9.0 (30 Oct 2026)**, MIT. Layout springs / AnimatePresence / scroll springs confirmed. |
| GSAP | "GSAP is now 100% free for everyone, thanks to Webflow" — confirmed live. Not OSI open source; do not mirror. |
| AutoAnimate | MIT (FormKit, 2026) confirmed; `autoAnimate(parent)` + `prefers-reduced-motion` confirmed. Ships as `nexusItemPlacement` + `itemPlacement`. |
| Godly → recent.design | Still redirects. Feed + jobs (pen.dev, Paper, MOMENT). Trend feed only. |
| Awwwards | SOTD 05 Nov 2026: "Agrumea Farm" (Studio K95, 7.33/10). Winners Grail, DeepBook, Grids, MECHA, etc. Art-direction benchmark only. |
| Mobbin | 600,000+ screens, llms.txt/MCP/REST API docs live. Most useful inspiration source; downloads still gated. |
| Dribbble | Root returns empty body (JS-gated); `/shots/popular` not fetchable via simple HTTP — matches first-visit note. Reference only. |
| Refero | Root fetch empty (JS-gated); search confirms "Refero MCP ... real product interfaces and user flows" + Figma plugin. Reference only. |
