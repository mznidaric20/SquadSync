package hr.foi.squadsync.data.teams.local

import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRoster

internal object DemoTeamCatalog {
    fun create(coachId: String): List<TeamRoster> {
        val seniors = listOf(
            TeamMember("demo-member-1", "Marko", "Horvat", "marko.horvat@primjer.com"),
            TeamMember("demo-member-2", "Ivan", "Marić", "ivan.maric@primjer.com"),
            TeamMember("demo-member-3", "Kristijan", "Krajnović", "kristijan.krajnovic@primjer.com"),
            TeamMember("demo-member-4", "Petar", "Perić", "petar.peric@primjer.com")
        )
        return listOf(
            TeamRoster(Team("demo-seniori", "Seniori", "Nogomet", coachId, seniors.size), seniors),
            TeamRoster(Team("demo-juniori", "Juniori", "Nogomet", coachId, 0), emptyList())
        )
    }
}
