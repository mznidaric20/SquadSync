package hr.foi.squadsync.teams

import hr.foi.squadsync.data.teams.DefaultTeamRepository
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.TeamError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamRepositoryTest {
    private val auth = TestAuthRepository()
    private val source = TestTeamDataSource()
    private var currentTime = 1_000L
    private val repository = DefaultTeamRepository(auth, source) { currentTime }

    @Test fun signedOutUserCannotReadTeamData() = runTest {
        auth.sessionState.value = SessionState.SignedOut
        assertEquals(TeamError.SESSION_REQUIRED, teamFailure { repository.getMyTeams() })
        assertEquals(0, auth.refreshCalls)
        assertEquals(0, source.listCalls)
    }

    @Test fun athleteCannotFetchMembersEvenWithAValidTeamId() = runTest {
        auth.sessionState.value = SessionState.Authenticated(coachSession.copy(user = coach.copy(role = UserRole.ATHLETE)))
        assertEquals(TeamError.ACCESS_DENIED, teamFailure { repository.getRoster(ownTeam.id) })
        assertEquals(0, source.teamCalls)
        assertEquals(0, source.memberCalls)
    }

    @Test fun expiredOrBlankTokenCannotFetchData() = runTest {
        listOf(coachSession.copy(expiresAtEpochMillis = currentTime), coachSession.copy(accessToken = "")).forEach {
            auth.sessionState.value = SessionState.Authenticated(it)
            assertEquals(TeamError.SESSION_REQUIRED, teamFailure { repository.getMyTeams() })
        }
        assertEquals(0, source.listCalls)
    }

    @Test fun teamListContainsOnlyTheCurrentCoachsTeams() = runTest {
        assertEquals(listOf(ownTeam), repository.getMyTeams())
        assertEquals(1, auth.refreshCalls)
    }

    @Test fun coachCanFetchMembersOfAnOwnedTeam() = runTest {
        val roster = repository.getRoster(ownTeam.id)
        assertEquals(ownTeam, roster.team)
        assertEquals(listOf(athlete), roster.members)
        assertEquals(1, source.teamCalls)
        assertEquals(1, source.memberCalls)
    }

    @Test fun foreignTeamIsRejectedBeforeAnyMembersAreRead() = runTest {
        assertEquals(TeamError.ACCESS_DENIED, teamFailure { repository.getRoster(foreignTeam.id) })
        assertEquals(1, source.teamCalls)
        assertEquals(0, source.memberCalls)
    }

    @Test fun unknownTeamDoesNotReturnAnotherTeamsMembers() = runTest {
        assertEquals(TeamError.NOT_FOUND, teamFailure { repository.getRoster("missing-team") })
        assertEquals(0, source.memberCalls)
    }

    @Test fun failedSessionValidationStopsTheFetch() = runTest {
        auth.refreshFailure = AuthException(AuthError.SERVICE_UNAVAILABLE)
        assertEquals(TeamError.DATA_UNAVAILABLE, teamFailure { repository.getMyTeams() })
        assertEquals(0, source.listCalls)
    }

    @Test fun logoutWhileFetchingDoesNotReleaseMembers() = runTest {
        source.memberGate = CompletableDeferred()
        val result = async { teamFailure { repository.getRoster(ownTeam.id) } }
        runCurrent()
        auth.signOut()
        source.memberGate!!.complete(Unit)
        assertEquals(TeamError.SESSION_REQUIRED, result.await())
    }

    @Test fun switchingUsersWhileFetchingDoesNotReleaseThePreviousCoachsMembers() = runTest {
        source.memberGate = CompletableDeferred()
        val result = async { teamFailure { repository.getRoster(ownTeam.id) } }
        runCurrent()
        auth.sessionState.value = SessionState.Authenticated(coachSession.copy(
            user = coach.copy(id = "coach-b"), accessToken = "another-token"))
        source.memberGate!!.complete(Unit)
        assertEquals(TeamError.SESSION_REQUIRED, result.await())
    }

    @Test fun expiryWhileFetchingDoesNotReleaseMembers() = runTest {
        source.memberGate = CompletableDeferred()
        val result = async { teamFailure { repository.getRoster(ownTeam.id) } }
        runCurrent()
        currentTime = coachSession.expiresAtEpochMillis
        source.memberGate!!.complete(Unit)
        assertEquals(TeamError.SESSION_REQUIRED, result.await())
    }

    @Test fun revokedCoachRoleWhileFetchingDoesNotReleaseMembers() = runTest {
        source.memberGate = CompletableDeferred()
        val result = async { teamFailure { repository.getRoster(ownTeam.id) } }
        runCurrent()
        auth.sessionState.value = SessionState.Authenticated(coachSession.copy(user = coach.copy(role = UserRole.ATHLETE)))
        source.memberGate!!.complete(Unit)
        assertEquals(TeamError.ACCESS_DENIED, result.await())
    }

    @Test fun aUserChangeDuringSessionRefreshPreventsDataAccess() = runTest {
        auth.onRefresh = { auth.sessionState.value = SessionState.Authenticated(coachSession.copy(user = coach.copy(id = "coach-b"))) }
        assertEquals(TeamError.SESSION_REQUIRED, teamFailure { repository.getMyTeams() })
        assertEquals(0, source.listCalls)
    }

    @Test fun cancellationPropagatesWithoutBeingMappedToADataError() = runTest {
        source.memberFailure = CancellationException("Cancelled fetch")
        try {
            repository.getRoster(ownTeam.id)
            fail("Expected cancellation")
        } catch (exception: CancellationException) {
            assertEquals("Cancelled fetch", exception.message)
        }
    }

    @Test fun unresolvedOrFailedSessionRestorationCannotReadTeamData() = runTest {
        listOf(SessionState.Loading, SessionState.RestoreFailed(AuthError.SERVICE_UNAVAILABLE)).forEach { state ->
            auth.sessionState.value = state
            assertEquals(TeamError.SESSION_REQUIRED, teamFailure { repository.getMyTeams() })
        }
        assertEquals(0, source.listCalls)
    }

    @Test fun mismatchedTeamMetadataIsRejectedBeforeReadingMembers() = runTest {
        source.returnedTeam = ownTeam.copy(id = "different-team")
        assertEquals(TeamError.ACCESS_DENIED, teamFailure { repository.getRoster(ownTeam.id) })
        assertEquals(0, source.memberCalls)
    }

    @Test fun replacingTheSessionTokenDuringAFetchDoesNotReleaseItsResult() = runTest {
        source.memberGate = CompletableDeferred()
        val result = async { teamFailure { repository.getRoster(ownTeam.id) } }
        runCurrent()
        auth.sessionState.value = SessionState.Authenticated(coachSession.copy(accessToken = "replacement-token"))
        source.memberGate!!.complete(Unit)
        assertEquals(TeamError.SESSION_REQUIRED, result.await())
    }

    @Test fun anOwnedEmptyTeamReturnsAnEmptyRosterSuccessfully() = runTest {
        source.teams = listOf(emptyTeam)
        val roster = repository.getRoster(emptyTeam.id)
        assertEquals(emptyTeam, roster.team)
        assertTrue(roster.members.isEmpty())
        assertEquals(listOf(emptyTeam.id), source.memberRequests)
    }

    @Test fun consecutiveTeamSelectionsDoNotMixTheirMembers() = runTest {
        val reserves = emptyTeam.copy(memberCount = 1)
        val otherMember = athlete.copy(id = "member-b", firstName = "Petar", email = "petar@primjer.com")
        source.teams = listOf(ownTeam, reserves)
        source.membersByTeam[reserves.id] = listOf(otherMember)
        assertEquals(listOf(athlete), repository.getRoster(ownTeam.id).members)
        assertEquals(listOf(otherMember), repository.getRoster(reserves.id).members)
        assertEquals(listOf(ownTeam.id, reserves.id), source.memberRequests)
    }
}
