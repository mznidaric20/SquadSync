package hr.foi.squadsync.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hr.foi.squadsync.R
import hr.foi.squadsync.ui.theme.SquadSyncTheme

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegistrationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthLayout(false, onLogin = {}, onRegistration = onRegistrationClick, modifier,
        navigationEnabled = !state.isSubmitting) { compact ->
        if (!compact) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.welcome_back), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.login_description), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        AuthField(stringResource(R.string.email_label), state.email, onEmailChange,
            stringResource(R.string.email_placeholder), R.drawable.ic_mail,
            Modifier.testTag("login_email"), error = state.errors.email,
            enabled = !state.isSubmitting, keyboardType = KeyboardType.Email)
        AuthField(stringResource(R.string.password_label), state.password, onPasswordChange,
            stringResource(R.string.password_placeholder), R.drawable.ic_lock,
            Modifier.testTag("login_password"), error = state.errors.lozinka, password = true,
            enabled = !state.isSubmitting, imeAction = ImeAction.Done, onDone = onSubmit)
        state.error?.let { AuthErrorMessage(it) }
        AuthSubmitButton(stringResource(R.string.sign_in), stringResource(R.string.signing_in),
            state.isSubmitting, onSubmit, Modifier.testTag("login_submit"))
        if (!compact) {
            SessionHint()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.no_account), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onRegistrationClick, enabled = !state.isSubmitting) {
                    Text(stringResource(R.string.join_team))
                    Spacer(Modifier.width(8.dp))
                    AuthIcon(R.drawable.ic_arrow)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
private fun LoginPreview() {
    SquadSyncTheme(darkTheme = false) { LoginScreen(LoginUiState(), {}, {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun LoginDarkPreview() {
    SquadSyncTheme(darkTheme = true) { LoginScreen(LoginUiState(), {}, {}, {}, {}) }
}
