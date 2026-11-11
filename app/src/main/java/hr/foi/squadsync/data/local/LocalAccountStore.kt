package hr.foi.squadsync.data.local

import hr.foi.squadsync.domain.auth.UserProfile

data class LocalAccount(
    val user: UserProfile,
    val salt: String,
    val passwordHash: String,
    val tokenHash: String? = null,
    val expiresAtEpochMillis: Long? = null
)

interface LocalAccountStore {
    suspend fun read(): List<LocalAccount>
    suspend fun write(accounts: List<LocalAccount>)
}
