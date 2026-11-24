package hr.foi.squadsync.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.domain.teams.TeamJoinError
import hr.foi.squadsync.domain.teams.TeamJoinException
import hr.foi.squadsync.domain.teams.TeamJoinRepository
import hr.foi.squadsync.domain.validation.AccessCodeValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class JoinTeamUiState(
    val accessCode: String = "",
    val codeError: String? = null,
    val error: TeamJoinError? = null,
    val isSubmitting: Boolean = false,
    val joinedTeamName: String? = null
)

class JoinTeamViewModel(private val repository: TeamJoinRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(JoinTeamUiState())
    val uiState = _uiState.asStateFlow()

    // Zahtjev koji je u tijeku, da ga clear() moze otkazati.
    private var joinJob: Job? = null

    fun onAccessCodeChange(value: String) {
        // Dok zahtjev traje, kod se ne smije mijenjati.
        if (_uiState.value.isSubmitting) return
        _uiState.update {
            it.copy(accessCode = value, codeError = null, error = null, joinedTeamName = null)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        // Krivi kod se odbija odmah, bez slanja zahtjeva bazi.
        val codeError = AccessCodeValidator.validate(state.accessCode)
        if (codeError != null) {
            _uiState.update { it.copy(codeError = codeError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null, joinedTeamName = null) }
        joinJob = viewModelScope.launch {
            try {
                val teamName = repository.joinTeam(state.accessCode)
                // Ako je clear() otkazao zahtjev, rezultat pripada prethodnom korisniku.
                if (!isActive) return@launch
                _uiState.update { it.copy(isSubmitting = false, accessCode = "", joinedTeamName = teamName) }
            } catch (exception: CancellationException) {
                // Stanje je vec ocistio clear(), pa ga ovdje ne diramo.
                throw exception
            } catch (exception: TeamJoinException) {
                if (!isActive) return@launch
                _uiState.update { it.copy(isSubmitting = false, error = exception.error) }
            }
        }
    }

    fun clear() {
        joinJob?.cancel()
        joinJob = null
        _uiState.value = JoinTeamUiState()
    }
}
