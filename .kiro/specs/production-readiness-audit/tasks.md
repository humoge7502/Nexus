# Implementation Plan: NEXUS Production-Readiness Audit System

## Overview

This implementation plan creates a comprehensive automated validation framework to assess NEXUS Android application production readiness across 8 critical phases. The system uses a pipeline architecture with modular phase analyzers, codebase scanning utilities (AST parsing, pattern matching), and multi-format report generation (Markdown, JSON, HTML). It will scan the NEXUS codebase, validate production readiness across all criteria, and generate actionable reports with Launch Readiness and Startup Quality scores.

The implementation follows a layered approach: core orchestration → scanning utilities → individual phase analyzers → reporting layer → CLI integration.

## Tasks

- [x] 1. Set up project structure and core type definitions
  - Create module directory structure: `audit-core/`, `audit-scanner/`, `phase-analyzers/`, `audit-rules/`, `audit-reporting/`, `audit-cli/`
  - Define core data types: `AuditConfig`, `AuditContext`, `PhaseResult`, `Finding`, `Severity`, `CodeLocation`
  - Define `PhaseAnalyzer` base interface with `analyze()` and `calculateScore()` methods
  - Define `AuditResult` aggregation types
  - Set up Gradle build configuration for multi-module structure with Kotlin JVM
  - Add core dependencies: Kotlin stdlib, kotlinx-coroutines, kotlinx-serialization
  - _Requirements: 9.1, 10.1, 12.1_

- [ ] 2. Implement core orchestration layer
  - [-] 2.1 Create AuditOrchestrator implementation
    - Implement `runFullAudit(config: AuditConfig)` method
    - Implement `runPhases(phases: List<Int>, config: AuditConfig)` method
    - Implement phase dependency resolution and execution order logic
    - Implement shared context building (`AuditContext` population)
    - Implement result aggregation across all phases
    - Implement `generateDeploymentRecommendation()` method with rules: (1) ANY deployment blocker forces "Not Ready" with no exceptions, (2) Allow "Ready" regardless of Launch_Readiness_Score when no blockers exist, (3) Deployment blockers have absolute veto power
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.8_

  - [ ]* 2.2 Write unit tests for AuditOrchestrator
    - Test single phase execution
    - Test multi-phase execution with context sharing
    - Test error handling when phases fail
    - Test deployment recommendation logic: verify ANY blocker forces "Not Ready", verify "Ready" allowed without blockers regardless of score, verify no override mechanisms exist
    - _Requirements: 11.1-11.8_

- [ ] 3. Implement codebase scanning utilities
  - [ ] 3.1 Create FileSystemScanner implementation
    - Implement `scanKotlinFiles()` to recursively find all .kt files
    - Implement `scanBuildFiles()` to find build.gradle.kts files
    - Implement `scanResources()` to find resource directories and files
    - Implement `buildModuleGraph()` to parse module structure and dependencies
    - _Requirements: 10.1, 10.2_

  - [~] 3.2 Create KotlinASTParser implementation
    - Integrate kotlin-compiler-embeddable or kotlinx.ast library
    - Implement `parse(file: File)` to create AST from Kotlin source
    - Implement `findClasses()` to extract class declarations
    - Implement `findFunctions()` to extract function declarations
    - Implement `findImports()` to extract import statements
    - Implement `findAnnotationUsages()` to find annotated elements
    - Add error handling for malformed Kotlin files
    - _Requirements: 10.2, 10.3_

  - [~] 3.3 Create PatternMatcher implementation
    - Implement `detectMockPatterns()` to identify hardcoded lists, fake data classes, placeholder comments
    - Implement `detectHardcodedSecrets()` to scan for API keys, tokens, passwords
    - Implement `detectArchitectureViolations()` to validate MVVM/MVI patterns
    - Implement `detectMemoryLeakPatterns()` to find leaked contexts, uncleared listeners
    - Use regex patterns for TODO/FIXME/MOCK/PLACEHOLDER comments
    - _Requirements: 10.2, 10.3, 10.5, 10.6_

  - [ ]* 3.4 Write unit tests for scanning utilities
    - Test FileSystemScanner on sample project structure
    - Test KotlinASTParser on various Kotlin syntax patterns
    - Test PatternMatcher detection accuracy with positive and negative examples
    - _Requirements: 10.1-10.10_

