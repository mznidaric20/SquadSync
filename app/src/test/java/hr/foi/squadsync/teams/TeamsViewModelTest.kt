package hr.foi.squadsync.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import hr.foi.squadsync.auth.MainDispatcherRule
import hr.foi.squadsync.domain.teams.TeamError
import java.io.IOException
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

    @Test fun changingTeamsClearsThePreviousRosterWhileLoading() = runTest {
        val viewModel = TeamsViewModel(repository, SavedStateHandle())
        try {
            viewModel.refresh()
            runCurrent()
            assertEquals(listOf(athlete), viewModel.uiState.value.members)
            repository.gates[emptyTeam.id] = CompletableDeferred()
            viewModel.selectTeam(emptyTeam.id)
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertTrue(viewModel.uiState.value.isLoadingMembers)
            runCurrent()
            viewModel.selectTeam(ownTeam.id)
            runCurrent()
            repository.gates[emptyTeam.id]!!.complete(Unit)
            runCurrent()
            assertEquals(ownTeam.id, viewModel.uiState.value.selectedTeamId)
            assertEquals(listOf(athlete), viewModel.uiState.value.members)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun deniedFetchRemovesPreviouslyDisplayedTeamsAndMembers() = runTest {
        val viewModel = TeamsViewModel(repository, SavedStateHandle())
        try {
            viewModel.refresh()
            runCurrent()
            repository.rosterError = TeamError.ACCESS_DENIED
            viewModel.selectTeam(emptyTeam.id)
            runCurrent()
            assertTrue(viewModel.uiState.value.teams.isEmpty())
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertNull(viewModel.uiState.value.selectedTeamId)
            assertEquals(TeamError.ACCESS_DENIED, viewModel.uiState.value.error)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun closingTheScreenClearsPrivateDataButKeepsTheSelectedIdForRevalidation() = runTest {
        val saved = SavedStateHandle()
        val viewModel = TeamsViewModel(repository, saved)
        try {
            viewModel.refresh()
            runCurrent()
            viewModel.selectTeam(emptyTeam.id)
            runCurrent()
            viewModel.onScreenClosed()
            assertTrue(viewModel.uiState.value.teams.isEmpty())
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertTrue(viewModel.uiState.value.isLoading)
            viewModel.refresh()
            runCurrent()
            assertEquals(emptyTeam.id, viewModel.uiState.value.selectedTeamId)
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertFalse(viewModel.uiState.value.isLoadingMembers)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun savedForeignSelectionIsIgnoredWhenLoadingTheCurrentCoachsTeams() = runTest {
        val saved = SavedStateHandle(mapOf("selected_team_id" to foreignTeam.id))
        val viewModel = TeamsViewModel(repository, saved)
        try {
            viewModel.refresh()
            runCurrent()
            assertEquals(ownTeam.id, viewModel.uiState.value.selectedTeamId)
            assertEquals(listOf(ownTeam.id), repository.rosterRequests)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun anEmptyTeamCatalogFinishesLoadingWithoutRequestingARoster() = runTest {
        repository.teams = emptyList()
        val viewModel = TeamsViewModel(repository, SavedStateHandle())
        try {
            viewModel.refresh()
            runCurrent()
            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.selectedTeamId)
            assertTrue(repository.rosterRequests.isEmpty())
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun aRefreshFailureClearsOldMembersAndReportsAnErrorInsteadOfSuccessfulEmptyData() = runTest {
        val viewModel = TeamsViewModel(repository, SavedStateHandle())
        try {
            viewModel.refresh()
            runCurrent()
            repository.listFailure = IOException("Unavailable test data source")
            viewModel.refresh()
            runCurrent()
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertTrue(viewModel.uiState.value.teams.isEmpty())
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(TeamError.DATA_UNAVAILABLE, viewModel.uiState.value.error)
        } finally { viewModel.viewModelScope.cancel() }
    }

    @Test fun aValidSavedEmptyTeamIsRestoredAndDoesNotShowTheFirstTeamsMembers() = runTest {
        val saved = SavedStateHandle(mapOf("selected_team_id" to emptyTeam.id))
        val viewModel = TeamsViewModel(repository, saved)
        try {
            viewModel.refresh()
            runCurrent()
            assertEquals(emptyTeam.id, viewModel.uiState.value.selectedTeamId)
            assertTrue(viewModel.uiState.value.members.isEmpty())
            assertFalse(viewModel.uiState.value.isLoadingMembers)
            assertEquals(listOf(emptyTeam.id), repository.rosterRequests)
        } finally { viewModel.viewModelScope.cancel() }
    }
}
