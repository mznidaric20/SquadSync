package hr.foi.squadsync.csv

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvParserTest {

    @Test
    fun validCsvShouldReturnTwoAthletes() {
        val csv = """
            ime,prezime,email
            Luka,Horvat,lhorvat@gmail.com
            Ante,Juric,antejuric@hotmail.com
        """.trimIndent()

        val result = CsvParser.parse(csv)

        assertEquals(2, result.validRows.size)
        assertEquals(0, result.invalidRows.size)
    }

    @Test
    fun csvWithoutFirstNameShouldBeInvalid() {
        val csv = """
        ime,prezime,email
        ,Novak,pnovak@gmail.com
    """.trimIndent()

        val result = CsvParser.parse(csv)

        assertEquals(0, result.validRows.size)
        assertEquals(1, result.invalidRows.size)
    }
    @Test
    fun csvWithoutLastNameShouldBeInvalid() {
        val csv = """
        ime,prezime,email
        Luka,,lhorvat@gmail.com
    """.trimIndent()

        val result = CsvParser.parse(csv)

        assertEquals(0, result.validRows.size)
        assertEquals(1, result.invalidRows.size)
    }

    @Test
    fun csvWithoutEmailShouldBeInvalid() {
        val csv = """
        ime,prezime,email
        Ivan,Kovacevic,
    """.trimIndent()

        val result = CsvParser.parse(csv)

        assertEquals(0, result.validRows.size)
        assertEquals(1, result.invalidRows.size)
    }


    @Test
    fun csvWithInvalidEmailShouldBeInvalid() {
        val csv = """
        ime,prezime,email
        Marko,Babic,mbabic3student.foi.hr
    """.trimIndent()

        val result = CsvParser.parse(csv)

        assertEquals(0, result.validRows.size)
        assertEquals(1, result.invalidRows.size)
    }

    @Test
    fun csvWithExtraColumnShouldBeInvalid() {
        val csv = """
        ime,prezime,email
        Luka,Horvat,lhorvat@gmail.com,visak
    """.trimIndent()

        val result = CsvParser.parse(csv)

        assertEquals(0, result.validRows.size)
        assertEquals(1, result.invalidRows.size)
    }

    @Test
    fun csvWithDuplicateEmailShouldBeInvalid() {
        val csv = """
        ime,prezime,email
        Luka,Horvat,lhorvat@gmail.com
        Luka,Horvat,lhorvat@gmail.com
    """.trimIndent()

        val result = CsvParser.parse(csv)

        assertEquals(1, result.validRows.size)
        assertEquals(1, result.invalidRows.size)
    }


}
