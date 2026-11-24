package hr.foi.squadsync.domain.teams

import java.security.SecureRandom

object AccessCodeGenerator {

    // Isti znakovi koje dopusta AccessCodeValidator i provjera u bazi (bez 0, O, 1, I i L).
    private const val ALLOWED_CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    private const val CODE_LENGTH = 6

    // Kod sluzi kao mala lozinka za ulazak u momcad, pa SecureRandom jer ga je teze pogoditi.
    private val random = SecureRandom()

    // Kod nije sigurno jedinstven. Baza odbija kod koji vec postoji, pa se tada stvara novi.
    fun generate(): String {
        var code = ""
        for (i in 1..CODE_LENGTH) {
            val index = random.nextInt(ALLOWED_CHARACTERS.length)
            code += ALLOWED_CHARACTERS[index]
        }
        return code
    }
}
