package hr.foi.squadsync.csv

data class CsvResult(
    val validRows: List<String>,
    val invalidRows: List<String>
)

object CsvParser {

    fun parse(csv: String): CsvResult {
        val rows = csv.lines()
            .drop(1)
            .filter { it.isNotBlank() }

        return CsvResult(
            validRows = rows,
            invalidRows = emptyList()
        )
    }
}
