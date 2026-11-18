package hr.foi.squadsync.ui.teams

import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
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
                val teams = repository.getMyTeams()
                currentCoroutineContext().ensureActive()
                val savedId = savedStateHandle.get<String>(SELECTED_TEAM)
                val selected = teams.firstOrNull { it.id == savedId } ?: teams.firstOrNull()
                _uiState.update { it.copy(teams = teams, selectedTeamId = selected?.id,
                    isLoading = false, isLoadingMembers = selected != null) }
                if (selected != null) loadRoster(selected.id)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleError(exception)
            }
        }
    }

    fun selectTeam(teamId: String) {
        val state = _uiState.value
        if (state.isLoading || state.teams.none { it.id == teamId }) return
        if (state.selectedTeamId == teamId && state.error == null) return
        request?.cancel()
        _uiState.update { it.copy(selectedTeamId = teamId, members = emptyList(),
            isLoadingMembers = true, error = null) }
        request = viewModelScope.launch {
            try {
                loadRoster(teamId)
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

    private suspend fun loadRoster(teamId: String) {
        val roster = repository.getRoster(teamId)
        currentCoroutineContext().ensureActive()
        savedStateHandle[SELECTED_TEAM] = teamId
        _uiState.update { state -> state.copy(
            teams = state.teams.map { if (it.id == teamId) roster.team else it },
            members = roster.members.toList(), isLoadingMembers = false, error = null
        ) }
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

    private companion object {
        const val SELECTED_TEAM = "selected_team_id"
    }
}
