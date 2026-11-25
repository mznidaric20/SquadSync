package hr.foi.squadsync.export

import hr.foi.squadsync.domain.export.CsvExporter
import hr.foi.squadsync.domain.teams.TeamMember
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterTest {

    @Test
    fun givenAthletes_whenCsvGenerated_thenReturnsExpectedCsv() {
        val athletes = listOf(
            TeamMember("1", "Luka", "Horvat", "lhorvat@gmail.com"),
            TeamMember("2", "Ivan", "Kovacevic", "ivan.kovacevic@gmail.com"),
            TeamMember("3", "Marko", "Babic", "mbabic3@student.foi.hr"),
            TeamMember("4", "Ante", "Juric", "antejuric@hotmail.com"),
            TeamMember("5", "Petar", "Novak", "pnovak@gmail.com"),
            TeamMember("6", "Josip", "Maric", "josip.maric12@gmail.com")
        )
        val expectedCsv = "ime,prezime,email\n" +
            "Luka,Horvat,lhorvat@gmail.com\n" +
            "Ivan,Kovacevic,ivan.kovacevic@gmail.com\n" +
            "Marko,Babic,mbabic3@student.foi.hr\n" +
            "Ante,Juric,antejuric@hotmail.com\n" +
            "Petar,Novak,pnovak@gmail.com\n" +
            "Josip,Maric,josip.maric12@gmail.com\n"

        val csv = CsvExporter.generateCsv(athletes)

        assertEquals(expectedCsv, csv)
    }
}
