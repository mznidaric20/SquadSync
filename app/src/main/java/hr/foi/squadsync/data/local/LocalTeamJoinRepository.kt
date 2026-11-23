package hr.foi.squadsync.data.local

import hr.foi.squadsync.domain.teams.TeamJoinError
import hr.foi.squadsync.domain.teams.TeamJoinException
import hr.foi.squadsync.domain.teams.TeamJoinRepository

// Koristi se kad Supabase nije postavljen. Momcadi postoje samo u Supabaseu,
// pa se sportas bez njega ne moze pridruziti momcadi.
class LocalTeamJoinRepository : TeamJoinRepository {

    override suspend fun joinTeam(accessCode: String): String {
        throw TeamJoinException(TeamJoinError.SERVICE_UNAVAILABLE)
    }
}
