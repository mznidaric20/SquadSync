package hr.foi.squadsync.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hr.foi.squadsync.R
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.ui.theme.SquadSyncTheme

@Composable
fun RegistrationScreen(
    state: RegistrationUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onSubmit: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthLayout(true, onLoginClick, onRegistration = {}, modifier, navigationEnabled = !state.isSubmitting) { compact ->
        if (!compact) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.create_account_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.registration_description), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        BoxWithConstraints {
            val firstName: @Composable (Modifier) -> Unit = { fieldModifier ->
                AuthField(stringResource(R.string.first_name_label), state.firstName, onFirstNameChange,
                    stringResource(R.string.first_name_placeholder), R.drawable.ic_person,
                    fieldModifier.testTag("register_first_name"), error = state.errors.ime, enabled = !state.isSubmitting)
            }
            val lastName: @Composable (Modifier) -> Unit = { fieldModifier ->
                AuthField(stringResource(R.string.last_name_label), state.lastName, onLastNameChange,
                    stringResource(R.string.last_name_placeholder), R.drawable.ic_person,
                    fieldModifier.testTag("register_last_name"), error = state.errors.prezime, enabled = !state.isSubmitting)
            }
            if (maxWidth >= 320.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    firstName(Modifier.weight(1f))
                    lastName(Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    firstName(Modifier.fillMaxWidth())
                    lastName(Modifier.fillMaxWidth())
                }
            }
        }
        AuthField(stringResource(R.string.email_label), state.email, onEmailChange,
            stringResource(R.string.email_placeholder), R.drawable.ic_mail,
            Modifier.testTag("register_email"), error = state.errors.email,
            enabled = !state.isSubmitting, keyboardType = KeyboardType.Email)
        AuthField(stringResource(R.string.password_label), state.password, onPasswordChange,
            stringResource(R.string.new_password_placeholder), R.drawable.ic_lock,
            Modifier.testTag("register_password"), error = state.errors.lozinka,
            password = true, enabled = !state.isSubmitting)
        AuthField(stringResource(R.string.confirm_password_label), state.confirmPassword, onConfirmPasswordChange,
            stringResource(R.string.confirm_password_placeholder), R.drawable.ic_lock,
            Modifier.testTag("register_confirm_password"), error = state.errors.potvrdaLozinke,
            password = true, enabled = !state.isSubmitting, imeAction = ImeAction.Done, onDone = onSubmit)
        RolePicker(state.role, onRoleChange, !state.isSubmitting, state.errors.uloga)
        state.error?.let { AuthErrorMessage(it) }
        AuthSubmitButton(stringResource(R.string.sign_up), stringResource(R.string.signing_up),
            state.isSubmitting, onSubmit, Modifier.testTag("register_submit"))
        if (!compact) {
            Text(stringResource(R.string.required_fields_hint), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            SessionHint()
            TextButton(onLoginClick, enabled = !state.isSubmitting, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.has_account) + " " + stringResource(R.string.sign_in))
            }
        }
    }
}

@Composable
private fun RolePicker(selected: UserRole?, onRoleChange: (UserRole) -> Unit, enabled: Boolean, error: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.role_label), style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UserRole.entries.forEach { role ->
                val isSelected = selected == role
                val shape = RoundedCornerShape(16.dp)
                val athlete = role == UserRole.ATHLETE
                Column(Modifier.weight(1f).clip(shape).background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .border(if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape)
                    .selectable(isSelected, enabled = enabled, role = Role.RadioButton, onClick = { onRoleChange(role) })
                    .testTag("role_${role.name}").padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AuthIcon(if (athlete) R.drawable.ic_athlete else R.drawable.ic_coach,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.weight(1f))
                        if (isSelected) AuthIcon(R.drawable.ic_check, tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(stringResource(if (athlete) R.string.role_athlete else R.string.role_coach),
                        style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(if (athlete) R.string.role_athlete_description else R.string.role_coach_description),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        error?.let { FieldError(it) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun RegistrationPreview() {
    SquadSyncTheme(darkTheme = false) {
        RegistrationScreen(RegistrationUiState(role = UserRole.ATHLETE), {}, {}, {}, {}, {}, {}, {}, {})
    }
}
