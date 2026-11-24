package hr.foi.squadsync.data.teams

import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamMember

/** A backend adapter can use the supplied session token for authenticated HTTP requests. */
interface TeamDataSource {
    suspend fun fetchTeams(session: AuthSession): List<Team>
    suspend fun fetchTeam(session: AuthSession, teamId: String): Team?
    suspend fun fetchMembers(session: AuthSession, teamId: String): List<TeamMember>
}
