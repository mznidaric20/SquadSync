package hr.foi.squadsync.data.auth

import hr.foi.squadsync.domain.auth.AuthSession

interface SessionStore {
    suspend fun read(): AuthSession?
    suspend fun write(session: AuthSession)
    suspend fun clear()
}
