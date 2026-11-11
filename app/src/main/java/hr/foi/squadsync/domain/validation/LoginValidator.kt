package hr.foi.squadsync.domain.validation

data class LoginErrors(
    val email: String? = null,
    val lozinka: String? = null
) {
    val hasErrors: Boolean get() = email != null || lozinka != null
}

object LoginValidator {

    private val emailRegex =
        Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validate(
        email: String,
        lozinka: String
    ): LoginErrors {

        return LoginErrors(

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

                else -> null
            }
        )
    }
}
