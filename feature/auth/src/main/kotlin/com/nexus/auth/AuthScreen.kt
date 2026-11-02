package com.nexus.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.common.ApiResult
import com.nexus.common.Constants
import com.nexus.designsystem.component.CalibratedRule
import com.nexus.designsystem.component.InstrumentGauge
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusMark
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.NexusWordmark
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

@Composable
fun AuthScreen(
    onGoogleSignIn: () -> Unit = {},
    onGitHubSignIn: () -> Unit = {},
    onSkip: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var showEmailDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val reducedMotion = LocalReducedMotion.current
    LaunchedEffect(Unit) { visible = true }

    if (showEmailDialog) {
        EmailSignInDialog(
            authState = authState,
            onDismiss = { showEmailDialog = false },
            onSignIn = { email, password -> viewModel.signInWithEmail(email, password) },
            onSignUp = { email, password -> viewModel.signUpWithEmail(email, password) },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(
            modifier = Modifier.matchParentSize(),
            bloomHeight = 560.dp,
        )

        AnimatedVisibility(
            visible = visible,
            enter = if (reducedMotion) {
                fadeIn(tween(0))
            } else {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = NexusAnimation.Duration.emphasized,
                        easing = NexusAnimation.Easing.emphasizedDecelerate,
                    ),
                ) + slideInVertically(
                    animationSpec = tween(
                        durationMillis = NexusAnimation.Duration.emphasized,
                        easing = NexusAnimation.Easing.emphasizedDecelerate,
                    ),
                    initialOffsetY = { it / 24 },
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = NexusSpacing.xl, vertical = NexusSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AuthMasthead()

                Spacer(modifier = Modifier.height(NexusSpacing.lg))

                NexusButton(
                    text = stringResource(R.string.auth_google),
                    onClick = onGoogleSignIn,
                    gradient = NexusGradients.SignalSweep,
                    modifier = Modifier.height(56.dp),
                )

                Spacer(modifier = Modifier.height(NexusSpacing.sm))

                NexusOutlineButton(
                    text = stringResource(R.string.auth_github),
                    onClick = onGitHubSignIn,
                    icon = Icons.Default.Code,
                )

                Spacer(modifier = Modifier.height(NexusSpacing.sm))

                NexusOutlineButton(
                    text = stringResource(R.string.auth_email),
                    onClick = { showEmailDialog = true },
                    icon = Icons.Default.Email,
                )

                Spacer(modifier = Modifier.height(NexusSpacing.xs))

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.heightIn(min = NexusSpacing.touchTarget),
                ) {
                    Text(
                        text = stringResource(R.string.text_skip_for_now),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(NexusSpacing.xs))

                AuthPrivacyNotice()
            }
        }
    }
}

/** The typeset opening: wordmark, instrument, eyebrow, headline and calibrated rule. */
@Composable
private fun AuthMasthead() {
    val ext = MaterialTheme.nexus

    NexusWordmark(
        label = stringResource(R.string.auth_wordmark),
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(NexusSpacing.xl))

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        InstrumentGauge(
            progress = 1f,
            modifier = Modifier.matchParentSize(),
            stroke = 8.dp,
            contentDescription = stringResource(R.string.auth_instrument_description),
        )
        NexusMark(size = 64.dp)
    }

    Spacer(modifier = Modifier.height(NexusSpacing.lg))

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
            text = stringResource(R.string.auth_eyebrow).uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = ext.signal,
        )
    }

    Spacer(modifier = Modifier.height(NexusSpacing.sm))

    Text(
        text = stringResource(R.string.text_engineer_your_future),
        style = NexusTypography.DisplayMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(NexusSpacing.xs))

    Text(
        text = stringResource(R.string.text_your_ai_powered_career_co_pilot),
        style = NexusTypography.BodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(NexusSpacing.xl))

    CalibratedRule(
        progress = 1f,
        modifier = Modifier.fillMaxWidth(),
        divisions = 24,
        height = 16.dp,
        accent = ext.signal.copy(alpha = 0.55f),
    )
}

