package hr.foi.squadsync.auth

import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.data.local.DemoAccounts
import hr.foi.squadsync.data.local.LocalAccount
import hr.foi.squadsync.data.local.LocalAccountStore
import hr.foi.squadsync.data.local.LocalAuthDataSource
import hr.foi.squadsync.data.local.PasswordHasher
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LocalAuthDataSourceTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val store = MemoryLocalAccountStore()
    private fun source() = LocalAuthDataSource(store, dispatcher = mainDispatcherRule.dispatcher, now = { 1_000L })
    private fun repository() = DefaultAuthRepository(source(), MemorySessionStore()) { 1_000L }

    @Test fun bothDemoAccountsCanLogInAndFetchTheirOwnProfile() = runTest {
        val source = source()
        DemoAccounts.users.forEach { demo ->
            val session = source.signIn(demo.email, demo.password)
            val profile = source.fetchCurrentUser(session)
            assertEquals(demo.email, profile.email)
            assertEquals(demo.firstName, profile.firstName)
            assertEquals(demo.lastName, profile.lastName)
            assertEquals(demo.role, profile.role)
            source.signOut(session)
            expectError(AuthError.SESSION_EXPIRED) { source.fetchCurrentUser(session) }
        }
        assertEquals(2, store.accounts.size)
        assertEquals(setOf(UserRole.ATHLETE, UserRole.COACH), store.accounts.map { it.user.role }.toSet())
    }

    @Test fun registrationAddsToTheSameListAndAllowsLoginFromANewDataSource() = runTest {
        val repository = repository()
        repository.register(testRegistration)
        val registeredProfile = (repository.sessionState.value as SessionState.Authenticated).session.user
        assertEquals(3, store.accounts.size)
        assertEquals(registeredProfile, store.accounts.single { it.user.email == testRegistration.email }.user)
        repository.signOut()

        val restarted = repository()
        restarted.signIn("  LUKA@PRIMJER.COM  ", testRegistration.password)
        val loggedIn = restarted.sessionState.value as SessionState.Authenticated
        assertEquals(registeredProfile, loggedIn.session.user)
        assertEquals(3, store.accounts.size)
    }

    @Test fun wrongPasswordDoesNotCreateASession() = runTest {
        val repository = repository()
        repository.restoreSession()
        expectError(AuthError.INVALID_CREDENTIALS) {
            repository.signIn(DemoAccounts.users.first().email, "incorrect-password")
        }
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertTrue(store.accounts.all { it.tokenHash == null })
    }

    @Test fun unknownEmailIsRejectedInsteadOfCreatingAnAccount() = runTest {
        expectError(AuthError.INVALID_CREDENTIALS) { source().signIn("unknown@primjer.com", "Lozinka123") }
        assertEquals(DemoAccounts.users.map { it.email }.toSet(), store.accounts.map { it.user.email }.toSet())
    }

    @Test fun registrationCannotDuplicateADemoEmailWithDifferentCasing() = runTest {
        val demo = DemoAccounts.users.first()
        expectError(AuthError.EMAIL_ALREADY_USED) {
            repository().register(testRegistration.copy(email = " ${demo.email.uppercase(Locale.ROOT)} "))
        }
        assertEquals(2, store.accounts.size)
        assertEquals(demo.firstName, store.accounts.single { it.user.email == demo.email }.user.firstName)
    }

    @Test fun addingDemoUsersPreservesExistingProfilePasswordAndToken() = runTest {
        val hasher = PasswordHasher()
        val salt = hasher.newSalt()
        val existingPassword = "ExistingPassword123!"
        val email = DemoAccounts.users.first().email
        val existing = LocalAccount(testUser.copy(email = email, role = UserRole.COACH), salt,
            hasher.hash(existingPassword, salt), "existing-token-hash", 9_000L)
        store.write(listOf(existing))
        val source = source()
        val otherDemo = DemoAccounts.users.last()
        source.signIn(otherDemo.email, otherDemo.password)
        assertEquals(existing, store.accounts.single { it.user.email == email })
        assertEquals(2, store.accounts.size)
        val session = source.signIn(email, existingPassword)
        assertEquals(existing.user, session.user)
    }

    @Test fun profileFetchReadsUpdatedDataFromTheList() = runTest {
        val source = source()
        val demo = DemoAccounts.users.first()
        val session = source.signIn(demo.email, demo.password)
        val updatedProfile = session.user.copy(firstName = "Lucija")
        store.write(store.accounts.map { account ->
            if (account.user.id == updatedProfile.id) account.copy(user = updatedProfile) else account
        })
        assertEquals(updatedProfile, source.fetchCurrentUser(session))
    }

    private suspend fun expectError(expected: AuthError, action: suspend () -> Unit) {
        try {
            action()
            fail("Expected $expected")
        } catch (exception: AuthException) {
            assertEquals(expected, exception.error)
        }
    }
}

private class MemoryLocalAccountStore : LocalAccountStore {
    var accounts: List<LocalAccount> = emptyList()
        private set

    override suspend fun read(): List<LocalAccount> = accounts.toList()
    override suspend fun write(accounts: List<LocalAccount>) { this.accounts = accounts.toList() }
}
