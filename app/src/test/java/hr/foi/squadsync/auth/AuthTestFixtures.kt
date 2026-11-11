package hr.foi.squadsync.auth

import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.data.auth.SessionStore
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.UserProfile
import hr.foi.squadsync.domain.auth.UserRole
import kotlinx.coroutines.CompletableDeferred

internal val testUser = UserProfile("user-1", "Luka", "Horvat", "luka@primjer.com", UserRole.ATHLETE)
internal val testSession = AuthSession(testUser, "local-test-token", 10_000L)
internal val testRegistration = RegistrationRequest("Luka", "Horvat", "luka@primjer.com", "Lozinka123", UserRole.ATHLETE)

internal class MemorySessionStore(var saved: AuthSession? = null) : SessionStore {
    var readFailure: Exception? = null
    var writeFailure: Exception? = null
    var clearFailure: Exception? = null
    override suspend fun read(): AuthSession? {
        readFailure?.let { throw it }
        return saved
    }
    override suspend fun write(session: AuthSession) {
        writeFailure?.let { throw it }
        saved = session
    }
    override suspend fun clear() {
        clearFailure?.let { throw it }
        saved = null
    }
}

internal class FakeAuthDataSource : AuthDataSource {
    var session = testSession
    var profile = testUser
    var signInFailure: Exception? = null
    var fetchFailure: Exception? = null
    var signOutFailure: Exception? = null
    var gate: CompletableDeferred<Unit>? = null
    var fetchGate: CompletableDeferred<Unit>? = null
    var signInCalls = 0
    var registrationCalls = 0
    var fetchCalls = 0
    var receivedEmail: String? = null
    var receivedRegistration: RegistrationRequest? = null

    override suspend fun signIn(email: String, password: String): AuthSession {
        signInCalls++
        receivedEmail = email
        gate?.await()
        signInFailure?.let { throw it }
        return session
    }
    override suspend fun register(request: RegistrationRequest): AuthSession {
        registrationCalls++
        receivedRegistration = request
        gate?.await()
        return session
    }
    override suspend fun fetchCurrentUser(session: AuthSession): UserProfile {
        fetchCalls++
        fetchGate?.await()
        fetchFailure?.let { throw it }
        return profile
    }
    override suspend fun signOut(session: AuthSession) {
        signOutFailure?.let { throw it }
    }
}
