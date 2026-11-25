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
        val seenEmails = mutableSetOf<String>()

        for (row in rows) {
            val columns = row.split(",").map { it.trim() }

            if (!isValidRow(columns)) {
                invalidRows.add(row)
                continue
            }

            val email = columns[2].lowercase()

            if (seenEmails.add(email)) {
                validRows.add(row)
            } else {
                invalidRows.add(row)
            }
        }

        return CsvResult(
            validRows = validRows,
            invalidRows = invalidRows
        )
    }

    private fun isValidRow(columns: List<String>): Boolean {
        return columns.size == 3 &&
                columns[0].isNotBlank() &&
                columns[1].isNotBlank() &&
                emailRegex.matches(columns[2])
    }
}
