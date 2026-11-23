package hr.foi.squadsync.domain.teams

// Podaci koje trener upisuje prilikom kreiranja momčadi.
data class CreateTeamRequest(
    val name: String,
    val description: String?
)

// Podaci o momčadi nakon uspješnog kreiranja.
data class CreatedTeam(
    val id: String,
    val name: String,
    val description: String?,
    val coachId: String,
    val accessCode: String
)
