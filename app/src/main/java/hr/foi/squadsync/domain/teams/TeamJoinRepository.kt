package hr.foi.squadsync.domain.teams

interface TeamJoinRepository {
    // Pridruzuje prijavljenog sportasa momcadi i vraca naziv momcadi.
    suspend fun joinTeam(accessCode: String): String
}

enum class TeamJoinError {
    NOT_ATHLETE, INVALID_CODE, ALREADY_MEMBER, SERVICE_UNAVAILABLE
}

class TeamJoinException(val error: TeamJoinError, cause: Throwable? = null) : Exception(error.name, cause)