- [~] 4. Checkpoint - Verify core infrastructure
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement Phase 1: Real Data Validator
  - [~] 5.1 Create RealDataValidator analyzer
    - Implement `analyze(context: AuditContext)` method
    - Implement `identifyFeatures()` to categorize files into features
    - Implement `validateDataSource()` to check for real API calls vs mocks
    - Implement `detectMockImplementations()` using PatternMatcher
    - Implement `validateEdgeCases()` to check empty/error handling
    - Implement `calculateRealDataCoverage()` percentage calculation
    - Generate `Phase1Result` with feature-by-feature validations
    - Categorize features as PRODUCTION_READY, PARTIALLY_MOCKED, or FULLY_MOCKED
    - _Requirements: 1.1, 1.2, 1.3, 1.7, 1.8, 1.9, 1.10_

  - [~] 5.2 Implement score calculation for Phase 1
    - Calculate score based on real data coverage percentage
    - Penalize score for fully-mocked features
    - Generate findings for each mock pattern detected
    - _Requirements: 1.7, 1.10, 9.14_

  - [ ]* 5.3 Write unit tests for RealDataValidator
    - Test feature identification logic
    - Test mock pattern detection accuracy
    - Test scoring logic with various coverage percentages
    - _Requirements: 1.1-1.10_

- [ ] 6. Implement Phase 2: API Integration Auditor
  - [~] 6.1 Create APIIntegrationAuditor analyzer
    - Implement `scanForIntegrationPoints()` to find Retrofit interfaces, Firebase, Supabase, GraphQL
    - Implement `validateEndpoint()` to check authentication, error handling, retries
    - Implement `validateAuthImplementations()` for OAuth flows
    - Implement `validateErrorHandling()` to verify 4xx/5xx/timeout handling
    - Implement `validateRetryStrategies()` to check exponential backoff
    - Implement `validateCaching()` to assess caching strategies
    - Implement `calculateAPIHealthScore()` based on integration reliability
    - Generate `Phase2Result` with endpoint validation details
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.10, 2.11, 2.12_

  - [~] 6.2 Implement validation for background services
    - Validate background worker scheduling and cancellation
    - Validate push notification configuration
    - Validate database transaction handling
    - _Requirements: 2.5, 2.8, 2.9_

  - [ ]* 6.3 Write unit tests for APIIntegrationAuditor
    - Test integration point detection
    - Test endpoint validation logic
    - Test API health score calculation
    - _Requirements: 2.1-2.12_

- [ ] 7. Implement Phase 3: Architecture Analyzer
  - [~] 7.1 Create ArchitectureAnalyzer analyzer
    - Implement `detectArchitecturePattern()` to identify MVVM/MVI/Clean Architecture
    - Implement `validatePatternConsistency()` across modules
    - Implement `analyzeViewModels()` to check lifecycle handling and state management
    - Implement `validateDIConfiguration()` to verify Hilt scope usage
    - Implement `analyzeComposeState()` to detect unnecessary recomposition
    - Implement `analyzeNavigation()` to validate NavController and deep links
    - Implement `detectMemoryLeaks()` using PatternMatcher
    - Implement `analyzeCoroutineUsage()` to verify dispatcher selection and structured concurrency
    - Implement `analyzeLifecycleHandling()` to check resource cleanup
    - Implement `analyzeTestCoverage()` by scanning test directories
    - Generate `Phase3Result` with architecture quality assessment
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.14, 3.15_

  - [~] 7.2 Implement dependency and build analysis
    - Analyze dependency versions for outdated libraries
    - Check Gradle build optimization settings
    - Validate build variant configurations
    - _Requirements: 3.11, 3.12, 3.13_

  - [ ]* 7.3 Write unit tests for ArchitectureAnalyzer
    - Test architecture pattern detection
    - Test ViewModel analysis logic
    - Test memory leak pattern detection
    - _Requirements: 3.1-3.15_

