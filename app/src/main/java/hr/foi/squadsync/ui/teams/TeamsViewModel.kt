package hr.foi.squadsync.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.domain.teams.TeamException
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamsUiState(
    val teams: List<Team> = emptyList(),
    val selectedTeamId: String? = null,
    val members: List<TeamMember> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val error: TeamError? = null,
    val isDemo: Boolean = false
) {
    val selectedTeam: Team? get() = teams.firstOrNull { it.id == selectedTeamId }
}

class TeamsViewModel(
    private val repository: TeamRepository,
    private val teamId: String,
    isDemo: Boolean = false
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamsUiState(isLoading = true, isDemo = isDemo))
    val uiState = _uiState.asStateFlow()
    private var request: Job? = null

    fun refresh() {
        request?.cancel()
        _uiState.value = TeamsUiState(isLoading = true, isDemo = _uiState.value.isDemo)
        request = viewModelScope.launch {
            try {
                val roster = repository.getRoster(teamId)
                currentCoroutineContext().ensureActive()
                _uiState.update { it.copy(
                    teams = listOf(roster.team), selectedTeamId = roster.team.id,
                    members = roster.members.toList(), isLoading = false
                ) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleError(exception)
            }
        }
    }

    fun onScreenClosed() {
        request?.cancel()
        _uiState.value = TeamsUiState(isLoading = true, isDemo = _uiState.value.isDemo)
    }

    private suspend fun handleError(exception: Exception) {
        currentCoroutineContext().ensureActive()
        val error = (exception as? TeamException)?.error ?: TeamError.DATA_UNAVAILABLE
        val denied = error == TeamError.SESSION_REQUIRED || error == TeamError.ACCESS_DENIED
        _uiState.update { it.copy(
            teams = if (denied) emptyList() else it.teams,
            selectedTeamId = if (denied) null else it.selectedTeamId,
            members = emptyList(), isLoading = false, isLoadingMembers = false, error = error
        ) }
    }
}
