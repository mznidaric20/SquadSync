package hr.foi.squadsync.ui.teams.demo

import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRoster

/** Presentation fixtures for the screen subtask; no team fetching or permissions are implemented. */
internal object DemoTeamRosters {
    val teams = listOf(
        TeamRoster(
            id = "demo-seniori",
            name = "Seniori",
            sport = "Nogomet",
            members = listOf(
                TeamMember("demo-member-1", "Marko", "Horvat", "marko.horvat@primjer.com"),
                TeamMember("demo-member-2", "Ivan", "Marić", "ivan.maric@primjer.com"),
                TeamMember("demo-member-3", "Kristijan", "Krajnović", "kristijan.krajnovic@primjer.com"),
                TeamMember("demo-member-4", "Petar", "Perić", "petar.peric@primjer.com")
            )
        ),
        TeamRoster(id = "demo-juniori", name = "Juniori", sport = "Nogomet", members = emptyList())
    )
}