- [ ] 8. Implement Phase 4: UI/UX Validator
  - [~] 8.1 Create UIUXValidator analyzer
    - Implement navigation flow analysis to detect dead ends
    - Implement responsive layout validation across device sizes
    - Implement accessibility compliance checking (content descriptions, touch targets, contrast)
    - Implement loading state verification
    - Implement empty state validation
    - Implement error message validation
    - Implement animation performance checking
    - Implement dark mode validation
    - Implement form validation feedback checking
    - Implement design system consistency validation
    - Generate `Phase4Result` with UX readiness findings
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.11, 4.12, 4.13_

  - [ ]* 8.2 Write unit tests for UIUXValidator
    - Test navigation analysis logic
    - Test accessibility compliance checking
    - Test design system consistency validation
    - _Requirements: 4.1-4.13_

- [ ] 9. Implement Phase 5: Performance Profiler
  - [~] 9.1 Create PerformanceProfiler analyzer (static analysis portion)
    - Implement `identifyBottlenecks()` through static code analysis
    - Detect main thread blocking patterns
    - Detect excessive recomposition patterns
    - Detect inefficient database query patterns
    - Detect large image loading without optimization
    - Detect unoptimized algorithms (nested loops, inefficient collections)
    - Generate `Phase5Result` with bottleneck analysis
    - Note: Runtime profiling (cold startup, memory, frame rates) requires instrumentation
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.6, 5.7, 5.12_

  - [~] 9.2 Add integration hooks for runtime metrics
    - Define interfaces for collecting runtime metrics from external profilers
    - Add support for parsing Android Studio profiler output
    - Add support for parsing instrumented test results
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 5.10, 5.11_

  - [ ]* 9.3 Write unit tests for PerformanceProfiler
    - Test bottleneck detection logic
    - Test runtime metric integration interfaces
    - _Requirements: 5.1-5.12_

- [~] 10. Checkpoint - Verify phase analyzers 1-5
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Implement Phase 6: Security Auditor
  - [~] 11.1 Create SecurityAuditor analyzer
    - Implement `validateHTTPSEnforcement()` by checking network config and API base URLs
    - Implement `validateCredentialStorage()` to verify Keystore/EncryptedSharedPreferences usage
    - Implement `validateAuthFlows()` for OAuth implementations
    - Implement `validateInputSanitization()` to detect injection vulnerabilities
    - Implement `validateEncryption()` for sensitive data
    - Implement `validatePermissions()` to check runtime permission handling
    - Implement `validatePrivacyCompliance()` for GDPR/CCPA requirements
    - Implement `validateRateLimiting()` for abuse prevention
    - Implement `validateContentModeration()` for user-generated content
    - Implement `scanForHardcodedSecrets()` using PatternMatcher
    - Implement `validateLogging()` to ensure no sensitive data in logs
    - Implement `validateObfuscation()` to check ProGuard/R8 configuration
    - Implement `identifyVulnerabilities()` to aggregate security issues
    - Generate `Phase6Result` with security vulnerability report
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10, 6.11, 6.12, 6.13_

  - [ ]* 11.2 Write unit tests for SecurityAuditor
    - Test hardcoded secret detection
    - Test HTTPS enforcement validation
    - Test credential storage validation
    - _Requirements: 6.1-6.13_

