package hr.foi.squadsync.domain.teams

// Definira kreiranje momcadi za trenutno prijavljenog trenera.
interface CreateTeamRepository {
    suspend fun createTeam(
        request: CreateTeamRequest
    ): CreatedTeam
}

// Moguce greske prilikom kreiranja momcadi.
enum class CreateTeamError {
    NAME_REQUIRED,
    SESSION_REQUIRED,
    ACCESS_DENIED,
    DATA_UNAVAILABLE
}

class CreateTeamException(
    val error: CreateTeamError,
    cause: Throwable? = null
) : Exception(error.name, cause)

// Provjerava naziv i uklanja nepotrebne razmake.
fun CreateTeamRequest.validate(): CreateTeamRequest {
    val cleanName = name.trim()

    if (cleanName.isBlank()) {
        throw CreateTeamException(
            CreateTeamError.NAME_REQUIRED
        )
    }

    return copy(
        name = cleanName,
        description = description
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    )
}
