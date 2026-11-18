package hr.foi.squadsync.ui.teams

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.squadsync.ui.teams.demo.DemoTeamRosters

@Composable
fun TeamsRoute(coachId: String, onBack: () -> Unit) {
    val factory = remember {
        viewModelFactory {
            initializer {
                TeamsViewModel(
                    initialTeams = DemoTeamRosters.teams,
                    savedStateHandle = createSavedStateHandle(),
                    isDemo = true
                )
            }
        }
    }
    val viewModel: TeamsViewModel = viewModel(key = "teams_$coachId", factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TeamsScreen(state = state, onTeamSelect = viewModel::selectTeam, onBack = onBack)
}
