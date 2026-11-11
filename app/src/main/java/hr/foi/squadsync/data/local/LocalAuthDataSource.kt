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

class LocalAuthDataSource(
    private val accountStore: LocalAccountStore,
    private val passwordHasher: PasswordHasher = PasswordHasher(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val now: () -> Long = System::currentTimeMillis
) : AuthDataSource {
    private val mutex = Mutex()

    override suspend fun signIn(email: String, password: String): AuthSession = withContext(dispatcher) {
        mutex.withLock {
            val accounts = readAccounts()
            val index = accounts.indexOfFirst { it.user.email == email }
            if (index < 0) throw AuthException(AuthError.INVALID_CREDENTIALS)
            val account = accounts[index]
            if (!passwordHasher.matches(password, account.salt, account.passwordHash)) {
                throw AuthException(AuthError.INVALID_CREDENTIALS)
            }
            startSession(index, accounts)
        }
    }

    override suspend fun register(request: RegistrationRequest): AuthSession = withContext(dispatcher) {
        mutex.withLock {
            val accounts = readAccounts()
            if (accounts.any { it.user.email == request.email }) {
                throw AuthException(AuthError.EMAIL_ALREADY_USED)
            }
            accounts.add(createAccount(request))
            startSession(accounts.lastIndex, accounts)
        }
    }

    override suspend fun fetchCurrentUser(session: AuthSession): UserProfile = withContext(dispatcher) {
        mutex.withLock {
            val account = readAccounts().firstOrNull { it.user.id == session.user.id }
                ?: throw AuthException(AuthError.SESSION_EXPIRED)
            if (session.isExpired(now()) || (account.expiresAtEpochMillis ?: 0L) <= now() ||
                account.tokenHash != tokenHash(session.accessToken)) {
                throw AuthException(AuthError.SESSION_EXPIRED)
            }
            account.user
        }
    }

    override suspend fun signOut(session: AuthSession) = withContext(dispatcher) {
        mutex.withLock {
            val accounts = readAccounts()
            val index = accounts.indexOfFirst { it.tokenHash == tokenHash(session.accessToken) }
            if (index >= 0) {
                accounts[index] = accounts[index].copy(tokenHash = null, expiresAtEpochMillis = null)
                accountStore.write(accounts)
            }
            Unit
        }
    }

    private suspend fun readAccounts(): MutableList<LocalAccount> {
        val accounts = accountStore.read().toMutableList()
        val missingDemoUsers = DemoAccounts.users.filter { demo ->
            accounts.none { it.user.email.equals(demo.email, ignoreCase = true) }
        }
        if (missingDemoUsers.isNotEmpty()) {
            accounts.addAll(missingDemoUsers.map(::createAccount))
            accountStore.write(accounts)
        }
        return accounts
    }

    private fun createAccount(request: RegistrationRequest): LocalAccount {
        val salt = passwordHasher.newSalt()
        return LocalAccount(
            user = UserProfile(UUID.randomUUID().toString(), request.firstName, request.lastName,
                request.email, request.role),
            salt = salt,
            passwordHash = passwordHasher.hash(request.password, salt)
        )
    }

    private suspend fun startSession(index: Int, accounts: MutableList<LocalAccount>): AuthSession {
        val account = accounts[index]
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also {
            SecureRandom().nextBytes(it)
        })
        val expiresAt = now() + SESSION_LIFETIME_MILLIS
        accounts[index] = account.copy(tokenHash = tokenHash(token), expiresAtEpochMillis = expiresAt)
        accountStore.write(accounts)
        return AuthSession(account.user, token, expiresAt)
    }

    private fun tokenHash(token: String): String = Base64.getEncoder().encodeToString(
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
    )

    companion object {
        private const val SESSION_LIFETIME_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}