- [ ] 12. Implement Phase 7: Deployment Checker
  - [~] 12.1 Create DeploymentChecker analyzer
    - Implement `validateCICDSetup()` by checking for GitHub Actions/GitLab CI config files
    - Implement `validateReleaseBuild()` to verify ProGuard/R8 enablement
    - Implement `validateEnvironments()` to check dev/staging/prod configuration separation
    - Implement `validateCrashlytics()` to verify Firebase Crashlytics integration
    - Implement `validateMonitoring()` to check Firebase Analytics and logging
    - Implement `validateAnalytics()` to verify event tracking implementation
    - Implement `validateFeatureFlags()` to check remote config or feature flag system
    - Implement `validatePlayStoreRequirements()` for target API, permissions, content policy
    - Implement `validateVersioning()` to check semantic versioning adherence
    - Implement `validateRollback()` to assess rollback mechanisms
    - Implement `validateSigning()` to verify release signing configuration
    - Implement `validateAppSize()` to check APK/AAB optimization
    - Implement `identifyDeploymentBlockers()` to generate blocker checklist
    - Generate `Phase7Result` with deployment readiness assessment
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 7.12, 7.13_

  - [ ]* 12.2 Write unit tests for DeploymentChecker
    - Test CI/CD validation logic
    - Test Play Store compliance checking
    - Test deployment blocker identification
    - _Requirements: 7.1-7.13_

- [ ] 13. Implement Phase 8: Startup Quality Evaluator
  - [~] 13.1 Create StartupQualityEvaluator analyzer
    - Implement `evaluateReliability()` using crash analytics data
    - Implement `evaluatePremiumFeel()` by assessing animation quality and design polish
    - Implement `evaluateTrustworthiness()` by reviewing security and privacy practices
    - Implement `compareArchitecture()` against industry best practices
    - Implement `evaluateAIFeatures()` to assess AI sophistication
    - Implement `evaluateScalability()` by analyzing database and caching design
    - Implement `evaluateUserReadiness()` by reviewing onboarding and feature discoverability
    - Implement `compareToBenchmarks()` for qualitative comparison against YC startups
    - Implement `identifyWorldClassGaps()` to list improvement areas
    - Implement `generateRecommendations()` for reaching startup-grade quality
    - Generate `Phase8Result` with startup quality evaluation
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9, 8.10_

  - [ ]* 13.2 Write unit tests for StartupQualityEvaluator
    - Test scoring logic for each quality dimension
    - Test benchmark comparison logic
    - Test recommendation generation
    - _Requirements: 8.1-8.10_

- [ ] 14. Implement score calculation system
  - [~] 14.1 Create ScoreCalculator implementation
    - Implement `calculateLaunchReadinessScore()` aggregating scores from Phases 1-7
    - Implement `calculateStartupQualityScore()` using Phase 8 results
    - Implement weighting logic for different phases (security/reliability higher weight)
    - Implement penalty logic for critical issues
    - Ensure scores are on 1-10 scale with clear thresholds
    - Ensure scoring logic produces scores even for severely flawed projects (minimum scores allowed)
    - _Requirements: 9.14, 9.15, 11.4_

  - [ ]* 14.2 Write property test for score calculation
    - **Property 1: Score monotonicity**
    - **Validates: Requirements 9.14, 9.15**
    - Verify that improving any individual phase score never decreases overall score
    - Test: For any audit result, if we increase any phase score, overall score should not decrease

  - [ ]* 14.3 Write unit tests for ScoreCalculator
    - Test score aggregation logic
    - Test weighting and penalty calculations
    - Test threshold-based recommendations
    - _Requirements: 9.14, 9.15_

