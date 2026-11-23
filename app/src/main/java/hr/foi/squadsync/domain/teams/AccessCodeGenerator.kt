package hr.foi.squadsync.domain.teams

object AccessCodeGenerator {

    // Isti znakovi koje dopusta AccessCodeValidator i provjera u bazi (bez 0, O, 1, I i L).
    private const val ALLOWED_CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    private const val CODE_LENGTH = 6

    // Kod nije sigurno jedinstven. Baza odbija kod koji vec postoji, pa se tada stvara novi.
    fun generate(): String {
        var code = ""
        for (i in 1..CODE_LENGTH) {
            code += ALLOWED_CHARACTERS.random()
        }
        return code
    }
}
