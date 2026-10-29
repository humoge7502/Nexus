# Hyperframes Composition Brief: Ascent

## Objective
Create a short launch-style brag video for Ascent, an AI career co-pilot Android app for Indian CSE students.

## Output
- Composition directory: `brag-output/composition/`
- Rendered video: `brag-output/brag.mp4`
- Format: landscape — 1920x1080
- Duration: 20 seconds

## Source Material
- Project root: `Nexus/` (Android/Kotlin/Compose app)
- Primary source: real emulator screenshots of the verified build (captured from AVD NexusApi35)
- Product name: Ascent
- Tagline / strongest claim: "Engineer Your Future — Your AI-powered career co-pilot"; live Career Readiness score
- Key UI or visual moment to recreate: Home hero card on the signature Ascent gradient; Explore internship cards
- Copy that must appear verbatim:
  - "Placement season doesn't wait."
  - "Your live Career Readiness score"
  - "Real internships, matched to you"
  - "Every action moves your score"
  - "An AI mentor that knows your goals"
  - "Ascent" / "Engineer Your Future" / "Built for Indian CSE students."

## Creative Direction
- Tone preset: polished
- Creative direction: quiet premium product film for a student career co-pilot — confident, warm, not corporate
- Interpretation: restrained motion, generous holds, one idea per scene; no hype copy
- Angle: the readiness score is the spine — entry → action → result across four real screens
- Hook: "Placement season doesn't wait." over the Ascent gradient, wordmark rises
- Outro / punchline: Ascent wordmark + "Engineer Your Future"
- Avoid: generic SaaS language, abstract filler, unrelated redesign

## Visual Identity
- Background: #FAFBFE (light) with full-bleed gradient scenes for hook/outro
- Text: #0B1220 (ink)
- Accent: #1A6BFF-family azure; signature gradient deep azure → azure → cyan (approx #16255B → #2B6BFF → #37C6FF)
- Display font: Plus Jakarta Sans (Google Fonts; same family as the app)
- Body font: Inter (Google Fonts; same family as the app)
- Visual references from the project: precision-instrument grid texture, hairline strokes, pill chips, rounded 24-28px cards

## Storyboard (use brag-output/brag-plan.md as the creative contract)
1. Hook — 3.8s — headline + Ascent wordmark on gradient
2. Reveal (Home) — 4.8s — phone with real Home screenshot, label chip "Your live Career Readiness score" (settle on beat 3.82)
3. Explore — 3.9s — phone scrolling real internship cards (beat-locked 8.74; card accents on 8.74/9.29)
4. Growth — 3.3s — score ring + streak (beat-locked 12.55; dots on 13.11/13.64)
5. AI Hub — 1.6s — Ascent AI hero + tools (beat 15.84)
6. Outro — 2.6s — wordmark + tagline (beat-locked 17.47)

## Audio
- Audio role: minimal polished electronic bed + restrained UI accents
- Audio arc: gentle start, quiet build, fade under the outro wordmark
- Music: `happy-beats-business-moves-vol-12-by-ende-dot-app.mp3` (bundled; `polished`/`cinematic` pick)
- Music treatment: volume 0.32; start at 0; no ducking (no voiceover)
- Music cue guidance: bundled preset `C:/Users/Devesh tiwari/.zcode/skills/brag/assets/music/cues/happy-beats-business-moves-vol-12-by-ende-dot-app.music-cues.json` (strongCues 8.74 / 9.29 / 10.93 / 12.55 / 13.11 / 13.64 / 15.84 / 17.47; beats listed in plan)
- Audio-reactive treatment: subtle — let the hero glow/card presence breathe with RMS if the extraction helper is available in this environment; if extraction is unavailable, skip it and note it (do not block render)
- Audio-coupled moments:
  - Scene 2 phone settle — soft drop
  - Scene 3 first card land — soft card-place
  - Outro wordmark — soft logo accent
- SFX selection guidance: polished tone = 2-3 very subtle SFX, nothing aggressive
- SFX analysis guidance: `C:/Users/Devesh tiwari/.zcode/skills/brag/assets/sfx/sfx-analysis.md`
- Exact SFX choice: Hyperframes picks filenames/density/volume after animation exists
- Audio files: copy music + chosen SFX into `brag-output/composition/assets/`

## Hyperframes Instructions
Load hyperframes-core (+ cli guidance already loaded); /brag owns story/copy/tone, Hyperframes owns implementation. Requirements:
- Real UI from the project on screen (screenshots in `assets/ui/` are genuine captures of the verified build)
- All text readable; 20s total; beat locks marked `// beat-locked`, grids marked `// beat-grid`
- Run `npx hyperframes check` semantics: lint → validate → inspect before render
- Local rendering only
