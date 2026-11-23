package hr.foi.squadsync.data.remote

import kotlinx.serialization.Serializable

// Jedan red iz tablice korisnici. Imena polja moraju biti ista kao stupci u bazi.
@Serializable
data class KorisnikRow(
    val id: String,
    val ime: String,
    val prezime: String,
    val email: String,
    val uloga: String
)
