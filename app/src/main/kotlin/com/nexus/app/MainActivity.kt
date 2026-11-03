package com.nexus.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nexus.achievements.AchievementsScreen
import com.nexus.achievements.LeaderboardScreen
import com.nexus.ai.AIChatScreen
import com.nexus.ai.AIHubScreen
import com.nexus.ai.AIResumeAnalyzerScreen
import com.nexus.ai.CareerInsightsScreen
import com.nexus.ai.InterviewCoachScreen
import com.nexus.ai.LinkedInPostScreen
import com.nexus.ai.ResearchScreen
import com.nexus.ai.RoadmapGeneratorScreen
import com.nexus.ai.SkillGapScreen
import com.nexus.app.tour.GuidedTour
import com.nexus.app.tour.TourViewModel
import com.nexus.applications.ApplicationsScreen
import com.nexus.auth.AuthScreen
import com.nexus.auth.AuthViewModel
import com.nexus.careermirror.CareerMirrorScreen
import com.nexus.certifications.CertificationBrowserScreen
import com.nexus.certifications.CertificationDetailScreen
import com.nexus.cohortintelligence.CohortIntelligenceScreen
import com.nexus.common.ApiResult
import com.nexus.community.CommunityScreen
import com.nexus.designsystem.component.NexusBottomBar
import com.nexus.designsystem.component.NexusCommand
import com.nexus.designsystem.component.NexusCommandPalette
import com.nexus.designsystem.component.NexusNavigationLayout
import com.nexus.designsystem.component.NexusRailBar
import com.nexus.designsystem.component.NexusSideBar
import com.nexus.designsystem.component.nexusNavigationLayoutFor
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.designsystem.theme.NexusTransitions
import com.nexus.explore.ExploreScreen
import com.nexus.explore.HackathonDetailScreen
import com.nexus.goals.GoalsScreen
import com.nexus.growth.DSATrackerScreen
import com.nexus.growth.GrowthScreen
import com.nexus.growth.NexusArenaScreen
import com.nexus.growth.NexusOracleScreen
import com.nexus.home.HomeScreen
import com.nexus.interviewlog.InterviewLogScreen
import com.nexus.internships.InternshipDetailScreen
import com.nexus.internships.InternshipListScreen
import com.nexus.notifications.NotificationsScreen
import com.nexus.onboarding.OnboardingScreen
import com.nexus.onboarding.OnboardingViewModel
import com.nexus.placementforecast.PlacementForecastScreen
import com.nexus.portfolio.PortfolioScreen
import com.nexus.productivity.ProductivityScreen
import com.nexus.profile.ProfileScreen
import com.nexus.search.SearchScreen
import com.nexus.settings.SettingsScreen
import com.nexus.settings.SettingsViewModel
import com.nexus.tribe.TribeScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingDestination by mutableStateOf<NexusDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDestination = NexusDeepLinks.resolve(intent)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val themeMode = settings.themeMode.uppercase()
            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK", "AMOLED" -> true
                else -> systemDark
            }
            NexusTheme(
                darkTheme = darkTheme,
                dynamicColor = false,
                amoled = settings.amoledEnabled && darkTheme,
                accent = settings.accentColor,
            ) {
                NexusApp(
                    activity = this@MainActivity,
                    pendingDestination = pendingDestination,
                    onDestinationConsumed = { pendingDestination = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDestination = NexusDeepLinks.resolve(intent)
    }
}

@Composable
fun NexusApp(
    activity: MainActivity? = null,
    pendingDestination: NexusDestination? = null,
    onDestinationConsumed: () -> Unit = {},
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val onboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    var currentStep by rememberSaveable { mutableStateOf("loading") }
    val scope = rememberCoroutineScope()

    // Result is handled in the launcher callback — the sign-in activity's
    // result can never be missed (the old static-variable + immediate
    // coroutine read raced the activity result and always saw null).
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            val success = authViewModel.handleGoogleSignInResult(result.data)
            if (success) currentStep = "onboarding"
        }
    }

    LaunchedEffect(authState, onboardingCompleted) {
        val onboarded = onboardingCompleted
        if (authState is ApiResult.Loading || onboarded == null) {
            currentStep = "loading"
            return@LaunchedEffect
        }
        currentStep = when {
            onboarded -> "main"
            (authState as? ApiResult.Success)?.data == true -> "onboarding"
            else -> "auth"
        }
    }

    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Denial is fine: channels simply stay silent. */ }
    LaunchedEffect(currentStep) {
        val host = activity ?: return@LaunchedEffect
        if (currentStep != "main" || permissionRequested) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            host.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequested = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val bootReducedMotion = LocalReducedMotion.current
    AnimatedContent(
        targetState = currentStep,
        // The boot flow only ever moves forward — auth, then onboarding, then the
        // app — so it takes the forward pair rather than inventing its own timing.
        // Reduced motion swaps instantly: the step change itself is the signal.
        transitionSpec = {
            if (bootReducedMotion) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                NexusTransitions.forwardEnter() togetherWith NexusTransitions.forwardExit()
            }
        },
        label = "bootFlow",
    ) { step ->
        when (step) {
            "loading" -> { /* splash screen visible */ }
            // Auth and onboarding draw their own decorative layers but no
            // surface, so without this they render on the splash window's
            // black background and light-theme text turns invisible.
            "auth" -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                AuthScreen(
                    onGoogleSignIn = {
                        val act = activity ?: return@AuthScreen
                        authViewModel.initGoogleSignIn(act)
                        authViewModel.getGoogleSignInIntent()?.let { intent ->
                            googleSignInLauncher.launch(intent)
                        }
                    },
                    onGitHubSignIn = { currentStep = "onboarding" },
                    onSkip = { currentStep = "onboarding" }
                )
            }
            "onboarding" -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onComplete = { currentStep = "main" },
                )
            }
            "main" -> NexusMainScaffold(
                pendingDestination = pendingDestination,
                onDestinationConsumed = onDestinationConsumed,
            )
        }
    }
}

