package hr.foi.squadsync.teams

import hr.foi.squadsync.auth.MainDispatcherRule
import hr.foi.squadsync.domain.teams.TeamJoinError
import hr.foi.squadsync.domain.teams.TeamJoinException
import hr.foi.squadsync.domain.teams.TeamJoinRepository
import hr.foi.squadsync.ui.teams.JoinTeamViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

// Lazna izvedba umjesto Supabasea: vraca zadani naziv momcadi ili zadanu gresku.
private class FakeTeamJoinRepository : TeamJoinRepository {
    var error: TeamJoinError? = null
    var calls = 0

    override suspend fun joinTeam(accessCode: String): String {
        calls++
        val currentError = error
        if (currentError != null) {
            throw TeamJoinException(currentError)
        }
        return "Seniori"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class JoinTeamViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val repository = FakeTeamJoinRepository()

    @Test
    fun givenEmptyCode_whenSubmitted_thenCodeErrorWithoutCallingRepository() = runTest {
        val viewModel = JoinTeamViewModel(repository)

        viewModel.submit()
        runCurrent()

        assertEquals("Unesite pristupni kod.", viewModel.uiState.value.codeError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun givenValidCode_whenSubmitted_thenJoinedTeamNameShownAndCodeCleared() = runTest {
        val viewModel = JoinTeamViewModel(repository)

        viewModel.onAccessCodeChange("X7K2P4")
        viewModel.submit()
        runCurrent()

        assertEquals("Seniori", viewModel.uiState.value.joinedTeamName)
        assertEquals("", viewModel.uiState.value.accessCode)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun givenUnknownCode_whenSubmitted_thenInvalidCodeError() = runTest {
        repository.error = TeamJoinError.INVALID_CODE
        val viewModel = JoinTeamViewModel(repository)

        viewModel.onAccessCodeChange("X7K2P4")
        viewModel.submit()
        runCurrent()

        assertEquals(TeamJoinError.INVALID_CODE, viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.joinedTeamName)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun givenAlreadyMember_whenSubmitted_thenAlreadyMemberError() = runTest {
        repository.error = TeamJoinError.ALREADY_MEMBER
        val viewModel = JoinTeamViewModel(repository)

        viewModel.onAccessCodeChange("X7K2P4")
        viewModel.submit()
        runCurrent()

        assertEquals(TeamJoinError.ALREADY_MEMBER, viewModel.uiState.value.error)
    }

    @Test
    fun givenError_whenCodeChanged_thenErrorCleared() = runTest {
        repository.error = TeamJoinError.INVALID_CODE
        val viewModel = JoinTeamViewModel(repository)
        viewModel.onAccessCodeChange("X7K2P4")
        viewModel.submit()
        runCurrent()

        viewModel.onAccessCodeChange("X7K2P5")

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun givenJoinedTeam_whenCleared_thenMessageRemoved() = runTest {
        val viewModel = JoinTeamViewModel(repository)
        viewModel.onAccessCodeChange("X7K2P4")
        viewModel.submit()
        runCurrent()

        viewModel.clear()

        assertNull(viewModel.uiState.value.joinedTeamName)
    }
}
