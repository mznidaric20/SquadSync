package hr.foi.squadsync.ui.auth

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.squadsync.domain.auth.AuthRepository

fun authViewModelFactory(repository: AuthRepository) = viewModelFactory {
    initializer { LoginViewModel(repository) }
    initializer { RegistrationViewModel(repository) }
    initializer { SessionViewModel(repository) }
}
