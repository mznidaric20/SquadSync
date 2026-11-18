package hr.foi.squadsync.data.teams.local

import hr.foi.squadsync.domain.teams.TeamRoster

interface TeamStore {
    /** null means storage has not been initialized; an empty list is a saved empty catalog. */
    suspend fun read(): List<TeamRoster>?
    suspend fun write(rosters: List<TeamRoster>)
}
