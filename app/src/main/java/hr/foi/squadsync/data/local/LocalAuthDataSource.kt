package hr.foi.squadsync.data.local

import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.UserProfile
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Local development adapter; accounts are confined to this installation. */
class LocalAuthDataSource(
    private val storage: EncryptedFileStore,
    private val passwordHasher: PasswordHasher = PasswordHasher(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val now: () -> Long = System::currentTimeMillis
) : AuthDataSource {
    private val mutex = Mutex()

    override suspend fun signIn(email: String, password: String): AuthSession = withContext(dispatcher) {
        mutex.withLock {
            val accounts = readAccounts()
            val account = accounts.firstOrNull { it.getJSONObject("user").getString("email") == email }
                ?: throw AuthException(AuthError.INVALID_CREDENTIALS)
            if (!passwordHasher.matches(password, account.getString("salt"), account.getString("passwordHash"))) {
                throw AuthException(AuthError.INVALID_CREDENTIALS)
            }
            issueSession(account).also { saveAccounts(accounts) }
        }
    }

    override suspend fun register(request: RegistrationRequest): AuthSession = withContext(dispatcher) {
        mutex.withLock {
            val accounts = readAccounts().toMutableList()
            if (accounts.any { it.getJSONObject("user").getString("email") == request.email }) {
                throw AuthException(AuthError.EMAIL_ALREADY_USED)
            }
            val salt = passwordHasher.newSalt()
            val user = UserProfile(UUID.randomUUID().toString(), request.firstName, request.lastName,
                request.email, request.role)
            val account = JSONObject().put("user", user.toJson())
                .put("salt", salt).put("passwordHash", passwordHasher.hash(request.password, salt))
            accounts.add(account)
            issueSession(account).also { saveAccounts(accounts) }
        }
    }

    override suspend fun fetchCurrentUser(session: AuthSession): UserProfile = withContext(dispatcher) {
        mutex.withLock {
            val account = readAccounts().firstOrNull {
                it.getJSONObject("user").getString("id") == session.user.id
            } ?: throw AuthException(AuthError.SESSION_EXPIRED)
            if (session.isExpired(now()) || account.optLong("expiresAt") <= now() ||
                account.optString("tokenHash") != tokenHash(session.accessToken)) {
                throw AuthException(AuthError.SESSION_EXPIRED)
            }
            account.getJSONObject("user").toUser()
        }
    }

    override suspend fun signOut(session: AuthSession) = withContext(dispatcher) {
        mutex.withLock {
            val accounts = readAccounts()
            accounts.firstOrNull { it.optString("tokenHash") == tokenHash(session.accessToken) }?.let {
                it.remove("tokenHash")
                it.remove("expiresAt")
                saveAccounts(accounts)
            }
            Unit
        }
    }

    private suspend fun readAccounts(): List<JSONObject> {
        val content = storage.read() ?: return emptyList()
        val json = JSONObject(content)
        require(json.getInt("version") == 1)
        val accounts = json.getJSONArray("accounts")
        return List(accounts.length()) { accounts.getJSONObject(it) }
    }

    private suspend fun saveAccounts(accounts: List<JSONObject>) {
        storage.write(JSONObject().put("version", 1).put("accounts", JSONArray(accounts)).toString())
    }

    private fun issueSession(account: JSONObject): AuthSession {
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also {
            SecureRandom().nextBytes(it)
        })
        val expiresAt = now() + SESSION_LIFETIME_MILLIS
        account.put("tokenHash", tokenHash(token)).put("expiresAt", expiresAt)
        return AuthSession(account.getJSONObject("user").toUser(), token, expiresAt)
    }

    private fun tokenHash(token: String): String = Base64.getEncoder().encodeToString(
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
    )

    companion object {
        private const val SESSION_LIFETIME_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}
