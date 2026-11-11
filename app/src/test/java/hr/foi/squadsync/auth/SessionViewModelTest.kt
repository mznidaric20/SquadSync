package hr.foi.squadsync.auth

import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.ui.auth.SessionViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun sessionExpiresEvenWhenAnInFlightProfileRefreshFails() = runTest {
        val source = FakeAuthDataSource()
        val store = MemorySessionStore(testSession.copy(expiresAtEpochMillis = 1_000L))
        val repository = DefaultAuthRepository(source, store) { testScheduler.currentTime }
        val viewModel = SessionViewModel(repository) { testScheduler.currentTime }
        try {
            runCurrent()
            assertTrue(repository.sessionState.value is SessionState.Authenticated)
            source.fetchGate = CompletableDeferred()
            source.fetchFailure = AuthException(AuthError.SERVICE_UNAVAILABLE)
            viewModel.refresh()
            runCurrent()
            assertTrue(viewModel.uiState.value.isBusy)
            advanceTimeBy(1_000L)
            runCurrent()
            source.fetchGate!!.complete(Unit)
            runCurrent()
            assertEquals(SessionState.SignedOut, repository.sessionState.value)
            assertNull(store.saved)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }
}
