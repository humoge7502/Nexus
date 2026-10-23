# Production Readiness Audit System - Core Module

## Overview

The `audit-core` module contains the foundational types, interfaces, and data structures for the NEXUS Production-Readiness Audit System. This module is dependency-free and provides the core abstractions used by all other audit modules.

## Key Components

### Core Data Types

#### AuditConfig
Configuration for the audit system, specifying:
- Project root directory
- Enabled phases (1-8)
- Quality thresholds
- Output formats (Markdown, JSON, HTML)
- Report output directory

#### AuditContext
Shared context passed between phases containing:
- Project structure information
- Source file locations
- Module dependency graph
- Results from previous phases

#### PhaseResult
Base sealed class for phase-specific results:
- Phase number (1-8)
- List of findings
- Metadata specific to the phase

#### Finding
Represents a single audit finding with:
- Severity level (CRITICAL, HIGH, MEDIUM, LOW, INFO)
- Category and title
- Description and recommendation
- Optional code location
- Link to requirement

#### AuditResult
Aggregated results from all phases including:
- Phase results and scores
- Launch readiness score (1-10)
- Startup quality score (1-10)
- Deployment decision (READY, READY_WITH_RISKS, NOT_READY)

### Core Interfaces

#### PhaseAnalyzer
Base interface for all 8 phase analyzers:
- `analyze(context)`: Executes phase analysis
- `calculateScore(result)`: Calculates phase score (1-10)

## Module Structure

```
audit-core/
├── src/
│   ├── main/kotlin/com/nexus/audit/core/
│   │   ├── types/
│   │   │   ├── AuditConfig.kt
│   │   │   ├── AuditContext.kt
│   │   │   ├── AuditResult.kt
│   │   │   ├── PhaseResult.kt
│   │   │   ├── Finding.kt
│   │   │   └── FileSerializer.kt
│   │   └── analyzer/
│   │       └── PhaseAnalyzer.kt
│   └── test/kotlin/com/nexus/audit/core/
│       └── types/
│           ├── AuditConfigTest.kt
│           └── FindingTest.kt
└── build.gradle.kts
```

## Dependencies

- Kotlin stdlib
- kotlinx-coroutines-core
- kotlinx-serialization-json
- JUnit (test only)

## Validates Requirements

- **Requirement 9.1**: Core orchestration and report generation types
- **Requirement 10.1**: Codebase scanning foundation types
- **Requirement 12.1**: Parser and report output data structures
