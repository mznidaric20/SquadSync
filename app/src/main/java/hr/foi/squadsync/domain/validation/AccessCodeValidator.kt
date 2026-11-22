package hr.foi.squadsync.domain.validation

object AccessCodeValidator {

    // Isti dopusteni znakovi kao u provjeri tablice momcadi u bazi (bez 0, O, 1, I i L).
    private val accessCodeRegex = Regex("^[A-HJKMNP-Z2-9]{6}$")

    // Korisnik moze upisati kod malim slovima ili s razmacima, a u bazi je zapisan bez njih.
    fun normalize(code: String): String {
        return code.replace(" ", "").uppercase()
    }

    fun validate(code: String): String? {
        val normalizedCode = normalize(code)

        return when {
            normalizedCode.isEmpty() ->
                "Unesite pristupni kod."

            !accessCodeRegex.matches(normalizedCode) ->
                "Pristupni kod nije ispravan."

            else -> null
        }
    }
}
