package hr.foi.squadsync.domain.export

import hr.foi.squadsync.domain.teams.TeamMember

object CsvExporter {

    fun generateCsv(athletes: List<TeamMember>): String {
        val csv = StringBuilder("ime,prezime,email\n")

        for (athlete in athletes) {
            csv.append("${athlete.firstName},${athlete.lastName},${athlete.email}\n")
        }

        return csv.toString()
    }
}
