<![CDATA[# NEXUS — Your Career. Engineered. 🚀

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-purple?logo=kotlin" />
  <img src="https://img.shields.io/badge/Compose-BOM_2024.12-blue?logo=jetpackcompose" />
  <img src="https://img.shields.io/badge/Architecture-MVVM-green" />
  <img src="https://img.shields.io/badge/Dagger-Hilt-orange" />
  <img src="https://img.shields.io/badge/API-26%2B-brightgreen" />
</p>

**NEXUS** is a production-grade Android application built for CSE students to manage their entire career journey — discovering internships, tracking certifications, building productivity habits, and showcasing achievements — all in one premium, beautifully designed experience.

---

## ✨ Features

| Feature | Description |
|---|---|
| **Smart Dashboard** | Career readiness score, animated stats, daily opportunities, streak tracking |
| **Internship Discovery** | Browse, search, filter, save, and track applications with status management |
| **Certification Tracker** | Browse certifications by provider, track progress, and share to LinkedIn |
| **Pomodoro Timer** | Focus session timer with animated ring, session tracking, and daily stats |
| **Skill Radar Chart** | Custom Canvas-drawn radar chart showing your skill proficiency |
| **Achievement System** | Gamified milestones with progress bars, unlock animations, and badges |
| **AMOLED Dark Mode** | True-black backgrounds with vibrant accent colors for maximum OLED quality |
| **Glassmorphism** | Frosted glass effects using Haze library for premium depth perception |
| **Animated Backgrounds** | Live mesh gradients and floating particle backgrounds |
| **Onboarding Flow** | 3-page animated onboarding with gradient page indicators |
| **Profile** | Skill radar chart, achievement gallery, career stats |

## 🏗️ Architecture

```
Nexus/
├── app/                    # Application shell, DI, navigation
├── core/
│   ├── designsystem/       # Design tokens, components, effects
│   ├── domain/             # Use cases, domain models
│   ├── data/               # Repositories, data sources
│   ├── network/            # Retrofit + OkHttp API layer
│   ├── database/           # Room database, DAOs, entities
│   ├── datastore/          # DataStore preferences
│   ├── analytics/          # Analytics event tracking
│   ├── notifications/      # FCM notification handling
│   ├── common/             # Shared utilities
│   └── ui/                 # Shared UI utilities
└── feature/
    ├── onboarding/         # Onboarding screens
    ├── home/               # Dashboard
    ├── internships/        # List + detail screens
    ├── certifications/     # Browse + detail screens
    ├── productivity/       # Pomodoro timer, streaks
    ├── profile/            # User profile, skill radar
    ├── achievements/       # Gamification
    └── settings/           # App configuration
```

**Pattern**: MVVM (StateFlow-driven) + Clean Architecture + Modular (MVI migration on roadmap)

## 🛠️ Tech Stack

| Category | Choice |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation (type-safe routes) |
| Architecture | MVVM + Clean Architecture |
| DI | Dagger Hilt 2.52 |
| Async | Coroutines + Flow + StateFlow |
| Local DB | Room 2.6.1 |
| Preferences | DataStore |
| Networking | Retrofit 2.11 + OkHttp 4.12 |
| Image Loading | Coil 2.7.0 |
| Charts | Vico 2.0.0-alpha.28 |
| Animations | Lottie 6.6.0 + Compose Animation |
| Glassmorphism | Haze 1.0.0 |
| Particles | Konfetti 2.0.4 |
| Shimmer | Compose Shimmer 1.3.1 |
| Backend | Firebase (Auth, Firestore, FCM) |

## 🎨 Design System

A custom design system built from scratch:

- **Colors**: Indigo-Violet primary, Cyan secondary, AMOLED dark base (#0A0A0F)
- **Typography**: Plus Jakarta Sans (headings) + Inter (body)
- **Shapes**: Consistent 8/12/16/20/24dp radius system
- **Motion**: Spring physics everywhere (bouncy but responsive)
- **Effects**: Glassmorphism, mesh gradients, particle backgrounds
- **Components**: 15+ production-ready composables (Card, Button, Chip, BottomBar, etc.)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35

### Setup

```bash
git clone https://github.com/yourname/nexus.git
cd nexus
```

1. Open the project in Android Studio
2. Sync Gradle (File → Sync Project with Gradle Files)
3. Place your `google-services.json` in `app/` (for Firebase)
4. Run on emulator or device

### Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## 📸 Screens

| Home Dashboard | Internship List | Profile |
|---|---|---|
| Career gauge, stats, cards | Search, filters, save | Avatar, radar chart, badges |

| Certifications | Pomodoro | Achievements |
|---|---|---|
| Featured carousel, browse | Timer ring, stats | Locked/unlocked badges |

## 📄 License

MIT License — see [LICENSE](LICENSE)

---

Built with ❤️ for CSE students who want to engineer their future.
]]>