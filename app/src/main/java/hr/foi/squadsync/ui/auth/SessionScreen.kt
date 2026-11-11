package hr.foi.squadsync.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hr.foi.squadsync.R
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.UserProfile
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.ui.theme.SquadCyan
import hr.foi.squadsync.ui.theme.SquadNavy
import hr.foi.squadsync.ui.theme.SquadSyncTheme

@Composable
fun SessionScreen(user: UserProfile, state: SessionUiState, onRefresh: () -> Unit, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(SquadNavy, Color(0xFF0E3670))))) {
            CourtArtwork(Modifier.matchParentSize())
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().align(Alignment.Center).statusBarsPadding().padding(24.dp)) {
                SquadBrand()
                Spacer(Modifier.height(40.dp))
                Text(stringResource(R.string.session_active), style = MaterialTheme.typography.labelSmall, color = SquadCyan)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.hello_user, user.firstName), color = Color.White,
                    style = MaterialTheme.typography.displaySmall, modifier = Modifier.testTag("session_greeting"))
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.profile_description), style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFBECEE6))
                Spacer(Modifier.height(24.dp))
            }
        }
        Surface(Modifier.widthIn(max = 552.dp).fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-16).dp),
            shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(60.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text(user.firstName.take(1) + user.lastName.take(1),
                            style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(user.fullName, style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(if (user.role == UserRole.ATHLETE) R.string.role_athlete else R.string.role_coach),
                            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(stringResource(R.string.email_label), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user.email, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("session_email"))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AuthIcon(R.drawable.ic_shield, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.session_saved), style = MaterialTheme.typography.labelLarge)
                }
                state.error?.let { AuthErrorMessage(it) }
                OutlinedButton(onRefresh, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = !state.isBusy,
                    shape = RoundedCornerShape(14.dp)) {
                    if (state.isBusy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text(stringResource(R.string.refresh_profile))
                }
                TextButton(onSignOut, enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth().testTag("sign_out")) {
                    Text(stringResource(R.string.sign_out), fontWeight = FontWeight.Bold)
                }
                Text(stringResource(R.string.profile_local_note), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun SessionLoadingScreen() {
    Column(Modifier.fillMaxSize().background(SquadNavy).safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        SquadBrand()
        Spacer(Modifier.height(36.dp))
        CircularProgressIndicator(color = SquadCyan, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.restoring_session), color = Color(0xFFBECEE6), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun SessionRecoveryScreen(error: AuthError, state: SessionUiState, onRetry: () -> Unit, onSignOut: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.restore_failed_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))
            AuthErrorMessage(state.error ?: error)
            Spacer(Modifier.height(20.dp))
            Button(onRetry, enabled = !state.isBusy) { Text(stringResource(R.string.retry)) }
            TextButton(onSignOut, enabled = !state.isBusy) { Text(stringResource(R.string.back_to_login)) }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 840)
@Composable
private fun SessionPreview() {
    SquadSyncTheme(darkTheme = false) {
        SessionScreen(UserProfile("1", "Luka", "Horvat", "luka@primjer.com", UserRole.ATHLETE), SessionUiState(), {}, {})
    }
}