@Composable
fun NexusMainScaffold(
    pendingDestination: NexusDestination? = null,
    onDestinationConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val tourViewModel: TourViewModel = hiltViewModel()
    val tourVisible by tourViewModel.tourVisible.collectAsStateWithLifecycle(initialValue = false)

    // Measured by the bar itself. The tour points at a station it can see, never
    // at a coordinate it recomputes from padding it would then have to duplicate.
    var stationsBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(pendingDestination) {
        val destination = pendingDestination ?: return@LaunchedEffect
        navController.navigate(destination) { launchSingleTop = true }
        onDestinationConsumed()
    }

    // Compare destinations by route class, never by qualifiedName: R8 renames
    // classes in release builds, so string-based matching silently hides the bar.
    val topLevelDestinations = listOf(
        NexusDestination.Home,
        NexusDestination.Explore,
        NexusDestination.AIHub,
        NexusDestination.Growth,
        NexusDestination.Profile,
    )
    val topLevelRoutes = listOf(
        NexusDestination.Home::class,
        NexusDestination.Explore::class,
        NexusDestination.AIHub::class,
        NexusDestination.Growth::class,
        NexusDestination.Profile::class,
    )
    val selectedTab = topLevelRoutes.indexOfFirst { route ->
        backStackEntry?.destination?.hasRoute(route) == true
    }

    // Compact keeps the thumb-reachable bar. A wide viewport gets a rail, then a
    // permanent drawer — chosen from the width itself rather than a device label,
    // so an unfolding foldable changes chrome without the app being told it moved
    // to another class of hardware.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layout = nexusNavigationLayoutFor(maxWidth)
        val chromeVisible = selectedTab >= 0
        val sideChrome = chromeVisible && layout != NexusNavigationLayout.BottomBars
        val onNavigateTab: (Int) -> Unit = { index ->
            navigateToTab(navController, topLevelDestinations, index)
        }
        // The command palette: one query to anywhere. State lives at the shell
        // level so any screen's trigger opens the same overlay.
        var paletteOpen by rememberSaveable { mutableStateOf(false) }
        var paletteQuery by rememberSaveable { mutableStateOf("") }

        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (chromeVisible && layout == NexusNavigationLayout.BottomBars) {
                    NexusBottomBar(
                        selectedIndex = selectedTab,
                        onNavigate = onNavigateTab,
                        onStationsBoundsChanged = { bounds -> stationsBounds = bounds },
                    )
                }
            },
            // Side chrome takes its own window insets; padding the content with
            // system bars as well would drop a screen a status bar below a drawer
            // that already starts beneath one.
            contentWindowInsets = if (sideChrome) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
        ) { paddingValues ->
            Row(modifier = Modifier.fillMaxSize()) {
                if (sideChrome) {
                    if (layout == NexusNavigationLayout.Rail) {
                        NexusRailBar(selectedIndex = selectedTab, onNavigate = onNavigateTab)
                    } else {
                        NexusSideBar(selectedIndex = selectedTab, onNavigate = onNavigateTab)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .then(
                            if (sideChrome) {
                                Modifier.statusBarsPadding().navigationBarsPadding()
                            } else {
                                Modifier
                            },
                        )
                        .padding(paddingValues),
                ) {
                    // Tab switches are peer-to-peer and fade-lift — sliding a peer
                    // sideways would claim an order the navigation does not have.
                    // Only opening or leaving a detail slides. Reduced motion
                    // collapses every page change to an instant swap.
                    val reducedMotion = LocalReducedMotion.current
                    val isTopLevel = { entry: NavBackStackEntry ->
                        topLevelRoutes.any { entry.destination.hasRoute(it) }
                    }
                    NavHost(
                        navController = navController,
                        startDestination = NexusDestination.Home,
                        enterTransition = navEnter(reducedMotion, isTopLevel),
                        exitTransition = navExit(reducedMotion, isTopLevel),
                        popEnterTransition = navEnter(reducedMotion, isTopLevel, backward = true),
                        popExitTransition = navExit(reducedMotion, isTopLevel, backward = true),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                // HOME
                composable<NexusDestination.Home> {
                    HomeScreen(
                        onInternshipClick = { id -> navController.navigate(NexusDestination.InternshipDetail(id)) },
                        onCertificationClick = { id -> navController.navigate(NexusDestination.CertificationDetail(id)) },
                        onViewAllInternships = { navController.navigate(NexusDestination.InternshipList) },
                        onNavigateToProductivity = { navController.navigate(NexusDestination.Productivity) },
                        onNavigateToProfile = { navController.navigate(NexusDestination.Profile) },
                        onNavigateToNotifications = { navController.navigate(NexusDestination.Notifications) },
                        onOpenCommandPalette = { paletteOpen = true },
                    )
                }

                // EXPLORE
                composable<NexusDestination.Explore> {
                    ExploreScreen(
                        onInternshipClick = { id -> navController.navigate(NexusDestination.InternshipDetail(id)) },
                        onHackathonClick = { id -> navController.navigate(NexusDestination.HackathonDetail(id)) },
                        onCertificationClick = { id -> navController.navigate(NexusDestination.CertificationDetail(id)) },
                        showBackArrow = false,
                    )
                }
                composable<NexusDestination.HackathonDetail> { entry ->
                    val dest = entry.toRoute<NexusDestination.HackathonDetail>()
                    HackathonDetailScreen(hackathonId = dest.hackathonId, onBack = { navController.popBackStack() })
                }

                // AI HUB
                composable<NexusDestination.AIHub> {
                    AIHubScreen(
                        onToolRoute = { route ->
                            navController.navigate(
                                when (route) {
                                    "chat" -> NexusDestination.AIChat
                                    "resume" -> NexusDestination.AIResumeAnalyzer
                                    "research" -> NexusDestination.Research
                                    "roadmap" -> NexusDestination.Roadmap
                                    "skillgap" -> NexusDestination.SkillGap
                                    "linkedin" -> NexusDestination.LinkedInPost
                                    "interview" -> NexusDestination.InterviewCoach
                                    else -> NexusDestination.CareerInsights
                                },
                            )
                        },
                        showBackArrow = false,
                    )
                }
                composable<NexusDestination.AIChat> {
                    AIChatScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.AIResumeAnalyzer> {
                    AIResumeAnalyzerScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Roadmap> {
                    RoadmapGeneratorScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.SkillGap> {
                    SkillGapScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.LinkedInPost> {
                    LinkedInPostScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.InterviewCoach> {
                    InterviewCoachScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.CareerInsights> {
                    CareerInsightsScreen(onBack = { navController.popBackStack() })
                }

                // GROWTH
                composable<NexusDestination.Growth> {
                    GrowthScreen(
                        onDsaTracker = { navController.navigate(NexusDestination.DSATracker) },
                        onGoals = { navController.navigate(NexusDestination.Goals) },
                        onOracle = { navController.navigate(NexusDestination.Oracle) },
                        onArena = { navController.navigate(NexusDestination.Arena) },
                        onSkills = { navController.navigate(NexusDestination.Profile) },
                        showBackArrow = false,
                    )
                }
                composable<NexusDestination.DSATracker> {
                    DSATrackerScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Goals> {
                    GoalsScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Oracle> {
                    NexusOracleScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Arena> {
                    NexusArenaScreen(onBack = { navController.popBackStack() })
                }

                // LEGACY SCREENS
                composable<NexusDestination.InternshipList> {
                    InternshipListScreen(
                        onInternshipClick = { id -> navController.navigate(NexusDestination.InternshipDetail(id)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<NexusDestination.InternshipDetail> { entry ->
                    val dest = entry.toRoute<NexusDestination.InternshipDetail>()
                    InternshipDetailScreen(internshipId = dest.internshipId, onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Profile> {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onSettings = { navController.navigate(NexusDestination.Settings) },
                        onAchievements = { navController.navigate(NexusDestination.Achievements) },
                        onLeaderboard = { navController.navigate(NexusDestination.Leaderboard) },
                        onGoPro = { navController.navigate(NexusDestination.Paywall) },
                        onApplications = { navController.navigate(NexusDestination.Applications) },
                        onPlacementForecast = { navController.navigate(NexusDestination.PlacementForecast) },
                        onCareerMirror = { navController.navigate(NexusDestination.CareerMirror) },
                        onCohort = { navController.navigate(NexusDestination.CohortIntelligence) },
                        onInterviewLog = { navController.navigate(NexusDestination.InterviewLog) },
                        onPortfolio = { navController.navigate(NexusDestination.Portfolio) },
                        onSearch = { navController.navigate(NexusDestination.Search()) },
                        onCommunity = { navController.navigate(NexusDestination.Community) },
                        onTribe = { navController.navigate(NexusDestination.Tribe) },
                        showBackArrow = false,
                    )
                }
                composable<NexusDestination.Research> {
                    ResearchScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Paywall> {
                    com.nexus.paywall.PaywallScreen(
                        onDismiss = { navController.popBackStack() },
                        onPurchased = { navController.popBackStack() },
                    )
                }
                composable<NexusDestination.Achievements> {
                    AchievementsScreen(
                        onBack = { navController.popBackStack() },
                        onSettings = { navController.navigate(NexusDestination.Settings) },
                    )
                }
                composable<NexusDestination.Leaderboard> {
                    LeaderboardScreen(
                        onBack = { navController.popBackStack() },
                        onOpenArena = { navController.navigate(NexusDestination.Arena) },
                    )
                }
                composable<NexusDestination.Notifications> {
                    NotificationsScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.CertificationBrowser> {
                    CertificationBrowserScreen(
                        onBack = { navController.popBackStack() },
                        onCertClick = { id -> navController.navigate(NexusDestination.CertificationDetail(id)) }
                    )
                }
                composable<NexusDestination.Productivity> {
                    ProductivityScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Settings> {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onGoPro = { navController.navigate(NexusDestination.Paywall) },
                    )
                }
                composable<NexusDestination.CertificationDetail> { entry ->
                    val dest = entry.toRoute<NexusDestination.CertificationDetail>()
                    CertificationDetailScreen(certId = dest.certId, onBack = { navController.popBackStack() })
                }

                // CAREER INTELLIGENCE & COMMUNITY
                composable<NexusDestination.Applications> {
                    ApplicationsScreen(onNavigateBack = { navController.popBackStack() })
                }
                composable<NexusDestination.CareerMirror> {
                    CareerMirrorScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.CohortIntelligence> {
                    CohortIntelligenceScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Community> {
                    CommunityScreen(
                        onBack = { navController.popBackStack() },
                        onOpenCohort = { navController.navigate(NexusDestination.CohortIntelligence) },
                        onOpenTribe = { navController.navigate(NexusDestination.Tribe) },
                        onOpenHackathon = { hackathonId ->
                            navController.navigate(NexusDestination.HackathonDetail(hackathonId))
                        },
                    )
                }
                composable<NexusDestination.InterviewLog> {
                    InterviewLogScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.PlacementForecast> {
                    PlacementForecastScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Portfolio> {
                    PortfolioScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Search> { entry ->
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        onResultClick = { id -> navController.navigate(NexusDestination.InternshipDetail(id)) },
                        initialQuery = entry.toRoute<NexusDestination.Search>().initialQuery,
                    )
                }
                composable<NexusDestination.Tribe> {
                    TribeScreen(onBack = { navController.popBackStack() })
                }
            }
                }
            }
        }

            // Only once the chrome exists: a tour that spotlights a bar which never
            // got laid out would point at nothing.
            if (tourVisible && chromeVisible) {
                GuidedTour(
                    stationsBounds = stationsBounds,
                    onNavigateToStation = onNavigateTab,
                    onFinish = { tourViewModel.finish() },
                )
            }

            // The palette floats above the shell: reachable from wherever the
            // user is standing, not just from the screen that hosts a trigger.
            CommandPaletteOverlay(
                open = paletteOpen,
                query = paletteQuery,
                onQueryChange = { paletteQuery = it },
                onDismiss = {
                    paletteOpen = false
                    paletteQuery = ""
                },
                onNavigate = navController::navigate,
            )
        }
    }
}

/** The palette's presentation half: filters the destination index, offers the
 *  content-search hand-off once there is something to search, and maps a
 *  selection onto the navigation graph. The shell owns the state; this owns
 *  the shape and the routing decision. */
@Composable
private fun CommandPaletteOverlay(
    open: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onNavigate: (NexusDestination) -> Unit,
) {
    if (!open) return
    val destinations = paletteDestinations()
    NexusCommandPalette(
        query = query,
        onQueryChange = onQueryChange,
        hint = stringResource(R.string.palette_hint),
        emptyLabel = stringResource(R.string.palette_empty),
        commands = buildList {
            addAll(destinations.map { it.first })
            if (query.trim().length >= 2) {
                add(
                    NexusCommand(
                        id = "search-content",
                        title = stringResource(R.string.palette_search_content, query.trim()),
                        category = "FIND",
                    ),
                )
            }
        },
        onSelect = { command ->
            val destination = destinations
                .firstOrNull { it.first.id == command.id }
                ?.second
            when {
                command.id == "search-content" ->
                    onNavigate(NexusDestination.Search(query.trim()))
                destination != null -> onNavigate(destination)
            }
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

/**
 * One rule for every piece of chrome: switching a tab replaces the stack rather
 * than stacking on it, and state is restored against Home so a destination never
 * sits behind the one the user just came from. The bar, the rail and the drawer
 * all call this, which is the only reason they cannot drift apart.
 */
private fun navigateToTab(
    navController: NavHostController,
    destinations: List<NexusDestination>,
    index: Int,
) {
    navController.navigate(destinations[index]) {
        popUpTo(NexusDestination.Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** The palette's index of the product: every place worth jumping to, grouped
 *  the way a table of contents groups chapters. Curated rather than
 *  exhaustive — the five tabs, the finders, the career tools, the rooms. */
@Composable
private fun paletteDestinations(): List<Pair<NexusCommand, NexusDestination>> = listOf(
    NexusCommand("home", stringResource(R.string.palette_destination_home), category = "GO") to
        NexusDestination.Home,
    NexusCommand("explore", stringResource(R.string.palette_destination_explore), category = "GO") to
        NexusDestination.Explore,
    NexusCommand("aihub", stringResource(R.string.palette_destination_aihub), category = "GO") to
        NexusDestination.AIHub,
    NexusCommand("growth", stringResource(R.string.palette_destination_growth), category = "GO") to
        NexusDestination.Growth,
    NexusCommand("profile", stringResource(R.string.palette_destination_profile), category = "GO") to
        NexusDestination.Profile,
    NexusCommand("search", stringResource(R.string.palette_destination_search), category = "FIND") to
        NexusDestination.Search(),
    NexusCommand("internships", stringResource(R.string.palette_destination_internships), category = "FIND") to
        NexusDestination.InternshipList,
    NexusCommand("certifications", stringResource(R.string.palette_destination_certifications), category = "FIND") to
        NexusDestination.CertificationBrowser,
    NexusCommand("applications", stringResource(R.string.palette_destination_applications), category = "CAREER") to
        NexusDestination.Applications,
    NexusCommand("forecast", stringResource(R.string.palette_destination_forecast), category = "CAREER") to
        NexusDestination.PlacementForecast,
    NexusCommand("portfolio", stringResource(R.string.palette_destination_portfolio), category = "CAREER") to
        NexusDestination.Portfolio,
    NexusCommand("interviewlog", stringResource(R.string.palette_destination_interviewlog), category = "CAREER") to
        NexusDestination.InterviewLog,
    NexusCommand("careermirror", stringResource(R.string.palette_destination_careermirror), category = "CAREER") to
        NexusDestination.CareerMirror,
    NexusCommand("goals", stringResource(R.string.palette_destination_goals), category = "GROW") to
        NexusDestination.Goals,
    NexusCommand("dsa", stringResource(R.string.palette_destination_dsa), category = "GROW") to
        NexusDestination.DSATracker,
    NexusCommand("productivity", stringResource(R.string.palette_destination_productivity), category = "GROW") to
        NexusDestination.Productivity,
    NexusCommand("achievements", stringResource(R.string.palette_destination_achievements), category = "GROW") to
        NexusDestination.Achievements,
    NexusCommand("leaderboard", stringResource(R.string.palette_destination_leaderboard), category = "GROW") to
        NexusDestination.Leaderboard,
    NexusCommand("arena", stringResource(R.string.palette_destination_arena), category = "GROW") to
        NexusDestination.Arena,
    NexusCommand("notifications", stringResource(R.string.palette_destination_notifications), category = "GROW") to
        NexusDestination.Notifications,
    NexusCommand("aichat", stringResource(R.string.palette_destination_aichat), category = "AI") to
        NexusDestination.AIChat,
    NexusCommand("research", stringResource(R.string.palette_destination_research), category = "AI") to
        NexusDestination.Research,
    NexusCommand("cohort", stringResource(R.string.palette_destination_cohort), category = "MORE") to
        NexusDestination.CohortIntelligence,
    NexusCommand("oracle", stringResource(R.string.palette_destination_oracle), category = "MORE") to
        NexusDestination.Oracle,
    NexusCommand("community", stringResource(R.string.palette_destination_community), category = "MORE") to
        NexusDestination.Community,
    NexusCommand("tribe", stringResource(R.string.palette_destination_tribe), category = "MORE") to
        NexusDestination.Tribe,
    NexusCommand("settings", stringResource(R.string.palette_destination_settings), category = "MORE") to
        NexusDestination.Settings,
)

/** What arrives on a page change: peers fade-lift, a detail slides in from
 *  its parent, and reduced motion swaps instantly — the hop itself carries
 *  the meaning, so no movement is added on top of it. */private fun navEnter(
    reducedMotion: Boolean,
    isTopLevel: (NavBackStackEntry) -> Boolean,
    backward: Boolean = false,
): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    when {
        reducedMotion -> EnterTransition.None
        isTopLevel(initialState) && isTopLevel(targetState) -> NexusTransitions.peerEnter()
        backward -> NexusTransitions.backwardEnter()
        else -> NexusTransitions.forwardEnter()
    }
}

/** What leaves on a page change — the mirror of [navEnter]. */
private fun navExit(
    reducedMotion: Boolean,
    isTopLevel: (NavBackStackEntry) -> Boolean,
    backward: Boolean = false,
): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    when {
        reducedMotion -> ExitTransition.None
        isTopLevel(initialState) && isTopLevel(targetState) -> NexusTransitions.peerExit()
        backward -> NexusTransitions.backwardExit()
        else -> NexusTransitions.forwardExit()
    }
}
