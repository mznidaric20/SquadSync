package hr.foi.squadsync.data.teams

import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.domain.teams.TeamException
import hr.foi.squadsync.domain.teams.TeamRepository
import hr.foi.squadsync.domain.teams.TeamRoster
import kotlinx.coroutines.CancellationException

class DefaultTeamRepository(
    private val authRepository: AuthRepository,
    private val dataSource: TeamDataSource,
    private val now: () -> Long = System::currentTimeMillis
) : TeamRepository {
    override suspend fun getMyTeams(): List<Team> = authorized { session ->
        dataSource.fetchTeams(session).filter { it.coachId == session.user.id }
    }

    override suspend fun getRoster(teamId: String): TeamRoster = authorized { session ->
        val team = dataSource.fetchTeam(session, teamId) ?: throw TeamException(TeamError.NOT_FOUND)
        ensureSession(session)
        if (team.id != teamId || team.coachId != session.user.id) {
            throw TeamException(TeamError.ACCESS_DENIED)
        }
        // Ownership is checked before requesting any member data.
        val members = dataSource.fetchMembers(session, teamId).toList()
        TeamRoster(team.copy(memberCount = members.size), members)
    }

    private suspend fun <T> authorized(action: suspend (AuthSession) -> T): T {
        try {
            val initial = requireCoach()
            authRepository.refreshSession()
            val session = requireCoach()
            if (session.user.id != initial.user.id) throw TeamException(TeamError.SESSION_REQUIRED)
            return action(session).also { ensureSession(session) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: TeamException) {
            throw exception
        } catch (exception: AuthException) {
            throw TeamException(
                if (exception.error == AuthError.SESSION_EXPIRED) TeamError.SESSION_REQUIRED else TeamError.DATA_UNAVAILABLE,
                exception
            )
        } catch (exception: Exception) {
            throw TeamException(TeamError.DATA_UNAVAILABLE, exception)
        }
    }

    private fun requireCoach(): AuthSession {
        val session = (authRepository.sessionState.value as? SessionState.Authenticated)?.session
            ?: throw TeamException(TeamError.SESSION_REQUIRED)
        if (session.accessToken.isBlank() || session.isExpired(now())) {
            throw TeamException(TeamError.SESSION_REQUIRED)
        }
        if (session.user.role != UserRole.COACH) throw TeamException(TeamError.ACCESS_DENIED)
        return session
    }

    private fun ensureSession(expected: AuthSession) {
        val current = requireCoach()
        if (current.user.id != expected.user.id || current.accessToken != expected.accessToken) {
            throw TeamException(TeamError.SESSION_REQUIRED)
        }
    }
}
