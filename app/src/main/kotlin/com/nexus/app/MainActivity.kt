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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.nexus.designsystem.component.NexusNavigationLayout
import com.nexus.designsystem.component.NexusRailBar
import com.nexus.designsystem.component.NexusSideBar
import com.nexus.designsystem.component.nexusNavigationLayoutFor
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

    AnimatedContent(
        targetState = currentStep,
        // The boot flow only ever moves forward — auth, then onboarding, then the
        // app — so it takes the forward pair rather than inventing its own timing.
        transitionSpec = { NexusTransitions.forwardEnter() togetherWith NexusTransitions.forwardExit() },
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
                    NavHost(
                        navController = navController,
                        startDestination = NexusDestination.Home,
                        enterTransition = { NexusTransitions.forwardEnter() },
                        exitTransition = { NexusTransitions.forwardExit() },
                        popEnterTransition = { NexusTransitions.backwardEnter() },
                        popExitTransition = { NexusTransitions.backwardExit() },
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
                        onNavigateToNotifications = { navController.navigate(NexusDestination.Notifications) }
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
                        onSearch = { navController.navigate(NexusDestination.Search) },
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
                composable<NexusDestination.Search> {
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        onResultClick = { id -> navController.navigate(NexusDestination.InternshipDetail(id)) },
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
        }
    }
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