- [~] 15. Checkpoint - Verify all phase analyzers complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 16. Implement report generation layer
  - [~] 16.1 Create ReportGenerator implementation
    - Implement `generateReport()` to orchestrate multi-format report generation
    - Implement `generateExecutiveSummary()` for stakeholder view
    - Aggregate findings across all phases
    - Calculate overall statistics (total issues by severity, coverage percentages)
    - Implement deployment decision logic: (1) ANY deployment blocker forces "Not Ready" regardless of scores, (2) Allow "Ready" when no blockers exist regardless of Launch_Readiness_Score, (3) No exceptions or override mechanisms for blockers
    - Ensure report generation succeeds even for severely flawed projects with minimum quality scores
    - _Requirements: 9.1, 9.14, 9.15, 11.1, 11.2, 11.3, 11.4, 12.10_

  - [~] 16.2 Create MarkdownFormatter implementation
    - Implement `format(result: AuditResult)` for Markdown output
    - Generate executive summary section
    - Generate phase-by-phase findings sections
    - Generate deployment blocker checklist
    - Generate high-risk systems analysis
    - Generate recommendations and priorities
    - Include score visualizations using ASCII charts
    - Ensure formatting succeeds even for minimum scores and numerous critical findings
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9, 9.10, 9.11, 9.12, 9.13, 12.2, 12.6_

  - [~] 16.3 Create JSONFormatter implementation
    - Implement `format(result: AuditResult)` for JSON output using kotlinx-serialization
    - Ensure structured data suitable for programmatic analysis
    - Include all findings, scores, and metadata
    - _Requirements: 12.3, 12.5_

  - [~] 16.4 Create HTMLFormatter implementation
    - Implement `format(result: AuditResult)` for HTML output
    - Implement `generateCharts()` using Chart.js or similar library
    - Create radar chart for phase scores
    - Create pie chart for issue severity distribution
    - Create bar chart for feature-level data coverage
    - Include CSS styling for professional presentation
    - _Requirements: 12.4, 12.5_

  - [ ]* 16.5 Write property test for report formatting round-trip
    - **Property 2: JSON round-trip consistency**
    - **Validates: Requirements 12.7**
    - Verify that parsing JSON report then formatting produces equivalent structure
    - Test: For any valid AuditResult, serialize to JSON, deserialize, serialize again should produce identical JSON

  - [ ]* 16.6 Write unit tests for report formatters
    - Test Markdown output structure and content
    - Test JSON serialization correctness
    - Test HTML generation with charts
    - Test executive summary generation
    - Test report generation succeeds with minimum quality scores
    - Test report generation succeeds with numerous critical findings
    - _Requirements: 9.1-9.15, 12.1-12.10_

- [ ] 17. Implement filtering and customization
  - [~] 17.1 Add report filtering capabilities
    - Implement severity-based filtering (critical, high, medium, low)
    - Implement phase-based filtering (Phase 1-8)
    - Implement feature-based filtering (by module or feature name)
    - Update all formatters to support filtered views
    - _Requirements: 12.8, 12.9_

  - [ ]* 17.2 Write unit tests for filtering
    - Test severity filtering correctness
    - Test phase filtering correctness
    - Test combined filters
    - _Requirements: 12.8, 12.9_

- [ ] 18. Implement CLI interface
  - [~] 18.1 Create CLI command using Clikt library
    - Implement `AuditCommand` with command-line option parsing
    - Add `--project-root` option to specify NEXUS project directory
    - Add `--phases` option to select specific phases (default: all)
    - Add `--output-format` option (markdown, json, html, all)
    - Add `--output-dir` option to specify report output directory
    - Add `--threshold` options for score thresholds
    - Add `--filter-severity` option for severity filtering
    - Add `--verbose` flag for detailed logging
    - _Requirements: 9.1, 12.8, 12.9_

  - [~] 18.2 Implement CLI execution flow
    - Parse command-line arguments
    - Build AuditConfig from CLI options
    - Initialize AuditOrchestrator
    - Execute audit with progress reporting
    - Generate reports in requested formats
    - Print executive summary to console
    - Return appropriate exit codes (0 for ready, 1 for not ready)
    - _Requirements: 11.1, 11.2, 11.3_

  - [~] 18.3 Add progress reporting and logging
    - Implement progress indicators for long-running operations
    - Add structured logging for debugging (using kotlin-logging)
    - Log phase execution start/end with timing
    - Log file scanning progress
    - Log error details for troubleshooting
    - _Requirements: 9.1_

  - [ ]* 18.4 Write integration tests for CLI
    - Test CLI argument parsing
    - Test end-to-end execution on sample project
    - Test error handling for invalid inputs
    - _Requirements: 9.1, 12.8, 12.9_

