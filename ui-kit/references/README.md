# Reference imagery — attribution

12 images downloaded 2026-11-06 for **private moodboard / research use only**. These are the
creators' and platforms' work; they are not assets to ship in the APK or redistribute. The exact
source URL for each file is machine-readable in [`manifest.tsv`](manifest.tsv)
(`filename → site → upstream URL`).

| File | Site | What it shows |
|---|---|---|
| `21st-liquid-metal-hero.jpg` | 21st.dev | Liquid Metal Hero — top saved component |
| `21st-hero-ascii.jpg` | 21st.dev | Hero ASCII One — ASCII/shader trend |
| `21st-hover-preview.jpg` | 21st.dev | Hover Preview — pointer-only interaction |
| `21st-background-paths.jpg` | 21st.dev | Background Paths |
| `aceternity-3d-card.webp` | ui.aceternity.com | 3D Card Effect |
| `aceternity-background-beams.webp` | ui.aceternity.com | Background Beams |
| `aceternity-wobble-card.webp` | ui.aceternity.com | Wobble Card |
| `aceternity-hero-highlight.webp` | ui.aceternity.com | Hero Highlight |
| `aceternity-saas-template.webp` | ui.aceternity.com | Simplistic SaaS template preview |
| `motion-lime-example.webp` | motion.dev | Motion example study (lime) |
| `dribbble-cricket-app.jpg` | dribbble.com | Cricket sports app — match schedule UI (Nixtio) |
| `dribbble-bovine.jpg` | dribbble.com | Bovine — popular shot (George Bokhua) |

## Why there are no Mobbin / Awwwards / Refero / Godly images

- **Mobbin** — screen downloads are account-gated. Its image CDN is routed through a Next.js
  image proxy that returned 400/404 for direct requests. Curated research links are in
  `SOURCES.md` C3 instead.
- **Awwwards** — full-size winner screenshots are served behind the site's own gallery; only
  user avatars were exposed in the fetched markup. Site of the Day (Agrumea Farm, 05 Nov 2026)
  and the winner list are recorded in `SOURCES.md` C2.
- **Refero** — content is gated; captured as a research link (`SOURCES.md` C5).
- **Godly** — the domain redirects to `recent.design`, a feed with no stable component imagery
  (`SOURCES.md` C1).

Nothing was scraped behind a login wall. All 17 sites are accounted for in `SOURCES.md` and in
the interactive board `index.html`.
