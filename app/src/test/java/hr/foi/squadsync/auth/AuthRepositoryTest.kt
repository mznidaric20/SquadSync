package hr.foi.squadsync.auth

import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.SessionState
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryTest {
    private val source = FakeAuthDataSource()
    private val store = MemorySessionStore()
    private var currentTime = 1_000L
    private fun repository() = DefaultAuthRepository(source, store) { currentTime }

    @Test fun noSavedSessionShowsLogin() = runTest {
        val repository = repository()
        repository.restoreSession()
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertEquals(0, source.fetchCalls)
    }

    @Test fun successfulSignInPersistsSessionAndNormalizesEmail() = runTest {
        val repository = repository()
        repository.signIn("  LUKA@primjer.com  ", "Lozinka123")
        assertEquals("luka@primjer.com", source.receivedEmail)
        assertEquals(testSession, store.saved)
        assertEquals(SessionState.Authenticated(testSession), repository.sessionState.value)
    }

    @Test fun registrationNormalizesProfileAndStartsSession() = runTest {
        val repository = repository()
        repository.register(testRegistration.copy(firstName = " Luka ", lastName = " Horvat ", email = " LUKA@PRIMJER.COM "))
        assertEquals(testRegistration, source.receivedRegistration)
        assertEquals(testSession, store.saved)
    }

    @Test fun freshRepositoryFetchesProfileUsingSavedSession() = runTest {
        repository().signIn(testUser.email, "Lozinka123")
        source.profile = testUser.copy(firstName = "Lucija")
        val restarted = repository()
        restarted.restoreSession()
        assertEquals(1, source.fetchCalls)
        assertEquals("Lucija", (restarted.sessionState.value as SessionState.Authenticated).session.user.firstName)
        assertEquals(source.profile, store.saved?.user)
    }

    @Test fun expiredSessionIsRemovedWithoutFetchingProfile() = runTest {
        store.saved = testSession
        currentTime = testSession.expiresAtEpochMillis
        val repository = repository()
        repository.restoreSession()
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertNull(store.saved)
        assertEquals(0, source.fetchCalls)
    }

    @Test fun rejectedTokenAndCorruptSessionRequireNewSignIn() = runTest {
        store.saved = testSession
        source.fetchFailure = AuthException(AuthError.SESSION_EXPIRED)
        val repository = repository()
        repository.restoreSession()
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertNull(store.saved)

        store.readFailure = AuthException(AuthError.SESSION_EXPIRED)
        repository.restoreSession()
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
    }

    @Test fun serviceOutagePreservesSavedSessionForRetry() = runTest {
        store.saved = testSession
        source.fetchFailure = AuthException(AuthError.SERVICE_UNAVAILABLE)
        val repository = repository()
        repository.restoreSession()
        assertEquals(SessionState.RestoreFailed(AuthError.SERVICE_UNAVAILABLE), repository.sessionState.value)
        assertEquals(testSession, store.saved)
        source.fetchFailure = null
        repository.restoreSession()
        assertEquals(SessionState.Authenticated(testSession), repository.sessionState.value)
    }

    @Test fun aDifferentFetchedUserCannotTakeOverSavedSession() = runTest {
        store.saved = testSession
        source.profile = testUser.copy(id = "another-user")
        val repository = repository()
        repository.restoreSession()
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertNull(store.saved)
    }

    @Test fun failedPersistenceDoesNotAuthenticate() = runTest {
        val repository = repository()
        repository.restoreSession()
        store.writeFailure = IOException("Disk unavailable")
        try {
            repository.signIn(testUser.email, "Lozinka123")
            fail("Expected persistence failure")
        } catch (_: IOException) {
            assertEquals(SessionState.SignedOut, repository.sessionState.value)
            assertNull(store.saved)
        }
    }

    @Test fun signOutClearsSessionEvenIfRemoteRevocationFails() = runTest {
        val repository = repository()
        repository.signIn(testUser.email, "Lozinka123")
        source.signOutFailure = IOException("Offline")
        repository.signOut()
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertNull(store.saved)
        val restarted = repository()
        restarted.restoreSession()
        assertEquals(SessionState.SignedOut, restarted.sessionState.value)
    }

    @Test fun failedLocalSignOutDoesNotClaimTheSessionWasRemoved() = runTest {
        val repository = repository()
        repository.signIn(testUser.email, "Lozinka123")
        store.clearFailure = IOException("Disk unavailable")
        try {
            repository.signOut()
            fail("Expected removal failure")
        } catch (_: IOException) {
            assertEquals(SessionState.Authenticated(testSession), repository.sessionState.value)
        }
    }

    @Test fun refreshInvalidatesSessionThatExpiredWhileAppWasOpen() = runTest {
        val repository = repository()
        repository.signIn(testUser.email, "Lozinka123")
        currentTime = testSession.expiresAtEpochMillis
        repository.refreshSession()
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertNull(store.saved)
    }

    @Test fun cancellationIsNotConvertedIntoAnAuthenticationError() = runTest {
        val repository = repository()
        repository.restoreSession()
        source.signInFailure = CancellationException("Cancelled")
        try {
            repository.signIn(testUser.email, "Lozinka123")
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            assertEquals(SessionState.SignedOut, repository.sessionState.value)
        }
    }
}