/** What the app collects, then the legal line and its own hit area. */
@Composable
private fun AuthPrivacyNotice() {
    val ext = MaterialTheme.nexus
    val uriHandler = LocalUriHandler.current

    // Play wants this on the screen where data is actually collected, in plain
    // sight rather than behind a link. The wording tracks docs/privacy-policy.html.
    Text(
        text = stringResource(R.string.auth_data_disclosure),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(NexusSpacing.xs))

    Text(
        text = stringResource(R.string.auth_privacy_prefix).trimEnd(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
    )
    Box(
        modifier = Modifier
            .heightIn(min = NexusSpacing.minTouchTarget)
            .clip(NexusShapes.xs)
            .clickable(role = Role.Button) {
                uriHandler.openUri(Constants.PRIVACY_POLICY_URL)
            }
            .padding(horizontal = NexusSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.auth_privacy),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = ext.signal,
        )
    }
}

private fun isEmailValid(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

@Composable
private fun EmailSignInDialog(
    authState: ApiResult<Boolean>,
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val isLoading = authState is ApiResult.Loading
    val errorMessage = (authState as? ApiResult.Error)?.message
    val emailError = attempted && !isEmailValid(email)
    val passwordError = attempted && password.length < 8

    LaunchedEffect(authState) {
        if (authState.dataOrNull == true) {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = NexusSpacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = NexusShapes.xl,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(NexusSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
                ) {
                    EmailDialogHeader(
                        isSignUp = isSignUp,
                        dismissEnabled = !isLoading,
                        onDismiss = onDismiss,
                    )

                    NexusTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = stringResource(R.string.auth_college_email),
                        isError = emailError,
                        supportingText = if (emailError) {
                            stringResource(R.string.auth_enter_a_valid_email_address)
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )

                    EmailPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        visible = passwordVisible,
                        onToggleVisible = { passwordVisible = !passwordVisible },
                        isError = passwordError,
                        supportingText = if (passwordError) {
                            stringResource(R.string.auth_use_at_least_8_characters)
                        } else {
                            null
                        },
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    NexusButton(
                        text = stringResource(
                            if (isSignUp) R.string.auth_create_account else R.string.auth_sign_in,
                        ),
                        onClick = {
                            attempted = true
                            if (isEmailValid(email) && password.length >= 8) {
                                if (isSignUp) onSignUp(email, password) else onSignIn(email, password)
                            }
                        },
                        gradient = NexusGradients.SignalSweep,
                        loading = isLoading,
                    )

                    TextButton(
                        onClick = {
                            isSignUp = !isSignUp
                            attempted = false
                        },
                        enabled = !isLoading,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(
                            text = stringResource(
                                if (isSignUp) R.string.auth_switch_to_sign_in else R.string.auth_switch_to_sign_up,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Eyebrow, title and the close control, which must go dark while a request is in flight. */
@Composable
private fun EmailDialogHeader(
    isSignUp: Boolean,
    dismissEnabled: Boolean,
    onDismiss: () -> Unit,
) {
    val ext = MaterialTheme.nexus

    Row(verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.auth_authentication).uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = ext.signal,
            )
            Spacer(modifier = Modifier.height(NexusSpacing.xs))
            Text(
                text = stringResource(
                    if (isSignUp) R.string.auth_create_account_title else R.string.auth_welcome_back,
                ),
                style = NexusTypography.HeadlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onDismiss, enabled = dismissEnabled) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.auth_cancel),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmailPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    isError: Boolean,
    supportingText: String?,
) {
    NexusTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.auth_password_min_8_characters),
        isError = isError,
        supportingText = supportingText,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = stringResource(
                        if (visible) R.string.auth_hide_password else R.string.auth_show_password,
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
