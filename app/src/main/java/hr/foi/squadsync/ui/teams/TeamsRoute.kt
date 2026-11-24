package hr.foi.squadsync.ui.teams

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.squadsync.domain.teams.TeamRepository

@Composable
fun TeamsRoute(coachId: String, teamId: String, repository: TeamRepository, onBack: () -> Unit) {
    val factory = remember(repository, teamId) {
        viewModelFactory {
            initializer {
                TeamsViewModel(
                    repository = repository,
                    teamId = teamId
                )
            }
        }
    }
    val viewModel: TeamsViewModel = viewModel(key = "team_members_${coachId}_$teamId", factory = factory)
    LifecycleStartEffect(viewModel) {
        viewModel.refresh()
        onStopOrDispose { viewModel.onScreenClosed() }
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TeamsScreen(state = state, onBack = onBack,
        onReload = viewModel::refresh)
}
