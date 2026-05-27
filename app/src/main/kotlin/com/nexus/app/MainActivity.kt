package com.nexus.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nexus.achievements.AchievementsScreen
import com.nexus.achievements.LeaderboardScreen
import com.nexus.ai.AIChatScreen
import com.nexus.ai.AIHubScreen
import com.nexus.ai.AIResumeAnalyzerScreen
import com.nexus.auth.AuthScreen
import com.nexus.auth.AuthViewModel
import com.nexus.certifications.CertificationBrowserScreen
import com.nexus.certifications.CertificationDetailScreen
import com.nexus.common.ApiResult
import com.nexus.designsystem.component.NexusBottomBar
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.explore.ExploreScreen
import com.nexus.explore.HackathonDetailScreen
import com.nexus.goals.GoalsScreen
import com.nexus.growth.DSATrackerScreen
import com.nexus.growth.GrowthScreen
import com.nexus.growth.NexusOracleScreen
import com.nexus.growth.NexusArenaScreen
import com.nexus.home.HomeScreen
import com.nexus.internships.InternshipDetailScreen
import com.nexus.internships.InternshipListScreen
import com.nexus.notifications.NotificationsScreen
import com.nexus.onboarding.OnboardingScreen
import com.nexus.productivity.ProductivityScreen
import com.nexus.profile.ProfileScreen
import com.nexus.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
     internal val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleSignInResult = result.data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusTheme { NexusApp(activity = this@MainActivity) }
        }
    }

    companion object {
        var googleSignInResult: Intent? = null
    }
}

@Composable
fun NexusApp(activity: MainActivity? = null) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    var currentStep by remember { mutableStateOf("loading") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        when (authState) {
            is ApiResult.Success -> {
                val isLoggedIn = (authState as ApiResult.Success<Boolean>).data
                currentStep = if (isLoggedIn) "main" else "auth"
            }
            is ApiResult.Loading -> currentStep = "loading"
            is ApiResult.Error -> currentStep = "auth"
        }
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "bootFlow"
    ) { step ->
        when (step) {
            "loading" -> { /* splash screen visible */ }
            "auth" -> AuthScreen(
                onGoogleSignIn = {
                    authViewModel.initGoogleSignIn(activity ?: return@AuthScreen)
                    val intent = authViewModel.getGoogleSignInIntent()
                    if (intent != null) {
                        activity?.let {
                            it.googleSignInLauncher.launch(intent)
                            scope.launch {
                                MainActivity.googleSignInResult?.let { data ->
                                    authViewModel.handleGoogleSignInResult(data)
                                }
                            }
                        }
                    }
                },
                onGitHubSignIn = { /* GitHub OAuth - implement web flow */ currentStep = "onboarding" },
                onEmailSignIn = { currentStep = "onboarding" },
                onSkip = { currentStep = "onboarding" }
            )
            "onboarding" -> OnboardingScreen(
                onComplete = { currentStep = "main" }
            )
            "main" -> NexusMainScaffold()
        }
    }
}

@Composable
fun NexusMainScaffold() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NexusBottomBar(
                currentRoute = navController.currentDestination?.route ?: "",
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(NexusDestination.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = NexusDestination.Home,
                modifier = Modifier.padding(paddingValues)
            ) {
                // HOME
                composable<NexusDestination.Home> {
                    HomeScreen(
                        onInternshipClick = { id -> navController.navigate(NexusDestination.InternshipDetail(id)) },
                        onCertificationClick = { id -> navController.navigate(NexusDestination.CertificationDetail(id)) },
                        onViewAllInternships = { navController.navigate(NexusDestination.InternshipList) },
                        onNavigateToProductivity = { navController.navigate(NexusDestination.Productivity) },
                        onNavigateToProfile = { navController.navigate(NexusDestination.Profile) }
                    )
                }

                // EXPLORE
                composable<NexusDestination.Explore> {
                    ExploreScreen(
                        onInternshipClick = { id -> navController.navigate(NexusDestination.InternshipDetail(id)) },
                        onHackathonClick = { id -> navController.navigate(NexusDestination.HackathonDetail(id)) },
                        onCertificationClick = { id -> navController.navigate(NexusDestination.CertificationDetail(id)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<NexusDestination.HackathonDetail> { entry ->
                    val dest = entry.toRoute<NexusDestination.HackathonDetail>()
                    HackathonDetailScreen(hackathonId = dest.hackathonId, onBack = { navController.popBackStack() })
                }

                // AI HUB
                composable<NexusDestination.AIHub> {
                    AIHubScreen(
                        onChatClick = { navController.navigate(NexusDestination.AIChat) },
                        onResumeAnalyzer = { navController.navigate(NexusDestination.AIResumeAnalyzer) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<NexusDestination.AIChat> {
                    AIChatScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.AIResumeAnalyzer> {
                    AIResumeAnalyzerScreen(onBack = { navController.popBackStack() })
                }

                // GROWTH
                composable<NexusDestination.Growth> {
                    GrowthScreen(
                        onDsaTracker = { navController.navigate(NexusDestination.DSATracker) },
                        onGoals = { navController.navigate(NexusDestination.Goals) },
                        onOracle = { navController.navigate(NexusDestination.Oracle) },
                        onArena = { navController.navigate(NexusDestination.Arena) },
                        onBack = { navController.popBackStack() }
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
                        onLeaderboard = { navController.navigate(NexusDestination.Leaderboard) }
                    )
                }
                composable<NexusDestination.Achievements> {
                    AchievementsScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.Leaderboard> {
                    LeaderboardScreen(onBack = { navController.popBackStack() })
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
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                composable<NexusDestination.CertificationDetail> { entry ->
                    val dest = entry.toRoute<NexusDestination.CertificationDetail>()
                    CertificationDetailScreen(certId = dest.certId, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
