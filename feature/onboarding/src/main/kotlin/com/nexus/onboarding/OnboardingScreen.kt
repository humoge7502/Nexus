package com.nexus.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>,
)

private val pages = listOf(
    OnboardingPage(
        emoji = "🚀",
        title = "Track Your\nCareer Journey",
        subtitle = "Monitor internship applications, certifications, and skill growth in one place.",
        gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
    ),
    OnboardingPage(
        emoji = "🎯",
        title = "Smart\nProductivity",
        subtitle = "AI-powered study plans, deadline reminders, and progress analytics made for CSE students.",
        gradient = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
    ),
    OnboardingPage(
        emoji = "🏆",
        title = "Stand Out\nFrom The Crowd",
        subtitle = "Build your portfolio, earn certifications, connect with opportunities matching your skills.",
        gradient = listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
    ),
)

private const val SETUP_PAGE_INDEX = 3

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { pages.size + 1 })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        val currentPage = pagerState.currentPage
        val accent = pages.getOrNull(currentPage)?.gradient?.first()
            ?: Color(0xFF6366F1)
        val bgColor by animateColorAsState(
            targetValue = accent.copy(alpha = 0.08f),
            label = "bg",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, end = 24.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { viewModel.skipAndComplete(onComplete) }) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = pagerState.currentPage != SETUP_PAGE_INDEX,
            ) { page ->
                if (page == SETUP_PAGE_INDEX) {
                    ProfileSetupPage(state = state, viewModel = viewModel)
                } else {
                    OnboardingPageContent(pages[page])
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(pages.size + 1) { index ->
                        val width by animateDpAsState(
                            targetValue = if (index == pagerState.currentPage) 32.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dotWidth",
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (index == pagerState.currentPage) {
                                pages.getOrNull(pagerState.currentPage)?.gradient?.first()
                                    ?: Color(0xFF6366F1)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            },
                            label = "dotColor",
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(RoundedCornerShape(4.dp))
                                .background(dotColor),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isLastPage = pagerState.currentPage == SETUP_PAGE_INDEX
                Button(
                    onClick = {
                        if (isLastPage) {
                            viewModel.saveAndComplete(onComplete)
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages.getOrNull(pagerState.currentPage)
                            ?.gradient?.first() ?: Color(0xFF6366F1),
                    ),
                ) {
                    AnimatedContent(
                        targetState = isLastPage,
                        label = "buttonText",
                    ) { last ->
                        Text(
                            text = if (last) "Finish setup" else "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = page.emoji,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = MaterialTheme.typography.displayLarge.fontSize * 2,
            ),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileSetupPage(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Make it yours",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Your matches, forecasts, and AI answers are personalized from this profile. It stays on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.userName,
            onValueChange = viewModel::updateName,
            label = { Text("Your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.college,
            onValueChange = viewModel::updateCollege,
            label = { Text("College") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Year of study")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            yearOptions.forEach { year ->
                FilterChip(
                    selected = state.academicYear == year,
                    onClick = { viewModel.updateYear(year) },
                    label = { Text(year) },
                )
            }
        }

        SectionLabel("Skills (pick up to 5)")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            skillOptions.forEach { skill ->
                val selected = skill in state.skills
                FilterChip(
                    selected = selected,
                    enabled = selected || state.skills.size < 5,
                    onClick = { viewModel.toggleSkill(skill) },
                    label = { Text(skill) },
                )
            }
        }

        SectionLabel("What are you looking for?")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            interestOptions.forEach { interest ->
                FilterChip(
                    selected = interest in state.interests,
                    onClick = { viewModel.toggleInterest(interest) },
                    label = { Text(interest) },
                )
            }
        }

        OutlinedTextField(
            value = state.careerGoal,
            onValueChange = viewModel::updateCareerGoal,
            label = { Text("Career goal (optional)") },
            placeholder = { Text("e.g. SDE internship at a product company") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}
