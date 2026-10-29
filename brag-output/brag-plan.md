# Brag Plan — Ascent

## 9-question rubric

1. **What is the app?**
   Ascent is an AI-powered career co-pilot Android app for Indian CSE students: it surfaces real internships, tracks applications, scores Career Readiness, and mentors with cited AI answers.

2. **Funniest / most impressive claim?**
   Your live **Career Readiness score** — a single number that moves as you learn and apply. Plus "Deep Research with cited answers" in the AI Hub.

3. **Visual hook?**
   The signature Ascent gradient (deep azure → azure → cyan daybreak) on the Home hero card, with the precision-instrument grid texture and the readiness dial.

4. **What should be shown from the actual UI?**
   Home hero (readiness score + greeting), Explore list with real internships (Goldman Sachs, Uber, TCS Research cards with skill chips), Growth (score ring, streak calendar, Oracle/Arena tiles), AI Hub ("Ascent AI — Your personal career mentor", Deep Research, 8 tool tiles).

5. **Shortest satisfying video?**
   20 seconds.

6. **Tone?**
   Preset: `polished`. Creative direction: "quiet premium product film for a student career co-pilot — confident, warm, not corporate."

7. **Audio?**
   Minimal polished electronic bed (soft pulse, gentle build), restrained UI-motion SFX on card reveals, one soft logo hit at the outro. Final fade out.

8. **Share caption?**
   "Ascent — the AI career co-pilot built for Indian engineering students. Real internships, a live readiness score, and an AI mentor that knows your goals."

9. **User flow worth showing?**
   entry (Home + readiness score) → key action (Explore real internships; Growth score/streak) → result (AI mentor / Deep Research answers). The app is real and verified on emulator; the flow above is the actual happy path.

## Colors (from core/designsystem theme)

- Background: `#FAFBFE` (light surface, Neutral50-family)
- Ink / primary text: `#0B1220`-family (Neutral950 / Ink)
- Accent: Ascent azure `#1A6BFF`-family (NexusBlue)
- Signature gradient: deep azure → azure → cyan (`NexusBlueDeep → NexusBlue → Cyan400`)
- Secondary accents: AuroraGreen, Amber400 (streak flame)

## Fonts

- Display: **Plus Jakarta Sans** (Google Fonts — same family as the app)
- Body: **Inter** (Google Fonts — same family as the app)

## Storyboard (20.0s total, landscape)

| # | Time | Scene | On-screen text | Motion / transition | SFX |
|---|------|-------|----------------|--------------------|-----|
| 1 | 0.0–3.0 | **Hook.** Black-to-gradient wipe; the Ascent wordmark rises with a thin light sweep. | `Placement season doesn't wait.` → wordmark `Ascent` | Gradient wash L→R; wordmark scale-in with sheen sweep | Soft swell start |
| 2 | 3.0–6.5 | **Reveal — Home.** Phone frame appears; the real Home screen: greeting, Career Readiness hero card on the Ascent gradient, readiness dial ticking up. | Label chip: `Your live Career Readiness score` | Phone slides up + settles; dial ticks; grid texture visible | Tick accents on dial |
| 3 | 6.5–10.5 | **Highlight — Explore.** Phone scrolls the Explore list; internship cards (Goldman Sachs · Engineering Intern, Uber · SWE Intern, TCS Research) snap in. | `Real internships, matched to you` | Cards stagger-in with press-scale feel; skill chips pop | Soft pop per card (3) |
| 4 | 10.5–14.0 | **Highlight — Growth.** Growth screen: score ring fills, 7-day streak dots fill, Oracle/Arena tiles rise. | `Every action moves your score` | Ring draws clockwise; dots fill L→R | Ring draw whoosh |
| 5 | 14.0–17.0 | **Highlight — AI Hub.** AI Hub screen: Ascent AI hero + 8 tool tiles; "Deep Research" tile highlights with a glow. | `An AI mentor that knows your goals` | Tiles cascade; Deep Research tile glows | Single soft hit on glow |
| 6 | 17.0–20.0 | **Outro.** Gradient full-bleed; wordmark + tagline; small "Built for Indian CSE students" line; fade. | `Ascent` / `Engineer Your Future` | Wordmark settles, light sweep, fade to black | Logo hit + fade |

All phone-screen content is real UI from the verified build (screenshots captured from the emulator); no fabricated metrics — the readiness/zero-state values shown are the app's true first-run state. No secrets, no personal data on screen.

## Music cue guidance

Minimal bed, ~95 BPM feel; beats land approximately at 3.0s, 9.0s, 15.0s — align scene 2 settle, card stagger accent, and outro wordmark to those points. Cue metadata optional; story and readability primary.
