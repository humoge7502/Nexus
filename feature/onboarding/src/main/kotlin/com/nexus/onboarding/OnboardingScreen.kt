package com.nexus.onboarding

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.CalibratedPageScale
import com.nexus.designsystem.component.InstrumentPlate
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusWordmark
import com.nexus.designsystem.component.SignalChoice
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.feedback.EditorialStatusBand
import com.nexus.designsystem.component.feedback.StatusTone
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

private const val SETUP_PAGE_INDEX = 3
private const val MAX_SKILLS = 5

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
    val ofPages = pages.size + 1
    val pagerState = rememberPagerState(pageCount = { ofPages })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == SETUP_PAGE_INDEX
    val stepLabel = stringResource(R.string.onboarding_step_of, pagerState.currentPage + 1, ofPages)

    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(
            modifier = Modifier.matchParentSize(),
            bloomHeight = 460.dp,
        )

        Column(modifier = Modifier.fillMaxSize()) {
            NexusWordmark(
                label = stringResource(R.string.onboarding_wordmark),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = NexusSpacing.xl, vertical = NexusSpacing.xs),
            ) {
                TextButton(
                    onClick = { viewModel.skipAndComplete(onComplete) },
                    modifier = Modifier.heightIn(min = NexusSpacing.touchTarget),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = pagerState.currentPage != SETUP_PAGE_INDEX,
                beyondViewportPageCount = 1,
            ) { page ->
                Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                    if (page == SETUP_PAGE_INDEX) {
                        ProfileSetupPage(state = state, viewModel = viewModel)
                    } else {
                        OnboardingPageContent(
                            page = pages[page],
                            pageIndex = page,
                            pagerState = pagerState,
                        )
                    }
                }
            }

            OnboardingFooter(
                state = state,
                isLastPage = isLastPage,
                pageCount = ofPages,
                currentPage = pagerState.currentPage,
                stepLabel = stepLabel,
                onFinish = { viewModel.saveAndComplete(onComplete) },
                onContinue = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
            )
        }
    }
}

@Composable
private fun OnboardingFooter(
    state: OnboardingUiState,
    isLastPage: Boolean,
    pageCount: Int,
    currentPage: Int,
    stepLabel: String,
    onFinish: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(
                start = NexusSpacing.xl,
                end = NexusSpacing.xl,
                top = NexusSpacing.xs,
                bottom = NexusSpacing.md,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalibratedPageScale(
                pageCount = pageCount,
                currentPage = currentPage,
            )
            Text(
                text = stepLabel.uppercase(Locale.ROOT),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(NexusSpacing.md))

        state.saveFault?.let {
            EditorialStatusBand(
                label = stringResource(R.string.onboarding_setup_not_saved),
                detail = stringResource(R.string.onboarding_save_retry_hint),
                tone = StatusTone.Fault,
            )
            Spacer(modifier = Modifier.height(NexusSpacing.sm))
        }

        NexusButton(
            text = stringResource(
                if (isLastPage) R.string.onboarding_finish_setup else R.string.onboarding_continue,
            ),
            onClick = if (isLastPage) onFinish else onContinue,
            enabled = !state.isSaving,
            loading = state.isSaving,
            icon = if (isLastPage) Icons.Default.Check else null,
            gradient = NexusGradients.SignalSweep,
            modifier = Modifier.height(56.dp),
        )
    }
}

@Composable
private fun pageParallaxModifier(pageIndex: Int, pagerState: PagerState): Modifier {
    val reducedMotion = LocalReducedMotion.current
    if (reducedMotion) return Modifier
    return Modifier.graphicsLayer {
        val offset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
            .coerceIn(-1f, 1f)
        translationX = offset * size.width * 0.18f
        val shrink = 1f - abs(offset) * 0.06f
        scaleX = shrink
        scaleY = shrink
        alpha = 1f - abs(offset) * 0.75f
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    pagerState: PagerState,
) {
    val ext = MaterialTheme.nexus
    val parallax = pageParallaxModifier(pageIndex = pageIndex, pagerState = pagerState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(parallax)
            .padding(horizontal = NexusSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        InstrumentPlate(
            modifier = Modifier.size(112.dp),
            shape = NexusShapes.xl,
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = ext.signal,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(modifier = Modifier.height(NexusSpacing.xxl))

        SignalEyebrow(
            text = stringResource(R.string.onboarding_module, pageIndex + 1),
        )

        Spacer(modifier = Modifier.height(NexusSpacing.sm))

        Text(
            text = page.title,
            style = NexusTypography.DisplayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(NexusSpacing.sm))

        Text(
            text = page.subtitle,
            style = NexusTypography.BodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SignalEyebrow(text: String) {
    val ext = MaterialTheme.nexus
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(10.dp)
                .background(ext.signal),
        )
        Text(
            text = text.uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = ext.signal,
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
            .padding(horizontal = NexusSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
    ) {
        Spacer(modifier = Modifier.height(NexusSpacing.xs))

        SignalEyebrow(text = stringResource(R.string.onboarding_profile_calibration))

        Text(
            text = stringResource(R.string.text_make_it_yours),
            style = NexusTypography.HeadlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.text_your_matches_forecasts_and_ai_answers_are),
            style = NexusTypography.BodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(NexusSpacing.xxs))

        NexusTextField(
            value = state.userName,
            onValueChange = viewModel::updateName,
            label = stringResource(R.string.onboarding_your_name),
        )
        NexusTextField(
            value = state.college,
            onValueChange = viewModel::updateCollege,
            label = stringResource(R.string.onboarding_college),
        )

        SectionLabel(text = stringResource(R.string.onboarding_year_of_study))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
        ) {
            yearOptions.forEach { year ->
                SignalChoice(
                    label = year,
                    selected = state.academicYear == year,
                    onClick = { viewModel.updateYear(year) },
                )
            }
        }

        SectionLabel(
            text = stringResource(R.string.onboarding_skills_pick_up_to_five),
            trailing = stringResource(R.string.onboarding_skill_quota, state.skills.size, MAX_SKILLS),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
        ) {
            skillOptions.forEach { skill ->
                val selected = skill in state.skills
                SignalChoice(
                    label = skill,
                    selected = selected,
                    enabled = selected || state.skills.size < MAX_SKILLS,
                    onClick = { viewModel.toggleSkill(skill) },
                )
            }
        }

        SectionLabel(text = stringResource(R.string.onboarding_what_are_you_looking_for))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
        ) {
            interestOptions.forEach { interest ->
                SignalChoice(
                    label = interest,
                    selected = interest in state.interests,
                    onClick = { viewModel.toggleInterest(interest) },
                )
            }
        }

        NexusTextField(
            value = state.careerGoal,
            onValueChange = viewModel::updateCareerGoal,
            label = stringResource(R.string.onboarding_career_goal_optional),
            placeholder = stringResource(R.string.onboarding_e_g_sde_internship_at_a),
        )

        Spacer(modifier = Modifier.height(NexusSpacing.sm))
    }
}

@Composable
private fun SectionLabel(text: String, trailing: String? = null) {
    val ext = MaterialTheme.nexus
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(NexusSpacing.sm))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(NexusSpacing.hairline)
                .background(ext.hairlineStrong.copy(alpha = 0.5f)),
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(NexusSpacing.sm))
            Text(
                text = trailing,
                style = NexusTypography.TelemetryMicro,
                color = ext.signal,
            )
        }
    }
}
