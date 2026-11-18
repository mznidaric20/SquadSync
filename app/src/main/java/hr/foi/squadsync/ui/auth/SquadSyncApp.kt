package hr.foi.squadsync.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.ui.teams.TeamsRoute

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
    var showTeams by rememberSaveable { mutableStateOf(false) }
    var teamsOwnerId by rememberSaveable { mutableStateOf<String?>(null) }
    val authenticatedUserId = (session as? SessionState.Authenticated)?.session?.user?.id

    LaunchedEffect(authenticatedUserId, session == SessionState.SignedOut) {
        if (authenticatedUserId != null) {
            if (teamsOwnerId != authenticatedUserId) showTeams = false
            teamsOwnerId = authenticatedUserId
            loginViewModel.clear()
            registrationViewModel.clear()
            destination = AuthDestination.LOGIN
        } else if (session == SessionState.SignedOut) {
            showTeams = false
            teamsOwnerId = null
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
        is SessionState.Authenticated -> {
            val coach = current.session.user.role == UserRole.COACH
            if (coach && showTeams && teamsOwnerId == current.session.user.id) {
                BackHandler { showTeams = false }
                TeamsRoute(coachId = current.session.user.id, onBack = { showTeams = false })
            } else {
                SessionScreen(current.session.user, sessionUi,
                    sessionViewModel::refresh, sessionViewModel::signOut,
                    onTeamsClick = if (coach) ({ showTeams = true }) else null)
            }
        }
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
