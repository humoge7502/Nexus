package com.nexus.onboarding

import androidx.compose.ui.res.stringResource
import com.nexus.onboarding.R

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

private const val SETUP_PAGE_INDEX = 3

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.RocketLaunch,
            title = stringResource(R.string.onboarding_track_career_title),
            subtitle = stringResource(R.string.onboarding_track_career_subtitle),
        ),
        OnboardingPage(
            icon = Icons.Default.Bolt,
            title = stringResource(R.string.onboarding_smart_productivity_title),
            subtitle = stringResource(R.string.onboarding_smart_productivity_subtitle),
        ),
        OnboardingPage(
            icon = Icons.Default.WorkspacePremium,
            title = stringResource(R.string.onboarding_stand_out_title),
            subtitle = stringResource(R.string.onboarding_stand_out_subtitle),
        ),
    )
    val pagerState = rememberPagerState(pageCount = { pages.size + 1 })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, end = 24.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { viewModel.skipAndComplete(onComplete) }) {
                    Text(stringResource(R.string.onboarding_skip),
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
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PageIndicator(
                    pageCount = pages.size + 1,
                    currentPage = pagerState.currentPage,
                )

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
                ) {
                    AnimatedContent(
                        targetState = isLastPage,
                        label = "buttonText",
                    ) { last ->
                        Text(
                            text = if (last) {
                                stringResource(R.string.onboarding_finish_setup)
                            } else {
                                stringResource(R.string.onboarding_continue)
                            },
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
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val width by animateDpAsState(
                targetValue = if (index == currentPage) 32.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dotWidth",
            )
            val dotColor by animateColorAsState(
                targetValue = if (index == currentPage) {
                    MaterialTheme.colorScheme.primary
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
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(modifier = Modifier.height(36.dp))
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
        Text(text = stringResource(R.string.text_make_it_yours),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(text = stringResource(R.string.text_your_matches_forecasts_and_ai_answers_are),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.userName,
            onValueChange = viewModel::updateName,
            label = { Text(stringResource(R.string.onboarding_your_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.college,
            onValueChange = viewModel::updateCollege,
            label = { Text(stringResource(R.string.onboarding_college)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel(stringResource(R.string.onboarding_year_of_study))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            yearOptions.forEach { year ->
                FilterChip(
                    selected = state.academicYear == year,
                    onClick = { viewModel.updateYear(year) },
                    label = { Text(year) },
                )
            }
        }

        SectionLabel(stringResource(R.string.onboarding_skills_pick_up_to_five))
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

        SectionLabel(stringResource(R.string.onboarding_what_are_you_looking_for))
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
            label = { Text(stringResource(R.string.onboarding_career_goal_optional)) },
            placeholder = { Text(stringResource(R.string.onboarding_e_g_sde_internship_at_a)) },
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
