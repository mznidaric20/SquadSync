package hr.foi.squadsync.ui.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hr.foi.squadsync.R
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamRepository
import kotlinx.coroutines.CancellationException

@Composable
fun MyCreatedTeamsScreen(
    repository: TeamRepository,
    onShowMembers: (String) -> Unit,
    onBack: () -> Unit
) {
    var teams by remember {
        mutableStateOf<List<Team>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(repository, refreshKey) {
        isLoading = true
        error = null

        try {
            teams = repository.getMyTeams()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "Nije moguće dohvatiti momčadi."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(24.dp)
            .testTag("my_teams"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Moje momčadi",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Popis momčadi koje si kreirao.",
            style = MaterialTheme.typography.bodyMedium
        )

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            teams.isEmpty() -> {
                Text("Još nemaš kreiranih momčadi.", modifier = Modifier.testTag("no_teams"))
            }

            else -> {
                teams.forEach { team ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = team.name,
                                style = MaterialTheme.typography.titleLarge
                            )

                            team.description?.let {
                                Text(it)
                            }

                            team.accessCode?.let { accessCode ->
                                Text(
                                    text = "Pristupni kod: $accessCode",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            OutlinedButton(
                                onClick = { onShowMembers(team.id) },
                                modifier = Modifier.fillMaxWidth()
                                    .testTag("show_members_${team.id}")
                            ) {
                                Text(stringResource(R.string.teams_show_members))
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { refreshKey++ },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Osvježi")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Natrag")
        }
    }
}
