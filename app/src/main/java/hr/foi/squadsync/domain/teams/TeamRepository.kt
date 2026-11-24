package hr.foi.squadsync.domain.teams

interface TeamRepository {
    suspend fun getMyTeams(): List<Team>
    suspend fun getRoster(teamId: String): TeamRoster
}

enum class TeamError { SESSION_REQUIRED, ACCESS_DENIED, NOT_FOUND, DATA_UNAVAILABLE }

class TeamException(val error: TeamError, cause: Throwable? = null) : Exception(error.name, cause)
