package hr.foi.squadsync.ui.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import hr.foi.squadsync.domain.teams.TeamRoster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TeamsUiState(
    val teams: List<TeamRoster> = emptyList(),
    val selectedTeamId: String? = null,
    val isDemo: Boolean = false
) {
    val selectedTeam: TeamRoster? get() = teams.firstOrNull { it.id == selectedTeamId }
}

class TeamsViewModel(
    initialTeams: List<TeamRoster>,
    private val savedStateHandle: SavedStateHandle,
    isDemo: Boolean = false
) : ViewModel() {
    private val teams = initialTeams.map { it.copy(members = it.members.toList()) }
    private val initialSelection = savedStateHandle.get<String>(SELECTED_TEAM)
        ?.takeIf { id -> teams.any { it.id == id } } ?: teams.firstOrNull()?.id
    private val _uiState = MutableStateFlow(TeamsUiState(teams, initialSelection, isDemo))
    val uiState = _uiState.asStateFlow()

    fun selectTeam(teamId: String) {
        if (teams.none { it.id == teamId }) return
        savedStateHandle[SELECTED_TEAM] = teamId
        _uiState.update { it.copy(selectedTeamId = teamId) }
    }

    private companion object {
        const val SELECTED_TEAM = "selected_team_id"
    }
}
