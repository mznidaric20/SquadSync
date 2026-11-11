package hr.foi.squadsync.domain.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionState: StateFlow<SessionState>

    suspend fun restoreSession()
    suspend fun signIn(email: String, password: String)
    suspend fun register(request: RegistrationRequest)
    suspend fun refreshSession()
    suspend fun signOut()
}
