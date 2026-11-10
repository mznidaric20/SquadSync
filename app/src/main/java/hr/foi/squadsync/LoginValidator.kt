package hr.foi.squadsync

data class LoginErrors(
    val email: String? = null,
    val lozinka: String? = null
)

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