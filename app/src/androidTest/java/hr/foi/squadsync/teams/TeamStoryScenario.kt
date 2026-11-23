package hr.foi.squadsync.teams

import hr.foi.squadsync.data.teams.DefaultTeamRepository
import hr.foi.squadsync.data.teams.TeamDataSource
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserProfile
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRoster
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

/** Isolated test identities and data: these tests never sign in to Supabase. */
internal class TeamStoryScenario {
    val coach = UserProfile("story-coach-a", "Marko", "Trener", "coach-a@example.test", UserRole.COACH)
    val coachWithoutTeams = coach.copy(id = "story-coach-c", email = "coach-c@example.test")
    val athleteUser = coach.copy(id = "story-athlete", email = "athlete@example.test", role = UserRole.ATHLETE)
    val first = Team("story-seniors", "Seniori", "Nogomet", coach.id, 2)
    val second = Team("story-reserves", "Druga momčad", "Nogomet", coach.id, 1)
    val empty = Team("story-juniors", "Juniori", "Nogomet", coach.id, 0)
    val foreign = Team("story-foreign", "Tuđa momčad", "Nogomet", "story-coach-b", 1)
    val ana = TeamMember("story-ana", "Ana", "Testić", "ana.testic@example.test")
    val ivan = TeamMember("story-ivan", "Ivan", "Testić", "ivan.testic@example.test")
    val petar = TeamMember("story-petar", "Petar", "Primjer", "petar.primjer@example.test")
    val privateMember = TeamMember("story-private", "Privatni", "Sportaš", "private@example.test")
    val auth = StoryAuthRepository(coach)
    val source = StoryTeamDataSource(listOf(
        TeamRoster(first, listOf(ana, ivan)), TeamRoster(second, listOf(petar)),
        TeamRoster(empty, emptyList()), TeamRoster(foreign, listOf(privateMember))
    ))
    val repository = DefaultTeamRepository(auth, source)
}

internal class StoryAuthRepository(user: UserProfile) : AuthRepository {
    override val sessionState = MutableStateFlow<SessionState>(SessionState.SignedOut)

    init { signInAs(user) }

    fun signInAs(user: UserProfile) {
        sessionState.value = SessionState.Authenticated(AuthSession(
            user, "isolated-test-token-${user.id}", System.currentTimeMillis() + 3_600_000L
        ))
    }

    override suspend fun restoreSession() = Unit
    override suspend fun refreshSession() = Unit
    override suspend fun signOut() { sessionState.value = SessionState.SignedOut }
    override suspend fun signIn(email: String, password: String) = error("Real sign-in is disabled in isolated tests")
    override suspend fun register(request: RegistrationRequest) = error("Real registration is disabled in isolated tests")
}

internal class StoryTeamDataSource(private val rosters: List<TeamRoster>) : TeamDataSource {
    var listCalls = 0
    var memberCalls = 0
    var failNextList = false
    var listGate: CompletableDeferred<Unit>? = null
    var memberGate: CompletableDeferred<Unit>? = null
    var denyFirstRoster = false

    override suspend fun fetchTeams(session: AuthSession): List<Team> {
        listCalls++
        listGate?.await()
        if (failNextList) {
            failNextList = false
            throw IOException("Isolated test transport failure")
        }
        // Include a foreign team deliberately: the real repository must filter it out.
        return rosters.map { it.team }
    }

    override suspend fun fetchTeam(session: AuthSession, teamId: String): Team? =
        rosters.firstOrNull { it.team.id == teamId }?.team?.let {
            if (denyFirstRoster && teamId == rosters.first().team.id) it.copy(coachId = "story-coach-b") else it
        }

    override suspend fun fetchMembers(session: AuthSession, teamId: String): List<TeamMember> {
        memberCalls++
        memberGate?.await()
        return rosters.first { it.team.id == teamId }.members
    }
}
