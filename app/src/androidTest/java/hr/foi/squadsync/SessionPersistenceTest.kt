package hr.foi.squadsync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.data.local.EncryptedAccountStore
import hr.foi.squadsync.data.local.EncryptedSessionStore
import hr.foi.squadsync.data.local.LocalAuthDataSource
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefix = "test_${UUID.randomUUID()}"
    private val request = RegistrationRequest("Luka", "Horvat", "luka@primjer.com", "Lozinka123", UserRole.ATHLETE)
    private fun accounts() = EncryptedFileStore(context, "${prefix}_accounts.enc")
    private fun session() = EncryptedFileStore(context, "${prefix}_session.enc")
    private fun repository() = DefaultAuthRepository(LocalAuthDataSource(EncryptedAccountStore(accounts())), EncryptedSessionStore(session()))

    @After fun cleanUp() = runBlocking {
        accounts().clear()
        session().clear()
    }

    @Test fun registrationSurvivesNewRepositoryAndSignOutSurvivesRestart() = runBlocking {
        repository().register(request)
        val restarted = repository()
        restarted.restoreSession()
        val active = restarted.sessionState.value as SessionState.Authenticated
        assertEquals(request.email, active.session.user.email)
        assertEquals(request.role, active.session.user.role)
        val encrypted = File(context.noBackupFilesDir, "${prefix}_session.enc").readBytes().toString(Charsets.UTF_8)
        assertFalse(encrypted.contains(request.email))
        assertFalse(encrypted.contains(request.password))
        assertFalse(encrypted.contains(active.session.accessToken))
        restarted.signOut()
        val signedOut = repository()
        signedOut.restoreSession()
        assertEquals(SessionState.SignedOut, signedOut.sessionState.value)
        signedOut.signIn(" LUKA@PRIMJER.COM ", request.password)
        assertTrue(signedOut.sessionState.value is SessionState.Authenticated)
    }

    @Test fun corruptSessionReturnsToLoginAndKeepsRegisteredAccount() = runBlocking {
        repository().register(request)
        File(context.noBackupFilesDir, "${prefix}_session.enc").writeText("invalid ciphertext")
        val restarted = repository()
        restarted.restoreSession()
        assertEquals(SessionState.SignedOut, restarted.sessionState.value)
        restarted.signIn(request.email, request.password)
        assertTrue(restarted.sessionState.value is SessionState.Authenticated)
    }

    @Test fun wrongPasswordAndDuplicateEmailAreRejected() = runBlocking {
        val repository = repository()
        repository.register(request)
        repository.signOut()
        try {
            repository.signIn(request.email, "incorrect")
            fail("Expected invalid credentials")
        } catch (exception: AuthException) {
            assertEquals(AuthError.INVALID_CREDENTIALS, exception.error)
        }
        try {
            repository.register(request.copy(email = " LUKA@PRIMJER.COM "))
            fail("Expected duplicate account")
        } catch (exception: AuthException) {
            assertEquals(AuthError.EMAIL_ALREADY_USED, exception.error)
        }
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
    }
}
