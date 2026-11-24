package hr.foi.squadsync.teams

import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.auth.MainDispatcherRule
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.ui.teams.TeamsViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val repository = TestTeamRepository()

    @Test fun loadsOnlyTheTeamRequestedByTheCard() = runTest {
        val viewModel = TeamsViewModel(repository, emptyTeam.id)
        try {
            viewModel.refresh()
            runCurrent()
            assertEquals(emptyTeam.id, viewModel.uiState.value.selectedTeamId)
            assertEquals(listOf(emptyTeam.id), repository.rosterRequests)
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertFalse(viewModel.uiState.value.isLoading)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun returnsTheRequestedTeamsMembersAndMetadata() = runTest {
        repository.teams = listOf(ownTeam.copy(description = "Prva momčad", accessCode = "ABC123"))
        val viewModel = TeamsViewModel(repository, ownTeam.id)
        try {
            viewModel.refresh()
            runCurrent()
            assertEquals(listOf(athlete), viewModel.uiState.value.members)
            assertEquals("Prva momčad", viewModel.uiState.value.selectedTeam?.description)
            assertEquals("ABC123", viewModel.uiState.value.selectedTeam?.accessCode)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun missingRequestedTeamDoesNotFallBackToAnotherTeam() = runTest {
        val viewModel = TeamsViewModel(repository, foreignTeam.id)
        try {
            viewModel.refresh()
            runCurrent()
            assertEquals(TeamError.NOT_FOUND, viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.selectedTeam)
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertEquals(listOf(foreignTeam.id), repository.rosterRequests)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun deniedRefreshRemovesPreviouslyDisplayedMembers() = runTest {
        val viewModel = TeamsViewModel(repository, ownTeam.id)
        try {
            viewModel.refresh()
            runCurrent()
            repository.rosterError = TeamError.ACCESS_DENIED
            viewModel.refresh()
            runCurrent()
            assertTrue(viewModel.uiState.value.teams.isEmpty())
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertNull(viewModel.uiState.value.selectedTeamId)
            assertEquals(TeamError.ACCESS_DENIED, viewModel.uiState.value.error)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun refreshClearsOldMembersWhileWaitingForTheNewRoster() = runTest {
        val viewModel = TeamsViewModel(repository, ownTeam.id)
        try {
            viewModel.refresh()
            runCurrent()
            repository.gates[ownTeam.id] = CompletableDeferred()
            viewModel.refresh()
            runCurrent()
            assertTrue(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.members.isEmpty())
            repository.gates.getValue(ownTeam.id).complete(Unit)
            runCurrent()
            assertEquals(listOf(athlete), viewModel.uiState.value.members)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun retryAfterAnErrorRequestsTheSameTeam() = runTest {
        repository.rosterError = TeamError.DATA_UNAVAILABLE
        val viewModel = TeamsViewModel(repository, ownTeam.id)
        try {
            viewModel.refresh()
            runCurrent()
            assertEquals(TeamError.DATA_UNAVAILABLE, viewModel.uiState.value.error)
            repository.rosterError = null
            viewModel.refresh()
            runCurrent()
            assertNull(viewModel.uiState.value.error)
            assertEquals(listOf(athlete), viewModel.uiState.value.members)
            assertEquals(listOf(ownTeam.id, ownTeam.id), repository.rosterRequests)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun closingCancelsPendingMembersAndReopeningRevalidatesTheSameTeam() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gates[emptyTeam.id] = gate
        val viewModel = TeamsViewModel(repository, emptyTeam.id)
        try {
            viewModel.refresh()
            runCurrent()
            viewModel.onScreenClosed()
            gate.complete(Unit)
            runCurrent()
            assertTrue(viewModel.uiState.value.teams.isEmpty())
            assertTrue(viewModel.uiState.value.members.isEmpty())
            viewModel.refresh()
            runCurrent()
            assertEquals(emptyTeam.id, viewModel.uiState.value.selectedTeamId)
            assertFalse(viewModel.uiState.value.isLoading)
        } finally { viewModel.viewModelScope.cancel() }
    }
}
