# Nexus UI/UX Research & Compose Integration Plan

> Research date: 2026-11-06  
> Target: `humoge7502/Nexus` — native Android / Kotlin / Jetpack Compose  
> Scope: all 17 requested references. This document records what is useful, what is not directly installable, licensing cautions, and the native implementation plan.

## Executive decision

Nexus is not a React web app. Its production stack is Kotlin 2.1.21, Jetpack Compose, Material 3, Android SDK 36, MVVM + Clean Architecture, Hilt, StateFlow and type-safe Compose Navigation. React/Tailwind/shadcn components cannot be installed into this application as Android UI components.

The correct strategy is to use the component libraries as visual/interaction references, then implement or refine equivalent native Compose components inside `core/designsystem`. Do not add a WebView or a parallel React application just to consume these libraries.

The repository already has a distinctive design direction ("precision instrument with an editorial voice"), a documented token system, motion system, loading/empty states, and a substantial component catalogue. Therefore, this is an audit-and-elevate exercise, not a wholesale replacement. Preserve that identity; avoid generic glassmorphism, purple-blue gradients, indiscriminate card grids, and decorative animation.

## The 17 references — audited individually

### Advanced component sources (9)

| Source | What it is best for | Nexus use | Integration decision |
|---|---|---|---|
| [21st.dev](https://21st.dev/) | Broad designer-authored component marketplace, animated heroes, shaders, cards, navigation, AI-chat patterns and agent/CLI workflows | Inspiration for distinctive component treatments, especially AI chat, command surfaces, cards and visual polish | **Reference only.** Marketplace items have author-specific rights; do not bulk-copy or vendor source. Verify the exact item's license and terms before reuse. |
| [Aceternity UI](https://ui.aceternity.com/) | High-impact visual effects, animated backgrounds, 3D cards, spotlight/beam effects, carousels and marketing blocks | Select restrained treatments for feature introductions, AI/research surfaces and milestone moments | **Translate selectively to Compose.** React + Tailwind + Motion source is incompatible. Free/Pro rights differ; Pro license allows end-product use but prohibits source redistribution. Check per-item terms. |
| [Magic UI](https://magicui.design/) | Polished motion primitives: number ticker, marquee, beam, globe, shimmer, text reveal, animated grid and progress effects | Number transitions, milestone ticker, subtle shimmer, lightweight list/section reveals | **Reference and port.** The official install flow targets shadcn/React. Do not install its React package into the Android app. Check each component's current license and dependencies before copying code. |
| [React Bits](https://reactbits.dev/) | Creative text animation, backgrounds, micro-interactions, cursor and image effects; JS/TS and CSS/Tailwind variants | A source of motion concepts for progress reveal, subtle ambient effects and achievement moments | **Reference only for Android.** React source is incompatible. The upstream project identifies an MIT + Commons Clause license; review its restrictions and component-level dependencies before any source reuse. |
| [Motion-Primitives](https://motion-primitives.com/) | Open-source animated UI components built around Motion and Tailwind | Spring, stagger, reveal and transition behavior references | **Port behavior, not React source.** Recreate with Compose animation APIs and the existing Nexus motion system. |
| [Animate UI](https://animate-ui.com/) | Animated accessible primitives built on Radix, Base UI or React Aria and Motion | Useful interaction and accessibility references for menus, dialogs, check states and navigation | **Not installable in Compose.** Its package targets React 19 and web primitive ecosystems. Translate interaction specifications to Compose Material 3/semantics. |
| [Origin UI](https://originui.com/) | Everyday application controls and variants: inputs, buttons, date pickers, selects, tables, pagination, dialogs, upload, timeline | Audit Nexus form controls, filters, date selection, stepper, table-like lists and dialogs for consistency | **Reference only.** React + Tailwind components cannot be dropped into Android. Use Compose-native controls and maintain one canonical component per role. |
| [Kibo UI](https://www.kibo-ui.com/) | Higher-level composites: kanban, Gantt, file dropzone, code blocks, image cropper, QR and AI chat primitives | Applications pipeline, learning plans, interview timelines, AI chat and document upload patterns | **Reference and adapt.** Open-source React/shadcn companion; no direct Android integration. Reuse domain logic only if separately licensed and technically portable. |
| [ReUI](https://reui.io/) | Product-ready data grids, kanban, event calendars, Gantt, filters, cascaders, steppers and realistic dashboard examples | Applications tracker, opportunity filtering, interview calendar/timeline, learning roadmap and structured data views | **Reference and adapt.** Free examples/primitives and paid blocks/icons have different access conditions. Do not mistake the registry namespace for an npm package; its documented install path is shadcn CLI. |

### Animation sources (3)

| Source | Best capability | Nexus use | Integration decision |
|---|---|---|---|
| [Motion](https://motion.dev/) | Spring transitions, layout/gesture/scroll animation, enter/exit presence, stagger | Motion behavior references for navigation, state changes, progress and content arrival | **Use native Compose equivalents.** Do not add the JavaScript Motion runtime to Android. |
| [GSAP](https://gsap.com/) | Precise timeline choreography, sequencing, ScrollTrigger, SVG and text animation | Reference for orchestrating complex multi-step reveals and milestone sequences | **Do not add GSAP.** Use Compose `Transition`, `Animatable`, `updateTransition`, `AnimatedVisibility`, `animate*AsState`, and `Canvas`/draw-phase animation. |
| [AutoAnimate](https://auto-animate.formkit.com/) | Zero-config FLIP-style add/remove/reorder transitions for lists | Dynamic list changes in opportunities, saved items, DSA tasks, applications and notifications | **Recreate only where useful.** Native Compose lazy-list item placement animation and AnimatedVisibility are preferable; avoid adding a web DOM mutation library. |

### Design inspiration sources (5)

| Source | Best use | Nexus research focus | Guardrail |
|---|---|---|---|
| [Godly](https://godly.website/) | Curated standout web/app visual design | Distinctive visual hierarchy, editorial layouts, art direction and restrained motion | Inspiration, not a source-code library. The domain currently redirects; verify the current destination when researching examples. |
| [Awwwards](https://www.awwwards.com/) | Award-winning digital experiences and interaction craft | Art direction, typography, transitions, storytelling and polished details | Primarily web experiences; adapt ideas to mobile ergonomics and Android performance. |
| [Mobbin](https://mobbin.com/) | Real shipped mobile/web screens, flows, animations and UI elements | Onboarding, sign-in, home/dashboard, AI chat, filters, paywalls, profile, settings, empty/error/loading states | Prefer proven product patterns over decorative Dribbble concepts. Some research features require paid access. |
| [Dribbble](https://dribbble.com/) | Broad visual exploration and designer portfolios | Mobile dashboards, career/education apps, analytics, AI products, visual motifs and composition | Treat shots as concepts, not validated UX. Do not copy artwork or proprietary layouts. |
| [Refero](https://refero.design/) | Real product screens and end-to-end flows, with research/search tooling | Dashboard, onboarding, subscription, product details, profile, workflows, AI chat and data tables | Use to validate real interaction patterns; access may be limited by plan. Do not reproduce proprietary screenshots/assets. |

## Existing Nexus design direction to preserve

The repository's `docs/DESIGN_DIRECTION.md` and `docs/DESIGN_SYSTEM.md` are the authority. The existing identity is a precision career instrument with an editorial voice:

- Ink/paper surfaces; acid-lime `Signal #C3F53C` as the single primary signal; Structure indigo reserved for secondary data; ember reserved for attention.
- Plus Jakarta Sans for UI hierarchy, Inter for prose, JetBrains Mono for measured values, Newsreader for narrative.
- Numbered sections, calibrated instrument plates, hairline separators, generous whitespace between dense data surfaces, and restrained signature gradients.
- Existing design-system components include NexusCard/GlassCard, NexusButton, NexusTextField, NexusChip, NexusTopBar, NexusBottomBar, NexusAdaptiveNavigation, InstrumentGauge, StatCard, TelemetryStrip, MomentumChart, SkillConstellation, LearningHeatmap, Editorial compositions, EmptyState, LoadingState/shimmer, AchievementBurst and NexusCommandPalette.
- Reduced-motion support, contrast rules, 48dp touch targets, TalkBack semantics, dynamic type, localized strings and preview coverage are explicit requirements.

**Do not create duplicate components** until the existing catalogue has been inspected. Prefer improving an existing canonical component over adding a second version.

## Recommended native Compose upgrade backlog

Priority is based on product impact, reuse and fit with the existing identity.

### P0 — Audit and consistency

1. Inventory every screen in the 24 feature modules and map its controls to the canonical design-system components.
2. Capture screenshots for Home, Explore, AI, Growth, Forecast, Applications, Certifications, DSA, Tribe, Interview Log, Portfolio and Profile in light, dark and AMOLED themes.
3. Audit spacing, typography, content density, icon alignment, card radius, surface hierarchy, loading/error/empty states and CTA hierarchy.
4. Remove raw Material buttons/cards/text fields from feature modules where the design-system contract requires canonical Nexus components.
5. Check 200% font scaling, TalkBack order, contrast, 48dp touch targets and reduced-motion behavior.

### P1 — High-value reusable surfaces

- **Career readiness hero:** instrument-plate composition with score, confidence/context, trend and one clear next action. Avoid turning every metric into a separate card.
- **Opportunity discovery:** high-quality filter chips, sort/filter bottom sheet, saved state, clear result count, useful empty state and stable list transitions.
- **Applications pipeline:** native status board/list with clear status labels, timeline, next action and accessible status indicators.
- **AI conversation:** readable streaming response, citation/source affordances, composer states, retry/error handling, attachment state and clear distinction between user and assistant messages.
- **Learning roadmap:** stepper/timeline, milestones, progress and next action; preserve meaningful hierarchy.
- **Analytics:** accessible charts with honest data, labeled axes/units, selected-period controls and textual summaries.
- **Skeleton system:** screen-shaped placeholders that match final content geometry; avoid spinner-only page loading.
- **Command palette/search:** keyboard and touch accessible destination/action search, recent items and grouped results.
- **Empty/error/offline states:** editorial voice, actionable recovery, retry and offline status; no decorative dead ends.

### P2 — Signature motion and finish

- One shared reveal clock per screen; stagger only in reading order.
- Consistent press-scale + haptic response for tappable controls.
- Shared spring/timing tokens; no per-feature ad-hoc durations.
- Animate meaningful values and progress, not every decoration.
- Animate list insertion/removal/reordering only where it helps users understand change.
- Reserve celebration effects for real milestones; make them skippable.
- Keep infinite motion rare, lifecycle-aware and disabled under reduced motion.
- Use draw-phase/graphics-layer animation for gauges and charts; avoid layout thrash and excessive overdraw.

## Suggested implementation architecture

Keep all reusable UI in `core/designsystem`:

- `components/`: canonical controls and domain-neutral composites.
- `components/instrument/`: gauges, plates, readouts, data visualization primitives.
- `components/editorial/`: narrative compositions and voice-bearing states.
- `components/state/`: loading, empty, error, offline and success patterns.
- `theme/`: color, typography, spacing, shape, elevation, motion and transition tokens.
- `previews/`: light/dark/AMOLED preview coverage.

Feature modules should compose these components and own feature state/view models; the design system should not depend on feature modules. Keep business logic out of composables. Keep UI strings in Android resources.

## Dependency policy

Do **not** add React, Next.js, Tailwind, shadcn, Motion-for-React, GSAP or AutoAnimate dependencies to the Android application. They do not render native Compose UI.

Before adding any Android dependency, verify:
1. It solves a real gap not already covered by Compose/Material 3 or the existing design system.
2. It supports the project's Kotlin/Compose/compiler versions and minSdk.
3. Its license is compatible with the app's MIT license and distribution model.
4. It has acceptable maintenance, binary size, accessibility and runtime performance.
5. It does not duplicate existing design-system behavior.

For code or assets sourced from any catalog, record the exact item URL, author, license, attribution requirement, source revision and modifications in a per-item attribution manifest. Do not bulk-download entire marketplaces, premium assets without a license, or screenshots/illustrations from inspiration galleries.

## Acceptance criteria for the UI/UX pass

- No feature module introduces a competing design language or duplicate primitive.
- All key screens have designed loading, empty, error and offline states.
- Light, dark and AMOLED remain coherent and contrast-compliant.
- Reduced motion is honored throughout.
- 200% font scale and TalkBack workflows remain usable.
- No fake metrics, placeholder charts or non-functional controls.
- No avoidable recomposition, unbounded animation, expensive blur or unnecessary overdraw.
- Compose previews cover canonical components in supported themes.
- Screenshot regression sweep and targeted Compose UI tests pass.
- `./gradlew :app:assembleDebug`, relevant JVM/UI tests and detekt pass.

## Official references

- 21st: https://docs.21st.dev/
- Aceternity install: https://ui.aceternity.com/installation ; license: https://ui.aceternity.com/licence
- Magic UI components: https://magicui.design/docs/components ; install: https://magicui.design/docs/installation
- React Bits: https://www.reactbits.dev/ ; upstream source/license: https://github.com/DavidHDev/react-bits
- Motion-Primitives: https://motion-primitives.com/docs
- Animate UI: https://animate-ui.com/docs/react
- Origin UI: https://originui.com/
- Kibo UI: https://www.kibo-ui.com/docs
- ReUI: https://reui.io/docs
- Motion: https://motion.dev/docs
- GSAP: https://gsap.com/docs/v3/
- AutoAnimate: https://auto-animate.formkit.com/
- Godly: https://godly.website/
- Awwwards: https://www.awwwards.com/
- Mobbin: https://mobbin.com/
- Dribbble: https://dribbble.com/
- Refero: https://refero.design/

---
This document is a research and implementation specification. It does not claim that third-party React components have been downloaded or that the app UI has already been changed. Actual screen changes should follow the audit and acceptance criteria above.
