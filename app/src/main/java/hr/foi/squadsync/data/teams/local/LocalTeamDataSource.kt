package hr.foi.squadsync.data.teams.local

import hr.foi.squadsync.data.local.DemoAccounts
import hr.foi.squadsync.data.local.LocalAccountStore
import hr.foi.squadsync.data.teams.TeamDataSource
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.domain.teams.TeamException
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRoster
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalTeamDataSource(
    private val teamStore: TeamStore,
    private val accountStore: LocalAccountStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TeamDataSource {
    private val mutex = Mutex()

    override suspend fun fetchTeams(session: AuthSession): List<Team> = withContext(dispatcher) {
        mutex.withLock { readCatalog().map { it.team }.filter { it.coachId == session.user.id } }
    }

    override suspend fun fetchTeam(session: AuthSession, teamId: String): Team? = withContext(dispatcher) {
        mutex.withLock { readCatalog().firstOrNull { it.team.id == teamId }?.team }
    }

    override suspend fun fetchMembers(session: AuthSession, teamId: String): List<TeamMember> = withContext(dispatcher) {
        mutex.withLock {
            val roster = readCatalog().firstOrNull { it.team.id == teamId }
                ?: throw TeamException(TeamError.NOT_FOUND)
            if (roster.team.coachId != session.user.id || session.user.role != UserRole.COACH) {
                throw TeamException(TeamError.ACCESS_DENIED)
            }
            roster.members.toList()
        }
    }

    private suspend fun readCatalog(): List<TeamRoster> {
        teamStore.read()?.let { return it }
        val demoEmail = DemoAccounts.users.first { it.role == UserRole.COACH }.email
        val owner = accountStore.read().firstOrNull {
            it.user.role == UserRole.COACH && it.user.email.equals(demoEmail, ignoreCase = true)
        }?.user ?: return emptyList()
        // Seed only once, with the stored account's stable ID, never the requesting user's ID.
        return DemoTeamCatalog.create(owner.id).also { teamStore.write(it) }
    }
}
