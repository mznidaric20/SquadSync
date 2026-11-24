package hr.foi.squadsync.teams

import hr.foi.squadsync.auth.MainDispatcherRule
import hr.foi.squadsync.data.teams.local.LocalTeamDataSource
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.domain.teams.TeamRoster
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LocalTeamDataSourceTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val store = MemoryTeamStore()
    private val accounts = TeamAccountStore()
    private val source = LocalTeamDataSource(store, accounts, mainDispatcherRule.dispatcher)

    @Test fun firstCallerDoesNotBecomeOwnerOfTheDemoTeams() = runTest {
        val other = coachSession.copy(user = coach.copy(id = "coach-b", email = "drugi@primjer.com"))
        assertTrue(source.fetchTeams(other).isEmpty())
        assertTrue(store.rosters!!.all { it.team.coachId == coach.id })
        val teams = source.fetchTeams(coachSession)
        assertEquals(2, teams.size)
        assertEquals(4, source.fetchMembers(coachSession, "demo-seniori").size)
        assertTrue(source.fetchMembers(coachSession, "demo-juniori").isEmpty())
        assertEquals(1, store.writes)
    }

    @Test fun localMemberReadAlsoRejectsForeignCoachesAndAthletes() = runTest {
        source.fetchTeams(coachSession)
        val foreign = coachSession.copy(user = coach.copy(id = "coach-b"))
        val athleteSession = coachSession.copy(user = coach.copy(role = UserRole.ATHLETE))
        assertEquals(TeamError.ACCESS_DENIED, teamFailure { source.fetchMembers(foreign, "demo-seniori") })
        assertEquals(TeamError.ACCESS_DENIED, teamFailure { source.fetchMembers(athleteSession, "demo-seniori") })
    }

    @Test fun existingOwnershipIsNotReassignedToARecreatedAccount() = runTest {
        val saved = TeamRoster(ownTeam.copy(coachId = "old-account-id"), listOf(athlete))
        store.rosters = listOf(saved)
        assertTrue(source.fetchTeams(coachSession).isEmpty())
        assertEquals(listOf(saved), store.rosters)
        assertEquals(0, store.writes)
        assertEquals(TeamError.ACCESS_DENIED, teamFailure { source.fetchMembers(coachSession, ownTeam.id) })
    }

    @Test fun aSavedEmptyCatalogIsNotOverwrittenWithDemoData() = runTest {
        store.rosters = emptyList()
        assertTrue(source.fetchTeams(coachSession).isEmpty())
        assertEquals(0, store.writes)
    }

    @Test fun missingDemoCoachDoesNotPermanentlySaveAnEmptyCatalog() = runTest {
        val original = accounts.accounts
        accounts.accounts = emptyList()
        assertTrue(source.fetchTeams(coachSession).isEmpty())
        assertNull(store.rosters)
        accounts.accounts = original
        assertEquals(2, source.fetchTeams(coachSession).size)
        assertEquals(1, store.writes)
    }
}
