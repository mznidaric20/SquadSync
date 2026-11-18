package hr.foi.squadsync.domain.teams

data class TeamMember(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val initials: String get() = firstName.take(1) + lastName.take(1)
}

data class TeamRoster(
    val id: String,
    val name: String,
    val sport: String,
    val members: List<TeamMember>
)
