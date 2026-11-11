package hr.foi.squadsync.domain.validation

data class RegistrationErrors(
    val ime: String? = null,
    val prezime: String? = null,
    val email: String? = null,
    val lozinka: String? = null,
    val potvrdaLozinke: String? = null,
    val uloga: String? = null
) {
    val hasErrors: Boolean get() = listOf(ime, prezime, email, lozinka, potvrdaLozinke, uloga).any { it != null }
}

object RegistrationValidator {

    private val emailRegex =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")

    fun validate(
        ime: String,
        prezime: String,
        email: String,
        lozinka: String,
        potvrdaLozinke: String,
        uloga: String
    ): RegistrationErrors {

        return RegistrationErrors(

            ime = if (ime.isBlank()) {
                "Unesite ime."
            } else null,

            prezime = if (prezime.isBlank()) {
                "Unesite prezime."
            } else null,

            email = when {
                email.isBlank() ->
                    "Unesite e-poštu."

                !emailRegex.matches(email.trim()) ->
                    "E-pošta nije ispravna."

                else -> null
            },

            lozinka = when {
                lozinka.isBlank() ->
                    "Unesite lozinku."

                lozinka.length < 8 ->
                    "Lozinka mora imati najmanje 8 znakova."

                else -> null
            },

            potvrdaLozinke = when {
                potvrdaLozinke.isBlank() ->
                    "Potvrdite lozinku."

                potvrdaLozinke != lozinka ->
                    "Lozinke se ne podudaraju."

                else -> null
            },

            uloga = when {
                uloga.isBlank() ->
                    "Odaberite korisničku ulogu."

                uloga != "Trener" && uloga != "Sportaš" ->
                    "Odaberite dopuštenu korisničku ulogu."

                else -> null
            }
        )
    }
}
