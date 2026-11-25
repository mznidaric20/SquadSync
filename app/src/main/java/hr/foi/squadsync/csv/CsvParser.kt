package hr.foi.squadsync.csv

data class CsvResult(
    val validRows: List<String>,
    val invalidRows: List<String>
)

object CsvParser {

    private val emailRegex =
        Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun parse(csv: String): CsvResult {
        val rows = csv.lines()
            .drop(1)
            .filter { it.isNotBlank() }

        val validRows = mutableListOf<String>()
        val invalidRows = mutableListOf<String>()

        for (row in rows) {
            val columns = row.split(",")
            val firstName = columns[0].trim()
            val lastName = columns.getOrElse(1) { "" }.trim()
            val email = columns.getOrElse(2) { "" }.trim()

            if (firstName.isBlank() ||
                lastName.isBlank() ||
                email.isBlank() ||
                !emailRegex.matches(email)
            ) {
                invalidRows.add(row)
            } else {
                validRows.add(row)
            }
        }

        return CsvResult(
            validRows = validRows,
            invalidRows = invalidRows
        )
    }
}
