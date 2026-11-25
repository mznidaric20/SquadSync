package hr.foi.squadsync.csv

data class CsvResult(
    val validRows: List<String>,
    val invalidRows: List<String>
)

object CsvParser {

    fun parse(csv: String): CsvResult {
        return CsvResult(
            validRows = emptyList(),
            invalidRows = emptyList()
        )
    }
}