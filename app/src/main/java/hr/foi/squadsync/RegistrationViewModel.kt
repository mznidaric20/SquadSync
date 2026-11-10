package hr.foi.squadsync

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class RegistrationErrors(
    val ime: String? = null,
    val prezime: String? = null,
    val email: String? = null,
    val lozinka: String? = null,
    val potvrdaLozinke: String? = null,
    val uloga: String? = null
)

class RegistrationViewModel : ViewModel() {

    var ime by mutableStateOf("")
    var prezime by mutableStateOf("")
    var email by mutableStateOf("")
    var lozinka by mutableStateOf("")
    var potvrdaLozinke by mutableStateOf("")
    var uloga by mutableStateOf("")

    var greske by mutableStateOf(RegistrationErrors())
        private set

    var poruka by mutableStateOf("")
        private set

    fun provjeriPodatke() {

        greske = RegistrationValidator.validate(
            ime = ime,
            prezime = prezime,
            email = email,
            lozinka = lozinka,
            potvrdaLozinke = potvrdaLozinke,
            uloga = uloga
        )

        val imaGresaka = listOf(
            greske.ime,
            greske.prezime,
            greske.email,
            greske.lozinka,
            greske.potvrdaLozinke,
            greske.uloga
        ).any { it != null }

        poruka = if (imaGresaka) {
            "Molimo ispravite označena polja."
        } else {
            "Podaci su ispravni. Registracija još nije povezana s poslužiteljem."
        }
    }
}