package hr.foi.squadsync.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.ui.teams.CreateTeamScreen
import hr.foi.squadsync.domain.teams.CreateTeamRepository
import hr.foi.squadsync.domain.teams.CreateTeamError
import hr.foi.squadsync.domain.teams.CreateTeamException
import kotlinx.coroutines.launch



private enum class AuthDestination { LOGIN, REGISTRATION }

@Composable
fun SquadSyncApp(
    repository: AuthRepository,
    createTeamRepository: CreateTeamRepository? = null
) {
    val factory = remember(repository) { authViewModelFactory(repository) }
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    val registrationViewModel: RegistrationViewModel = viewModel(factory = factory)
    val session by sessionViewModel.sessionState.collectAsStateWithLifecycle()
    val sessionUi by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val login by loginViewModel.uiState.collectAsStateWithLifecycle()
    val registration by registrationViewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(AuthDestination.LOGIN) }
    var showCreateTeam by rememberSaveable { mutableStateOf(false) }
    var formMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var formSuccess by rememberSaveable { mutableStateOf<String?>(null) }
    var isSavingTeam by remember { mutableStateOf(false) }
    val teamScope = rememberCoroutineScope()



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

    BackHandler(
        enabled = session is SessionState.Authenticated && showCreateTeam
    ) {
        showCreateTeam = false
        formMessage = null
    }

    when (val current = session) {
        SessionState.Loading -> SessionLoadingScreen()
        is SessionState.RestoreFailed -> SessionRecoveryScreen(current.error, sessionUi,
            sessionViewModel::retryRestore, sessionViewModel::signOut)

        is SessionState.Authenticated -> {
            if (current.session.user.role == UserRole.COACH && showCreateTeam) {

                CreateTeamScreen(
                    onBack = {
                        showCreateTeam = false
                        formMessage = null
                        formSuccess = null
                    },
                    onCreate = { request ->
                        val teamRepository = createTeamRepository

                        if (teamRepository == null) {
                            formMessage = "Supabase nije konfiguriran."
                        } else if (!isSavingTeam && formSuccess == null) {
                            isSavingTeam = true
                            formMessage = null

                            teamScope.launch {
                                try {
                                    val team = teamRepository.createTeam(request)

                                    formSuccess =
                                        "Momčad '${team.name}' uspješno je kreirana. " +
                                                "Pristupni kod: ${team.accessCode}"

                                } catch (e: CreateTeamException) {
                                    formMessage = when (e.error) {
                                        CreateTeamError.NAME_REQUIRED ->
                                            "Naziv momčadi je obvezan."

                                        CreateTeamError.SESSION_REQUIRED ->
                                            "Moraš biti prijavljen."

                                        CreateTeamError.ACCESS_DENIED ->
                                            "Samo trener može kreirati momčad."

                                        CreateTeamError.DATA_UNAVAILABLE ->
                                            "Spremanje nije uspjelo. Provjeri vezu s bazom."
                                    }
                                } finally {
                                    isSavingTeam = false
                                }
                            }
                        }
                    },
                    isSaving = isSavingTeam,
                    saveError = formMessage,
                    saveSuccess = formSuccess
                )
            } else {
                SessionScreen(
                    user = current.session.user,
                    state = sessionUi,
                    onRefresh = sessionViewModel::refresh,
                    onSignOut = {
                        showCreateTeam = false
                        formMessage = null
                        sessionViewModel.signOut()
                    },
                    onCreateTeamClick = {
                        formMessage = null
                        formSuccess = null
                        showCreateTeam = true
                    }
                )
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
