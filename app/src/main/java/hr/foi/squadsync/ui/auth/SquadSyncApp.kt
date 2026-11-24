package hr.foi.squadsync.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserProfile
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.TeamRepository
import hr.foi.squadsync.domain.teams.CreateTeamRepository
import hr.foi.squadsync.domain.teams.CreateTeamError
import hr.foi.squadsync.domain.teams.CreateTeamException
import hr.foi.squadsync.data.remote.SupabaseTeamListRepository
import hr.foi.squadsync.ui.teams.CreateTeamScreen
import hr.foi.squadsync.ui.teams.MyCreatedTeamsScreen
import hr.foi.squadsync.ui.teams.TeamsRoute
import hr.foi.squadsync.ui.access.RoleAwareScreen
import kotlinx.coroutines.launch

private enum class AuthDestination {
    LOGIN,
    REGISTRATION
}

private enum class TeamDestination {
    HOME,
    CREATE_TEAM,
    MY_TEAMS,
    MEMBERS
}

@Composable
fun SquadSyncApp(
    repository: AuthRepository,
    createTeamRepository: CreateTeamRepository? = null,
    teamListRepository: SupabaseTeamListRepository? = null,
    teamRepository: TeamRepository? = null
) {
    val factory = remember(repository) {
        authViewModelFactory(repository)
    }

    val sessionViewModel: SessionViewModel =
        viewModel(factory = factory)

    val loginViewModel: LoginViewModel =
        viewModel(factory = factory)

    val registrationViewModel: RegistrationViewModel =
        viewModel(factory = factory)

    val session by sessionViewModel.sessionState
        .collectAsStateWithLifecycle()

    val sessionUi by sessionViewModel.uiState
        .collectAsStateWithLifecycle()

    val login by loginViewModel.uiState
        .collectAsStateWithLifecycle()

    val registration by registrationViewModel.uiState
        .collectAsStateWithLifecycle()

    var destination by rememberSaveable {
        mutableStateOf(AuthDestination.LOGIN)
    }

    val authenticatedUserId =
        (session as? SessionState.Authenticated)?.session?.user?.id

    LaunchedEffect(authenticatedUserId) {
        if (authenticatedUserId != null) {
            loginViewModel.clear()
            registrationViewModel.clear()
            destination = AuthDestination.LOGIN
        }
    }

    BackHandler(
        enabled = session == SessionState.SignedOut &&
                destination == AuthDestination.REGISTRATION
    ) {
        if (!registration.isSubmitting) {
            registrationViewModel.clear()
            destination = AuthDestination.LOGIN
        }
    }

    when (val current = session) {

        SessionState.Loading -> SessionLoadingScreen()

        is SessionState.RestoreFailed -> SessionRecoveryScreen(
            current.error,
            sessionUi,
            sessionViewModel::retryRestore,
            sessionViewModel::signOut
        )

        is SessionState.Authenticated -> {
            val user = current.session.user

            key(user.id, user.role) {
                AuthenticatedContent(
                    user = user,
                    sessionUi = sessionUi,
                    onRefresh = sessionViewModel::refresh,
                    onSignOut = sessionViewModel::signOut,
                    createTeamRepository = createTeamRepository,
                    teamListRepository = teamListRepository,
                    teamRepository = teamRepository
                )
            }
        }

        SessionState.SignedOut -> when (destination) {

            AuthDestination.LOGIN -> LoginScreen(
                login,
                loginViewModel::onEmailChange,
                loginViewModel::onPasswordChange,
                loginViewModel::submit,
                onRegistrationClick = {
                    loginViewModel.clear()
                    destination = AuthDestination.REGISTRATION
                }
            )

            AuthDestination.REGISTRATION -> RegistrationScreen(
                registration,
                registrationViewModel::onFirstNameChange,
                registrationViewModel::onLastNameChange,
                registrationViewModel::onEmailChange,
                registrationViewModel::onPasswordChange,
                registrationViewModel::onConfirmPasswordChange,
                registrationViewModel::onRoleChange,
                registrationViewModel::submit,
                onLoginClick = {
                    registrationViewModel.clear()
                    destination = AuthDestination.LOGIN
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedContent(
    user: UserProfile,
    sessionUi: SessionUiState,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    createTeamRepository: CreateTeamRepository?,
    teamListRepository: SupabaseTeamListRepository?,
    teamRepository: TeamRepository?
) {
    var teamDestination by rememberSaveable {
        mutableStateOf(TeamDestination.HOME)
    }

    var formMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var formSuccess by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var isSavingTeam by remember {
        mutableStateOf(false)
    }

    val teamScope = rememberCoroutineScope()
    val saveableStateHolder = rememberSaveableStateHolder()

    val coach = user.role == UserRole.COACH

    // Pregled clanova zasad koristi lokalne ogledne podatke.
    val localMembersAvailable =
        teamRepository != null &&
                teamListRepository == null &&
                createTeamRepository == null

    BackHandler(
        enabled = teamDestination != TeamDestination.HOME
    ) {
        if (!isSavingTeam) {
            teamDestination = TeamDestination.HOME
            formMessage = null
            formSuccess = null
        }
    }

    when {

        // Pregled kreiranih momcadi - SCRUM-17
        coach &&
                teamDestination == TeamDestination.MY_TEAMS &&
                teamListRepository != null -> {

            MyCreatedTeamsScreen(
                repository = teamListRepository,
                onBack = {
                    teamDestination = TeamDestination.HOME
                }
            )
        }

        // Kreiranje momcadi - SCRUM-17
        coach &&
                teamDestination == TeamDestination.CREATE_TEAM -> {

            CreateTeamScreen(
                onBack = {
                    if (!isSavingTeam) {
                        teamDestination = TeamDestination.HOME
                        formMessage = null
                        formSuccess = null
                    }
                },

                onCreate = { request ->

                    if (createTeamRepository == null) {

                        formMessage = "Supabase nije konfiguriran."

                    } else if (!isSavingTeam && formSuccess == null) {

                        isSavingTeam = true
                        formMessage = null

                        teamScope.launch {
                            try {
                                val team =
                                    createTeamRepository.createTeam(request)

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
        }

        // Pregled clanova momcadi - lokalni nacin rada
        coach &&
                teamDestination == TeamDestination.MEMBERS &&
                localMembersAvailable &&
                teamRepository != null -> {

            TeamsRoute(
                coachId = user.id,
                repository = teamRepository,
                onBack = {
                    teamDestination = TeamDestination.HOME
                }
            )
        }

        // Profil i kontrola pristupa - SCRUM-42
        else -> {

            saveableStateHolder.SaveableStateProvider(
                key = user.id
            ) {

                RoleAwareScreen(
                    user = user,
                    state = sessionUi,
                    onRefresh = onRefresh,

                    onSignOut = {
                        teamDestination = TeamDestination.HOME
                        formMessage = null
                        formSuccess = null
                        onSignOut()
                    },

                    onCreateTeamClick = {
                        if (coach) {
                            formMessage = null
                            formSuccess = null
                            teamDestination = TeamDestination.CREATE_TEAM
                        }
                    },

                    onMyTeamsClick = {
                        if (coach && teamListRepository != null) {
                            teamDestination = TeamDestination.MY_TEAMS
                        }
                    },

                    onTeamsClick = if (coach && localMembersAvailable) {
                        {
                            teamDestination = TeamDestination.MEMBERS
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}