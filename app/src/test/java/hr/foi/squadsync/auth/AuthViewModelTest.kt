package hr.foi.squadsync.auth

import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.ui.auth.LoginViewModel
import hr.foi.squadsync.ui.auth.RegistrationViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val source = FakeAuthDataSource()
    private val repository = DefaultAuthRepository(source, MemorySessionStore()) { 1_000L }

    @Test fun invalidLoginStaysInFormWithoutCallingDataSource() = runTest {
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChange("invalid-email")
        viewModel.submit()
        runCurrent()
        assertTrue(viewModel.uiState.value.errors.hasErrors)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals(0, source.signInCalls)
    }

    @Test fun repeatedSubmitOnlyStartsOneRequestAndClearsPasswordAfterSuccess() = runTest {
        source.gate = CompletableDeferred()
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChange(testUser.email)
        viewModel.onPasswordChange("Lozinka123")
        viewModel.submit()
        viewModel.submit()
        assertTrue(viewModel.uiState.value.isSubmitting)
        runCurrent()
        assertEquals(1, source.signInCalls)
        source.gate!!.complete(Unit)
        runCurrent()
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test fun wrongPasswordShowsErrorAndEditingClearsIt() = runTest {
        source.signInFailure = AuthException(AuthError.INVALID_CREDENTIALS)
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChange(testUser.email)
        viewModel.onPasswordChange("wrong-password")
        viewModel.submit()
        runCurrent()
        assertEquals(AuthError.INVALID_CREDENTIALS, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSubmitting)
        viewModel.onPasswordChange("correct-password")
        assertNull(viewModel.uiState.value.error)
    }

    @Test fun registrationRequiresMatchingPasswordsAndAnExplicitRole() = runTest {
        val viewModel = RegistrationViewModel(repository)
        viewModel.onFirstNameChange("Luka")
        viewModel.onLastNameChange("Horvat")
        viewModel.onEmailChange(testUser.email)
        viewModel.onPasswordChange("Lozinka123")
        viewModel.onConfirmPasswordChange("Different123")
        viewModel.submit()
        runCurrent()
        assertNotNull(viewModel.uiState.value.errors.potvrdaLozinke)
        assertNotNull(viewModel.uiState.value.errors.uloga)
        assertEquals(0, source.registrationCalls)
    }

    @Test fun validRegistrationSendsChosenRoleAndClearsBothPasswords() = runTest {
        val viewModel = RegistrationViewModel(repository)
        viewModel.onFirstNameChange("Luka")
        viewModel.onLastNameChange("Horvat")
        viewModel.onEmailChange(testUser.email)
        viewModel.onPasswordChange("Lozinka123")
        viewModel.onConfirmPasswordChange("Lozinka123")
        viewModel.onRoleChange(UserRole.COACH)
        viewModel.submit()
        viewModel.submit()
        runCurrent()
        assertEquals(1, source.registrationCalls)
        assertEquals(UserRole.COACH, source.receivedRegistration?.role)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals("", viewModel.uiState.value.confirmPassword)
    }
}
