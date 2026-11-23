package hr.foi.squadsync.teams

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import hr.foi.squadsync.data.local.EncryptedAccountStore
import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.data.local.LocalAccount
import hr.foi.squadsync.data.teams.DefaultTeamRepository
import hr.foi.squadsync.data.teams.local.EncryptedTeamStore
import hr.foi.squadsync.data.teams.local.LocalTeamDataSource
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.domain.teams.TeamException
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeamStorageIntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefix = "team-story-${UUID.randomUUID()}"
    private val scenario = TeamStoryScenario()
    private val accountsFile = EncryptedFileStore(context, "$prefix-accounts.enc")
    private val teamsFile = EncryptedFileStore(context, "$prefix-teams.enc")
    private val accounts = EncryptedAccountStore(accountsFile)
    private val store = EncryptedTeamStore(teamsFile)

    @After fun cleanUp() = runBlocking {
        accountsFile.clear()
        teamsFile.clear()
    }

    @Test fun encryptedRoundTripPreservesIdentitiesNamesAndEmptyTeams() = runBlocking {
        val rosters = listOf(
            hr.foi.squadsync.domain.teams.TeamRoster(scenario.first, listOf(scenario.ana, scenario.ivan)),
            hr.foi.squadsync.domain.teams.TeamRoster(scenario.empty, emptyList())
        )
        store.write(rosters)
        assertEquals(rosters, EncryptedTeamStore(EncryptedFileStore(context, "$prefix-teams.enc")).read())
        val encrypted = File(context.noBackupFilesDir, "$prefix-teams.enc").readBytes().toString(Charsets.UTF_8)
        assertFalse(encrypted.contains(scenario.ana.email))
        assertFalse(encrypted.contains(scenario.coach.id))
    }

    @Test fun aNewRepositoryRestoresSavedTeamsAndTheirOriginalOwner() = runBlocking {
        seedDemoOwner()
        val first = repository()
        val original = first.getMyTeams()
        assertEquals(2, original.size)
        val restarted = repository()
        assertEquals(original, restarted.getMyTeams())
        assertEquals(4, restarted.getRoster("demo-seniori").members.size)
        assertTrue(restarted.getRoster("demo-juniori").members.isEmpty())
    }

    @Test fun aSecondCoachCannotReadSavedMembersUsingTheirKnownTeamId() = runBlocking {
        seedDemoOwner()
        repository().getMyTeams()
        scenario.auth.signInAs(scenario.coachWithoutTeams)
        val otherCoach = repository()
        assertTrue(otherCoach.getMyTeams().isEmpty())
        assertEquals(TeamError.ACCESS_DENIED, failure { otherCoach.getRoster("demo-seniori") })
    }

    @Test fun ownershipChangeIsEnforcedAfterReloadingEncryptedStorage() = runBlocking {
        seedDemoOwner()
        repository().getMyTeams()
        val reassigned = requireNotNull(store.read()).map {
            it.copy(team = it.team.copy(coachId = scenario.coachWithoutTeams.id))
        }
        store.write(reassigned)
        assertTrue(repository().getMyTeams().isEmpty())
        assertEquals(TeamError.ACCESS_DENIED, failure { repository().getRoster("demo-seniori") })
        scenario.auth.signInAs(scenario.coachWithoutTeams)
        assertEquals(2, repository().getMyTeams().size)
    }

    @Test fun corruptStorageIsReportedAndIsNotReplacedWithPermissiveDemoData() = runBlocking {
        seedDemoOwner()
        repository().getMyTeams()
        val file = File(context.noBackupFilesDir, "$prefix-teams.enc")
        file.writeText("deliberately-invalid-test-ciphertext")
        assertEquals(TeamError.DATA_UNAVAILABLE, failure { repository().getMyTeams() })
        assertEquals("deliberately-invalid-test-ciphertext", file.readText())
    }

    private suspend fun seedDemoOwner() {
        accounts.write(listOf(LocalAccount(
            scenario.coach.copy(email = "trener@squadsync.hr"), "test-only-salt", "test-only-hash"
        )))
    }

    private fun repository() = DefaultTeamRepository(scenario.auth, LocalTeamDataSource(
        EncryptedTeamStore(EncryptedFileStore(context, "$prefix-teams.enc")), accounts
    ))

    private suspend fun failure(action: suspend () -> Any?): TeamError {
        try { action(); throw AssertionError("Expected access to fail") }
        catch (exception: TeamException) { return exception.error }
    }
}
