package hr.foi.squadsync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.validation.LoginErrors
import hr.foi.squadsync.domain.validation.LoginValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errors: LoginErrors = LoginErrors(),
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update {
        if (it.isSubmitting) it else it.copy(email = value, errors = it.errors.copy(email = null), error = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        if (it.isSubmitting) it else it.copy(password = value, errors = it.errors.copy(lozinka = null), error = null)
    }

    fun submit() {
        val form = _uiState.value
        if (form.isSubmitting) return
        val errors = LoginValidator.validate(form.email, form.password)
        _uiState.update { it.copy(errors = errors, error = null, isSubmitting = !errors.hasErrors) }
        if (errors.hasErrors) return
        viewModelScope.launch {
            try {
                repository.signIn(form.email, form.password)
                _uiState.update { it.copy(password = "", isSubmitting = false) }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(isSubmitting = false) }
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = exception.toAuthError()) }
            }
        }
    }

    fun clear() { _uiState.value = LoginUiState() }
}

internal fun Exception.toAuthError(): AuthError =
    (this as? AuthException)?.error ?: AuthError.STORAGE_UNAVAILABLE