- [ ] 19. Implement Gradle plugin (optional integration)
  - [~] 19.1 Create Gradle plugin structure
    - Create `audit-gradle-plugin` module
    - Define `AuditTask` as a Gradle task
    - Map Gradle extension properties to AuditConfig
    - Integrate with existing Gradle build lifecycle
    - _Requirements: 7.1_

  - [~] 19.2 Implement Gradle task execution
    - Execute audit system from Gradle context
    - Access project properties and build configuration
    - Generate reports in build/audit directory
    - Fail build if critical issues found (configurable)
    - _Requirements: 7.1, 11.2_

  - [ ]* 19.3 Write tests for Gradle plugin
    - Test plugin application
    - Test task configuration and execution
    - Test integration with Gradle build lifecycle
    - _Requirements: 7.1_

- [~] 20. Checkpoint - Verify complete system integration
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 21. Create comprehensive integration tests
  - [ ]* 21.1 Write integration test for full audit on sample Android project
    - Create minimal Android project with known patterns (mocks, architecture, security issues)
    - Run full audit and verify all phases execute
    - Verify expected findings are detected
    - Verify scores are calculated correctly
    - Verify reports are generated in all formats
    - _Requirements: 1.1-10.10_

  - [ ]* 21.2 Write integration test for NEXUS project audit
    - Run audit on actual NEXUS codebase
    - Verify audit completes without errors
    - Verify all features are analyzed
    - Verify report generation succeeds
    - _Requirements: 1.1-10.10_

- [ ] 22. Implement error handling and resilience
  - [~] 22.1 Add comprehensive error handling
    - Wrap phase analyzers with error boundaries
    - Implement graceful degradation when phase fails
    - Include error details in audit report
    - Continue execution when non-critical phases fail
    - Log errors for debugging
    - Ensure report generation proceeds even when audit results indicate severe quality issues
    - _Requirements: 9.1_

  - [~] 22.2 Add input validation
    - Validate project root directory exists
    - Validate project is an Android/Kotlin project
    - Validate output directory is writable
    - Provide helpful error messages for validation failures
    - _Requirements: 9.1_

  - [ ]* 22.3 Write tests for error handling
    - Test behavior on malformed Kotlin files
    - Test behavior on missing directories
    - Test behavior on invalid configurations
    - Test partial audit execution when phases fail
    - _Requirements: 9.1_

- [ ] 23. Add caching and performance optimizations
  - [~] 23.1 Implement AST caching
    - Cache parsed AST for each Kotlin file
    - Implement cache invalidation based on file modification time
    - Store cache in-memory for single audit execution
    - _Requirements: 10.1_

  - [~] 23.2 Implement parallel processing
    - Execute independent phases in parallel (Phases 1-2, 6-8 can run concurrently)
    - Process files in batches using coroutines
    - Implement worker pool for file analysis
    - _Requirements: 10.1_

  - [ ]* 23.3 Write performance tests
    - Test audit completes in under 5 minutes for large project (1000+ files)
    - Test memory usage remains reasonable during execution
    - _Requirements: 10.1_

- [ ] 24. Create documentation and examples
  - [~] 24.1 Write README with usage instructions
    - Document installation steps
    - Provide CLI usage examples
    - Provide Gradle plugin usage examples
    - Document configuration options
    - Include sample report screenshots
    - _Requirements: 9.1_

  - [~] 24.2 Write developer documentation
    - Document architecture and design decisions
    - Document how to add custom audit rules
    - Document how to extend with new phases
    - Document report format specifications
    - _Requirements: 9.1_

  - [~] 24.3 Create sample audit reports
    - Generate sample Markdown report
    - Generate sample JSON report
    - Generate sample HTML report
    - Include in documentation as examples
    - _Requirements: 9.1, 12.2, 12.3, 12.4_

