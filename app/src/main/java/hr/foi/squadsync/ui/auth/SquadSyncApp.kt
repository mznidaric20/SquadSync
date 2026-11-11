package hr.foi.squadsync.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.SessionState

private enum class AuthDestination { LOGIN, REGISTRATION }

@Composable
fun SquadSyncApp(repository: AuthRepository) {
    val factory = remember(repository) { authViewModelFactory(repository) }
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    val registrationViewModel: RegistrationViewModel = viewModel(factory = factory)
    val session by sessionViewModel.sessionState.collectAsStateWithLifecycle()
    val sessionUi by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val login by loginViewModel.uiState.collectAsStateWithLifecycle()
    val registration by registrationViewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(AuthDestination.LOGIN) }

    LaunchedEffect(session is SessionState.Authenticated) {
        if (session is SessionState.Authenticated) {
            loginViewModel.clear()
            registrationViewModel.clear()
            destination = AuthDestination.LOGIN
        }
    }
    BackHandler(enabled = session == SessionState.SignedOut && destination == AuthDestination.REGISTRATION) {
        if (!registration.isSubmitting) {
            registrationViewModel.clear()
            destination = AuthDestination.LOGIN
        }
    }
    when (val current = session) {
        SessionState.Loading -> SessionLoadingScreen()
        is SessionState.RestoreFailed -> SessionRecoveryScreen(current.error, sessionUi,
            sessionViewModel::retryRestore, sessionViewModel::signOut)
        is SessionState.Authenticated -> SessionScreen(current.session.user, sessionUi,
            sessionViewModel::refresh, sessionViewModel::signOut)
        SessionState.SignedOut -> when (destination) {
            AuthDestination.LOGIN -> LoginScreen(login, loginViewModel::onEmailChange,
                loginViewModel::onPasswordChange, loginViewModel::submit, onRegistrationClick = {
                    loginViewModel.clear()
                    destination = AuthDestination.REGISTRATION
                })
            AuthDestination.REGISTRATION -> RegistrationScreen(registration,
                registrationViewModel::onFirstNameChange, registrationViewModel::onLastNameChange,
                registrationViewModel::onEmailChange, registrationViewModel::onPasswordChange,
                registrationViewModel::onConfirmPasswordChange, registrationViewModel::onRoleChange,
                registrationViewModel::submit, onLoginClick = {
                    registrationViewModel.clear()
                    destination = AuthDestination.LOGIN
                })
        }
    }
}
