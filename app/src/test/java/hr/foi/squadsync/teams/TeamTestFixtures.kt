package hr.foi.squadsync.teams

import hr.foi.squadsync.data.local.LocalAccount
import hr.foi.squadsync.data.local.LocalAccountStore
import hr.foi.squadsync.data.teams.TeamDataSource
import hr.foi.squadsync.data.teams.local.TeamStore
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserProfile
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.domain.teams.TeamException
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRepository
import hr.foi.squadsync.domain.teams.TeamRoster
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal val coach = UserProfile("coach-a", "Marko", "Kovač", "trener@squadsync.hr", UserRole.COACH)
internal val coachSession = AuthSession(coach, "test-coach-token", 10_000L)
internal val ownTeam = Team("team-a", "Seniori", "Nogomet", coach.id, 1)
internal val emptyTeam = Team("team-empty", "Juniori", "Nogomet", coach.id, 0)
internal val foreignTeam = Team("team-foreign", "Kadeti", "Nogomet", "coach-b", 1)
internal val athlete = TeamMember("member-a", "Ivan", "Marić", "ivan@primjer.com")

internal class TestAuthRepository : AuthRepository {
    override val sessionState = MutableStateFlow<SessionState>(SessionState.Authenticated(coachSession))
    var refreshFailure: Exception? = null
    var onRefresh: () -> Unit = {}
    var refreshCalls = 0
    override suspend fun refreshSession() {
        refreshCalls++
        refreshFailure?.let { throw it }
        onRefresh()
    }
    override suspend fun restoreSession() = Unit
    override suspend fun signIn(email: String, password: String) = Unit
    override suspend fun register(request: RegistrationRequest) = Unit
    override suspend fun signOut() { sessionState.value = SessionState.SignedOut }
}

internal class TestTeamDataSource : TeamDataSource {
    var teams = listOf(ownTeam, foreignTeam)
    var memberGate: CompletableDeferred<Unit>? = null
    var memberFailure: Exception? = null
    var returnedTeam: Team? = null
    val memberRequests = mutableListOf<String>()
    val membersByTeam = mutableMapOf(ownTeam.id to listOf(athlete))
    var listCalls = 0
    var teamCalls = 0
    var memberCalls = 0
    override suspend fun fetchTeams(session: AuthSession): List<Team> { listCalls++; return teams }
    override suspend fun fetchTeam(session: AuthSession, teamId: String): Team? {
        teamCalls++
        return returnedTeam ?: teams.firstOrNull { it.id == teamId }
    }
    override suspend fun fetchMembers(session: AuthSession, teamId: String): List<TeamMember> {
        memberCalls++
        memberRequests.add(teamId)
        memberGate?.await()
        memberFailure?.let { throw it }
        return membersByTeam[teamId].orEmpty()
    }
}

internal class MemoryTeamStore(var rosters: List<TeamRoster>? = null) : TeamStore {
    var writes = 0
    override suspend fun read(): List<TeamRoster>? = rosters?.toList()
    override suspend fun write(rosters: List<TeamRoster>) { writes++; this.rosters = rosters.toList() }
}

internal class TeamAccountStore : LocalAccountStore {
    var accounts = listOf(LocalAccount(coach, "unused-test-salt", "unused-test-hash"))
    override suspend fun read(): List<LocalAccount> = accounts.toList()
    override suspend fun write(accounts: List<LocalAccount>) { this.accounts = accounts.toList() }
}

internal class TestTeamRepository : TeamRepository {
    var teams = listOf(ownTeam, emptyTeam)
    var listFailure: Exception? = null
    var rosterError: TeamError? = null
    val gates = mutableMapOf<String, CompletableDeferred<Unit>>()
    val rosterRequests = mutableListOf<String>()
    override suspend fun getMyTeams(): List<Team> {
        listFailure?.let { throw it }
        return teams
    }
    override suspend fun getRoster(teamId: String): TeamRoster {
        rosterRequests.add(teamId)
        gates[teamId]?.await()
        rosterError?.let { throw TeamException(it) }
        val team = teams.firstOrNull { it.id == teamId } ?: throw TeamException(TeamError.NOT_FOUND)
        return TeamRoster(team, if (teamId == ownTeam.id) listOf(athlete) else emptyList())
    }
}

internal suspend fun teamFailure(action: suspend () -> Any?): TeamError {
    try {
        action()
        throw AssertionError("Expected team access to fail")
    } catch (exception: TeamException) {
        return exception.error
    }
}
