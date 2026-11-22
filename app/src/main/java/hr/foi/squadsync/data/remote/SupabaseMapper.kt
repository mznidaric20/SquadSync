package hr.foi.squadsync.data.remote

import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.UserRole

// Prevodi vrijednosti izmedu aplikacije i Supabase baze.
object SupabaseMapper {

    // Vrijednosti moraju biti iste kao u enumu korisnicka_uloga u bazi.
    fun roleToDatabase(role: UserRole): String {
        return when (role) {
            UserRole.COACH -> "Trener"
            UserRole.ATHLETE -> "Sportaš"
        }
    }

    fun roleFromDatabase(value: String): UserRole {
        return when (value) {
            "Trener" -> UserRole.COACH
            else -> UserRole.ATHLETE
        }
    }

    // Kodovi gresaka koje vraca Supabase Auth
    fun errorFromCode(code: String?): AuthError {
        return when (code) {
            "invalid_credentials", "email_not_confirmed" -> AuthError.INVALID_CREDENTIALS
            "user_already_exists", "email_exists" -> AuthError.EMAIL_ALREADY_USED
            "session_not_found", "refresh_token_not_found" -> AuthError.SESSION_EXPIRED
            else -> AuthError.SERVICE_UNAVAILABLE
        }
    }
}
