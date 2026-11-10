package hr.foi.squadsync

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
        private set

    var lozinka by mutableStateOf("")
        private set

    var greske by mutableStateOf(LoginErrors())
        private set

    var poruka by mutableStateOf("")
        private set

    fun promijeniEmail(noviEmail: String) {
        email = noviEmail
        greske = greske.copy(email = null)
        poruka = ""
    }

    fun promijeniLozinku(novaLozinka: String) {
        lozinka = novaLozinka
        greske = greske.copy(lozinka = null)
        poruka = ""
    }

    fun provjeriPodatke() {

        greske = LoginValidator.validate(
            email = email,
            lozinka = lozinka
        )

        val imaGresaka =
            greske.email != null ||
                    greske.lozinka != null

        poruka = if (imaGresaka) {

            "Molimo ispravite označena polja."

        } else {

            "Podaci su ispravni. Prijava još nije povezana s poslužiteljem."

        }
    }
}