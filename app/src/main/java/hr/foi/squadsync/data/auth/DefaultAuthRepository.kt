package hr.foi.squadsync.data.auth

import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.SessionState
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultAuthRepository(
    private val dataSource: AuthDataSource,
    private val sessionStore: SessionStore,
    private val now: () -> Long = System::currentTimeMillis
) : AuthRepository {
    private val mutex = Mutex()
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    override val sessionState = _sessionState.asStateFlow()

    override suspend fun restoreSession() = mutex.withLock {
        _sessionState.value = SessionState.Loading
        try {
            val saved = sessionStore.read()
            if (saved == null) {
                _sessionState.value = SessionState.SignedOut
            } else {
                restore(saved)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            handleRestoreFailure(exception)
        }
    }

    override suspend fun signIn(email: String, password: String) = mutex.withLock {
        persist(dataSource.signIn(email.trim().lowercase(Locale.ROOT), password))
    }

    override suspend fun register(request: RegistrationRequest) = mutex.withLock {
        persist(dataSource.register(request.copy(
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            email = request.email.trim().lowercase(Locale.ROOT)
        )))
    }

    override suspend fun refreshSession() = mutex.withLock {
        val current = (_sessionState.value as? SessionState.Authenticated)?.session
            ?: return@withLock
        try {
            restore(current)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: AuthException) {
            if (exception.error == AuthError.SESSION_EXPIRED) {
                sessionStore.clear()
                _sessionState.value = SessionState.SignedOut
            } else {
                throw exception
            }
        }
    }

    override suspend fun signOut() = mutex.withLock {
        val current = (_sessionState.value as? SessionState.Authenticated)?.session
        // Local removal must succeed before reporting a successful sign-out.
        sessionStore.clear()
        _sessionState.value = SessionState.SignedOut
        if (current != null) {
            try {
                dataSource.signOut(current)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // A remote outage must not restore a session that was removed locally.
            }
        }
    }

    private suspend fun persist(session: AuthSession) {
        if (session.isExpired(now()) || session.accessToken.isBlank()) {
            throw AuthException(AuthError.SESSION_EXPIRED)
        }
        sessionStore.write(session)
        _sessionState.value = SessionState.Authenticated(session)
    }

    private suspend fun restore(session: AuthSession) {
        if (session.isExpired(now()) || session.accessToken.isBlank()) {
            throw AuthException(AuthError.SESSION_EXPIRED)
        }
        val user = dataSource.fetchCurrentUser(session)
        if (user.id != session.user.id) throw AuthException(AuthError.SESSION_EXPIRED)
        persist(session.copy(user = user))
    }

    private suspend fun handleRestoreFailure(exception: Exception) {
        if (exception is AuthException && exception.error == AuthError.SESSION_EXPIRED) {
            try {
                sessionStore.clear()
                _sessionState.value = SessionState.SignedOut
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _sessionState.value = SessionState.RestoreFailed(AuthError.STORAGE_UNAVAILABLE)
            }
        } else {
            _sessionState.value = SessionState.RestoreFailed(
                (exception as? AuthException)?.error ?: AuthError.STORAGE_UNAVAILABLE
            )
        }
    }
}
