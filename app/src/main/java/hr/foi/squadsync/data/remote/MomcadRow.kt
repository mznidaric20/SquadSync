package hr.foi.squadsync.data.remote

import kotlinx.serialization.Serializable

// Podaci koje saljemo u Supabase pri kreiranju momcadi.
@Serializable
data class NovaMomcadRow(
    val naziv: String,
    val opis: String?,
    val trener_id: String,
    val pristupni_kod: String
)

// Podaci koje Supabase vraca nakon kreiranja.
@Serializable
data class MomcadRow(
    val id: String,
    val naziv: String,
    val opis: String?,
    val trener_id: String,
    val pristupni_kod: String
)