- [ ] 25. Final integration and validation
  - [~] 25.1 Run full audit on NEXUS project
    - Execute complete audit with all 8 phases
    - Generate reports in all formats
    - Review findings for accuracy
    - Validate scores reflect actual production readiness
    - _Requirements: 1.1-11.8_

  - [~] 25.2 Create release artifacts
    - Build CLI executable JAR with all dependencies
    - Publish Gradle plugin to Maven Local or repository
    - Package documentation and examples
    - Create release notes
    - _Requirements: 9.1_

  - [~] 25.3 Final checkpoint and handoff
    - Ensure all tests pass
    - Verify documentation is complete
    - Confirm NEXUS team can run audit independently
    - Provide recommendations based on initial audit results
    - _Requirements: 9.1, 11.5, 11.6, 11.7_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate critical correctness properties (score monotonicity, round-trip consistency)
- Unit tests validate specific behaviors and edge cases
- The system is designed for extensibility: new phases, custom rules, and custom report templates can be added
- Performance optimizations (caching, parallelization) are implemented to handle large codebases efficiently
- The audit can be run as CLI tool or integrated into CI/CD via Gradle plugin
- Phase analyzers are independent and can be developed/tested in isolation
- All formatters share the same data model ensuring consistency across output formats

**CRITICAL IMPLEMENTATION RULES:**

1. **Report Generation Resilience**: Report generation MUST proceed even when the project has fundamental quality issues resulting in minimum quality scores. The purpose of auditing is to document problems - poorly-designed projects need comprehensive issue documentation MORE than well-functioning ones. Reports should never fail due to low scores or numerous critical findings.

2. **Score Independence for Ready Decisions**: When all audit phases complete without deployment blockers, the system MAY recommend "Ready" regardless of Launch_Readiness_Score value. Scores provide guidance, but blockers are the absolute decision criterion. Low scores are a factor but not an absolute blocker.

3. **Deployment Blocker Absolute Veto**: ANY deployment blocker forces a "Not Ready" recommendation with no exceptions or override mechanisms. Blockers represent fundamental issues that must be resolved before production deployment. There are no special handling provisions for urgent hotfixes or critical security patches - blockers have absolute veto power.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1", "3.1"] },
    { "id": 2, "tasks": ["2.2", "3.2", "3.3"] },
    { "id": 3, "tasks": ["3.4"] },
    { "id": 4, "tasks": ["5.1", "6.1"] },
    { "id": 5, "tasks": ["5.2", "5.3", "6.2"] },
    { "id": 6, "tasks": ["6.3", "7.1"] },
    { "id": 7, "tasks": ["7.2", "7.3", "8.1"] },
    { "id": 8, "tasks": ["8.2", "9.1"] },
    { "id": 9, "tasks": ["9.2", "9.3"] },
    { "id": 10, "tasks": ["11.1"] },
    { "id": 11, "tasks": ["11.2", "12.1"] },
    { "id": 12, "tasks": ["12.2", "13.1"] },
    { "id": 13, "tasks": ["13.2", "14.1"] },
    { "id": 14, "tasks": ["14.2", "14.3"] },
    { "id": 15, "tasks": ["16.1"] },
    { "id": 16, "tasks": ["16.2", "16.3", "16.4"] },
    { "id": 17, "tasks": ["16.5", "16.6", "17.1"] },
    { "id": 18, "tasks": ["17.2", "18.1"] },
    { "id": 19, "tasks": ["18.2"] },
    { "id": 20, "tasks": ["18.3"] },
    { "id": 21, "tasks": ["18.4", "19.1"] },
    { "id": 22, "tasks": ["19.2"] },
    { "id": 23, "tasks": ["19.3", "21.1", "21.2"] },
    { "id": 24, "tasks": ["22.1", "22.2"] },
    { "id": 25, "tasks": ["22.3", "23.1", "23.2"] },
    { "id": 26, "tasks": ["23.3", "24.1", "24.2", "24.3"] },
    { "id": 27, "tasks": ["25.1"] },
    { "id": 28, "tasks": ["25.2", "25.3"] }
  ]
}
```
