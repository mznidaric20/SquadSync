package hr.foi.squadsync.data.auth

import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.UserProfile

interface AuthDataSource {
    suspend fun signIn(email: String, password: String): AuthSession
    suspend fun register(request: RegistrationRequest): AuthSession
    suspend fun fetchCurrentUser(session: AuthSession): UserProfile
    suspend fun signOut(session: AuthSession)
}
