package hr.foi.squadsync.ui.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hr.foi.squadsync.R
import hr.foi.squadsync.domain.teams.TeamJoinError
import hr.foi.squadsync.ui.theme.SquadSyncTheme

// Minimalni izgled. Uskladivanje sa skicom ide u zaseban tiket za redizajn.
@Composable
fun JoinTeamSection(
    state: JoinTeamUiState,
    onAccessCodeChange: (String) -> Unit,
    onJoinClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.team_join_title), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.accessCode,
            onValueChange = onAccessCodeChange,
            label = { Text(stringResource(R.string.team_join_code_label)) },
            isError = state.codeError != null,
            enabled = !state.isSubmitting,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth()
        )

        if (state.codeError != null) {
            Text(state.codeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (state.error != null) {
            Text(stringResource(teamJoinErrorMessage(state.error)), color = MaterialTheme.colorScheme.error)
        }
        if (state.joinedTeamName != null) {
            Text(stringResource(R.string.team_join_success, state.joinedTeamName), color = MaterialTheme.colorScheme.primary)
        }

        Button(onClick = onJoinClick, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.team_join_button))
            }
        }
    }
}

private fun teamJoinErrorMessage(error: TeamJoinError): Int {
    return when (error) {
        TeamJoinError.NOT_ATHLETE -> R.string.team_join_not_athlete
        TeamJoinError.INVALID_CODE -> R.string.team_join_invalid_code
        TeamJoinError.ALREADY_MEMBER -> R.string.team_join_already_member
        TeamJoinError.SERVICE_UNAVAILABLE -> R.string.team_join_unavailable
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun JoinTeamSectionPreview() {
    SquadSyncTheme(darkTheme = false) {
        Column(Modifier.padding(24.dp)) {
            JoinTeamSection(JoinTeamUiState(accessCode = "X7K2P4", error = TeamJoinError.INVALID_CODE), {}, {})
        }
    }
}
