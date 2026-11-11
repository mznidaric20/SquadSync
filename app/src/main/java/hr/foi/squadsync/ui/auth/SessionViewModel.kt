package hr.foi.squadsync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.SessionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiState(val isBusy: Boolean = false, val error: AuthError? = null)

class SessionViewModel(
    private val repository: AuthRepository,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {
    val sessionState = repository.sessionState
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        retryRestore()
        viewModelScope.launch {
            sessionState.collectLatest { state ->
                if (state is SessionState.Authenticated) {
                    delay((state.session.expiresAtEpochMillis - now()).coerceAtLeast(0))
                    expireSession()
                }
            }
        }
    }

    fun retryRestore() = perform { repository.restoreSession() }
    fun refresh() = perform { repository.refreshSession() }
    fun signOut() = perform { repository.signOut() }

    private fun expireSession() {
        // Do not skip expiry while a manual request is busy, or cancel it when session state changes.
        viewModelScope.launch {
            try {
                repository.refreshSession()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.toAuthError()) }
            }
        }
    }

    private fun perform(action: suspend () -> Unit) {
        if (_uiState.value.isBusy) return
        _uiState.value = SessionUiState(isBusy = true)
        viewModelScope.launch {
            try {
                action()
                _uiState.value = SessionUiState()
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(isBusy = false) }
                throw exception
            } catch (exception: Exception) {
                _uiState.value = SessionUiState(error = exception.toAuthError())
            }
        }
    }
}
