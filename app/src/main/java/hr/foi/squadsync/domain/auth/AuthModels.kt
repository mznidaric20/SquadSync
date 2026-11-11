package hr.foi.squadsync.domain.auth

enum class UserRole { ATHLETE, COACH }

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: UserRole
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class AuthSession(
    val user: UserProfile,
    val accessToken: String,
    val expiresAtEpochMillis: Long
) {
    fun isExpired(now: Long): Boolean = expiresAtEpochMillis <= now
}

data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: UserRole
)

enum class AuthError {
    INVALID_CREDENTIALS, EMAIL_ALREADY_USED, SESSION_EXPIRED, STORAGE_UNAVAILABLE, SERVICE_UNAVAILABLE
}

class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class Authenticated(val session: AuthSession) : SessionState
    data class RestoreFailed(val error: AuthError) : SessionState
}
