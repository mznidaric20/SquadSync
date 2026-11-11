package hr.foi.squadsync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.validation.RegistrationErrors
import hr.foi.squadsync.domain.validation.RegistrationValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: UserRole? = null,
    val errors: RegistrationErrors = RegistrationErrors(),
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

class RegistrationViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    private fun edit(transform: (RegistrationUiState) -> RegistrationUiState) = _uiState.update {
        if (it.isSubmitting) it else transform(it).copy(error = null)
    }

    fun onFirstNameChange(value: String) = edit { it.copy(firstName = value, errors = it.errors.copy(ime = null)) }
    fun onLastNameChange(value: String) = edit { it.copy(lastName = value, errors = it.errors.copy(prezime = null)) }
    fun onEmailChange(value: String) = edit { it.copy(email = value, errors = it.errors.copy(email = null)) }
    fun onPasswordChange(value: String) = edit {
        it.copy(password = value, errors = it.errors.copy(lozinka = null, potvrdaLozinke = null))
    }
    fun onConfirmPasswordChange(value: String) = edit {
        it.copy(confirmPassword = value, errors = it.errors.copy(potvrdaLozinke = null))
    }
    fun onRoleChange(value: UserRole) = edit { it.copy(role = value, errors = it.errors.copy(uloga = null)) }

    fun submit() {
        val form = _uiState.value
        if (form.isSubmitting) return
        val errors = RegistrationValidator.validate(form.firstName, form.lastName, form.email,
            form.password, form.confirmPassword, when (form.role) {
                UserRole.ATHLETE -> "Sportaš"
                UserRole.COACH -> "Trener"
                null -> ""
            })
        _uiState.update { it.copy(errors = errors, error = null, isSubmitting = !errors.hasErrors) }
        if (errors.hasErrors) return
        viewModelScope.launch {
            try {
                repository.register(RegistrationRequest(form.firstName, form.lastName, form.email,
                    form.password, requireNotNull(form.role)))
                _uiState.update { it.copy(password = "", confirmPassword = "", isSubmitting = false) }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(isSubmitting = false) }
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = exception.toAuthError()) }
            }
        }
    }

    fun clear() { _uiState.value = RegistrationUiState() }
}
